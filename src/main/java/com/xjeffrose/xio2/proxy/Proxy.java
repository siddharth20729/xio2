package com.xjeffrose.xio2.proxy;

import com.xjeffrose.xio2.server.Server;
import java.util.HashMap;
import java.util.Map;

public class Proxy {

  private boolean ssl = false;
  private Map<Integer, String> routeMap = new HashMap<>();
  private Map<Integer, Server> ServerMap = new HashMap<>();

  public Proxy() {
  }

  public void addRoute(Integer port, String url) {
    routeMap.put(port, url);
  }

  public void ssl(boolean b) {
    this.ssl = b;
  }

  public void start() {
    for (Integer port : routeMap.keySet()) {

    }
  }
}
