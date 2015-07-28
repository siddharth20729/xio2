package com.xjeffrose.xio2.http.server;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.xjeffrose.xio2.http.Http;
import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ServiceTest {
  Server s = Http.newServer();
  OkHttpClient client = new OkHttpClient();


  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
    s.close();
  }

  @Test
  public void testHandleGet() throws Exception {
    s.addRoute("/service_test", new TestService());
    s.serve(9011);

    Request request = new Request.Builder()
        .url("http://localhost:9011/service_test")
        .build();

    Response response = client.newCall(request).execute();
    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

    assertTrue(response.isSuccessful());
    assertEquals(response.code(), 200);
  }
}