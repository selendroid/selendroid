package io.selendroid.client.adb;

/**
 * Allow the user to interact with the device based on the adb connection.
 */
public interface AdbConnection {
  /**
   * Sends text to the Android device/emulator. This is equivalent to running
   * {@code adb shell input text (TEXT)} on the command line.
   * 
   * @param text to send to the device/emulator
   */
  public void sendText(String text);


  /**
   * Sends key events to the Android device/emulator. This is equivalent to running
   * {@code adb shell input keyevent (KEYCODE)} on the command line.
   * 
   * @param keyCode to send to device/emulator.
   * @see <a href="http://developer.android.com/reference/android/view/KeyEvent.html#KEYCODE_0">KEYCODE_ constants</a>
   */
  public void sendKeyEvent(int keyCode);

  /**
   * Sends key event tap to the Android device/emulator. This is equivalent to running
   * {@code adb shell input tap (x) (y)} on the command line.
   * 
   * @param x X coordinate to tap on.
   * @param y Y coordinate to tap on.
   */
  public void tap(int x, int y);

  /**
   * Sends the @command to the emulator.
   * 
   * @param command The command to execute on the device under test.
   * @return command execution output
   */
  public String executeShellCommand(String command);
}
