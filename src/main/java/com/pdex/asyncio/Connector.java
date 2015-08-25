package com.pdex.asyncio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Connector extends Selectable {
  private static final Logger log = Logger.getLogger(Connector.class.getName());

  private SocketChannel channel;
  private final InetSocketAddress address;
  private final ConnectionFactory factory;
  private final SelectorLoopStrategy strategy;
  private SelectionKey key;

  public Connector(InetSocketAddress address, ConnectionFactory factory, SelectorLoopStrategy strategy) {
    this.address = address;
    this.factory = factory;
    this.strategy = strategy;
  }

  public void connect() throws IOException {
    if (channel.finishConnect()) {
      key.interestOps(0);
      Connection connection = factory.build(channel);
      strategy.getLoop().add(connection);
      log.info("Connected outgoing connection to address '" + new Address(address) + "' from address '" + new Address(channel.getLocalAddress()) + "'");
    }
  }

  public void configure(Selector selector) {
    try {
      channel = SocketChannel.open();
      channel.configureBlocking(false);
      channel.connect(address);
      key = channel.register(selector, SelectionKey.OP_CONNECT, this);
    } catch (IOException e) {
      log.log(Level.SEVERE, "Error configuring Connector for address '" + new Address(address) + "'", e);
    }
  }
}
