package com.pdex.asyncio;

import java.io.Closeable;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

/**
 * Wraps a ByteBuffer
 *
 * Used for the following scenario:
 * initial state empty
 * populate the buffer with some unknown number of bytes (think channel.read)
 * drain the buffer of some unknown number of bytes (think channel.write)
 */
public class InputBuffer {
  private ByteBuffer buffer;
  private Guard watchdog = null;
  private int bytesWritten = 0;
  private int bytesRead = 0;

  public InputBuffer(int capacity) {
    this.buffer = ByteBuffer.allocateDirect(capacity);
  }

  public int getBytesRead() {
    return bytesRead;
  }

  public int getBytesWritten() {
    return bytesWritten;
  }

  public int getBytesAvailableForReading() {
    return Math.max(0, bytesWritten - bytesRead);
  }

  public int getBytesAvailableForWriting() {
    return buffer.remaining();
  }

  public int position() {
    return buffer.position();
  }

  public int limit() {
    return buffer.limit();
  }

  public ByteBuffer debug() { return buffer; }

  public void increaseCapacity(int needBytes) {
    int new_capacity = buffer.capacity();
    do {
      new_capacity = new_capacity * 2;
    } while(new_capacity < (needBytes + buffer.position()));
    ByteBuffer tmp = ByteBuffer.allocateDirect(new_capacity);
    if (getBytesAvailableForReading() > 0) {
      buffer.flip();
      tmp.put(buffer);
    }
    buffer = tmp;
  }

  public void put(ByteBuffer bytes) {
    int bytesToWrite = bytes.remaining();
    if (bytes.remaining() > buffer.remaining()) {
      increaseCapacity(bytes.remaining());
    }
    buffer.put(bytes);
    bytesWritten += bytesToWrite;
  }

  public CharBuffer charView() {
    ByteBuffer view = buffer.duplicate();
    view.flip();
    return Charset.forName("UTF-8").decode(view);
  }

  public abstract class Guard implements Closeable {
    protected final ByteBuffer duplicate;
    Guard(ByteBuffer duplicate) {
      this.duplicate = duplicate;
    }
    public ByteBuffer getByteBuffer() {
      return duplicate;
    }
    public abstract void close();
  }

  private class WriteGuard extends Guard {
    private final int position;
    WriteGuard(ByteBuffer duplicate) {
      super(duplicate);
      position = duplicate.position();
    }
    public void close() {
      int written = (duplicate.position()-position);
      bytesWritten += written;
      buffer.position(buffer.position() + written);
      watchdog = null;
    }
  }

  private class ReadGuard extends Guard {
    private int limit;
    ReadGuard(ByteBuffer duplicate) {
      super(duplicate);
    }
    public ByteBuffer getByteBuffer() {
      duplicate.flip();
      limit = duplicate.limit();
      return duplicate;
    }
    public void close() {
      bytesRead += duplicate.position();
      buffer.position(duplicate.position());
      buffer.limit(limit);
      watchdog = null;
      buffer.compact();
    }
  }

  public Guard openForReading() {
    return openForReading(true);
  }
  public Guard openForReading(boolean usingStupidSSLEngine) {
    if (watchdog == null) {
      if (usingStupidSSLEngine) {
        watchdog = new ReadGuard(buffer.duplicate());
      } else {
        watchdog = new ReadGuard(buffer.asReadOnlyBuffer());
      }
      return watchdog;
    } else {
      throw new IllegalStateException("The previous call to openForWriting has not been closed, did you forget to use try-with-resources");
    }
  }

  public Guard openForWriting() {
    if (watchdog == null) {
      watchdog = new WriteGuard(buffer.duplicate());
      return watchdog;
    } else {
      throw new IllegalStateException("The previous call to openForWriting has not been closed, did you forget to use try-with-resources");
    }
  }

  public String toString() {
    return buffer.toString();
  }
}