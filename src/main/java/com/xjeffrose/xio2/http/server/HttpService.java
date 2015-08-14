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

import com.xjeffrose.xio2.Request;
import com.xjeffrose.xio2.Service;
import com.xjeffrose.xio2.ChannelContext;
import com.xjeffrose.xio2.http.HttpRequest;
import java.util.concurrent.ConcurrentLinkedDeque;

public abstract class HttpService implements Service {
  public final ConcurrentLinkedDeque<HttpService> httpServiceList = new ConcurrentLinkedDeque<>();
  public ChannelContext ctx;
  public HttpRequest req;

  public HttpService() { }

  public void handle(ChannelContext ctx, Request req) {
    this.ctx = ctx;
    this.req = (HttpRequest) req;

    switch (((HttpRequest) ctx.req).method_) {
      case GET:
        handleGet();
        serviceStream();
        return;
      case POST:
        handlePost();
        serviceStream();
        return;
      case PUT:
        handlePut();
        serviceStream();
        return;
      case DELETE:
        handleDelete();
        serviceStream();
        return;
      case TRACE:
        handleTrace();
        serviceStream();
        return;
      case OPTION:
        handleOption();
        serviceStream();
        return;
      case CONNECT:
        handleConnect();
        serviceStream();
        return;
      case PATCH:
        handlePatch();
        serviceStream();
        return;
      default:
        handleGet();
        serviceStream();
        return;
    }
  }

  public void handleGet() { }

  public void handlePost() { }

  public void handlePut() { }

  public void handleDelete() { }

  private void handleTrace() { }

  private void handleOption() { }

  private void handleConnect() { }

  private void handlePatch() { }

  public void andThen(HttpService httpService) {
    httpServiceList.addLast(httpService);
  }

  private void serviceStream() {
    while (httpServiceList.size() > 0) {
      httpServiceList.removeLast().handle(ctx, req);
    }
  }
}

