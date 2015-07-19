package com.xjeffrose.xio2.server;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ServerTest {
  Server s;

  @Before
  public void setUp() throws Exception {
    s = new Server();
  }

  @After
  public void tearDown() throws Exception {
    s.close();
  }

  @Test
  public void testServe() throws Exception {
    s.serve(9000);
  }

  @Test
  public void testAddRoute() throws Exception {
    s.serve(9001);
    s.addRoute("/test", new Service());
  }

}