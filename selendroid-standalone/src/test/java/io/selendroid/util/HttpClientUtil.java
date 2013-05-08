package io.selendroid.util;



import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selendroid.exceptions.SelendroidException;

public class HttpClientUtil {
	public static final String ANY_STRING = "ANY-STRING";

	public static HttpClient getHttpClient() {
		return new DefaultHttpClient();
	}

	public static HttpResponse executeRequestWithPayload(String uri, int port,
			HttpMethod method, String payload) throws Exception {
		BasicHttpEntityEnclosingRequest request = new BasicHttpEntityEnclosingRequest(
				method.getName(), uri);
		request.setEntity(new StringEntity(payload, "UTF-8"));

		return getHttpClient()
				.execute(new HttpHost("localhost", port), request);
	}

	public static JSONObject parseJsonResponse(HttpResponse response)
			throws Exception {
		return new JSONObject(IOUtils.toString(response.getEntity()
				.getContent()));
	}

	public static HttpResponse executeRequest(String url, HttpMethod method)
			throws Exception {
		HttpRequestBase request = null;
		if (HttpMethod.GET.equals(method)) {
			request = new HttpGet(url);
		} else if (HttpMethod.POST.equals(method)) {
			request = new HttpPost(url);
		} else if (HttpMethod.DELETE.equals(method)) {
			request = new HttpDelete(url);
		} else {
			throw new RuntimeException("Provided HttpMethod not supported");
		}
		return getHttpClient().execute(request);
	}

	public static HttpResponse executeCreateSessionRequest(int port)
			throws Exception {
		String url = "http://localhost:" + port + "/wd/hub/session";
		HttpResponse response = executeRequestWithPayload(url, port,
				HttpMethod.POST, getCapabilityPayload().toString());
		return response;
	}

	public static JSONObject getCapabilityPayload() {
		Capabilities desiredCapabilities = getCapabilities();
		JSONObject payload = new JSONObject();

		try {
			payload.put("desiredCapabilities", new JSONObject(
					desiredCapabilities.asMap()));
		} catch (JSONException e) {
			throw new SelendroidException(e);
		}
		return payload;
	}

	public static Capabilities getCapabilities() {
		return Capabilities.android(ANY_STRING, ANY_STRING, ANY_STRING);
	}
}
