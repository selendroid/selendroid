/*
 * Copyright 2013 selendroid committers.
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

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;

public class ShellCommand {
  private static final Logger log = Logger.getLogger(ShellCommand.class.getName());

  public static String exec(String command) throws ShellCommandException {
    log.info("executing command: " + command);
    OutputStream outputStream = new ByteArrayOutputStream();

    CommandLine commandline = CommandLine.parse(command);
    DefaultExecutor exec = new DefaultExecutor();
    PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
    exec.setStreamHandler(streamHandler);
    try {
      exec.execute(commandline);
    } catch (Exception e) {
      throw new ShellCommandException("An error occured while executing shell command: "
          + outputStream.toString());
    }
    return (outputStream.toString());
  }
}
