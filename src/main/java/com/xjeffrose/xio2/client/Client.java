package com.xjeffrose.xio2.client;

import com.xjeffrose.log.Log;
import com.xjeffrose.xio2.http.Http;
import com.xjeffrose.xio2.http.HttpRequest;
import com.xjeffrose.xio2.http.HttpResponse;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.logging.Logger;

public class Client {
  private static final Logger log = Log.getLogger(Client.class.getName());

  private InetSocketAddress addr;
  private SocketChannel channel;
  private HttpRequest req;
  private boolean tls = false;


  Client() {

  }

  public void connect(String host, int port) throws IOException {

  }

  HttpResponse get(HttpRequest req) throws IOException {
    this.req = req;
    return HttpResponse.DefaultResponse(Http.Version.HTTP1_1, Http.Status.OK);
  }
}
