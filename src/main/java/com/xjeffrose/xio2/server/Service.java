package com.xjeffrose.xio2.server;

import com.xjeffrose.xio2.http.HttpRequest;
import java.util.concurrent.ConcurrentLinkedDeque;

public class Service {
  public HttpRequest req;
  public ChannelContext ctx;

  private final ConcurrentLinkedDeque<Service> serviceList = new ConcurrentLinkedDeque<Service>();

  protected Service() { }

  public void handle(ChannelContext ctx, HttpRequest req) {
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

  public void andThen(Service service) {
    serviceList.addLast(service);
  }

  private void serviceStream() {
    while (serviceList.size() > 0) {
      serviceList.removeLast().handle(ctx, req);
    }
  }
}
