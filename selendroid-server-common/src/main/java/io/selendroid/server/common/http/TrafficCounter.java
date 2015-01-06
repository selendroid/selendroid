/*
 * Copyright 2014 eBay Software Foundation and selendroid committers.
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
package io.selendroid.server.common.http;

import io.netty.handler.traffic.GlobalTrafficShapingHandler;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class TrafficCounter {
  private static ScheduledExecutorService executorService;
  private static GlobalTrafficShapingHandler shaper;

  private static void initIfNecessary() {
    if (executorService == null || executorService.isShutdown()) {
      executorService = Executors.newScheduledThreadPool(1);
    }
    if (shaper == null) {
      shaper = new GlobalTrafficShapingHandler(executorService, 500);
    }
  }

  public static void shutdown() {
    if (shaper != null) {
      shaper.release();
      shaper = null;
    }
    if (executorService != null) {
      executorService.shutdownNow();
      executorService = null;
    }
  }

  public static GlobalTrafficShapingHandler getShaper() {
    initIfNecessary();
    return shaper;
  }

  public static long readBytes() {
    return getShaper().trafficCounter().cumulativeReadBytes();
  }

  public static long writtenBytes() {
    return getShaper().trafficCounter().cumulativeWrittenBytes();
  }
}