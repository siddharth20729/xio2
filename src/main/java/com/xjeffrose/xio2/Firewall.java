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
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("ALL")
public class Firewall {
  private final List<InetSocketAddress> whiteList = new ArrayList<>();
  private final List<InetSocketAddress> greyList = new ArrayList<>();
  private final List<InetSocketAddress> blackList = new ArrayList<>();

  public Firewall() { }

  public static long getRemoteAddress(SocketAddress remoteAddress) {
    InetSocketAddress remoteInetSocketAddress = (InetSocketAddress) remoteAddress;
    byte[] octets = remoteInetSocketAddress.getAddress().getAddress();
    long result = 0;
    for (byte octet : octets) {
      result <<= 8;
      result |= octet & 0xff;
    }
    return result;
  }

  public boolean isAddrWhiteListed(SocketAddress remoteAddress) {
    return whiteList.contains(remoteAddress);
  }

  public boolean isAddrGreyListed(SocketAddress remoteAddress) {
    return greyList.contains(remoteAddress);
  }

  public boolean isAddrBlackListed(SocketAddress remoteAddress) {
    return blackList.contains(remoteAddress);
  }

  public void whiteList(InetSocketAddress inetSocketAddress) {
    whiteList.add(inetSocketAddress);
  }

  public void greyList(InetSocketAddress inetSocketAddress) {
    greyList.add(inetSocketAddress);
  }

  public void blackList(InetSocketAddress inetSocketAddress) {
    blackList.add(inetSocketAddress);
  }

  public void blackListAliens() {

  }
}
