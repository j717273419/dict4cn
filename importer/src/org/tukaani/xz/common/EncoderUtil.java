/*
 * EncoderUtil
 *
 * Author: Lasse Collin <lasse.collin@tukaani.org>
 *
 * This file has been put into the public domain.
 * You can do whatever you want with this file.
 */

package org.tukaani.xz.common;

import java.io.IOException;
import java.io.OutputStream;

public class EncoderUtil extends Util
{
  public static void encodeVLI(OutputStream out, long num) throws IOException
  {
    while (num >= 0x80)
    {
      out.write((byte) (num | 0x80));
      num >>>= 7;
    }

    out.write((byte) num);
  }

  private final static byte[] BUFFER_CRC = new byte[4];


  public static void writeCRC32(OutputStream out) throws IOException
  {
    out.write(BUFFER_CRC);
  }
}
