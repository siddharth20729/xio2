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




