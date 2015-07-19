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

  @Override
  public String format(final LogRecord record) {
    return Glog.formatRecord(this, record);
  }

  @Override
  public String getMessage(LogRecord record) {
    return formatMessage(record);
  }

  @Override
  public String getClassName(LogRecord record) {
    return record.getSourceClassName();
  }

  @Override
  public String getMethodName(LogRecord record) {
    return record.getSourceMethodName();
  }

  @Override
  public Glog.Level getLevel(LogRecord record) {
    return levelLabel.get(record.getLevel());
  }

  @Override
  public long getTimeStamp(LogRecord record) {
    return record.getMillis();
  }

  @Override
  public long getThreadId(LogRecord record) {
    return record.getThreadID();
  }

  @Override
  public Throwable getThrowable(LogRecord record) {
    return record.getThrown();
  }
}

