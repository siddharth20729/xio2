package com.pdex.asyncio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.logging.Logger;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;

public class TLSConnection implements Protocol {
  private static final Logger log = Logger.getLogger(TLSConnection.class.getName());

  final private Protocol protocol;
  private SSLContext sslCtx;
  private SSLEngine engine;
  private ArrayList<ByteBuffer> netInputBuffers = new ArrayList<>();
  private ByteBuffer netInputBuffer;
  private ByteBuffer netOutputBuffer;
  private ByteBuffer appInputBuffer;
  private ByteBuffer appOutputBuffer;

  private void handleSSLEngineResult(SSLEngineResult sslEngineResult) throws SSLException {
    log.info("handleSSLEngineResult " + sslEngineResult);
    handleHandshakeStatus(sslEngineResult.getHandshakeStatus());
    if (sslEngineResult.getStatus() == SSLEngineResult.Status.OK && sslEngineResult.bytesProduced() == 0) {
      if (sslEngineResult.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.NEED_UNWRAP && bufferHasData(netInputBuffer)) {
        handleSSLEngineResult(unwrap());
      } else if (sslEngineResult.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.NEED_WRAP && bufferHasData(appOutputBuffer)) {
        handleSSLEngineResult(wrap());
      }
    }
  }

  private boolean bufferHasData(ByteBuffer buffer) {
    return !(buffer.position() == 0 && buffer.limit() == buffer.capacity());
  }

  private void appendToBuffer(ByteBuffer src, ByteBuffer dst) {
    dst.mark();
    log.info("dst " + dst);
    if (dst.limit() != dst.capacity()) {
      dst.position(dst.limit());
      dst.limit(dst.capacity());
    }
    log.info("dst " + dst + " src " + src);
    dst.put(src);
    log.info("dst " + dst + " src " + src);
    dst.limit(dst.position());
    log.info("dst " + dst);
    dst.reset();
    log.info("dst " + dst);
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
    return engine.unwrap(netInputBuffer, appInputBuffer);
  }

  private SSLEngineResult wrap() throws SSLException {
    return engine.wrap(appOutputBuffer, netOutputBuffer);
  }

  private void handleHandshakeStatus(SSLEngineResult.HandshakeStatus handshakeStatus) throws SSLException {
    log.info("handleHandshakeStatus " + handshakeStatus);
    switch (handshakeStatus) {
      case NEED_TASK:
        engine.getDelegatedTask().run();
        break;
      case NEED_UNWRAP:
//          handleSSLEngineResult(unwrap());
        break;
      case NEED_WRAP:
//          handleSSLEngineResult(wrap());
        break;
      case NOT_HANDSHAKING:
        break;
      case FINISHED:
        break;
    }
  }

  public TLSConnection(Protocol protocol) {
    this.protocol = protocol;
  }

  @Override
  public void onConnect() {
    try {
      sslCtx = SSLContext.getInstance("TLS");
      sslCtx.init(null, null, null);
      engine = sslCtx.createSSLEngine();
      engine.setNeedClientAuth(false);
      engine.setUseClientMode(true);
      netInputBuffer = ByteBuffer.allocateDirect(engine.getSession().getPacketBufferSize());
      netOutputBuffer = ByteBuffer.allocateDirect(engine.getSession().getPacketBufferSize());
      appInputBuffer = ByteBuffer.allocateDirect(engine.getSession().getApplicationBufferSize());
      appOutputBuffer = ByteBuffer.allocateDirect(engine.getSession().getApplicationBufferSize());
      protocol.onConnect();
    } catch (NoSuchAlgorithmException | KeyManagementException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void onInputReady(ByteBuffer buffer, SelectionKey key) throws IOException {
    log.info("INPUT INPUT INPUT INPUT INPUT INPUT INPUT INPUT INPUT INPUT");
    logBufferStatus("before if");
    if (buffer.remaining() > 0) {
      log.info("BUFFER " + buffer);
      appendToBuffer(buffer, netInputBuffer);
    }
    logBufferStatus("before unwrap");
    handleSSLEngineResult(unwrap());
    logBufferStatus("after unwrap");
    if (netInputBuffer.position() != 0) {
      netInputBuffer.compact();
      netInputBuffer.flip();
    }
    logBufferStatus("after compact");
//      log.info("appInputBuffer " + appInputBuffer);
    appInputBuffer.flip();
    protocol.onInputReady(appInputBuffer, key);
    appInputBuffer.compact();
  }

  @Override
  public ByteBuffer onOutputReady() throws IOException {
    log.info("OUTPUT OUTPUT OUTPUT OUTPUT OUTPUT OUTPUT OUTPUT OUTPUT OUTPUT OUTPUT");
    ByteBuffer buffer = protocol.onOutputReady();
    logBufferStatus("before if");
    if (buffer != null && buffer.remaining() > 0) {
      appendToBuffer(buffer, appOutputBuffer);
    }
    logBufferStatus("before wrap");
    handleSSLEngineResult(wrap());
    logBufferStatus("before compact");
    if (appOutputBuffer.position() != 0) {
      appOutputBuffer.compact();
    }
    if (netOutputBuffer.position() != 0) {
      logBufferStatus("before flip");
      netOutputBuffer.flip();
      buffer = ByteBuffer.allocateDirect(netOutputBuffer.remaining());
      logBufferStatus("before put");
      buffer.put(netOutputBuffer);
      logBufferStatus("before compact");
      netOutputBuffer.compact();
      logBufferStatus("after compact");
      buffer.flip();
      return buffer;
    }
    return null;
  }
}
