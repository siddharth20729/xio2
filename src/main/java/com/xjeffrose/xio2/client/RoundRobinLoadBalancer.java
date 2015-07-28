package com.xjeffrose.xio2.client;

import java.net.InetSocketAddress;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class RoundRobinLoadBalancer implements LoadBalancingStrategy {

  private final Deque<InetSocketAddress> hosts;

  public static RoundRobinLoadBalancerBuilder Builder() {
    return new RoundRobinLoadBalancerBuilder();
  }

  public static class RoundRobinLoadBalancerBuilder {
    List<InetSocketAddress> hosts = new ArrayList<>();
    public RoundRobinLoadBalancerBuilder addServer(String hostname, int port) {
      hosts.add(new InetSocketAddress(hostname, port));

      return this;
    }

    public RoundRobinLoadBalancer build() {
      return new RoundRobinLoadBalancer(hosts);
    }
  }

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
