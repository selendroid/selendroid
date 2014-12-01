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

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.CharsetUtil;
import io.selendroid.server.common.http.HttpRequest;

import java.util.HashMap;
import java.util.Map;

public class NettyHttpRequest implements HttpRequest {
  private FullHttpRequest request;
  private Map<String, Object> data;

  public NettyHttpRequest(FullHttpRequest request) {
    this.request = request;
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
