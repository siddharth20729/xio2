package com.xjeffrose.xio2.http.server;

public interface Handler {

  void handle(ChannelContext ctx);
}