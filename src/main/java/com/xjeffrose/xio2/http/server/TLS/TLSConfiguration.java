package com.xjeffrose.xio2.http.server.TLS;

public class TLSConfiguration {

  public String version = null;
  public String keystorePath = null;
  public char[] keystorePassphrase = null;
  public String trusttorePath = null;
  public char[] truststorePassphrase = null;

  public boolean selfSignedCert = true;

  TLSConfiguration() { }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    public String version = null;
    public String keystorePath = null;
    public String keystorePassphrase = null;
    public String truststorePath = null;
    public String truststorePassphrase = null;

    public boolean selfSignedCert = true;

    public Builder version(String version) {
      this.version = version;
      return this;
    }

    public Builder keystorePath(String keystorePath) {
      this.keystorePath = keystorePath;
      return this;
    }

    public Builder keystorePassphrase(String keystorePassphrase) {
      this.keystorePassphrase = keystorePassphrase;
      return this;
    }

    public Builder truststorePath(String truststorePath) {
      this.truststorePath = truststorePath;
      return this;
    }

    public Builder truststorePassphrase(String truststorePassphrase) {
      this.truststorePassphrase = truststorePassphrase;
      return this;
    }

    public Builder selfSignedCert(boolean b) {
      this.selfSignedCert = b;
      return this;
    }

    public TLSConfiguration build() {
      TLSConfiguration config = new TLSConfiguration();
      config.version = version;
      config.keystorePath = keystorePath;
      config.keystorePassphrase = keystorePassphrase.toCharArray();
      config.trusttorePath = truststorePath;
      config.truststorePassphrase = truststorePassphrase.toCharArray();
      config.selfSignedCert = selfSignedCert;

      return config;
    }

  }
}
