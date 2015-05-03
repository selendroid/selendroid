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

package io.selendroid.server.android;

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
import io.selendroid.server.ServerInstrumentation;
import io.selendroid.server.model.TouchScreen;
import io.selendroid.server.model.interactions.Coordinates;
import io.selendroid.server.util.SelendroidLogger;
import io.selendroid.server.android.internal.Point;
import io.selendroid.server.common.action.touch.FlickDirection;
import io.selendroid.server.common.exceptions.SelendroidException;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/**
 * Implements touch capabilities of a device.
 * 
 */
public class AndroidTouchScreen implements TouchScreen {
  private static final int MOTION_EVENT_INJECTION_DELAY_MILLIS = 5;
  private static final int MOTION_EVENT_META_STATE = 0;
  private static final float MOTION_EVENT_X_PRECISION = 1.0f;
  private static final float MOTION_EVENT_Y_PRECISION = 1.0f;
  private static final int MOTION_EVENT_DEVICE_ID = 0;
  private static final int MOTION_EVENT_EDGE_FLAGS = 0;
  private static final int MOTION_EVENT_SOURCE = 0;
  private static final int MOTION_EVENT_FLAGS = 0;
  private final ServerInstrumentation instrumentation;
  private final MotionSender motions;
  private ArrayDeque<Pointer> pointers = new ArrayDeque<Pointer>();

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

  public void down(int x, int y, int id) {
    List<MotionEvent> event = new ArrayList<MotionEvent>();
    long downTime = SystemClock.uptimeMillis();
    int action;
    Pointer p = new Pointer(id, x, y);
    if (pointers.isEmpty()) {
      action = MotionEvent.ACTION_DOWN;
      pointers.add(p);
    } else {
      action = MotionEvent.ACTION_POINTER_DOWN;
      pointers.addFirst(p);
    }
    event.add(getMotionEvent(downTime, downTime, action));
    motions.send(event);
  }

  public void up(int x, int y) {
    List<MotionEvent> event = new ArrayList<MotionEvent>();
    long downTime = SystemClock.uptimeMillis();
    Point coords = new Point(x, y);
    event.add(getMotionEvent(downTime, downTime, MotionEvent.ACTION_UP, coords));
    motions.send(event);
  }

  public void up(int x, int y, int id) {
    List<MotionEvent> event = new ArrayList<MotionEvent>();
    long downTime = SystemClock.uptimeMillis();
    int action;
    if (pointers.size() == 1) {
      action = MotionEvent.ACTION_UP;
    } else {
      action = MotionEvent.ACTION_POINTER_UP;
      movePointerToFront(id);
    }
    event.add(getMotionEvent(downTime, downTime, action));
    motions.send(event);
    pointers.removeFirst();
  }

  private void movePointerToFront(int id) {
    for(Pointer p : pointers) {
      if(p.getId() == id) {
        pointers.remove(p);
        pointers.addFirst(p);
        break;
      }
    }
  }

  public void move(int x, int y) {
    List<MotionEvent> event = new ArrayList<MotionEvent>();
    long downTime = SystemClock.uptimeMillis();
    Point coords = new Point(x, y);
    event.add(getMotionEvent(downTime, downTime, MotionEvent.ACTION_MOVE, coords));
    motions.send(event);
  }

  public void move(int x, int y, int id) {
    List<MotionEvent> event = new ArrayList<MotionEvent>();
    long downTime = SystemClock.uptimeMillis();
    for(Pointer p : pointers) {
      if (p.getId() == id) {
        p.setCoords(x, y);
      }
    }
    event.add(getMotionEvent(downTime, downTime, MotionEvent.ACTION_MOVE));
    motions.send(event);
  }

  public void cancel() {
    for(Pointer p : pointers) {
      up((int)p.getCoords().x, (int)p.getCoords().y, p.getId());
    }
  }

  public void scroll(Coordinates where, int xOffset, int yOffset) {
    long downTime = SystemClock.uptimeMillis();
    List<MotionEvent> motionEvents = new ArrayList<MotionEvent>();
    Point origin = where.getLocationOnScreen();
    Point destination = new Point(origin.x + xOffset, origin.y + yOffset);
    motionEvents.add(getMotionEvent(downTime, downTime, MotionEvent.ACTION_DOWN, origin));

    Scroll scroll = new Scroll(origin, destination, downTime);
    // Initial acceleration from origin to reference point
    motionEvents.add(getBatchedMotionEvent(downTime, downTime, origin, scroll.getDecelerationPoint(),
        Scroll.INITIAL_STEPS, Scroll.TIME_BETWEEN_EVENTS));
    // Deceleration phase from reference point to destination
    motionEvents.add(getBatchedMotionEvent(downTime, scroll.getEventTimeForReferencePoint(),
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
    Instrumentation inst = instrumentation;

    MotionEvent event = null;
    boolean isSuccess = false;
    int retry = 0;
    while (!isSuccess && retry < 10) {
      try {
        if (event == null) {
          event =
              MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, point.x, point.y, 0);
        }
        SelendroidLogger.debug("trying to send pointer");
        inst.sendPointerSync(event);
        isSuccess = true;
      } catch (SecurityException e) {
        SelendroidLogger.error("failed: " + retry);
        retry++;
      }
    }
    if (!isSuccess) {
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
      Thread.currentThread().interrupt();
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
    DynamicIntervalFlick flick = new DynamicIntervalFlick(speed);
    motionEvents.add(getMotionEvent(downTime, downTime, MotionEvent.ACTION_DOWN, origin));
    motionEvents.add(getBatchedMotionEvent(downTime, downTime, origin, destination, flick.getNumberOfSteps(),
        flick.getTimeBetweenEvents()));
    motionEvents.add(getMotionEvent(downTime, flick.getTimeForDestinationPoint(downTime),
        MotionEvent.ACTION_UP, destination));
    motions.send(motionEvents);
  }

  public void flick(Point origin, FlickDirection direction, int distance, int duration) {
    int xOffset = distance * direction.getxMultiplier();
    int yOffset = distance * direction.getyMultiplier();
    Point destination = new Point(origin.x + xOffset, origin.y + yOffset);
    generateFlickMotions(origin, destination, new FixedIntervalFlick(duration));
  }

  private void generateFlickMotions(Point origin, Point destination, Flick flick) {
    long downTime = SystemClock.uptimeMillis();

    List<MotionEvent> motionEvents = new ArrayList<MotionEvent>();
    motionEvents.add(getMotionEvent(downTime, downTime, MotionEvent.ACTION_DOWN, origin));
    motionEvents.add(getBatchedMotionEvent(
        downTime, downTime, origin, destination, flick.getNumberOfSteps(),
        flick.getTimeBetweenEvents()));
    motionEvents.add(getMotionEvent(downTime, flick.getTimeForDestinationPoint(downTime),
        MotionEvent.ACTION_UP, destination));
    motions.send(motionEvents);
  }

  private MotionEvent getMotionEvent(long start, long eventTime, int action, Point coords) {
    return MotionEvent.obtain(start, eventTime, action, coords.x, coords.y, 0);
  }

  private MotionEvent getMotionEvent(long start, long eventTime, int action) {
    return MotionEvent.obtain(start, eventTime, action, pointers.size(), getPointerIds(), getPointerCoords(),
            MOTION_EVENT_META_STATE, MOTION_EVENT_X_PRECISION, MOTION_EVENT_Y_PRECISION, MOTION_EVENT_DEVICE_ID,
            MOTION_EVENT_EDGE_FLAGS, MOTION_EVENT_SOURCE, MOTION_EVENT_FLAGS);
  }

  private int[] getPointerIds () {
    int[] pointerIds = new int[pointers.size()];
    int i = 0;
    for(Pointer p : pointers) {
      pointerIds[i++] = p.getId();
    }
    return pointerIds;
  }

  private PointerCoords[] getPointerCoords () {
    PointerCoords[] pointerCoords = new PointerCoords[pointers.size()];
    int i = 0;
    for(Pointer p : pointers) {
      pointerCoords[i++] = p.getCoords();
    }
    return pointerCoords;
  }

  private MotionEvent getBatchedMotionEvent(long downTime, long startingEventTime, Point origin,
                                            Point destination, int steps, long timeBetweenEvents) {
    float xStep = (destination.x - origin.x) / steps;
    float yStep = (destination.y - origin.y) / steps;
    float x = origin.x;
    float y = origin.y;
    long eventTime = startingEventTime;

    x += xStep;
    y += yStep;
    MotionEvent event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, x, y, 0);

    for (int i = 0; i < steps - 1; i++) {
      x += xStep;
      y += yStep;

      eventTime += timeBetweenEvents;
      event.addBatch(eventTime, x, y, 1.0f, 1.0f, 0);
    }

    eventTime += timeBetweenEvents;
    event.addBatch(eventTime, destination.getX(), destination.getY(), 1.0f, 1.0f, 0);
    return event;
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

  interface Flick {
    public int getNumberOfSteps();
    public long getTimeBetweenEvents();
    public long getTimeForDestinationPoint(long downTime);
  }

  final class DynamicIntervalFlick implements Flick {

    private final int SPEED_NORMAL = 0;
    private final int SPEED_FAST = 1;
    private final int SPEED_SLOW = 2;

    private int speed;

    public DynamicIntervalFlick(int speed) {
      this.speed = speed;
    }

    public int getNumberOfSteps() {
      return speed == SPEED_SLOW ? 8 : 4;
    }

    public long getTimeBetweenEvents() {
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

    public long getTimeForDestinationPoint(long downTime) {
      return (downTime + getNumberOfSteps() * getTimeBetweenEvents());
    }
  }

  final class FixedIntervalFlick implements Flick {
    private int EVENT_INTERVAL_MS = 15;

    private int time; // Total time in ms of flick gesture

    public FixedIntervalFlick(int time) {
      this.time = time;
    }

    public int getNumberOfSteps() {
      return time / EVENT_INTERVAL_MS;
    }

    public long getTimeBetweenEvents() {
      return (time == 0) ? 0 : EVENT_INTERVAL_MS;
    }

    public long getTimeForDestinationPoint(long downTime) {
      return downTime + time;
    }
  }

  public class Pointer
  {
    private final int id;
    private PointerCoords coords;

    public Pointer(int id, int x, int y)
    {
      this.id   = id;
      coords = new PointerCoords();
      setCoords(x, y);
    }

    public int getId()   {
      return id;
    }

    public PointerCoords getCoords() {
      return coords;
    }

    public void setCoords(int x, int y) {
      coords.x = x;
      coords.y = y;
    }
  }
}
