package com.pdex.asyncio;

import java.nio.channels.SelectionKey;

public class NetChannelInterest implements ChannelInterest {
  private final SelectionKey key;

  public NetChannelInterest(SelectionKey key) {
    this.key = key;
  }

  @Override
  public void registerReadInterest() {
    key.interestOps(key.interestOps() | SelectionKey.OP_READ);
  }

  @Override
  public void unregisterReadInterest() {
    key.interestOps(key.interestOps() ^ SelectionKey.OP_READ);
  }

  @Override
  public void registerWriteInterest() {
    key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
  }

  @Override
  public void unregisterWriteInterest() {
    key.interestOps(key.interestOps() ^ SelectionKey.OP_WRITE);
  }

  @Override
  public boolean wantRead() {
    return (key.interestOps() & SelectionKey.OP_READ) == SelectionKey.OP_READ;
  }

  @Override
  public boolean wantWrite() {
    return (key.interestOps() & SelectionKey.OP_WRITE) == SelectionKey.OP_WRITE;
  }
}
