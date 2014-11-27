/*
 * Copyright 2012-2014 eBay Software Foundation and selendroid committers.
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
package io.selendroid.standalone.server.model.impl;

import io.selendroid.standalone.server.model.EmulatorPortFinder;
import io.selendroid.standalone.server.model.impl.DefaultPortFinder;

import org.junit.Assert;
import org.junit.Test;

public class DefaultPortFinderTest {

  private final static Integer MIN_PORT = 5560;
  private final static Integer MAX_PORT = 5590;

  @Test
  public void assertTestIsAbleToGetNextIntOfRange() {
    EmulatorPortFinder finder = new DefaultPortFinder(MIN_PORT, MAX_PORT);

    for (int i = MIN_PORT; i <= MAX_PORT; i += 2) {
      Assert.assertEquals(i, finder.next().intValue());
    }

    Assert.assertNull(finder.next());
  }

  @Test
  public void assertTestIsAbleToReleasePorts() {
    EmulatorPortFinder finder = new DefaultPortFinder(MIN_PORT, MAX_PORT);

    Assert.assertEquals(5560, finder.next().intValue());
    Assert.assertEquals(5562, finder.next().intValue());
    Assert.assertEquals(5564, finder.next().intValue());
    finder.release(5564);
    Assert.assertEquals(5564, finder.next().intValue());
  }

  @Test
  public void assertTestIsNotAbleToAddPortNumberHigherThanMaxValue() {
    EmulatorPortFinder finder = anEmptyFinder();
    finder.release(MAX_PORT + 2);
    Assert.assertNull(finder.next());
  }

  @Test
  public void assertTestIsNotAbleToAddPortNumberHigherThanMinValue() {
    EmulatorPortFinder finder = anEmptyFinder();
    finder.release(MIN_PORT - 2);
    Assert.assertNull(finder.next());
  }

  @Test
  public void assertTestIsNotAbleToAddOddPortNumber() {
    EmulatorPortFinder finder = anEmptyFinder();
    finder.release(MIN_PORT + 1);
    Assert.assertNull(finder.next());
  }

  private EmulatorPortFinder anEmptyFinder() {
    EmulatorPortFinder finder = new DefaultPortFinder(MIN_PORT, MAX_PORT);

    for (int i = MIN_PORT; i <= MAX_PORT; i += 2) {
      finder.next();
    }
    return finder;
  }
}
