package com.xjeffrose.xio2.http.server;

import com.xjeffrose.log.Log;
import com.xjeffrose.xio2.http.Http;
import com.xjeffrose.xio2.http.HttpResponse;
import java.util.logging.Logger;

class TestService extends Service {
  private static final Logger log = Log.getLogger(TestService.class.getName());

//  public void handleNotFound() {
//    ctx.write(HttpResponse.DefaultResponse(Http.Version.HTTP1_1, Http.Status.NOT_FOUND));
//  }

  public void handleGet(ChannelContext ctx) {
    ctx.write(HttpResponse.DefaultResponse(Http.Version.HTTP1_1, Http.Status.OK));
  }

}