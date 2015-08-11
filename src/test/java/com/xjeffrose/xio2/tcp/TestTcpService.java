package com.xjeffrose.xio2.tcp;

import com.xjeffrose.xio2.ChannelContext;
import com.xjeffrose.xio2.Request;
import com.xjeffrose.xio2.tcp.server.TcpService;
import com.xjeffrose.xio2.util.BB;

public class TestTcpService extends TcpService {

  @Override
  public void handle(ChannelContext ctx, Request req) {
    ctx.write(BB.StringtoBB("This is a tcp test"));
  }
}
