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

import org.junit.Test;

import static org.junit.Assert.*;

public class xioCertGeneratorTest {

  @Test
  public void testGenerate() throws Exception {
    xioCertificate cert = xioCertGenerator.generate("src/test/resources/privateKey.pem", "src/test/resources/test.crt");
    assertEquals("CN=xio2.example.com,OU=dev,O=xio2,L=Chicago,ST=IL,C=US", cert.getCert().getIssuerX500Principal().getName());
  }
}