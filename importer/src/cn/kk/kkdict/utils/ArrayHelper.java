package cn.kk.kkdict.utils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class ArrayHelper {
    public static boolean WARN = true;

    public static final int MAX_LINE_BYTES_NORMAL = 1024;

    public static final int MAX_LINE_BYTES_MEDIUM = 1024 * 32;

    public static final int MAX_LINE_BYTES_LARGE = 1024 * 64;

    private static final List<ByteBuffer> byteBuffersPoolNormal = new ArrayList<ByteBuffer>();
    private static final List<ByteBuffer> byteBuffersPoolMedium = new ArrayList<ByteBuffer>();
    private static final List<ByteBuffer> byteBuffersPoolLarge = new ArrayList<ByteBuffer>();
    private static final int MAX_BUFFER_SIZE = 10;

    public final static void giveBack(ByteBuffer bb) {
        if (bb != null) {
            final int capacity = bb.capacity();
            if (capacity == MAX_LINE_BYTES_NORMAL) {
                if (byteBuffersPoolNormal.size() < MAX_BUFFER_SIZE) {
                    byteBuffersPoolNormal.add(bb);
                }
            } else if (capacity == MAX_LINE_BYTES_MEDIUM) {
                if (byteBuffersPoolMedium.size() < MAX_BUFFER_SIZE) {
                    byteBuffersPoolMedium.add(bb);
                }
            } else if (capacity == MAX_LINE_BYTES_LARGE) {
                if (byteBuffersPoolLarge.size() < MAX_BUFFER_SIZE) {
                    byteBuffersPoolLarge.add(bb);
                }
            } else {
                throw new IllegalArgumentException("Non compatible byte buffer size: " + capacity);
            }
            bb.clear();
        }
    }

    public final static ByteBuffer getByteBufferNormal() {
        if (byteBuffersPoolNormal.isEmpty()) {
            return ByteBuffer.allocate(MAX_LINE_BYTES_NORMAL);
        } else {
            return byteBuffersPoolNormal.remove(0);
        }
    }

    public final static ByteBuffer getByteBufferMedium() {
        if (byteBuffersPoolMedium.isEmpty()) {
            return ByteBuffer.allocate(MAX_LINE_BYTES_MEDIUM);
        } else {
            return byteBuffersPoolMedium.remove(0);
        }
    }

    public final static ByteBuffer getByteBufferLarge() {
        if (byteBuffersPoolLarge.isEmpty()) {
            return ByteBuffer.allocate(MAX_LINE_BYTES_LARGE);
        } else {
            return byteBuffersPoolLarge.remove(0);
        }
    }

    public static final boolean isEquals(final ByteBuffer bb1, final ByteBuffer bb2) {
        final int l1 = bb1.limit();
        final int l2 = bb2.limit();
        return isEquals(bb1, 0, l1, bb2, 0, l2);
    }

    public static final boolean isEquals(final ByteBuffer bb1, int offset1, int l1, final ByteBuffer bb2, int offset2,
            final int l2) {
        // return v1.equalsIgnoreCase(v2);
        if (l1 == l2) {
            while (l1-- != 0) {
                if (bb1.get(offset1 + l1) != bb2.get(offset2 + l1)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public static final boolean isPredessorEquals(final ByteBuffer bb1, final ByteBuffer bb2) {
        final int l1 = bb1.limit();
        final int l2 = bb2.limit();
        return isPredessorEquals(bb1, 0, l1, bb2, 0, l2);
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

    public static final boolean isSuccessor(final ByteBuffer bb1, final ByteBuffer bb2) {
        final int l1 = bb1.limit();
        final int l2 = bb2.limit();
        return isSuccessor(bb1, 0, l1, bb2, 0, l2);
    }

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

    public final static boolean contains(final byte[] text, final int offset, final int limit, final byte[] s) {
        final int len = s.length;
        if (limit >= len) {
            final int size = limit - len + 1 + offset;
            for (int i = offset; i < size; i++) {
                if (equals(text, i, s, 0, len)) {
                    return true;
                }
            }
        }
        return false;
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

    /**
     * 
     * @param text
     * @param offset
     * @param limit
     *            relative limit
     * @param s
     * @return absolute index
     */
    public final static int indexOf(final byte[] text, final int offset, final int limit, final byte[] s) {
        return indexOf(text, offset, limit, s, 0, s.length);
    }

    /**
     * 
     * @param text
     * @param offset
     * @param limit
     *            relative limit
     * @param s
     * @param offset2
     * @param limit2
     *            relative limit
     * @return absolute index
     */
    public final static int indexOf(final byte[] text, final int offset, final int limit, final byte[] s,
            final int offset2, final int limit2) {
        if (limit >= limit2) {
            final int size = limit - limit2 + 1 + offset;
            for (int i = offset; i < size; i++) {
                if (equals(text, i, s, offset2, limit2)) {
                    return i;
                }
            }
        }
        return -1;
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

    /**
     * 
     * @param in
     * @param bb
     * @return line without '\n'-character
     * @throws IOException
     */
    public static final int readLine(BufferedInputStream in, ByteBuffer bb) throws IOException {
        int b;
        bb.clear();
        int len = 0;
        while (-1 != (b = in.read())) {
            len++;
            if (b == '\r') {
                b = in.read();
                if (-1 == b) {
                    break;
                } else if (b != '\n') {
                    if (bb.hasRemaining()) {
                        // skip beyond max line size
                        bb.put((byte) '\r');
                    }
                }
            }
            if (b != '\n') {
                if (bb.hasRemaining()) {
                    // skip beyond max line size
                    bb.put((byte) b);
                }
            } else {
                b = bb.position();
                bb.limit(b);
                bb.rewind();
                if (WARN && --len > bb.limit()) {
                    System.err.println("跳过超长部分：总" + len + "字符，跳过" + (len - bb.limit()) + "字符");
                }
                return b;
            }
        }
        if ((b = bb.position()) != 0) {
            bb.limit(b);
            bb.rewind();
            if (len > bb.limit()) {
                System.err.println("跳过超长部分：总" + len + "字符，跳过" + (len - bb.limit()) + "字符");
            }
            return b;
        }
        return -1;
    }

    /**
     * 
     * @param fileBB
     * @param lineBB
     * @return line length without '\n'-character, -1 if eof
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
                if (b != '\n') {
                    if (lineBB.hasRemaining()) {
                        // skip beyond max line size
                        lineBB.put((byte) '\r');
                    }
                }
            }
            if (b != '\n') {
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
     * @param bb
     * @return new bb limit
     */
    public final static int substringBetween(final byte[] text, final int offset, final int limit, final byte[] start,
            final byte[] end, ByteBuffer bb) {
        return substringBetween(text, offset, limit, start, end, true, bb);
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

    public final static int substringBetweenLast(final byte[] text, final int offset, final int limit,
            final byte[] start, final byte[] end, ByteBuffer bb) {
        return substringBetweenLast(text, offset, limit, start, end, true, bb);
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

    public final static byte[] toBytes(final ByteBuffer bb) {
        return toBytes(bb, bb.limit());
    }

    /**
     * 
     * @param bb
     * @param offset
     * @param len relative
     * @return
     */
    public final static byte[] toBytes(final ByteBuffer bb, int offset, int len) {
        final byte[] result = new byte[len];
        System.arraycopy(bb.array(), offset, result, 0, len);
        return result;
    }

    public final static byte[] toBytes(final ByteBuffer bb, int len) {
        return toBytes(bb, 0, len);
    }

    public static final int indexOf(final ByteBuffer bb, final byte b) {
        return indexOf(bb.array(), 0, bb.limit(), b);
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

    public static String toHexString(final byte[] data) {
        return toHexString(data, 0, data.length);
    }

    public static String toHexString(ByteBuffer bb) {
        return toHexString(bb.array(), 0, bb.limit());
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

    public static final String toString(final ByteBuffer bb) {
        return toString(bb.array(), 0, bb.limit());
    }

    public static final String toString(final byte[] array, int offset, int len) {
        return new String(array, offset, len, Helper.CHARSET_UTF8);
    }

    public static final String toString(final byte[] bb) {
        return new String(bb, 0, bb.length, Helper.CHARSET_UTF8);
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

    public static int[] toIntArray(byte[] bytes) {
        int[] result = new int[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            result[i] = bytes[i] & 0xff;
        }
        return result;
    }

}
