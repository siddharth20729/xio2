package com.xjeffrose.xio2.http.client.LoadBalancerStrategies;

import java.net.InetSocketAddress;

public interface LoadBalancingStrategy {
  InetSocketAddress nextAddress();
}
