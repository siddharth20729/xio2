/*
 * Copyright (C) 2015 Jeff Rose
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
 */
package com.xjeffrose.xio2.http.server;

import com.xjeffrose.log.Log;
import com.xjeffrose.xio2.ChannelContext;
import com.xjeffrose.xio2.http.Http;
import com.xjeffrose.xio2.http.HttpResponse;
import java.util.logging.Logger;

class TestHttpService extends HttpService {
  private static final Logger log = Log.getLogger(TestHttpService.class.getName());

//  public void handleNotFound() {
//    ctx.write(HttpResponse.DefaultResponse(Http.Version.HTTP1_1, Http.Status.NOT_FOUND));
//  }

  public void handleGet() {
    ctx.write(HttpResponse.DefaultResponse(Http.Version.HTTP1_1, Http.Status.OK));
  }

}