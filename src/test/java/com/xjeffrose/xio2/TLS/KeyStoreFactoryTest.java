package com.xjeffrose.xio2.TLS;

import java.security.KeyStore;
import org.junit.Test;

import static org.junit.Assert.*;

public class KeyStoreFactoryTest {

  @Test
  public void testGenerate() throws Exception {

    KeyStore ks = KeyStoreFactory.Generate(SelfSignedCertGenerator.generate("example.com"), "selfsignedcert");

    assertTrue(ks.containsAlias("example.com"));
  }
}