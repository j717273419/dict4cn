/*  Copyright (c) 2010 Xiaoyun Zhu
 * 
 *  Permission is hereby granted, free of charge, to any person obtaining a copy  
 *  of this software and associated documentation files (the "Software"), to deal  
 *  in the Software without restriction, including without limitation the rights  
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell  
 *  copies of the Software, and to permit persons to whom the Software is  
 *  furnished to do so, subject to the following conditions:
 *  
 *  The above copyright notice and this permission notice shall be included in  
 *  all copies or substantial portions of the Software.
 *  
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR  
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,  
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE  
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER  
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,  
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN  
 *  THE SOFTWARE.  
 */
package cn.kk.kkdict.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

public final class ChineseHelper {
  private static final boolean DEBUG = false;
  private static int[]         CODEPOINTS_ST_SIMPLE;
  private static int[]         CODEPOINTS_ST_TRADITIONAL;
  private static int[]         CODEPOINTS_TS_SIMPLE;
  private static int[]         CODEPOINTS_TS_TRADITIONAL;

  /**
   * Converts input text to traditional chinese text
   * 
   * @param input
   *          simplified chinese text
   * @return traditional chinese text
   */
  public static String toTraditionalChinese(final String input) {
    if (ChineseHelper.CODEPOINTS_ST_SIMPLE == null) {
      ChineseHelper.createSimpleTraditionalMap();
    }
    final StringBuilder sb = new StringBuilder();

    int idx;
    for (int i = 0; i < input.length(); i++) {
      final int codePoint = input.codePointAt(i);
      if ((idx = Arrays.binarySearch(ChineseHelper.CODEPOINTS_ST_SIMPLE, codePoint)) >= 0) {
        sb.append(Character.toChars(ChineseHelper.CODEPOINTS_ST_TRADITIONAL[idx]));
      } else {
        sb.append(Character.toChars(codePoint));
      }
    }
    return sb.toString();
  }

  /**
   * Converts input text to simplified chinese text
   * 
   * @param input
   *          traditional chinese text
   * @return simplified chinese text
   */
  public static String toSimplifiedChinese(final String input) {
    if (ChineseHelper.CODEPOINTS_TS_SIMPLE == null) {
      ChineseHelper.createTraditionalSimpleMap();
    }
    final StringBuilder sb = new StringBuilder();

    int idx;
    for (int i = 0; i < input.length(); i++) {
      final int codePoint = input.codePointAt(i);
      if ((idx = Arrays.binarySearch(ChineseHelper.CODEPOINTS_TS_TRADITIONAL, codePoint)) >= 0) {
        sb.append(Character.toChars(ChineseHelper.CODEPOINTS_TS_SIMPLE[idx]));
      } else {
        sb.append(Character.toChars(codePoint));
      }
    }
    return sb.toString();
  }

  public static void main(final String[] args) {
    final Charset cs = Helper.CHARSET_UTF8;
    final ByteBuffer bb = ArrayHelper.borrowByteBufferSmall();
    final byte[] array = cs.encode("丟並乾亂亙亞丢并干乱亘亚").array();
    System.out.println(ArrayHelper.toHexString(array));
    System.arraycopy(array, 0, bb.array(), 0, array.length);
    bb.limit(array.length);
    ChineseHelper.toSimplifiedChinese(bb);
    System.out.println(new String(cs.decode(bb).array()));
    bb.rewind();
    ChineseHelper.toTraditionalChinese(bb);
    System.out.println(new String(cs.decode(bb).array()));
    //
    // int min = 100;
    // int max = 0;
    // String strs = new String(CODEPOINTS_TS_SIMPLE, 0, CODEPOINTS_TS_SIMPLE.length);
    // for (int i = 0; i < strs.length(); i++) {
    // String str = strs.substring(i, i + 1);
    // byte[] b = cs.encode(str).array();
    // min = Math.min(min, b.length);
    // max = Math.max(max, b.length);
    // System.out.println(str + " - unicode: " + str.codePointAt(0) + " - 0x"
    // + Integer.toHexString(str.codePointAt(0)) + " , utf-8: " + CODEPOINTS_TS_SIMPLE[i] + " - "
    // + Helper.toHexString(b));
    // }
    // System.out.println("min: " + min);
    // System.out.println("max: " + max);
    ArrayHelper.giveBack(bb);
  }

  public static final int toSimplifiedChinese(final ByteBuffer bb) {
    if (ChineseHelper.CODEPOINTS_TS_SIMPLE == null) {
      ChineseHelper.createTraditionalSimpleMap();
    }
    return ChineseHelper.decode(bb, ChineseHelper.CODEPOINTS_TS_TRADITIONAL, ChineseHelper.CODEPOINTS_TS_SIMPLE);
  }

  public static final int toTraditionalChinese(final ByteBuffer bb) {
    if (ChineseHelper.CODEPOINTS_ST_SIMPLE == null) {
      ChineseHelper.createSimpleTraditionalMap();
    }
    return ChineseHelper.decode(bb, ChineseHelper.CODEPOINTS_ST_SIMPLE, ChineseHelper.CODEPOINTS_ST_TRADITIONAL);
  }

  private static final int decode(final ByteBuffer bb, final int[] from, final int[] to) {
    // ByteBuffer tmpBB = ArrayHelper.borrowByteBuffer(bb.remaining());
    // ArrayHelper.copyP(bb, tmpBB);
    int mark = bb.position();
    final int limit = bb.limit();
    int idx;
    int i1, i2, i3;
    byte b1, b2, b3;
    while (mark < limit) {
      b1 = bb.get(mark);
      i1 = b1;
      if (i1 >= 0) {
        // 1 byte, 0XXXXXXX
        mark++;
      } else if ((i1 >> 5) == -2) {
        // 2 bytes, 110XXXXX 10XXXXXX
        if ((limit - mark) < 2) {
          return -1;
        }
        mark += 2;
      } else if ((i1 >> 4) == -2) {
        // 3 bytes, 1110XXXX 10XXXXXX 10XXXXXX
        // chinese characters in convertion map are all of 3 bytes length, replace the bytes here
        if ((limit - mark) < 3) {
          return -1;
        }
        b2 = bb.get(mark + 1);
        i2 = b2;
        b3 = bb.get(mark + 2);
        i3 = b3;
        int uc = (i1 << 12) ^ (i2 << 6) ^ (i3 ^ (((byte) 0xE0 << 12) ^ ((byte) 0x80 << 6) ^ ((byte) 0x80 << 0)));

        if ((idx = Arrays.binarySearch(from, uc)) >= 0) {
          uc = to[idx];
          bb.put(mark++, (byte) ((uc >> 12) | 0xE0));
          bb.put(mark++, (byte) (((uc >> 6) & 0x3F) | 0x80));
          bb.put(mark++, (byte) ((uc & 0x3F) | 0x80));
        } else {
          mark += 3;
        }
      } else if ((i1 >> 3) == -2) {
        // 4 bytes, 11110XXX 10XXXXXX 10XXXXXX 10XXXXXX
        if ((limit - mark) < 4) {
          return -1;
        }
        mark += 4;
      } else {
        if (ChineseHelper.DEBUG) {
          System.err.println("err: " + mark + "(0x" + Integer.toHexString(mark) + ") -> " + (char) i1 + ", " + i1 + " (0x" + Integer.toHexString(i1) + "):\n"
              + ArrayHelper.toString(bb));
        }
        mark++;
      }
    }
    // ArrayHelper.giveBack(tmpBB);
    return limit;
  }

  public static char lowSurrogate(final int codePoint) {
    return (char) ((codePoint & 0x3ff) + Character.MIN_LOW_SURROGATE);
  }

  public final static char highSurrogate(final int codePoint) {
    return (char) ((codePoint >>> 10) + (Character.MIN_HIGH_SURROGATE - (Character.MIN_SUPPLEMENTARY_CODE_POINT >>> 10)));
  }

  private static void createTraditionalSimpleMap() {
    final String file = "traditional2simple.txt";
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new InputStreamReader(Helper.findResourceAsStream(file), Helper.CHARSET_UTF8));
      String line;
      while (null != (line = reader.readLine())) {
        final String[] parts = line.split(Helper.SEP_PARTS);
        if (parts.length == 2) {
          final String map = parts[1];
          final int length = map.length();
          if (parts[0].equals("sortedSimple")) {
            ChineseHelper.CODEPOINTS_TS_SIMPLE = new int[length];
            for (int i = 0; i < length; i++) {
              ChineseHelper.CODEPOINTS_TS_SIMPLE[i] = map.codePointAt(i);
            }
          } else if (parts[0].equals("sortedTraditional")) {
            ChineseHelper.CODEPOINTS_TS_TRADITIONAL = new int[length];
            for (int i = 0; i < length; i++) {
              ChineseHelper.CODEPOINTS_TS_TRADITIONAL[i] = map.codePointAt(i);
            }
          }
        }
      }
    } catch (final IOException e) {
      System.out.println("Failed to load " + file + "!");
    } finally {
      Helper.close(reader);
    }
  }

  private static void createSimpleTraditionalMap() {
    final String file = "simple2traditional.txt";
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new InputStreamReader(Helper.findResourceAsStream(file), Helper.CHARSET_UTF8));
      String line;
      while (null != (line = reader.readLine())) {
        final String[] parts = line.split(Helper.SEP_PARTS);
        if (parts.length == 2) {
          final String map = parts[1];
          final int length = map.length();
          if (parts[0].equals("sortedSimple")) {
            ChineseHelper.CODEPOINTS_ST_SIMPLE = new int[length];
            for (int i = 0; i < length; i++) {
              ChineseHelper.CODEPOINTS_ST_SIMPLE[i] = map.codePointAt(i);
            }
          } else if (parts[0].equals("sortedTraditional")) {
            ChineseHelper.CODEPOINTS_ST_TRADITIONAL = new int[length];
            for (int i = 0; i < length; i++) {
              ChineseHelper.CODEPOINTS_ST_TRADITIONAL[i] = map.codePointAt(i);
            }
          }
        }
      }
    } catch (final IOException e) {
      System.out.println("Failed to load " + file + "!");
    } finally {
      Helper.close(reader);
    }
  }

  public static final boolean containsChinese(final String str) {
    for (int i = 0; i < str.length(); i++) {
      // CJK Unified Ideographs 4E00-9FFF Common
      // CJK Unified Ideographs Extension A 3400-4DFF Rare
      // CJK Unified Ideographs Extension B 20000-2A6DF Rare, historic
      // CJK Compatibility Ideographs F900-FAFF Duplicates, unifiable variants, corporate characters
      // CJK Compatibility Ideographs Supplement 2F800-2FA1F Unifiable variants
      final int cp = str.codePointAt(i);
      if (((cp > 0x4e00) && (cp < 0x9fff)) || ((cp > 0x3400) && (cp < 0x4Dff)) || ((cp > 0x20000) && (cp < 0x2a6df)) || ((cp > 0xf900) && (cp < 0xfaff))
          || ((cp > 0x2f800) && (cp < 0x2fa1f))) {
        return true;
      }
    }
    return false;
  }
}
