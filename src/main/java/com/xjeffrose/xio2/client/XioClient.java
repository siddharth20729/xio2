package com.xjeffrose.xio2.client;

import com.xjeffrose.log.Log;
import com.xjeffrose.xio2.http.HttpRequest;
import com.xjeffrose.xio2.util.BB;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;
import javax.net.ssl.SSLEngineResult;

public class XioClient {
  private static final Logger log = Log.getLogger(XioClient.class.getName());

  private SocketChannel channel;
  private Selector selector;
  private HttpRequest req;
  private boolean parserOk;
  private Iterator<SelectionKey> iterator;
  private ClientTLS tls;
  ByteBuffer encryptedRequest = ByteBuffer.allocateDirect(20000);
  ByteBuffer rawRequest = ByteBuffer.allocateDirect(20000);
  ByteBuffer rawResponse = ByteBuffer.allocateDirect(20000);
  ByteBuffer encryptedResponse = ByteBuffer.allocateDirect(20000);

  XioClient() { }

  public void connect(String host, int port) {
    try {
      channel = SocketChannel.open();
      channel.configureBlocking(false);
      channel.connect(new InetSocketAddress(host, port));
      tls = new ClientTLS(channel,
          rawRequest,
          encryptedRequest,
          encryptedResponse,
          rawResponse
      );
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public HttpObject get(HttpRequest req) {
    this.req = req;


    if (checkConnect()) {
      if (tls.execute()) {
        if (write()) {
          return read();
        }
      }
    }
    return null;
  }

  private boolean checkConnect() {
    try {
      selector = Selector.open();

      channel.register(selector, SelectionKey.OP_CONNECT);
      selector.select();


      Set<SelectionKey> connectKeys = selector.selectedKeys();
      iterator = connectKeys.iterator();


      SelectionKey key = iterator.next();
      iterator.remove();

      if (key.isValid() && key.isConnectable()) {
        if (!channel.finishConnect()) {
          key.cancel();
          throw new RuntimeException();
        }
//        tls.execute();
        channel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        return true;
      } else if (!key.isValid()) {
        key.cancel();
        return false;
      } else {

      }

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return false;
  }

  private boolean write() {
    try {
      while (true) {

        selector.select();

        Set<SelectionKey> acceptKeys = selector.selectedKeys();
        iterator = acceptKeys.iterator();

        while (iterator.hasNext()) {
          SelectionKey key = iterator.next();
          iterator.remove();

          if (key.isValid() && key.isWritable()) {
//            channel.write(req.toBB());
//            rawRequest.put(req.toBB());
//            tls.wrap();
            log.info(tls.engine.wrap(req.toBB(), encryptedRequest).toString());
            encryptedRequest.flip();
            channel.write(encryptedRequest);
            encryptedRequest.compact();
            return true;
          }
        }
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private HttpResponse read() {
    int nread = 1;
    final HttpResponse resp = new HttpResponse();
    final HttpResponseParser parser = new HttpResponseParser();
    try {
      while (true) {

        selector.select();

        Set<SelectionKey> acceptKeys = selector.selectedKeys();
        iterator = acceptKeys.iterator();

        while (iterator.hasNext()) {
          SelectionKey key = iterator.next();
          iterator.remove();

          if (key.isValid() && key.isReadable()) {
            encryptedResponse.clear();
            while (nread > 0) {
              nread = channel.read(encryptedResponse);
              log.info("here " + nread);
            }
            encryptedResponse.flip();
            log.info(tls.engine.unwrap(encryptedResponse, rawResponse).toString());
            encryptedResponse.compact();

//            parserOk = parser.parse(resp);
//            System.out.println(resp.toString());
            System.out.println("HEREEEEE " + parserOk + "  " + BB.BBtoString(rawResponse));

            System.out.println(BB.BBtoString(rawResponse));
            return resp;
          }
        }
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}

