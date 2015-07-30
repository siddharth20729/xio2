package com.xjeffrose.xio2.http.server;

import com.xjeffrose.xio2.http.HttpRequest;
import java.util.concurrent.ConcurrentLinkedDeque;

public abstract class Service {
  public HttpRequest req;

  private final ConcurrentLinkedDeque<Service> serviceList = new ConcurrentLinkedDeque<Service>();
  public ChannelContext ctx;

  public Service() {
  }

  public void handle(ChannelContext ctx) {
    this.ctx = ctx;
    this.req = ctx.req;

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
        handleGet();
        serviceStream();
        return;
    }
  }

  public void handleGet() { }

  public void handlePost() { }

  public void handlePut() { }

  public void handleDelete() { }

  public void andThen(Service service) {
    serviceList.addLast(service);
  }

  private void serviceStream() {
    while (serviceList.size() > 0) {
      serviceList.removeLast().handle(ctx);
    }
  }
}

