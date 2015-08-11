package com.xjeffrose.xio2.tcp.server;

import com.xjeffrose.xio2.ChannelContext;
import com.xjeffrose.xio2.Handler;
import com.xjeffrose.xio2.Request;
import com.xjeffrose.xio2.http.Http;
import java.nio.ByteBuffer;

public class TcpHandler implements Handler {

  private TcpRequest req = new TcpRequest();
  private TcpService service = null;

  @Override
  public boolean parse(ChannelContext ctx) {
    return true;
  }

  @Override
  public void handle(ChannelContext ctx) {
    ctx.state = ChannelContext.State.start_response;
    service.handle(ctx, req);
  }

  @Override
  public void handleError(ChannelContext ctx) {

  }

  @Override
  public void handleFatalError(ChannelContext ctx) {

  }

  public void addService(TcpService service) {
    this.service = service;
  }

  @Override
  public Request getReq() {
    return null;
  }

  @Override
  public Http.Method getMethod() {
    return null;
  }

  @Override
  public ByteBuffer getInputBuffer() {
    return req.inputBuffer;
  }
}
