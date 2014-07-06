package io.selendroid.server.http;

import java.util.Map;

public interface HttpRequest {
  String method();

  String uri();

  String body();

  String header(String name);
  
  Map<String, Object> data();
}
