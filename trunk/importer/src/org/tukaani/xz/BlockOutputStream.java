/*
 * BlockOutputStream
 *
 * Author: Lasse Collin <lasse.collin@tukaani.org>
 *
 * This file has been put into the public domain.
 * You can do whatever you want with this file.
 */

package org.tukaani.xz;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.tukaani.xz.common.EncoderUtil;

/**
 * BUFFER_WRITE: single thread access only
 * 
 */
class BlockOutputStream extends FinishableOutputStream
{
  private final OutputStream out;

  private final CountingOutputStream outCounted;

  private FinishableOutputStream filterChain;

  private final int headerSize;

  private long uncompressedSize = 0;


  public BlockOutputStream(OutputStream out, FilterEncoder[] filters) throws IOException
  {
    this.out = out;

    // Initialize the filter chain.
    outCounted = new CountingOutputStream(out);
    filterChain = outCounted;
    for (int i = filters.length - 1; i >= 0; --i)
      filterChain = filters[i].getOutputStream(filterChain);

    // Prepare to encode the Block Header field.
    ByteArrayOutputStream bufStream = new ByteArrayOutputStream();

    // Write a dummy Block Header Size field. The real value is written
    // once everything else except CRC32 has been written.
    bufStream.write(0x00);

    // Write Block Flags. Storing Compressed Size or Uncompressed Size
    // isn't supported for now.
    bufStream.write(filters.length - 1);

    // List of Filter Flags
    for (int i = 0; i < filters.length; ++i)
    {
      EncoderUtil.encodeVLI(bufStream, filters[i].getFilterID());
      byte[] filterProps = filters[i].getFilterProps();
      EncoderUtil.encodeVLI(bufStream, filterProps.length);
      bufStream.write(filterProps);
    }

    // Header Padding
    while ((bufStream.size() & 3) != 0)
      bufStream.write(0x00);

    byte[] buf = bufStream.toByteArray();

    headerSize = buf.length;

    // This is just a sanity check.
    if (headerSize > EncoderUtil.BLOCK_HEADER_SIZE_MAX)
      throw new UnsupportedOptionsException();

    // Block Header Size
    buf[0] = (byte) (buf.length / 4);

    // Write the Block Header field to the output stream.
    out.write(buf);
  }

  private static final byte[] BUFFER_WRITE = new byte[1];


  public void write(int b) throws IOException
  {
    BUFFER_WRITE[0] = (byte) b;
    write(BUFFER_WRITE, 0, 1);
  }


  public void write(byte[] buf, int off, int len) throws IOException
  {
    filterChain.write(buf, off, len);
    uncompressedSize += len;
  }


  public void flush() throws IOException
  {
    filterChain.flush();
  }


  public void finish() throws IOException
  {
    // Finish the Compressed Data field.
    filterChain.finish();

    // Block Padding
    for (long i = outCounted.getSize(); (i & 3) != 0; ++i)
      out.write(0x00);

  }


  public long getUnpaddedSize()
  {
    return headerSize + outCounted.getSize();
  }


  public long getUncompressedSize()
  {
    return uncompressedSize;
  }
}
