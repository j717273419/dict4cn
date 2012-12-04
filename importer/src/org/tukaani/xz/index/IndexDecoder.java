/*
 * IndexDecoder
 *
 * Author: Lasse Collin <lasse.collin@tukaani.org>
 *
 * This file has been put into the public domain.
 * You can do whatever you want with this file.
 */

package org.tukaani.xz.index;

import java.io.EOFException;
import java.io.IOException;

import org.tukaani.xz.CorruptedInputException;
import org.tukaani.xz.SeekableInputStream;
import org.tukaani.xz.common.DecoderUtil;
import org.tukaani.xz.common.StreamFlags;

public class IndexDecoder extends IndexBase
{
  private final BlockInfo info = new BlockInfo();

  private final long streamPadding;

  private final int memoryUsage;

  private final long[] unpadded;

  private final long[] uncompressed;

  private long largestBlockSize = 0;

  /**
   * Current position in the arrays. This is initialized to <code>-1</code>
   * because then it is possible to use <code>hasNext()</code> and
   * <code>getNext()</code> to get BlockInfo of the first Block.
   */
  private int pos = -1;


  public IndexDecoder(SeekableInputStream in, StreamFlags streamFooterFlags, long streamPadding, int memoryLimit)
      throws IOException
  {
    super(new CorruptedInputException("XZ Index is corrupt"));
    info.streamFlags = streamFooterFlags;
    this.streamPadding = streamPadding;

    long endPos = in.position() + streamFooterFlags.backwardSize;

    in.read();

    try
    {
      // Number of Records
      long count = DecoderUtil.decodeVLI(in);

      // Calculate approximate memory requirements and check the
      // memory usage limit.
      memoryUsage = 1 + (int) ((16L * count + 1023) / 1024);

      // Allocate the arrays for the Records.
      unpadded = new long[(int) count];
      uncompressed = new long[(int) count];
      int record = 0;

      // Decode the Records.
      for (int i = (int) count; i > 0; --i)
      {
        // Get the next Record.
        long unpaddedSize = DecoderUtil.decodeVLI(in);
        long uncompressedSize = DecoderUtil.decodeVLI(in);

        // Add the new Record.
        unpadded[record] = blocksSum + unpaddedSize;
        uncompressed[record] = uncompressedSum + uncompressedSize;
        ++record;
        super.add(unpaddedSize, uncompressedSize);
        assert record == recordCount;

        // Remember the uncompressed size of the largest Block.
        if (largestBlockSize < uncompressedSize)
          largestBlockSize = uncompressedSize;
      }
    } catch (EOFException e)
    {
      // EOFException is caught just in case a corrupt input causes
      // DecoderUtil.decodeVLI to read too much at once.
      throw new CorruptedInputException("XZ Index is corrupt");
    }

    // Validate that the size of the Index field matches
    // Backward Size.
    int indexPaddingSize = getIndexPaddingSize();
    if (in.position() + indexPaddingSize != endPos)
      throw new CorruptedInputException("XZ Index is corrupt: " + (in.position() + indexPaddingSize) + ", but: "
          + endPos);

    // Index Padding
    while (indexPaddingSize-- > 0)
      in.read();
  }


  public BlockInfo locate(long target)
  {
    assert target < uncompressedSum;

    int left = 0;
    int right = unpadded.length - 1;

    while (left < right)
    {
      int i = left + (right - left) / 2;

      if (uncompressed[i] <= target)
        left = i + 1;
      else
        right = i;
    }

    pos = left;
    return getInfo();
  }


  public int getMemoryUsage()
  {
    return memoryUsage;
  }


  public long getStreamAndPaddingSize()
  {
    return getStreamSize() + streamPadding;
  }


  public long getUncompressedSize()
  {
    return uncompressedSum;
  }


  public long getLargestBlockSize()
  {
    return largestBlockSize;
  }


  public boolean hasNext()
  {
    return pos + 1 < recordCount;
  }


  public BlockInfo getNext()
  {
    ++pos;
    return getInfo();
  }


  private BlockInfo getInfo()
  {
    if (pos == 0)
    {
      info.compressedOffset = 0;
      info.uncompressedOffset = 0;
    } else
    {
      info.compressedOffset = (unpadded[pos - 1] + 3) & ~3;
      info.uncompressedOffset = uncompressed[pos - 1];
    }

    info.unpaddedSize = unpadded[pos] - info.compressedOffset;
    info.uncompressedSize = uncompressed[pos] - info.uncompressedOffset;

    info.compressedOffset += DecoderUtil.STREAM_HEADER_SIZE;
    return info;
  }
}
