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
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class ChannelContext {
  private static final Logger log = Log.getLogger(ChannelContext.class.getName());
  private final String requestId;

  private int nread = 1;
  public Handler handler;
  protected final ConcurrentLinkedDeque<ByteBuffer> bbList = new ConcurrentLinkedDeque<>();
  public State state = State.got_request;
  public SocketChannel channel;
  public Request req;
  public ByteBuffer inputBuffer = ByteBuffer.allocateDirect(4096);

  public ChannelContext(SocketChannel channel, Handler handler, String requestId) {
    this.channel = channel;
    this.handler = handler;
    this.requestId = requestId;
  }

  public boolean isSecure() {
    return false;
  }

  public void handleFatalError() {
    handler.handleFatalError(this);
  }

  public void onConnect() {
  }

  public enum State {
    got_request,
    start_parse,
    finished_parse,
    start_response,
    finished_response,
  }

  public int readIntoBuffer(ByteBuffer inputBuffer) {
    try {
      return channel.read(inputBuffer);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void read() {
    while (nread > 0 && state == State.got_request) {
      inputBuffer.clear();
      nread = readIntoBuffer(inputBuffer);
      state = State.start_parse;
    }
    if (nread == -1) {
      try {
        channel.close();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    if (handler.parse(this)) {
      state = State.finished_parse;
      handler.handle(this);
    } else {
      state = State.start_response;
      handler.handleError(this);
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
    if (state == State.start_response) {
      bbList.add(bb);
      state = State.finished_response;
    }
  }
}

