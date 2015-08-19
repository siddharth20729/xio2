/*
 *  Copyright (C) 2015 Jeff Rose
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

package com.xjeffrose.xio2.http.server;

import com.xjeffrose.xio2.ChannelContext;
import com.xjeffrose.xio2.SecureChannelContext;
import com.xjeffrose.xio2.TLS.TLS;
import com.xjeffrose.xio2.TLS.TLSConfiguration;
import java.nio.channels.SocketChannel;

public class SecureFileHandler extends FileHandler {

  private TLSConfiguration config;
  private String keyPath = null;
  private String x509CertPath = null;
  private boolean selfSignedCert = false;

  public SecureFileHandler() {

    this.selfSignedCert = true;
  }

  public SecureFileHandler(String keyPath, String x509CertPath) {
    this.keyPath = keyPath;
    this.x509CertPath = x509CertPath;
  }

  public SecureFileHandler(TLSConfiguration config) {
    this.config = config;
  }

  public ChannelContext buildChannelContext(SocketChannel channel) {
    return new SecureChannelContext(channel, this);
  }

  @Override
  public void secureContext(SecureChannelContext secureChannelContext) {
    final TLS tls;
    if (selfSignedCert) {
      tls = new TLS(secureChannelContext);
    } else {
      if (this.config != null) {
        tls =  new TLS(secureChannelContext, config);
      } else {
        tls =  new TLS(secureChannelContext, keyPath, x509CertPath);
      }
    }
    tls.doHandshake();
  }
}
