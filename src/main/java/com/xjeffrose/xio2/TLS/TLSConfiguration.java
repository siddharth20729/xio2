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
  public String password = null;
  public String privateKeyPath = null;
  public String x509CertPath = null;
  public char[] passwordCharArray = null;

  TLSConfiguration() { }

  public static class Builder {
    public String fqdn = null;
    public String version = null;
    public String password = null;
    private String privateKeyPath = null;
    private String x509CertPath = null;

    public Builder fqdn(String fqdn) {
      this.fqdn = fqdn;
      return this;
    }

    public Builder version(String version) {
      this.version = version;
      return this;
    }

    public Builder password(String password) {
      //TODO: Maybe pass this is as an env var?
      this.password = password;
      return this;
    }

    public Builder privateKeyPath(String privateKeyPath) {
      this.privateKeyPath = privateKeyPath;
      return this;
    }

    public Builder x509CertPath(String x509CertPath) {
      this.x509CertPath = x509CertPath;
      return this;
    }

    public TLSConfiguration build() {
      TLSConfiguration config = new TLSConfiguration();
      config.fqdn = fqdn;
      config.version = version;
      config.password = password;
      config.passwordCharArray = password.toCharArray();
      config.privateKeyPath = privateKeyPath;
      config.x509CertPath = x509CertPath;

      return config;
    }

  }
}
