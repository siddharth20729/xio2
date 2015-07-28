package com.xjeffrose.log;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class  Log {
  private static Logger logger;
  private static Handler handler;

  private Log() { }

  public static Logger getLogger(String className) {
    logger = Logger.getLogger(className);
    handler = new ConsoleHandler();
    handler.setFormatter(new LogFormatter());
    handler.setLevel(Level.ALL);
    handler.setFilter(null);
    logger.setUseParentHandlers(false);
    logger.addHandler(handler);

    return logger;
  }
}




