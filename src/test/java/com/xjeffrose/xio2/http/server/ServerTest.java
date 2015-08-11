/*
 * Copyright (C) 2015 Jeff Rose
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.xjeffrose.xio2.http.server;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.xjeffrose.xio2.Server;
import com.xjeffrose.xio2.http.Http;
import java.io.IOException;
import java.security.cert.CertificateException;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ServerTest {
  Server s;
  OkHttpClient client = new OkHttpClient();

  HttpHandler testHandler = new HttpHandler();

  private OkHttpClient getUnsafeOkHttpClient() {
    try {
      // Create a trust manager that does not validate certificate chains
      final TrustManager[] trustAllCerts = new TrustManager[] {
          new X509TrustManager() {
            @Override
            public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
            }

            @Override
            public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
            }

            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
              return null;
            }
          }
      };

      // Install the all-trusting trust manager
      final SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
      sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
      // Create an ssl socket factory with our all-trusting manager
      final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

      OkHttpClient okHttpClient = new OkHttpClient();
      okHttpClient.setSslSocketFactory(sslSocketFactory);
      okHttpClient.setHostnameVerifier(new HostnameVerifier() {
        @Override
        public boolean verify(String hostname, SSLSession session) {
          return true;
        }
      });

      return okHttpClient;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }


  @Before
  public void setUp() throws Exception {
    s = Http.newServer();
    testHandler.addRoute("/test", new TestHttpService());
  }

  @After
  public void tearDown() throws Exception {
    s.close();
  }

  @Test(expected = IOException.class)
  public void testServe() throws Exception {
    s.bind(9000);
    s.serve();

    Request request = new Request.Builder()
        .url("http://localhost:9000/")
        .build();

    Response response = client.newCall(request).execute();
    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

    assertEquals(response.code(), 404);
  }

  @Test
  public void testServeMany () throws Exception {
    s.bind(9001);
    s.serve();

    Request request = new Request.Builder()
        .url("http://localhost:9001/")
        .build();

    // Simulate 1500 obj's / second
    final int reqs = 1500;

    for (int i = 0; i < reqs; i++) {
      Response response = client.newCall(request).execute();
      assertEquals(response.code(), 404);
    }
  }

  @Test
  public void testAddRoute() throws Exception {
    s.serve(9003, testHandler);

    Request request = new Request.Builder()
        .url("http://localhost:9003/test")
        .build();

    Response response = client.newCall(request).execute();
    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

    assertTrue(response.isSuccessful());
    assertEquals(response.code(), 200);
    assertEquals("THIS IS BODY", response.body().string());
  }

  @Test
  public void testAddRouteMany() throws Exception {
    s.serve(9004, testHandler);

    Request request = new Request.Builder()
        .url("http://localhost:9004/test")
        .build();

    // Simulate 1500 obj's / second
    final int reqs = 1500;

    for (int i = 0; i < reqs; i++) {
      Response response = client.newCall(request).execute();
      if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

      assertTrue(response.isSuccessful());
      assertEquals(response.code(), 200);
      assertEquals("THIS IS BODY", response.body().string());
    }
  }

  @Test
  public void testSsl() throws Exception {
    OkHttpClient unsafeClient = getUnsafeOkHttpClient();

    s.tls(true);
    s.serve(9005, testHandler);

    Request request = new Request.Builder()
        .url("https://localhost:9005/test")
        .build();

    Response response = unsafeClient.newCall(request).execute();
    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

    assertEquals(response.code(), 200);
    assertEquals("THIS IS BODY", response.body().string());
  }

  @Test
  public void testSslMany() throws Exception {
    OkHttpClient unsafeClient = getUnsafeOkHttpClient();

    s.tls(true);
    s.serve(9006, testHandler);


    Request request = new Request.Builder()
        .url("https://localhost:9006/test")
        .build();

    // Simulate 100 req's / second
    final int reqs = 100;

    for (int i = 0; i < reqs; i++) {
      Response response = unsafeClient.newCall(request).execute();
      if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

      assertTrue(response.isSuccessful());
      assertEquals(response.code(), 200);
      assertEquals("THIS IS BODY", response.body().string());
    }
  }


  @Test(expected = SSLException.class)
  public void testSslFail() throws Exception {
    OkHttpClient unsafeClient = getUnsafeOkHttpClient();

    s.tls(false);
    s.serve(9007, testHandler);

    Request request = new Request.Builder()
        .url("https://localhost:9007/test")
        .build();

    Response response = unsafeClient.newCall(request).execute();
    assertTrue(!response.isSuccessful());
  }
}