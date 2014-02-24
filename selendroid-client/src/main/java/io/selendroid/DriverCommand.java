package io.selendroid;

public enum DriverCommand {
  SEND_KEYS_TO_ELEMENT(org.openqa.selenium.remote.DriverCommand.SEND_KEYS_TO_ELEMENT);
  DriverCommand(String command) {
    this.command = command;
  }

  public String command;
}
