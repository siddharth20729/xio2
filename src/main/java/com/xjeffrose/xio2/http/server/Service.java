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

import java.util.concurrent.ConcurrentLinkedDeque;

public abstract class Service {
  private final ConcurrentLinkedDeque<Service> serviceList = new ConcurrentLinkedDeque<Service>();

  public Service() { }

  public void handle(ChannelContext ctx) {
    switch (ctx.req.method_) {
      case GET:
        handleGet(ctx);
        serviceStream(ctx);
        return;
      case POST:
        handlePost(ctx);
        serviceStream(ctx);
        return;
      case PUT:
        handlePut(ctx);
        serviceStream(ctx);
        return;
      case DELETE:
        handleDelete(ctx);
        serviceStream(ctx);
        return;
      case TRACE:
        handleTrace(ctx);
        serviceStream(ctx);
        return;
      case OPTION:
        handleOption(ctx);
        serviceStream(ctx);
        return;
      case CONNECT:
        handleConnect(ctx);
        serviceStream(ctx);
        return;
      case PATCH:
        handlePatch(ctx);
        serviceStream(ctx);
        return;
      default:
        handleGet(ctx);
        serviceStream(ctx);
        return;
    }
  }

  public void handleGet(ChannelContext ctx) { }

  public void handlePost(ChannelContext ctx) { }

  public void handlePut(ChannelContext ctx) { }

  public void handleDelete(ChannelContext ctx) { }

  private void handleTrace(ChannelContext ctx) { }

  private void handleOption(ChannelContext ctx) { }

  private void handleConnect(ChannelContext ctx) { }

  private void handlePatch(ChannelContext ctx) { }

  public void andThen(Service service) {
    serviceList.addLast(service);
  }

  private void serviceStream(ChannelContext ctx) {
    while (serviceList.size() > 0) {
      serviceList.removeLast().handle(ctx);
    }
  }
}

