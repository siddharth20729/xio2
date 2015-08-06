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

public class HttpRequestTest {

  @Test
  public void testAddHeader() throws Exception {

    HttpRequest testReq = new HttpRequest.Builder()
        .url("/")
        .build();

    testReq.headers.set("X-TEST-HEADER", "test header value");

    assertEquals("test header value", testReq.headers.get("X-TEST-HEADER"));
  }
}