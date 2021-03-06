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
import com.xjeffrose.xio2.Request;
import com.xjeffrose.xio2.http.Http;
import com.xjeffrose.xio2.http.client.Client;

public class ProxyHttpService extends HttpService {
  private final String proxiedService;
  public ProxyHttpService(String proxiedService) {
    this.proxiedService = proxiedService;
  }

  @Override
  public void handle(ChannelContext ctx, Request req) {

    Client c = null;

    if (ctx.isSecure()) {
      c = Http.newTLSClient(proxiedService);

    } else {
      c = Http.newClient(proxiedService);
    }

    c.proxy(ctx);
  }
}
