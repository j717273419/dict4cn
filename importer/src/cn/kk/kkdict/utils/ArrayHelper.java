package cn.kk.kkdict.utils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class ArrayHelper {
    public static final int[] EMPTY_INTS = new int[0];

    private static final List<ByteBuffer> byteBuffersPoolLarge = new ArrayList<ByteBuffer>();

    private static final List<ByteBuffer> byteBuffersPoolMedium = new ArrayList<ByteBuffer>();

    private static final List<ByteBuffer> byteBuffersPoolNormal = new ArrayList<ByteBuffer>();

    private static final List<ByteBuffer> byteBuffersPoolVeryLarge = new ArrayList<ByteBuffer>();

    public static final Comparator<byte[]> COMPARATOR_BYTE_ARRAY = new Comparator<byte[]>() {
        @Override
        public int compare(byte[] o1, byte[] o2) {
            return ArrayHelper.compareTo(o1, 0, o1.length, o2, 0, o2.length);
        }
    };

    private static final int MAX_BUFFER_SIZE = 10;
    public static final int MAX_LINE_BYTES_LARGE = 1024 * 64;
    public static final int MAX_LINE_BYTES_MEDIUM = 1024 * 32;
    public static final int MAX_LINE_BYTES_NORMAL = 1024;
    public static final int MAX_LINE_BYTES_VERY_LARGE = 1024 * 280;

    public static boolean WARN = true;

    public static final int compareTo(ByteBuffer bb1, ByteBuffer bb2) {
        return compareTo(bb1.array(), 0, bb1.limit(), bb2.array(), 0, bb2.limit());
    }

    public static final int compareToP(ByteBuffer bb1, ByteBuffer bb2) {
        final int offset1 = bb1.position();
        final int offset2 = bb2.position();
        final byte[] array1 = bb1.array();
        final byte[] array2 = bb2.array();
        final int len1 = bb1.remaining();
        final int len2 = bb2.remaining();
        return compareTo(array1, offset1, len1, array2, offset2, len2);
    }

    /**
     * 
     * @param bs1
     * @param offset1
     * @param len1
     *            relative length
     * @param bs2
     * @param offset2
     * @param len2
     *            relative length
     * @return
     */
    public static final int compareTo(byte[] bs1, int offset1, int len1, byte[] bs2, int offset2, int len2) {
        final int n = offset1 + Math.min(len1, len2);
        while (offset1 < n) {
            byte c1 = bs1[offset1++];
            byte c2 = bs2[offset2++];
            if (c1 != c2) {
                return c1 - c2;
            }
        }
        return len1 - len2;
    }

    public final static boolean containsP(final ByteBuffer bb1, final ByteBuffer bb2) {
        return contains(bb1.array(), bb1.position(), bb1.limit(), bb2.array(), bb2.position(), bb2.limit());
    }

    public final static boolean contains(final ByteBuffer bb1, final ByteBuffer bb2) {
        return contains(bb1.array(), 0, bb1.limit(), bb2.array(), 0, bb2.limit());
    }

    /**
     * 
     * @param text
     * @param offset1
     * @param end1
     *            absolute
     * @param s
     * @return
     */
    public final static boolean contains(final byte[] array1, final int offset1, final int end1, final byte[] array2,
            final int offset2, final int end2) {
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
        return contains(array1, offset1, end2, array2, 0, array2.length);
    }

    public final static int copy(final ByteBuffer from, final ByteBuffer to) {
        final int len = from.limit();
        return copy(from, 0, to, 0, len);
    }

    public final static int copy(final ByteBuffer from, final int offset1, final ByteBuffer to, final int offset2,
            final int len) {
        System.arraycopy(from.array(), offset1, to.array(), offset2, len);
        to.limit(offset2 + len);
        return len;
    }

    public final static int copyP(final ByteBuffer from, final ByteBuffer to) {
        final int offset1 = from.position();
        final int offset2 = to.position();
        final int len = from.limit() - offset1;
        return copy(from, offset1, to, offset2, len);
    }

    public final static int count(final byte[] array1, final int offset1, final int end1, final byte[] array2) {
        return count(array1, offset1, end1, array2, 0, array2.length);
    }

    public final static int count(final byte[] array1, final int offset1, final int end1, final byte[] array2,
            final int offset2, final int end2) {
        final int len1 = end1 - offset1;
        final int len2 = end2 - offset2;
        int count = 0;
        if (len2 > 0 && len1 >= len2) {
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
        return count(array, 0, endIdx, text, 0, text.length);
    }

    public final static int countP(final ByteBuffer bb, final byte[] text) {
        final int endIdx = bb.limit();
        final byte[] array = bb.array();
        return count(array, bb.position(), endIdx, text, 0, text.length);
    }

    public final static int count(final ByteBuffer bb1, final ByteBuffer bb2) {
        final int end1 = bb1.limit();
        final int end2 = bb2.limit();
        final byte[] array1 = bb1.array();
        final byte[] array2 = bb2.array();
        return count(array1, 0, end1, array2, 0, end2);
    }

    public final static int countP(final ByteBuffer bb1, final ByteBuffer bb2) {
        final int end1 = bb1.limit();
        final int end2 = bb2.limit();
        final int offset1 = bb1.position();
        final int offset2 = bb2.position();
        final byte[] array1 = bb1.array();
        final byte[] array2 = bb2.array();
        return count(array1, offset1, end1, array2, offset2, end2);
    }

    public static void debug(byte[] data) {
        if (data.length == 1) {
            System.out.println("byte: " + data[0]);
        }
        if (data.length == 2) {
            System.out.println("short (le): " + (((data[1] & 0xFF) << 8) | (data[0] & 0xFF)));
            System.out.println("short (be): " + (((data[1] & 0xFF) << 8) | (data[0] & 0xFF)));
        }
        if (data.length == 4) {
            System.out.println("int (le): "
                    + (((data[3] & 0xFF) << 24) | ((data[2] & 0xFF) << 16) | ((data[1] & 0xFF) << 8) | data[0] & 0xFF));
            System.out.println("int (be): "
                    + (((data[0] & 0xFF) << 24) | ((data[1] & 0xFF) << 16) | ((data[2] & 0xFF) << 8) | data[3] & 0xFF));
        }
        if (data.length < 1024) {
            System.out.println("BYTES: " + toHexString(data));
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
        } catch (UnsupportedEncodingException e) {
        }
    }

    public final static boolean equals(final byte[] array1, final int start1, final byte[] array2, final int start2,
            final int len) {
        if (start1 + len > array1.length || start2 + len > array2.length) {
            return false;
        }
        for (int i = 0; i < len; i++) {
            if (array1[start1 + i] != array2[start2 + i]) {
                return false;
            }
        }
        return true;
    }

    public final static boolean equals(final byte[] array1, final int offset1, final int end1, final byte[] array2,
            final int offset2, final int end2) {
        final int len1 = end1 - offset1;
        final int len2 = end2 - offset2;
        if (len1 != len2) {
            return false;
        } else {
            return equals(array1, offset1, array2, offset2, len1);
        }
    }

    public final static boolean equals(final byte[] array1, final int offset1, final byte[] array2) {
        return equals(array1, offset1, array2, 0, array2.length);
    }

    public final static boolean equals(final ByteBuffer bb1, final ByteBuffer bb2) {
        final byte[] array1 = bb1.array();
        final byte[] array2 = bb2.array();
        final int end1 = bb1.limit();
        final int end2 = bb2.limit();
        return equals(array1, 0, end1, array2, 0, end2);
    }

    public final static boolean equalsP(final ByteBuffer bb1, final ByteBuffer bb2) {
        final byte[] array1 = bb1.array();
        final byte[] array2 = bb2.array();
        final int offset1 = bb1.position();
        final int offset2 = bb2.position();
        final int end1 = bb1.limit();
        final int end2 = bb2.limit();
        return equals(array1, offset1, end1, array2, offset2, end2);
    }

    public final static int findTrimmedEndIdx(final ByteBuffer bb) {
        return findTrimmedEndIdx(bb.array(), 0, bb.limit());
    }

    public final static int findTrimmedEndIdxP(final ByteBuffer bb) {
        final int offset = bb.position();
        final int limit = bb.limit();
        final byte[] array = bb.array();
        return findTrimmedEndIdx(array, offset, limit);
    }

    public static int findTrimmedEndIdx(final byte[] array, final int offset, final int limit) {
        byte b;
        for (int i = limit - 1; i >= offset; i--) {
            b = array[i];
            if (b == ' ' || b == '\t' || b == Helper.SEP_NEWLINE_CHAR || b == '\r' || b == '\0') {
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
        return findTrimmedOffset(bb.array(), 0, bb.limit());
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
        return findTrimmedOffset(array, offset, l);
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
            if (b == ' ' || b == '\t' || b == Helper.SEP_NEWLINE_CHAR || b == '\r' || b == '\0') {
                continue;
            } else {
                return i;
            }
        }
        return endIdx;
    }

    public static final ByteBuffer borrowByteBuffer(final int capacity) {
        if (capacity >= MAX_LINE_BYTES_VERY_LARGE) {
            return ByteBuffer.allocate(capacity);
        } else if (capacity >= MAX_LINE_BYTES_LARGE) {
            return borrowByteBufferVeryLarge();
        } else if (capacity >= MAX_LINE_BYTES_MEDIUM) {
            return borrowByteBufferLarge();
        } else if (capacity >= MAX_LINE_BYTES_NORMAL) {
            return borrowByteBufferMedium();
        } else {
            return borrowByteBufferNormal();
        }
    }

    public final static ByteBuffer borrowByteBufferLarge() {
        if (byteBuffersPoolLarge.isEmpty()) {
            return ByteBuffer.allocate(MAX_LINE_BYTES_LARGE);
        } else {
            return byteBuffersPoolLarge.remove(0);
        }
    }

    public final static ByteBuffer borrowByteBufferMedium() {
        if (byteBuffersPoolMedium.isEmpty()) {
            return ByteBuffer.allocate(MAX_LINE_BYTES_MEDIUM);
        } else {
            return byteBuffersPoolMedium.remove(0);
        }
    }

    public final static ByteBuffer borrowByteBufferNormal() {
        if (byteBuffersPoolNormal.isEmpty()) {
            return ByteBuffer.allocate(MAX_LINE_BYTES_NORMAL);
        } else {
            return byteBuffersPoolNormal.remove(0);
        }
    }

    public final static ByteBuffer borrowByteBufferVeryLarge() {
        if (byteBuffersPoolVeryLarge.isEmpty()) {
            return ByteBuffer.allocate(MAX_LINE_BYTES_VERY_LARGE);
        } else {
            return byteBuffersPoolVeryLarge.remove(0);
        }
    }

    public final static void giveBack(ByteBuffer bb) {
        if (bb != null) {
            final int capacity = bb.capacity();
            if (capacity == MAX_LINE_BYTES_NORMAL) {
                if (!byteBuffersPoolNormal.contains(bb) && byteBuffersPoolNormal.size() < MAX_BUFFER_SIZE) {
                    byteBuffersPoolNormal.add(bb);
                }
            } else if (capacity == MAX_LINE_BYTES_MEDIUM) {
                if (!byteBuffersPoolMedium.contains(bb) && byteBuffersPoolMedium.size() < MAX_BUFFER_SIZE) {
                    byteBuffersPoolMedium.add(bb);
                }
            } else if (capacity == MAX_LINE_BYTES_LARGE) {
                if (!byteBuffersPoolLarge.contains(bb) && byteBuffersPoolLarge.size() < MAX_BUFFER_SIZE) {
                    byteBuffersPoolLarge.add(bb);
                }
            } else if (capacity == MAX_LINE_BYTES_VERY_LARGE) {
                if (!byteBuffersPoolLarge.contains(bb) && byteBuffersPoolLarge.size() < 2) {
                    byteBuffersPoolLarge.add(bb);
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
     *            absolute
     * @param limit
     *            absolute
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
     *            relative
     * @param s
     * @return absolute index
     */
    public final static int indexOf(final byte[] text, final int offset, final int len, final byte[] s) {
        return indexOf(text, offset, len, s, 0, s.length);
    }

    /**
     * 
     * @param text
     * @param offset
     * @param len1
     *            relative
     * @param s
     * @param offset2
     * @param len2
     *            relative
     * @return absolute index
     */
    public final static int indexOf(final byte[] text, final int offset, final int len1, final byte[] s,
            final int offset2, final int len2) {
        if (len1 >= len2) {
            final int size = len1 - len2 + 1 + offset;
            for (int i = offset; i < size; i++) {
                if (equals(text, i, s, offset2, len2)) {
                    return i;
                }
            }
        }
        return -1;
    }

    public static final int indexOf(final ByteBuffer bb, final byte b) {
        return indexOf(bb.array(), 0, bb.limit(), b);
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
        return indexOf(bb.array(), bb.position(), bb.remaining(), b);
    }

    public static final boolean isPredessorEquals(final ByteBuffer bb1, final ByteBuffer bb2) {
        final int l1 = bb1.limit();
        final int l2 = bb2.limit();
        return isPredessorEquals(bb1, 0, l1, bb2, 0, l2);
    }

    public static boolean isPredessorEqualsP(ByteBuffer bb1, ByteBuffer bb2) {
        return isPredessorEquals(bb1, bb1.position(), bb1.remaining(), bb2, bb2.position(), bb2.remaining());
    }

    public static final boolean isPredessorEquals(final ByteBuffer bb1, final int offset1, final int l1,
            final ByteBuffer bb2, final int offset2, final int l2) {
        // return v1.compareToIgnoreCase(v2) <= 0;
        if (l1 <= 0) {
            return false;
        } else if (l2 <= 0) {
            return true;
        }
        return compareTo(bb1.array(), offset1, l1, bb2.array(), offset2, l2) <= 0;
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
        return isSuccessor(bb1, 0, l1, bb2, 0, l2);
    }

    /**
     * 
     * @param bb1
     * @param offset1
     * @param l1
     *            relative length
     * @param bb2
     * @param offset2
     * @param l2
     *            relative length
     * @return
     */
    public static final boolean isSuccessor(final ByteBuffer bb1, final int offset1, final int l1,
            final ByteBuffer bb2, final int offset2, final int l2) {
        // return v1.compareToIgnoreCase(v2) > 0;
        if (l1 <= 0) {
            return true;
        } else if (l2 <= 0) {
            return false;
        }
        return compareTo(bb1.array(), offset1, l1, bb2.array(), offset2, l2) > 0;
    }

    public final static int lastIndexOf(final byte[] text, final int offset, final int limit, final byte[] s) {
        final int len = s.length;
        if (limit >= len) {
            final int size = limit - len + 1;
            for (int i = size - 1; i >= offset; i--) {
                if (equals(text, i, s, 0, len)) {
                    return i;
                }
            }
        }
        return -1;
    }

    public static void main(String[] args) {
        byte[] text = "      <namespace key=\"-2\" case=\"first-letter\">Medėjė</namespace><namespace key=\"-2\" case=\"first-letter\">Medėjė2</namespace>"
                .getBytes(Helper.CHARSET_UTF8);
        System.out.println(contains(text, 0, text.length, "</namespace>".getBytes(Helper.CHARSET_UTF8)));
        System.out.println(substringBetween(text, 0, text.length, "\">".getBytes(Helper.CHARSET_UTF8),
                "</namespace>".getBytes(Helper.CHARSET_UTF8)));
        ByteBuffer bbBuffer = ByteBuffer.allocate(MAX_LINE_BYTES_NORMAL);
        substringBetween(text, 0, text.length, "\">".getBytes(Helper.CHARSET_UTF8),
                "</namespace>".getBytes(Helper.CHARSET_UTF8), bbBuffer);
        System.out.println(toString(bbBuffer));

        substringBetweenLast(text, 0, text.length, "\">".getBytes(Helper.CHARSET_UTF8),
                "</namespace>".getBytes(Helper.CHARSET_UTF8), bbBuffer);
        System.out.println(toString(bbBuffer));

        System.out.println(substringBetweenLast(text, 0, text.length, "\">".getBytes(Helper.CHARSET_UTF8),
                "</namespace>".getBytes(Helper.CHARSET_UTF8)));
        System.out.println(indexOf(text, 1, text.length - 1, "namespace".getBytes(Helper.CHARSET_UTF8), 0, 4));
        bbBuffer.put((byte) 1);
        bbBuffer.put((byte) 2);
        bbBuffer.put((byte) 3);
        bbBuffer.put((byte) 4);
        bbBuffer.put((byte) 5);
        bbBuffer.put((byte) 6);
        bbBuffer.put((byte) 7);
        bbBuffer.put((byte) 8);
        substring(bbBuffer, 1);
        System.out.println(toHexString(bbBuffer));
    }

    /**
     * 
     * @param in
     * @param bb
     * @return line without Helper.SEP_NEWLINE_CHAR-character
     * @throws IOException
     */
    public static final int readLine(BufferedInputStream in, ByteBuffer bb) throws IOException {
        int b;
        bb.clear();
        int len = 0;
        while (-1 != (b = in.read())) {
            len++;
            if (b != Helper.SEP_NEWLINE_CHAR && b != '\r') {
                if (bb.hasRemaining()) {
                    // skip beyond max line size
                    bb.put((byte) b);
                }
            } else {
                bb.limit(bb.position()).rewind();
                if (WARN && --len > bb.capacity()) {
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
    public static int readLine(ByteBuffer fileBB, ByteBuffer lineBB) {
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
                if (WARN && --len > lineBB.limit()) {
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
    public static final int readLineTrimmed(BufferedInputStream in, ByteBuffer bb) throws IOException {
        int b;
        bb.clear();
        int len = 0;
        boolean valid = false;
        int lastValid = 0;
        while (-1 != (b = in.read())) {
            len++;
            if (b != Helper.SEP_NEWLINE_CHAR && b != '\r') {
                final boolean empty = b == ' ' || b == '\t' || b == '\0';
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
                if (WARN && --len > bb.capacity()) {
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

    public static final boolean startsWith(byte[] array, byte[] prefix) {
        final int l1 = array.length;
        final int l2 = prefix.length;
        return startsWith(array, l1, prefix, l2);
    }

    public static final boolean startsWith(final byte[] array, final int l1, final byte[] prefix, int l2) {
        if (l1 >= l2) {
            while (l2-- != 0) {
                if (array[l2] != prefix[l2]) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
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
            byte[] array = bb.array();
            int i = 0;
            int s = startIdx;
            while (s < limit) {
                array[i] = array[s];
                s++;
                i++;
            }
            bb.limit(limit - startIdx);
            return bb.limit();
        }
    }

    public final static String substringBetween(final byte[] text, final int offset, final int limit,
            final byte[] start, final byte[] end) {
        return substringBetween(text, offset, limit, start, end, true);
    }

    public final static String substringBetween(final byte[] text, final int offset, final int limit,
            final byte[] start, final byte[] end, final boolean trim) {
        int nStart = indexOf(text, offset, limit, start);
        final int nEnd = indexOf(text, nStart + start.length + 1, limit, end);
        if (nStart != -1 && nEnd > nStart) {
            nStart += start.length;
            String str = new String(text, nStart, nEnd - nStart, Helper.CHARSET_UTF8);
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
    public final static int substringBetween(final byte[] text, final int offset, final int limit, final byte[] start,
            final byte[] end, final boolean trim, ByteBuffer bb) {
        int nStart = indexOf(text, offset, limit, start);
        int nEnd = indexOf(text, nStart + start.length + 1, limit, end);
        if (nStart != -1 && nEnd > nStart) {
            nStart += start.length;
            if (trim) {
                byte c;
                int i;
                for (i = nStart; i < nEnd; i++) {
                    c = text[i];
                    if (c != ' ' && c != '\t' && c != '\r') {
                        break;
                    }
                }
                nStart = i;
                for (i = nEnd; i >= nStart; i--) {
                    c = text[i];
                    if (c != ' ' && c != '\t' && c != '\r') {
                        break;
                    }
                }
                nEnd = i;
            }
            if (nEnd > nStart) {
                int len = nEnd - nStart;
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
    public final static int substringBetween(final byte[] text, final int offset, final int limit, final byte[] start,
            final byte[] end, ByteBuffer bb) {
        return substringBetween(text, offset, limit, start, end, true, bb);
    }

    public final static String substringBetweenLast(final byte[] text, final int offset, final int limit,
            final byte[] start, final byte[] end) {
        return substringBetweenLast(text, offset, limit, start, end, true);
    }

    public final static String substringBetweenLast(final byte[] text, final int offset, final int limit,
            final byte[] start, final byte[] end, final boolean trim) {
        int nEnd = lastIndexOf(text, offset, limit, end);
        int nStart = -1;
        if (nEnd > start.length) {
            nStart = lastIndexOf(text, offset, nEnd - 1, start);
        } else {
            return null;
        }
        if (nStart < nEnd && nStart != -1 && nEnd != -1) {
            nStart += start.length;
            String str = new String(text, nStart, nEnd - nStart);
            if (trim) {
                return str.trim();
            } else {
                return str;
            }
        } else {
            return null;
        }
    }

    public final static int substringBetweenLast(final byte[] text, final int offset, final int limit,
            final byte[] start, final byte[] end, final boolean trim, ByteBuffer bb) {
        int nEnd = lastIndexOf(text, offset, limit, end);
        int nStart = -1;
        if (nEnd > start.length) {
            nStart = lastIndexOf(text, offset, nEnd - 1, start);
            if (nStart < nEnd && nStart != -1 && nEnd != -1) {
                nStart += start.length;
                if (trim) {
                    byte c;
                    int i;
                    for (i = nStart; i < nEnd; i++) {
                        c = text[i];
                        if (c != ' ' && c != '\t' && c != '\r') {
                            break;
                        }
                    }
                    nStart = i;
                    for (i = nEnd; i >= nStart; i--) {
                        c = text[i];
                        if (c != ' ' && c != '\t' && c != '\r') {
                            break;
                        }
                    }
                    nEnd = i;
                }
                if (nEnd > nStart) {
                    int len = nEnd - nStart;
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

    public final static int substringBetweenLast(final byte[] text, final int offset, final int limit,
            final byte[] start, final byte[] end, ByteBuffer bb) {
        return substringBetweenLast(text, offset, limit, start, end, true, bb);
    }

    public final static byte[] toBytes(final ByteBuffer bb) {
        return toBytes(bb, bb.limit());
    }

    public final static byte[] toBytes(final ByteBuffer bb, int len) {
        return toBytes(bb, 0, len);
    }

    /**
     * 
     * @param bb
     * @param offset
     * @param len
     *            relative
     * @return
     */
    public final static byte[] toBytes(final ByteBuffer bb, int offset, int len) {
        final byte[] result = new byte[len];
        System.arraycopy(bb.array(), offset, result, 0, len);
        return result;
    }

    public final static byte[] toBytesP(final ByteBuffer bb) {
        return toBytes(bb, bb.position(), bb.remaining());
    }

    public final static byte[] toBytesP(final ByteBuffer bb, int len) {
        return toBytes(bb, bb.position(), len);
    }

    public static String toHexString(final byte[] data) {
        return toHexString(data, 0, data.length);
    }

    /**
     * 
     * @param data
     * @param offset
     * @param len
     *            relative
     * @return
     */
    public static String toHexString(final byte[] data, final int offset, final int len) {
        StringBuffer sb = new StringBuffer(len * 2);
        for (int idx = offset; idx < offset + len; idx++) {
            byte b = data[idx];
            int i = b & 0xff;
            if (i < 0xf) {
                sb.append('0');
            }
            sb.append(Integer.toHexString(i)).append(' ');
        }
        return sb.toString();
    }

    public static String toHexString(ByteBuffer bb) {
        return toHexString(bb.array(), 0, bb.limit());
    }

    public static String toHexStringP(ByteBuffer bb) {
        return toHexString(bb.array(), bb.position(), bb.remaining());
    }

    public static int[] toIntArray(byte[] bytes) {
        int[] result = new int[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            result[i] = bytes[i] & 0xff;
        }
        return result;
    }

    public static final String toString(final byte[] bb) {
        return new String(bb, 0, bb.length, Helper.CHARSET_UTF8);
    }

    /**
     * 
     * @param array
     * @param offset
     * @param len
     *            relative length
     * @return
     */
    public static final String toString(final byte[] array, int offset, int len) {
        return new String(array, offset, len, Helper.CHARSET_UTF8);
    }

    public static final String toString(final ByteBuffer bb) {
        return toString(bb.array(), 0, bb.limit());
    }

    public static final String toStringP(final ByteBuffer bb) {
        return toString(bb.array(), bb.position(), bb.remaining());
    }

    /**
     * 
     * @param bb
     * @return length of trimmed value, position points to the beginning and limit is set to trimmed end
     */
    public final static ByteBuffer trimP(final ByteBuffer bb) {
        final int trimmedLimit = findTrimmedEndIdxP(bb);
        bb.limit(trimmedLimit);
        final int offset = bb.position();
        if (trimmedLimit == offset) {
            return bb;
        } else {
            final int trimmedOffset = findTrimmedOffsetP(bb);
            bb.position(trimmedOffset);
            return bb;
        }
    }

    public static int sizeP(ByteBuffer bb) {
        return bb.remaining();
    }

    public final static int stripWikiLine(final ByteBuffer inBB, final ByteBuffer outBB, final int maxChars) {
        final byte[] array = inBB.array();
        final int limit = inBB.limit();
        outBB.clear();
        return stripWikiLine(array, 0, limit, outBB, maxChars);
    }

    public final static int stripWikiLineP(final ByteBuffer inBB, final ByteBuffer outBB, final int maxChars) {
        final byte[] array = inBB.array();
        final int offset = inBB.position();
        final int limit = inBB.limit();
        return stripWikiLine(array, offset, limit, outBB, maxChars);
    }

    protected static int stripWikiLine(final byte[] array, final int offset, final int limit, final ByteBuffer outBB,
            final int maxChars) {
        byte b;
        int countEquals = 0;
        int countQuos = 0;
        int countSpaces = 0;
        int linkOpened = -1;
        boolean externalOpened = false;
        byte lastByte = -1;
        final int startPos = outBB.position();
        for (int i = offset; i < limit; i++) {
            if (outBB.position() > maxChars) {
                break;
            }
            b = array[i];
            if (b != ' ' && countSpaces > 0) {
                if (lastByte != ' ' && lastByte != '(' && lastByte != -1) {
                    outBB.put((byte) ' ');
                    lastByte = ' ';
                }
                countSpaces = 0;
            }
            if (b != '\'' && countQuos == 1) {
                outBB.put((byte) '\'');
                lastByte = '\'';
                countQuos = 0;
            }
            if (b != '=' && countEquals == 1) {
                outBB.put((byte) '=');
                lastByte = '=';
                countEquals = 0;
            }
            switch (b) {
            case ' ':
                if (externalOpened && linkOpened != -1) {
                    linkOpened = -1;
                }
                countSpaces++;
                continue;
            case '&':
                if (i + 8 < limit && ArrayHelper.equals(array, i, Helper.SEP_HTML_TAG_START_BYTES)) {
                    i += Helper.SEP_HTML_TAG_START_BYTES.length - 1;
                    final int stop = ArrayHelper.indexOf(array, i, limit - i, Helper.SEP_HTML_TAG_STOP_BYTES);
                    if (stop != -1) {
                        i = stop + Helper.SEP_HTML_TAG_STOP_BYTES.length - 1;
                    }
                } else if (i + 3 < limit) {
                    final int stop = ArrayHelper.indexOf(array, i, limit, (byte) ';');
                    if (stop != -1) {
                        i = stop;
                    }
                }
                continue;
            case '{':
                if (i + 4 < limit && array[i + 1] == '{') {
                    final int start = i + 2;
                    final int stop = ArrayHelper.indexOf(array, i, limit, (byte) '}');
                    final int valueIdx  = ArrayHelper.indexOf(array, i, limit, (byte) '=');
                    if (-1 != stop && valueIdx == -1) {
                        final int[] wallsIdx = ArrayHelper.countArray(array, i, stop, (byte) '|');
                        if (wallsIdx.length > 0 && wallsIdx.length < 3) {
                            final int end1;
                            final int len1 = wallsIdx[0] - start;
                            final int end2;
                            final int len2;
                            if (wallsIdx.length > 1) {
                                end1 = wallsIdx[1];
                                len2 = wallsIdx[1] - wallsIdx[0] - 1;
                                if (wallsIdx.length > 2) {
                                    end2 = wallsIdx[2];
                                } else {
                                    end2 = stop;
                                }
                            } else {
                                end1 = stop;
                                len2 = -1;
                                end2 = stop;
                            }
                            
                            // main word
                            if (len1 > len2) {
                                write(array, wallsIdx[0] + 1, end1, outBB);
                            } else {
                                write(array, wallsIdx[1] + 1, end2, outBB);
                            }
                            outBB.put((byte) ' ').put((byte) '(');
                            // first word
                            write(array, start, wallsIdx[0], outBB);
                            // further words
                            if (wallsIdx.length > 1) {
                                outBB.put((byte) ',').put((byte) ' ');
                                if (len1 > len2) {
                                    write(array, wallsIdx[1] + 1, stop, outBB);
                                } else if (wallsIdx.length > 2) {
                                    write(array, wallsIdx[2] + 1, stop, outBB);
                                }
                            }
                            outBB.put((byte) ')');
                            lastByte = ')';

                            i = stop + 1;
                        } else {
                            i = stop + 1;
                        }
                    }
                }
                continue;
            case '}':
                continue;
            case '[':
                linkOpened = i;
                if (i + 4 < limit && array[i + 1] == '[') {
                    final int stop = ArrayHelper.indexOf(array, i, limit, (byte) ']');
                    final int[] wallsIdx = ArrayHelper.countArray(array, i, stop, (byte) '|');
                    if (wallsIdx.length > 1) {
                        linkOpened = -1;
                        i = stop + 1;
                    }
                } else if (i + 7 < limit
                        && (-1 != ArrayHelper.indexOf(array, i + 4, i + 8, Helper.SEP_URI_POSTFIX_BYTES))) {
                    externalOpened = true;
                }
                continue;
            case '|':
                if (linkOpened != -1) {
                    if (i - 4 >= 0 && array[i - 4] == '.' || array[i - 3] == '.') {
                        externalOpened = true;
                    }
                    linkOpened = -1;
                }
                continue;
            case ']':
                if (!externalOpened && linkOpened != -1) {
                    i = linkOpened;
                }
                linkOpened = -1;
                externalOpened = false;
                continue;
            case '\'':
                countQuos++;
                continue;
            case '=':
                countEquals++;
                continue;
            case '*':
                continue;
            case '#':
                continue;
            }
            if (linkOpened == -1) {
                outBB.put(b);
                lastByte = b;
            }
        }
        if (outBB.position() > maxChars) {
            int idx = lastIndexOf(outBB.array(), startPos, outBB.position(), "，".getBytes(Helper.CHARSET_UTF8));
            if (idx == -1) {
                idx = lastIndexOf(outBB.array(), startPos, outBB.position(), "。".getBytes(Helper.CHARSET_UTF8));
                if (idx == -1) {
                    idx = lastIndexOf(outBB.array(), startPos, outBB.position(), Helper.SEP_SPACE_BYTES);
                }
            }
            if (idx != -1) {
                outBB.position(idx);
            }
            outBB.put((byte) ' ').put(Helper.SEP_ETC_BYTES);
        }
        outBB.limit(outBB.position()).rewind();
        return outBB.limit();
    }

    private final static void write(final byte[] array, final int offset, final int end, final ByteBuffer outBB) {
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
     *            absolute
     * @param limit
     *            absolute
     * @param b
     * @return absolute indexes
     */
    public final static int[] countArray(final byte[] array, final int offset, final int limit, final byte b) {
        int j = 0;
        for (int i = offset; i < limit; i++) {
            if (array[i] == b) {
                COUNT_ARRAY_INTS[j++] = i;
                if (j == COUNT_ARRAY_INTS.length) {
                    break;
                }
            }
        }
        if (j == 0) {
            return EMPTY_INTS;
        } else {
            int[] result = new int[j];
            System.arraycopy(COUNT_ARRAY_INTS, 0, result, 0, j);
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
        return (bb.position() == 0 && bb.limit() == bb.capacity()) || bb.limit() == 0;
    }

}
