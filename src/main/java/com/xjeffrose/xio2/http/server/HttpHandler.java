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
import com.xjeffrose.xio2.Firewall;
import com.xjeffrose.xio2.Handler;
import com.xjeffrose.xio2.RateLimiter;
import com.xjeffrose.xio2.SecureChannelContext;
import com.xjeffrose.xio2.http.Http;
import com.xjeffrose.xio2.http.HttpRequest;
import com.xjeffrose.xio2.http.HttpRequestParser;
import com.xjeffrose.xio2.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HttpHandler implements Handler {
  private final Map<Route, HttpService> routes = new ConcurrentHashMap<>();

  public Firewall firewall = new Firewall();
  public RateLimiter rateLimiter = new RateLimiter();

  public HttpHandler() { }

  public ChannelContext buildChannelContext(SocketChannel channel) {
    return new ChannelContext(channel, this);
  }

  @Override
  public void secureContext(SecureChannelContext secureChannelContext) { }

  @Override
  public Firewall firewall() {
    return firewall;
  }

  public boolean parse(ChannelContext ctx) {
    ctx.req = new HttpRequest(ctx.inputBuffer);
    return new HttpRequestParser().parse((HttpRequest)ctx.req);
  }

  public void handle(ChannelContext ctx) {
    HttpRequest req = (HttpRequest)ctx.req;
    final String uri = req.getUri().toString();
    for (Map.Entry<Route, HttpService> entry : routes.entrySet()) {
      if (entry.getKey().matches(uri)) {
        ctx.state = ChannelContext.State.start_response;
        entry.getValue().handle(ctx, req);
        return;
      }
    }
    ctx.state = ChannelContext.State.start_response;
    ctx.write(HttpResponse.DefaultResponse(Http.Version.HTTP1_1, Http.Status.NOT_FOUND).toBB());
  }

  public void handleError(ChannelContext ctx) {
    ctx.write(HttpResponse.DefaultResponse(Http.Version.HTTP1_1, Http.Status.BAD_REQUEST).toBB());
  }

  public void handleFatalError(ChannelContext ctx) {
    ctx.write(HttpResponse.DefaultResponse(Http.Version.HTTP1_1, Http.Status.INTERNAL_SERVER_ERROR).toBB());
    ctx.flush();
  }

  public void addRoute(String route, HttpService httpService) {
    routes.putIfAbsent(Route.build(route), httpService);
  }
}
