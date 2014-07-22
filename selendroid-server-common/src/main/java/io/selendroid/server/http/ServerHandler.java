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
package io.selendroid.server.http;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.selendroid.server.http.impl.NettyHttpRequest;
import io.selendroid.server.http.impl.NettyHttpResponse;

import java.util.List;

public class ServerHandler extends ChannelInboundHandlerAdapter {

  private List<HttpServlet> httpHandlers;

  public ServerHandler(List<HttpServlet> handlers) {
    this.httpHandlers = handlers;
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    if (!(msg instanceof FullHttpRequest)) {
      return;
    }

    FullHttpRequest request = (FullHttpRequest) msg;
    FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK);

    HttpRequest httpRequest = new NettyHttpRequest(request);
    HttpResponse httpResponse = new NettyHttpResponse(response);

    for (HttpServlet handler : httpHandlers) {
      handler.handleHttpRequest(httpRequest, httpResponse);
      if (httpResponse.isClosed()) {
        break;
      }
    }

    if (!httpResponse.isClosed()) {
      httpResponse.setStatus(404);
      httpResponse.end();
    }

    ctx.write(response).addListener(ChannelFutureListener.CLOSE);
  }

  @Override
  public void channelReadComplete(ChannelHandlerContext ctx) {
    ctx.flush();
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    cause.printStackTrace();
    ctx.close();
  }
}
