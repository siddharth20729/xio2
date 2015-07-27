package com.xjeffrose.xio2.client;

import com.xjeffrose.xio2.http.HttpObject;
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
  }

  @After
  public void tearDown() throws Exception {
    s.close();
  }

  @Test
  public void testGet() throws Exception {
    s.serve(9018);
    c.connect("localhost", 9018);

    HttpRequest req = new HttpRequest.Builder()
        .url("/")
        .build();

    HttpObject resp = c.get(req);
    assertEquals(resp.getHttpVersion(), "HTTP/1.1");
    assertEquals(resp.getStatus(), "404 Not Found");
    assertEquals(resp.headers.size(), 4);
    assertEquals(resp.headers.get("Content-Type"), "text/html; charset=UTF-8");
    assertEquals(resp.headers.get("Server"), "xio2");
  }

  @Test
  public void testSSLGet() throws Exception {
    s.ssl(true);
    c.ssl(true);

    s.serve(9017);
    c.connect("localhost", 9017);

    HttpRequest req = new HttpRequest.Builder()
        .url("/")
        .build();

    HttpObject resp = c.get(req);
    assertEquals(resp.getHttpVersion(), "HTTP/1.1");
    assertEquals(resp.getStatus(), "404 Not Found");
    assertEquals(resp.headers.size(), 4);
    assertEquals(resp.headers.get("Content-Type"), "text/html; charset=UTF-8");
    assertEquals(resp.headers.get("Server"), "xio2");
  }
}