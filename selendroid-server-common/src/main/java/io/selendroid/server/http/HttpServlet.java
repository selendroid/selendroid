package io.selendroid.server.http;

public interface HttpServlet {
  public void handleHttpRequest(HttpRequest httpRequest, HttpResponse httpResponse)
      throws Exception;
}
