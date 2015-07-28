package com.xjeffrose.xio2.server;

import com.xjeffrose.xio2.http.HttpObject;

public interface Handler {
  void handle(ChannelContext ctx, HttpObject req);
}