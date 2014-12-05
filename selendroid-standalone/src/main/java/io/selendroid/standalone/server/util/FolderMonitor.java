/*
 * Copyright 2012-2013 eBay Software Foundation and selendroid committers
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package io.selendroid.standalone.server.util;

import io.selendroid.standalone.SelendroidConfiguration;
import io.selendroid.standalone.exceptions.AndroidSdkException;
import io.selendroid.standalone.server.model.SelendroidStandaloneDriver;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.logging.Logger;

public class FolderMonitor implements Runnable {

  private static final Logger log = Logger.getLogger(FolderMonitor.class.getName());
  private SelendroidConfiguration selendroidConfiguration;
  private SelendroidStandaloneDriver selendroidStandaloneDriver;
  private WatchService folderWatcher;
  private final Object stoppedLock;
  private boolean stopped;
  private Thread thread;

  public FolderMonitor(SelendroidStandaloneDriver selendroidStandaloneDriver, SelendroidConfiguration selendroidConfiguration)
      throws IOException {
    this.selendroidStandaloneDriver = selendroidStandaloneDriver;
    this.selendroidConfiguration = selendroidConfiguration;
    stoppedLock = new Object();
    stopped = false;
    init();
    folderWatcher = FileSystems.getDefault().newWatchService();
    Path watchedFolder = Paths.get(selendroidConfiguration.getAppFolderToMonitor());
    try {
      watchedFolder.register(folderWatcher, StandardWatchEventKinds.ENTRY_CREATE,
                             StandardWatchEventKinds.ENTRY_MODIFY,
                             StandardWatchEventKinds.ENTRY_DELETE);
    } catch (NoSuchFileException e) {
      stop();
      log.warning("invalid location: " + new File(selendroidConfiguration.getAppFolderToMonitor())
          .getAbsolutePath());
    }
  }

  private void init() {
    File[] listOfFiles = new File(selendroidConfiguration.getAppFolderToMonitor()).listFiles();
    if (listOfFiles == null) {
      return;
    }
    for (File file : listOfFiles) {
      if (isResigned(file)) {
          file.delete();
      } else if (isApp(file)) {
        addApplication(file);
      }
    }
  }

  @Override
  public void run() {
    synchronized (stoppedLock) {
      while (!stopped) {
        checkForChanges();
        try {
          stoppedLock.wait(1000, 0);
        } catch (InterruptedException ignore) {
        }
      }
    }
  }

  private void checkForChanges() {
    final WatchKey key = folderWatcher.poll();

    if (key != null) {
      for (WatchEvent<?> watchEvent : key.pollEvents()) {
        final Path filePath = (Path) watchEvent.context();
        final WatchEvent.Kind<?> kind = watchEvent.kind();
        log.fine(kind + " : " + filePath);
        handleFileChange(kind,
                         new File(selendroidConfiguration.getAppFolderToMonitor(),
                                  filePath.getFileName().toString()));
      }

      boolean valid = key.reset();
      if (!valid) {
        log.warning("Cannot monitor this folder anymore. Has it been deleted?");
        stop();
      }
    }
  }

  private void handleFileChange(WatchEvent.Kind kind, File file) {
    if (kind.equals(StandardWatchEventKinds.ENTRY_CREATE)) {
      if (isApp(file)) {
        // new file but only added to the list only 
        // if they are not a resigned files.
        if(!isResigned(file)) {
            log.info("New app found! " + file.getName());
            addToAppStore(file);
        }
      }
    } else if (kind.equals(StandardWatchEventKinds.ENTRY_MODIFY)) {
      log.info("App modified - no handler implemented!");
    } else if (kind.equals(StandardWatchEventKinds.ENTRY_DELETE)) {
      log.info("App deleted - no handler implemented!");
    }
  }

  private void addApplication(File file) {
    String app;
    if (isApp(file)) {
      if (!isResigned(file)) {
        app = file.getAbsolutePath();
        selendroidConfiguration.addSupportedApp(app);
        log.info("File added to supported list:\n\t" + app);
      }
    }
  }

  private void addToAppStore(File file) {
    String app;
    if (isApp(file)) {
      app = file.getAbsolutePath();
      try {
        selendroidStandaloneDriver.addToAppsStore(file);
        log.info("File added to app store:\n\t" + app);
      }catch (AndroidSdkException e) {
        log.info("An error occurred while accessing the details of'" + file.getName()
                + "'. ");
      }
    }
  }

  private boolean isApp(File file) {
    if (file != null) {
      return file.getAbsolutePath().endsWith(".apk");
    }
    return false;
  }

  private boolean isResigned(File file) {
    return (isApp(file) && file.getAbsolutePath().contains("resigned"));
  }

  public void start() {
    thread = new Thread(this);
    thread.start();
    log.info("The Folder Monitor has been started with '"
        + selendroidConfiguration.getAppFolderToMonitor()
        + "'. New apps in this folder will be avalilable for testing immediately.");
  }

  public void stop() {
    synchronized (stoppedLock) {
      stopped = true;
    }
    try {
      if (thread != null) {
        thread.join();
        thread = null;
      }
    } catch (InterruptedException ignore) {
    }
  }
}
