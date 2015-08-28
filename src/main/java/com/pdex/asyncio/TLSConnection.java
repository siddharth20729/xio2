package com.pdex.asyncio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLException;

public class TLSConnection implements Protocol {
  private static final Logger log = Logger.getLogger(TLSConnection.class.getName());

  final private Protocol protocol;
  private SSLContext sslCtx;
  private SSLEngine engine;
  /**
   * initial state empty
   * onInputReady populate the buffer
   * unwrap drain the buffer
   */
  private InputBuffer netInputBuffer;
  /**
   * initial state empty
   * wrap populate the buffer
   * onOutputReady copy/clear the buffer
   */
  private InputBuffer netOutputBuffer;
  /**
   * initial state empty
   * unwrap populate the buffer
   * onInputReady copy/clear the buffer
   */
  private InputBuffer appInputBuffer;
  /**
   * initial state empty
   * onOutputReady populate the buffer
   * wrap drain the buffer
   */
  private InputBuffer appOutputBuffer;
  private AppChannelInterest appChannelInterest = new AppChannelInterest();
  private int totalBytesWritten = 0;
  private int appBytesWritten = 0;
  private HandshakeStatus handshakeStatus;
  private enum Mode {
    Wrap,
    Unwrap;
  }

  private void handleHandshakeStatus(SSLEngineResult.HandshakeStatus handshakeStatus) throws SSLException {
//    log.info("handleHandshakeStatus " + handshakeStatus);
    this.handshakeStatus = handshakeStatus;
    switch (handshakeStatus) {
      case NEED_TASK:
        engine.getDelegatedTask().run();
        break;
      case NEED_UNWRAP:
        break;
      case NEED_WRAP:
        break;
      case NOT_HANDSHAKING:
        break;
      case FINISHED:
        break;
    }
  }

  private void handleSSLEngineResult(SSLEngineResult sslEngineResult, Mode mode) throws SSLException {
//    if (sslEngineResult.getStatus() != SSLEngineResult.Status.OK) {
//      log.info("handleSSLEngineResult " + sslEngineResult);
//      logBufferStatus("after " + mode);
//    }
    handleHandshakeStatus(sslEngineResult.getHandshakeStatus());
    if (sslEngineResult.getStatus() == SSLEngineResult.Status.BUFFER_OVERFLOW) {
      switch (mode) {
        case Unwrap:
          appInputBuffer.increaseCapacity(netInputBuffer.getBytesAvailableForReading());
          handleSSLEngineResult(unwrap(), Mode.Unwrap);
          break;
        case Wrap:
          netOutputBuffer.increaseCapacity(appOutputBuffer.getBytesAvailableForReading());
          handleSSLEngineResult(wrap(), Mode.Wrap);
          break;
      }
    }
    if (sslEngineResult.getStatus() == SSLEngineResult.Status.OK) {
      if (sslEngineResult.getHandshakeStatus() == HandshakeStatus.NOT_HANDSHAKING) {
        if (netInputBuffer.getBytesAvailableForReading() > 0) {
          handleSSLEngineResult(unwrap(), Mode.Unwrap);
        }
        if (appOutputBuffer.getBytesAvailableForReading() > 0) {
          handleSSLEngineResult(wrap(), Mode.Wrap);
        }
      } else if (sslEngineResult.bytesProduced() == 0) {
        if (netInputBuffer.getBytesAvailableForReading() > 0 && sslEngineResult.getHandshakeStatus() == HandshakeStatus.NEED_UNWRAP) {
          handleSSLEngineResult(unwrap(), Mode.Unwrap);
        } else if (sslEngineResult.getHandshakeStatus() == HandshakeStatus.NEED_WRAP) {
          handleSSLEngineResult(wrap(), Mode.Wrap);
        }
      }
    }
  }

  private void logBufferStatus(String message) {
    log.info(message + " " +
            "BUFFER STATUS \n" +
            "   netInputBuffer: " + netInputBuffer + "\n" +
            "   netOutputBuffer: " + netOutputBuffer + "\n" +
            "   appInputBuffer: " + appInputBuffer + "\n" +
            "   appOutputBuffer: " + appOutputBuffer + "\n"
    );
  }

  private SSLEngineResult unwrap() throws SSLException {
    try (InputBuffer.Guard g = netInputBuffer.openForReading(); InputBuffer.Guard gw = appInputBuffer.openForWriting()) {
      return engine.unwrap(g.getByteBuffer(), gw.getByteBuffer());
    }
  }

  private SSLEngineResult wrap() throws SSLException {
    try (InputBuffer.Guard g = appOutputBuffer.openForReading(); InputBuffer.Guard gw = netOutputBuffer.openForWriting()) {
      return engine.wrap(g.getByteBuffer(), gw.getByteBuffer());
    }
  }

  public TLSConnection(Protocol protocol) {
    this.protocol = protocol;
//    log.setLevel(Level.OFF);
  }

  @Override
  public void onConnect() {
    try {
      sslCtx = SSLContext.getInstance("TLS");
      sslCtx.init(null, null, null);
      engine = sslCtx.createSSLEngine();
      engine.setNeedClientAuth(false);
      engine.setUseClientMode(true);
      netInputBuffer = new InputBuffer(engine.getSession().getPacketBufferSize());
      netOutputBuffer = new InputBuffer(engine.getSession().getPacketBufferSize());
      appInputBuffer = new InputBuffer(engine.getSession().getApplicationBufferSize());
      appOutputBuffer = new InputBuffer(engine.getSession().getApplicationBufferSize());
      protocol.onConnect();
    } catch (NoSuchAlgorithmException | KeyManagementException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void onInputReady(ByteBuffer buffer, SelectionKey key) throws IOException {
    log.info("INPUT INPUT INPUT INPUT INPUT INPUT INPUT INPUT INPUT INPUT");
//    logBufferStatus("before if");
//    if (buffer.remaining() > 0) {
//      log.info("BUFFER " + buffer);
//      appendToBuffer(buffer, netInputBuffer);
//    }
    netInputBuffer.put(buffer);
//    logBufferStatus("before unwrap");
    handleSSLEngineResult(unwrap(), Mode.Unwrap);
//    logBufferStatus("after unwrap");
//    if (netInputBuffer.position() != 0) {
//      netInputBuffer.compact();
//      netInputBuffer.flip();
//    }
//    logBufferStatus("after compact");
//      log.info("appInputBuffer " + appInputBuffer);
    if (appInputBuffer.getBytesAvailableForReading() > 0) {
      try (InputBuffer.Guard g = appInputBuffer.openForReading()) {
        protocol.onInputReady(g.getByteBuffer(), key);
      }
    }
//    logBufferStatus("after on input");
  }

  @Override
  public ByteBuffer onOutputReady() throws IOException {
    log.info("OUTPUT OUTPUT OUTPUT OUTPUT OUTPUT OUTPUT OUTPUT OUTPUT OUTPUT OUTPUT");
    ByteBuffer buffer = protocol.onOutputReady();
//    logBufferStatus("before if");
    if (buffer != null && buffer.remaining() > 0) {
      appBytesWritten = buffer.remaining();
      appOutputBuffer.put(buffer);
//      appendToBuffer(buffer, appOutputBuffer);
    }
//    logBufferStatus("before wrap");
    handleSSLEngineResult(wrap(), Mode.Unwrap);
//    logBufferStatus("after wrap");
//    if (appOutputBuffer.position() != 0) {
//      appOutputBuffer.compact();
//    }
//    netInputBuffers.toArray(new ByteBuffer[netInputBuffers.size()]);
//    engine.wrap()
    if (netOutputBuffer.getBytesAvailableForReading() > 0) {
//      log.info("net output buffer " + netOutputBuffer);
      try (InputBuffer.Guard g = netOutputBuffer.openForReading()) {
        buffer = ByteBuffer.allocateDirect(netOutputBuffer.getBytesAvailableForReading());
        buffer.put(g.getByteBuffer());
        buffer.flip();
        return buffer;
      }
    }
//    if (netOutputBuffer.position() != 0) {
//      logBufferStatus("before flip");
//      netOutputBuffer.flip();
//      buffer = ByteBuffer.allocateDirect(netOutputBuffer.remaining());
//      logBufferStatus("before put");
//      buffer.put(netOutputBuffer);
//      logBufferStatus("before compact");
//      netOutputBuffer.compact();
//      logBufferStatus("after compact");
//      buffer.flip();
//      return buffer;
//    }
    return null;
  }

  @Override
  public void onOutputComplete(int bytesWritten, ChannelInterest channelInterest) {
    totalBytesWritten += bytesWritten;
    protocol.onOutputComplete(appBytesWritten, appChannelInterest);
//    if (!appChannelInterest.wantWrite()) {
//      engine.closeOutbound();
//    }
    // TODO check that all of netOutputBuffer has been written
    if (!appChannelInterest.wantWrite() && totalBytesWritten >= netOutputBuffer.getBytesWritten() && handshakeStatus == HandshakeStatus.NOT_HANDSHAKING) {
      log.info("turning off write interest " + handshakeStatus);
      channelInterest.unregisterWriteInterest();
    } else {
      log.info("want write " + appChannelInterest.wantWrite() + " totalBytesWritten " + totalBytesWritten + " netOutputBuffer " + netOutputBuffer.getBytesWritten() + " " + handshakeStatus);
    }
  }
}
