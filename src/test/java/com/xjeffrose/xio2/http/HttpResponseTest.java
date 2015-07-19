package com.xjeffrose.xio2.http;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import org.junit.Test;

import static org.junit.Assert.*;

public class HttpResponseTest {
  HttpResponse resp = new HttpResponse();

  @Test
  public void testToString() throws Exception {
    assertEquals(HttpResponse.Status.NOT_FOUND.toString(), "404 Not Found");
  }

  @Test
  public void testDate() throws Exception {
    assertEquals(resp.date(), ZonedDateTime
        .now(ZoneId.of("UTC"))
        .format(DateTimeFormatter.RFC_1123_DATE_TIME));
  }
}