package cn.kk.dict2go;

public final class ClientHelper {
  public static final int compareTo(final byte[] bs1, int offset1, final int len1, final byte[] bs2, int offset2, final int len2) {
    final int n = offset1 + Math.min(len1, len2);
    int o1 = offset1;
    int o2 = offset2;
    int c1;
    int c2;
    int l1 = len1;
    while (o1 < n) {
      c1 = 0xff & bs1[o1++];
      c2 = 0xff & bs2[o2++];
      if (c1 != c2) {
        return c1 - c2;
      }
      if (c1 != 0) {
        l1 = o1 - offset1;
      }
    }
    while (o1 < (offset1 + len1)) {
      c1 = 0xff & bs1[o1++];
      if (c1 != 0) {
        l1 = o1 - offset1;
        break;
      }
    }
    if (len2 < l1) {
      // contains
      return Integer.MAX_VALUE;
    } else {
      return l1 - len2;
    }
  }

  public static final int toInt(byte[] data, int off) {
    return ((0xff & data[off]) << 24) | ((0xff & data[off + 1]) << 16) | ((0xff & data[off + 2]) << 8) | (0xff & data[off + 3]);
  }

  public static final int toShort(byte[] data, int off) {
    return (short) (((0xff & data[off]) << 8) | ((0xff & data[off + 1])));
  }
}
