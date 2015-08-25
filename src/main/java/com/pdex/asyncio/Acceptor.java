package com.pdex.asyncio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Acceptor extends Selectable {
  private static final Logger log = Logger.getLogger(Acceptor.class.getName());

  private ServerSocketChannel channel;
  private final InetSocketAddress address;
  private final ConnectionFactory factory;
  private final SelectorLoopStrategy strategy;

  public Acceptor(InetSocketAddress address, ConnectionFactory factory, SelectorLoopStrategy strategy) {
    this.address = address;
    this.factory = factory;
    this.strategy = strategy;
  }

  public void accept() throws IOException {
    SocketChannel client = channel.accept();
    Connection connection = factory.build(client);
    strategy.getLoop().add(connection);
    log.info("Accepted incoming connection to address '" + new Address(address) + "' from address '" + new Address(client.getRemoteAddress()) + "'");
  }

  public void configure(Selector selector) {
    try {
      channel = ServerSocketChannel.open();
      channel.configureBlocking(false);
      channel.bind(address);
      channel.register(selector, SelectionKey.OP_ACCEPT, this);
    } catch (IOException e) {
      log.log(Level.SEVERE, "Error configuring Acceptor for address '" + new Address(address) + "'", e);
    }
  }
}
