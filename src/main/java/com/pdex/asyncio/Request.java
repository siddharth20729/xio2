package com.pdex.asyncio;

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

  public URL getURL() {
    return url;
  }
}
