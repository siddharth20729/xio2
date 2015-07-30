package com.xjeffrose.xio2.http.server;

import com.xjeffrose.xio2.http.Http;
import com.xjeffrose.xio2.http.client.Client;

public class ProxyService extends Service {
  private final String proxiedService;
  public ProxyService(String proxiedService) {
    this.proxiedService = proxiedService;
  }

  @Override
  public void handle(ChannelContext ctx) {

    Client c = null;

    if (ctx.ssl) {
      c = Http.newSslClient(proxiedService);

    } else {
      c = Http.newClient(proxiedService);
    }

    c.proxy(ctx);
  }
}
