package com.xjeffrose.xio2.http;

import com.xjeffrose.log.Log;
import com.xjeffrose.xio2.util.BB;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

public class HttpObject {
  private static final Logger log = Log.getLogger(HttpObject.class.getName());

  public final ByteBuffer inputBuffer = ByteBuffer.allocateDirect(4096);
  public int http_version_major = 0;
  public int http_version_minor = 0;
  public Method method;
  public Uri uri;
  public Headers headers;
  public Body body;
  public HttpMethod method_ = HttpMethod.GET;

  public HttpObject() {

    method = new Method();
    uri = new Uri();
    headers = new Headers();
    body = new Body();
  }

  public String getHttpVersion() {
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

  class Headers {
    private final Deque<Header> header_list = new ArrayDeque<Header>();

    // TODO prefer list of Header objects to minimize String objects at parse time.
    public Map<String, Header> headerMap = new HashMap<String, Header>();
    private Header currentHeader = null;

    Headers() {

    }

    public void newHeader() {
      if (currentHeader != null) {
        done();
      }
      currentHeader = new Header();
    }

    public void done() {
      headerMap.put(currentHeader.name(), currentHeader);
    }

    public void newValue() {
      currentHeader.startValue();
    }

    public void tick(int currentPos) {
      currentHeader.tick(currentPos);
    }

    public boolean empty() {
      return header_list.size() == 0;
    }

    public String get(String name) {
      Header header = headerMap.get(name);
      if (header != null) {
        return header.value();
      } else {
        return "";
      }
    }

    public void set(String name, String value) {
      headerMap.putIfAbsent(name, new Header(name, value));
    }
  }

  class Header {

    private final Picker name = new Picker();
    private final Picker value = new Picker();
    private Picker currentPicker;

    public Header() {
      currentPicker = name;
    }

    public Header(String name, String value) {
    }

    public String name() {
      return name.get();
    }

    public String value() {
      return value.get();
    }

    public void tick(int currentPos) {
      currentPicker.tick(currentPos);
    }

    public void startValue() {
      currentPicker = value;
    }
  }

  class Body extends Picker {
    private ByteBuffer buf;

    public Body() { }

    public void set(int position) {
      final int size = new Integer(headers.get("Content-Length"));
      this.position = position + 1;
      this.limit = size - 1;
      //buf = ByteBuffer.allocateDirect(size);
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
