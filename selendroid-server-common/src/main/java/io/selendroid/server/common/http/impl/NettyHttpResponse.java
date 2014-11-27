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
package io.selendroid.server.common.http.impl;

import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;
import io.selendroid.server.common.http.HttpResponse;

import java.nio.charset.Charset;

public class NettyHttpResponse implements HttpResponse {

  private final FullHttpResponse response;
  private boolean closed = false;
  private Charset charset = CharsetUtil.UTF_8;

  public NettyHttpResponse(FullHttpResponse response) {
    this.response = response;
    response.headers().add("Content-Encoding", "identity");
  }

  public HttpResponse setStatus(int status) {
    response.setStatus(HttpResponseStatus.valueOf(status));
    return this;
  }

  public HttpResponse setContentType(String mimeType) {
    response.headers().add("Content-Type", mimeType);
    return this;
  }

  public HttpResponse setContent(byte[] data) {
    response.headers().add("Content-Length", data.length);
    response.content().writeBytes(data);
    return this;
  }

  public HttpResponse setContent(String message) {
    setContent(message.getBytes(charset));
    return this;
  }

  public HttpResponse sendRedirect(String to) {
    setStatus(301);
    response.headers().add("location", to);
    return this;
  }

  public HttpResponse sendTemporaryRedirect(String to) {
    setStatus(302);
    response.headers().add("location", to);
    return this;
  }

  @Override
  public void end() {
    closed = true;
  }

  public boolean isClosed() {
    return closed;
  }

  @Override
  public HttpResponse setEncoding(Charset charset) {
    this.charset = charset;
    return this;
  }
}
