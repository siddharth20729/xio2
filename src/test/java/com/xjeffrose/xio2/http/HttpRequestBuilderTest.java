package com.xjeffrose.xio2.http;

import java.net.URI;
import org.junit.Test;

import static org.junit.Assert.*;

public class HttpRequestBuilderTest {

  @Test
  public void testFullBuild() throws Exception {

    HttpRequest testReq = new HttpRequest.Builder()
        .method(Http.Method.GET)
        .url("/")
        .version(Http.Version.HTTP1_1)
        .body("This is the body")
        .tls(false)
        .build();

    assertEquals(testReq.getMethod(), "GET");
    assertEquals(testReq.getUri(), new URI("/"));
    assertEquals(testReq.getHttpVersion(), "HTTP/1.1");
    assertEquals(testReq.headers.get("User-Agent"), "xio2");
    assertEquals(testReq.headers.get("Accept"), "*/*");
    assertEquals(testReq.headers.get("Content-Length"), "16");
    assertEquals(testReq.getBody(), "This is the body");
  }

  @Test
  public void testSimpleBuild() throws Exception {

    HttpRequest testReq = new HttpRequest.Builder()
        .url("/")
        .build();

    assertEquals(testReq.getMethod(), "GET");
    assertEquals(testReq.getUri(), new URI("/"));
    assertEquals(testReq.getHttpVersion(), "HTTP/1.1");
    assertEquals(testReq.headers.get("User-Agent"), "xio2");
    assertEquals(testReq.headers.get("Accept"), "*/*");
    assertEquals(testReq.headers.get("Content-Length"), "0");
  }
}