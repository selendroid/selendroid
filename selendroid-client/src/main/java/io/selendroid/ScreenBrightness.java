package io.selendroid;

/**
 * Allow the user to set the brightness of the screen, turning it on or off as necessary.
 */
public interface ScreenBrightness {
  /**
   * @return The brightness of the screen, with 0% meaning off and 100% being at full brightness.
   */
  int getBrightness();

  /**
   * @param desiredBrightness The brightness to set the screen to, as a percentage.
   */
  void setBrightness(int desiredBrightness);
}
