package com.xjeffrose.xio2;

import com.sun.javafx.image.ByteToBytePixelConverter;
import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.SocketAddress;
import java.net.StandardSocketOptions;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;

public class SSLSpike {
  private static final Logger log = Logger.getLogger(SSLSpike.class.getName());
  public interface SelectorLoopStrategy {
    SelectorLoop getLoop();
  }
  public interface ConnectionFactory {
    Connection build(SocketChannel client);
  }
  public abstract class Selectable {
    public void accept() throws IOException {}
    public void configure(Selector selector) {}
    public void connect() throws IOException {}
    public void fill() throws IOException {}
    public void flush() throws IOException {}
  }
  public class Address {
    private final InetSocketAddress address;
    public Address(InetSocketAddress address) {
      this.address = address;
    }
    public Address(SocketAddress address) {
      this.address = (InetSocketAddress)address;
    }
    public String toString() {
      return address.getAddress().getHostAddress() + ":" + address.getPort();
    }
  }
  public class Acceptor extends Selectable {
    private ServerSocketChannel channel;
    private final InetSocketAddress address;
    private final ConnectionFactory factory;
    private final SelectorLoopStrategy strategy;
    public Acceptor(InetSocketAddress address, ConnectionFactory factory, SelectorLoopStrategy strategy) {
      this.address = address;
      this.factory = factory;
      this.strategy = strategy;
    }
    public void accept() throws IOException {
      SocketChannel client = channel.accept();
      Connection connection = factory.build(client);
      strategy.getLoop().add(connection);
      log.info("Accepted incoming connection to address '" + new Address(address) + "' from address '" + new Address(client.getRemoteAddress()) + "'");
    }
    public void configure(Selector selector) {
      try {
        channel = ServerSocketChannel.open();
        channel.configureBlocking(false);
        channel.bind(address);
        channel.register(selector, SelectionKey.OP_ACCEPT, this);
      } catch (IOException e) {
        log.log(Level.SEVERE, "Error configuring Acceptor for address '" + new Address(address) + "'", e);
      }
    }
  }
  public interface Protocol {
    void onConnect();
    void onInputReady(ByteBuffer buffer, SelectionKey key) throws IOException;
    ByteBuffer onOutputReady() throws IOException;
  }
  public class Connection extends Selectable {
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
        this.address = (InetSocketAddress)channel.getLocalAddress();
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
        outputBuffer.limit(outputBuffer.limit()+buffer.remaining());
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
  public class TLSConnection implements Protocol {
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
      log.info("handleHandshakeStatus " +handshakeStatus);
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
  public class Connector extends Selectable {
    private SocketChannel channel;
    private final InetSocketAddress address;
    private final ConnectionFactory factory;
    private final SelectorLoopStrategy strategy;
    private SelectionKey key;
    public Connector(InetSocketAddress address, ConnectionFactory factory, SelectorLoopStrategy strategy) {
      this.address = address;
      this.factory = factory;
      this.strategy = strategy;
    }
    public void connect() throws IOException {
      if (channel.finishConnect()) {
        key.interestOps(0);
        Connection connection = factory.build(channel);
        strategy.getLoop().add(connection);
        log.info("Connected outgoing connection to address '" + new Address(address) + "' from address '" + new Address(channel.getLocalAddress()) + "'");
      }
    }
    public void configure(Selector selector) {
      try {
        channel = SocketChannel.open();
        channel.configureBlocking(false);
        channel.connect(address);
        key = channel.register(selector, SelectionKey.OP_CONNECT, this);
      } catch (IOException e) {
        log.log(Level.SEVERE, "Error configuring Connector for address '" + new Address(address) + "'", e);
      }
    }
  }

  public class SelectorLoop implements Closeable, Runnable {
    private final ConcurrentLinkedDeque<Selectable> selectablesToAdd = new ConcurrentLinkedDeque<>();
    private final AtomicBoolean isRunning = new AtomicBoolean(true);
    private Selector selector;
    private boolean running() {
      return isRunning.get();
    }
    private void setupNewSelectables() {
      while (selectablesToAdd.size() > 0) {
        Selectable selectable = selectablesToAdd.pop();
        selectable.configure(selector);
      }
    }
    private void handleSelection(SelectionKey key) {
      try {
        Selectable selectable = (Selectable) key.attachment();
        if (key.isValid() && key.isReadable()) {
          selectable.fill();
        }
        if (key.isValid() && key.isWritable()) {
          selectable.flush();
        }
        if (key.isValid() && key.isAcceptable()) {
          selectable.accept();
        }
        if (key.isValid() && key.isConnectable()) {
          selectable.connect();
        }
        if (!key.isValid()) {
          key.channel().close();
          key.cancel();
        }
        //This is a catch all for any error in this thread.
      } catch (Exception e) {
        log.log(Level.SEVERE, "Error inside Read | Write loop", e);
        key.cancel();
      }
    }
    private void poke() {
      if (selector != null) {
        selector.wakeup();
      }
    }

    public void add(Selectable selectable) {
      selectablesToAdd.push(selectable);
      poke();
    }

    @Override
    public void close() {
      isRunning.set(false);
      poke();
    }

    @Override
    public void run() {
      try {
        selector = Selector.open();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }

      while (running()) {
        try {
          setupNewSelectables();
          selector.select();

          Set<SelectionKey> acceptKeys = selector.selectedKeys();
          Iterator<SelectionKey> iterator = acceptKeys.iterator();

          while (iterator.hasNext()) {
            SelectionKey key = iterator.next();
            iterator.remove();

            handleSelection(key);

            if (!running()) {
              break;
            }
          }
        } catch (Exception e) {
          log.log(Level.SEVERE, "Error in SelectorLoop", e);
        }
      }
    }
  }

  public class HttpClientConnectionFactory implements ConnectionFactory {
    final private Request request;
    public HttpClientConnectionFactory(Request request) {
      this.request = request;
    }
    public Connection build(SocketChannel channel) {
      if (request.url.getProtocol().equals("https")) {
        return new Connection(channel, new TLSConnection(new HttpClientConnection(request)));
      } else {
        return new Connection(channel, new HttpClientConnection(request));
      }
    }
  }
  class HttpClientConnection implements Protocol {
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
          .append("GET / HTTP/1.1")
          .append(CRLF)
          .append("User-Agent: curl/7.37.1")
          .append(CRLF)
          .append("Host: ")
          .append(request.url.getHost())
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
  class Request {
    final private URL url;
    Request(URL url) {
      this.url = url;
    }
    Request(String url) {
      try {
        this.url = new URL(url);
      } catch (MalformedURLException e) {
        throw new RuntimeException(e);
      }
    }
  }
  interface RequestConnectionFactory {
    ConnectionFactory build(Request request);
  }
  class Client {
    final private RequestConnectionFactory requestConnectionFactory;
    final private SelectorLoopStrategy strategy;

    Client(RequestConnectionFactory requestConnectionFactory, SelectorLoopStrategy strategy) {
      this.requestConnectionFactory = requestConnectionFactory;
      this.strategy = strategy;
    }

    private InetSocketAddress getAddress(URL url) {
      String host = url.getHost();
      int port = url.getPort();
      if (port == -1) {
        port = url.getDefaultPort();
      }
      return new InetSocketAddress(host, port);
    }
    private Connector getConnector(Request request, ConnectionFactory factory) {
      return new Connector(getAddress(request.url), factory, strategy);
    }
    public void execute(Request request) {
      strategy.getLoop().add(getConnector(request, requestConnectionFactory.build(request)));
    }
  }
  SelectorLoop selectorLoop = new SelectorLoop();
  InetSocketAddress address = new InetSocketAddress("127.0.0.1", 8080);
  ConnectionFactory factory = client -> new Connection(client, new Protocol() {
    @Override
    public void onConnect() {
    }
    @Override
    public void onInputReady(ByteBuffer buffer, SelectionKey key) throws IOException {
    }
    @Override
    public ByteBuffer onOutputReady() {
      return null;
    }
  });
  SelectorLoopStrategy strategy = () -> selectorLoop;
  Acceptor acceptor = new Acceptor(address, factory, strategy);
  Connector connector = new Connector(address, factory, strategy);
  SSLSpike() {
  }
  public void run() {
//    selectorLoop.add(acceptor);
//    new Thread(() -> {
//      try {
//        Thread.sleep(1000);
//      } catch (InterruptedException e) {
//        e.printStackTrace();
//      }
//      selectorLoop.add(connector);
//    }).start();
    Client client = new Client(HttpClientConnectionFactory::new, strategy);
//    client.execute(new Request("https://google.com/"));
    client.execute(new Request("https://twitter.com/"));
    //selectorLoop.add(new Connector(new InetSocketAddress("www.google.com", 80), HttpClientConnection::new, strategy));
//    selectorLoop.add(new Connector(new InetSocketAddress("127.0.0.1", 8666), HttpClientConnection::new, strategy));
    selectorLoop.run();
  }
  static public void main(String args[]) {
    new SSLSpike().run();
  }
}
