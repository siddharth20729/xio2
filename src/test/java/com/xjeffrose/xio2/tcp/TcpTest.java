package com.xjeffrose.xio2.tcp;

import com.xjeffrose.xio2.Server;
import com.xjeffrose.xio2.tcp.client.Client;
import com.xjeffrose.xio2.tcp.server.TcpHandler;
import com.xjeffrose.xio2.tcp.server.TcpRequest;
import com.xjeffrose.xio2.tcp.server.TcpResponse;
import com.xjeffrose.xio2.tcp.server.TcpService;
import com.xjeffrose.xio2.util.BB;
import org.junit.Test;

import static org.junit.Assert.*;

public class TcpTest {

  @Test
  public void testNewServer() throws Exception {

    TcpHandler tcpHandler = new TcpHandler();
    TcpService tcpService = new TestTcpService();
    tcpHandler.addService(tcpService);

    Server s = Tcp.newServer();
    s.bind(10001, tcpHandler);
    s.serve();

    Client c = Tcp.newClient("localhost:10001");
    TcpResponse resp = c.call(new TcpRequest("Testing"));
    assertEquals(BB.BBtoString(resp.inputBuffer), "This is a tcp test");

  }

  @Test
  public void testNewTLSServer() throws Exception {
    TcpHandler tcpHandler = new TcpHandler();
    TcpService tcpService = new TestTcpService();
    tcpHandler.addService(tcpService);

    Server s = Tcp.newTLSServer();
    s.bind(10002, tcpHandler);
    s.serve();

    Client c = Tcp.newTLSClient("localhost:10002");
    TcpResponse resp = c.call(new TcpRequest("Testing"));
    assertEquals(BB.BBtoString(resp.inputBuffer), "This is a tcp test");

  }
}