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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class HttpRequestParserTest {
  public String payload1 = "GET / HTTP/1.1\r\n" +
      "User-Agent: curl/7.35.0\r\n" +
      "Host: localhost:8000\r\n" +
      "Accept: */*\r\n" +
      "\r\n";

  public String payload2 = "POST / HTTP/1.1\r\n" +
      "User-Agent: curl/7.35.0\r\n" +
      "Host: localhost:8000\r\n" +
      "Content-Length: 16\r\n" +
      "Accept: */*\r\n" +
      "\r\n" +
      "This is the body";

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {

  }

  @Test
  public void testParseGET() throws Exception {
    HttpRequestParser parser = new HttpRequestParser();
    HttpRequest request = new HttpRequest();
    request.inputBuffer.put(payload1.getBytes());
    Boolean parseOk = parser.parse(request);

    assertTrue(parseOk);
    assertEquals(request.getMethod(), "GET");
    assertEquals(request.getUri().toString(), "/");
    assertEquals(request.getHttpVersion(),"HTTP/1.1");
    assertEquals(request.headers.size(), 3);
    assertEquals(request.headers.get("User-Agent"), "curl/7.35.0");
    assertEquals(request.headers.get("Host"), "localhost:8000");
    assertEquals(request.headers.get("Accept"), "*/*");
  }

  @Test
  public void testParsePOST() throws Exception {
    HttpRequestParser parser = new HttpRequestParser();
    HttpRequest request = new HttpRequest();
    request.inputBuffer.put(payload2.getBytes());
    Boolean parseOk = parser.parse(request);

    assertTrue(parseOk);
    assertEquals(request.getMethod(), "POST");
    assertEquals(request.getUri().toString(), "/");
    assertEquals(request.getHttpVersion(),"HTTP/1.1");
    assertEquals(request.headers.size(), 4);
    assertEquals(request.headers.get("User-Agent"), "curl/7.35.0");
    assertEquals(request.headers.get("Host"), "localhost:8000");
    assertEquals(request.headers.get("Accept"), "*/*");
    assertEquals(request.headers.get("Content-Length"), "16");
    assertEquals(request.getBody(), "This is the body");
    assertEquals(request.getBody().length(), Integer.parseInt(request.headers.get("Content-Length")));
  }

  @Test
  public void testRequest() throws Exception {

  }
}