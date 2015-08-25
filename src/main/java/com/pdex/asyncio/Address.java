package com.pdex.asyncio;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class Address {
  private final InetSocketAddress address;

  public Address(InetSocketAddress address) {
    this.address = address;
  }

  public Address(SocketAddress address) {
    this.address = (InetSocketAddress) address;
  }

  public String toString() {
    return address.getAddress().getHostAddress() + ":" + address.getPort();
  }
}
