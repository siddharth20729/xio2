package com.xjeffrose.xio2.client;

import com.xjeffrose.xio2.http.HttpRequest;
import com.xjeffrose.xio2.server.Server;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ClientTest {
  Server s = new Server();
  XioClient c = new XioClient();


  @Before
  public void setUp() throws Exception {
    s.ssl.set(true);
    s.serve(9007);

  }

  @After
  public void tearDown() throws Exception {
    s.close();
  }

  @Test
  public void testGet() throws Exception {
//    c.ssl = true;
    c.connect("localhost", 9007);
//    c.connect("localhost", 4433);
//    c.get();

    HttpRequest req = new HttpRequest.Builder()
        .url("/")
        .build();

    HttpObject resp = c.get(req);
//    assertEquals(resp.getHttpVersion(), "HTTP/1.1");
//    assertEquals(resp.getStatus(), "404 Not Found");
//    assertEquals(resp.headers.size(), 3);
//    assertEquals(resp.headers.get("Content-Type"), "text/html; charset=UTF-8");
//    assertEquals(resp.headers.get("Server"), "xio2");
  }

  @Test
  public void testPost() throws Exception {

  }

  @Test
  public void testPut() throws Exception {

  }

  @Test
  public void testDelete() throws Exception {

  }

  @Test
  public void testDelete1() throws Exception {

  }
}