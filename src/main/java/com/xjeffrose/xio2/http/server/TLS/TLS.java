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
package com.xjeffrose.xio2.http.server.TLS;

import com.xjeffrose.log.Log;
import com.xjeffrose.xio2.ChannelContext;
import java.io.FileInputStream;
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


public class TLS {
  private static final Logger log = Log.getLogger(TLS.class.getName());

  private SSLContext sslCtx;
  private SSLEngineResult sslResult;
  private ByteBuffer encryptedRequest;
  private ByteBuffer decryptedRequest;
  private ByteBuffer rawResponse;
  private ByteBuffer encryptedResponse;
  private char[] passphrase = "changeit".toCharArray();
  private SSLEngineResult.HandshakeStatus handshakeStatus;
  private SocketChannel channel;
  private String path;
  private String version = "TLSv1.2";
  private boolean selfSignedCert = true;

  public SSLEngine engine;

  public TLS(ChannelContext ctx) {
    this.channel = ctx.channel;

    genEngine();
    ctx.engine = engine;
  }

  public TLS(ChannelContext ctx, TLSConfiguration config) {
    this.channel = ctx.channel;

    genEngine();
    ctx.engine = engine;
  }

  public TLS(ChannelContext ctx, String version, boolean selfSignedCert) {
    this.channel = ctx.channel;
    this.version = version;
    this.selfSignedCert = selfSignedCert;

    genEngine();
    ctx.engine = engine;
  }

  public TLS(ChannelContext ctx, String version, String path, String passphrase) {
    this.channel = ctx.channel;
    this.version = version;
    this.path = path;
    this.passphrase = passphrase.toCharArray();
    this.selfSignedCert = false;

    genEngine();
    ctx.engine = engine;
  }

  private void genEngine() {
    try {
      // TODO: Get keystore path
      KeyStore ks;
      if (selfSignedCert) {
        ks = KeyStoreGenerator.Build();
      } else {
        ks =  KeyStore.getInstance("PKCS12");
        ks.load(new FileInputStream(path), passphrase);
      }

      KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
      kmf.init(ks, passphrase);

      // TODO: Allow for truststore and call truststore path
//      CertificateFactory cf = CertificateFactory.getInstance("X.509");
//      X509Certificate x509Certificate =
//        (X509Certificate) cf.generateCertificate(new FileInputStream("/path/to/ca"));
//      KeyStore ts = KeyStore.getInstance("PKCS12");
//      ts.load(null);
//      ts.setCertificateEntry("alias", x509Certificate);

      // TrustManagers decide whether to allow connections
//      TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
//      tmf.init(ts);

      sslCtx = SSLContext.getInstance(version);
      sslCtx.init(kmf.getKeyManagers(), null, new SecureRandom());
//      sslCtx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());

      SSLParameters params = new SSLParameters();
      params.setProtocols(new String[]{version});

      engine = sslCtx.createSSLEngine();
      engine.setSSLParameters(params);
      engine.setNeedClientAuth(false);
      engine.setUseClientMode(false);

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void handleSSLResult(boolean network) {

    switch (sslResult.getStatus()) {
      case OK:
//        System.out.println("OKKKKKK");
        break;
      case BUFFER_UNDERFLOW:
        read();
        break;
      case BUFFER_OVERFLOW:
        if (network) {
          rawResponse.flip();
          write();
          rawResponse.compact();
        } else {
          decryptedRequest.flip();
          decryptedRequest.compact();
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
        nread = channel.read(encryptedRequest);
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
      channel.write(encryptedResponse);
      handleSSLResult(true);
    } catch (Exception e) {
      log.severe("Pooo face" + e);
    }
  }

  public void doHandshake() {
//    ctx.engine = engine;
    encryptedRequest = ByteBuffer.allocateDirect(engine.getSession().getPacketBufferSize());
    decryptedRequest = ByteBuffer.allocateDirect(engine.getSession().getApplicationBufferSize());
    rawResponse = ByteBuffer.allocateDirect(engine.getSession().getApplicationBufferSize());
    encryptedResponse = ByteBuffer.allocateDirect(engine.getSession().getPacketBufferSize());

    try {
      engine.beginHandshake();
      handshakeStatus = engine.getHandshakeStatus();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    while (handshakeStatus != SSLEngineResult.HandshakeStatus.FINISHED
        && handshakeStatus != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING) {
      handshakeStatus = engine.getHandshakeStatus();
      switch (handshakeStatus) {

        case NEED_TASK:
          Runnable task;
          while ((task = engine.getDelegatedTask()) != null) {
            new Thread(task).start();
          }
          handshakeStatus = engine.getHandshakeStatus();
          break;

        case NEED_UNWRAP:
          read();
          encryptedRequest.flip();
          unwrap();
          encryptedRequest.compact();
          handshakeStatus = engine.getHandshakeStatus();
          break;

        case NEED_WRAP:
          wrap();
          encryptedResponse.flip();
          write();
          encryptedResponse.compact();
          handshakeStatus = engine.getHandshakeStatus();
          break;

        case FINISHED:
          log.info("Successful TLS Handshake");
//          ctx.handshakeOK = true;
          break;
      }
    }
  }

  private void unwrap() {
    try {
      sslResult = engine.unwrap(encryptedRequest, decryptedRequest);
      handleSSLResult(false);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void wrap() {
    try {
      sslResult = engine.wrap(rawResponse, encryptedResponse);
      handleSSLResult(true);
    } catch (SSLException e) {
      e.printStackTrace();
    }
  }
}
