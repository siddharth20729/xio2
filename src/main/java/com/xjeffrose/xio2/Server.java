/*
 * Copyright (C) 2015 Jeff Rose
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.xjeffrose.xio2;

import com.xjeffrose.log.Log;
import com.xjeffrose.xio2.admin.AdminHandler;
import com.xjeffrose.xio2.http.server.HttpHandler;
import com.xjeffrose.xio2.http.server.HttpsHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class Server {
  private static final Logger log = Log.getLogger(Server.class.getName());

  private List<Acceptor> acceptorList = new ArrayList<>();
  private final int cores = Runtime.getRuntime().availableProcessors();
  private EventLoopPool pool;

  public AdminHandler adminHandler = new AdminHandler();
  public int adminHandlerPort = 8081;

  public Server() {
    pool = new EventLoopPool(cores);
  }

  public void bind(int port, boolean tls) {
    if (tls) {
      bind("0.0.0.0", port, new HttpsHandler());
    } else {
      bind("0.0.0.0", port, new HttpHandler());
    }
  }

  public void bind(int port) throws IOException {
    bind(port, false);
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
      ServerSocketChannel channel = ServerSocketChannel.open();
      channel.configureBlocking(false);
      channel.bind(addr);

      acceptorList.add(new Acceptor(channel, pool, handler));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void serve(int port, Handler handler) {
    bind(port, handler);
    serve();
  }

  private void registerAdminServer() {
    bind(adminHandlerPort, adminHandler);
  }

  public void serve() {
//    registerAdminServer();
    pool.start();
    acceptorList.forEach(a -> a.start());
  }

  public void close() {
    acceptorList.forEach(a -> a.close());
    pool.close();
  }

}
