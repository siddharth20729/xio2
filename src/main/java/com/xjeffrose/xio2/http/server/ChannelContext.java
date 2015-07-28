package com.xjeffrose.xio2.http.server;

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
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;

public class ChannelContext {
  private static final Logger log = Log.getLogger(ChannelContext.class.getName());

  private final ConcurrentLinkedDeque<ByteBuffer> bbList = new ConcurrentLinkedDeque<ByteBuffer>();

  private final HttpParser parser = new HttpParser();
  public final HttpRequest req = new HttpRequest();

  public SSLEngine engine;
  public boolean ssl = false;
  public SocketChannel channel;

  private int nread = 1;
  private boolean parserOk;
  private Map<Route, HttpHandler> routes;
  private State state = State.got_request;
  private SSLEngineResult sslEngineResult;
  private ByteBuffer encryptedRequest = ByteBuffer.allocateDirect(4096);


  ChannelContext(SocketChannel channel, Map<Route, HttpHandler> routes) {
    this.channel = channel;
    this.routes = routes;
  }

  private enum State {
    got_request,
    start_parse,
    finished_parse,
    start_response,
    finished_response,
  }

  public void read() {
    while (nread > 0 && state == State.got_request) {
      try {
        if (ssl && engine != null) {
          nread = channel.read(encryptedRequest);
          encryptedRequest.flip();
          sslEngineResult = engine.unwrap(encryptedRequest, req.inputBuffer);
          sslEngineResult.getStatus();
        } else {
          nread = channel.read(req.inputBuffer);
        }
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
    } else {
      state = State.start_response;
      write(HttpResponse.DefaultResponse(Http.Version.HTTP1_1, Http.Status.BAD_REQUEST));
    }
  }

  private void handleReq() {
    final String uri = req.getUri().toString();
    if (state == State.finished_parse) {
      for (Map.Entry<Route, HttpHandler> entry : routes.entrySet()) {
        if (entry.getKey().matches(uri)) {
          state = State.start_response;
          entry.getValue().handle(this, req);
        }
      }
      state = State.start_response;
      write(HttpResponse.DefaultResponse(Http.Version.HTTP1_1, Http.Status.NOT_FOUND));
    }
  }

  public void flush() {
    try {
      if (!bbList.isEmpty()) {
        for (int i = 0; i < bbList.size(); i++) {
          channel.write(bbList.removeFirst());
        }
        channel.close();
      }
    } catch (Exception e) {
      write(HttpResponse.DefaultResponse(Http.Version.HTTP1_1, Http.Status.INTERNAL_SERVER_ERROR));
      log.severe("This isn't correct - " + channel);
      //channel.close();
      throw new RuntimeException(e);
    }
  }

  public void write(HttpResponse resp) {
    ByteBuffer encryptedResponse = null;
    if (ssl) {
      encryptedResponse = ByteBuffer.allocateDirect(engine.getSession().getPacketBufferSize());
    }
    if (state == State.start_response) {
      if (ssl && engine != null) {
        try {
          sslEngineResult = engine.wrap(resp.toBB(), encryptedResponse);
          sslEngineResult.getStatus();
        } catch (SSLException e) {
          e.printStackTrace();
        }
        encryptedResponse.flip();
        bbList.addLast(encryptedResponse);
      } else {
        bbList.addLast(resp.toBB());
      }
    }
    state = State.finished_response;
  }
}

