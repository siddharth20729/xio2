package com.xjeffrose.xio2;

import java.nio.channels.SocketChannel;

public interface Handler {

  boolean parse(ChannelContext ctx);

  void handle(ChannelContext ctx);

  void handleError(ChannelContext ctx);

  void handleFatalError(ChannelContext ctx);

  ChannelContext buildChannelContext(SocketChannel channel, String requestId);

  void secureContext(SecureChannelContext secureChannelContext);

  Firewall firewall();

  void logRequest(ChannelContext ctx);

}
