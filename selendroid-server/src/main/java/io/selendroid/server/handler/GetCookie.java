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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.webbitserver.HttpRequest;

/**
 * 
 *This class retrieves the stored cookie information ,
 *if the request is for a specific cookie or for all the cookies.
 *
 *This class currently supports only those cookies whose
 *	1. Format is <cookie-name>=<cooke-value>
 *	2. Cookie Value is alpha numeric with the exception of "_" .This check has been done , 
 *     as Selenium WebDriver throws an exception if it encounters any special characters .
 *Constructs the response as a list of maps where each map representing details of one cookie.    
 *
 */
public class GetCookie extends RequestHandler{

	public GetCookie(HttpRequest request, String mappedUri) {
		super(request, mappedUri);
	}

	@Override
	public Response handle() throws JSONException {
		 SelendroidDriver driver = getSelendroidDriver();
		 Object cookieObj = driver.executeScript("return document.cookie");
			JSONObject object= new JSONObject(String.valueOf(cookieObj));
			String cookiesStr = object.getString("value");
			Map<String,Object> map = new HashMap<String, Object>();
			List<Map<String,Object>> result = new ArrayList<Map<String,Object>>();
			if (cookiesStr!=null) {
				String[] cookies = cookiesStr.split(";");
				if (cookies!=null) {
					for (String cookie : cookies) {
						String[] attributes = cookie.split("=");
						if (attributes!=null && attributes.length==2 && attributes[1].matches("[a-zA-Z0-9_]*")) {
							map = new HashMap<String, Object>();
							map.put("name",attributes[0]);
							map.put("value",attributes[1]);
							result.add(map);
						}
					}
				}
			}
			
		 return new SelendroidResponse(getSessionId(), 0, result);
	}

}
