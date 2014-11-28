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
package io.selendroid.server.common;

import java.util.HashMap;
import java.util.Map;

public enum StatusCode {
  SUCCESS(0), /* The command executed successfully. */
  NO_SUCH_DRIVER(6), /* A session is either terminated or not started */
  NO_SUCH_ELEMENT(7), /* An element could not be located on the page
  using the given search parameters. */
  NO_SUCH_FRAME(8), /* A request to switch to a frame could not be satisfied
  because the frame could not be found. */
  UNKNOWN_COMMAND(9), /*  The requested resource could not be found,
  or a request was received using an HTTP method that is not supported by the mapped resource.  */
  STALE_ELEMENT_REFERENCE(10), /* An element command failed
  because the referenced element is no longer attached to the DOM. */
  ELEMENT_NOT_VISIBLE(11), /* An element command could not be completed
  because the element is not visible on the page. */
  INVALID_ELEMENT_STATE(12), /* An element command could not be completed
   because the element is in an invalid state (e.g. attempting to click a disabled element). */
  UNKNOWN_ERROR(13), /* An unknown server-side error occurred while processing the command. */
  ELEMENT_IS_NOT_SELECTABLE(15), /* An attempt was made to select an element
   that cannot be selected. */
  JAVA_SCRIPT_ERROR(17), /* An error occurred while executing user supplied JavaScript. */
  X_PATH_LOOKUP_ERROR(19), /* An error occurred while searching for an element by XPath. */
  TIMEOUT(21), /* An operation did not complete before its timeout expired. */
  NO_SUCH_WINDOW(23), /* A request to switch to a different window could not be satisfied
  because the window could not be found. */
  INVALID_COOKIE_DOMAIN(24),
  /* An illegal attempt was made to set a cookie under a different domain than the current page. */
  UNABLE_TO_SET_COOKIE(25), /* A request to set a cookie's value could not be satisfied. */
  UNEXPECTED_ALERT_OPEN(26), /*  A modal dialog was open, blocking this operation  */
  NO_ALERT_OPEN_ERROR(27),
  /* An attempt was made to operate on a modal dialog when one was not open. */
  SCRIPT_TIMEOUT(28), /* A script did not complete before its timeout expired. */
  INVALID_ELEMENT_COORDINATES(29), /* The coordinates provided to an interactions operation
   are invalid. */
  IME_NOT_AVAILABLE(30), /* IME was not available. */
  IME_ENGINE_ACTIVATION_FAILED(31), /* An IME engine could not be started. */
  INVALID_SELECTOR(32), /* Argument was an invalid selector (e.g. XPath/CSS). */
  SESSION_NOT_CREATED_EXCEPTION(33), /* A new session could not be created. */
  MOVE_TARGET_OUT_OF_BOUNDS(34); /* Target provided for a move action is out of bounds. */

  private static Map<Integer, StatusCode> statusCodeMap = new HashMap<Integer, StatusCode>();

  static {
    // Build map so we get the StatusCode for a given int in the pesky dynamic bits
    for (StatusCode statusCode : StatusCode.values()) {
      statusCodeMap.put(statusCode.getCode(), statusCode);
    }
  }

  private int code;

  private StatusCode(int code) {
    this.code = code;
  }

  public int getCode() {
    return code;
  }

  public static StatusCode fromInteger(Integer code) {
    return statusCodeMap.get(code);
  }
}