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
 *
 */

package com.xjeffrose.xio2.http.server;

import com.xjeffrose.xio2.http.Http;
import com.xjeffrose.xio2.http.HttpResponse;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class HttpHandler {
  private final AtomicInteger _requestsHandled = new AtomicInteger(0);
  private final Map<Route, Service> routes = new ConcurrentHashMap<Route, Service>();

  public HttpHandler() { }

  public void handle(ChannelContext ctx) {

    final String uri = ctx.req.getUri().toString();
    for (Map.Entry<Route, Service> entry : routes.entrySet()) {
      if (entry.getKey().matches(uri)) {
        ctx.state = ChannelContext.State.start_response;
        entry.getValue().handle(ctx);
        _requestsHandled.incrementAndGet();
        return;
      }
    }
    ctx.state = ChannelContext.State.start_response;
    ctx.write(HttpResponse.DefaultResponse(Http.Version.HTTP1_1, Http.Status.NOT_FOUND));
  }

  public void addRoute(String route, Service service) {
    routes.putIfAbsent(Route.build(route), service);
  }

  public int requestsHandled() {
    return _requestsHandled.get();
  }
}
