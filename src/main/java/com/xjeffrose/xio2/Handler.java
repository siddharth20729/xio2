package com.xjeffrose.xio2;

import com.xjeffrose.xio2.http.Http;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public interface Handler {

  boolean parse();

  void handle(ChannelContext ctx);

  void handleError(ChannelContext ctx);

  void handleFatalError(ChannelContext ctx);

  Request getReq();

  Http.Method getMethod();

  ByteBuffer getInputBuffer();

  ChannelContext buildChannelContext(SocketChannel channel);

  void secureContext(SecureChannelContext secureChannelContext);
}
