package com.xjeffrose.xio2;

import com.xjeffrose.xio2.server.Server;

public class HumorChris {
  public static void main(String[] args) {
    Server s = new Server();

    s.ssl(true);
    s.serve(9000);
  }
}
