/*
 * BlockInputStream
 *
 * Author: Lasse Collin <lasse.collin@tukaani.org>
 *
 * This file has been put into the public domain.
 * You can do whatever you want with this file.
 */

package org.tukaani.xz;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.tukaani.xz.common.DecoderUtil;

/**
 * BUFFER_HEADER: single thread access only
 * BUFFER_READ
 */
class BlockInputStream extends InputStream
{
  private final DataInputStream inData;

  private final CountingInputStream inCounted;

  private InputStream filterChain;

  private long uncompressedSizeInHeader = -1;

  private long compressedSizeInHeader = -1;

  private long compressedSizeLimit;

  private final int headerSize;

  private long uncompressedSize = 0;

  private boolean endReached = false;

  private static final byte[] BUFFER_HEADER = new byte[DecoderUtil.BLOCK_HEADER_SIZE_MAX];


  public BlockInputStream(InputStream in, int memoryLimit, long unpaddedSizeInIndex, long uncompressedSizeInIndex)
      throws IOException, CorruptedInputException
  {
    inData = new DataInputStream(in);

    // Block Header Size or Index Indicator
    inData.readFully(BUFFER_HEADER, 0, 1);

    // See if this begins the Index field.
    if (BUFFER_HEADER[0] == 0x00)
      throw new CorruptedInputException();

    // Read the rest of the Block Header.
    headerSize = 4 * ((BUFFER_HEADER[0] & 0xFF));
    inData.readFully(BUFFER_HEADER, 1, headerSize - 1);

    // Memory for the Filter Flags field
    int filterCount = (BUFFER_HEADER[1] & 0x03) + 1;
    long[] filterIDs = new long[filterCount];
    byte[][] filterProps = new byte[filterCount][];

    ByteArrayInputStream bufStream = new ByteArrayInputStream(BUFFER_HEADER, 2, headerSize - 2);

    try
    {
      // Set the maximum valid compressed size. This is overriden
      // by the value from the Compressed Size field if it is present.
      compressedSizeLimit = (DecoderUtil.VLI_MAX & ~3) - headerSize;

      // Decode and validate Compressed Size if the relevant flag
      // is set in Block Flags.
      if ((BUFFER_HEADER[1] & 0x40) != 0x00)
      {
        compressedSizeInHeader = DecoderUtil.decodeVLI(bufStream);

        if (compressedSizeInHeader == 0 || compressedSizeInHeader > compressedSizeLimit)
          throw new CorruptedInputException();

        compressedSizeLimit = compressedSizeInHeader;
      }

      // Decode Uncompressed Size if the relevant flag is set
      // in Block Flags.
      if ((BUFFER_HEADER[1] & 0x80) != 0x00)
        uncompressedSizeInHeader = DecoderUtil.decodeVLI(bufStream);

      // Decode Filter Flags.
      for (int i = 0; i < filterCount; ++i)
      {
        filterIDs[i] = DecoderUtil.decodeVLI(bufStream);

        long filterPropsSize = DecoderUtil.decodeVLI(bufStream);
        filterProps[i] = new byte[(int) filterPropsSize];
        bufStream.read(filterProps[i]);
      }

    } catch (IOException e)
    {
      throw new CorruptedInputException("XZ Block Header is corrupt");
    }

    // Check that the remaining bytes are zero.
    for (int i = bufStream.available(); i > 0; --i)
      bufStream.read();

    // Validate the Blcok Header against the Index when doing
    // random access reading.
    if (unpaddedSizeInIndex != -1)
    {
      // Compressed Data must be at least one byte, so if Block Header
      // and Check alone take as much or more space than the size
      // stored in the Index, the file is corrupt.
      int headerAndCheckSize = headerSize;
      if (headerAndCheckSize >= unpaddedSizeInIndex)
        throw new CorruptedInputException("XZ Index does not match a Block Header");

      // The compressed size calculated from Unpadded Size must
      // match the value stored in the Compressed Size field in
      // the Block Header.
      long compressedSizeFromIndex = unpaddedSizeInIndex - headerAndCheckSize;
      if (compressedSizeFromIndex > compressedSizeLimit
          || (compressedSizeInHeader != -1 && compressedSizeInHeader != compressedSizeFromIndex))
        throw new CorruptedInputException("XZ Index does not match a Block Header");

      // The uncompressed size stored in the Index must match
      // the value stored in the Uncompressed Size field in
      // the Block Header.
      if (uncompressedSizeInHeader != -1 && uncompressedSizeInHeader != uncompressedSizeInIndex)
        throw new CorruptedInputException("XZ Index does not match a Block Header");

      // For further validation, pretend that the values from the Index
      // were stored in the Block Header.
      compressedSizeLimit = compressedSizeFromIndex;
      compressedSizeInHeader = compressedSizeFromIndex;
      uncompressedSizeInHeader = uncompressedSizeInIndex;
    }

    // Check if the Filter IDs are supported, decode
    // the Filter Properties, and check that they are
    // supported by this decoder implementation.
    FilterDecoder[] filters = new FilterDecoder[filterIDs.length];

    for (int i = 0; i < filters.length; ++i)
    {
      if (filterIDs[i] == LZMA2Coder.FILTER_ID)
        filters[i] = new LZMA2Decoder(filterProps[i]);
    }

    // Use an input size counter to calculate
    // the size of the Compressed Data field.
    inCounted = new CountingInputStream(in);

    // Initialize the filter chain.
    filterChain = inCounted;
    for (int i = filters.length - 1; i >= 0; --i) {
      final FilterDecoder filterDecoder = filters[i];
      filterChain = filterDecoder.getInputStream(filterChain);
    }
  }

  private static final byte[] BUFFER_READ = new byte[1];


  public int read() throws IOException
  {
    return read(BUFFER_READ, 0, 1) == -1 ? -1 : (BUFFER_READ[0] & 0xFF);
  }


  public int read(byte[] buf, int off, int len) throws IOException
  {
    if (endReached)
      return -1;

    int ret = filterChain.read(buf, off, len);

    if (ret > 0)
    {
      uncompressedSize += ret;

      // Catch invalid values.
      long compressedSize = inCounted.getSize();
      if (compressedSize < 0 || compressedSize > compressedSizeLimit || uncompressedSize < 0
          || (uncompressedSizeInHeader != -1 && uncompressedSize > uncompressedSizeInHeader))
        throw new CorruptedInputException();

      // Check the Block integrity as soon as possible:
      // - The filter chain shouldn't return less than requested
      // unless it hit the end of the input.
      // - If the uncompressed size is known, we know when there
      // shouldn't be more data coming. We still need to read
      // one byte to let the filter chain catch errors and to
      // let it read end of payload marker(s).
      if (ret < len || uncompressedSize == uncompressedSizeInHeader)
      {
        if (filterChain.read() != -1)
          throw new CorruptedInputException();

        validate();
        endReached = true;
      }
    } else if (ret == -1)
    {
      validate();
      endReached = true;
    }

    return ret;
  }


  private void validate() throws IOException
  {
    long compressedSize = inCounted.getSize();

    // Block Padding bytes must be zeros.
    while ((compressedSize++ & 3) != 0)
      if (inData.readUnsignedByte() != 0x00)
        throw new CorruptedInputException();
  }


  public int available() throws IOException
  {
    return filterChain.available();
  }


  public long getUnpaddedSize()
  {
    return headerSize + inCounted.getSize();
  }


  public long getUncompressedSize()
  {
    return uncompressedSize;
  }
}
