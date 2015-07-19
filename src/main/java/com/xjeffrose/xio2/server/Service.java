package com.xjeffrose.xio2.server;

import com.xjeffrose.xio2.http.HttpRequest;
import com.xjeffrose.xio2.http.HttpResponse;
import java.util.concurrent.ConcurrentLinkedDeque;

public class Service {
  public Route route;
  public HttpRequest req;
  public HttpResponse resp;

  private final ConcurrentLinkedDeque<Service> serviceList = new ConcurrentLinkedDeque<Service>();

  public Service() {
  }

  public void handle(Route route, HttpRequest req, HttpResponse resp) {
    this.route = route;
    this.req = req;
    this.resp = resp;
  }

  public void handle() {

//    switch(req.method) {
//      case HttpObject.HttpMethod.GET:
//        handleGet();
//        serviceStream();
//        return;
//      case POST:
//        handlePost();
//        serviceStream();
//        return;
//      case PUT:
//        handlePut();
//        serviceStream();
//        return;
//      case DELETE:
//        handleDelete();
//        serviceStream();
//        return;
//      default:
//        handleGet();
//        serviceStream();
//        return;
//    }
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
      //serviceList.removeLast().handle(route,req,resp);
    }
  }
}
