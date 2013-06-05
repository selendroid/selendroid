package io.selendroid.server;



public interface Response {

  public abstract String getSessionId();

  public abstract String render();

}
