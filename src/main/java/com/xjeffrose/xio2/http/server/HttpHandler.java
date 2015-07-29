package com.xjeffrose.xio2.http.server;

import com.xjeffrose.xio2.http.HttpObject;
import java.util.concurrent.ConcurrentLinkedDeque;

public abstract class HttpHandler implements Handler {
  public HttpObject req;
  public ChannelContext ctx;

  private final ConcurrentLinkedDeque<HttpHandler> httpHandlerList = new ConcurrentLinkedDeque<HttpHandler>();

  protected HttpHandler() { }

  public void handle(ChannelContext ctx, HttpObject req) {
    this.ctx = ctx;
    this.req = req;

    switch (req.method_) {
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
      default:
        handleNotFound();
        return;
    }
  }

  public void handleNotFound() { }

  public void handleGet() { }

  public void handlePost() { }

  public void handlePut() { }

  public void handleDelete() { }

  public void andThen(HttpHandler httpHandler) {
    httpHandlerList.addLast(httpHandler);
  }

  private void serviceStream() {
    while (httpHandlerList.size() > 0) {
      httpHandlerList.removeLast().handle(ctx, req);
    }
  }
}
