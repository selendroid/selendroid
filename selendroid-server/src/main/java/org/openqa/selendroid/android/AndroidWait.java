/*
 * Copyright 2011 NativeDriver committers Copyright 2011 Google Inc.
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
package org.openqa.selendroid.android;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

import org.openqa.selendroid.android.internal.AndroidSystemClock;
import org.openqa.selendroid.server.exceptions.SelendroidException;
import org.openqa.selendroid.server.exceptions.TimeoutException;

import android.content.res.Resources.NotFoundException;
import org.openqa.selendroid.util.Function;


/**
 * An implementation of the Wait interface that makes use of Android Native WebDriver.
 * 
 * @author Tomohiro Kaizu
 */
public class AndroidWait implements Wait<Void> {
  public static final long DEFAULT_SLEEP_INTERVAL = 200;
  private static final long DEFAULT_TIMEOUT = 5000;

  private final Clock clock;
  private final long sleepIntervalInMillis;

  private long timeoutInMillis;

  /**
   * Constructs an instance with default settings: 100 ms for sleep interval and 1 second for
   * timeout.
   */
  public AndroidWait() {
    this(new AndroidSystemClock(), DEFAULT_SLEEP_INTERVAL, DEFAULT_TIMEOUT);
  }

  /**
   * @param clock clock to use when measuring the timeout
   * @param sleepIntervalInMillis amount of time to sleep between attempts in milliseconds
   * @param timeoutInMillis timeout in milliseconds
   */
  protected AndroidWait(Clock clock, long sleepIntervalInMillis, long timeoutInMillis) {
    this.clock = clock;
    this.sleepIntervalInMillis = sleepIntervalInMillis;
    this.timeoutInMillis = timeoutInMillis;
  }

  @Override
  public <T> T until(Function<Void, T> isTrue) {
    long end = clock.laterBy(timeoutInMillis);
    NotFoundException lastException = null;

    do {
      try {
        T value = isTrue.apply(null);

        if (value != null && !Boolean.FALSE.equals(value)) {
          return value;
        }
      } catch (NotFoundException exception) {
        // Common case in many conditions, so swallow here, but be ready to
        // rethrow if it the element never appears.
        lastException = exception;
      }
      sleep();
    } while (clock.isNowBefore(end));

    throw new TimeoutException(String.format("Timed out after %d seconds",
        SECONDS.convert(timeoutInMillis, MILLISECONDS)), lastException);
  }

  /**
   * Sleeps for a few milliseconds.
   */
  protected void sleep() {
    try {
      Thread.sleep(sleepIntervalInMillis);
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      throw new SelendroidException(exception);
    }
  }

  /**
   * Sets the time limit in milliseconds.
   * 
   * @param timeoutInMillis time limit
   */
  public void setTimeoutInMillis(long timeoutInMillis) {
    this.timeoutInMillis = timeoutInMillis;
  }

  /**
   * Gets the time limit in milliseconds.
   * 
   * @return time limit
   */
  public long getTimeoutInMillis() {
    return timeoutInMillis;
  }
}
