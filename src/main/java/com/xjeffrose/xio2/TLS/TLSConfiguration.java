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

public class TLSConfiguration {
  public String fqdn = null;
  public String version = null;
  public String keystorePath = null;
  public char[] keystorePassphrase = null;
  public String truststorePath = null;
  public char[] truststorePassphrase = null;

  TLSConfiguration() { }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    public String fqdn = null;
    public String version = null;
    public String keystorePath = null;
    public String keystorePassphrase = null;
    public String truststorePath = null;
    public String truststorePassphrase = null;

    public Builder fqdn(String fqdn) {
      this.fqdn = fqdn;
      return this;
    }

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

    public TLSConfiguration build() {
      TLSConfiguration config = new TLSConfiguration();
      config.fqdn = fqdn;
      config.version = version;
      config.keystorePath = keystorePath;
      config.keystorePassphrase = keystorePassphrase.toCharArray();
      config.truststorePath = truststorePath;
      config.truststorePassphrase = truststorePassphrase.toCharArray();

      return config;
    }

  }
}
