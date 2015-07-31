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

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

class LogFormatter extends Formatter implements Glog.Formatter<LogRecord> {
  private Map<Level, Glog.Level> levelLabel = new HashMap<Level, Glog.Level>();

  LogFormatter() {
    levelLabel.put(Level.FINEST, Glog.Level.DEBUG);
    levelLabel.put(Level.FINER, Glog.Level.DEBUG);
    levelLabel.put(Level.FINE, Glog.Level.DEBUG);
    levelLabel.put(Level.CONFIG, Glog.Level.INFO);
    levelLabel.put(Level.INFO, Glog.Level.INFO);
    levelLabel.put(Level.WARNING, Glog.Level.WARNING);
    levelLabel.put(Level.SEVERE, Glog.Level.ERROR);
  }

  public String format(final LogRecord record) {
    return Glog.formatRecord(this, record);
  }

  public String getMessage(LogRecord record) {
    return formatMessage(record);
  }

  public String getClassName(LogRecord record) {
    return record.getSourceClassName();
  }

  public String getMethodName(LogRecord record) {
    return record.getSourceMethodName();
  }

  public Glog.Level getLevel(LogRecord record) {
    return levelLabel.get(record.getLevel());
  }

  public long getTimeStamp(LogRecord record) {
    return record.getMillis();
  }

  public long getThreadId(LogRecord record) {
    return record.getThreadID();
  }

  public Throwable getThrowable(LogRecord record) {
    return record.getThrown();
  }
}

