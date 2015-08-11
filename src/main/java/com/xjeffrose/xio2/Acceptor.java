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
import com.xjeffrose.xio2.http.server.HttpHandler;
import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public class Acceptor extends Thread {
  private static final Logger log = Log.getLogger(Acceptor.class.getName());

  private final AtomicBoolean isRunning = new AtomicBoolean(true);
  private final Selector selector;
  private final EventLoopPool eventLoopPool;
  private Handler handler;

  public Acceptor(ServerSocketChannel serverChannel,
           EventLoopPool eventLoopPool,
           Handler handler) {
    this.eventLoopPool = eventLoopPool;
    this.handler = handler;

    try {
      selector = Selector.open();
      serverChannel.register(selector, SelectionKey.OP_ACCEPT);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
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
            EventLoop next = eventLoopPool.next();
            next.addContext(new ChannelContext(channel, handler));
          } else if (!key.isValid()) {
            log.info("Key " + key + " is no longer valid.");
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
