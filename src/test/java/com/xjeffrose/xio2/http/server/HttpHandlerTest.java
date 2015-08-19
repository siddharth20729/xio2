package com.xjeffrose.xio2.http.server;

import com.xjeffrose.xio2.ChannelContext;
import com.xjeffrose.xio2.Request;
import com.xjeffrose.xio2.http.HttpRequest;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;
import static org.junit.Assert.*;

public class HttpHandlerTest {

  @Test
  public void testDefaultService() throws Exception {
    AtomicBoolean called = new AtomicBoolean(false);
    HttpHandler handler = new HttpHandler(new HttpService() {
      public void handle(ChannelContext ctx, Request req) {
        called.set(true);
      }
    });
    ChannelContext ctx = new ChannelContext(null, null);
    ctx.req = HttpRequest.newBuilder().url("http://example.com/foo/bar/baz").build();
    handler.handle(ctx);
    assertTrue("Default handler was invoked", called.get());
    called.set(false);
    handler.addRoute("/foo/bar/baz", new HttpService() {
      public void handle(ChannelContext ctx, Request req) {
      }
    });
    assertFalse("Default handler wasn't invoked", called.get());
  }

}