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
 * This class contains functionality to delete
 * 1. a specific cookie by name
 * 2. all cookies.
 *
 */

public class DeleteCookie extends RequestHandler{

	public DeleteCookie(HttpRequest request, String mappedUri) {
		super(request, mappedUri);
	}

	@Override
	public Response handle() throws JSONException {
		 SelendroidDriver driver = getSelendroidDriver();
		 String uri = request.uri();
		 if (!uri.endsWith("cookie")) {
			 String cookieName =  uri.substring(uri.lastIndexOf("/")+1);
			 driver.executeScript("document.cookie = \""+cookieName+"="+"deleted; expires=" + toGMTString(new Date(0))+"\"");
		 } else {
			 Object cookieObj = driver.executeScript("return document.cookie");
				JSONObject object= new JSONObject(String.valueOf(cookieObj));
				String cookiesStr = object.getString("value");
				if (cookiesStr!=null) {
					String[] cookies = cookiesStr.split(";");
					if (cookies!=null) {
						for (String cookie : cookies) {
							String[] attributes = cookie.split("=");
							if (attributes!=null && attributes.length==2 && attributes[1].matches("[a-zA-Z0-9_]*")) {
								driver.executeScript("document.cookie = \""+attributes[0]+"="+"deleted; expires=" + toGMTString(new Date(0))+"\"");
							}
						}
					}
				}
		 }
		 
		 return new SelendroidResponse(getSessionId(), "");
	}
	
	private String toGMTString(Date date){
	    SimpleDateFormat sd = new SimpleDateFormat("d MMM yyyy HH:mm:ss");
	    sd.setTimeZone(TimeZone.getTimeZone("GMT"));
	    return sd.format(date);
	}
}
