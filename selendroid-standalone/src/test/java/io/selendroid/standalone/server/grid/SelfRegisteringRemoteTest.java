/*
 * Copyright 2012-2015 eBay Software Foundation and selendroid committers.
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
package io.selendroid.standalone.server.grid;

import io.selendroid.server.common.exceptions.SelendroidException;
import io.selendroid.standalone.SelendroidConfiguration;
import io.selendroid.standalone.server.model.SelendroidStandaloneDriver;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.json.JSONArray;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SelfRegisteringRemoteTest {

  private static final int HTTP_STATUS_CODE_OK = 200;
  private static final int HTTP_STATUS_CODE_FORBIDDEN = 403;
  @Mock private SelendroidConfiguration configuration;
  @Mock private SelendroidStandaloneDriver driver;
  @Mock private CloseableHttpClient client;
  @Mock private CloseableHttpResponse response;
  @Mock private StatusLine statusLine;
  @Mock private HttpEntity responseEntity;

  private SelfRegisteringRemote remote;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void shouldRegisterAtGrid() throws Exception {
    givenSupportedDevices();
    givenRegistrationUrl();
    givenResponseWithStatusCode(HTTP_STATUS_CODE_OK);
    givenHttpClient();
    givenSelfRegisteringRemote();
    remote.performRegistration();
  }

  @Test(expected = SelendroidException.class)
  public void shouldThrowExceptionWhenRegisterAtGridFails() throws Exception {
    givenSupportedDevices();
    givenRegistrationUrl();
    givenResponseWithStatusCode(HTTP_STATUS_CODE_FORBIDDEN);
    givenHttpClient();
    givenSelfRegisteringRemote();
    remote.performRegistration();
  }

  @Test
  public void shouldRegisterAtGridFirstTime() throws Exception {
    givenSupportedDevices();
    givenRegistrationUrl();
    givenResponseWithStatusCode(HTTP_STATUS_CODE_OK);
    givenHttpClient();
    givenSelfRegisteringRemote();
    remote.performRegistrationIfNotRegistered();
    verify(remote, times(1)).performRegistration();
    verify(remote, times(0)).isRegistered();
  }

  @Test
  public void shouldNotRegisterAtGridWhenAlreadyRegistered() throws Exception {
    givenSupportedDevices();
    givenRegistrationUrl();

    when(statusLine.getStatusCode()).thenReturn(HTTP_STATUS_CODE_OK);
    when(response.getStatusLine()).thenReturn(statusLine);

    final byte[] bytes = "{ \"success\" : true }".getBytes(UTF_8);
    when(responseEntity.getContent()).thenReturn(new ByteArrayInputStream(bytes), new ByteArrayInputStream(bytes));

    when(response.getEntity()).thenReturn(responseEntity);

    givenHttpClient();
    givenSelfRegisteringRemote();

    remote.performRegistrationIfNotRegistered(); // first registration - no check
    remote.performRegistrationIfNotRegistered(); // check if registered - no reg
    remote.performRegistrationIfNotRegistered(); // check if registered - no reg

    verify(remote, times(1)).performRegistration();
    verify(remote, times(2)).isRegistered();
  }

  @Test
  public void shouldReRegisterAtGridWhenGridRestarted() throws Exception {
    givenSupportedDevices();
    givenRegistrationUrl();
    givenResponseWithStatusCode(HTTP_STATUS_CODE_OK);

    givenGridRestartedWhenCheckSecondTime();

    givenHttpClient();
    givenSelfRegisteringRemote();

    remote.performRegistrationIfNotRegistered();    // first registration - no check
    verify(remote, times(1)).performRegistration(); // call for reg
    verify(remote, times(0)).isRegistered();        // no call for check

    remote.performRegistrationIfNotRegistered();    // check if registered - no reg
    verify(remote, times(1)).performRegistration(); // no more calls for reg
    verify(remote, times(1)).isRegistered();        // call for check

    // restarting grid
    // isRegistered returns false for second check
    remote.performRegistrationIfNotRegistered();    // check if registered and reg!
    verify(remote, times(2)).performRegistration(); // second call for reg
    verify(remote, times(2)).isRegistered();        // second call for check
  }

  protected void givenGridRestartedWhenCheckSecondTime() throws IOException {
    final byte[] bytesSuccess = "{ \"success\" : true }".getBytes(UTF_8); // first isRegistered check
    final byte[] bytesFail = "{ \"success\" : false }".getBytes(UTF_8);   // second isRegistered check
    when(responseEntity.getContent()).thenReturn(
            new ByteArrayInputStream(bytesSuccess),
            new ByteArrayInputStream(bytesFail));
    when(response.getEntity()).thenReturn(responseEntity);
  }

  @Test
  public void shouldThrowMalformedURLException() {
    try {
      givenSelfRegisteringRemote();
    } catch (Exception e) {
      assertThat("Must be instance of " + SelendroidException.class.getName(), e,
              instanceOf(SelendroidException.class));
      assertThat("Cause must be instance of " + MalformedURLException.class.getName(), e.getCause(),
              instanceOf(MalformedURLException.class));
    }
  }

  private void givenHttpClient() throws IOException {
    when(client.execute(any(HttpHost.class), any(HttpRequest.class))).thenReturn(response);
  }

  private void givenResponseWithStatusCode(int code) {
    when(statusLine.getStatusCode()).thenReturn(code);
    when(response.getStatusLine()).thenReturn(statusLine);
  }

  private void givenSupportedDevices() {
    JSONArray ja = new JSONArray();
    when(driver.getSupportedDevices()).thenReturn(ja);
  }

  private void givenRegistrationUrl() {
    when(configuration.getRegistrationUrl()).thenReturn("http://1.2.3.4:4444/grid/register");
  }

  private void givenSelfRegisteringRemote() {
    remote = Mockito.spy(new SelfRegisteringRemoteWithMocks(configuration, driver));
  }

  private class SelfRegisteringRemoteWithMocks extends SelfRegisteringRemote {
    public SelfRegisteringRemoteWithMocks(SelendroidConfiguration config, SelendroidStandaloneDriver driver) {
      super(config, driver);
    }

    @Override
    protected HttpClient getHttpClient() {
      return client;
    }
  }
}
