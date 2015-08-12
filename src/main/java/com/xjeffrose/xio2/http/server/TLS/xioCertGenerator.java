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
package com.xjeffrose.xio2.http.server.TLS;

import com.xjeffrose.log.Log;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.util.Base64;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import sun.security.util.DerInputStream;
import sun.security.util.DerValue;
import sun.security.x509.X509CertImpl;

public final class xioCertGenerator {
  private static final Logger log = Log.getLogger(xioCertGenerator.class.getName());

  private xioCertGenerator() { }

  public static Map<String, Key> GenerateKeyFromFile(String path) {
    try {

      String rawKeyString = new String(Files.readAllBytes(Paths.get(path)));

      String _rawKeyString = rawKeyString
      .replace("-----BEGIN RSA PRIVATE KEY-----\n", "");

      String __rawKeyString = _rawKeyString
          .replace("-----END RSA PRIVATE KEY-----\n", "");

      String ___rawKeyString = __rawKeyString
          .replace("\n", "");

      // Base64 decode the data
      Base64.Decoder b64decoder = Base64.getDecoder();
      byte[] encoded = b64decoder.decode(___rawKeyString);

      // PKCS8 decode the encoded RSA private key
//      PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
      DerInputStream derReader = new DerInputStream(encoded);

      DerValue[] seq = derReader.getSequence(0);

      if (seq.length < 9) {
        throw new GeneralSecurityException("Could not parse a PKCS1 private key.");
      }

      // skip version seq[0];
      BigInteger modulus = seq[1].getBigInteger();
      BigInteger publicExp = seq[2].getBigInteger();
      BigInteger privateExp = seq[3].getBigInteger();
      BigInteger prime1 = seq[4].getBigInteger();
      BigInteger prime2 = seq[5].getBigInteger();
      BigInteger exp1 = seq[6].getBigInteger();
      BigInteger exp2 = seq[7].getBigInteger();
      BigInteger crtCoef = seq[8].getBigInteger();

      RSAPrivateCrtKeySpec keySpec = new RSAPrivateCrtKeySpec(modulus, publicExp, privateExp, prime1, prime2, exp1, exp2, crtCoef);

      KeyFactory kf = KeyFactory.getInstance("RSA");
      PrivateKey privateKey = kf.generatePrivate(keySpec);
      //PublicKey publicKey = kf.generatePublic(keySpec);

      Map<String, Key> keyPair = new HashMap<>();

      keyPair.put("privateKey", privateKey);
      //keyPair.put("publicKey", publicKey);

      return keyPair;
    } catch (Exception e) {
      return null;
    }
 }

  public static xioCertificate generate(String keyPath, String certPath) throws Exception {

    Map<String, Key> keyPair = GenerateKeyFromFile(keyPath);;
    PrivateKey privateKey =(PrivateKey) keyPair.get("privateKey");
    //PublicKey publicKey = (PublicKey) keyPair.get("publicKey");

    // Sign the cert to identify the algorithm that's used.
    CertificateFactory cf = CertificateFactory.getInstance("X.509");
    X509Certificate x509Certificate = (X509Certificate) cf.generateCertificate(new FileInputStream(certPath));
    X509CertImpl cert = (X509CertImpl) x509Certificate;

//    cert.sign(privateKey, "SHA1withRSA");
    //cert.verify(publicKey);

    return new xioCertificate(cert.getIssuerX500Principal().getName(), privateKey, cert);
  }
}
