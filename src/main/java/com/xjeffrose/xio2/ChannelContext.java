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
package com.xjeffrose.xio2;

import com.xjeffrose.log.Log;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.logging.Logger;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;

public class ChannelContext {
  private static final Logger log = Log.getLogger(ChannelContext.class.getName());

  private int nread = 1;
  public boolean parserOk;
  public Handler handler;
  private SSLEngineResult sslEngineResult;
  private ByteBuffer encryptedRequest = ByteBuffer.allocateDirect(4096);
  private final ConcurrentLinkedDeque<ByteBuffer> bbList = new ConcurrentLinkedDeque<>();
  public State state = State.got_request;
  public SSLEngine engine;
  public boolean tls = false;
  public SocketChannel channel;

  public ChannelContext(SocketChannel channel, Handler handler) {
    this.channel = channel;
    this.handler = handler;
  }

  public void handleFatalError() {
    handler.handleFatalError(this);
  }

  public enum State {
    got_request,
    start_parse,
    finished_parse,
    start_response,
    finished_response,
  }

  public void read() {
    while (nread > 0 && state == State.got_request) {
      try {
        // Turns out we are only ever allocating 1 input bytebuffer per event loop.
        // It is super efficient but it requires you to sanitize your ByteBuffers
        // before you read each time. There you have it.
        final ByteBuffer inputBuffer = handler.getInputBuffer();
        inputBuffer.clear();
        if (tls && engine != null) {
          nread = channel.read(encryptedRequest);
          encryptedRequest.flip();
          sslEngineResult = engine.unwrap(encryptedRequest, inputBuffer);
          sslEngineResult.getStatus();
        } else {
          nread = channel.read(inputBuffer);
        }

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
    parse();
    state = State.finished_parse;
    if (parserOk) {
      handle();
    } else {
      state = State.start_response;
      handler.handleError(this);
    }
  }

  private boolean parse() {
    parserOk = handler.parse(this);
    return parserOk;
  }

  private void handle() {
    if (state == State.finished_parse) {
      handler.handle(this);
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
      handleFatalError();
      log.severe("This isn't correct - " + channel);
      try {
        channel.close();
      } catch (java.io.IOException e2) {
        throw new RuntimeException(e2);
      }
      throw new RuntimeException(e);
    }
  }

  public void write(ByteBuffer bb) {
    ByteBuffer encryptedResponse = null;
    if (tls) {
      encryptedResponse = ByteBuffer.allocateDirect(engine.getSession().getPacketBufferSize());
    }
    if (state == State.start_response) {
      if (tls && engine != null) {
        try {
          sslEngineResult = engine.wrap(bb, encryptedResponse);
          sslEngineResult.getStatus();
        } catch (SSLException e) {
          e.printStackTrace();
        }
        encryptedResponse.flip();
        bbList.addLast(encryptedResponse);
      } else {
        bbList.addLast(bb);
      }
    }
    state = State.finished_response;
  }
}

