package io.selendroid;

import org.openqa.selenium.remote.CommandInfo;
import org.openqa.selenium.remote.HttpCommandExecutor;
import org.openqa.selenium.remote.HttpVerb;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class SelendroidCommandExecutor extends HttpCommandExecutor {

  private final static Map<String, CommandInfo> SELENDROID_COMMANDS = new HashMap<String, CommandInfo>() {{
    put("selendroid-getBrightness", new CommandInfo("-selendroid/:sessionId/screen/brightness", HttpVerb.GET));
    put("selendroid-setBrightness", new CommandInfo("-selendroid/:sessionId/screen/brightness", HttpVerb.POST));
  }};

  public SelendroidCommandExecutor(String url) throws MalformedURLException {
    super(SELENDROID_COMMANDS, new URL(url));
  }
}
