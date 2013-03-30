/*
 * DecoderUtil
 *
 * Author: Lasse Collin <lasse.collin@tukaani.org>
 *
 * This file has been put into the public domain.
 * You can do whatever you want with this file.
 */

package org.tukaani.xz.common;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class DecoderUtil extends Util
{
  public static StreamFlags decodeStreamFooter(byte[] buf) throws IOException
  {
    StreamFlags streamFlags = new StreamFlags();

    streamFlags.backwardSize = 0;
    for (int i = 0; i < 4; ++i)
      streamFlags.backwardSize |= (buf[i] & 0xFF) << (i * 8);

    streamFlags.backwardSize = (streamFlags.backwardSize + 1) * 4;

    return streamFlags;
  }


  public static long decodeVLI(InputStream in) throws IOException
  {
    int b = in.read();
    if (b == -1)
      throw new EOFException();

    long num = b & 0x7F;
    int i = 0;

    while ((b & 0x80) != 0x00)
    {
      b = in.read();
      if (b == -1)
        throw new EOFException();

      num |= (long) (b & 0x7F) << (++i * 7);
    }

    return num;
  }
}
