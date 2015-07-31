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