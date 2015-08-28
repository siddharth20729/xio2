package com.pdex.asyncio;

public class AppChannelInterest implements ChannelInterest {
  private boolean _wantRead = true;
  private boolean _wantWrite = true;
  @Override
  public void registerReadInterest() {
    _wantRead = true;
  }

  @Override
  public void unregisterReadInterest() {
    _wantRead = false;
  }

  @Override
  public void registerWriteInterest() {
    _wantWrite = true;
  }

  @Override
  public void unregisterWriteInterest() {
    _wantWrite = false;
  }

  @Override
  public boolean wantRead() {
    return _wantRead;
  }

  @Override
  public boolean wantWrite() {
    return _wantWrite;
  }
}
