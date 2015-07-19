package com.xjeffrose.xio2.http;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import org.junit.Test;

import static org.junit.Assert.*;

public class HttpResponseTest {
  HttpResponse resp = new HttpResponse();

  @Test
  public void testToString() throws Exception {
    assertEquals(HttpResponse.Status.NOT_FOUND.toString(), "404 Not Found");
  }

  @Test
  public void testDefaultResponseNoBody() throws Exception {

    HttpResponse testDefaultResponse = HttpResponse.
        DefaultResponse(Http.Version.HTTP1_1, Http.Status.NOT_FOUND);

    assertEquals(testDefaultResponse.getHttpVersion(), "HTTP/1.1");
    assertEquals(testDefaultResponse.getStatus(), "404 Not Found");
    assertEquals(testDefaultResponse.headers.size(), 3);
    assertEquals(testDefaultResponse.headers.get("Content-Type"), "text/html; charset=UTF-8");
    assertEquals(testDefaultResponse.headers.get("Server"), "xio2");

  }

  @Test
  public void testDefaultResponseWithBody() throws Exception {

    HttpResponse testDefaultResponse = HttpResponse.
        DefaultResponse(Http.Version.HTTP1_1, Http.Status.OK, "This is the body");
  }

//  @Test
//  public void testDate() throws Exception {
//    assertEquals(resp.date(), ZonedDateTime
//        .now(ZoneId.of("UTC"))
//        .format(DateTimeFormatter.RFC_1123_DATE_TIME));
//  }
}