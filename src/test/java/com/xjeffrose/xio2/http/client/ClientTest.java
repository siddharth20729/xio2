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
import com.xjeffrose.xio2.http.server.HttpHandler;
import com.xjeffrose.xio2.http.server.HttpsHandler;
import com.xjeffrose.xio2.http.server.ProxyHttpService;
import com.xjeffrose.xio2.Server;
import com.xjeffrose.xio2.http.server.HttpService;
import com.xjeffrose.xio2.util.OS;
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

    HttpResponse resp = c.call(req);
    assertEquals(resp.getHttpVersion(), "HTTP/1.1");
    assertEquals(resp.getStatus(), "404 Not Found");
    assertEquals(resp.headers.size(), 3);
    assertEquals(resp.headers.get("Content-Type"), "text/html; charset=UTF-8");
    assertEquals(resp.headers.get("Server"), "xio2");
  }

  @Test
  public void testMultipleCalls() throws Exception {
    new Thread(new SimpleTestServer(9039)).start();

    //Give Jetty a Min to Wake up and run
    Thread.sleep(500);

    HttpRequest req = new HttpRequest.Builder()
        .url("/")
        .build();

    for (int i = 1; i <= 10000; i++) {
      Client c = Http.newClient("localhost:9039");

      HttpResponse resp = c.call(req);
      assertEquals(resp.getHttpVersion(), "HTTP/1.1");
      assertEquals(resp.getStatus(), "200 OK");
      assertEquals(resp.headers.size(), 4);
      assertEquals(resp.headers.get("Content-Type"), "text/html;charset=utf-8");
      assertEquals(resp.headers.get("Server"), "Jetty(9.3.1.v20150714)");
      assertEquals("CONGRATS!\n", resp.getBody());
      assertEquals("CONGRATS!\n".length(), Integer.parseInt(resp.headers.get("Content-Length")));
      assertTrue("Iteration [" + i + "] Open file descriptors less than 10% of max: " + OS.getOpenFileDescriptorCount() + "/" + OS.getMaxFileDescriptorCount(),
          OS.getOpenFileDescriptorCount() < OS.getMaxFileDescriptorCount() * 0.10);
    }
  }

  @Test
  public void testSSLCall() throws Exception {
    Client c = Http.newClient("localhost:9017");

    c.tls(true);

    s.bind(9017, true);
    s.serve();

    HttpRequest req = new HttpRequest.Builder()
        .url("/")
        .build();

    HttpObject resp = c.call(req);
    assertEquals(resp.getHttpVersion(), "HTTP/1.1");
    assertEquals(resp.getStatus(), "404 Not Found");
    assertEquals(resp.headers.size(), 3);
    assertEquals(resp.headers.get("Content-Type"), "text/html; charset=UTF-8");
    assertEquals(resp.headers.get("Server"), "xio2");
  }

  @Test
  public void testServerSet() throws Exception {

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
      assertEquals(resp.headers.size(), 3);
      assertEquals(resp.headers.get("Content-Type"), "text/html; charset=UTF-8");
      assertEquals(resp.headers.get("Server"), "xio2");
    }
  }

//  @Test
//  public void testServerSetSSL() throws Exception {
//
//    s.bind(9021, true);
//    s.bind(9022, true);
//    s.bind(9023, true);
//    s.serve();
//
//    Client c = new Client("localhost:9021, localhost:9022, localhost:9023");
//
//    c.tls(true);
//
//    HttpRequest req = new HttpRequest.Builder()
//        .url("/")
//        .build();
//
//    for (int i = 0; i < 4; i++) {
//      HttpObject resp = c.call(req);
//      assertEquals(resp.getHttpVersion(), "HTTP/1.1");
//      assertEquals(resp.getStatus(), "404 Not Found");
//      assertEquals(resp.headers.size(), 3);
//      assertEquals(resp.headers.get("Content-Type"), "text/html; charset=UTF-8");
//      assertEquals(resp.headers.get("Server"), "xio2");
//    }
//  }

  @Test
  public void testProxy() throws Exception {
    new Thread(new SimpleTestServer(9041)).start();

    //Give Jetty a Min to Wake up and run
    Thread.sleep(500);
    Server client_int = Http.newServer();
    HttpHandler testHandler = new HttpHandler();
    testHandler.addRoute("/", new ProxyHttpService("localhost:9041"));
    client_int.serve(9040, testHandler);

    HttpRequest req = new HttpRequest.Builder()
        .url("/")
        .build();

    req.headers.set("X-TEST-HEADER", "Test/header/value");

    Client client = Http.newClient("localhost:9040");
    HttpObject resp = client.call(req);

    assertEquals(resp.getHttpVersion(), "HTTP/1.1");
    assertEquals(resp.getStatus(), "200 OK");
    assertEquals(resp.headers.size(), 5);
    assertEquals(resp.headers.get("Content-Type"), "text/html;charset=utf-8");
    assertEquals(resp.headers.get("Server"), "Jetty(9.3.1.v20150714)");
    assertEquals(resp.headers.get("X-TEST-HEADER"), "Test/header/value");

    assertEquals("CONGRATS!\n", resp.getBody());
    assertEquals("CONGRATS!\n".length(), Integer.parseInt(resp.headers.get("Content-Length")));
  }

  @Test
  public void testMultipleProxyCalls() throws Exception {
    new Thread(new SimpleTestServer(9049)).start();

    //Give Jetty a Min to Wake up and run
    Thread.sleep(500);
    Server client_int = Http.newServer();
    HttpHandler testHandler = new HttpHandler();
    testHandler.addRoute("/", new ProxyHttpService("localhost:9049"));
    client_int.serve(9045, testHandler);

    for (int i = 1; i < 11; i++) {
      HttpRequest req = new HttpRequest.Builder()
          .url("/")
          .build();

      Client client = Http.newClient("localhost:9045");
      HttpObject resp = client.call(req);

      assertEquals(resp.getHttpVersion(), "HTTP/1.1");
      assertEquals(resp.getStatus(), "200 OK");
      assertEquals(resp.headers.size(), 4);
      assertEquals(resp.headers.get("Content-Type"), "text/html;charset=utf-8");
      assertEquals(resp.headers.get("Server"), "Jetty(9.3.1.v20150714)");
      assertEquals("CONGRATS!\n", resp.getBody());
      assertEquals("CONGRATS!\n".length(), Integer.parseInt(resp.headers.get("Content-Length")));

    }
  }

  @Test
  public void testProxySSL() throws Exception {
    Server service_int = Http.newServer();
    HttpsHandler proxiedHandler = new HttpsHandler();
    proxiedHandler.addRoute("/", new HttpService() {
      @Override
      public void handleGet() {
        ctx.write(com.xjeffrose.xio2.http.HttpResponse.DefaultResponse(Http.Version.HTTP1_1, Http.Status.OK, "CONGRATS!\n").toBB());
      }
    });

    service_int.serve(9043, proxiedHandler);

    Server client_int = Http.newServer();
    HttpsHandler testHandler = new HttpsHandler();
    testHandler.addRoute("/", new ProxyHttpService("localhost:9043"));
    client_int.serve(9042, testHandler);

    HttpRequest req = new HttpRequest.Builder()
        .url("/")
        .build();

    Client client = Http.newTLSClient("localhost:9042");
    HttpObject resp = client.call(req);

    assertEquals(resp.getHttpVersion(), "HTTP/1.1");
    assertEquals(resp.getStatus(), "200 OK");
    assertEquals(resp.headers.size(), 4);
    assertEquals(resp.headers.get("Content-Type"), "text/html; charset=UTF-8");
    assertEquals(resp.headers.get("Server"), "xio2");
    assertEquals("CONGRATS!\n", resp.getBody());
    assertEquals("CONGRATS!\n".length(), Integer.parseInt(resp.headers.get("Content-Length")));
  }
}