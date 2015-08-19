package com.xjeffrose.xio2.tcp.client;

import com.xjeffrose.log.Log;
import com.xjeffrose.xio2.ChannelContext;
import com.xjeffrose.xio2.http.client.LoadBalancerStrategies.LoadBalancingStrategy;
import com.xjeffrose.xio2.http.client.LoadBalancerStrategies.NullLoadBalancer;
import com.xjeffrose.xio2.http.client.LoadBalancerStrategies.RoundRobinLoadBalancer;
import com.xjeffrose.xio2.TLS.TLS;
import com.xjeffrose.xio2.tcp.server.TcpRequest;
import com.xjeffrose.xio2.tcp.server.TcpResponse;
import java.io.IOException;
import java.net.InetSocketAddress;
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

//TODO: Make client higher a higher level class, maybe?
public class Client {
  private static final Logger log = Log.getLogger(Client.class.getName());

  private Selector selector;
  private TcpRequest req;
  private TLS tls = null;
  public LoadBalancer lb = LoadBalancer.NullLoadBalancer;
  private LoadBalancingStrategy lbs;
  public boolean _tls = false;
  private String serverString;

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
    PullBased;
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

  public TcpResponse call(TcpRequest req) {
    this.req = req;

    return execute(getChannel(lbs.nextAddress()));
  }

  public void proxy(ChannelContext serverCtx) {

    TcpRequest req = (TcpRequest) serverCtx.req;
    TcpResponse response = call(req);

    serverCtx.write(response.toBB());
  }

  private TcpResponse execute(SocketChannel channel) {
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

  private TcpResponse read(SocketChannel channel) {
    int nread = 1;
    final TcpResponse resp = new TcpResponse();
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
            Thread.sleep(1);
            cleanup(channel);
            if (nread == -1) {
//              cleanup(channel);
//              return null; //Need to return some error of some sort, I guess?
            }
            return resp;
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