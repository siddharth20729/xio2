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

package com.xjeffrose.xio2.http.server;

import com.xjeffrose.xio2.SecureChannelContext;
import com.xjeffrose.xio2.TLS.TLS;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class HttpsHandlerTest {

  @Test
  public void testShortcut() throws Exception {
    HttpsHandler handler = new HttpsHandler("src/test/resources/privateKey.pem", "src/test/resources/cert.pem");

    SecureChannelContext ctx = new SecureChannelContext(null, handler, "");
    TLS tls = handler.buildTLS(ctx);

    assertTrue("tls engine has been created", tls.engine != null);
  }

  @Test
  public void testSelfSigned() throws Exception {
    HttpsHandler handler = new HttpsHandler();

    SecureChannelContext ctx = new SecureChannelContext(null, handler, "");
    TLS tls = handler.buildTLS(ctx);

    assertTrue("tls engine has been created", tls.engine != null);
  }

//  @Test
//  public void testConfig() throws Exception {
//    TLSConfiguration
//    HttpsHandler handler = new HttpsHandler("src/test/resources/privateKey.pem", "src/test/resources/cert.pem");
//
//    SecureChannelContext ctx = new SecureChannelContext(null, handler);
//    TLS tls = handler.buildTLS(ctx);
//
//    assertTrue("tls engine has been created", tls.engine != null);
//  }

}