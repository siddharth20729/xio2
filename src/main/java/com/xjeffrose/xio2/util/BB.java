package com.xjeffrose.xio2.util;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class BB {

  private BB() { }

  public static String BBtoString(ByteBuffer bb) {
    final byte[] value = new byte[bb.capacity()];
    final ByteBuffer bbTemp = bb.duplicate();
    bbTemp.position(0);
    bbTemp.limit(bbTemp.capacity());
    bbTemp.get(value);
    return new String(value, Charset.forName("UTF-8"));
  }

  public static ByteBuffer StringtoBB(String s) {
    final ByteBuffer bb = ByteBuffer.allocateDirect(s.length());
    bb.position(0);
    bb.limit(bb.capacity());
    bb.put(s.getBytes());
    bb.flip();
    return bb;
  }
}
