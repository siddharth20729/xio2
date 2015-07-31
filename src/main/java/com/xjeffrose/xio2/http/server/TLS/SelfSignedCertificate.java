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

package com.xjeffrose.xio2.http.server.TLS;

import com.xjeffrose.log.Log;
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.OutputStream;
//import java.security.KeyPair;
//import java.security.KeyPairGenerator;
//import java.security.NoSuchAlgorithmException;
//import java.security.PrivateKey;
//import java.security.SecureRandom;
//import java.security.cert.CertificateEncodingException;
//import java.security.cert.CertificateException;
//import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.logging.Logger;
//import sun.security.x509.X509CertImpl;

public final class SelfSignedCertificate {
  private static final Logger log = Log.getLogger(SelfSignedCertificate.class.getName());

//  public SelfSignedCertificate(String fqdn, PrivateKey key, X509CertImpl cert) {
//
//  }

  static final Date NOT_BEFORE = new Date(System.currentTimeMillis() - 86400000L * 365);
  static final Date NOT_AFTER = new Date(253402300799000L);

  private SelfSignedCertificate() { }

  //private final File certificate;
  //private final File privateKey;

  //
//    public SelfSignedCertificate() {
//      this("example.com");
//    }
//
//    public SelfSignedCertificate(String fqdn) {
//      // Bypass entrophy collection by using insecure random generator.
//      // We just want to generate it without any delay because it's for testing purposes only.
//      this(fqdn, ThreadLocalInsecureRandom.current(), 1024);
//    }
//
//  public SelfSignedCertificate(String fqdn, SecureRandom random,
// X509CertImpl bits) throws CertificateException {
//    // Generate an RSA key pair.
//    final KeyPair keypair;
//    try {
//      KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
//      keyGen.initialize(1024, null);
//      keypair = keyGen.generateKeyPair();
//    } catch (NoSuchAlgorithmException e) {
//      // Should not reach here because every Java implementation must have RSA key pair generator.
//      throw new Error(e);
//    }
//
//      String[] paths;
//      try {
//        // Try the OpenJDK's proprietary implementation.
//        paths = OpenJdkSelfSignedCertGenerator.generate(fqdn, keypair, random);
//      } catch (Throwable t) {
//        log.fine("Failed to generate a self-signed X.509 certificate using sun.security.x509:");
//
//      }
//
//      certificate = new File(paths[0]);
//      privateKey = new File(paths[1]);
//    }
//
//    /**
//     * Returns the generated X.509 certificate file in PEM format.
//     */
//    public File certificate() {
//      return certificate;
//    }
//
//    /**
//     * Returns the generated RSA private key file in PEM format.
//     */
//    public File privateKey() {
//      return privateKey;
//    }
//
//    /**
//     * Deletes the generated X.509 certificate file and RSA private key file.
//     */
//    public void delete() {
//      safeDelete(certificate);
//      safeDelete(privateKey);
//    }
//
//    static String[] newSelfSignedCertificate(
//        String fqdn, PrivateKey key, X509Certificate cert)
// throws IOException, CertificateEncodingException {
//
////      // Encode the private key into a file.
////      String keyText = "-----BEGIN PRIVATE KEY-----\n" +
////          Base64.encode(Unpooled.wrappedBuffer(key.getEncoded()), true)
// .toString(CharsetUtil.US_ASCII) +
////          "\n-----END PRIVATE KEY-----\n";
//
//      File keyFile = File.createTempFile("keyutil_" + fqdn + '_', ".key");
//      keyFile.deleteOnExit();
//
//      OutputStream keyOut = new FileOutputStream(keyFile);
//      try {
//        keyOut.write(keyText.getBytes(CharsetUtil.US_ASCII));
//        keyOut.close();
//        keyOut = null;
//      } finally {
//        if (keyOut != null) {
//          safeClose(keyFile, keyOut);
//          safeDelete(keyFile);
//        }
//      }
//
////      // Encode the certificate into a CRT file.
////      String certText = "-----BEGIN CERTIFICATE-----\n" +
////          Base64.encode(Unpooled.wrappedBuffer(cert.getEncoded()), true)
// .toString(CharsetUtil.US_ASCII) +
////          "\n-----END CERTIFICATE-----\n";
//
//      File certFile = File.createTempFile("keyutil_" + fqdn + '_', ".crt");
//      certFile.deleteOnExit();
//
//      OutputStream certOut = new FileOutputStream(certFile);
//      try {
//        certOut.write(certText.getBytes(CharsetUtil.US_ASCII));
//        certOut.close();
//        certOut = null;
//      } finally {
//        if (certOut != null) {
//          safeClose(certFile, certOut);
//          safeDelete(certFile);
//          safeDelete(keyFile);
//        }
//      }
//
//      return new String[] { certFile.getPath(), keyFile.getPath() };
//    }
//
//    private static void safeDelete(File certFile) {
//      if (!certFile.delete()) {
//        log.warning("Failed to delete a file");
//      }
//    }
//
//    private static void safeClose(File keyFile, OutputStream keyOut) {
//      try {
//        keyOut.close();
//      } catch (IOException e) {
//        log.warning("Failed to close a file: " + keyFile);
//      }
//    }
//  }
}

