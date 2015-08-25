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

  public HttpClientConnection(Request request) {
    this.request = request;
  }

  @Override
  public void onInputReady(ByteBuffer buffer, SelectionKey key) throws IOException {
    ByteBuffer view = buffer.duplicate();
    CharBuffer charBuffer = Charset.forName("UTF-8").decode(view);
    Matcher matcher = pattern.matcher(charBuffer);
    if (matcher.find()) {
      charBuffer.rewind();
      log.info("MATCHES " + charBuffer);
      key.cancel();
    } else {
      charBuffer.rewind();
      log.info("DOESN'T MATCH " + charBuffer.toString().replace("\r", "\\r").replace("\n", "\\n"));
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
}
