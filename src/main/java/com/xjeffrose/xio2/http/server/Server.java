package com.xjeffrose.xio2.http.server;

import com.xjeffrose.log.Log;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public class Server {
  private static final Logger log = Log.getLogger(Server.class.getName());

//
  private List<Acceptor> acceptorList = new ArrayList<>();
  private final int cores = Runtime.getRuntime().availableProcessors();
  private ServerSocketChannel channel;
  private Acceptor acceptor;
  private EventLoopPool pool;

  private AtomicBoolean ssl = new AtomicBoolean(false);

  public Server() {
    pool = new EventLoopPool(cores, ssl);
  }

  public void ssl(boolean b) {
    ssl.set(b);
  }

  public void bind(int port) throws IOException {
    bind("0.0.0.0", port, new HttpHandler());
  }

  public void bind(int port, Handler handler) {
    bind("0.0.0.0", port, handler);
  }

  public void bind(String ipAddr, int port, Handler handler) {
    final InetSocketAddress addr = new InetSocketAddress(ipAddr, port);
    bind(addr, handler);
  }

  public void bind(InetSocketAddress addr, Handler handler) {
    try {
      channel = ServerSocketChannel.open();
      channel.configureBlocking(false);
      channel.bind(addr);

      acceptor = new Acceptor(channel, pool, handler);
      acceptor.start();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void serve(int port, Handler handler) {
    bind(port, handler);
    serve();
  }

  public void serve() {
    pool.start();
    acceptorList.forEach(a -> a.start());
  }

  public void close() {
    acceptorList.forEach(a -> a.close());
    pool.close();
  }

}
