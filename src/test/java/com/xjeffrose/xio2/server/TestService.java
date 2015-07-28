package com.xjeffrose.xio2.server;

import com.xjeffrose.log.Log;
import com.xjeffrose.xio2.http.Http;
import com.xjeffrose.xio2.http.HttpObject;
import com.xjeffrose.xio2.http.HttpResponse;
import java.util.logging.Logger;

class TestService extends HttpHandler {
  private static final Logger log = Log.getLogger(TestService.class.getName());

  public void handleNotFound() {
    ctx.write(HttpResponse.DefaultResponse(Http.Version.HTTP1_1, Http.Status.NOT_FOUND));
  }

  public void handleGet() {
    ctx.write(HttpResponse.DefaultResponse(Http.Version.HTTP1_1, Http.Status.OK));
  }

}