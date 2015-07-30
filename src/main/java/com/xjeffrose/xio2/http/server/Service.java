package com.xjeffrose.xio2.http.server;

import com.xjeffrose.xio2.http.HttpRequest;
import java.util.concurrent.ConcurrentLinkedDeque;

public abstract class Service {
  private final ConcurrentLinkedDeque<Service> serviceList = new ConcurrentLinkedDeque<Service>();

  public HttpRequest req;
  public ChannelContext ctx;

  public Service() { }

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

  public void andThen(Service service) {
    serviceList.addLast(service);
  }

  private void serviceStream() {
    while (serviceList.size() > 0) {
      serviceList.removeLast().handle(ctx);
    }
  }
}

