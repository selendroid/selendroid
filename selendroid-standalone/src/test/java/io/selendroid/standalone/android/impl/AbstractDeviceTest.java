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
package io.selendroid.standalone.android.impl;

import org.apache.commons.exec.CommandLine;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;

import java.util.regex.Pattern;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AbstractDeviceTest {
  private static final Matcher<CommandLine> matchesCmdLine(final String cmdString) {
    return new BaseMatcher<CommandLine>() {
      @Override
      public boolean matches(Object cmdLine) {
        if (!(cmdLine instanceof CommandLine)) {
          return false;
        }
        return Pattern.matches(cmdString, cmdLine.toString());
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("command was not " + cmdString);
      }
    };
  }

  @Test
  public void testGetCrashLogContents() {
    AbstractDevice device = mock(AbstractDevice.class);
    when(device.getExternalStoragePath()).thenReturn("/storage");
    when(device.getCrashLog()).thenCallRealMethod();
    when(
        device.executeCommandQuietly(argThat(matchesCmdLine(".*adb(\\.exe)?, shell, ls, /storage/]$"))))  // The trailing '/' is key
        .thenReturn("some_file\nappcrash.log\nanother_file");
    when(
        device.executeCommandQuietly(argThat(matchesCmdLine(".*adb(\\.exe)?, shell, cat, /storage/appcrash\\.log]$"))))
        .thenReturn("crash log contents");

    assertEquals("crash log contents", device.getCrashLog());
  }

  @Test
  public void testListThirdPartyProcesses() {
    AbstractDevice device = mock(AbstractDevice.class);
    when(device.listRunningThirdPartyProcesses()).thenCallRealMethod();
    when(device.runAdbCommand(anyString())).thenCallRealMethod();
    String psOutput =
        "PID NAME\n" +
        "11 com.example.myapp\n" +
        "123 com.android.calendar\n" +
        "15 com.example.another\n" +
        "1 zygote\n" +
        "23 /system/bin/mediaserver";
    when(
        device.executeCommandQuietly(argThat(matchesCmdLine(".*adb(\\.exe)?, shell, ps]$"))))
            .thenReturn(psOutput);
    String expected =
        "PID NAME\n" +
        "11 com.example.myapp\n" +
        "15 com.example.another\n";
    assertEquals(expected, device.listRunningThirdPartyProcesses());
  }
}
