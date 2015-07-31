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
 *
 */

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
      EventLoop loop = new EventLoop(ssl);
      loop.setName("EventLoop " + i);
      pool.addLast(loop);
//      pool.addLast(new EventLoop(ssl));
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
