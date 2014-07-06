package io.selendroid.server.http;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.selendroid.server.netty.impl.NettyHttpRequest;
import io.selendroid.server.netty.impl.NettyHttpResponse;

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
