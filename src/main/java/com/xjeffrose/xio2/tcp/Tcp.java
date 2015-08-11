package com.xjeffrose.xio2.tcp;

import com.xjeffrose.xio2.Server;
import com.xjeffrose.xio2.tcp.client.Client;

public class Tcp {

  public static Server newServer() {
    Server s = new Server();
    return s;
  }

  public static Server newTLSServer() {
    Server s = new Server();
    String tlsVersion = "tlsv1.2";
    boolean selfSignedCert = true;
    s.tls(true);
    return s;
  }

  public static Client newClient(String hostString) {
    Client c = new Client(hostString);
    return c;
  }

  public static Client newTLSClient(String hostString) {
    Client c = new Client(hostString);
    String tlsVersion = "tlsv1.2";
    c.tls(true);
    return c;
  }

}
