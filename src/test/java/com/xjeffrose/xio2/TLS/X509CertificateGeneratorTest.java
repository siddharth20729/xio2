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

import java.security.PrivateKey;
import java.security.PublicKey;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class X509CertificateGeneratorTest {

  @Before
  public void setUp() throws Exception {

  }

  @After
  public void tearDown() throws Exception {

  }

  @Test
  public void testParsePrivateKeyFromPEM() throws Exception {
    PrivateKey privateKey = X509CertificateGenerator.parsePrivateKeyFromPEM("src/test/resources/privateKey.pem");
    //assert
  }

  @Test
  public void testParseDERKeySpec() throws Exception {

  }

  @Test
  public void testParsePublicKeyFromPEM() throws Exception {
    PublicKey publicKey = X509CertificateGenerator.parsePublicKeyFromPEM("src/test/resources/privateKey.pem");

  }

  @Test
  public void testGenerate() throws Exception {
    X509Certificate certificate = X509CertificateGenerator.generate("src/test/resources/privateKey.pem", "src/test/resources/cert.pem");
    assertEquals("CN=xio2.example.com,OU=dev,O=xio2,L=Chicago,ST=IL,C=US", certificate.getCert().getIssuerX500Principal().getName());

  }
}