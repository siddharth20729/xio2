package com.xjeffrose.xio2.http.server;

import com.xjeffrose.xio2.http.Http;
import com.xjeffrose.xio2.http.HttpObject;
import com.xjeffrose.xio2.http.HttpResponse;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class HttpHandler implements Handler {
  public HttpObject req;
  public ChannelContext ctx;
  private final Map<Route, Service> routes = new ConcurrentHashMap<Route, Service>();

  protected HttpHandler() { }

  public void handle(ChannelContext ctx) {
    this.ctx = ctx;
    this.req = ctx.req;

    final String uri = req.getUri().toString();
      for (Map.Entry<Route, Service> entry : routes.entrySet()) {
        if (entry.getKey().matches(uri)) {
          ctx.state = ChannelContext.State.start_response;
          entry.getValue().handle(ctx);
        }
      }
      ctx.state = ChannelContext.State.start_response;
      ctx.write(HttpResponse.DefaultResponse(Http.Version.HTTP1_1, Http.Status.NOT_FOUND));
  }

  void addRoute(String route, Service service) {
    routes.putIfAbsent(Route.build(route), service);
  }
}
