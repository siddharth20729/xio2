package com.xjeffrose.xio2;

import com.xjeffrose.xio2.http.HttpRequest;
import java.nio.ByteBuffer;

public interface Handler {

  boolean parse(ChannelContext ctx);

  void handle(ChannelContext ctx);

  void handleError(ChannelContext ctx);

  void handleFatalError(ChannelContext ctx);

  //TODO: Need to make this a generic request
  HttpRequest getReq();

  ByteBuffer getInputBuffer();
}
