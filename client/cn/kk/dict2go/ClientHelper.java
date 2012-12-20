package cn.kk.dict2go;

public final class ClientHelper
{
  public static final int readInt(final byte[] ba, final int offset)
  {
    return ((ba[offset] << 24) + (ba[offset + 1] << 16) + (ba[offset + 2] << 8) + (ba[offset + 3] << 0));
  }


  public static final int compareTo(final byte[] bs1, int offset1, final int len1, final byte[] bs2, int offset2,
      final int len2)
  {
    final int n = offset1 + Math.min(len1, len2);
    int o1 = offset1;
    int o2 = offset2;
    int c1;
    int c2;
    while (o1 < n)
    {
      c1 = 0xffff & bs1[o1++];
      c2 = 0xffff & bs2[o2++];
      if (c1 != c2)
      {
        return c1 - c2;
      }
    }
    if (len2 < len1)
    {
      // contains
      return Integer.MAX_VALUE;
    } else
    {
      return len1 - len2;
    }
  }


  public static final int toInt(byte[] data, int off)
  {
    return (int) ((0xff & data[off]) << 24 | (0xff & data[off + 1]) << 16 | (0xff & data[off + 2]) << 8 | (0xff & data[off + 3]) << 0);
  }


  public static final int toShort(byte[] data, int off)
  {
    return (short) ((0xff & data[off]) << 8 | (0xff & data[off + 1]) << 0);
  }
}
