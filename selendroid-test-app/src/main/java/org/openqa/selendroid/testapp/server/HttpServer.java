/*
 * Copyright 2012 selendroid committers.
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
package org.openqa.selendroid.testapp.server;

import java.io.File;
import java.util.Properties;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Represent a simple client server browser app where the user can tell his name and then the server
 * is saying hello.
 * 
 * @author ddary
 * 
 */
public class HttpServer extends NanoHTTPD {
  private boolean running = true;
  private final Lock lock = new ReentrantLock();
  private final Condition shutdownCondition = lock.newCondition();
  private static HttpServer instance = null;

  public synchronized static HttpServer getInstance() {
    if (instance == null) {
      instance = new HttpServer();
    }
    return instance;
  }

  private HttpServer() {
    super(4450, new File("/"));
    System.out.println("HTTP Server started on port 4450");
  }

  public Response serve(String uri, String method, Properties header, Properties parms,
      Properties files) {
    Logger.log("URI: " + uri);
    if (uri.endsWith("/sayhello")) {
      System.out.println("header: " + header);
      System.out.println("parms: " + parms);
      System.out.println("method: " + method);
      StringBuffer html = new StringBuffer();
      String name = parms.getProperty("name");
      html.append("<html><head><title>Hello: " + name + "</title></head>");
      html.append("<body>");
      html.append("<h1>This is my way of saying hello</h1>");
      html.append("<h2>Hello !</h2>");
      html.append("<h3>Your name is:</h3>");
      html.append("&quot;" + name + "&quot;");
      html.append("<br><hr>to start again click <a href='http://localhost:4450/'>here</a>");
      html.append("</body></html>");

      return new NanoHTTPD.Response(HTTP_OK, MIME_HTML, html.toString());
    } else {
      StringBuffer html = new StringBuffer();
      html.append("<html><head><title>Say Hello Demo</title></head>");
      html.append("<body>");
      html.append("Hello, can you please tell me your name?");
      html.append("<form name='myform' action='http://localhost:4450/sayhello' method='get'>");
      html.append("<div align='center'><br><br>");
      html.append("<input type='text' id='name_input' name='name' size='25' value='Enter your name here!'>");
      html.append("<br><input type='submit' value='Send me your name!'><br>");
      html.append("</div></form>");

      html.append("</body></html>");
      return new NanoHTTPD.Response(HTTP_OK, MIME_HTML, html.toString());
    }
  }

  public void waitUntilShutdown() throws InterruptedException {
    lock.lock();
    try {
      while (running) {
        shutdownCondition.await();
      }
    } finally {
      lock.unlock();
    }
  }
}
