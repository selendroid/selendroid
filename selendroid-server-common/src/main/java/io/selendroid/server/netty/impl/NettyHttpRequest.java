package io.selendroid.server.netty.impl;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.CharsetUtil;
import io.selendroid.server.http.HttpRequest;

import java.util.HashMap;
import java.util.Map;

public class NettyHttpRequest implements HttpRequest {
  private FullHttpRequest request;
  private Map<String, Object> data;

  public NettyHttpRequest(FullHttpRequest reuqest) {
    this.request = reuqest;
    this.data = new HashMap<String, Object>();
  }

  @Override
  public String method() {
    return request.getMethod().name();
  }

  @Override
  public String uri() {
    return request.getUri();
  }

  @Override
  public String body() {
    return request.content().toString(CharsetUtil.UTF_8);
  }

  @Override
  public String header(String name) {
    return request.headers().get(name);
  }

  @Override
  public Map<String, Object> data() {
    return data;
  }
}
