package com.pdex.asyncio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Connection extends Selectable {
  private static final Logger log = Logger.getLogger(Connection.class.getName());

  private final SocketChannel channel;
  private final Protocol protocol;
  private InetSocketAddress address;
  private ByteBuffer inputBuffer;
  private ByteBuffer outputBuffer;
  private SelectionKey key;

  public Connection(SocketChannel channel, Protocol protocol) {
    this.channel = channel;
    this.protocol = protocol;
  }

  public void configure(Selector selector) {
    try {
      this.address = (InetSocketAddress) channel.getLocalAddress();
      channel.configureBlocking(false);
      protocol.onConnect();
      key = channel.register(selector, SelectionKey.OP_WRITE | SelectionKey.OP_READ, this);
    } catch (IOException e) {
      log.log(Level.SEVERE, "Error configuring Connection for address '" + new Address(address) + "'", e);
    }
  }

  @Override
  public void fill() throws IOException {
    if (inputBuffer == null) {
      int capacity = channel.getOption(StandardSocketOptions.SO_RCVBUF);
      log.info("Allocating " + capacity + " bytes for inputBuffer");
      inputBuffer = ByteBuffer.allocateDirect(capacity);
    }
    log.info("inputBuffer " + inputBuffer);
    if (inputBuffer.position() > 0) {
      inputBuffer.compact();
    }
    log.info("inputBuffer " + inputBuffer);
    int nread = channel.read(inputBuffer);
    log.info("Read " + nread + " bytes ");
    if (inputBuffer.position() == inputBuffer.capacity()) {
      log.info("inputBuffer " + inputBuffer);
      int capacity = inputBuffer.capacity() * 2;
      log.info("Allocating " + capacity + " bytes for inputBuffer");
      ByteBuffer tmp = ByteBuffer.allocateDirect(capacity);
      inputBuffer.flip();
      tmp.put(inputBuffer);
      inputBuffer = tmp;
    }
    log.info("inputBuffer " + inputBuffer);
    inputBuffer.flip();
    log.info("inputBuffer " + inputBuffer);
    protocol.onInputReady(inputBuffer, key);
  }

  @Override
  public void flush() throws IOException {
    if (outputBuffer == null) {
      int capacity = channel.getOption(StandardSocketOptions.SO_SNDBUF);
      log.info("Allocating " + capacity + " bytes for outputBuffer");
      outputBuffer = ByteBuffer.allocateDirect(capacity);
      outputBuffer.limit(0);
    }
    ByteBuffer buffer = protocol.onOutputReady();
    log.info("outputBuffer " + outputBuffer);
    log.info("buffer " + buffer);
    if (buffer != null && buffer.remaining() > 0) {
      outputBuffer.limit(outputBuffer.limit() + buffer.remaining());
      outputBuffer.put(buffer);
      outputBuffer.flip();
//        long nwrote = channel.write(new ByteBuffer[] {outputBuffer, buffer});
//      getKey().interestOps(getKey().interestOps() ^ SelectionKey.OP_WRITE);
//      log.info("wrote request");
    }
    if (outputBuffer.remaining() > 0) {
      int nwrote = channel.write(outputBuffer);
      log.info("wrote " + nwrote + " bytes");
      outputBuffer.compact();
      if (outputBuffer.position() == 0) {
        outputBuffer.limit(0);
      }
    }
  }
//    public SocketChannel getChannel() {
//      return channel;
//    }
//    public SelectionKey getKey() {
//      return key;
//    }
}
