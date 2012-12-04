/*
 * SeekableFileInputStream
 *
 * Author: Lasse Collin <lasse.collin@tukaani.org>
 *
 * This file has been put into the public domain.
 * You can do whatever you want with this file.
 */

package org.tukaani.xz;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Wraps a {@link java.io.RandomAccessFile RandomAccessFile} in a
 * SeekableInputStream.
 */
public class SeekableFileInputStream extends SeekableInputStream
{
  /**
   * The RandomAccessFile that has been wrapped into a SeekableFileInputStream.
   */
  protected RandomAccessFile randomAccessFile;

  private final long offset;

  private final long len;


  public SeekableFileInputStream(String name) throws IOException
  {
    this(name, 0, Long.MAX_VALUE);
  }


  public SeekableFileInputStream(String name, long offset) throws IOException
  {
    this(name, offset, Long.MAX_VALUE);
  }


  /**
   * Creates a new seekable input stream that reads from a file with the
   * specified name.
   * 
   * @throws IOException
   */
  public SeekableFileInputStream(String name, long offset, long len) throws IOException
  {
    this.randomAccessFile = new RandomAccessFile(name, "r");
    this.offset = offset;
    this.len = Math.min(randomAccessFile.length() - offset, len);
    this.randomAccessFile.seek(offset);
  }


  /**
   * Calls {@link RandomAccessFile#read() randomAccessFile.read()}.
   */
  public int read() throws IOException
  {
    return randomAccessFile.read();
  }


  /**
   * Calls {@link RandomAccessFile#read(byte[]) randomAccessFile.read(buf)}.
   */
  public int read(byte[] buf) throws IOException
  {
    return randomAccessFile.read(buf);
  }


  /**
   * Calls {@link RandomAccessFile#read(byte[],int,int)
   * randomAccessFile.read(buf, off, len)}.
   */
  public int read(byte[] buf, int off, int len) throws IOException
  {
    return randomAccessFile.read(buf, off, len);
  }


  /**
   * Calls {@link RandomAccessFile#close() randomAccessFile.close()}.
   */
  public void close() throws IOException
  {
    randomAccessFile.close();
  }


  /**
   * Calls {@link RandomAccessFile#length() randomAccessFile.length()}.
   */
  public long length() throws IOException
  {
    return len;
  }


  /**
   * Calls {@link RandomAccessFile#getFilePointer()
   * randomAccessFile.getFilePointer()}.
   */
  public long position() throws IOException
  {
    return randomAccessFile.getFilePointer() - offset;
  }


  /**
   * Calls {@link RandomAccessFile#seek(long) randomAccessFile.seek(long)}.
   */
  public void seek(long pos) throws IOException
  {
    randomAccessFile.seek(pos + offset);
  }
}
