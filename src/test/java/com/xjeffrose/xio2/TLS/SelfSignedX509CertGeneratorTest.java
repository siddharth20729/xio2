package com.xjeffrose.xio2.TLS;

import org.junit.Test;

import static org.junit.Assert.*;

public class SelfSignedX509CertGeneratorTest {

  @Test
  public void testGenerate() throws Exception {
    X509Certificate x509cert = SelfSignedX509CertGenerator.generate("poo.example.com");

    assertEquals("CN=poo.example.com", x509cert.getCert().getIssuerX500Principal().getName());
    assertNotNull(x509cert.getCert().getPublicKey());
  }
}