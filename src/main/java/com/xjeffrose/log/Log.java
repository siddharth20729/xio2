package com.xjeffrose.log;

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Log {
  private static Logger logger;
  private static Handler handler;

  private Log() { }

  public static Logger getLogger(String className) {

    return getLogger(className, Level.ALL);
  }

  public static Logger getLogger(String className, Level level) {
    logger = Logger.getLogger(className);
    handler = new ConsoleHandler();
    handler.setFormatter(new LogFormatter());
    handler.setLevel(level);
    handler.setFilter(null);
    logger.setUseParentHandlers(false);
    logger.addHandler(handler);

    return logger;
  }

  public static Logger getLogger(String className, String path) {

    return getLogger(className, path, Level.ALL);
  }

  public static Logger getLogger(String className, String path, Level level) {
    logger = Logger.getLogger(className);
    try {
      handler = new FileHandler(path);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    handler.setFormatter(new LogFormatter());
    handler.setLevel(level);
    handler.setFilter(null);
    logger.setUseParentHandlers(false);
    logger.addHandler(handler);

    return logger;
  }
}




