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
package io.selendroid.standalone.io;

import io.selendroid.standalone.exceptions.DeviceOfflineException;
import io.selendroid.standalone.exceptions.ShellCommandException;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.exec.*;
import org.apache.commons.exec.environment.EnvironmentUtils;

public class ShellCommand {

  private static final Logger log = Logger.getLogger(ShellCommand.class.getName());

  public static String exec(CommandLine commandLine) throws ShellCommandException {
    return exec(commandLine, 20000);
  }

  public static String exec(CommandLine commandline, long timeoutInMillies)
      throws ShellCommandException {
    log.info("Executing shell command: " + commandline);
    PrintingLogOutputStream outputStream = new PrintingLogOutputStream();
    DefaultExecutor exec = new DefaultExecutor();
    exec.setWatchdog(new ExecuteWatchdog(timeoutInMillies));
    PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
    exec.setStreamHandler(streamHandler);
    try {
      exec.execute(commandline);
    } catch (Exception e) {
      log.log(Level.SEVERE, "Error executing command: " + commandline, e);
      if (e.getMessage().contains("device offline")) {
        throw new DeviceOfflineException(e);
      }
      throw new ShellCommandException(
          "Error executing shell command: " + commandline, new ShellCommandException(outputStream.getOutput()));
    }
    String result = outputStream.getOutput().trim();
    log.info("Shell command output\n-->\n" + result + "\n<--");
    return result;
  }

  public static void execAsync(CommandLine commandline) throws ShellCommandException {
    execAsync(null, commandline);
  }

  public static void execAsync(String display, CommandLine commandline)
          throws ShellCommandException {
    execAsync(display, commandline, null, null);
  }

  public static void execAsync(String display,
                               CommandLine commandline,
                               ExecuteStreamHandler streamHandler,
                               ExecuteResultHandler resultHandler)
          throws ShellCommandException {
    log.info("executing async command: " + commandline);
    DefaultExecutor exec = new DefaultExecutor();

    if (resultHandler == null) {
      resultHandler = new DefaultExecuteResultHandler();
    }
    if (streamHandler == null) {
      streamHandler = new PumpStreamHandler(new PrintingLogOutputStream());
    }
    exec.setStreamHandler(streamHandler);
    try {
      if (display == null || display.isEmpty()) {
        exec.execute(commandline, resultHandler);
      } else {
        Map env = EnvironmentUtils.getProcEnvironment();
        EnvironmentUtils.addVariableToEnvironment(env, "DISPLAY=:" + display);

        exec.execute(commandline, env, resultHandler);
      }
    } catch (Exception e) {
      log.log(Level.SEVERE, "Error executing command: " + commandline, e);
      throw new ShellCommandException("Error executing shell command: " + commandline, e);
    }
  }

  public static class PrintingLogOutputStream extends LogOutputStream {

    private StringBuilder output = new StringBuilder();

    @Override
    protected void processLine(String line, int level) {
      log.fine("OUTPUT FROM PROCESS: " + line);

      output.append(line).append("\n");
    }

    public String getOutput() {
      return output.toString();
    }
  }
}
