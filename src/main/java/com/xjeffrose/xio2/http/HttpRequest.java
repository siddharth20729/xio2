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
package com.xjeffrose.xio2.http;

import com.xjeffrose.xio2.Request;
import com.xjeffrose.xio2.util.BB;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class HttpRequest extends HttpObject implements Request {

  public HttpRequest() { }

  public static HttpRequest copy(HttpRequest other, boolean tls) {
    return HttpRequest.newBuilder()
        .method(other.method_)
        .url(other.getUri().toString())
        .version(other.version)
        .body(other.getBody())
        .tls(tls)
        .build();
  }

  public static Builder newBuilder() {
    return new Builder();
  }
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

    headers.headerMap.keySet()
        .stream()
        .filter(h -> !headers.get(h).equals(""))
        .forEach(h -> sb.append(h).append(": ").append(headers.get(h)).append("\r\n"));
    sb.append("\r\n");

    if (!headers.get("Content-Length").equals("")) {
      if (Integer.parseInt(headers.get("Content-Length")) > 0) {
        sb.append(getBody());
      }
    }

    return sb.toString();
  }

  public ByteBuffer toBB() {
    return BB.StringtoBB(toString());
  }

}
