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

