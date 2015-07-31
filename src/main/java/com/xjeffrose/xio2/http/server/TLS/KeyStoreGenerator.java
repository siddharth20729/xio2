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

import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Date;
import sun.security.tools.keytool.CertAndKeyGen;
import sun.security.x509.X500Name;

public class KeyStoreGenerator {

  private static final int keysize = 1024;
  private static final String commonName = "test";
  private static final String organizationalUnit = "IT";
  private static final String organization = "test";
  private static final String city = "test";
  private static final String state = "test";
  private static final String country = "US";
  private static final long validity = 1096; // 3 years
  private static final String alias = "xio2";
  private static final char[] keyPass = "changeit".toCharArray();

  private KeyStoreGenerator() { }

  public static KeyStore Build() throws Exception {
    KeyStore keyStore = KeyStore.getInstance("JKS");
    keyStore.load(null, null);

    CertAndKeyGen keypair = new CertAndKeyGen("RSA", "SHA1WithRSA", null);

    X500Name x500Name =
 new X500Name(commonName, organizationalUnit, organization, city, state, country);

    keypair.generate(keysize);
    PrivateKey privKey = keypair.getPrivateKey();

    X509Certificate[] chain = new X509Certificate[1];

    chain[0] = keypair.getSelfCertificate(x500Name, new Date(), (long) validity * 24 * 60 * 60);

    keyStore.setKeyEntry(alias, privKey, keyPass, chain);

    //keyStore.store(new FileOutputStream(".keystore"), keyPass);

    return keyStore;

  }
}
