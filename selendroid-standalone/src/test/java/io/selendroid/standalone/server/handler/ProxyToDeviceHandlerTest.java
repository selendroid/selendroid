package io.selendroid.standalone.server.handler;

import io.selendroid.server.common.Response;
import io.selendroid.server.common.SelendroidResponse;
import io.selendroid.server.common.StatusCode;
import io.selendroid.server.common.http.HttpRequest;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ProxyToDeviceHandlerTest
{
    private ProxyToDeviceHandler handler;

    @Before
    public void setUp() {
        handler = new ProxyToDeviceHandler("/some/uri");
    }

    @Test
    public void testRespondsWithFailureIfRequestDoesNotContainASessionIdHeader() throws JSONException {
        HttpRequest request = createHttpRequestWithData(new HashMap<String, Object>());

        Response response = handler.handle(request);

        assertNull(response.getSessionId());
        assertTrue("It's a selendroid response", response instanceof  SelendroidResponse);

        SelendroidResponse selendroidResponse = (SelendroidResponse) response;

        assertEquals(StatusCode.UNKNOWN_ERROR.getCode(), selendroidResponse.getStatus());
        assertTrue(selendroidResponse.getValue().toString().contains("No session id passed"));
    }

    private HttpRequest createHttpRequestWithData(Map<String, Object> data) {
        HttpRequest request = mock(HttpRequest.class);

        when(request.data()).thenReturn(data);

        return request;
    }
}