package com.xjeffrose.xio2.server;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.xjeffrose.log.Log;
import com.xjeffrose.xio2.http.Http;
import com.xjeffrose.xio2.http.HttpResponse;
import java.io.IOException;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ServerTest {
  Server s;
  OkHttpClient client = new OkHttpClient();

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
    s.serve(9000);
    Request request = new Request.Builder()
        .url("http://localhost:9000/")
        .build();

    // Simulate 1500 req's / second
    final int reqs = 1500;

    for (int i = 0; i < reqs; i++) {
      Response response = client.newCall(request).execute();
      assertEquals(response.code(), 404);
    }
  }

  @Test
  public void testAddRoute() throws Exception {
    s.addRoute("/test", new TestService());
    s.serve(9001);
    // For AB Testing
//    Thread.sleep(100000);

    Request request = new Request.Builder()
        .url("http://localhost:9001/test")
        .build();

    Response response = client.newCall(request).execute();
    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

    assertTrue(response.isSuccessful());
    assertEquals(response.code(), 200);
  }

  @Test
  public void testAddRouteMany() throws Exception {
    s.serve(9001);
    s.addRoute("/test", new TestService());

    Request request = new Request.Builder()
        .url("http://localhost:9001/test")
        .build();

    // Simulate 1500 req's / second
    final int reqs = 1500;

    for (int i = 0; i < reqs; i++) {
      Response response = client.newCall(request).execute();
      if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

      assertTrue(response.isSuccessful());
      assertEquals(response.code(), 200);
    }
  }

}