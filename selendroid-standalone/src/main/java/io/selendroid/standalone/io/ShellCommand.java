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

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
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
    OutputStream os = new ByteArrayOutputStream();
    DefaultExecutor exec = new DefaultExecutor();
    exec.setWatchdog(new ExecuteWatchdog(timeoutInMillies));
    exec.setStreamHandler(new PumpStreamHandler(os));

    try {
      exec.execute(commandline);
    } catch (Exception e) {
      log.log(
        Level.SEVERE,
        String.format("Shell command execution failed: %s", commandline),
        e);

      if (e.getMessage().contains("device offline")) {
        throw new DeviceOfflineException(e);
      }
      throw new ShellCommandException(
          "Error executing shell command: " + commandline, new ShellCommandException(os.toString().trim()));
    }

    String output = os.toString().trim();
    log.log(
      Level.INFO,
      String.format(
        "Shell command executed: %s\n-->\n%s\n<--",
        commandline,
        output));
    return output;
  }

  public static void execAsync(
    CommandLine commandLine) throws ShellCommandException {
    execAsync(null, commandLine, null);
  }

  public static void execAsync(
    CommandLine commandLine,
    Listener listener) throws ShellCommandException {
    execAsync(null, commandLine, listener);
  }

  public static void execAsync(
    String display,
    CommandLine commandLine) throws ShellCommandException {
    execAsync(display, commandLine, null);
  }

  public static void execAsync(
    String display,
    CommandLine commandLine,
    Listener listener) throws ShellCommandException {
    final OutputStream os = new ByteArrayOutputStream();
    try {
      DefaultExecutor exec = new DefaultExecutor();
      exec.setStreamHandler(new PumpStreamHandler(os));

      Map<String, String> env = EnvironmentUtils.getProcEnvironment();
      if (display == null || display.isEmpty()) {
        EnvironmentUtils.addVariableToEnvironment(env, "DISPLAY=:" + display);
      }

      log.log(
        Level.INFO,
        String.format(
          "Executing shell command asynchronously: %s",
          commandLine));

      exec.execute(
        commandLine,
        env,
        new ExecuteResultHandler() {
          @Override
          public void onProcessComplete(int exitValue) {
            String output = os.toString().trim();
            log.log(
              Level.INFO,
              String.format(
                "Shell command executed: %s\n-->\n%s\n<--\n",
                commandLine,
                output));

            if (listener != null) {
              listener.onCommandExecutionFinished(exitValue, output);
            }
          }

          @Override
          public void onProcessFailed(ExecuteException e) {
            String output = os.toString();
            log.log(
              Level.SEVERE,
              String.format(
                "Shell command execution failed: %s\n-->\n%s\n<--\n",
                commandLine,
                output),
              e);

            if (listener != null) {
              listener.onCommandExecutionFailed(e, os.toString());
            }
          }
        });
    } catch (Exception e) {
      String message = "Shell command execution failed: " + commandLine;
      String output = os.toString();

      if (output != null && !output.isEmpty()) {
        message += "\n-->" + output + "\n<--";
      }

      log.log(Level.SEVERE, message, e);
      throw new ShellCommandException(message, e);
    }
  }

  // Listener for async shell command execution
  public static interface Listener {
    // Callback for when the command finished execution
    public void onCommandExecutionFinished(int exitCode, String output);
    // Callback for when the command execution failed, include whatever output
    // we have up to that point
    public void onCommandExecutionFailed(Exception error, String partialOutput);
  }
}
