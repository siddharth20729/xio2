package com.xjeffrose.xio2.http.server;

import com.xjeffrose.log.Log;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public class Server {
  private static final Logger log = Log.getLogger(Server.class.getName());

  private final Map<Route, HttpHandler> routes = new ConcurrentHashMap<Route, HttpHandler>();
  private ServerSocketChannel channel;
  private Acceptor acceptor;
  private EventLoopPool pool;

  private AtomicBoolean ssl = new AtomicBoolean(false);

  public Server() { }

  public void ssl(boolean b) {
    ssl.set(b);
  }

  private void schedule(ServerSocketChannel channel) {
    final int cores = Runtime.getRuntime().availableProcessors();
    pool = new EventLoopPool(cores, ssl);
    acceptor = new Acceptor(channel, pool, routes);
    acceptor.start();
    pool.start();
  }

  private void bind(InetSocketAddress addr) throws IOException {
    channel = ServerSocketChannel.open();
    channel.configureBlocking(false);
    channel.bind(addr);
    schedule(channel);
  }

  public void serve(int port) {
    try {
      bind(new InetSocketAddress(port));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void addRoute(String route, HttpHandler httpHandler) {
    routes.putIfAbsent(Route.build(route), httpHandler);
  }

  public void close() {
    try {
      acceptor.close();
      pool.close();
      channel.socket().close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
