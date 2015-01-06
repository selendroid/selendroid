/*
 * Copyright 2014 Selenium committers Copyright 2012 Software Freedom Conservancy
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

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

import java.util.List;

public class ServerInitializer extends ChannelInitializer<SocketChannel> {

  private final List<HttpServlet> handlers;

  public ServerInitializer(List<HttpServlet> handlers) {
    this.handlers = handlers;
  }

  @Override
  public void initChannel(SocketChannel ch) throws Exception {
    ChannelPipeline pipeline = ch.pipeline();
    pipeline.addLast("codec", new HttpServerCodec());
    pipeline.addLast("aggregator", new HttpObjectAggregator(65536));
    pipeline.addLast("shaper", TrafficCounter.getShaper());
    pipeline.addLast("handler", new ServerHandler(handlers));
  }
}
