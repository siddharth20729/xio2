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
import com.xjeffrose.xio2.TLS.TLS;
import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

class EventLoop extends Thread {
  private final Logger log = Log.getLogger(EventLoop.class.getName());

  private final ConcurrentLinkedDeque<ChannelContext> contextsToAdd = new ConcurrentLinkedDeque<>();
  private final AtomicBoolean isRunning = new AtomicBoolean(true);
  private final Selector selector;

  EventLoop() {
    try {
      selector = Selector.open();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void addContext(ChannelContext context) {
    contextsToAdd.push(context);
    selector.wakeup();
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

        Set<SelectionKey> acceptKeys = selector.selectedKeys();
        Iterator<SelectionKey> iterator = acceptKeys.iterator();

        while (iterator.hasNext()) {
          SelectionKey key = iterator.next();
          iterator.remove();

          try {
            if (key.isValid() && key.isReadable()) {
              ChannelContext ctx = (ChannelContext) key.attachment();
              ctx.read();
            }
            if (key.isValid() && key.isWritable()) {
              ChannelContext ctx = (ChannelContext) key.attachment();
              ctx.flush();
            }
            if (!key.isValid()) {
              key.channel().close();
              key.cancel();
            }
            //This is a catch all for any error in this thread.
          } catch (Exception e) {
            e.printStackTrace();
            ChannelContext ctx = (ChannelContext) key.attachment();
            ctx.handleFatalError();
            key.cancel();
          }
          if (!running()) {
            break;
          }
        }
        configureChannel();
      } catch (IOException e) {
      }
    }
  }

  private void configureChannel() {
    while (contextsToAdd.size() > 0) {
      try {
        ChannelContext context = contextsToAdd.pop();
        context.channel.configureBlocking(false);
        context.onConnect();

        context.channel.register(selector, SelectionKey.OP_WRITE | SelectionKey.OP_READ, context);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      if (!running()) {
        break;
      }
    }
  }
}
