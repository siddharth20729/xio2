package com.xjeffrose.xio2.server;

import com.xjeffrose.log.Log;
import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

class Acceptor extends Thread {
  private static final Logger log = Log.getLogger(Acceptor.class.getName());

  private final AtomicBoolean isRunning = new AtomicBoolean(true);
  private final AtomicBoolean isReady = new AtomicBoolean(true);
  private final Selector selector;
  private final EventLoopPool eventLoopPool;
  private Map<Route, Service> routes;

  Acceptor(ServerSocketChannel serverChannel,
           EventLoopPool eventLoopPool,
           Map<Route, Service> routes) {
    this.eventLoopPool = eventLoopPool;
    this.routes = routes;

    try {
      selector = Selector.open();
      serverChannel.register(selector, SelectionKey.OP_ACCEPT);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public boolean ready() {
    return isReady.get();
  }

  public boolean running() {
    return isRunning.get();
  }

  public void close() {
    isRunning.set(false);
    selector.wakeup();
  }

  public void run() {
    while (running()) {
      try {
        selector.select();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }

      Set<SelectionKey> acceptKeys = selector.selectedKeys();
      Iterator<SelectionKey> iterator = acceptKeys.iterator();

      while (iterator.hasNext()) {
        SelectionKey key = iterator.next();
        iterator.remove();

        try {
          if (key.isValid() && key.isAcceptable()) {
            ServerSocketChannel server = (ServerSocketChannel) key.channel();
            SocketChannel channel = server.accept();
            //log.info("Accepting Connection from: " + channel);
            EventLoop next = eventLoopPool.next();
            next.addContext(new ChannelContext(channel, routes));
          } else if (!key.isValid()) {
            key.cancel();
          }
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
        if (!running()) {
          break;
        }
      }
    }
  }
}
