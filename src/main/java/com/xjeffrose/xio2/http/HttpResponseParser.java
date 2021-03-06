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
package com.xjeffrose.xio2.http;

import com.xjeffrose.log.Log;
import java.nio.ByteBuffer;
import java.util.logging.Logger;

@SuppressWarnings (value = { "fallthrough" })

// http://tools.ietf.org/html/rfc2616
public class HttpResponseParser {
  private static final Logger log = Log.getLogger(HttpResponseParser.class.getName());

  private int lastByteRead;
  private ByteBuffer temp;
  private HttpResponse response;

  public Status status = Status.BUFFER_UNDERFLOW;

  public HttpResponseParser() {
    lastByteRead = -1;
  }

  public enum Status {
    BUFFER_UNDERFLOW,
    BUFFER_OVERFLOW,
    NEEDS_PARSE,
    FINISHED,
  }

  private enum state {
    http_version_h,
    http_version_t_1,
    http_version_t_2,
    http_version_p,
    http_version_slash,
    http_version_major_start,
    http_version_major,
    http_version_minor_start,
    http_version_minor,
    status_code,
    reason_phrase,
    expecting_newline_1,
    header_line_start,
    header_lws,
    header_name,
    space_before_header_value,
    header_value,
    expecting_newline_2,
    expecting_newline_3,
    expecting_body
  };

  private state state_ = state.http_version_h;
  private int http_status = 0;

  public boolean parse(HttpResponse resp) {
    this.response = resp;
    this.temp = resp.inputBuffer.duplicate();

    ParseState result = ParseState.indeterminate;
    temp.flip();
    temp.position(lastByteRead + 1);
    while (temp.hasRemaining()) {
      lastByteRead = temp.position();
      result = parseSegment(temp.get());
      if (result == ParseState.good) {
        return true;
      }
    }
    if (state_ == state.expecting_body) {
      log.info("content-length: " + response.headers.get("content-length"));
      log.info("temp limit: " + temp.limit());
      log.info("lastByteRead: " + lastByteRead + " (+1) " + (lastByteRead + 1));
      log.info("body position: " + response.body.position);

      }
    return false;
  }

  private boolean is_char(int c) {
    return c >= 0 && c <= 127;
  }

  private boolean is_ctl(int c) {
    return (c >= 0 && c <= 31) || (c == 127);
  }

  private boolean is_tspecial(int c) {
    switch (c) {
      case '(': case ')': case '<': case '>': case '@':
      case ',': case ';': case ':': case '\\': case '"':
      case '/': case '[': case ']': case '?': case '=':
      case '{': case '}': case ' ': case '\t':
        return true;
      default:
        return false;
    }
  }

  private boolean is_digit(char c) {
    return c >= '0' && c <= '9';
  }

  private enum ParseState {
    good,
    bad,
    indeterminate;

    private static ParseState fromBoolean(boolean state) {
      if (state) {
        return ParseState.good;
      } else {
        return ParseState.bad;
      }
    }
  }

  private ParseState parseSegment(byte input) {
    switch (state_) {
      case http_version_h:
        if (input == 'H') {
          state_ = state.http_version_t_1;
          return ParseState.indeterminate;
        } else {
          return ParseState.bad;
        }
      case http_version_t_1:
        if (input == 'T') {
          state_ = state.http_version_t_2;
          return ParseState.indeterminate;
        } else {
          return ParseState.bad;
        }
      case http_version_t_2:
        if (input == 'T') {
          state_ = state.http_version_p;
          return ParseState.indeterminate;
        } else {
          return ParseState.bad;
        }
      case http_version_p:
        if (input == 'P') {
          state_ = state.http_version_slash;
          return ParseState.indeterminate;
        } else {
          return ParseState.bad;
        }
      case http_version_slash:
        if (input == '/') {
          state_ = state.http_version_major_start;
          return ParseState.indeterminate;
        } else {
          return ParseState.bad;
        }
      case http_version_major_start:
        if (is_digit((char) input)) {
          response.http_version_major = (char) input - '0';
          state_ = state.http_version_major;
          return ParseState.indeterminate;
        } else {
          return ParseState.bad;
        }
      case http_version_major:
        if (input == '.') {
          state_ = state.http_version_minor_start;
          return ParseState.indeterminate;
        } else if (is_digit((char) input)) {
          response.http_version_major = response.http_version_major * 10 + (char) input - '0';
          return ParseState.indeterminate;
        } else {
          return ParseState.bad;
        }
      case http_version_minor_start:
        if (is_digit((char) input)) {
          response.http_version_minor = (char) input - '0';
          state_ = state.http_version_minor;
          return ParseState.indeterminate;
        } else {
          return ParseState.bad;
        }
      case http_version_minor:
        if (input == ' ') {
          state_ = state.status_code;
          return ParseState.indeterminate;
        } else if (is_digit((char) input)) {
          response.http_version_minor = response.http_version_minor * 10 + (char) input - '0';
          return ParseState.indeterminate;
        } else {
          return ParseState.bad;
        }
      case status_code:
        if (input == ' ') {
          response.setStatus(Http.Status.fromCode(http_status));
          state_ = state.reason_phrase;
          return ParseState.indeterminate;
        } else if (is_digit((char) input)) {
          http_status = http_status * 10 + (char) input - '0';
          return ParseState.indeterminate;
        } else {
          return ParseState.bad;
        }
      case reason_phrase:
        if (input == '\r') {
          state_ = state.expecting_newline_1;
          return ParseState.indeterminate;
        } else if (!is_ctl(input)) {
          return ParseState.indeterminate;
        } else {
          return ParseState.bad;
        }
      case expecting_newline_1:
        if (input == '\n') {
          state_ = state.header_line_start;
          return ParseState.indeterminate;
        } else {
          return ParseState.bad;
        }
      case header_line_start:
        if (input == '\r') {
          state_ = state.expecting_newline_3;
          return ParseState.indeterminate;
        } else if (!response.headers.empty() && (input == ' ' || input == '\t')) {
          state_ = state.header_lws;
          return ParseState.indeterminate;
        } else if (!is_char(input) || is_ctl(input) || is_tspecial(input)) {
          return ParseState.bad;
        } else {
          //TODO
          response.headers.newHeader();
          response.headers.tick(lastByteRead);
          state_ = state.header_name;
          return ParseState.indeterminate;
        }
      case header_lws:
        if (input == '\r') {
          state_ = state.expecting_newline_2;
          return ParseState.indeterminate;
        } else if (input == ' ' || input == '\t') {
          return ParseState.indeterminate;
        } else if (is_ctl(input)) {
          return ParseState.bad;
        } else {
          state_ = state.header_value;
          response.headers.newValue();
          response.headers.tick(lastByteRead);
          return ParseState.indeterminate;
        }
      case header_name:
        if (input == ':') {
          state_ = state.space_before_header_value;
          return ParseState.indeterminate;
        } else if (!is_char(input) || is_ctl(input) || is_tspecial(input)) {
          return ParseState.bad;
        } else {
          response.headers.tick(lastByteRead);
          return ParseState.indeterminate;
        }
      case space_before_header_value:
        if (input == ' ') {
          state_ = state.header_value;
          response.headers.newValue();
          return ParseState.indeterminate;
        } else {
          return ParseState.bad;
        }
      case header_value:
        if (input == '\r') {
          state_ = state.expecting_newline_2;
          return ParseState.indeterminate;
        } else if (is_ctl(input)) {
          return ParseState.bad;
        } else {
          response.headers.tick(lastByteRead);
          return ParseState.indeterminate;
        }
      case expecting_newline_2:
        if (input == '\n') {
          state_ = state.header_line_start;
          return ParseState.indeterminate;
        } else {
          return ParseState.bad;
        }
      case expecting_newline_3:
        if (input == '\n') {
          response.headers.done();
          if (response.headers.headerMap.containsKey("content-length")) {
            state_ = state.expecting_body;
            response.body.set(lastByteRead);
//            System.out.println("content-length: " + response.headers.get("content-length"));
//            System.out.println("temp limit: " + temp.limit());
//            System.out.println("lastByteRead: " + lastByteRead + " (+1) " + (lastByteRead+1));
            /*
            if (((lastByteRead + 1)
                + Integer.parseInt(response.headers.get("content-length"))) == temp.limit()) {
              response.body.set(lastByteRead);
              status = Status.FINISHED;
              return ParseState.good;
            } else {
              status = Status.BUFFER_UNDERFLOW;
              return ParseState.bad;
            }
            */
            return ParseState.indeterminate;
          } else {
            //TODO: Handle better
            status = Status.FINISHED;
            return ParseState.good;
          }
        }
      case expecting_body:
//            System.out.println("content-length: " + response.headers.get("content-length"));
//            System.out.println("temp limit: " + temp.limit());
//            System.out.println("lastByteRead: " + lastByteRead + " (+1) " + (lastByteRead+1));
        if (response.body.position + Integer.parseInt(response.headers.get("content-length")) == (lastByteRead + 1)) {
          status = Status.FINISHED;
          return ParseState.good;
        } else {
          return ParseState.indeterminate;
        }
      default:
        return ParseState.bad;
    }
  }
}
