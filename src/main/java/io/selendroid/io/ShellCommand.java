package io.selendroid.io;

import io.selendroid.exceptions.ShellCommandException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.PumpStreamHandler;

public class ShellCommand {
  public static String exec(String command) throws ShellCommandException {
    System.out.println("executing command: " + command);
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    CommandLine commandline = CommandLine.parse(command);
    DefaultExecutor exec = new DefaultExecutor();
    PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
    exec.setStreamHandler(streamHandler);
    try {
      exec.execute(commandline);
    } catch (ExecuteException e) {
      e.printStackTrace();
      throw new ShellCommandException(e);
    } catch (IOException e) {
      e.printStackTrace();
      throw new ShellCommandException(e);
    }
    return (outputStream.toString());
  }
}
