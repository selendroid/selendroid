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
 package io.selendroid.standalone.android;

import java.lang.instrument.Instrumentation;
import org.junit.Assert;
import org.junit.Test;

public class InstrumentationProcessOutputTest {

  @Test
  public void testNativeCrash() {
    InstrumentationProcessOutput output = InstrumentationProcessOutput.parse(
      "INSTRUMENTATION_RESULT: shortMsg=Native crash\n" +
      "INSTRUMENTATION_RESULT: longMsg=Native crash: Segmentation fault\n" +
      "INSTRUMENTATION_CODE: 0");

    Assert.assertTrue(output.isAppCrash());
    Assert.assertFalse(output.isRegularAppCrash());
    Assert.assertTrue(output.isNativeCrash());
    Assert.assertEquals("Native crash: Segmentation fault", output.getMessage());
  }

  @Test
  public void testRegularCrash() {
    InstrumentationProcessOutput output = InstrumentationProcessOutput.parse(
      "INSTRUMENTATION_RESULT: shortMsg=Process crashed.\n"+
      "INSTRUMENTATION_CODE: 0");

    Assert.assertTrue(output.isAppCrash());
    Assert.assertTrue(output.isRegularAppCrash());
    Assert.assertFalse(output.isNativeCrash());
    Assert.assertEquals("Process crashed.", output.getMessage());
  }

  @Test
  public void testAppNotInstalled() {
    InstrumentationProcessOutput output = InstrumentationProcessOutput.parse(
      "INSTRUMENTATION_STATUS: Error=Unable to find instrumentation target package: com.foo\n" +
      "INSTRUMENTATION_STATUS_CODE: -1");

    Assert.assertFalse(output.isAppCrash());
    Assert.assertEquals(
      "Unable to find instrumentation target package: com.foo",
      output.getMessage());
  }

  @Test
  public void testOtherValidInstrumentationOutput() {
    InstrumentationProcessOutput output = InstrumentationProcessOutput.parse(
      "INSTRUMENTATION_RESULT: shortMsg=keyDispatchingTimedOut\n" +
      "INSTRUMENTATION_RESULT: longMsg=Input dispatching timed out (Waiting because the touched window has not finished processing the input events that were previously delivered to it.)");

    Assert.assertFalse(output.isAppCrash());
    Assert.assertEquals(
      "keyDispatchingTimedOut\n" +
      "Input dispatching timed out (Waiting because the touched window has not finished processing the input events that were previously delivered to it.)",
      output.getMessage());
  }

  @Test
  public void testUnstructuredInstrumentationOutput() {
    InstrumentationProcessOutput output = InstrumentationProcessOutput.parse(
      "android.util.AndroidException: INSTRUMENTATION_FAILED: io.selendroid.com.facebook.orca/io.selendroid.server.SelendroidInstrumentation\n" +
    	"at com.android.commands.am.Am.runInstrument(Am.java:865)\n" +
    	"at com.android.commands.am.Am.onRun(Am.java:282)\n" +
    	"at com.android.internal.os.BaseCommand.run(BaseCommand.java:47)\n" +
    	"at com.android.commands.am.Am.main(Am.java:76)\n" +
    	"at com.android.internal.os.RuntimeInit.nativeFinishInit(Native Method)\n" +
    	"at com.android.internal.os.RuntimeInit.main(RuntimeInit.java:243)\n" +
    	"at dalvik.system.NativeStart.main(Native Method)");

    Assert.assertFalse(output.isAppCrash());
    Assert.assertEquals(
      "android.util.AndroidException: INSTRUMENTATION_FAILED: io.selendroid.com.facebook.orca/io.selendroid.server.SelendroidInstrumentation\n" +
    	"at com.android.commands.am.Am.runInstrument(Am.java:865)\n" +
    	"at com.android.commands.am.Am.onRun(Am.java:282)\n" +
    	"at com.android.internal.os.BaseCommand.run(BaseCommand.java:47)\n" +
    	"at com.android.commands.am.Am.main(Am.java:76)\n" +
    	"at com.android.internal.os.RuntimeInit.nativeFinishInit(Native Method)\n" +
    	"at com.android.internal.os.RuntimeInit.main(RuntimeInit.java:243)\n" +
    	"at dalvik.system.NativeStart.main(Native Method)",
      output.getMessage());
  }
}
