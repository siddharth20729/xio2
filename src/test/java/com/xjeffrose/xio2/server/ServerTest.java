package com.xjeffrose.xio2.server;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
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
    s = new Server();
  }

  @After
  public void tearDown() throws Exception {
    s.close();
  }

  @Test(expected = IOException.class)
  public void testServe() throws Exception {
    s.serve(9000);
    // For AB Testing
//    Thread.sleep(100000);
    Request request = new Request.Builder()
        .url("http://localhost:9000/")
        .build();

    Response response = client.newCall(request).execute();
    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

    assertEquals(response.code(), 404);
  }

  @Test
  public void testServeMany () throws Exception {
    s.serve(9001);
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
    s.addRoute("/test", new TestService());
    s.serve(9003);
    // For AB Testing
//    Thread.sleep(100000);

    Request request = new Request.Builder()
        .url("http://localhost:9003/test")
        .build();

    Response response = client.newCall(request).execute();
    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

    assertTrue(response.isSuccessful());
    assertEquals(response.code(), 200);
  }

  @Test
  public void testAddRouteMany() throws Exception {
    s.serve(9004);
    s.addRoute("/test", new TestService());

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
    }
  }

  @Test
  public void testSsl() throws Exception {
    OkHttpClient unsafeClient = getUnsafeOkHttpClient();

    s.ssl(true);
    s.addRoute("/test", new TestService());
    s.serve(9005);
    // For AB Testing
//    Thread.sleep(100000);
    Request request = new Request.Builder()
        .url("https://localhost:9005/test")
        .build();

    Response response = unsafeClient.newCall(request).execute();
    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

    assertEquals(response.code(), 200);
  }

  @Test
  public void testSslMany() throws Exception {
    OkHttpClient unsafeClient = getUnsafeOkHttpClient();

    s.ssl(true);
    s.serve(9006);
    s.addRoute("/test", new TestService());

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
    }
  }


  @Test(expected = SSLException.class)
  public void testSslFail() throws Exception {
    OkHttpClient unsafeClient = getUnsafeOkHttpClient();

    s.ssl(false);
    s.addRoute("/test", new TestService());
    s.serve(9007);

    Request request = new Request.Builder()
        .url("https://localhost:9007/test")
        .build();

    Response response = unsafeClient.newCall(request).execute();
    assertTrue(!response.isSuccessful());
  }


//  @Test
//  public void testChrisServiceAPI() throws Exception {
//
//    Server s = new Server();
//    RouteMap routes;
//    routes.add("/url", new ProxyingService());
//
//    s.serve(9999, routes);
//
//    Handler handler = new ProxyingHandler(routes);
//    s.serve(9999, handler);
//  }
}