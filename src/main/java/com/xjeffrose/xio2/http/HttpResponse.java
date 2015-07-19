package com.xjeffrose.xio2.http;

public class HttpResponse extends HttpObject {

  public HttpResponse() { }

  public static HttpResponse DefaultResponse(Http.Version version, Http.Status status) {
    final HttpResponse resp = new HttpResponse();

    resp.setVersion(version);
    resp.setStatus(status);
    resp.headers.set("Content-Type", "text/html; charset=UTF-8");
    resp.headers.set("Date", resp.date());
    resp.headers.set("Server", "xio2");

    return resp;
  }

  public static HttpResponse DefaultResponse(
      Http.Version version,
      Http.Status status,
      String body) {
    final HttpResponse resp = new HttpResponse();

    resp.setVersion(version);
    resp.setStatus(status);
    resp.headers.set("Content-Type", "text/html; charset=UTF-8");
    resp.headers.set("Date", resp.date());
    resp.headers.set("Server", "xio2");
    resp.headers.set("Content-Length", Integer.toString(body.length()));

    return resp;
  }
}



//< HTTP/1.1 301 Moved Permanently
//< Location: http://www.google.com/
//< Content-Type: text/html; charset=UTF-8
//< Date: Sun, 19 Jul 2015 19:45:14 GMT
//< Expires: Tue, 18 Aug 2015 19:45:14 GMT
//< Cache-Control: public, max-age=2592000
//    * Server gws is not blacklisted
//< Server: gws
//< Content-Length: 219
//< X-XSS-Protection: 1; mode=block
//< X-Frame-Options: SAMEORIGIN
//< Alternate-Protocol: 80:quic,p=0
