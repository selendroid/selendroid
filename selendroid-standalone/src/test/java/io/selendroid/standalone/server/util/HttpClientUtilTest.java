package io.selendroid.standalone.server.util;

import io.netty.handler.codec.http.HttpMethod;
import junit.framework.TestCase;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.InputStream;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HttpClientUtilTest extends TestCase {

    private HttpClientUtil httpClientUtil = Mockito.spy(new HttpClientUtil());

    public void testParseJsonResponse() throws Exception {
        InputStream inputStream = IOUtils.toInputStream("{ 'id' : 5, 'name' : 'kitkat' }");

        JSONObject expectedResult = new JSONObject();
        expectedResult.put("id", 5);
        expectedResult.put("name", "kitkat");

        HttpEntity httpEntity = mock(HttpEntity.class);
        when(httpEntity.getContent()).thenReturn(inputStream);

        HttpResponse httpResponse = mock(HttpResponse.class);
        when(httpResponse.getEntity()).thenReturn(httpEntity);

        JSONObject result = httpClientUtil.parseJsonResponse(httpResponse);

        assertEquals(result.toString(), expectedResult.toString());
    }

    public void testParseJsonResponseThrowsException() throws Exception {
        InputStream inputStream = IOUtils.toInputStream("{{{ 'id' :;; 5, 'name' ? 'kitkat' }");

        HttpEntity httpEntity = mock(HttpEntity.class);
        when(httpEntity.getContent()).thenReturn(inputStream);

        HttpResponse httpResponse = mock(HttpResponse.class);
        when(httpResponse.getEntity()).thenReturn(httpEntity);

        Throwable e = null;
        try {
            JSONObject result = httpClientUtil.parseJsonResponse(httpResponse);
        } catch(Throwable ex) {
            e = ex;
        }

        assertTrue(e instanceof JSONException);
    }

    public void testExecuteThrowsException() throws Exception {
        HttpMethod httpMethod = new HttpMethod("DOESNOTEXIST");
        String url = "http://localhost:50/wd/hub/sessions";

        Throwable e = null;
        try {
            HttpResponse response = httpClientUtil.executeRequest(url, httpMethod);
        } catch(Exception ex) {
            e = ex;
        }

        assertTrue(e instanceof RuntimeException);
        assertEquals(e.getMessage(), "Provided HttpMethod not supported: DOESNOTEXIST");
    }
}
