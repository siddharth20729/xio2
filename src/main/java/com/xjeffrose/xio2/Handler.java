package com.xjeffrose.xio2;

import com.xjeffrose.xio2.http.Http;
import java.nio.ByteBuffer;

public interface Handler {

  boolean parse(ChannelContext ctx);

  void handle(ChannelContext ctx);

  void handleError(ChannelContext ctx);

  void handleFatalError(ChannelContext ctx);

  Request getReq();

  Http.Method getMethod();

  ByteBuffer getInputBuffer();
}
