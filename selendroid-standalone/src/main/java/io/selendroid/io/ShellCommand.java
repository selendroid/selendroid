/*
 * Copyright 2012-2013 eBay Software Foundation and selendroid committers.
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
package io.selendroid.io;

import io.selendroid.exceptions.ShellCommandException;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteResultHandler;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.LogOutputStream;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.exec.environment.EnvironmentUtils;
import org.apache.commons.exec.util.StringUtils;

public class ShellCommand {
  private static final Logger log = Logger.getLogger(ShellCommand.class.getName());
  private static boolean verbose = false;

  public static void setVerbose() {
    verbose = true;
  }

  public static String exec(List<String> command) throws ShellCommandException {
    return exec(command, 20000);
  }

  public static String exec(CommandLine commandline, long timeoutInMillies)
      throws ShellCommandException {
    log.info("executing command: " + commandline);
    PritingLogOutputStream outputStream = new PritingLogOutputStream();
    DefaultExecutor exec = new DefaultExecutor();
    exec.setWatchdog(new ExecuteWatchdog(timeoutInMillies));
    PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
    exec.setStreamHandler(streamHandler);
    try {
      exec.execute(commandline);
    } catch (Exception e) {
      throw new ShellCommandException("An error occured while executing shell command: "
          + commandline, new ShellCommandException(outputStream.getOutput()));
    }
    return (outputStream.getOutput());
  }

  public static String exec(List<String> command, long timeoutInMillies)
      throws ShellCommandException {
    String cmd = StringUtils.toString(command.toArray(new String[command.size()]), " ");

    log.info("executing command: " + cmd);
    CommandLine commandline = CommandLine.parse(cmd);
    return exec(commandline, timeoutInMillies);
  }

  public static void execAsync(List<String> command) throws ShellCommandException {
    execAsync(null, command);
  }

  public static void execAsync(String display, List<String> command) throws ShellCommandException {
    String cmd = StringUtils.toString(command.toArray(new String[command.size()]), " ");

    log.info("executing async command: " + cmd);
    CommandLine commandline = CommandLine.parse(cmd);
    DefaultExecutor exec = new DefaultExecutor();

    ExecuteResultHandler handler = new DefaultExecuteResultHandler();
    PumpStreamHandler streamHandler = new PumpStreamHandler(new PritingLogOutputStream());
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
      throw new ShellCommandException("An error occured while executing shell command: " + cmd, e);
    }
  }

  private static class PritingLogOutputStream extends LogOutputStream {
    private StringBuilder output = new StringBuilder();

    @Override
    protected void processLine(String line, int level) {
      if (verbose) {
        System.out.println("OUTPUT FROM PROCESS: " + line);
      }
      output.append(line).append("\n");
    }

    public String getOutput() {
      return output.toString();
    }
  }
}
