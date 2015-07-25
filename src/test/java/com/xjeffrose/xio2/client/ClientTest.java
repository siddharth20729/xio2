package com.xjeffrose.xio2.client;

import com.xjeffrose.xio2.server.Server;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ClientTest {
  Server s = new Server();
  Client c = new Client();


  @Before
  public void setUp() throws Exception {
    s.serve(9007);

  }

  @After
  public void tearDown() throws Exception {
    s.close();
  }

  @Test
  public void testGet() throws Exception {

  }

  @Test
  public void testPost() throws Exception {

  }

  @Test
  public void testPut() throws Exception {

  }

  @Test
  public void testDelete() throws Exception {

  }

  @Test
  public void testDelete1() throws Exception {

  }
}