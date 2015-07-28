package com.xjeffrose.xio2.http.client;

import java.net.InetSocketAddress;

public interface LoadBalancingStrategy {
  InetSocketAddress nextAddress();
}
