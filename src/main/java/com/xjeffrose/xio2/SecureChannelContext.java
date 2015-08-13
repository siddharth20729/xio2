package com.xjeffrose.xio2;

import com.xjeffrose.xio2.TLS.TLS;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class SecureChannelContext extends ChannelContext {
  private SSLEngineResult sslEngineResult;
  private ByteBuffer encryptedRequest = ByteBuffer.allocateDirect(4096);
  public SSLEngine engine;

  public SecureChannelContext(SocketChannel channel, Handler handler) {
    super(channel, handler);
  }

  public boolean isSecure() {
    return true;
  }

  public void onConnect() {
    handler.secureContext(this);
  }

  public int readIntoBuffer(ByteBuffer inputBuffer) {
    try {
      int result;
      result = channel.read(encryptedRequest);
      encryptedRequest.flip();
      sslEngineResult = engine.unwrap(encryptedRequest, inputBuffer);
      return result;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void write(ByteBuffer bb) {
    ByteBuffer encryptedResponse = ByteBuffer.allocateDirect(engine.getSession().getPacketBufferSize());
    if (state == State.start_response) {
      try {
        sslEngineResult = engine.wrap(bb, encryptedResponse);
      } catch (SSLException e) {
        e.printStackTrace();
      }
      encryptedResponse.flip();
      bbList.addLast(encryptedResponse);
      state = State.finished_response;
    }
  }
}
