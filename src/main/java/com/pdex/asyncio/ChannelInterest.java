package com.pdex.asyncio;

public interface ChannelInterest {
  void registerReadInterest();
  void unregisterReadInterest();
  void registerWriteInterest();
  void unregisterWriteInterest();
  boolean wantRead();
  boolean wantWrite();
}
