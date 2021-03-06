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
package com.xjeffrose.xio2.TLS;

import com.xjeffrose.log.Log;
import com.xjeffrose.xio2.SecureChannelContext;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLParameters;

public class TLS {
  private static final Logger log = Log.getLogger(TLS.class.getName());

  private final TLSConfiguration config;

  private SSLEngineResult sslResult;
  private ByteBuffer decryptedRequest;
  private ByteBuffer rawResponse;
  private SocketChannel channel;
  private boolean selfSignedCert = false;
  private boolean client = false;

  public final SSLEngine engine;
  public ByteBuffer encryptedRequest;
  public ByteBuffer encryptedResponse;

  public TLS(SecureChannelContext ctx) {
    this.channel = ctx.channel;
    this.selfSignedCert = true;

    try {
      this.config = new TLSConfiguration.Builder()
          .fqdn(InetAddress.getLocalHost().getHostName())
          .version("TLSv1")
          .password("selfsignedcert")
          .build();

      this.engine = genEngine();
    } catch (Exception e) {
      log.log(Level.SEVERE, "Failed to create the engine", e);
      System.exit(-1);
      throw new RuntimeException(e);
    }

    ctx.engine = engine;
  }

  public TLS(SecureChannelContext ctx, String privateKeyPath, String x509CrtPath) {
    this.channel = ctx.channel;
    try {
      this.config = new TLSConfiguration.Builder()
          .fqdn(InetAddress.getLocalHost().getHostName())
          .version("TLSv1")
          .privateKeyPath(privateKeyPath)
          .x509CertPath(x509CrtPath)
          .password("passwordsAreGood")
          .build();

      this.engine = genEngine();
    } catch (Exception e) {
      log.log(Level.SEVERE, "Failed to create the engine", e);
      System.exit(-1);
      throw new RuntimeException(e);
    }

    ctx.engine = engine;
  }

  public TLS(SecureChannelContext ctx, TLSConfiguration config) {
    this.channel = ctx.channel;
    this.config = config;

    try {
      this.engine = genEngine();
    } catch (Exception e) {
      log.log(Level.SEVERE, "Failed to create the engine", e);
      System.exit(-1);
      throw new RuntimeException(e);
    }

    ctx.engine = engine;
  }

  public TLS(SocketChannel channel) {
    this.channel = channel;
    this.client = true;
    try {
      this.config = new TLSConfiguration.Builder()
          .fqdn(InetAddress.getLocalHost().getHostName())
          .version("TLSv1.2")
          .password("passwordsAreGood")
          .build();

      this.engine = genEngine();
    } catch (Exception e) {
      log.log(Level.SEVERE, "Failed to create the engine", e);
      throw new RuntimeException(e);
    }

  }

  private SSLEngine genEngine() {
    try {
      KeyStore ks;
      KeyManagerFactory kmf = null;
      if (!client) {
        if (selfSignedCert) {
          ks = KeyStoreFactory.Generate(SelfSignedX509CertGenerator.generate("example.com"), "selfsignedcert");
        } else {
          ks = KeyStoreFactory.Generate(X509CertificateGenerator.generate(config.privateKeyPath, config.x509CertPath), config.password);
        }
        kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, config.passwordCharArray);
      }

      // TODO: Allow for truststore and call truststore path
      // TrustManagers decide whether to allow connections
//      TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
//      tmf.init(ts);

      SSLContext sslCtx = SSLContext.getInstance(config.version);
//      SSLContext sslCtx = SSLContext.getInstance("TLS");

      if (client) {
        sslCtx.init(null, TrustStoreFactory.Generate(), null);
//        sslCtx.init(null, null, null);
      } else {
        sslCtx.init(kmf.getKeyManagers(), null, null);
//      sslCtx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());
      }

      SSLParameters params = new SSLParameters();
      params.setProtocols(new String[]{config.version});
//      params.setProtocols(new String[]{"SSLv3", "TLSv1", "TLSv1.2"});

      final SSLEngine engine = sslCtx.createSSLEngine();
      engine.setSSLParameters(params);
      engine.setNeedClientAuth(false);
      engine.setUseClientMode(client);
      return engine;
    } catch (Exception e) {
      log.log(Level.SEVERE, "Cannot create the engine", e);
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
          rawResponse.flip();
          write();
          rawResponse.compact();
        } else {
          decryptedRequest.flip();
          decryptedRequest.compact();
        }
        break;
      case CLOSED:
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
          channel.close();
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

  public boolean doHandshake() {
    try {
      encryptedRequest = ByteBuffer.allocateDirect(engine.getSession().getPacketBufferSize());
      decryptedRequest = ByteBuffer.allocateDirect(engine.getSession().getApplicationBufferSize());
      rawResponse = ByteBuffer.allocateDirect(engine.getSession().getApplicationBufferSize());
      encryptedResponse = ByteBuffer.allocateDirect(engine.getSession().getPacketBufferSize());

      SSLEngineResult.HandshakeStatus handshakeStatus;

      try {
        engine.beginHandshake();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }

      while (true) {
        handshakeStatus = engine.getHandshakeStatus();
        switch (handshakeStatus) {

          case NEED_TASK:
            Runnable task;
            while ((task = engine.getDelegatedTask()) != null) {
              task.run();
//              new Thread(task).start();
            }
            break;

          case NEED_UNWRAP:
            read();
            encryptedRequest.flip();
            unwrap();
            encryptedRequest.compact();
            break;

          case NEED_WRAP:
            wrap();
            encryptedResponse.flip();
            write();
            encryptedResponse.compact();
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
    } catch (Exception e) {
      log.log(Level.SEVERE, "Failed to complete TLS Handshake", e);
    }
    return false;
  }

  private List<SSLEngineResult> results = new ArrayList<>();

  private void unwrap() {
    try {
      sslResult = engine.unwrap(encryptedRequest, decryptedRequest);
//      log.info("unwrap result" + sslResult);
      results.add(sslResult);
      handleSSLResult(false);
    } catch (Exception e) {
      for (SSLEngineResult r : results) {
        log.info("result: " + r);
      }
      throw new RuntimeException(e);
    }
  }

  private void wrap() {
    try {
      sslResult = engine.wrap(rawResponse, encryptedResponse);
      results.add(sslResult);
      handleSSLResult(true);
    } catch (SSLException e) {
      e.printStackTrace();
    }
  }
}
