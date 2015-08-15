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
package com.xjeffrose.xio2.http.server;

import com.xjeffrose.xio2.ChannelContext;
import com.xjeffrose.xio2.SecureChannelContext;

import com.xjeffrose.xio2.TLS.TLS;
import java.nio.channels.SocketChannel;

public class HttpsHandler extends HttpHandler {

  private String keyPath = null;
  private String x509CertPath = null;
  private boolean selfSignedCert = false;

  public HttpsHandler() {

    this.selfSignedCert = true;
  }

  public HttpsHandler(String keyPath, String x509CertPath) {
    this.keyPath = keyPath;
    this.x509CertPath = x509CertPath;
  }

  public ChannelContext buildChannelContext(SocketChannel channel) {
    return new SecureChannelContext(channel, this);
  }

  @Override
  public void secureContext(SecureChannelContext secureChannelContext) {
    final TLS tls = buildTLS(secureChannelContext);
    tls.doHandshake();
  }

  public TLS buildTLS(SecureChannelContext secureChannelContext) {
    if (selfSignedCert) {
      return new TLS(secureChannelContext);
    } else {
      return new TLS(secureChannelContext, keyPath, x509CertPath);
    }
  }
}
