package io.selendroid.server.http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.ArrayList;
import java.util.List;



public class HttpServer {
  private final int port;
  private Thread serverThread;
  private final List<HttpServlet> handlers = new ArrayList<HttpServlet>();

  public HttpServer(int port) {
    this.port = port;
  }

  public void addHandler(HttpServlet handler) {
    handlers.add(handler);
  }

  public void setStaleConnectionTimeout(long ignored) {
    // TODO: Do we need this? Currently we use timeout = 1 week.
  }

  public void start() {
    if (serverThread != null) {
      throw new IllegalStateException("Server is already running");
    }
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
          ch.closeFuture().sync();
        } catch (InterruptedException ignored) {
        } finally {
          bossGroup.shutdownGracefully();
          workerGroup.shutdownGracefully();
        }
      }
    };
    serverThread.start();
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
