package com.pdex.asyncio;

import java.nio.channels.SocketChannel;

public class HttpClientConnectionFactory implements ConnectionFactory {
  final private Request request;

  public HttpClientConnectionFactory(Request request) {
    this.request = request;
  }

  public Connection build(SocketChannel channel) {
    if (request.getURL().getProtocol().equals("https")) {
      return new Connection(channel, new TLSConnection(new HttpClientConnection(request)));
    } else {
      return new Connection(channel, new HttpClientConnection(request));
    }
  }
}
