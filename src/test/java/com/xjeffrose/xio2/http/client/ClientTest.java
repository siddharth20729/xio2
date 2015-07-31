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
package com.xjeffrose.xio2.http.client;

import com.xjeffrose.xio2.http.*;
import com.xjeffrose.xio2.http.server.ChannelContext;
import com.xjeffrose.xio2.http.server.HttpHandler;
import com.xjeffrose.xio2.http.server.ProxyService;
import com.xjeffrose.xio2.http.server.Server;
import com.xjeffrose.xio2.http.server.Service;
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
  public void testCall() throws Exception {
    s.bind(9018);
    s.serve();

    HttpRequest req = new HttpRequest.Builder()
        .url("/")
        .build();

    Client c = Http.newClient("localhost:9018");

    HttpObject resp = c.call(req);
    assertEquals(resp.getHttpVersion(), "HTTP/1.1");
    assertEquals(resp.getStatus(), "404 Not Found");
    assertEquals(resp.headers.size(), 4);
    assertEquals(resp.headers.get("Content-Type"), "text/html; charset=UTF-8");
    assertEquals(resp.headers.get("Server"), "xio2");
  }

  @Test
  public void testSSLCall() throws Exception {
    Client c = Http.newClient("localhost:9017");

    s.ssl(true);
    c.ssl(true);

    s.bind(9017);
    s.serve();

    HttpRequest req = new HttpRequest.Builder()
        .url("/")
        .build();

    HttpObject resp = c.call(req);
    assertEquals(resp.getHttpVersion(), "HTTP/1.1");
    assertEquals(resp.getStatus(), "404 Not Found");
    assertEquals(resp.headers.size(), 4);
    assertEquals(resp.headers.get("Content-Type"), "text/html; charset=UTF-8");
    assertEquals(resp.headers.get("Server"), "xio2");
  }

  @Test
  public void testServerSet() throws Exception {

    s.ssl(false);
    s.bind(9031);
    s.bind(9032);
    s.bind(9033);
    s.serve();

    Client c = new Client("localhost:9031, localhost:9032, localhost:9033");

    HttpRequest req = new HttpRequest.Builder()
        .url("/")
        .build();

    for (int i = 0; i < 4; i++) {
      HttpObject resp = c.call(req);
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
    s.bind(9021);
    s.bind(9022);
    s.bind(9023);
    s.serve();

    Client c = new Client("localhost:9021, localhost:9022, localhost:9023");

    c.ssl(true);

    HttpRequest req = new HttpRequest.Builder()
        .url("/")
        .build();

    for (int i = 0; i < 4; i++) {
      HttpObject resp = c.call(req);
      assertEquals(resp.getHttpVersion(), "HTTP/1.1");
      assertEquals(resp.getStatus(), "404 Not Found");
      assertEquals(resp.headers.size(), 4);
      assertEquals(resp.headers.get("Content-Type"), "text/html; charset=UTF-8");
      assertEquals(resp.headers.get("Server"), "xio2");
    }
  }

//  @Test
//  public void testProxy() throws Exception {
//    Server service_int = Http.newServer();
//    HttpHandler proxiedHandler = new HttpHandler();
//    proxiedHandler.addRoute("/", new Service() {
//      @Override
//      public void handleGet(ChannelContext ctx) {
//        ctx.write(com.xjeffrose.xio2.http.HttpResponse.DefaultResponse(Http.Version.HTTP1_1, Http.Status.OK, "CONGRATS!\n"));
//      }
//    });
//
//    service_int.serve(9041, proxiedHandler);
//
//    Server client_int = Http.newServer();
//    HttpHandler testHandler = new HttpHandler();
//    testHandler.addRoute("/", new ProxyService("localhost:9041"));
//    client_int.serve(9040, testHandler);
//
//    HttpRequest req = new HttpRequest.Builder()
//        .url("/")
//        .build();
//
//    Client client = Http.newClient("localhost:9040");
//    HttpObject resp = client.call(req);
//
////    assertEquals(1, testHandler.requestsHandled());
////    assertEquals(1, proxiedHandler.requestsHandled());
//    assertEquals(resp.getHttpVersion(), "HTTP/1.0");
//    assertEquals(resp.getStatus(), "200 OK");
//    assertEquals(resp.headers.size(), 4);
////    assertEquals(resp.headers.get("Content-Type"), "text/html; charset=UTF-8");
//    assertEquals(resp.headers.get("Server"), "SimpleHTTP/0.6 Python/2.7.10");
//    assertEquals("CONGRATS!\n", resp.getBody());
//    assertEquals("CONGRATS!\n".length(), Integer.parseInt(resp.headers.get("Content-Length")));
//  }
//
//  @Test
//  public void testMultipleProxyCalls() throws Exception {
//    /*
//    Server service_int = Http.newServer();
//    HttpHandler proxiedHandler = new HttpHandler();
//    proxiedHandler.addRoute("/", new Service() {
//      @Override
//      public void handleGet(ChannelContext ctx) {
//        ctx.write(com.xjeffrose.xio2.http.HttpResponse.DefaultResponse(Http.Version.HTTP1_1, Http.Status.OK, "CONGRATS!\n"));
//      }
//    });
//
//    service_int.serve(9041, proxiedHandler);
//*/
//    Server client_int = Http.newServer();
//    HttpHandler testHandler = new HttpHandler();
//    testHandler.addRoute("/", new ProxyService("localhost:9041"));
//    client_int.serve(9045, testHandler);
//
//    for (int i = 1; i < 11; i++) {
//      HttpRequest req = new HttpRequest.Builder()
//          .url("/")
//          .build();
//
//      Client client = Http.newClient("localhost:9045");
//      HttpObject resp = client.call(req);
//
//      //assertEquals(i, testHandler.requestsHandled());
//      //assertEquals(i, proxiedHandler.requestsHandled());
//      //assertEquals(resp.getHttpVersion(), "HTTP/1.1");
//      assertEquals(resp.getStatus(), "200 OK");
//      assertEquals(resp.headers.size(), 4);
//      //assertEquals(resp.headers.get("Content-Type"), "text/html; charset=UTF-8");
//      //assertEquals(resp.headers.get("Server"), "xio2");
//      assertEquals("CONGRATS!\n", resp.getBody());
//      assertEquals("CONGRATS!\n".length(), Integer.parseInt(resp.headers.get("Content-Length")));
//
//    }
//  }

  @Test
  public void testProxySSL() throws Exception {
    Server service_int = Http.newSslServer();
    HttpHandler proxiedHandler = new HttpHandler();
    proxiedHandler.addRoute("/", new Service() {
      @Override
      public void handleGet(ChannelContext ctx) {
        ctx.write(com.xjeffrose.xio2.http.HttpResponse.DefaultResponse(Http.Version.HTTP1_1, Http.Status.OK, "CONGRATS!\n"));
      }
    });

    service_int.serve(9043, proxiedHandler);

    Server client_int = Http.newSslServer();
    HttpHandler testHandler = new HttpHandler();
    testHandler.addRoute("/", new ProxyService("localhost:9043"));
    client_int.serve(9042, testHandler);

    HttpRequest req = new HttpRequest.Builder()
        .url("/")
        .build();

    Client client = Http.newSslClient("localhost:9042");
    HttpObject resp = client.call(req);

    assertEquals(1, testHandler.requestsHandled());
    assertEquals(1, proxiedHandler.requestsHandled());
    assertEquals(resp.getHttpVersion(), "HTTP/1.1");
    assertEquals(resp.getStatus(), "200 OK");
    assertEquals(resp.headers.size(), 4);
    assertEquals(resp.headers.get("Content-Type"), "text/html; charset=UTF-8");
    assertEquals(resp.headers.get("Server"), "xio2");
    assertEquals("CONGRATS!\n", resp.getBody());
    assertEquals("CONGRATS!\n".length(), Integer.parseInt(resp.headers.get("Content-Length")));
  }
}