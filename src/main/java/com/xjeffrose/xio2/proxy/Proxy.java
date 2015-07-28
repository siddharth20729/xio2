package com.xjeffrose.xio2.proxy;

import com.xjeffrose.xio2.client.XioClient;
import com.xjeffrose.xio2.server.Server;
import java.util.HashMap;
import java.util.Map;

public class Proxy {

  private Map<Integer, String> routeMap = new HashMap<>();
  private Server s;
  private XioClient c;

  public Proxy() {
  }

  public void addRoute(Integer port, String url) {
    routeMap.put(port, url);
  }

  public void ssl(boolean b) {
    s.ssl(b);
    c.ssl(b);
  }

  public void start() {
    for (Integer port : routeMap) {
      s.serve(port);
    }
  }
}
