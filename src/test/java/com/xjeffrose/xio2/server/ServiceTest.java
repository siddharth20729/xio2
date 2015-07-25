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
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ServiceTest {
  Server s = new Server();
  OkHttpClient client = new OkHttpClient();


  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
    s.close();
  }

  @Test
  public void testHandle() throws Exception {

  }

  @Test
  public void testHandleGet() throws Exception {
    s.addRoute("/test", new TestService());
    s.ssl(false);
    s.serve(9002);
//    Thread.sleep(100000);

    Request request = new Request.Builder()
        .url("http://localhost:9002/test")
        .build();

    Response response = client.newCall(request).execute();
    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

    assertTrue(response.isSuccessful());
    assertEquals(response.code(), 200);
  }

  @Test
  public void testHandlePost() throws Exception {

  }

  @Test
  public void testHandlePut() throws Exception {

  }

  @Test
  public void testHandleDelete() throws Exception {

  }

  @Test
  public void testAndThen() throws Exception {

  }
}