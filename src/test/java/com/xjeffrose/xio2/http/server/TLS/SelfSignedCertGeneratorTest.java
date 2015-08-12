package com.xjeffrose.xio2.http.server.TLS;

import org.junit.Test;

import static org.junit.Assert.*;

public class SelfSignedCertGeneratorTest {

  @Test
  public void testGenerate() throws Exception {
    xioCertificate x509cert = SelfSignedCertGenerator.generate("poo.example.com");

    assertEquals("CN=poo.example.com", x509cert.getCert().getIssuerX500Principal().getName());
    assertNotNull(x509cert.getCert().getPublicKey());
  }
}