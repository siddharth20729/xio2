package com.xjeffrose.xio2.client;

import java.net.InetSocketAddress;

public interface LoadBalancingStrategy {
  InetSocketAddress nextAddress();
}
