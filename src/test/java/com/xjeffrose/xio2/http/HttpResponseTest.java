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
package com.xjeffrose.xio2.http;

import org.junit.Test;

import static org.junit.Assert.*;

public class HttpResponseTest {
  HttpResponse resp = new HttpResponse();

  @Test
  public void testToString() throws Exception {
    assertEquals(Http.Status.NOT_FOUND.toString(), "404 Not Found");
  }

  @Test
  public void testDefaultResponseNoBody() throws Exception {

    HttpResponse testDefaultResponse = HttpResponse.
        DefaultResponse(Http.Version.HTTP1_1, Http.Status.NOT_FOUND);

    assertEquals(testDefaultResponse.getHttpVersion(), "HTTP/1.1");
    assertEquals(testDefaultResponse.getStatus(), "404 Not Found");
    assertEquals(testDefaultResponse.headers.size(), 3);
    assertEquals(testDefaultResponse.headers.get("Content-Type"), "application/text;charset=UTF-8");
    assertEquals(testDefaultResponse.headers.get("Server"), "xio2");

  }

  @Test
     public void testDefaultResponseWithBody() throws Exception {

    HttpResponse testDefaultResponse = HttpResponse.
        DefaultResponse(Http.Version.HTTP1_1, Http.Status.OK, "This is the body");

    assertEquals(testDefaultResponse.getHttpVersion(), "HTTP/1.1");
    assertEquals(testDefaultResponse.getStatus(), "200 OK");
    assertEquals(testDefaultResponse.headers.size(), 4);
    assertEquals(testDefaultResponse.headers.get("Content-Type"), "application/text;charset=UTF-8");
    assertEquals(testDefaultResponse.headers.get("Server"), "xio2");
    assertEquals(testDefaultResponse.headers.get("Content-Length"), "16");
    assertEquals(testDefaultResponse.getBody(), "This is the body");

  }

//  @Test
//  public void testDefaultResponsetoStringNoBody() throws Exception {
//
//    HttpResponse testDefaultResponse = HttpResponse.
//        DefaultResponse(Http.Version.HTTP1_1, Http.Status.OK);
//
//    String expectedResponse = "HTTP/1.1 200 OK\r\n" +
//        "Content-Type: text/html; charset=UTF-8 \r\n" +
//        "Date: Sun, 19 Jul 2015 22:13:30 GMT \r\n" +
//        "Server: xio2 \r\n" +
//        " \r\n";
//
//    assertEquals(testDefaultResponse.toString(), expectedResponse);
//  }
//
//  @Test
//  public void testDefaultResponsetoStringWithBody() throws Exception {
//
//    HttpResponse testDefaultResponse = HttpResponse.
//        DefaultResponse(Http.Version.HTTP1_1, Http.Status.OK, "This is the body");
//
//    String expectedResponse = "HTTP/1.1 200 OK\r\n" +
//    "Content-Type: text/html; charset=UTF-8 \r\n" +
//    "Date: Sun, 19 Jul 2015 22:13:30 GMT \r\n" +
//    "Server: xio2 \r\n" +
//        "Content-Length: 16 \r\n" +
//        " \r\n" +
//        "This is the body";
//
//    assertEquals(testDefaultResponse.toString(), expectedResponse);
//  }

//  @Test
//  public void testDate() throws Exception {
//    assertEquals(resp.date(), ZonedDateTime
//        .now(ZoneId.of("UTC"))
//        .format(DateTimeFormatter.RFC_1123_DATE_TIME));
//  }
}