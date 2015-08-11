package com.xjeffrose.xio2.tcp.server;

import java.nio.ByteBuffer;

public class TcpResponse {
  public final ByteBuffer inputBuffer = ByteBuffer.allocateDirect(4096);

  public TcpResponse() { };

  public ByteBuffer toBB() {
    inputBuffer.flip();
    return inputBuffer;
  }
}
