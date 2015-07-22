package com.xjeffrose.xio2.http;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class Http {

  private Http() { }

  public enum Method {
    GET("GET"),
    POST("POST"),
    PUT("PUT"),
    DELETE("DELETE");

    private String method;

    Method(String method) {
      this.method = method;
    }

    public String toString() {
      return method;
    }
  }

  public enum Version {
    HTTP1_0("HTTP/1.0"),
    HTTP1_1("HTTP/1.1"),
    HTTP2("HTTP/2");

    private String version;

    Version(String version) {
      this.version = version;
    }

    public String toString() {
      return version;
    }
  }

  public enum Status {
    SWITCHING_PROTOCOLS(101, "Switching Protocols"),
    OK(200, "OK"),
    CREATED(201, "Created"),
    ACCEPTED(202, "Accepted"),
    NO_CONTENT(204, "No Content"),
    MOVED_PERMANENTLY(301, "Moved Permanently"),
    SEE_OTHER(303, "See Other"),
    NOT_MODIFIED(304, "Not Modified"),
    TEMPORARY_REDIRECT(307, "Temporary Redirect"),
    BAD_REQUEST(400, "Bad Request"),
    UNAUTHORIZED(401, "Unauthorized"),
    FORBIDDEN(403, "Forbidden"),
    NOT_FOUND(404, "Not Found"),
    NOT_ACCEPTABLE(406, "Not Acceptable"),
    CONFLICT(409, "Conflict"),
    GONE(410, "Gone"),
    PRECONDITION_FAILED(412, "Precondition Failed"),
    INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
    SERVICE_UNAVAILABLE(503, "Service Unavailable");

    private int code;
    private String responseString;

    Status(int code, String responseString) {
      this.code = code;
      this.responseString = responseString;
    }

    public String toString() {
      return Integer.toString(code) + " " + responseString;
    }
  }

  public static String date() {
    return ZonedDateTime
        .now(ZoneId.of("UTC"))
        .format(DateTimeFormatter.RFC_1123_DATE_TIME);
  }
}
