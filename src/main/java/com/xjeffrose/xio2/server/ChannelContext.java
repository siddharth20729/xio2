package com.xjeffrose.xio2.server;

import com.xjeffrose.log.Log;
import com.xjeffrose.xio2.http.HttpParser;
import com.xjeffrose.xio2.http.HttpRequest;
import com.xjeffrose.xio2.http.HttpResponse;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.logging.Logger;

class ChannelContext {
  private static final Logger log = Log.getLogger(ChannelContext.class.getName());

  public final HttpParser parser = new HttpParser();
  public final HttpRequest req = new HttpRequest();
  public final HttpResponse resp = new HttpResponse();

  private State state = State.got_request;
  private Service service;
  private boolean parserOk;
  public SocketChannel channel;
  private Map<Route, Service> routes;

  ChannelContext(SocketChannel channel, Map<Route, Service> routes) {
    this.channel = channel;
    this.routes = routes;
  }

  private enum State {
    got_request,
    start_parse,
    finished_parse,
    start_response,
    finished_response,
  };

  public void read() {
    int nread = 1;

    while (nread > 0) {
      try {
        nread = channel.read(req.inputBuffer);
        parserOk = parser.parse(req);
        state = State.start_parse;
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    if (nread == -1) {
      try {
        channel.close();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    state = State.finished_parse;
    if (parserOk) {
      handleReq();
    }
  }

  private void handleReq() {
    final String uri = req.getUri().toString();
    if (state == State.finished_parse) {
      routes.entrySet()
          .stream()
          .parallel()
          .filter(entry -> entry.getKey().matches(uri))
          .forEach(entry -> entry.getValue().handle(entry.getKey(), req, resp));
    }
  }

  public void write() {
    try {
      if (state == State.finished_parse) {
        state = State.start_response;
        channel.write(resp.inputBuffer);
        channel.close();
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    state = State.finished_response;
  }
}
