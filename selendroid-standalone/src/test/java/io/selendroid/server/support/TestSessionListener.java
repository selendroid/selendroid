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
package io.selendroid.server.support;

import java.util.Properties;

import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selendroid.server.Response;

/**
 * Implementations are able to listen to the {@link SelendroidDeviceServerStub}
 * lifecycle.
 * 
 * @author ddary
 * 
 */
public abstract class TestSessionListener {
	public final String sessionId;
	public final JSONObject status;
	public final String uriMapping;

	public TestSessionListener(String sessionId,String uriMapping) throws JSONException {
		this.sessionId = sessionId;
		status = new JSONObject(
				"{build:{browserName:'selendroid',version:'0.4-SNAPSHOT'},"
						+ "os:{arch:'x86',locale:'en_US',version:16,name:'Android'}");
		this.uriMapping=uriMapping;
	}

	public Response deleteSession(Properties params) {
		return defaultResponseWithMessage("");
	}

	public Response status(Properties params) throws JSONException {
		return defaultResponseWithMessage(status);
	}
	
	public Response createSession(Properties params) throws JSONException {
		return defaultResponseWithMessage("");
	}

	public abstract Response executeSelendroidRequest(Properties params);

	protected Response defaultResponseWithMessage(Object message) {
		return new Response(sessionId, message);
	}

	protected Response defaultResponse() {
		return defaultResponseWithMessage("");
	}
}
