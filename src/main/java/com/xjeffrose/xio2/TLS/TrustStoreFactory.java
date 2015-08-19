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

package com.xjeffrose.xio2.TLS;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class TrustStoreFactory {

  public static TrustManager[] Generate() {
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

  //TODO: FIX THIS SHIX
  public static KeyStore Generate(TLSConfiguration config) {
    X509Certificate x509Certificate = null;
    try {
      CertificateFactory cf = CertificateFactory.getInstance("X.509");
      x509Certificate = (X509Certificate) cf.generateCertificate(new FileInputStream("caPath"));

      KeyStore ts = KeyStore.getInstance("PKCS12");
      ts.load(null, "".toCharArray());
      ts.setCertificateEntry(x509Certificate.getIssuerX500Principal().getName(), x509Certificate);

      return ts;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
}
