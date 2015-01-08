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
package io.selendroid.server.handler;

import io.selendroid.server.common.Response;
import io.selendroid.server.common.SelendroidResponse;
import io.selendroid.server.common.exceptions.PermissionDeniedException;
import io.selendroid.server.common.exceptions.SelendroidException;
import io.selendroid.server.common.http.HttpRequest;
import io.selendroid.server.common.utils.CallLogEntry;
import io.selendroid.server.model.DefaultSelendroidDriver;
import io.selendroid.server.util.SelendroidLogger;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ReadCallLog extends SafeRequestHandler {

  public ReadCallLog(String mappedUri) {
    super(mappedUri);
  }

  @Override
  public Response safeHandle(HttpRequest request) throws JSONException {
    SelendroidLogger.info("reading call log");
    try {        
        List<CallLogEntry> response = ((DefaultSelendroidDriver)getSelendroidDriver(request)).readCallLog();
        SelendroidLogger.info("Succesfully read call log.");
        JSONArray json = new JSONArray();
        for(CallLogEntry logEntry: response) {
            json.put(logEntry.toJSON());
        }
        return new SelendroidResponse(getSessionId(request), json);
    }
    catch(PermissionDeniedException e) {
        throw new SelendroidException("READ_CALL_LOG permission must be in a.u.t. to read call logs.");
    }
  }
  
}
