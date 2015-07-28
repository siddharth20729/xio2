package com.xjeffrose.xio2.http.server.TLS;

import java.security.KeyStore;
import org.junit.Test;

import static org.junit.Assert.*;

public class KeyStoreGeneratorTest {

  @Test
  public void testBuild() throws Exception {

    KeyStore ks = KeyStoreGenerator.Build();

    assertEquals(ks.getProvider().toString(), "SUN version 1.8");
    assertTrue(ks.isKeyEntry("xio2"));

  }
}