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

package com.xjeffrose.xio2;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class FirewallTest {
  Firewall fw;

  @Before
  public void setUp() throws Exception {
    fw  = new Firewall();
    fw.whiteList(new InetSocketAddress("192.168.1.1", 80));
    fw.greyList((new InetSocketAddress("192.168.1.1", 80)));
    fw.blackList((new InetSocketAddress("192.168.1.1", 80)));
  }

  @After
  public void tearDown() throws Exception {

  }

  @Test
  public void testgetRemoteAddress() throws Exception {
    long what = Firewall.getRemoteAddress((SocketAddress) new InetSocketAddress("192.168.1.1", 80));
    System.out.println(what);
    long what2 = Firewall.getRemoteAddress((SocketAddress) new InetSocketAddress("192.168.1.2", 80));
    System.out.println(what2);

  }

  @Test
  public void testIsAddrWhiteListed() throws Exception {
    assertTrue(fw.isAddrWhiteListed((new InetSocketAddress("192.168.1.1", 80))));
    assertFalse(fw.isAddrWhiteListed((new InetSocketAddress("192.168.1.2", 80))));
  }

  @Test
  public void testIsAddrGreyListed() throws Exception {
      assertTrue(fw.isAddrGreyListed((new InetSocketAddress("192.168.1.1", 80))));
      assertFalse(fw.isAddrGreyListed((new InetSocketAddress("192.168.1.2", 80))));
  }

  @Test
  public void testIsAddrBlackListed() throws Exception {
      assertTrue(fw.isAddrBlackListed((new InetSocketAddress("192.168.1.1", 80))));
      assertFalse(fw.isAddrBlackListed((new InetSocketAddress("192.168.1.2", 80))));
  }

  @Test
  public void testBlackListAliens() throws Exception {
  }

}