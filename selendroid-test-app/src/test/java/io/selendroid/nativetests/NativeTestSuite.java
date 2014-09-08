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
package io.selendroid.nativetests;

import io.selendroid.driver.SelendroidUnknownCommandHandlingTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
  BackgroundAppTest.class,
  BrightnessTest.class,
  CommandConfigurationTest.class,
  NativeChildElementFindingTest.class,
  NativeElementFindingTest.class,
  NativeElementInteractionTest.class,
  SelendroidUnknownCommandHandlingTest.class,
  SendKeyAndNativeKeyTest.class,
  UnhandledExceptionPropagatingTest.class
  
 // WaitForProgressBarGoneAwayTest.class
})
public class NativeTestSuite {
  // the class remains empty,
  // used only as a holder for the above annotations
}
