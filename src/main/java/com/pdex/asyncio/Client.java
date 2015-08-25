package com.pdex.asyncio;

class Client {
  final private RequestConnectionFactory requestConnectionFactory;
  final private SelectorLoopStrategy strategy;

  Client(RequestConnectionFactory requestConnectionFactory, SelectorLoopStrategy strategy) {
    this.requestConnectionFactory = requestConnectionFactory;
    this.strategy = strategy;
  }

  private Connector getConnector(Request request, ConnectionFactory factory) {
    return new Connector(request.getAddress(), factory, strategy);
  }

  public void execute(Request request) {
    strategy.getLoop().add(getConnector(request, requestConnectionFactory.build(request)));
  }
}
