package com.xjeffrose.xio2.tcp.server;

import com.xjeffrose.xio2.ChannelContext;
import com.xjeffrose.xio2.Handler;
import com.xjeffrose.xio2.Request;
import com.xjeffrose.xio2.SecureChannelContext;
import com.xjeffrose.xio2.http.Http;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class TcpHandler implements Handler {

  private final boolean tls;
  private TcpRequest req = new TcpRequest();
  private TcpService service = null;

  public TcpHandler(boolean tls) {
    this.tls = tls;
  }

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

  @Override
  public ChannelContext buildChannelContext(SocketChannel channel) {
    if (tls) {
      return new SecureChannelContext(channel, this);
    } else {
      return new ChannelContext(channel, this);
    }
  }
}
