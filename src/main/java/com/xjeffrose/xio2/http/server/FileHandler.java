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

import com.xjeffrose.xio2.ChannelContext;
import com.xjeffrose.xio2.Handler;
import com.xjeffrose.xio2.Request;
import com.xjeffrose.xio2.SecureChannelContext;
import com.xjeffrose.xio2.http.Http;
import com.xjeffrose.xio2.http.HttpRequest;
import com.xjeffrose.xio2.http.HttpRequestParser;
import com.xjeffrose.xio2.http.HttpResponse;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class FileHandler implements Handler {
  private final HttpRequest req = new HttpRequest();
  private String wwwDir = "public/";

  public FileHandler() { }

  public FileHandler(String wwwDir) {
    this.wwwDir = wwwDir;
  }

  @Override
  public Request getReq() {
    return req;
  }

  @Override
  public Http.Method getMethod() {
    return req.method_;
  }

  @Override
  public ByteBuffer getInputBuffer() {
    return req.inputBuffer;
  }

  @Override
  public ChannelContext buildChannelContext(SocketChannel channel) {
    return new ChannelContext(channel, this);
  }

  @Override
  public void secureContext(SecureChannelContext secureChannelContext) {}

  @Override
  public boolean parse() {
    final HttpRequestParser parser = new HttpRequestParser();
    return parser.parse(req);
  }

  public void handle(ChannelContext ctx) {
    try {
      final String path = req.getUri().getPath().equals("/") ? "index.html" : req.getUri().getPath();
      final FileChannel fileChannel = FileChannel.open(Paths.get(wwwDir + path), StandardOpenOption.READ);
      final HttpResponse resp = HttpResponse.DefaultResponse(Http.Version.HTTP1_1, Http.Status.OK);
      resp.headers.set("Content-Length", String.valueOf(fileChannel.size()));

//      System.out.println(req.toString());
//      System.out.println(path);

      ctx.channel.write(resp.toBB());
      fileChannel.transferTo(0, fileChannel.size(), ctx.channel);
      ctx.channel.close();
    } catch (IOException e) {
      ctx.state = ChannelContext.State.start_response;
      ctx.write(HttpResponse.DefaultResponse(Http.Version.HTTP1_1, Http.Status.NOT_FOUND).toBB());
    }
  }

  public void handleError(ChannelContext ctx) {
    ctx.write(HttpResponse.DefaultResponse(Http.Version.HTTP1_1, Http.Status.BAD_REQUEST).toBB());
  }

  public void handleFatalError(ChannelContext ctx) {
    ctx.write(HttpResponse.DefaultResponse(Http.Version.HTTP1_1, Http.Status.INTERNAL_SERVER_ERROR).toBB());
    ctx.flush();
  }
}
