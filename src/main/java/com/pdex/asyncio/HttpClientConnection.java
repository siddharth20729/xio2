package com.pdex.asyncio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.charset.Charset;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class HttpClientConnection implements Protocol {
  private static final Logger log = Logger.getLogger(HttpClientConnection.class.getName());

  final private Request request;
  final private String CRLF = "\r\n";
  final private Pattern pattern = Pattern.compile(CRLF + CRLF, Pattern.MULTILINE);
  private ByteBuffer outputBuffer;
  private InputBuffer inputBuffer = new InputBuffer(4096);
  private int totalBytesWritten = 0;
  private String header;
  private Pattern contentLengthPattern = Pattern.compile("content-length: (\\d*)", Pattern.CASE_INSENSITIVE);
  private long contentLength = 0;

  public HttpClientConnection(Request request) {
    this.request = request;
  }

  @Override
  public void onInputReady(ByteBuffer buffer, SelectionKey key) throws IOException {
    inputBuffer.put(buffer);
    CharBuffer charBuffer = inputBuffer.charView();
    Matcher matcher = pattern.matcher(charBuffer);
    if (matcher.find()) {
      if (header == null) {
        charBuffer.rewind();
        header = pattern.split(charBuffer)[0];
        log.info("Header: " + header);
      }
      if (header != null && contentLength == 0) {
        Matcher m = contentLengthPattern.matcher(header);
        if (m.find()) {
          contentLength = Integer.parseInt(m.group(1));
        }
      }
//      log.info("MATCHES " + inputBuffer);
//      log.info("Header: " + header);
//      log.info("content-length: " + contentLength);
      if (inputBuffer.position() >= header.length() + 4 + contentLength) {
        log.info("We got everything!");
//        key.interestOps(0);
        key.cancel();
      } else {
        log.info("Got " + inputBuffer.position() + " out of " + (header.length() + 4 + contentLength));
      }
    } else {
      log.info("DOESN'T MATCH " + inputBuffer);
//      charBuffer.rewind();
//      log.info("DOESN'T MATCH " + charBuffer.toString().replace("\r", "\\r").replace("\n", "\\n"));
    }
  }

  @Override
  public void onConnect() {
    StringBuilder builder = new StringBuilder();
    builder
        .append("GET ")
        .append(request.getPath())
        .append(" HTTP/1.1")
        .append(CRLF)
        .append("User-Agent: curl/7.37.1")
        .append(CRLF)
        .append("Host: ")
        .append(request.getHost())
        .append(CRLF)
        .append("Accept: */*")
//        .append(CRLF)
//        .append("Accept-Encoding: gzip, deflate")
        .append(CRLF)
        .append(CRLF)
    ;
    int capacity = builder.length();
    log.info("allocating " + capacity + " bytes");
    outputBuffer = ByteBuffer.allocateDirect(builder.length());
    outputBuffer.put(builder.toString().getBytes());
    outputBuffer.flip();
  }

  @Override
  public ByteBuffer onOutputReady() {
    return outputBuffer;
  }

  @Override
  public void onOutputComplete(int bytesWritten, ChannelInterest channelInterest) {
    totalBytesWritten += bytesWritten;
    if (totalBytesWritten == outputBuffer.limit()) {
      channelInterest.unregisterWriteInterest();
    }
  }
}
