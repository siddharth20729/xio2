package com.xjeffrose.xio2.tcp.server;

import com.xjeffrose.xio2.Request;
import com.xjeffrose.xio2.util.BB;
import java.nio.ByteBuffer;

public class TcpRequest implements Request {
  public ByteBuffer inputBuffer = ByteBuffer.allocateDirect(4096);

  public TcpRequest() { }

  public TcpRequest(String reqString) {
    inputBuffer.put(BB.StringtoBB(reqString));
  }

  public ByteBuffer toBB() {
    inputBuffer.flip();
    return inputBuffer;
  }
}
