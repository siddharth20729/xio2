package com.pdex.asyncio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

public interface Protocol {
  void onConnect();

  void onInputReady(ByteBuffer buffer, SelectionKey key) throws IOException;

  ByteBuffer onOutputReady() throws IOException;
}
