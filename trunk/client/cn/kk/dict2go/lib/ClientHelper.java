package cn.kk.dict2go.lib;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public final class ClientHelper {
  private static final List<Translation> trlsPool      = new LinkedList<>();
  private static final int               MAX_POOL_SIZE = 200;

  public final static Translation borrowTranslation() {
    if (ClientHelper.trlsPool.isEmpty()) {
      return new Translation();
    } else {
      return ClientHelper.trlsPool.remove(0);
    }
  }

  public final static void giveBack(final Translation trl) {
    if (trl != null) {
      if (ClientHelper.trlsPool.size() < ClientHelper.MAX_POOL_SIZE) {
        ClientHelper.trlsPool.add(trl);
      }
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

  /**
   * 
   * @param data
   * @param offset
   * @param len
   *          relative
   * @param human
   * @return
   */
  public static String toHexString(final byte[] data, final int offset, final int len, boolean human) {
    final StringBuffer sb = new StringBuffer(len * 2);
    boolean first = true;
    for (int idx = offset; idx < (offset + len); idx++) {
      if (first) {
        first = false;
      } else if (human) {
        sb.append(' ');
      }
      final byte b = data[idx];
      final int i = b & 0xff;
      if (i < 0x10) {
        sb.append('0');
      }
      sb.append(Integer.toHexString(i));
    }
    return sb.toString();
  }

  public static void close(Closeable... closables) {
    for (Closeable c : closables) {
      if (c != null) {
        try {
          c.close();
        } catch (IOException e) {
          // silent
        }
      }
    }
  }

  private static final SilentStringDecoder STRING_DECODER_SILENT = new SilentStringDecoder(Charset.forName("UTF-8"));

  public static final String toString(final byte[] array, final int offset, final int len) {
    return new String(ClientHelper.STRING_DECODER_SILENT.decode(array, offset, len));
  }

  public final static class SilentStringDecoder {
    public final String          name;

    private final CharsetDecoder cd;

    public SilentStringDecoder(final Charset cs) {
      this.cd = cs.newDecoder().onMalformedInput(CodingErrorAction.IGNORE).onUnmappableCharacter(CodingErrorAction.IGNORE);
      this.name = cs.name();
    }

    public final char[] decode(final byte[] ba, final int off, final int len) {
      final int en = (int) (len * (double) this.cd.maxCharsPerByte());
      final char[] ca = new char[en];
      if (len == 0) {
        return ca;
      }
      this.cd.reset();
      final ByteBuffer bb = ByteBuffer.wrap(ba, off, len);
      final CharBuffer cb = CharBuffer.wrap(ca);
      try {
        CoderResult cr = this.cd.decode(bb, cb, true);
        if (!cr.isUnderflow()) {
          cr.throwException();
        }
        cr = this.cd.flush(cb);
        if (!cr.isUnderflow()) {
          cr.throwException();
        }
      } catch (final CharacterCodingException x) {
        // Substitution is always enabled,
        // so this shouldn't happen
        throw new Error(x);
      }
      return ClientHelper.safeTrim(ca, cb.position());
    }

  }

  public final static char[] safeTrim(final char[] ca, final int len) {
    if (len == ca.length) {
      return ca;
    } else {
      return Arrays.copyOf(ca, len);
    }
  }

  /**
   * 
   * @param bs1
   * @param offset1
   * @param len1
   *          relative length
   * @param bs2
   * @param offset2
   * @param len2
   *          relative length
   * @return
   */
  public static final int compareTo(final byte[] bs1, int offset1, final int len1, final byte[] bs2, int offset2, final int len2) {
    final int n = offset1 + Math.min(len1, len2);
    int o1 = offset1;
    int o2 = offset2;
    while (o1 < n) {
      final byte c1 = bs1[o1++];
      final byte c2 = bs2[o2++];
      if (c1 != c2) {
        return c1 - c2;
      }
    }
    return len1 - len2;
  }

  public static boolean isNotEmptyOrNull(String text) {
    return (text != null) && (text.length() != 0);
  }

  public static int indexOf(int[] lngs, int lng) {
    for (int i = 0; i < lngs.length; i++) {
      if (lng == lngs[i]) {
        return i;
      }
    }
    return -1;
  }
}
