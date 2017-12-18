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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InstrumentationProcessOutput {
  private String errorMessage;
  private String shortMessage;
  private String longMessage;
  private String fullOutput;

  private static final String REGULAR_CRASH_MESSAGE = "Process crashed.";
  private static final String NATIVE_CRASH_MESSAGE = "Native crash";

  private static final String SHORT_MESSAGE_REGEX =
    "INSTRUMENTATION_RESULT: shortMsg=(.+)";
  private static final String LONG_MESSAGE_REGEX =
    "INSTRUMENTATION_RESULT: longMsg=(.+)";
  private static final String ERROR_MESSAGE_REGEX =
    "INSTRUMENTATION_STATUS: Error=(.+)";

  public static InstrumentationProcessOutput parse(String output) {
    Matcher shortMessageMatcher =
      Pattern
        .compile(SHORT_MESSAGE_REGEX)
        .matcher(output);
    Matcher longMessageMatcher =
      Pattern
        .compile(LONG_MESSAGE_REGEX)
        .matcher(output);
    Matcher errorMessageMatcher =
      Pattern
        .compile(ERROR_MESSAGE_REGEX)
        .matcher(output);

    String shortMessage;
    if (shortMessageMatcher.find()) {
      shortMessage = shortMessageMatcher.group(1);
    } else {
      shortMessage = null;
    }

    String longMessage;
    if (longMessageMatcher.find()) {
      longMessage = longMessageMatcher.group(1);
    } else {
      longMessage = null;
    }

    String errorMessage;
    if (errorMessageMatcher.find()) {
      errorMessage = errorMessageMatcher.group(1);
    } else {
      errorMessage = null;
    }

    return (new InstrumentationProcessOutput())
      .setFullOutput(output)
      .setShortMessage(shortMessage)
      .setLongMessage(longMessage)
      .setErrorMessage(errorMessage);
  }

  public boolean isAppCrash() {
    return isRegularAppCrash() || isNativeCrash();
  }

  public boolean isRegularAppCrash() {
    return REGULAR_CRASH_MESSAGE.equals(shortMessage);
  }

  public boolean isNativeCrash() {
    return NATIVE_CRASH_MESSAGE.equals(shortMessage);
  }

  public String getMessage() {
    if (isNativeCrash()) {
      return longMessage;
    }
    if (isRegularAppCrash()) {
      return shortMessage;
    }

    if (errorMessage != null) {
      return errorMessage;
    } else if (shortMessage != null) {
      String message = shortMessage;
      if (longMessage != null) {
        message += "\n" + longMessage;
      }
      return message;
    }

    return fullOutput;
  }

  public String getFullOutput() {
    return fullOutput;
  }

  private InstrumentationProcessOutput() {
  }

  private InstrumentationProcessOutput setFullOutput(String fullOutput) {
    this.fullOutput = fullOutput;
    return this;
  }

  private InstrumentationProcessOutput setShortMessage(String shortMessage) {
    this.shortMessage = shortMessage;
    return this;
  }

  private InstrumentationProcessOutput setLongMessage(String longMessage) {
    this.longMessage = longMessage;
    return this;
  }

  private InstrumentationProcessOutput setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
    return this;
  }
}
