package com.xjeffrose.xio2.http;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class HttpRequest extends HttpObject {

  public HttpRequest() { }

  public static class Builder {

    private Http.Method method = Http.Method.GET;
    private Http.Version version = Http.Version.HTTP1_1;
    private String url = "/";
    private String body = "";
    private boolean tls = false;

    public Builder() { }

    public Builder method(Http.Method method) {
      this.method = method;
      return this;
    }

    public Builder version(Http.Version version) {
      this.version = version;
      return this;
    }

    public Builder url(String url) {
      this.url = url;
      return this;
    }

    public Builder body(String body) {
      this.body = body;
      return this;
    }

    public Builder tls(boolean b) {
      this.tls = b;
      return this;
    }

    public HttpRequest build() {
      if (url == null) throw new IllegalStateException("url == null");
      if (body == null) {
        try {
          return HttpRequest.DefaultRequest(version, method, url, url);
        } catch (UnknownHostException e) {
          e.printStackTrace();
        }
      } else {
        try {
          return HttpRequest.DefaultRequest(version, method, url, body);
        } catch (UnknownHostException e) {
          e.printStackTrace();
        }
      }
      return null;
    }
  }

  public static HttpRequest DefaultRequest(Http.Version version, Http.Method method, String uri)
      throws UnknownHostException {
    final HttpRequest req = new HttpRequest();

    req.setVersion(version);
    req.setMethod(method);
    req.setUri(uri);

    req.headers.set("User-Agent", "xio2");
    req.headers.set("Host", InetAddress.getLocalHost().getHostName());
    req.headers.set("Accept", "*/*");

    return req;
  }

  public static HttpRequest DefaultRequest(
      Http.Version version,
      Http.Method method,
      String uri,
      String body) throws UnknownHostException {
    final HttpResponse resp = new HttpResponse();
    final HttpRequest req = new HttpRequest();

    req.setVersion(version);
    req.setMethod(method);
    req.setUri(uri);

    req.headers.set("User-Agent", "xio2");
    req.headers.set("Host", InetAddress.getLocalHost().getHostName());
    req.headers.set("Accept", "*/*");
    req.headers.set("Content-Length", Integer.toString(body.length()));
    req.body.set(body);

    return req;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append(getMethod() + " ");
    sb.append(getUri() + " ");
    sb.append(getHttpVersion() + "\r\n");

    sb.append("User-Agent" + ": " + headers.get("User-Agent") + "\r\n");
    sb.append("Host" + ": " + headers.get("Host") + "\r\n");
    sb.append("Accept" + ": " + headers.get("Accept") + "\r\n");
    if (headers.headerMap.containsKey("Content-Length")) {
      sb.append("Content-Length" + ": " + headers.get("Content-Length") + "\r\n");
      sb.append("\r\n");
      sb.append(getBody());
    } else {
      sb.append("Content-Length: 0\r\n");
      sb.append("\r\n");
    }
    return sb.toString();
  }
}
