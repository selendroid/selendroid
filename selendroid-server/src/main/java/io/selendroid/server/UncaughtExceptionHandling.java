/*
 * Copyright 2012-2014 eBay Software Foundation and selendroid committers.
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
package io.selendroid.server;

import java.io.File;
import java.io.PrintWriter;

import io.selendroid.server.model.ExternalStorage;
import io.selendroid.server.util.SelendroidLogger;

/**
 * Reports uncaught exceptions to a crash log file on the device so the client can read them.
 */
public class UncaughtExceptionHandling {
  /**
   * Delete existing crash log file.
   */
  public static void clearCrashLogFile() {
    ExternalStorage.getCrashLog().delete();
  }

  /**
   * Handle uncaught exceptions by logging them to the crash log file, then kill the application.
   */
  public static void setGlobalExceptionHandler() {
    Thread.setDefaultUncaughtExceptionHandler(new ExceptionDumper());
  }

  private static class ExceptionDumper implements Thread.UncaughtExceptionHandler {
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
      File crashLogFile = ExternalStorage.getCrashLog();
      String outputPath = crashLogFile.getAbsolutePath();

      PrintWriter pw = null;
      try {
        pw = new PrintWriter(crashLogFile);
        ex.printStackTrace(pw);
        pw.flush();

        SelendroidLogger.info("Process has crashed, log has been written to: " + outputPath);
      } catch (Exception logEx) {
        SelendroidLogger.error("Could not write crash log to: " + outputPath, logEx);
      } finally {
        if (pw != null) {
          pw.close();
        }
      }

      System.exit(-1);
    }
  }
}
