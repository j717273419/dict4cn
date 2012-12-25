package cn.kk.dict2go;

import java.util.LinkedList;
import java.util.List;

public final class ClientHelper {
  private static final List<Translation> trlsPool = new LinkedList<>();

  public final static Translation borrowTranslation() {
    if (ClientHelper.trlsPool.isEmpty()) {
      return new Translation();
    } else {
      return ClientHelper.trlsPool.remove(0);
    }
  }

  public final static void giveBack(final Translation trl) {
    if (trl != null) {
      ClientHelper.trlsPool.add(trl);
    }
  }

  public static final int compare(final byte[] bs1, int offset1, final int len1, final byte[] bs2, int offset2, final int len2) {
    final int n = offset1 + Math.min(len1, len2);
    int o1 = offset1;
    int o2 = offset2;
    int c1;
    int c2;
    while (o1 < n) {
      c1 = 0xff & bs1[o1++];
      c2 = 0xff & bs2[o2++];
      if (c1 != c2) {
        return c1 - c2;
      }
    }
    return len1 - len2;
  }

  public static final int compareExtended(final byte[] bs1, int offset1, final int len1, final byte[] bs2, int offset2, final int len2) {
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
      c1 = bs1[o1++];
      if (c1 != 0) {
        l1 = o1 - offset1;
        break;
      }
    }
    if (len2 < l1) {
      // starts with
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

  public final static int containsExtended(final byte[] array1, final int offset1, final int len1, final byte[] array2, final int offset2, final int len2) {
    final int end1 = len1 + offset1;
    final int end2 = len2 + offset2;
    int result = -1;
    if (len2 == 0) {
      result = 0;
    } else if (len1 >= len2) {
      byte b;
      int idx = offset2;

      for (int i = offset1; i < end1; i++) {
        b = array1[i];
        if (b == array2[idx]) {
          if (++idx == end2) {
            result = i + 1;
            break;
          }
        } else {
          idx = offset2;
        }
      }
      if (result != -1) {
        boolean endsWith = true;
        for (int i = result; i < end1; i++) {
          if (array1[i] != 0) {
            endsWith = false;
            break;
          }
        }
        if (endsWith) {
          result = Integer.MAX_VALUE;
        }
      }
    }
    return result;
  }

  public final static boolean contains(final byte[] array1, final int offset1, final int len1, final byte[] array2, final int offset2, final int len2) {
    final int end1 = len1 + offset1;
    final int end2 = len2 + offset2;
    if (len2 == 0) {
      return true;
    } else if (len1 >= len2) {
      byte b;
      int idx = offset2;
      for (int i = offset1; i < end1; i++) {
        b = array1[i];
        if (b == array2[idx]) {
          if (++idx == end2) {
            return true;
          }
        } else {
          idx = offset2;
        }
      }
    }
    return false;
  }
}
