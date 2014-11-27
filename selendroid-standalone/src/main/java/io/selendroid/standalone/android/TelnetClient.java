/*
 * Copyright 2014 eBay Software Foundation and selendroid committers.
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
package io.selendroid.standalone.android;

import io.selendroid.standalone.exceptions.AndroidDeviceException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TelnetClient {
  Socket socket = null;
  PrintWriter out = null;
  BufferedReader in = null;
  private Logger log = Logger.getLogger(TelnetClient.class.getName());

  public TelnetClient(Integer port) throws AndroidDeviceException {
    try {
      socket = new Socket("127.0.0.1", port);
      out = new PrintWriter(socket.getOutputStream(), true);
      in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      if (in.readLine() == null) {
        throw new AndroidDeviceException("Cannot establish a connection to device. Error reading from socket.");
      }
    } catch (Exception e) {
      throw new AndroidDeviceException("Cannot establish a connection to device.", e);
    }
  }

  public String sendCommand(String command) {
    try {
      sendQuietly(command);
      // read OK and ignore
      in.readLine();
      return in.readLine();
    } catch (Exception e) {
      String logMessage = String.format("Error reading response for command '%s'", command);
      log.log(Level.WARNING, logMessage, e);
      return "";
    }
  }

  public void sendQuietly(String command) {
    try {
      out.write(command);
      out.write("\r\n");
      out.flush();
    } catch (Exception e) {
      String logMessage = String.format("Error sending command '%s'", command);
      log.log(Level.WARNING, logMessage, e);
    }
  }

  public void close() {
    try {
      out.close();
    } catch (Exception e) {
      // do nothing
    }
    try {
      in.close();
    } catch (Exception e) {
      // do nothing
    }
    try {
      socket.close();
    } catch (Exception e) {
      // do nothing
    }
  }
}
