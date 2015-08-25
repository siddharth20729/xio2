package com.pdex.asyncio;

import java.nio.channels.SocketChannel;

public interface ConnectionFactory {
  Connection build(SocketChannel client);
}
