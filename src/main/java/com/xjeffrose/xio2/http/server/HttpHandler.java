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
package com.xjeffrose.xio2.http.server;

import com.xjeffrose.xio2.ChannelContext;
import com.xjeffrose.xio2.Handler;
import com.xjeffrose.xio2.http.Http;
import com.xjeffrose.xio2.http.HttpRequest;
import com.xjeffrose.xio2.http.HttpRequestParser;
import com.xjeffrose.xio2.http.HttpResponse;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class HttpHandler implements Handler {
  private final HttpRequest req = new HttpRequest();
  private final AtomicInteger _requestsHandled = new AtomicInteger(0);
  private final Map<Route, HttpService> routes = new ConcurrentHashMap<Route, HttpService>();

  public HttpHandler() { }

  public HttpRequest getReq() {
    return req;
  }

  public Http.Method getMethod() {
    return req.method_;
  }

  public ByteBuffer getInputBuffer() {
    return req.inputBuffer;
  }

  public boolean parse(ChannelContext ctx) {
    final HttpRequestParser parser = new HttpRequestParser();
    return parser.parse(req);
  }

  public void handle(ChannelContext ctx) {
    final String uri = req.getUri().toString();
    for (Map.Entry<Route, HttpService> entry : routes.entrySet()) {
      if (entry.getKey().matches(uri)) {
        ctx.state = ChannelContext.State.start_response;
        entry.getValue().handle(ctx);
        _requestsHandled.incrementAndGet();
        return;
      }
    }
    ctx.state = ChannelContext.State.start_response;
    ctx.write(HttpResponse.DefaultResponse(Http.Version.HTTP1_1, Http.Status.NOT_FOUND).toBB());
  }

  @Override
  public void handleError(ChannelContext ctx) {
    ctx.write(HttpResponse.DefaultResponse(Http.Version.HTTP1_1, Http.Status.BAD_REQUEST).toBB());
  }

  @Override
  public void handleFatalError(ChannelContext ctx) {
    ctx.write(HttpResponse.DefaultResponse(Http.Version.HTTP1_1, Http.Status.INTERNAL_SERVER_ERROR).toBB());
    ctx.flush();
  }

  public void addRoute(String route, HttpService httpService) {
    routes.putIfAbsent(Route.build(route), httpService);
  }

  public int requestsHandled() {
    return _requestsHandled.get();
  }
}
