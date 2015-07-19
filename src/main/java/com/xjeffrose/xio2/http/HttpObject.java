package com.xjeffrose.xio2.http;

import com.xjeffrose.log.Log;
import com.xjeffrose.xio2.util.BB;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

public class HttpObject extends Http {
  private static final Logger log = Log.getLogger(HttpObject.class.getName());

  public final ByteBuffer inputBuffer = ByteBuffer.allocateDirect(4096);
  public int http_version_major = 0;
  public int http_version_minor = 0;
  public Method method = new Method();
  public Uri uri = new Uri();
  public Headers headers = new Headers();
  public Body body = new Body();
  public HttpMethod method_ = HttpMethod.GET;
  private Version version;
  private Status status;

  public HttpObject() { }

  public String getHttpVersion() {

    if (http_version_major == 1) {
      return "HTTP"
          +
          "/"
          +
          http_version_major
          +
          "."
          +
          http_version_minor;
    }

    return version.toString();
  }

  public URI getUri() {
    return uri.getUri();
  }

  public void setUri(String inputUri) {
    uri.setUri(inputUri);
  }

  public String getMethod() {
    return method.getMethod();
  }

  public String getBody() {
    return body.getBody();
  }

  public void setVersion(Version version) {

    this.version = version;
  }

  public void setStatus(Status status) {

    this.status = status;
  }

  public String getStatus() {
    return status.toString();
  }

  public enum HttpMethod {
    GET,
    POST,
    PUT,
    DELETE
  }

  public void setMethod() {
    String meth = method.getMethod();
    if (meth.equalsIgnoreCase("get")) {
      method_ = HttpMethod.GET;
    } else if (meth.equalsIgnoreCase("post")) {
      method_ = HttpMethod.POST;
    } else if (meth.equalsIgnoreCase("put")) {
      method_ = HttpMethod.PUT;
    } else if (meth.equalsIgnoreCase("delete")) {
      method_ = HttpMethod.DELETE;
    }
  }

  public void setMethod(String meth) {
    if (meth.equalsIgnoreCase("get")) {
      method_ = HttpMethod.GET;
    } else if (meth.equalsIgnoreCase("post")) {
      method_ = HttpMethod.POST;
    } else if (meth.equalsIgnoreCase("put")) {
      method_ = HttpMethod.PUT;
    } else if (meth.equalsIgnoreCase("delete")) {
      method_ = HttpMethod.DELETE;
    }
  }

  class Picker {
    public int position = -1;
    public int limit = 0;

    private Picker() { }

    public void tick(int currentPos) {
      if (position == -1) {
        position = currentPos;
      }
      limit++;
    }

    public String get() {
      final byte[] value = new byte[limit];
      if (position > 0) {
        inputBuffer.position(position);
      } else {
        inputBuffer.position(0);
      }
      inputBuffer.get(value);
      return new String(value, Charset.forName("UTF-8"));
    }
  }

  class Method extends Picker {
    private String method = null;

    public String getMethod() {
      if (method == null) {
        method = get();
      }
      return method;
    }

    public void setMethod(String inputMethod) {
      method = inputMethod;
    }
  }

  class Uri extends Picker {
    private URI uri = null;

    public URI getUri() {
      if (uri == null) {
        uri = URI.create(get());
      }
      return uri;
    }

    public void setUri(String inUri) {
      uri = URI.create(inUri);
    }

    public Map<String, String> splitQuery(URI uri) throws UnsupportedEncodingException {
      Map<String, String> query_pairs = new LinkedHashMap<String, String>();
      String query = uri.getQuery();
      String[] pairs = query.split("&");
      for (String pair : pairs) {
        int idx = pair.indexOf("=");
        query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"),
            URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
      }
      return query_pairs;
    }
  }

  class Headers extends Picker {
    private String name;
    public Map<String, String> headerMap = new HashMap<>();

    public void set(String name, String value) {
      headerMap.put(name, value);
    }

    public String get(String name) {
      final String val = headerMap.get(name);
      if (val != null) {
        return val;
      } else {
        return "";
      }
    }

    public int size() {
      return headerMap.size();
    }

    public boolean empty() {
      return headerMap.isEmpty();
    }

    private void reset() {
      position = -1;
      limit = 0;
    }

    public void newHeader() {
      if (name != null) {
        headerMap.put(name, get());
        reset();
      }
    }

    public void newValue() {
      name = get();
      reset();
    }

    public void done() {
      headerMap.put(name, get());
      reset();
    }
  }

  class Body extends Picker {
    private ByteBuffer buf;

    public Body() { }

    public void set(int position) {
      final int size = new Integer(headers.get("Content-Length"));
      this.position = position + 1;
      this.limit = size - 1;
    }

    public void set(String body) {
      buf = ByteBuffer.allocateDirect(body.length() + 1);
      buf.put(body.getBytes());
    }

    private String getBody() {
      if (position == -1) {
        buf.flip();
        return BB.BBtoString(buf);
      }
      return get();
    }
  }
}
