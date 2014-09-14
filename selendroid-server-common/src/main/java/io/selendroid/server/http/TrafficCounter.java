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
package io.selendroid.server.http;

import java.util.concurrent.Executors;

import io.netty.handler.traffic.GlobalTrafficShapingHandler;

public class TrafficCounter {
  public static final GlobalTrafficShapingHandler shaper
      = new GlobalTrafficShapingHandler(Executors.newScheduledThreadPool(1), 500);

  public static long readBytes() {
    return shaper.trafficCounter().cumulativeReadBytes();
  }

  public static long writtenBytes() {
    return shaper.trafficCounter().cumulativeWrittenBytes();
  }
}