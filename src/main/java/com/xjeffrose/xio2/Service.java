package com.xjeffrose.xio2;

public interface Service {

  void handle(ChannelContext ctx, Request req);

}
