/*
 * SeekableFileInputStream
 * 
 * Author: Lasse Collin <lasse.collin@tukaani.org>
 * 
 * This file has been put into the public domain. You can do whatever you want with this file.
 */

package org.tukaani.xz;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Wraps a {@link java.io.RandomAccessFile RandomAccessFile} in a SeekableInputStream.
 */
public class SeekableMappedFileInputStream extends SeekableInputStream {
  /**
   * The RandomAccessFile that has been wrapped into a SeekableFileInputStream.
   */

  protected MappedByteBuffer mbb;
  private final long         len;

  /**
   * Creates a new seekable input stream that reads from a file with the specified name.
   * 
   * @param len
   * 
   * @throws IOException
   */
  public SeekableMappedFileInputStream(String name, long offset, long len) throws IOException {
    RandomAccessFile randomAccessFile = new RandomAccessFile(name, "r");
    this.len = len;
    this.mbb = randomAccessFile.getChannel().map(FileChannel.MapMode.READ_ONLY, offset, len);
    randomAccessFile.close();
  }

  /**
   * Calls {@link RandomAccessFile#read() randomAccessFile.read()}.
   */
  @Override
  public int read() throws IOException {
    return 0xff & this.mbb.get();
  }

  /**
   * Calls {@link RandomAccessFile#read(byte[]) randomAccessFile.read(buf)}.
   */
  @Override
  public int read(byte[] buf) throws IOException {
    return this.read(buf, 0, buf.length);
  }

  /**
   * Calls {@link RandomAccessFile#read(byte[],int,int) randomAccessFile.read(buf, off, len)}.
   */
  @Override
  public int read(byte[] buf, int off, int len) throws IOException {
    this.mbb.get(buf, off, len);
    return len;
  }

  /**
   * Calls {@link RandomAccessFile#close() randomAccessFile.close()}.
   */
  @Override
  public void close() throws IOException {
    // mbb.close(): A mapped byte buffer and the file mapping that it represents remain valid until the buffer
    // itself is garbage-collected.
    // sun.misc.Cleaner cleaner = ((DirectBuffer) this.mbb).cleaner();
    // cleaner.clean();
  }

  /**
   * Calls {@link RandomAccessFile#length() randomAccessFile.length()}.
   */
  @Override
  public long length() throws IOException {
    return this.len;
  }

  /**
   * Calls {@link RandomAccessFile#getFilePointer() randomAccessFile.getFilePointer()}.
   */
  @Override
  public long position() throws IOException {
    return this.mbb.position();
  }

  /**
   * Calls {@link RandomAccessFile#seek(long) randomAccessFile.seek(long)}.
   */
  @Override
  public void seek(long pos) throws IOException {
    this.mbb.position((int) pos);
  }
}
