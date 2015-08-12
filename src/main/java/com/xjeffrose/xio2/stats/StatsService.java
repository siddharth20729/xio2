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

package com.xjeffrose.xio2.stats;

import com.xjeffrose.xio2.http.Http;
import com.xjeffrose.xio2.http.HttpResponse;
import com.xjeffrose.xio2.http.server.HttpService;

public class StatsService extends HttpService {

  public void handleGet() { }

  public void handlePost() {
    ctx.write(HttpResponse.DefaultResponse(Http.Version.HTTP1_1, Http.Status.BAD_REQUEST).toBB());
  }

  public void handlePut() {
    ctx.write(HttpResponse.DefaultResponse(Http.Version.HTTP1_1, Http.Status.BAD_REQUEST).toBB());
  }

  public void handleDelete() {
    ctx.write(HttpResponse.DefaultResponse(Http.Version.HTTP1_1, Http.Status.BAD_REQUEST).toBB());
  }

  private void handleTrace() {
    ctx.write(HttpResponse.DefaultResponse(Http.Version.HTTP1_1, Http.Status.BAD_REQUEST).toBB());
  }

  private void handleOption() {
    ctx.write(HttpResponse.DefaultResponse(Http.Version.HTTP1_1, Http.Status.BAD_REQUEST).toBB());
  }

  private void handleConnect() {
    ctx.write(HttpResponse.DefaultResponse(Http.Version.HTTP1_1, Http.Status.BAD_REQUEST).toBB());
  }

  private void handlePatch() {
    ctx.write(HttpResponse.DefaultResponse(Http.Version.HTTP1_1, Http.Status.BAD_REQUEST).toBB());
  }

  public void andThen(HttpService httpService) {
    httpServiceList.addLast(httpService);
  }

  private void serviceStream() {
    while (httpServiceList.size() > 0) {
      httpServiceList.removeLast().handle(ctx, req);
    }
  }

}
