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
package io.selendroid.server.common.http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

public class HttpServer {
  private int port;
  private Thread serverThread;
  private final List<HttpServlet> handlers = new ArrayList<HttpServlet>();

  public HttpServer(int port) {
    this.port = port;
  }

  public void addHandler(HttpServlet handler) {
    handlers.add(handler);
  }

  public void start() {
    if (serverThread != null) {
      throw new IllegalStateException("Server is already running");
    }
    final SynchronousQueue<Integer> queue = new SynchronousQueue<Integer>();
    serverThread = new Thread() {
      @Override
      public void run() {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
          ServerBootstrap bootstrap = new ServerBootstrap();
          bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
          bootstrap.group(bossGroup, workerGroup)
              .channel(NioServerSocketChannel.class)
              .childHandler(new ServerInitializer(handlers));

          Channel ch = bootstrap.bind(port).sync().channel();

          // Should always be an InetSockedAddress, because we call bind()
          if (!(ch.localAddress() instanceof  InetSocketAddress)) {
            throw new RuntimeException("Expected an InetSocketAddress");
          }

          int startedPort = ((InetSocketAddress) ch.localAddress()).getPort();
          queue.put(startedPort);
          ch.closeFuture().sync();
        } catch (InterruptedException ignored) {
        } finally {
          bossGroup.shutdownGracefully();
          workerGroup.shutdownGracefully();
          TrafficCounter.shutdown();
        }
      }
    };
    serverThread.start();

    try {
      Integer startedPort = queue.poll(20, TimeUnit.SECONDS);
      if (startedPort == null) {
        throw new RuntimeException("Exceeded timeout while waiting for webserver to start.");
      }
      port = startedPort;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("Failure while waiting for port when starting webserver.");
    }
  }

  public void stop() {
    if (serverThread == null) {
      throw new IllegalStateException("Server is not running");
    }
    serverThread.interrupt();
  }

  public int getPort() {
    return port;
  }

}
