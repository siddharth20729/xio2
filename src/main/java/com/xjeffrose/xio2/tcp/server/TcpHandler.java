package com.xjeffrose.xio2.tcp.server;

import com.xjeffrose.xio2.ChannelContext;
import com.xjeffrose.xio2.Firewall;
import com.xjeffrose.xio2.Handler;
import com.xjeffrose.xio2.RateLimiter;
import com.xjeffrose.xio2.Request;
import com.xjeffrose.xio2.SecureChannelContext;
import com.xjeffrose.xio2.TLS.TLS;
import com.xjeffrose.xio2.http.Http;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class TcpHandler implements Handler {

  private final boolean tls;
  private TcpRequest req = new TcpRequest();
  private TcpService service = null;
  private boolean selfSignedCert;
  private String keyPath;
  private String x509CertPath;
  public Firewall firewall = new Firewall();
  public RateLimiter rateLimiter = new RateLimiter();

  public TcpHandler(boolean tls) {
    this.tls = tls;
    this.selfSignedCert = tls;
  }

  public TcpHandler(String keyPath, String x509CertPath) {
    this.tls = true;
    this.keyPath = keyPath;
    this.x509CertPath = x509CertPath;
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
  public ChannelContext buildChannelContext(SocketChannel channel) {
    if (tls) {
      return new SecureChannelContext(channel, this);
    } else {
      return new ChannelContext(channel, this);
    }
  }

  @Override
  public void secureContext(SecureChannelContext secureChannelContext) {
    final TLS tls;
    if (selfSignedCert) {
      tls = new TLS(secureChannelContext);
    } else {
      tls = new TLS(secureChannelContext, keyPath, x509CertPath);
    }
    tls.doHandshake();
  }

  @Override
  public Firewall firewall() {
    return firewall;
  }
}
