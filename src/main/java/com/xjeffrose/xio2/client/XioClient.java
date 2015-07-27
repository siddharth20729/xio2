package com.xjeffrose.xio2.client;

import com.xjeffrose.log.Log;
import com.xjeffrose.xio2.http.HttpObject;
import com.xjeffrose.xio2.http.HttpRequest;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;

public class XioClient {
  private static final Logger log = Log.getLogger(XioClient.class.getName());

  private SocketChannel channel;
  private Selector selector;
  private HttpRequest req;
  private boolean parserOk;
  private Iterator<SelectionKey> iterator;
  private ClientTLS tls = null;

  public boolean ssl = false;

  XioClient() { }

  public void ssl(boolean b) {
    this.ssl = b;
  }

  public void connect(String host, int port) {
    try {
      channel = SocketChannel.open();
      channel.configureBlocking(false);
      channel.connect(new InetSocketAddress(host, port));
      if (ssl) {
        tls = new ClientTLS(channel);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public HttpObject get(HttpRequest req) {
    this.req = req;

    if (ssl) {
      if (checkConnect()) {
        if (tls.execute()) {
          if (write()) {
            return read();
          }
        }
      }
    } else {
      if (checkConnect()) {
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
            if (ssl) {
              tls.engine.wrap(req.toBB(), tls.encryptedRequest);
              tls.encryptedRequest.flip();
              channel.write(tls.encryptedRequest);
              tls.encryptedRequest.compact();
              return true;
            } else {
              channel.write(req.toBB());
              return true;
            }
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
            if (ssl) {
              tls.encryptedResponse.clear();

              while (nread > 0) {
                nread = channel.read(tls.encryptedResponse);
              }
              tls.encryptedResponse.flip();
              tls.engine.unwrap(tls.encryptedResponse, resp.inputBuffer);
              tls.encryptedResponse.compact();
            } else {
              while (nread > 0) {
                nread = channel.read(resp.inputBuffer);
              }
            }
            parserOk = parser.parse(resp);
            if (parserOk) {
              return resp;
            } else {
              return null;
            }
          }
        }
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}

