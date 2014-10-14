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
package io.selendroid.server;

import io.selendroid.exceptions.*;
import io.selendroid.exceptions.UnsupportedOperationException;
import io.selendroid.server.http.HttpRequest;
import io.selendroid.server.model.AndroidElement;
import io.selendroid.server.model.DefaultSelendroidDriver;
import io.selendroid.server.model.KnownElements;
import io.selendroid.server.model.SelendroidDriver;

import io.selendroid.util.SelendroidLogger;
import org.json.JSONArray;
import org.json.JSONException;


public abstract class SafeRequestHandler extends BaseRequestHandler {

  public SafeRequestHandler(String mappedUri) {
    super(mappedUri);
  }
  
  protected SelendroidDriver getSelendroidDriver(HttpRequest request) {
    return (DefaultSelendroidDriver) request.data().get(AndroidServlet.DRIVER_KEY);
  }

  protected String getIdOfKnownElement(HttpRequest request, AndroidElement element) {
    KnownElements knownElements = getKnownElements(request);
    if (knownElements == null || knownElements.getIdOfElement(element) == null) {
      throw new NoSuchElementException("Element was not found.");
    }
    return knownElements.getIdOfElement(element);
  }

  protected AndroidElement getElementFromCache(HttpRequest request, String id) {
    KnownElements knownElements = getKnownElements(request);
    if (knownElements == null || knownElements.get(id) == null) {
      throw new StaleElementReferenceException(
          "The element with id '" + id + "' was not found.");
    }
    return knownElements.get(id);
  }

  protected KnownElements getKnownElements(HttpRequest request) {
    if (getSelendroidDriver(request).getSession() == null) {
      return null;
    }
    return getSelendroidDriver(request).getSession().getKnownElements();
  }

  protected String[] extractKeysToSendFromPayload(HttpRequest request) throws JSONException {
    JSONArray valueArr = getPayload(request).getJSONArray("value");
    if (valueArr == null || valueArr.length() == 0) {
      throw new SelendroidException("No key to send to an element was found.");
    }

    String[] toReturn = new String[valueArr.length()];

    for (int i = 0; i < valueArr.length(); i++) {
      toReturn[i] = valueArr.getString(i);
    }

    return toReturn;
  }

  public abstract Response safeHandle(HttpRequest request) throws JSONException;

  @Override
  public final Response handle(HttpRequest request) throws JSONException {
    try {
      return safeHandle(request);
    } catch (ElementNotVisibleException e) {
      SelendroidLogger.debug("Element not visible", e);
      return new SelendroidResponse(getSessionId(request), StatusCode.ELEMENT_NOT_VISIBLE, e);
    } catch (StaleElementReferenceException e) {
      SelendroidLogger.debug("Stale element reference", e);
      return new SelendroidResponse(getSessionId(request), StatusCode.STALE_ELEMENT_REFERENCE, e);
    } catch (IllegalStateException e) {
      SelendroidLogger.debug("Invalid element state", e);
      return new SelendroidResponse(getSessionId(request), StatusCode.INVALID_ELEMENT_STATE, e);
    } catch (NoSuchElementException e) {
      SelendroidLogger.debug("No such element", e);
      return new SelendroidResponse(getSessionId(request), StatusCode.NO_SUCH_ELEMENT, e);
    } catch (UnsupportedOperationException e) {
      SelendroidLogger.debug("Unknown command", e);
      return new SelendroidResponse(getSessionId(request), StatusCode.UNKNOWN_COMMAND, e);
    } catch (NoSuchContextException e) {
      //TODO update error code when w3c spec gets updated
      return new SelendroidResponse(getSessionId(request), StatusCode.NO_SUCH_WINDOW,
          new SelendroidException("Invalid window handle was used: only 'NATIVE_APP' and 'WEBVIEW' are supported."));
    } catch (Exception e) {
      SelendroidLogger.error("Error while handling action in: " + this.getClass().getName(), e);
      return new SelendroidResponse(getSessionId(request), StatusCode.UNKNOWN_ERROR, e);
    }
  }

}
