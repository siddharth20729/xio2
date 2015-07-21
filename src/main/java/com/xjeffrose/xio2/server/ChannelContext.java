package com.xjeffrose.xio2.server;

import com.xjeffrose.log.Log;
import com.xjeffrose.xio2.http.Http;
import com.xjeffrose.xio2.http.HttpParser;
import com.xjeffrose.xio2.http.HttpRequest;
import com.xjeffrose.xio2.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.logging.Logger;

class ChannelContext {
  private static final Logger log = Log.getLogger(ChannelContext.class.getName());

  private final ConcurrentLinkedDeque<ByteBuffer> bbList = new ConcurrentLinkedDeque<>();

  private final HttpParser parser = new HttpParser();
  public final HttpRequest req = new HttpRequest();

  private State state = State.got_request;
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

    while (nread > 0 && state == State.got_request) {
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
      for (Map.Entry<Route, Service> entry : routes.entrySet()) {
        if (entry.getKey().matches(uri)) {
          state = State.start_response;
          entry.getValue().handle(this, req);
        }
      }
      if (state == State.finished_parse) {
        state = State.start_response;
        write(HttpResponse.DefaultResponse(Http.Version.HTTP1_1, Http.Status.NOT_FOUND));
      }
    }
  }

  public void flush() {
    try {
      if (!bbList.isEmpty()) {
        for (int i = 0; i < bbList.size(); i++) {
          channel.write(bbList.removeFirst());
        }
      }
      channel.close();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    state = State.finished_response;
  }

  public void write(HttpResponse resp) {
    if (state == State.start_response) {
      bbList.addLast(resp.toBB());
    }
  }
}
