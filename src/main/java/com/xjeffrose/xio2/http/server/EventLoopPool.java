package com.xjeffrose.xio2.http.server;

import com.xjeffrose.log.Log;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

class EventLoopPool {
  private static final Logger log = Log.getLogger(EventLoopPool.class.getName());

  private final ConcurrentLinkedDeque<EventLoop> pool =
      new ConcurrentLinkedDeque<EventLoop>();
  private EventLoop loop;

  EventLoopPool(int poolSize, AtomicBoolean ssl) {
    for (int i = 0; i < poolSize; i++) {
      pool.addLast(new EventLoop(ssl));
    }
  }

  public void start() {
    for (EventLoop loop : pool) {
      loop.start();
    }
  }

  public EventLoop next() {
    loop = pool.removeFirst();
    pool.addLast(loop);
    return loop;
  }

  public void close() {
    for (EventLoop loop : pool) {
      loop.close();
    }
  }
}
