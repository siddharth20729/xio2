package com.xjeffrose.xio2.http.client;

import java.net.InetSocketAddress;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class RoundRobinLoadBalancer implements LoadBalancingStrategy {

  private final Deque<InetSocketAddress> hosts;

  public RoundRobinLoadBalancer(List<InetSocketAddress> hosts) {
    this.hosts = new ArrayDeque<>(hosts);
  }

  @Override
  public InetSocketAddress nextAddress() {
    InetSocketAddress next = hosts.removeFirst();
    hosts.addLast(next);
    return next;
  }

}
