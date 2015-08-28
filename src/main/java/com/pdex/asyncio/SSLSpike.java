package com.pdex.asyncio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.logging.Logger;

public class SSLSpike {
  private static final Logger log = Logger.getLogger(SSLSpike.class.getName());

  SelectorLoop.QuenchStrategy quenchStrategy = loop -> {
    System.out.println("quench strategy fired");
    loop.close();
  };
  SelectorLoop selectorLoop = new SelectorLoop(quenchStrategy);
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
    @Override
    public void onOutputComplete(int bytesWritten, ChannelInterest channelInterest) {
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
//    client.execute(new Request("https://www.google.com/"));
//    client.execute(new Request("https://google.com/"));
//    client.execute(new Request("http://google.com/"));
    client.execute(new Request("https://twitter.com/"));
//    client.execute(new Request("https://www.facebook.com/"));
    //selectorLoop.add(new Connector(new InetSocketAddress("www.google.com", 80), HttpClientConnection::new, strategy));
//    selectorLoop.add(new Connector(new InetSocketAddress("127.0.0.1", 8666), HttpClientConnection::new, strategy));
    selectorLoop.run();
  }
  static public void main(String args[]) {
    System.out.println("starting");
    new SSLSpike().run();
    System.out.println("stopping");
  }
}
