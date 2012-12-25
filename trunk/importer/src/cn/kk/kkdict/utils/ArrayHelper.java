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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public final class ArrayHelper {
  public static final int[]              EMPTY_INTS                = new int[0];

  public static final byte[]             EMPTY_BYTES               = new byte[0];

  private static final List<ByteBuffer>  byteBuffersPoolLarge      = new ArrayList<>();

  private static final List<ByteBuffer>  byteBuffersPoolMedium     = new ArrayList<>();

  private static final List<ByteBuffer>  byteBuffersPoolNormal     = new ArrayList<>();

  private static final List<ByteBuffer>  byteBuffersPoolSmall      = new ArrayList<>();

  private static final List<ByteBuffer>  byteBuffersPoolVeryLarge  = new ArrayList<>();

  public static final Comparator<byte[]> COMPARATOR_BYTE_ARRAY     = new Comparator<byte[]>() {
                                                                     @Override
                                                                     public int compare(final byte[] o1, final byte[] o2) {
                                                                       return ArrayHelper.compareTo(o1, 0, o1.length, o2, 0, o2.length);
                                                                     }
                                                                   };

  private static final int               MAX_BUFFER_SIZE           = 10;

  public static final int                MAX_LINE_BYTES_LARGE      = 1024 * 64;

  // public static final int MAX_LINE_BYTES_MEDIUM = 1024 * 32;
  public static final int                MAX_LINE_BYTES_MEDIUM     = 1024 * 32;

  public static final int                MAX_LINE_BYTES_NORMAL     = 1024 * 4;

  public static final int                MAX_LINE_BYTES_SMALL      = 1024;

  public static final int                MAX_LINE_BYTES_VERY_LARGE = 1024 * 280;

  public static boolean                  WARN                      = true;

  public static final int compareTo(final ByteBuffer bb1, final ByteBuffer bb2) {
    return ArrayHelper.compareTo(bb1.array(), 0, bb1.limit(), bb2.array(), 0, bb2.limit());
  }

  public static final int compareToP(final ByteBuffer bb1, final ByteBuffer bb2) {
    final int offset1 = bb1.position();
    final int offset2 = bb2.position();
    final byte[] array1 = bb1.array();
    final byte[] array2 = bb2.array();
    final int len1 = bb1.remaining();
    final int len2 = bb2.remaining();
    return ArrayHelper.compareTo(array1, offset1, len1, array2, offset2, len2);
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

  public final static boolean containsP(final ByteBuffer bb1, final ByteBuffer bb2) {
    return ArrayHelper.contains(bb1.array(), bb1.position(), bb1.limit(), bb2.array(), bb2.position(), bb2.limit());
  }

  public final static boolean contains(final ByteBuffer bb1, final ByteBuffer bb2) {
    return ArrayHelper.contains(bb1.array(), 0, bb1.limit(), bb2.array(), 0, bb2.limit());
  }

  /**
   * 
   * @param text
   * @param offset1
   * @param end1
   *          absolute
   * @param s
   * @return
   */
  public final static boolean contains(final byte[] array1, final int offset1, final int end1, final byte[] array2, final int offset2, final int end2) {
    final int len1 = end1 - offset1;
    final int len2 = end2 - offset2;
    if (len2 == 0) {
      return true;
    } else if (len1 >= len2) {
      byte b;
      int idx = offset2;
      for (int i = offset1; i < end1; i++) {
        b = array1[i];
        if (b == array2[idx]) {
          if (++idx >= end2) {
            return true;
          }
        } else {
          idx = offset2;
        }
      }
    }
    return false;
  }

  public final static boolean contains(final byte[] array1, final int offset1, final int end2, final byte[] array2) {
    return ArrayHelper.contains(array1, offset1, end2, array2, 0, array2.length);
  }

  public final static int copy(final ByteBuffer from, final ByteBuffer to) {
    final int len = from.limit();
    return ArrayHelper.copy(from, 0, to, 0, len);
  }

  public final static int copy(final ByteBuffer from, final int offset1, final ByteBuffer to, final int offset2, final int len) {
    System.arraycopy(from.array(), offset1, to.array(), offset2, len);
    to.limit(offset2 + len);
    return len;
  }

  public final static int copyP(final ByteBuffer from, final ByteBuffer to) {
    final int offset1 = from.position();
    final int offset2 = to.position();
    // TODO check this
    // final int len = Math.min(to.remaining(), from.limit() - offset1);
    final int len = from.limit() - offset1;
    return ArrayHelper.copy(from, offset1, to, offset2, len);
  }

  public final static int count(final byte[] array1, final int offset1, final int end1, final byte[] array2) {
    return ArrayHelper.count(array1, offset1, end1, array2, 0, array2.length);
  }

  public final static int count(final byte[] array1, final int offset1, final int end1, final byte[] array2, final int offset2, final int end2) {
    final int len1 = end1 - offset1;
    final int len2 = end2 - offset2;
    int count = 0;
    if ((len2 > 0) && (len1 >= len2)) {
      byte b;
      int idx = offset2;
      for (int i = offset1; i < end1; i++) {
        b = array1[i];
        if (b == array2[idx]) {
          if (++idx >= end2) {
            count++;
            idx = offset2;
          }
        } else {
          idx = offset2;
        }
      }
    }
    return count;
  }

  public final static int count(final ByteBuffer bb, final byte[] text) {
    final int endIdx = bb.limit();
    final byte[] array = bb.array();
    return ArrayHelper.count(array, 0, endIdx, text, 0, text.length);
  }

  public final static int countP(final ByteBuffer bb, final byte[] text) {
    final int endIdx = bb.limit();
    final byte[] array = bb.array();
    return ArrayHelper.count(array, bb.position(), endIdx, text, 0, text.length);
  }

  public final static int count(final ByteBuffer bb1, final ByteBuffer bb2) {
    final int end1 = bb1.limit();
    final int end2 = bb2.limit();
    final byte[] array1 = bb1.array();
    final byte[] array2 = bb2.array();
    return ArrayHelper.count(array1, 0, end1, array2, 0, end2);
  }

  public final static int countP(final ByteBuffer bb1, final ByteBuffer bb2) {
    final int end1 = bb1.limit();
    final int end2 = bb2.limit();
    final int offset1 = bb1.position();
    final int offset2 = bb2.position();
    final byte[] array1 = bb1.array();
    final byte[] array2 = bb2.array();
    return ArrayHelper.count(array1, offset1, end1, array2, offset2, end2);
  }

  public static void debug(final byte[] data) {
    if (data.length == 1) {
      System.out.println("byte: " + data[0]);
    }
    if (data.length == 2) {
      System.out.println("short (le): " + (((data[1] & 0xFF) << 8) | (data[0] & 0xFF)));
      System.out.println("short (be): " + (((data[1] & 0xFF) << 8) | (data[0] & 0xFF)));
    }
    if (data.length == 4) {
      System.out.println("int (le): " + (((data[3] & 0xFF) << 24) | ((data[2] & 0xFF) << 16) | ((data[1] & 0xFF) << 8) | (data[0] & 0xFF)));
      System.out.println("int (be): " + (((data[0] & 0xFF) << 24) | ((data[1] & 0xFF) << 16) | ((data[2] & 0xFF) << 8) | (data[3] & 0xFF)));
    }
    if (data.length < 1024) {
      System.out.println("BYTES: " + ArrayHelper.toHexString(data));
    }
    try {
      System.out.println("ISO-8859-1: " + new String(data, "ISO-8859-1"));
      System.out.println("UTF-8: " + new String(data, "UTF-8"));
      System.out.println("UTF-16LE: " + new String(data, "UTF-16LE"));
      System.out.println("UTF-16BE: " + new String(data, "UTF-16BE"));
      System.out.println("UTF-32LE: " + new String(data, "UTF-32LE"));
      System.out.println("UTF-32BE: " + new String(data, "UTF-32BE"));
      System.out.println("Big5: " + new String(data, "Big5"));
      System.out.println("GB18030: " + new String(data, "GB18030"));
      System.out.println("GB2312: " + new String(data, "GB2312"));
      System.out.println("GBK: " + new String(data, "GBK"));
    } catch (final UnsupportedEncodingException e) {
      // silent
    }
  }

  public final static boolean equals(final byte[] array1, final int start1, final byte[] array2, final int start2, final int len) {
    if (((start1 + len) > array1.length) || ((start2 + len) > array2.length)) {
      return false;
    }
    for (int i = 0; i < len; i++) {
      if (array1[start1 + i] != array2[start2 + i]) {
        return false;
      }
    }
    return true;
  }

  public final static boolean equals(final byte[] array1, final int offset1, final int end1, final byte[] array2, final int offset2, final int end2) {
    final int len1 = end1 - offset1;
    final int len2 = end2 - offset2;
    if (len1 != len2) {
      return false;
    } else {
      return ArrayHelper.equals(array1, offset1, array2, offset2, len1);
    }
  }

  public final static boolean equals(final byte[] array1, final int offset1, final byte[] array2) {
    return ArrayHelper.equals(array1, offset1, array2, 0, array2.length);
  }

  public final static boolean equals(final ByteBuffer bb1, final ByteBuffer bb2) {
    final byte[] array1 = bb1.array();
    final byte[] array2 = bb2.array();
    final int end1 = bb1.limit();
    final int end2 = bb2.limit();
    return ArrayHelper.equals(array1, 0, end1, array2, 0, end2);
  }

  public final static boolean equalsP(final ByteBuffer bb1, final ByteBuffer bb2) {
    final byte[] array1 = bb1.array();
    final byte[] array2 = bb2.array();
    final int offset1 = bb1.position();
    final int offset2 = bb2.position();
    final int end1 = bb1.limit();
    final int end2 = bb2.limit();
    return ArrayHelper.equals(array1, offset1, end1, array2, offset2, end2);
  }

  public final static int findTrimmedEndIdx(final ByteBuffer bb) {
    return ArrayHelper.findTrimmedEndIdx(bb.array(), 0, bb.limit());
  }

  public final static int findTrimmedEndIdxP(final ByteBuffer bb) {
    final int offset = bb.position();
    final int limit = bb.limit();
    final byte[] array = bb.array();
    return ArrayHelper.findTrimmedEndIdx(array, offset, limit);
  }

  public static int findTrimmedEndIdx(final byte[] array, final int offset, final int limit) {
    byte b;
    for (int i = limit - 1; i >= offset; i--) {
      b = array[i];
      if ((b == ' ') || (b == '\t') || (b == Helper.SEP_NEWLINE_CHAR) || (b == '\r') || (b == '\0')) {
        continue;
      } else {
        return i + 1;
      }
    }
    return offset;
  }

  /**
   * 
   * @param bb
   * @return absolute idx
   */
  public final static int findTrimmedOffset(final ByteBuffer bb) {
    return ArrayHelper.findTrimmedOffset(bb.array(), 0, bb.limit());
  }

  /**
   * 
   * @param bb
   * @return absolute offset idx
   */
  public final static int findTrimmedOffsetP(final ByteBuffer bb) {
    final int l = bb.limit();
    final int offset = bb.position();
    final byte[] array = bb.array();
    return ArrayHelper.findTrimmedOffset(array, offset, l);
  }

  /**
   * 
   * @param array
   * @param offset
   * @param endIdx
   * @return absolute idx
   */
  public static int findTrimmedOffset(final byte[] array, final int offset, final int endIdx) {
    byte b;
    for (int i = offset; i < endIdx; i++) {
      b = array[i];
      if ((b == ' ') || (b == '\t') || (b == Helper.SEP_NEWLINE_CHAR) || (b == '\r') || (b == '\0')) {
        continue;
      } else {
        return i;
      }
    }
    return endIdx;
  }

  public static final ByteBuffer borrowByteBuffer(final int capacity) {
    final ByteBuffer bb;
    if (capacity >= ArrayHelper.MAX_LINE_BYTES_VERY_LARGE) {
      bb = ByteBuffer.allocate(capacity);
    } else if (capacity >= ArrayHelper.MAX_LINE_BYTES_LARGE) {
      bb = ArrayHelper.borrowByteBufferVeryLarge();
    } else if (capacity >= ArrayHelper.MAX_LINE_BYTES_MEDIUM) {
      bb = ArrayHelper.borrowByteBufferLarge();
    } else if (capacity >= ArrayHelper.MAX_LINE_BYTES_NORMAL) {
      bb = ArrayHelper.borrowByteBufferMedium();
    } else if (capacity >= ArrayHelper.MAX_LINE_BYTES_SMALL) {
      bb = ArrayHelper.borrowByteBufferNormal();
    } else {
      bb = ArrayHelper.borrowByteBufferSmall();
    }
    bb.clear();
    return bb;
  }

  public final static ByteBuffer borrowByteBufferLarge() {
    if (ArrayHelper.byteBuffersPoolLarge.isEmpty()) {
      return ByteBuffer.allocate(ArrayHelper.MAX_LINE_BYTES_LARGE);
    } else {
      return ArrayHelper.byteBuffersPoolLarge.remove(0);
    }
  }

  public final static ByteBuffer borrowByteBufferMedium() {
    if (ArrayHelper.byteBuffersPoolMedium.isEmpty()) {
      return ByteBuffer.allocate(ArrayHelper.MAX_LINE_BYTES_MEDIUM);
    } else {
      return ArrayHelper.byteBuffersPoolMedium.remove(0);
    }
  }

  public final static ByteBuffer borrowByteBufferSmall() {
    if (ArrayHelper.byteBuffersPoolSmall.isEmpty()) {
      return ByteBuffer.allocate(ArrayHelper.MAX_LINE_BYTES_SMALL);
    } else {
      return ArrayHelper.byteBuffersPoolSmall.remove(0);
    }
  }

  public final static ByteBuffer borrowByteBufferNormal() {
    if (ArrayHelper.byteBuffersPoolNormal.isEmpty()) {
      return ByteBuffer.allocate(ArrayHelper.MAX_LINE_BYTES_NORMAL);
    } else {
      return ArrayHelper.byteBuffersPoolNormal.remove(0);
    }
  }

  public final static ByteBuffer borrowByteBufferVeryLarge() {
    if (ArrayHelper.byteBuffersPoolVeryLarge.isEmpty()) {
      return ByteBuffer.allocate(ArrayHelper.MAX_LINE_BYTES_VERY_LARGE);
    } else {
      return ArrayHelper.byteBuffersPoolVeryLarge.remove(0);
    }
  }

  public final static void giveBack(final ByteBuffer bb) {
    if (bb != null) {
      final int capacity = bb.capacity();
      if (capacity == ArrayHelper.MAX_LINE_BYTES_SMALL) {
        if (!ArrayHelper.byteBuffersPoolSmall.contains(bb) && (ArrayHelper.byteBuffersPoolSmall.size() < ArrayHelper.MAX_BUFFER_SIZE)) {
          ArrayHelper.byteBuffersPoolSmall.add(bb);
        }
      } else if (capacity == ArrayHelper.MAX_LINE_BYTES_MEDIUM) {
        if (!ArrayHelper.byteBuffersPoolMedium.contains(bb) && (ArrayHelper.byteBuffersPoolMedium.size() < ArrayHelper.MAX_BUFFER_SIZE)) {
          ArrayHelper.byteBuffersPoolMedium.add(bb);
        }
      } else if (capacity == ArrayHelper.MAX_LINE_BYTES_NORMAL) {
        if (!ArrayHelper.byteBuffersPoolNormal.contains(bb) && (ArrayHelper.byteBuffersPoolNormal.size() < ArrayHelper.MAX_BUFFER_SIZE)) {
          ArrayHelper.byteBuffersPoolNormal.add(bb);
        }
      } else if (capacity == ArrayHelper.MAX_LINE_BYTES_LARGE) {
        if (!ArrayHelper.byteBuffersPoolLarge.contains(bb) && (ArrayHelper.byteBuffersPoolLarge.size() < ArrayHelper.MAX_BUFFER_SIZE)) {
          ArrayHelper.byteBuffersPoolLarge.add(bb);
        }
      } else if (capacity == ArrayHelper.MAX_LINE_BYTES_VERY_LARGE) {
        if (!ArrayHelper.byteBuffersPoolLarge.contains(bb) && (ArrayHelper.byteBuffersPoolLarge.size() < 2)) {
          ArrayHelper.byteBuffersPoolLarge.add(bb);
        }
      } else {
        throw new IllegalArgumentException("Non compatible byte buffer size: " + capacity);
      }
      bb.clear();
    }
  }

  /**
   * 
   * @param bb
   * @param offset
   *          absolute
   * @param limit
   *          absolute
   * @param b
   * @return absolute index
   */
  public static final int indexOf(final byte[] bb, final int offset, final int limit, final byte b) {
    for (int i = offset; i < limit; i++) {
      if (bb[i] == b) {
        return i;
      }
    }
    return -1;
  }

  /**
   * 
   * @param text
   * @param offset
   * @param len
   *          relative
   * @param s
   * @return absolute index
   */
  public final static int indexOf(final byte[] text, final int offset, final int len, final byte[] s) {
    return ArrayHelper.indexOf(text, offset, len, s, 0, s.length);
  }

  /**
   * 
   * @param text
   * @param offset
   * @param len1
   *          relative
   * @param s
   * @param offset2
   * @param len2
   *          relative
   * @return absolute index
   */
  public final static int indexOf(final byte[] text, final int offset, final int len1, final byte[] s, final int offset2, final int len2) {
    if (len1 >= len2) {
      final int limit = offset + len1;
      int idx = 0;
      byte b;
      for (int i = offset; i < limit; i++) {
        b = text[i];
        if (b == s[offset2 + idx]) {
          if (++idx == len2) {
            return (i - len2) + 1;
          }
        } else {
          idx = 0;
        }
      }
    }
    return -1;
  }

  public static final int indexOf(final ByteBuffer bb, final byte b) {
    return ArrayHelper.indexOf(bb.array(), 0, bb.limit(), b);
  }

  public final static int indexOf(final char[][] pairs, final char c) {
    int i = 0;
    for (final char[] pair : pairs) {
      if (c == pair[0]) {
        return i;
      }
      i++;
    }
    return -1;
  }

  public static final int indexOfP(final ByteBuffer bb, final byte b) {
    return ArrayHelper.indexOf(bb.array(), bb.position(), bb.remaining(), b);
  }

  public static final boolean isPredessorEquals(final ByteBuffer bb1, final ByteBuffer bb2) {
    final int l1 = bb1.limit();
    final int l2 = bb2.limit();
    return ArrayHelper.isPredessorEquals(bb1, 0, l1, bb2, 0, l2);
  }

  public static boolean isPredessorEqualsP(final ByteBuffer bb1, final ByteBuffer bb2) {
    return ArrayHelper.isPredessorEquals(bb1, bb1.position(), bb1.remaining(), bb2, bb2.position(), bb2.remaining());
  }

  public static final boolean isPredessorEquals(final ByteBuffer bb1, final int offset1, final int l1, final ByteBuffer bb2, final int offset2, final int l2) {
    // return v1.compareToIgnoreCase(v2) <= 0;
    if (l1 <= 0) {
      return false;
    } else if (l2 <= 0) {
      return true;
    }
    return ArrayHelper.compareTo(bb1.array(), offset1, l1, bb2.array(), offset2, l2) <= 0;
  }

  /**
   * 
   * @param bb1
   * @param bb2
   * @return
   */
  public static final boolean isSuccessor(final ByteBuffer bb1, final ByteBuffer bb2) {
    final int l1 = bb1.limit();
    final int l2 = bb2.limit();
    return ArrayHelper.isSuccessor(bb1, 0, l1, bb2, 0, l2);
  }

  public static final boolean isSuccessorP(final ByteBuffer bb1, final ByteBuffer bb2) {
    return ArrayHelper.isSuccessor(bb1, bb1.position(), bb1.remaining(), bb2, bb2.position(), bb2.remaining());
  }

  /**
   * 
   * @param bb1
   * @param offset1
   * @param l1
   *          relative length
   * @param bb2
   * @param offset2
   * @param l2
   *          relative length
   * @return
   */
  public static final boolean isSuccessor(final ByteBuffer bb1, final int offset1, final int l1, final ByteBuffer bb2, final int offset2, final int l2) {
    // return v1.compareToIgnoreCase(v2) > 0;
    if (l1 <= 0) {
      return true;
    } else if (l2 <= 0) {
      return false;
    }
    return ArrayHelper.compareTo(bb1.array(), offset1, l1, bb2.array(), offset2, l2) > 0;
  }

  public final static int lastIndexOf(final byte[] text, final int offset, final int limit, final byte[] s) {
    final int len = s.length;
    if (limit >= len) {
      final int size = (limit - len) + 1;
      for (int i = size - 1; i >= offset; i--) {
        if (ArrayHelper.equals(text, i, s, 0, len)) {
          return i;
        }
      }
    }
    return -1;
  }

  public static void main(final String[] args) {
    final byte[] text = "      <namespace key=\"-2\" case=\"first-letter\">Medėjė</namespace><namespace key=\"-2\" case=\"first-letter\">Medėjė2</namespace>"
        .getBytes(Helper.CHARSET_UTF8);
    System.out.println(ArrayHelper.contains(text, 0, text.length, "</namespace>".getBytes(Helper.CHARSET_UTF8)));
    System.out.println(ArrayHelper.substringBetween(text, 0, text.length, "\">".getBytes(Helper.CHARSET_UTF8), "</namespace>".getBytes(Helper.CHARSET_UTF8)));
    final ByteBuffer bbBuffer = ByteBuffer.allocate(ArrayHelper.MAX_LINE_BYTES_SMALL);
    ArrayHelper.substringBetween(text, 0, text.length, "\">".getBytes(Helper.CHARSET_UTF8), "</namespace>".getBytes(Helper.CHARSET_UTF8), bbBuffer);
    System.out.println(ArrayHelper.toString(bbBuffer));

    ArrayHelper.substringBetweenLast(text, 0, text.length, "\">".getBytes(Helper.CHARSET_UTF8), "</namespace>".getBytes(Helper.CHARSET_UTF8), bbBuffer);
    System.out.println(ArrayHelper.toString(bbBuffer));

    System.out
        .println(ArrayHelper.substringBetweenLast(text, 0, text.length, "\">".getBytes(Helper.CHARSET_UTF8), "</namespace>".getBytes(Helper.CHARSET_UTF8)));
    System.out.println(ArrayHelper.indexOf(text, 1, text.length - 1, "namespace".getBytes(Helper.CHARSET_UTF8), 0, 4));
    bbBuffer.put((byte) 1);
    bbBuffer.put((byte) 2);
    bbBuffer.put((byte) 3);
    bbBuffer.put((byte) 4);
    bbBuffer.put((byte) 5);
    bbBuffer.put((byte) 6);
    bbBuffer.put((byte) 7);
    bbBuffer.put((byte) 8);
    ArrayHelper.substring(bbBuffer, 1);
    System.out.println(ArrayHelper.toHexString(bbBuffer));
  }

  /**
   * 
   * @param in
   * @param bb
   * @return line without Helper.SEP_NEWLINE_CHAR-character
   * @throws IOException
   */
  public static final int readLine(final BufferedInputStream in, final ByteBuffer bb) throws IOException {
    int b;
    bb.clear();
    int len = 0;
    while (-1 != (b = in.read())) {
      len++;
      if ((b != Helper.SEP_NEWLINE_CHAR) && (b != '\r')) {
        if (bb.hasRemaining()) {
          // skip beyond max line size
          bb.put((byte) b);
        }
      } else {
        bb.limit(bb.position()).rewind();
        if (ArrayHelper.WARN && (--len > bb.capacity())) {
          System.err.println("跳过超长部分：总" + len + "字符，跳过" + (len - bb.limit()) + "字符");
        }
        return bb.limit();
      }
    }
    if (bb.position() != 0) {
      bb.limit(bb.position());
      bb.rewind();
      if (len > bb.capacity()) {
        System.err.println("跳过超长部分：总" + len + "字符，跳过" + (len - bb.limit()) + "字符");
      }
      return bb.limit();
    }
    return -1;
  }

  /**
   * 
   * @param fileBB
   * @param lineBB
   * @return line length without Helper.SEP_NEWLINE_CHAR-character, -1 if eof
   * @throws IOException
   */
  public static int readLine(final ByteBuffer fileBB, final ByteBuffer lineBB) {
    int b;
    lineBB.clear();
    int len = 0;
    while (fileBB.hasRemaining()) {
      b = fileBB.get();
      len++;
      if (b == '\r') {
        if (fileBB.hasRemaining()) {
          b = fileBB.get();
        } else {
          break;
        }
        if (b != Helper.SEP_NEWLINE_CHAR) {
          if (lineBB.hasRemaining()) {
            // skip beyond max line size
            lineBB.put((byte) '\r');
          }
        }
      }
      if (b != Helper.SEP_NEWLINE_CHAR) {
        if (lineBB.hasRemaining()) {
          // skip beyond max line size
          lineBB.put((byte) b);
        }
      } else {
        b = lineBB.position();
        lineBB.limit(b);
        lineBB.rewind();
        if (ArrayHelper.WARN && (--len > lineBB.limit())) {
          System.err.println("跳过超长部分：总" + len + "字符，跳过" + (len - lineBB.limit()) + "字符");
        }
        return b;
      }
    }
    if ((b = lineBB.position()) != 0) {
      lineBB.limit(b);
      lineBB.rewind();
      if (len > lineBB.limit()) {
        System.err.println("跳过超长部分：总" + len + "字符，跳过" + (len - lineBB.limit()) + "字符");
      }
      return b;
    }
    return -1;
  }

  /**
   * 
   * @param in
   * @param bb
   * @return line without Helper.SEP_NEWLINE_CHAR-character
   * @throws IOException
   */
  public static final int readLineTrimmed(final BufferedInputStream in, final ByteBuffer bb) throws IOException {
    int b;
    bb.clear();
    int len = 0;
    boolean valid = false;
    int lastValid = 0;
    while (-1 != (b = in.read())) {
      len++;
      if ((b != Helper.SEP_NEWLINE_CHAR) && (b != '\r')) {
        final boolean empty = (b == ' ') || (b == '\t') || (b == '\0');
        if (!empty) {
          valid = true;
        }
        if (valid) {
          if (bb.hasRemaining()) {
            // skip beyond max line size
            bb.put((byte) b);
          }
          if (!empty) {
            lastValid = bb.position();
          }
        }
      } else {
        bb.limit(lastValid).rewind();
        if (ArrayHelper.WARN && (--len > bb.capacity())) {
          System.err.println("跳过超长部分：总" + len + "字符，跳过" + (len - bb.limit()) + "字符");
        }
        return lastValid;
      }
    }
    if (lastValid != 0) {
      bb.limit(lastValid).rewind();
      if (len > bb.capacity()) {
        System.err.println("跳过超长部分：总" + len + "字符，跳过" + (len - bb.limit()) + "字符");
      }
      return lastValid;
    }
    return -1;
  }

  public static final boolean startsWith(final byte[] array, final byte[] prefix) {
    final int l1 = array.length;
    final int l2 = prefix.length;
    return ArrayHelper.startsWith(array, 0, l1, prefix, 0, l2);
  }

  /**
   * 
   * @param array
   * @param limit1
   * @param prefix
   * @param limit2
   * @return
   */
  public static final boolean startsWith(final byte[] array, final int offset1, final int limit1, final byte[] prefix, final int offset2, final int limit2) {
    final int len1 = limit1 - offset1;
    final int len2 = limit2 - offset2;
    if (len1 >= len2) {
      for (int i = 0; i < len2; i++) {
        if (array[offset1 + i] != prefix[offset2 + i]) {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  /**
   * Stores substring in bb
   * 
   * @param bb
   * @param startIdx
   * @return new bb limit
   */
  public static final int substring(final ByteBuffer bb, final int startIdx) {
    final int limit = bb.limit();
    if (startIdx >= limit) {
      bb.limit(0);
      return 0;
    } else {
      final byte[] array = bb.array();
      int i = 0;
      int j = startIdx;
      while (j < limit) {
        array[i] = array[j];
        j++;
        i++;
      }
      bb.limit(limit - startIdx);
      return bb.limit();
    }
  }

  public final static String substringBetween(final byte[] text, final int offset, final int limit, final byte[] start, final byte[] end) {
    return ArrayHelper.substringBetween(text, offset, limit, start, end, true);
  }

  public final static String substringBetween(final byte[] text, final int offset, final int limit, final byte[] start, final byte[] end, final boolean trim) {
    int nStart = ArrayHelper.indexOf(text, offset, limit - offset, start);
    final int s = nStart + start.length + 1;
    final int nEnd = ArrayHelper.indexOf(text, s, limit - s, end);
    if ((nStart != -1) && (nEnd > nStart)) {
      nStart += start.length;
      final String str = new String(text, nStart, nEnd - nStart, Helper.CHARSET_UTF8);
      if (trim) {
        return str.trim();
      } else {
        return str;
      }
    } else {
      return null;
    }
  }

  /**
   * 
   * @param text
   * @param offset
   * @param limit
   * @param start
   * @param end
   * @param trim
   * @param bb
   * @return new bb limit
   */
  public final static int substringBetween(final byte[] text, final int offset, final int limit, final byte[] start, final byte[] end, final boolean trim,
      final ByteBuffer bb) {
    int nStart = ArrayHelper.indexOf(text, offset, limit - offset, start);
    final int s = nStart + start.length;
    int nEnd = ArrayHelper.indexOf(text, s, limit - s, end);
    if ((nStart != -1) && (nEnd > nStart)) {
      nStart += start.length;
      if (trim) {
        boolean empty = true;
        byte c;
        int i;
        for (i = nStart; i < nEnd; i++) {
          c = text[i];
          if ((c != ' ') && (c != '\t')) {
            nStart = i;
            empty = false;
            break;
          }
        }
        if (empty) {
          nEnd = -1;
        } else {
          for (i = nEnd; i >= nStart; i--) {
            c = text[i];
            if ((c != ' ') && (c != '\t')) {
              break;
            }
          }
          nEnd = i;
        }
      }
      if (nEnd > nStart) {
        final int len = nEnd - nStart;
        System.arraycopy(text, nStart, bb.array(), 0, len);
        bb.limit(len);
      } else {
        bb.limit(0);
      }
    } else {
      bb.limit(0);
    }
    return bb.limit();
  }

  /**
   * 
   * @param text
   * @param offset
   * @param limit
   * @param start
   * @param end
   * @param bb
   * @return new bb limit
   */
  public final static int substringBetween(final byte[] text, final int offset, final int limit, final byte[] start, final byte[] end, final ByteBuffer bb) {
    return ArrayHelper.substringBetween(text, offset, limit, start, end, true, bb);
  }

  public final static String substringBetweenLast(final byte[] text, final int offset, final int limit, final byte[] start, final byte[] end) {
    return ArrayHelper.substringBetweenLast(text, offset, limit, start, end, true);
  }

  public final static String substringBetweenLast(final byte[] text, final int offset, final int limit, final byte[] start, final byte[] end, final boolean trim) {
    final int nEnd = ArrayHelper.lastIndexOf(text, offset, limit, end);
    int nStart = -1;
    if (nEnd > start.length) {
      nStart = ArrayHelper.lastIndexOf(text, offset, nEnd - 1, start);
    } else {
      return null;
    }
    if ((nStart < nEnd) && (nStart != -1) && (nEnd != -1)) {
      nStart += start.length;
      final String str = new String(text, nStart, nEnd - nStart);
      if (trim) {
        return str.trim();
      } else {
        return str;
      }
    } else {
      return null;
    }
  }

  public final static int substringBetweenLast(final byte[] text, final int offset, final int limit, final byte[] start, final byte[] end, final boolean trim,
      final ByteBuffer bb) {
    int nEnd = ArrayHelper.lastIndexOf(text, offset, limit, end);
    int nStart = -1;
    if (nEnd > start.length) {
      nStart = ArrayHelper.lastIndexOf(text, offset, nEnd - 1, start);
      if ((nStart < nEnd) && (nStart != -1) && (nEnd != -1)) {
        nStart += start.length;
        if (trim) {
          byte c;
          int i;
          for (i = nStart; i < nEnd; i++) {
            c = text[i];
            if ((c != ' ') && (c != '\t') && (c != '\r')) {
              break;
            }
          }
          nStart = i;
          for (i = nEnd; i >= nStart; i--) {
            c = text[i];
            if ((c != ' ') && (c != '\t') && (c != '\r')) {
              break;
            }
          }
          nEnd = i;
        }
        if (nEnd > nStart) {
          final int len = nEnd - nStart;
          System.arraycopy(text, nStart, bb.array(), 0, len);
          bb.limit(len);
        } else {
          bb.limit(0);
        }
      } else {
        bb.limit(0);
      }
    } else {
      bb.limit(0);
    }
    return bb.limit();
  }

  public final static int substringBetweenLast(final byte[] text, final int offset, final int limit, final byte[] start, final byte[] end, final ByteBuffer bb) {
    return ArrayHelper.substringBetweenLast(text, offset, limit, start, end, true, bb);
  }

  public final static byte[] toBytes(final ByteBuffer bb) {
    return ArrayHelper.toBytes(bb, bb.limit());
  }

  public final static byte[] toBytes(final ByteBuffer bb, final int len) {
    return ArrayHelper.toBytes(bb, 0, len);
  }

  /**
   * 
   * @param bb
   * @param offset
   * @param len
   *          relative
   * @return
   */
  public final static byte[] toBytes(final ByteBuffer bb, final int offset, final int len) {
    final byte[] result = new byte[len];
    System.arraycopy(bb.array(), offset, result, 0, len);
    return result;
  }

  public final static byte[] toBytesP(final ByteBuffer bb) {
    return ArrayHelper.toBytes(bb, bb.position(), bb.remaining());
  }

  public final static byte[] toBytesP(final ByteBuffer bb, final int len) {
    return ArrayHelper.toBytes(bb, bb.position(), len);
  }

  public static String toHexString(final byte[] data) {
    return ArrayHelper.toHexString(data, true);
  }

  public static String toHexString(final byte[] data, boolean human) {
    return ArrayHelper.toHexString(data, 0, data.length, human);
  }

  public static String toHexString(final byte[] data, final int offset, final int len) {
    return ArrayHelper.toHexString(data, offset, len, true);
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

  public static String toHexString(final ByteBuffer bb) {
    return ArrayHelper.toHexString(bb.array(), 0, bb.limit());
  }

  public static String toHexStringP(final ByteBuffer bb) {
    return ArrayHelper.toHexString(bb.array(), bb.position(), bb.remaining());
  }

  public static int[] toIntArray(final byte[] bytes) {
    final int[] result = new int[bytes.length];
    for (int i = 0; i < bytes.length; i++) {
      result[i] = bytes[i] & 0xff;
    }
    return result;
  }

  public static final String toString(final byte[] array) {
    return ArrayHelper.toString(array, 0, array.length);
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
      return ArrayHelper.safeTrim(ca, cb.position());
    }

  }

  public final static char[] safeTrim(final char[] ca, final int len) {
    if (len == ca.length) {
      return ca;
    } else {
      return Arrays.copyOf(ca, len);
    }
  }

  private static final SilentStringDecoder STRING_DECODER_SILENT = new SilentStringDecoder(Helper.CHARSET_UTF8);

  /**
   * 
   * @param array
   * @param offset
   * @param len
   *          relative length
   * @return
   */
  public static final String toString(final byte[] array, final int offset, final int len) {
    return new String(ArrayHelper.STRING_DECODER_SILENT.decode(array, offset, len));
  }

  public static final String toString(final ByteBuffer bb) {
    return ArrayHelper.toString(bb.array(), 0, bb.limit());
  }

  public static final String toStringP(final ByteBuffer bb) {
    return ArrayHelper.toString(bb.array(), bb.position(), bb.remaining());
  }

  /**
   * 
   * @param bb
   * @return length of trimmed value, position points to the beginning and limit is set to trimmed end
   */
  public final static ByteBuffer trimP(final ByteBuffer bb) {
    final int trimmedLimit = ArrayHelper.findTrimmedEndIdxP(bb);
    bb.limit(trimmedLimit);
    final int offset = bb.position();
    if (trimmedLimit == offset) {
      return bb;
    } else {
      final int trimmedOffset = ArrayHelper.findTrimmedOffsetP(bb);
      bb.position(trimmedOffset);
      return bb;
    }
  }

  public static int sizeP(final ByteBuffer bb) {
    return bb.remaining();
  }

  /**
   * 
   * @param array
   * @param offset
   *          absolute
   * @param limit
   *          absolute
   * @param b
   * @return absolute
   */
  public final static int lastIndexOf(final byte[] array, final int offset, final int limit, final byte b) {
    for (int i = limit - 1; i >= offset; i--) {
      if (array[i] == b) {
        return i;
      }
    }
    return -1;
  }

  public final static void write(final byte[] array, final int offset, final int end, final ByteBuffer outBB) {
    byte b;
    for (int i = offset; i < end; i++) {
      b = array[i];
      if (b == '|') {
        outBB.put((byte) ',').put((byte) ' ');
      } else if (b == '\'') {
        boolean found = false;
        while (++i < end) {
          b = array[i];
          if (b == '\'') {
            found = true;
            continue;
          } else {
            if (!found) {
              outBB.put((byte) '\'');
            }
            outBB.put(b);
            break;
          }
        }
        continue;
      } else {
        outBB.put(b);
      }
    }
  }

  private static int[] COUNT_ARRAY_INTS = new int[5];

  /**
   * 
   * @param array
   * @param offset
   *          absolute
   * @param limit
   *          absolute
   * @param b
   * @return absolute indexes
   */
  public final static int[] countArray(final byte[] array, final int offset, final int limit, final byte b) {
    int j = 0;
    for (int i = offset; i < limit; i++) {
      if (array[i] == b) {
        ArrayHelper.COUNT_ARRAY_INTS[j++] = i;
        if (j == ArrayHelper.COUNT_ARRAY_INTS.length) {
          break;
        }
      }
    }
    if (j == 0) {
      return ArrayHelper.EMPTY_INTS;
    } else {
      final int[] result = new int[j];
      System.arraycopy(ArrayHelper.COUNT_ARRAY_INTS, 0, result, 0, j);
      return result;
    }
  }

  /**
   * position is 0 and limit = capacity.
   * 
   * @param bb
   * @return
   */
  public final static boolean isEmpty(final ByteBuffer bb) {
    return ((bb.position() == 0) && (bb.limit() == bb.capacity())) || (bb.limit() == 0);
  }

  public static final int substringBetween(final byte[] array, final int offset, final int limit, final byte startByte, final byte endByte,
      final ByteBuffer tmpBB, final boolean narrow) {
    int start = -1;
    byte b;
    tmpBB.clear();
    for (int i = offset; i < limit; i++) {
      b = array[i];
      if ((narrow || (start == -1)) && (b == startByte)) {
        start = i;
      } else if ((start != -1) && (b == endByte)) {
        final int len = i - start - 1;
        System.arraycopy(array, start + 1, tmpBB.array(), 0, len);
        tmpBB.limit(len);
        return start;
      }
    }
    return -1;
  }

  public static final boolean startsWith(final ByteBuffer bb, final byte[] prefix) {
    return ArrayHelper.startsWith(bb.array(), 0, bb.limit(), prefix, 0, prefix.length);
  }

  public static final int indexOfP(final ByteBuffer lineBB, final byte[][] textsLower, final byte[][] textsUpper) {
    return ArrayHelper.indexOf(lineBB.array(), lineBB.position(), lineBB.limit(), textsLower, textsUpper);
  }

  public static final int indexOf(final byte[] array, final int position, final int limit, final byte[][] textsLower, final byte[][] textsUpper) {
    byte b;
    final int[] idx = new int[textsLower.length];
    for (int i = position; i < limit; i++) {
      b = array[i];
      for (int j = 0; j < idx.length; j++) {
        final int textIdx = idx[j];
        final byte[] textLower = textsLower[j];
        final byte[] textUpper = textsUpper[j];
        if ((b == textLower[textIdx]) || (b == textUpper[textIdx])) {
          if ((textIdx + 1) < textLower.length) {
            idx[j] = textIdx + 1;
          } else {
            return (i - textLower.length) + 1;
          }
        } else {
          idx[j] = 0;
        }
      }
    }
    return -1;
  }

  /**
   * continue writing to bb starting from its current limit.
   * 
   * @param bb
   */
  public static void extend(final ByteBuffer bb) {
    bb.position(bb.limit()).limit(bb.capacity());
  }

  public static int positionP(final ByteBuffer bb, final int relPos) {
    return bb.position(bb.position() + relPos).position();
  }

  public final static void replaceP(final ByteBuffer lineBB, final byte from, final byte to) {
    final byte[] array = lineBB.array();
    final int offset = lineBB.position();
    final int limit = lineBB.limit();
    ArrayHelper.replace(array, offset, limit, from, to);
  }

  private final static void replace(final byte[] array, final int offset, final int limit, final byte from, final byte to) {
    for (int i = offset; i < limit; i++) {
      if (array[i] == from) {
        {
          array[i] = to;
        }

      }
    }
  }

  public final static void replace(final byte[] text, final byte from, final byte to) {
    ArrayHelper.replace(text, 0, text.length, from, to);
  }

  private static MessageDigest MD5;
  static {
    try {
      ArrayHelper.MD5 = MessageDigest.getInstance("MD5");
    } catch (final NoSuchAlgorithmException e) {
      e.printStackTrace();
    }
  }

  public static final byte[] md5P(final ByteBuffer bb) {
    final byte[] array = bb.array();
    final int position = bb.position();
    final int len = bb.remaining();
    return ArrayHelper.md5(array, position, len);
  }

  private static byte[] md5(final byte[] array, final int position, final int len) {
    ArrayHelper.MD5.reset();
    ArrayHelper.MD5.update(array, position, len);
    return ArrayHelper.MD5.digest();
  }

  public static final byte toHexChar(final int b) {
    if (b < 10) {
      return (byte) (b + 0x30);
    } else {
      return (byte) (b + 0x57);
    }
  }

  public final static int limitP(final ByteBuffer bb, final int i) {
    return bb.limit(bb.limit() + i).limit();

  }

  public static class SensitiveStringDecoder {
    public final String          name;

    private final CharsetDecoder cd;

    public SensitiveStringDecoder(final Charset cs) {
      this.cd = cs.newDecoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT);
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
      return ArrayHelper.safeTrim(ca, cb.position());
    }
  }

  public static final int stripP(final ByteBuffer bb, final byte crimpChar) {
    final byte[] array = bb.array();
    final int position = bb.position();
    final int limit = bb.limit();
    return bb.limit(ArrayHelper.strip(array, position, limit, crimpChar)).limit();
  }

  public static final int strip(final byte[] array, final int offset, final int limit, final byte crimpChar) {
    byte b;
    int pos = offset;
    for (int i = offset; i < limit; i++) {
      b = array[i];
      if (crimpChar != b) {
        array[pos++] = b;
      }
    }
    return pos;
  }

  public static final ByteBuffer wrap(final ByteBuffer bb) {
    return (ByteBuffer) ByteBuffer.wrap(bb.array()).limit(bb.limit()).position(bb.position());
  }

  public final static boolean equalsP(final ByteBuffer bb, final byte[] bytes) {
    return ArrayHelper.equals(bb.array(), bb.position(), bytes);
  }

  public final static void writeP(final OutputStream out, final ByteBuffer bb) throws IOException {
    out.write(bb.array(), bb.position(), bb.remaining());
  }

  public final static byte[] fromHex(String s) {
    int len = s.length();
    byte[] data = new byte[len / 2];
    for (int i = 0; i < len; i += 2) {
      data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
    }
    return data;
  }

  public final static byte[] toBytes(String val) {
    return val == null ? ArrayHelper.EMPTY_BYTES : val.getBytes(Helper.CHARSET_UTF8);
  }

  public static final byte[] toBytes(int value) {
    return new byte[] { (byte) (value >>> 24), (byte) (value >>> 16), (byte) (value >>> 8), (byte) value };
  }

  public static final byte[] toBytes(short value) {
    return new byte[] { (byte) (value >>> 8), (byte) value };
  }

}
