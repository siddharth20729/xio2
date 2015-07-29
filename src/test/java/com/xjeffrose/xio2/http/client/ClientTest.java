package com.xjeffrose.xio2.http.client;

import com.xjeffrose.xio2.http.Http;
import com.xjeffrose.xio2.http.HttpObject;
import com.xjeffrose.xio2.http.HttpRequest;
import com.xjeffrose.xio2.http.server.Server;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ClientTest {
  Server s = Http.newServer();

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

    HttpRequest req = new HttpRequest.Builder()
        .url("/")
        .build();

    Client c = Http.newClient("localhost:9018");
//    c.connect();

    HttpObject resp = c.get(req);
    assertEquals(resp.getHttpVersion(), "HTTP/1.1");
    assertEquals(resp.getStatus(), "404 Not Found");
    assertEquals(resp.headers.size(), 4);
    assertEquals(resp.headers.get("Content-Type"), "text/html; charset=UTF-8");
    assertEquals(resp.headers.get("Server"), "xio2");
  }

  @Test
  public void testSSLGet() throws Exception {
    Client c = Http.newClient("localhost:9017");
//    c.connect();

    s.ssl(true);
    c.ssl(true);

    s.serve(9017);

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
  public void testServerSet() throws Exception {

    s.ssl(false);
    s.serve(9031);
    s.serve(9032);
    s.serve(9033);

    Client c = new Client("localhost:9031, localhost:9032, localhost:9033");
//    c.connect();

    HttpRequest req = new HttpRequest.Builder()
        .url("/")
        .build();

    for (int i = 0; i < 4; i++) {
      HttpObject resp = c.get(req);
      assertEquals(resp.getHttpVersion(), "HTTP/1.1");
      assertEquals(resp.getStatus(), "404 Not Found");
      assertEquals(resp.headers.size(), 4);
      assertEquals(resp.headers.get("Content-Type"), "text/html; charset=UTF-8");
      assertEquals(resp.headers.get("Server"), "xio2");
    }
  }

  @Test
  public void testServerSetSSL() throws Exception {

    s.ssl(true);
    s.serve(9021);
    s.serve(9022);
    s.serve(9023);

    Client c = new Client("localhost:9021, localhost:9022, localhost:9023");
//    c.connect();

    c.ssl(true);

    HttpRequest req = new HttpRequest.Builder()
        .url("/")
        .build();

    for (int i = 0; i < 4; i++) {
      HttpObject resp = c.get(req);
      assertEquals(resp.getHttpVersion(), "HTTP/1.1");
      assertEquals(resp.getStatus(), "404 Not Found");
      assertEquals(resp.headers.size(), 4);
      assertEquals(resp.headers.get("Content-Type"), "text/html; charset=UTF-8");
      assertEquals(resp.headers.get("Server"), "xio2");
    }
  }
}