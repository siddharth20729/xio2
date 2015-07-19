package com.xjeffrose.xio2.services;

import com.xjeffrose.log.Log;

import com.xjeffrose.xio2.server.Service;
import java.util.logging.Logger;

class PooService extends Service {
  private static final Logger log = Log.getLogger(PooService.class.getName());

  PooService() {
  }

  public void handleGet() {
    //resp.ok();
    //resp.body("Hello from /poo\n");
  }

  public void handlePost() {
    //resp.ok();
    //resp.body(req.body.toString() + "\n"); //TODO: Should accept ByteBuffers for Proxy Functionality
  }

}
