package com.pdex.asyncio;

import java.nio.ByteBuffer;
import org.junit.Test;
import static org.junit.Assert.*;

public class InputBufferTest {

  @Test
  public void fillBuffer() {
    InputBuffer buffer = new InputBuffer(4096);

    try (InputBuffer.Guard watchdog = buffer.openForWriting()) {
      watchdog.getByteBuffer().put(new byte[1024]);
      watchdog.getByteBuffer().put(new byte[1024]);
    }
    assertEquals("bytes written", 2048, buffer.getBytesWritten());
    assertEquals("position equals bytes written", 2048, buffer.position());
    assertEquals("bytes availables for reading equals bytes written", 2048, buffer.getBytesAvailableForReading());
  }

  @Test
  public void fillBufferFullDrain() {
    InputBuffer buffer = new InputBuffer(4096);

    try (InputBuffer.Guard watchdog = buffer.openForWriting()) {
      watchdog.getByteBuffer().put(new byte[1024]);
      watchdog.getByteBuffer().put(new byte[1024]);
    }
    assertEquals("bytes written", 2048, buffer.getBytesWritten());
    assertEquals("position equals bytes written", 2048, buffer.position());
    assertEquals("bytes availables for reading equals bytes written", 2048, buffer.getBytesAvailableForReading());

    try (InputBuffer.Guard watchdog = buffer.openForReading()) {
      ByteBuffer output = ByteBuffer.allocateDirect(4096);
      ByteBuffer readOnlyBuffer = watchdog.getByteBuffer();
      assertEquals("position ready for reading", 0, readOnlyBuffer.position());
      assertEquals("limit ready for reading", 2048, readOnlyBuffer.limit());
      output.put(readOnlyBuffer);
    }

    assertEquals("bytes read", 2048, buffer.getBytesRead());
    assertEquals("position reset to zero", 0, buffer.position());
    assertEquals("limit reset to capacity", 4096, buffer.limit());
  }

  @Test
  public void fillBufferPartialDrain() {
    InputBuffer buffer = new InputBuffer(4096);

    try (InputBuffer.Guard watchdog = buffer.openForWriting()) {
      watchdog.getByteBuffer().put(new byte[1024]);
      watchdog.getByteBuffer().put(new byte[1024]);
    }
    assertEquals("bytes written", 2048, buffer.getBytesWritten());
    assertEquals("position equals bytes written", 2048, buffer.position());
    assertEquals("bytes availables for reading equals bytes written", 2048, buffer.getBytesAvailableForReading());

    try (InputBuffer.Guard watchdog = buffer.openForReading()) {
      ByteBuffer output = ByteBuffer.allocateDirect(1024);
      ByteBuffer readOnlyBuffer = watchdog.getByteBuffer();
      assertEquals("position ready for reading", 0, readOnlyBuffer.position());
      assertEquals("limit ready for reading", 2048, readOnlyBuffer.limit());
      readOnlyBuffer.limit(1024);
      output.put(readOnlyBuffer);
    }

    assertEquals("bytes read", 1024, buffer.getBytesRead());
    assertEquals("position reset to remaining bytes", 1024, buffer.position());
    assertEquals("limit reset to capacity", 4096, buffer.limit());
  }

  @Test
  public void fillBufferFullDrainFillAgain() {
    InputBuffer buffer = new InputBuffer(4096);

    try (InputBuffer.Guard watchdog = buffer.openForWriting()) {
      watchdog.getByteBuffer().put(new byte[1024]);
      watchdog.getByteBuffer().put(new byte[1024]);
    }
    assertEquals("bytes written", 2048, buffer.getBytesWritten());
    assertEquals("position equals bytes written", 2048, buffer.position());
    assertEquals("bytes availables for reading equals bytes written", 2048, buffer.getBytesAvailableForReading());

    try (InputBuffer.Guard watchdog = buffer.openForReading()) {
      ByteBuffer output = ByteBuffer.allocateDirect(2048);
      ByteBuffer readOnlyBuffer = watchdog.getByteBuffer();
      assertEquals("position ready for reading", 0, readOnlyBuffer.position());
      assertEquals("limit ready for reading", 2048, readOnlyBuffer.limit());
      output.put(readOnlyBuffer);
    }

    assertEquals("bytes read", 2048, buffer.getBytesRead());
    assertEquals("position reset to remaining bytes", 0, buffer.position());
    assertEquals("limit reset to capacity", 4096, buffer.limit());

    try (InputBuffer.Guard watchdog = buffer.openForWriting()) {
      watchdog.getByteBuffer().put(new byte[1024]);
      watchdog.getByteBuffer().put(new byte[1024]);
    }
    assertEquals("bytes written", 4096, buffer.getBytesWritten());
    assertEquals("position equals bytes written", 2048, buffer.position());
  }

  @Test
  public void fillBufferPartialDrainFillAgain() {
    InputBuffer buffer = new InputBuffer(4096);

    try (InputBuffer.Guard watchdog = buffer.openForWriting()) {
      watchdog.getByteBuffer().put(new byte[1024]);
      watchdog.getByteBuffer().put(new byte[1024]);
    }
    assertEquals("bytes written", 2048, buffer.getBytesWritten());
    assertEquals("position equals bytes written", 2048, buffer.position());
    assertEquals("bytes availables for reading equals bytes written", 2048, buffer.getBytesAvailableForReading());

    try (InputBuffer.Guard watchdog = buffer.openForReading()) {
      ByteBuffer output = ByteBuffer.allocateDirect(1024);
      ByteBuffer readOnlyBuffer = watchdog.getByteBuffer();
      assertEquals("position ready for reading", 0, readOnlyBuffer.position());
      assertEquals("limit ready for reading", 2048, readOnlyBuffer.limit());
      readOnlyBuffer.limit(1024);
      output.put(readOnlyBuffer);
    }

    assertEquals("bytes read", 1024, buffer.getBytesRead());
    assertEquals("position reset to remaining bytes", 1024, buffer.position());
    assertEquals("limit reset to capacity", 4096, buffer.limit());

    try (InputBuffer.Guard watchdog = buffer.openForWriting()) {
      watchdog.getByteBuffer().put(new byte[1024]);
      watchdog.getByteBuffer().put(new byte[1024]);
    }

    assertEquals("bytes written", 4096, buffer.getBytesWritten());
    assertEquals("position equals bytes written", 3072, buffer.position());
    assertEquals("bytes availables for reading equals bytes written", 3072, buffer.getBytesAvailableForReading());
  }

  @Test
  public void fillBufferPreventOverflow() {
    InputBuffer buffer = new InputBuffer(4096);

    ByteBuffer filler = ByteBuffer.allocate(8192);
    buffer.put(filler);

    assertEquals("bytes written", 8192, buffer.getBytesWritten());
    assertEquals("position equals bytes written", 8192, buffer.position());
    assertEquals("bytes availables for reading equals bytes written", 8192, buffer.getBytesAvailableForReading());
  }

  @Test
  public void fillOkThenFillBufferPreventOverflow() {
    InputBuffer buffer = new InputBuffer(4096);

    buffer.put(ByteBuffer.allocate(3013));
    assertEquals("bytes written", 3013, buffer.getBytesWritten());

    buffer.put(ByteBuffer.allocate(8144));

    assertEquals("bytes written", 3013+8144, buffer.getBytesWritten());
    assertEquals("position equals bytes written", 3013+8144, buffer.position());
    assertEquals("bytes availables for reading equals bytes written", 3013+8144, buffer.getBytesAvailableForReading());
  }

  @Test(expected = IllegalStateException.class)
  public void nagStupidUsers() {
    InputBuffer buffer = new InputBuffer(4096);

    try (InputBuffer.Guard watchdog = buffer.openForWriting()) {
      buffer.openForWriting();
    }
  }
}