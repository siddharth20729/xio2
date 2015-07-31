/*
 * Copyright (C) 2015 Jeff Rose
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
 */
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
