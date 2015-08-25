package com.pdex.asyncio;

import java.net.InetSocketAddress;
import java.net.URL;

class Client {
  final private RequestConnectionFactory requestConnectionFactory;
  final private SelectorLoopStrategy strategy;

  Client(RequestConnectionFactory requestConnectionFactory, SelectorLoopStrategy strategy) {
    this.requestConnectionFactory = requestConnectionFactory;
    this.strategy = strategy;
  }

  private InetSocketAddress getAddress(URL url) {
    String host = url.getHost();
    int port = url.getPort();
    if (port == -1) {
      port = url.getDefaultPort();
    }
    return new InetSocketAddress(host, port);
  }

  private Connector getConnector(Request request, ConnectionFactory factory) {
    return new Connector(getAddress(request.getURL()), factory, strategy);
  }

  public void execute(Request request) {
    strategy.getLoop().add(getConnector(request, requestConnectionFactory.build(request)));
  }
}
