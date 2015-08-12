/*
 *  Copyright (C) 2015 Jeff Rose
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
 *
 */

package com.xjeffrose.xio2.http.server;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.xjeffrose.xio2.Server;
import com.xjeffrose.xio2.http.Http;
import java.io.IOException;
import org.junit.Test;

import static org.junit.Assert.*;

public class FileHandlerTest {

  @Test
  public void testHandle() throws Exception {
    Server s = Http.newServer();
    s.bind(9012, new FileHandler("src/test/resources"));
    s.serve();

    OkHttpClient client = new OkHttpClient();
    Request request = new Request.Builder()
        .url("http://localhost:9012/test.txt")
        .build();

    Response response = client.newCall(request).execute();
    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

    assertTrue(response.isSuccessful());
    assertEquals(response.code(), 200);
    assertEquals("this is a test.txt", response.body().string());  }
}