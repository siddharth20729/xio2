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
 *
 */

package com.xjeffrose.xio2.http.client.TLS;

import com.xjeffrose.log.Log;
import com.xjeffrose.xio2.http.server.TLS.KeyStoreGenerator;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.logging.Logger;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class TLS {
  private static final Logger log = Log.getLogger(TLS.class.getName());

  private SSLContext sslCtx;
  private SSLEngineResult sslResult;

  private ByteBuffer rawRequest;
  public ByteBuffer encryptedRequest;

  public ByteBuffer encryptedResponse;
  private ByteBuffer rawResponse;

  private char[] passphrase = "changeit".toCharArray();
  private SSLEngineResult.HandshakeStatus handshakeStatus;
  private SocketChannel channel;

  public SSLEngine engine;

  public TLS(SocketChannel channel) {
    this.channel = channel;
    genEngine();
    this.rawRequest = ByteBuffer.allocateDirect(12);
    this.encryptedRequest = ByteBuffer.allocateDirect(engine.getSession().getPacketBufferSize());
    this.encryptedResponse = ByteBuffer.allocateDirect(engine.getSession().getPacketBufferSize());
    this.rawResponse = ByteBuffer.allocateDirect(engine.getSession().getApplicationBufferSize());
    try {
      engine.beginHandshake();
    } catch (SSLException e) {
      e.printStackTrace();
    }
  }

  private TrustManager[] getTrustAllCerts() {
    TrustManager[] trustAllCerts = new TrustManager[]{
        new X509TrustManager() {
          public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
          }

          public void checkClientTrusted(
              java.security.cert.X509Certificate[] certs, String authType) {
          }

          public void checkServerTrusted(
              java.security.cert.X509Certificate[] certs, String authType) {
          }
        }
    };
    return trustAllCerts;
  }

  private void genEngine() {
    try {
      KeyStore ks = KeyStoreGenerator.Build();
      KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
      kmf.init(ks, passphrase);

      sslCtx = SSLContext.getInstance("TLSv1.2");
      sslCtx.init(kmf.getKeyManagers(), getTrustAllCerts(), new SecureRandom());

      SSLParameters params = new SSLParameters();
      params.setProtocols(new String[]{"TLSv1.2"});

      engine = sslCtx.createSSLEngine();
      engine.setSSLParameters(params);
      engine.setUseClientMode(true);

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void handleSSLResult(boolean network) {

    switch (sslResult.getStatus()) {
      case OK:
        break;
      case BUFFER_UNDERFLOW:
        read();
        break;
      case BUFFER_OVERFLOW:
        if (network) {
          encryptedRequest.flip();
          write();
          encryptedRequest.compact();
        } else {
          rawResponse.flip();
          rawResponse.compact();
        }
        break;
      case CLOSED:
        System.out.print("Closed");
        break;
    }
  }

  private void read() {
    int nread = 1;
    while (nread > 0) {
      try {
        nread = channel.read(encryptedResponse);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
      if (nread == -1) {
        try {
          log.severe("Fool tried to close the channel, yo");
          //ctx.channel.close();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
  }

  private void write() {
    try {
      channel.write(encryptedRequest);
      handleSSLResult(true);
    } catch (Exception e) {
      log.severe("Pooo face" + e);
    }
  }

  public boolean execute() {
    try {
      engine.beginHandshake();
    } catch (SSLException e) {
      e.printStackTrace();
    }
    while (true) {
      handshakeStatus = engine.getHandshakeStatus();
      switch (handshakeStatus) {
        case NEED_TASK:

          Runnable task;
          while ((task = engine.getDelegatedTask()) != null) {
            task.run();
          }
          break;

        case NEED_UNWRAP:
          read();
          encryptedResponse.flip();
          unwrap();
          encryptedResponse.compact();
          break;

        case NEED_WRAP:
          wrap();
          encryptedRequest.flip();
          write();
          encryptedRequest.compact();
          break;

        case FINISHED:
          return true;

        case NOT_HANDSHAKING:
          return true;

        default:
          log.info("got rando status " + handshakeStatus);
          break;
      }
    }
  }

  public void unwrap() {
    try {
      sslResult = engine.unwrap(encryptedResponse, rawResponse);
      handleSSLResult(false);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void wrap() {
    try {
      sslResult = engine.wrap(rawRequest, encryptedRequest);
      handleSSLResult(true);
    } catch (SSLException e) {
      e.printStackTrace();
    }
  }
}
