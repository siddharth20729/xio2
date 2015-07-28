package com.xjeffrose.xio2.util;

import org.junit.Test;

import static org.junit.Assert.*;

public class ConfigTest {

  @Test
  public void testGet() throws Exception {
    Config conf = new Config("./test.ini");

    assertEquals(conf.get("section1", "foo"), "bar");
    assertEquals(conf.get("section2", "pop"), "top");
  }
}