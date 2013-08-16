/*
 * Copyright 2013 selendroid committers.
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

import io.selendroid.server.RequestHandler;
import io.selendroid.server.Response;
import io.selendroid.server.SelendroidResponse;
import io.selendroid.server.model.SelendroidDriver;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.json.JSONException;
import org.json.JSONObject;
import org.webbitserver.HttpRequest;
/**
 * 
 * This class contains the functionality for adding the cookies
 * Currenlty it supports stroing of cookie name,value and expiry date if any
 *
 */

public class AddCookie extends RequestHandler{

	public AddCookie(HttpRequest request, String mappedUri) {
		super(request, mappedUri);
	}

	@Override
	public Response handle() throws JSONException {
		JSONObject payload = getPayload();
		StringBuffer cookieStr = new StringBuffer();
		String cookieName = payload.getJSONObject("cookie").getString("name");
		String cookieValue = payload.getJSONObject("cookie").getString("value");
		cookieStr.append(cookieName).append("=").append(cookieValue);
		if (payload.getJSONObject("cookie").has("expiry")) {
			Date expiryDate = new Date(new Long(payload.getJSONObject("cookie").get("expiry").toString()).longValue());
			cookieStr.append("; expires=").append(toGMTString(expiryDate));
		}
		SelendroidDriver driver = getSelendroidDriver();
		driver.executeScript("document.cookie = \"" + cookieStr.toString()+ "\"");
		return new SelendroidResponse(getSessionId(), "");
	}
	
	private String toGMTString(Date date){
	    SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    sd.setTimeZone(TimeZone.getTimeZone("GMT"));
	    return sd.format(date);
	}
}
