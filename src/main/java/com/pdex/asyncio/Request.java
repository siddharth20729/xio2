package com.pdex.asyncio;

import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;

class Request {
  final private URL url;

  Request(URL url) {
    this.url = url;
  }

  Request(String url) {
    try {
      this.url = new URL(url);
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  public InetSocketAddress getAddress() {
    String host = url.getHost();
    int port = url.getPort();
    if (port == -1) {
      port = url.getDefaultPort();
    }
    return new InetSocketAddress(host, port);
  }

  public String getHost() {
    return url.getHost();
  }

  public String getPath() {
    return url.getPath();
  }

  public String getProtocol() {
    return url.getProtocol();
  }

  public URL getURL() {
    return url;
  }
}
