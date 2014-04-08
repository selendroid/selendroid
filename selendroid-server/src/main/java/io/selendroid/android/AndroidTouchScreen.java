/*
 * Copyright 2011 Selenium committers
 * 
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

package io.selendroid.android;

import android.app.Instrumentation;
import android.content.Context;
import android.os.PowerManager;
import android.os.SystemClock;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.MotionEvent.PointerProperties;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.WindowManager;
import android.view.MotionEvent.PointerCoords;
import android.webkit.WebView;
import android.widget.AbsListView;
import android.widget.ScrollView;
import io.selendroid.ServerInstrumentation;
import io.selendroid.android.internal.Point;
import io.selendroid.exceptions.SelendroidException;
import io.selendroid.server.model.TouchScreen;
import io.selendroid.server.model.interactions.Coordinates;
import io.selendroid.util.SelendroidLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * Implements touch capabilities of a device.
 * 
 */
public class AndroidTouchScreen implements TouchScreen {
  private static final int MOTION_EVENT_INJECTION_DELAY_MILLIS = 5;
  private final ServerInstrumentation instrumentation;
  private final MotionSender motions;

  public AndroidTouchScreen(ServerInstrumentation instrumentation, MotionSender motions) {
    this.instrumentation = instrumentation;
    this.motions = motions;
  }

  public void singleTap(Coordinates where) {
    Point toTap = where.getLocationOnScreen();
    List<MotionEvent> motionEvents = new ArrayList<MotionEvent>();
    long downTime = SystemClock.uptimeMillis();
    motionEvents.add(getMotionEvent(downTime, downTime, MotionEvent.ACTION_DOWN, toTap));
    motionEvents.add(getMotionEvent(downTime, downTime, MotionEvent.ACTION_UP, toTap));
    motions.send(motionEvents);
  }

  public void down(int x, int y) {
    List<MotionEvent> event = new ArrayList<MotionEvent>();
    long downTime = SystemClock.uptimeMillis();
    Point coords = new Point(x, y);
    event.add(getMotionEvent(downTime, downTime, MotionEvent.ACTION_DOWN, coords));
    motions.send(event);
  }

  public void up(int x, int y) {
    List<MotionEvent> event = new ArrayList<MotionEvent>();
    long downTime = SystemClock.uptimeMillis();
    Point coords = new Point(x, y);
    event.add(getMotionEvent(downTime, downTime, MotionEvent.ACTION_UP, coords));
    motions.send(event);
  }

  public void move(int x, int y) {
    List<MotionEvent> event = new ArrayList<MotionEvent>();
    long downTime = SystemClock.uptimeMillis();
    Point coords = new Point(x, y);
    event.add(getMotionEvent(downTime, downTime, MotionEvent.ACTION_MOVE, coords));
    motions.send(event);
  }

  public void scroll(Coordinates where, int xOffset, int yOffset) {
    long downTime = SystemClock.uptimeMillis();
    List<MotionEvent> motionEvents = new ArrayList<MotionEvent>();
    Point origin = where.getLocationOnScreen();
    Point destination = new Point(origin.x + xOffset, origin.y + yOffset);
    motionEvents.add(getMotionEvent(downTime, downTime, MotionEvent.ACTION_DOWN, origin));

    Scroll scroll = new Scroll(origin, destination, downTime);
    // Initial acceleration from origin to reference point
    motionEvents.addAll(getMoveEvents(downTime, downTime, origin, scroll.getDecelerationPoint(),
        Scroll.INITIAL_STEPS, Scroll.TIME_BETWEEN_EVENTS));
    // Deceleration phase from reference point to destination
    motionEvents.addAll(getMoveEvents(downTime, scroll.getEventTimeForReferencePoint(),
        scroll.getDecelerationPoint(), destination, Scroll.DECELERATION_STEPS,
        Scroll.TIME_BETWEEN_EVENTS));

    motionEvents.add(getMotionEvent(downTime,
        (downTime + scroll.getEventTimeForDestinationPoint()), MotionEvent.ACTION_UP, destination));
    motions.send(motionEvents);
  }

  public void doubleTap(Coordinates where) {
    Point toDoubleTap = where.getLocationOnScreen();
    List<MotionEvent> motionEvents = new ArrayList<MotionEvent>();
    long downTime = SystemClock.uptimeMillis();
    motionEvents.add(getMotionEvent(downTime, downTime, MotionEvent.ACTION_DOWN, toDoubleTap));
    motionEvents.add(getMotionEvent(downTime, downTime, MotionEvent.ACTION_UP, toDoubleTap));
    motionEvents.add(getMotionEvent(downTime, downTime, MotionEvent.ACTION_DOWN, toDoubleTap));
    motionEvents.add(getMotionEvent(downTime, downTime, MotionEvent.ACTION_UP, toDoubleTap));
    motions.send(motionEvents);
  }

  public void longPress(Coordinates where) {
    long downTime = SystemClock.uptimeMillis();
    long eventTime = SystemClock.uptimeMillis();
    Point point = where.getLocationOnScreen();
    // List<MotionEvent> motionEvents = new ArrayList<MotionEvent>();
    //
    // motionEvents.add(getMotionEvent(downTime, downTime, MotionEvent.ACTION_DOWN, point));
    // motionEvents.add(getMotionEvent(downTime, (downTime + 3000), MotionEvent.ACTION_UP, point));
    // sendMotionEvents(motionEvents);
    Instrumentation inst = instrumentation;


    MotionEvent event = null;
    boolean successfull = false;
    int retry = 0;
    while (!successfull && retry < 10) {
      try {
        if (event == null) {
          event =
              MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, point.x, point.y, 0);
        }
        SelendroidLogger.debug("trying to send pointer");
        inst.sendPointerSync(event);
        successfull = true;
      } catch (SecurityException e) {
        SelendroidLogger.error("failed: " + retry);
        // activityUtils.hideSoftKeyboard(null, false, true);
        retry++;
      }
    }
    if (!successfull) {
      throw new SelendroidException("Click can not be completed!");
    }
    inst.sendPointerSync(event);
    inst.waitForIdleSync();

    eventTime = SystemClock.uptimeMillis();
    final int touchSlop = ViewConfiguration.get(inst.getTargetContext()).getScaledTouchSlop();
    event =
        MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, point.x + touchSlop / 2,
            point.y + touchSlop / 2, 0);
    inst.sendPointerSync(event);
    inst.waitForIdleSync();

    try {
      Thread.sleep((long) (ViewConfiguration.getLongPressTimeout() * 1.5f));
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    eventTime = SystemClock.uptimeMillis();
    event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_UP, point.x, point.y, 0);
    inst.sendPointerSync(event);
    inst.waitForIdleSync();
  }

  public void scroll(final int xOffset, final int yOffset) {
    List<View> scrollableContainer =
        ViewHierarchyAnalyzer.getDefaultInstance().findScrollableContainer();

    if (scrollableContainer == null) {
      // nothing to do
      return;
    }
    for (View view : scrollableContainer) {
      if (view instanceof AbsListView) {
        final AbsListView absListView = (AbsListView) view;
        instrumentation.getCurrentActivity().runOnUiThread(new Runnable() {
          public void run() {
            absListView.scrollBy(xOffset, yOffset);
          }
        });
      } else if (view instanceof ScrollView) {
        final ScrollView scrollView = (ScrollView) view;
        instrumentation.getCurrentActivity().runOnUiThread(new Runnable() {
          public void run() {
            scrollView.scrollBy(xOffset, yOffset);
          }
        });
      } else if (view instanceof WebView) {
        final WebView webView = (WebView) view;
        instrumentation.getCurrentActivity().runOnUiThread(new Runnable() {
          public void run() {
            webView.scrollBy(xOffset, yOffset);
          }
        });
      }
    }
  }

  public void flick(final int speedX, final int speedY) {
    List<View> scrollableContainer =
        ViewHierarchyAnalyzer.getDefaultInstance().findScrollableContainer();

    if (scrollableContainer == null) {
      // nothing to do
      return;
    }
    for (View view : scrollableContainer) {
      if (view instanceof AbsListView) {
        // ignore
      } else if (view instanceof ScrollView) {
        final ScrollView scrollView = (ScrollView) view;
        instrumentation.getCurrentActivity().runOnUiThread(new Runnable() {
          public void run() {
            scrollView.fling(speedY);
          }
        });
      } else if (view instanceof WebView) {
        final WebView webView = (WebView) view;
        instrumentation.getCurrentActivity().runOnUiThread(new Runnable() {
          public void run() {
            webView.flingScroll(speedX, speedY);
          }
        });
      }
    }
  }

  public void flick(Coordinates where, int xOffset, int yOffset, int speed) {
    long downTime = SystemClock.uptimeMillis();
    List<MotionEvent> motionEvents = new ArrayList<MotionEvent>();
    Point origin = where.getLocationOnScreen();
    Point destination = new Point(origin.x + xOffset, origin.y + yOffset);
    Flick flick = new Flick(speed);
    motionEvents.add(getMotionEvent(downTime, downTime, MotionEvent.ACTION_DOWN, origin));
    motionEvents.addAll(getMoveEvents(downTime, downTime, origin, destination, flick.getNumberOfSteps(),
        flick.getTimeBetweenEvents()));
    motionEvents.add(getMotionEvent(downTime, flick.getTimeForDestinationPoint(downTime),
        MotionEvent.ACTION_UP, destination));
    motions.send(motionEvents);
  }

  private MotionEvent getMotionEvent(long start, long eventTime, int action, Point coords) {
    return MotionEvent.obtain(start, eventTime, action, coords.x, coords.y, 0);
  }

  private List<MotionEvent> getMoveEvents(long downTime, long startingEVentTime, Point origin,
      Point destination, int steps, long timeBetweenEvents) {
    List<MotionEvent> move = new ArrayList<MotionEvent>();
    MotionEvent event;

    float xStep = (destination.x - origin.x) / steps;
    float yStep = (destination.y - origin.y) / steps;
    float x = origin.x;
    float y = origin.y;
    long eventTime = startingEVentTime;

    for (int i = 0; i < steps - 1; i++) {
      x += xStep;
      y += yStep;
      eventTime += timeBetweenEvents;
      event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, x, y, 0);
      move.add(event);
    }

    eventTime += timeBetweenEvents;
    move.add(getMotionEvent(downTime, eventTime, MotionEvent.ACTION_MOVE, destination));
    return move;
  }

  @Override
  public float getBrightness() {
    PowerManager powerManager = (PowerManager) instrumentation.getContext().getSystemService(Context.POWER_SERVICE);

    if (!powerManager.isScreenOn()) {
      return 0f;
    } else {
      WindowManager.LayoutParams attributes = instrumentation.getCurrentActivity().getWindow().getAttributes();
      return attributes.screenBrightness;
    }
  }

  @Override
  public void setBrightness(float brightness) {
    if (brightness < 0) {
      brightness = 0;
    }
    if (brightness > 1) {
      brightness = 1;
    }

    PowerManager powerManager = (PowerManager) instrumentation.getContext().getSystemService(Context.POWER_SERVICE);
    final Window window = instrumentation.getCurrentActivity().getWindow();
    final WindowManager.LayoutParams attributes = window.getAttributes();
    PowerManager.WakeLock wakeLock = null;
    if (brightness != 0) {
      // Turn on display
      if (!powerManager.isScreenOn()) {
        wakeLock = powerManager.newWakeLock(
            PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "Selendroid screen wake");
      }
      // Now set the brightness
      attributes.screenBrightness = brightness;
    } else {
      // Turn off the display. Oh boy. This is derived from a reading of the PowerManager SDK docs.
      // http://developer.android.com/reference/android/os/PowerManager.html
      attributes.screenBrightness = 0;
      wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Selendroid screen sleep");
    }

    instrumentation.getCurrentActivity().runOnUiThread(
        new Runnable() {
          @Override public void run() {
            window.setAttributes(attributes);
          }
        }
    );
    instrumentation.waitForIdleSync();

    if (wakeLock != null) {
      try {
        wakeLock.acquire();
        wakeLock.release();
      } catch (SecurityException ignored) {
        // We can only turn off the screen if the AUT has the android.permission.WAKE_LOCK permission.
      }
    }
  }

  final class Scroll {

    private Point origin;
    private Point destination;
    private long downTime;
    // A regular scroll usually has 15 gestures, where the last 5 are used for deceleration
    final static int INITIAL_STEPS = 10;
    final static int DECELERATION_STEPS = 5;
    final int TOTAL_STEPS = INITIAL_STEPS + DECELERATION_STEPS;
    // Time in milliseconds to provide a speed similar to scroll
    final static long TIME_BETWEEN_EVENTS = 50;

    public Scroll(Point origin, Point destination, long downTime) {
      this.origin = origin;
      this.destination = destination;
      this.downTime = downTime;
    }

    // This method is used to calculate the point where the deceleration will start at 20% of the
    // distance to the destination point
    private Point getDecelerationPoint() {
      int deltaX = (destination.x - origin.x);
      int deltaY = (destination.y - origin.y);
      // Coordinates of reference point where deceleration should start for scroll gesture, on the
      // last 20% of the total distance to scroll
      int xRef = (int) (deltaX * 0.8);
      int yRef = (int) (deltaY * 0.8);
      return new Point(origin.x + xRef, origin.y + yRef);
    }

    private long getEventTimeForReferencePoint() {
      return (downTime + INITIAL_STEPS * TIME_BETWEEN_EVENTS);
    }

    private long getEventTimeForDestinationPoint() {
      return (downTime + TOTAL_STEPS * TIME_BETWEEN_EVENTS);
    }
  }

  final class Flick {

    private final int SPEED_NORMAL = 0;
    private final int SPEED_FAST = 1;
    private final int SPEED_SLOW = 2;

    private int speed;

    public Flick(int speed) {
      this.speed = speed;
    }

    public int getNumberOfSteps() {
      return speed == SPEED_SLOW ? 8 : 4;
    }

    private long getTimeBetweenEvents() {
      switch (speed) {
        case SPEED_SLOW:
          return 50;

        case SPEED_NORMAL:
          return 25; // Time in milliseconds to provide a speed similar to normal flick

        case SPEED_FAST:
          return 9;

        default:
          return 0;
      }
    }

    private long getTimeForDestinationPoint(long downTime) {
      return (downTime + getNumberOfSteps() * getTimeBetweenEvents());
    }
  }

  /**
   * Generates a two-pointer gesture with arbitrary starting and ending points.
   *
   * @param startPoint1 start point of pointer 1
   * @param startPoint2 start point of pointer 2
   * @param endPoint1 end point of pointer 1
   * @param endPoint2 end point of pointer 2
   * @param steps the number of steps for the gesture. Steps are injected
   * about 5 milliseconds apart, so 100 steps may take around 0.5 seconds to complete.
   * @return none
   */
  public void performTwoPointerGesture(Point startPoint1, Point startPoint2, Point endPoint1,
          Point endPoint2, int steps) {

      // avoid a divide by zero
      if(steps == 0)
          steps = 1;

      final float stepX1 = (endPoint1.x - startPoint1.x) / steps;
      final float stepY1 = (endPoint1.y - startPoint1.y) / steps;
      final float stepX2 = (endPoint2.x - startPoint2.x) / steps;
      final float stepY2 = (endPoint2.y - startPoint2.y) / steps;

      int eventX1, eventY1, eventX2, eventY2;
      eventX1 = startPoint1.x;
      eventY1 = startPoint1.y;
      eventX2 = startPoint2.x;
      eventY2 = startPoint2.y;

      // allocate for steps plus first down and last up
      PointerCoords[] points1 = new PointerCoords[steps + 2];
      PointerCoords[] points2 = new PointerCoords[steps + 2];

      // Include the first and last touch downs in the arrays of steps
      for (int i = 0; i < steps + 1; i++) {
          PointerCoords p1 = new PointerCoords();
          p1.x = eventX1;
          p1.y = eventY1;
          p1.pressure = 1;
          p1.size = 1;
          points1[i] = p1;

          PointerCoords p2 = new PointerCoords();
          p2.x = eventX2;
          p2.y = eventY2;
          p2.pressure = 1;
          p2.size = 1;
          points2[i] = p2;

          eventX1 += stepX1;
          eventY1 += stepY1;
          eventX2 += stepX2;
          eventY2 += stepY2;
      }

      // ending pointers coordinates
      PointerCoords p1 = new PointerCoords();
      p1.x = endPoint1.x;
      p1.y = endPoint1.y;
      p1.pressure = 1;
      p1.size = 1;
      points1[steps + 1] = p1;

      PointerCoords p2 = new PointerCoords();
      p2.x = endPoint2.x;
      p2.y = endPoint2.y;
      p2.pressure = 1;
      p2.size = 1;
      points2[steps + 1] = p2;

      performMultiPointerGesture(points1, points2);
  }

  /**
   * Performs a multi-touch gesture
   *
   * Takes a series of touch coordinates for at least 2 pointers. Each pointer must have
   * all of its touch steps defined in an array of {@link PointerCoords}. By having the ability
   * to specify the touch points along the path of a pointer, the caller is able to specify
   * complex gestures like circles, irregular shapes etc, where each pointer may take a
   * different path.
   *
   * To create a single point on a pointer's touch path
   * <code>
   *       PointerCoords p = new PointerCoords();
   *       p.x = stepX;
   *       p.y = stepY;
   *       p.pressure = 1;
   *       p.size = 1;
   * </code>
   * @param touches each array of {@link PointerCoords} constitute a single pointer's touch path.
   *        Multiple {@link PointerCoords} arrays constitute multiple pointers, each with its own
   *        path. Each {@link PointerCoords} in an array constitute a point on a pointer's path.
   * @return none
   */
  public void performMultiPointerGesture(PointerCoords[] ... touches) {
      if (touches.length < 2) {
          throw new IllegalArgumentException("Must provide coordinates for at least 2 pointers");
      }

      // Get the pointer with the max steps to inject.
      int maxSteps = 0;
      for (int x = 0; x < touches.length; x++)
          maxSteps = (maxSteps < touches[x].length) ? touches[x].length : maxSteps;

      // specify the properties for each pointer as finger touch
      PointerProperties[] properties = new PointerProperties[touches.length];
      PointerCoords[] pointerCoords = new PointerCoords[touches.length];
      for (int x = 0; x < touches.length; x++) {
          PointerProperties prop = new PointerProperties();
          prop.id = x;
          prop.toolType = MotionEvent.TOOL_TYPE_FINGER;
          properties[x] = prop;

          // for each pointer set the first coordinates for touch down
          pointerCoords[x] = touches[x][0];
      }

      // Touch down all pointers
      long downTime = SystemClock.uptimeMillis();
      MotionEvent event;
      event = MotionEvent.obtain(downTime, SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 1,
              properties, pointerCoords, 0, 0, 1, 1, 0, 0, InputDevice.SOURCE_TOUCHSCREEN, 0);
      injectEventSync(event);

      for (int x = 1; x < touches.length; x++) {
          event = MotionEvent.obtain(downTime, SystemClock.uptimeMillis(),
                  getPointerAction(MotionEvent.ACTION_POINTER_DOWN, x), x + 1, properties,
                  pointerCoords, 0, 0, 1, 1, 0, 0, InputDevice.SOURCE_TOUCHSCREEN, 0);
          injectEventSync(event);
      }

      // Move all pointers
      for (int i = 1; i < maxSteps - 1; i++) {
          // for each pointer
          for (int x = 0; x < touches.length; x++) {
              // check if it has coordinates to move
              if (touches[x].length > i)
                  pointerCoords[x] = touches[x][i];
              else
                  pointerCoords[x] = touches[x][touches[x].length - 1];
          }

          event = MotionEvent.obtain(downTime, SystemClock.uptimeMillis(),
                  MotionEvent.ACTION_MOVE, touches.length, properties, pointerCoords, 0, 0, 1, 1,
                  0, 0, InputDevice.SOURCE_TOUCHSCREEN, 0);

          injectEventSync(event);
          SystemClock.sleep(MOTION_EVENT_INJECTION_DELAY_MILLIS);
      }

      // For each pointer get the last coordinates
      for (int x = 0; x < touches.length; x++)
          pointerCoords[x] = touches[x][touches[x].length - 1];

      // touch up
      for (int x = 1; x < touches.length; x++) {
          event = MotionEvent.obtain(downTime, SystemClock.uptimeMillis(),
                  getPointerAction(MotionEvent.ACTION_POINTER_UP, x), x + 1, properties,
                  pointerCoords, 0, 0, 1, 1, 0, 0, InputDevice.SOURCE_TOUCHSCREEN, 0);
          injectEventSync(event);
      }

      // first to touch down is last up
      event = MotionEvent.obtain(downTime, SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 1,
              properties, pointerCoords, 0, 0, 1, 1, 0, 0, InputDevice.SOURCE_TOUCHSCREEN, 0);
      injectEventSync(event);
  }

  private void injectEventSync(MotionEvent event) {
	  instrumentation.sendPointerSync(event);
	  instrumentation.waitForIdleSync();
  }

  private int getPointerAction(int motionEnvent, int index) {
	  return motionEnvent + (index << MotionEvent.ACTION_POINTER_INDEX_SHIFT);
  }
}
