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

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteResultHandler;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.LogOutputStream;
import org.apache.commons.exec.PumpStreamHandler;
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
    log.info("executing async command: " + commandline);
    DefaultExecutor exec = new DefaultExecutor();

    ExecuteResultHandler handler = new DefaultExecuteResultHandler();
    PumpStreamHandler streamHandler = new PumpStreamHandler(new PrintingLogOutputStream());
    exec.setStreamHandler(streamHandler);
    try {
      if (display == null || display.isEmpty()) {
        exec.execute(commandline, handler);
      } else {
        Map env = EnvironmentUtils.getProcEnvironment();
        EnvironmentUtils.addVariableToEnvironment(env, "DISPLAY=:" + display);

        exec.execute(commandline, env, handler);
      }
    } catch (Exception e) {
      log.log(Level.SEVERE, "Error executing command: " + commandline, e);
      throw new ShellCommandException("Error executing shell command: " + commandline, e);
    }
  }

  private static class PrintingLogOutputStream extends LogOutputStream {

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
