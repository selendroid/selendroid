package io.selendroid.server;

public class JsResult implements Response {
  private String result;

  public JsResult(String result) {
    this.result = result;
  }

  @Override
  public String getSessionId() {
    return "";
  }

  @Override
  public String render() {
    return result;
  }

}
