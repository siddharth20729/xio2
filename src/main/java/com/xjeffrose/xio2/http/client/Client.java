/*
 * Copyright (C) 2015 Jeff Rose
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.xjeffrose.xio2.http.client;

import com.xjeffrose.log.Log;
import com.xjeffrose.xio2.http.Http;
import com.xjeffrose.xio2.http.HttpRequest;
import com.xjeffrose.xio2.http.HttpResponse;
import com.xjeffrose.xio2.http.HttpResponseParser;
import com.xjeffrose.xio2.http.client.LoadBalancerStrategies.LoadBalancingStrategy;
import com.xjeffrose.xio2.http.client.LoadBalancerStrategies.NullLoadBalancer;
import com.xjeffrose.xio2.http.client.LoadBalancerStrategies.RoundRobinLoadBalancer;
import com.xjeffrose.xio2.TLS.TLS;
import com.xjeffrose.xio2.ChannelContext;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javax.net.ssl.SSLEngineResult;

//TODO: Make client higher a higher level class, maybe?
public class Client {
  private static final Logger log = Log.getLogger(Client.class.getName());

  private Selector selector;
  private HttpRequest req;
  private TLS tls = null;
  public LoadBalancer lb = LoadBalancer.NullLoadBalancer;
  private LoadBalancingStrategy lbs;
  public boolean _tls = false;
  private String serverString;

  private boolean needConnect = false;
  public Client() {
    needConnect = true;
  }

  public Client(String serverString) {
    this.serverString = serverString;
    connect();
  }

  public void tls(boolean b) {
    this._tls = b;
  }

  public enum LoadBalancer {
    NullLoadBalancer,
    RoundRobin,
    FailFast,
    PullBased
  }

  private SocketChannel getChannel(InetSocketAddress addr) {
    try {
      SocketChannel channel = SocketChannel.open();
      channel.configureBlocking(false);
      channel.connect(addr);
      if (_tls) {
        tls = new TLS(channel);
      }
      return channel;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void connect() {
    String[] serversArray = serverString.split(",");
    Map<String, Integer> hostPortMap = new HashMap<>();

    for (String aServersArray : serversArray) {
      String[] temp = aServersArray.split(":");
      hostPortMap.put(temp[0], Integer.valueOf(temp[1]));
    }

    if (hostPortMap.size() == 0) {
      throw new RuntimeException("Bad Server address format");
    }

    if (hostPortMap.size() == 1) {
      lb = LoadBalancer.NullLoadBalancer;
    }

    if (hostPortMap.size() < 1) {
      lb = LoadBalancer.RoundRobin;
    }

    switch (lb) {
      case NullLoadBalancer:
        String n_key = hostPortMap.keySet().stream().findFirst().get();
        lbs = new NullLoadBalancer(new InetSocketAddress(n_key, hostPortMap.get(n_key)));
        break;
      case RoundRobin:
        List<InetSocketAddress> addrList = new ArrayList<>();
        for (String r_key : hostPortMap.keySet()) {
          addrList.add(new InetSocketAddress(r_key, hostPortMap.get(r_key)));
        }
        lbs = new RoundRobinLoadBalancer(addrList);
        break;
      case FailFast:
        break;
      case PullBased:
        break;
    }
  }

  public HttpResponse call(HttpRequest req) {
    this.req = req;

    if (needConnect) {
      serverString = req.getServerString();
      tls(req.isTls());
      connect();
    }

    return execute(getChannel(lbs.nextAddress()));
  };

  //PROXY!!!!!!
  public void proxy(ChannelContext serverCtx) {

    HttpRequest req = (HttpRequest) serverCtx.req;
    HttpResponse response = call(req);

    serverCtx.write(response.toBB());
  }

  private HttpResponse execute(SocketChannel channel) {
    if (_tls) {
      if (checkConnect(channel)) {
        if (tls.doHandshake()) {
          if (write(channel)) {
            return read(channel);
          }
        }
      }
    } else {
      if (checkConnect(channel)) {
        if (write(channel)) {
          return read(channel);
        }
      }
    }
    return null;
  }

  private boolean checkConnect(SocketChannel channel) {
    try {
      selector = Selector.open();

      channel.register(selector, SelectionKey.OP_CONNECT);
      selector.select();


      Set<SelectionKey> connectKeys = selector.selectedKeys();
      Iterator<SelectionKey> iterator = connectKeys.iterator();


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
        //TODO: Figure out what is supposed to be here (-_-)
      }

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return false;
  }

  private boolean write(SocketChannel channel) {
    try {
      while (true) {

        selector.select();

        Set<SelectionKey> acceptKeys = selector.selectedKeys();
        Iterator<SelectionKey> iterator = acceptKeys.iterator();

        while (iterator.hasNext()) {
          SelectionKey key = iterator.next();
          iterator.remove();

          if (key.isValid() && key.isWritable()) {
            if (_tls) {
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

  private HttpResponse read(SocketChannel channel) {
    int nread = 1;
    int totalBytes = 0;
    final HttpResponse resp = new HttpResponse();
    final HttpResponseParser parser = new HttpResponseParser();
    try {
      while (true) {

        selector.select();

        Set<SelectionKey> acceptKeys = selector.selectedKeys();
        Iterator<SelectionKey> iterator = acceptKeys.iterator();

        while (iterator.hasNext()) {
          SelectionKey key = iterator.next();
          iterator.remove();

          if (key.isValid() && key.isReadable()) {
            if (_tls) {
              log.info("top of the loop");
              log.info("encryptedResponse " + tls.encryptedResponse);
              log.info("inputBufffer " + resp.inputBuffer);
//              tls.encryptedResponse.clear();

              nread = 1;
              while (nread > 0) {
                nread = channel.read(tls.encryptedResponse);
                log.info("bytes read " + nread);
                totalBytes += nread;
              }
              log.info("Total Bytes read " + totalBytes);
              log.info("done reading");
              log.info("encryptedResponse " + tls.encryptedResponse);
              log.info("inputBufffer " + resp.inputBuffer);
              tls.encryptedResponse.flip();
              log.info("post flip");
              log.info("encryptedResponse " + tls.encryptedResponse);
              log.info("inputBufffer " + resp.inputBuffer);
              SSLEngineResult result;
              do {
                ByteBuffer slice = tls.encryptedResponse.slice();
                result = tls.engine.unwrap(slice, resp.inputBuffer);
                tls.encryptedResponse.position(tls.encryptedResponse.position() + result.bytesConsumed());
                log.info("post unwrap " + result.toString());
                log.info("encryptedResponse " + tls.encryptedResponse);
                log.info("inputBufffer " + resp.inputBuffer);
              } while (result.getStatus() == SSLEngineResult.Status.OK);
              tls.encryptedResponse.compact();
              log.info("post compact");
              log.info("encryptedResponse " + tls.encryptedResponse);
              log.info("inputBufffer " + resp.inputBuffer);
              /*
              if (result.getStatus() == SSLEngineResult.Status.BUFFER_UNDERFLOW) {
                tls.encryptedResponse.flip();
                log.info("post underflow flip");
                log.info("encryptedResponse " + tls.encryptedResponse);
                log.info("inputBufffer " + resp.inputBuffer);
              }
              */
            } else {
              while (nread > 0) {
                nread = channel.read(resp.inputBuffer);
              }
            }
            //TODO: Decouple HTTP
            boolean parserOk = parser.parse(resp);
            log.info("parserOk " + parserOk);
            if (parserOk) {
              cleanup(channel);
              return resp;
            } else if (nread == -1) {
              log.severe("Server disconnected with data remaining");
              cleanup(channel);
              return HttpResponse
                  .DefaultResponse(Http.Version.HTTP1_1, Http.Status.INTERNAL_SERVER_ERROR);
            } else {
              switch (parser.status) {
                case BUFFER_UNDERFLOW:
                  //PUMP THE BRAKES SPEED RACER
                  log.info("ARE YOU FUCKING KIDDING ME?");
                  //nread = channel.read(resp.inputBuffer);
                  //Thread.sleep(1);
                  break;
                case FINISHED:
                  break;
              }
            }
          }
        }
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void cleanup(SocketChannel channel) {
    try {
      channel.close();
      selector.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}

