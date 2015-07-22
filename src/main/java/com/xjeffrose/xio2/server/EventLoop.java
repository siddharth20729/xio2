package com.xjeffrose.xio2.server;

import com.xjeffrose.log.Log;
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
      } catch (IOException e) {
        throw new RuntimeException(e);
      }

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
        } catch (Exception e) {
          log.severe("Terminating connection to - " + key.channel());
          try {
            key.channel().close();
          } catch (IOException e1) {
            e1.printStackTrace();
          }
          key.cancel();
          //throw new RuntimeException(e);
        }
        if (!running()) {
          break;
        }
      }
      configureChannel();
    }
  }

  private void configureChannel() {
    while (contextsToAdd.size() > 0) {
      try {
        ChannelContext context = contextsToAdd.pop();
        context.channel.configureBlocking(false);
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
