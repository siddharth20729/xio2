/*
 *  Copyright (C) 2015 Jeff Rose
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

package com.xjeffrose.log;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.junit.Test;

import static org.junit.Assert.*;

public class GlogTest {

  @Test
  public void testFormatRecord() throws Exception {
    RuntimeException e = new RuntimeException("A runtime error occurred");
    StackTraceElement[] st = e.getStackTrace();
    e.setStackTrace(new StackTraceElement[] { st[0] });

    LogFormatter f = new LogFormatter();
    LogRecord r = new LogRecord(Level.SEVERE, "A severe ERROR occurred");
    r.setThrown(e);
    r.setMillis(1439510036401L);

    String actual = Glog.formatRecord(f, r);

    String expected = "E 0813 23:53:56.4011439510036401 THREAD1: A severe ERROR occurred\n" +
                      "  java.lang.RuntimeException: A runtime error occurred\n" +
                      "    at com.xjeffrose.log.GlogTest.testFormatRecord(GlogTest.java:30)\n"
    ;

    assertEquals("formatted result", expected, actual);
  }
}