package com.pdex.asyncio;

import java.io.Closeable;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SelectorLoop implements Closeable, Runnable {
  private static final Logger log = Logger.getLogger(SelectorLoop.class.getName());

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
      if (key.isValid() && key.isConnectable()) {
        selectable.connect();
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
