///*
// *  Copyright (C) 2015 Jeff Rose
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// * http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// *
// */
//
//package com.xjeffrose.log.log4j;
//
//import com.xjeffrose.log.Glog;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.logging.Level;
//import org.apache.log4j.Layout;
//import org.apache.log4j.Level;
//import org.apache.log4j.spi.LocationInfo;
//import org.apache.log4j.spi.LoggingEvent;
//import org.apache.log4j.spi.ThrowableInformation;
//
//public class GlogLayout extends Layout implements Formatter<LoggingEvent> {
//
//  private Map<Level, Glog.Level> levelLabel = new HashMap<Level, Glog.Level>();
//
//  LogFormatter() {
//    levelLabel.put(Level.FINEST, Glog.Level.DEBUG);
//    levelLabel.put(Level.FINER, Glog.Level.DEBUG);
//    levelLabel.put(Level.FINE, Glog.Level.DEBUG);
//    levelLabel.put(Level.CONFIG, Glog.Level.INFO);
//    levelLabel.put(Level.INFO, Glog.Level.INFO);
//    levelLabel.put(Level.WARNING, Glog.Level.WARNING);
//    levelLabel.put(Level.SEVERE, Glog.Level.ERROR);
//  }
//
//  @Override
//  public String format(LoggingEvent record) {
//    return Glog.formatRecord(this, record);
//  }
//
//  @Override
//  public boolean ignoresThrowable() {
//    return false; // We handle stack trace formatting.
//  }
//
//  @Override
//  public void activateOptions() {
//    // We use no options
//  }
//
//  @Override
//  public String getMessage(LoggingEvent record) {
//    return record.getRenderedMessage();
//  }
//
//  @Override
//  public String getClassName(LoggingEvent record) {
//    LocationInfo locationInformation = record.getLocationInformation();
//    return (locationInformation != null)
//        ? locationInformation.getClassName()
//        : null;
//  }
//
//  @Override
//  public String getMethodName(LoggingEvent record) {
//    LocationInfo locationInformation = record.getLocationInformation();
//    return (locationInformation != null)
//        ? record.getLocationInformation().getMethodName()
//        : null;
//  }
//
//  @Override
//  public Glog.Level getLevel(LoggingEvent record) {
//    return LEVEL_LABELS.get(record.getLevel());
//  }
//
//  @Override
//  public long getTimeStamp(LoggingEvent record) {
//    return record.getTimeStamp();
//  }
//
//  @Override
//  public long getThreadId(LoggingEvent record) {
//    return Thread.currentThread().getId();
//  }
//
//  @Override
//  public Throwable getThrowable(LoggingEvent record) {
//    ThrowableInformation throwableInformation = record.getThrowableInformation();
//    return throwableInformation != null ? throwableInformation.getThrowable() : null;
//  }
//}