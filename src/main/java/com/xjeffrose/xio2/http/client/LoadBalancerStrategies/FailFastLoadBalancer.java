package com.xjeffrose.xio2.http.client.LoadBalancerStrategies;

import java.net.InetSocketAddress;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class FailFastLoadBalancer implements LoadBalancingStrategy {

  private final Deque<InetSocketAddress> hosts;

  public FailFastLoadBalancer(List<InetSocketAddress> hosts) {
    this.hosts = new ArrayDeque<>(hosts);
  }

  @Override
  public InetSocketAddress nextAddress() {
    InetSocketAddress next = hosts.removeFirst();
    hosts.addLast(next);
    return next;
  }

}