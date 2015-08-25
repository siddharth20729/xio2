package com.pdex.asyncio;

import java.io.IOException;
import java.nio.channels.Selector;

public abstract class Selectable {
  public void accept() throws IOException {
  }

  public void configure(Selector selector) {
  }

  public void connect() throws IOException {
  }

  public void fill() throws IOException {
  }

  public void flush() throws IOException {
  }
}
