package com.xjeffrose.xio2;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SSLSpike {
  private static final Logger log = Logger.getLogger(SSLSpike.class.getName());
  public interface SelectorLoopStrategy {
    SelectorLoop getLoop();
  }
  public interface ConnectionFactory {
    Connection build(SocketChannel client);
  }
  public abstract class Selectable {
    public void accept() throws IOException {}
    public void configure(Selector selector) {}
    public void fill() {}
    public void flush() {}
  }
  public class Address {
    private final InetSocketAddress address;
    public Address(InetSocketAddress address) {
      this.address = address;
    }
    public Address(SocketAddress address) {
      this.address = (InetSocketAddress)address;
    }
    public String toString() {
      return address.getAddress().getHostAddress() + ":" + address.getPort();
    }
  }
  public class Acceptor extends Selectable {
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
  public class Connection extends Selectable {
    private final SocketChannel channel;
    private InetSocketAddress address;
    public Connection(SocketChannel channel) {
      this.channel = channel;
    }
    public void configure(Selector selector) {
      try {
        this.address = (InetSocketAddress)channel.getLocalAddress();
        channel.configureBlocking(false);
        //onConnect();
        channel.register(selector, SelectionKey.OP_WRITE | SelectionKey.OP_READ, this);
      } catch (IOException e) {
        log.log(Level.SEVERE, "Error configuring Connection for address '" + new Address(address) + "'", e);
      }
    }
  }

  public class SelectorLoop implements Closeable, Runnable {
    private final ConcurrentLinkedDeque<Selectable> selectablesToAdd = new ConcurrentLinkedDeque<>();
    private final AtomicBoolean isRunning = new AtomicBoolean(true);
    private Selector selector;
    private boolean running() {
      return isRunning.get();
    }
    private void setupNewSelectables() {
      while (selectablesToAdd.size() > 0) {
        Selectable selectable = selectablesToAdd.pop();
        selectable.configure(selector);
      }
    }
    private void handleSelection(SelectionKey key) {
      try {
        Selectable selectable = (Selectable) key.attachment();
        if (key.isValid() && key.isReadable()) {
          selectable.fill();
        }
        if (key.isValid() && key.isWritable()) {
          selectable.flush();
        }
        if (key.isValid() && key.isAcceptable()) {
          selectable.accept();
        }
        if (!key.isValid()) {
          key.channel().close();
          key.cancel();
        }
        //This is a catch all for any error in this thread.
      } catch (Exception e) {
        log.log(Level.SEVERE, "Error inside Read | Write loop", e);
        key.cancel();
      }
    }
    private void poke() {
      if (selector != null) {
        selector.wakeup();
      }
    }

    public void add(Selectable selectable) {
      selectablesToAdd.push(selectable);
      poke();
    }

    @Override
    public void close() {
      isRunning.set(false);
      poke();
    }

    @Override
    public void run() {
      try {
        selector = Selector.open();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }

      while (running()) {
        try {
          setupNewSelectables();
          selector.select();

          Set<SelectionKey> acceptKeys = selector.selectedKeys();
          Iterator<SelectionKey> iterator = acceptKeys.iterator();

          while (iterator.hasNext()) {
            SelectionKey key = iterator.next();
            iterator.remove();

            handleSelection(key);

            if (!running()) {
              break;
            }
          }
        } catch (Exception e) {
          log.log(Level.SEVERE, "Error in SelectorLoop", e);
        }
      }
    }
  }

  SelectorLoop selectorLoop = new SelectorLoop();
  InetSocketAddress address = new InetSocketAddress("127.0.0.1", 8080);
  ConnectionFactory factory = Connection::new;
  SelectorLoopStrategy strategy = () -> selectorLoop;
  Acceptor acceptor = new Acceptor(address, factory, strategy);
  SSLSpike() {
  }
  public void run() {
    selectorLoop.add(acceptor);
    selectorLoop.run();
  }
  static public void main(String args[]) {
    new SSLSpike().run();
  }
}
