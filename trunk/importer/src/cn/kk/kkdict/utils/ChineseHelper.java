package cn.kk.kkdict.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Arrays;

public final class ChineseHelper {
    private static int[] CODEPOINTS_ST_SIMPLE;
    private static int[] CODEPOINTS_ST_TRADITIONAL;
    private static int[] CODEPOINTS_TS_SIMPLE;
    private static int[] CODEPOINTS_TS_TRADITIONAL;

    /**
     * Converts input text to traditional chinese text
     * 
     * @param input
     *            simplified chinese text
     * @return traditional chinese text
     */
    public static String toTraditionalChinese(final String input) {
        if (CODEPOINTS_ST_SIMPLE == null) {
            createSimpleTraditionalMap();
        }
        StringBuilder sb = new StringBuilder();

        int idx;
        for (int i = 0; i < input.length(); i++) {
            int codePoint = input.codePointAt(i);
            if ((idx = Arrays.binarySearch(CODEPOINTS_ST_SIMPLE, codePoint)) >= 0) {
                sb.append(Character.toChars(CODEPOINTS_ST_TRADITIONAL[idx]));
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
     *            traditional chinese text
     * @return simplified chinese text
     */
    public static String toSimplifiedChinese(final String input) {
        if (CODEPOINTS_TS_SIMPLE == null) {
            createTraditionalSimpleMap();
        }
        StringBuilder sb = new StringBuilder();

        int idx;
        for (int i = 0; i < input.length(); i++) {
            int codePoint = input.codePointAt(i);
            if ((idx = Arrays.binarySearch(CODEPOINTS_TS_TRADITIONAL, codePoint)) >= 0) {
                sb.append(Character.toChars(CODEPOINTS_TS_SIMPLE[idx]));
            } else {
                sb.append(Character.toChars(codePoint));
            }
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        Charset cs = Helper.CHARSET_UTF8;
        ByteBuffer bb = ArrayHelper.getByteBufferNormal();
        byte[] array = cs.encode("丟並乾亂亙亞丢并干乱亘亚").array();
        System.out.println(ArrayHelper.toHexString(array));
        System.arraycopy(array, 0, bb.array(), 0, array.length);
        bb.limit(array.length);
        toSimplifiedChinese(bb);
        System.out.println(new String(cs.decode(bb).array()));
        bb.rewind();
        toTraditionalChinese(bb);
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

    public static final int toSimplifiedChinese(ByteBuffer bb) {
        if (CODEPOINTS_TS_SIMPLE == null) {
            createTraditionalSimpleMap();
        }
        return decode(bb, CODEPOINTS_TS_TRADITIONAL, CODEPOINTS_TS_SIMPLE);
    }

    public static final int toTraditionalChinese(ByteBuffer bb) {
        if (CODEPOINTS_ST_SIMPLE == null) {
            createSimpleTraditionalMap();
        }
        return decode(bb, CODEPOINTS_ST_SIMPLE, CODEPOINTS_ST_TRADITIONAL);
    }

    private static final int decode(ByteBuffer bb, int[] from, int[] to) {
        int mark = bb.position();
        int limit = bb.limit();
        int idx;
        int i1, i2, i3;
        byte b1, b2, b3;
        while (mark < limit) {
            b1 = bb.get();
            i1 = b1;
            if (i1 >= 0) {
                mark++;
            } else if ((i1 >> 5) == -2) {
                if (limit - mark < 2) {
                    return -1;
                }
                mark += 2;
            } else if ((i1 >> 4) == -2) {
                // chinese characters in convertion map are all of 3 bytes length, replace the bytes here
                if (limit - mark < 3) {
                    return -1;
                }
                b2 = bb.get();
                i2 = b2;
                b3 = bb.get();
                i3 = b3;
                int uc = (i1 << 12) ^ (i2 << 6)
                        ^ (i3 ^ (((byte) 0xE0 << 12) ^ ((byte) 0x80 << 6) ^ ((byte) 0x80 << 0)));

                if ((idx = Arrays.binarySearch(from, uc)) >= 0) {
                    uc = to[idx];
                    bb.put(mark++, (byte) (uc >> 12 | 0xE0));
                    bb.put(mark++, (byte) (uc >> 6 & 0x3F | 0x80));
                    bb.put(mark++, (byte) (uc & 0x3F | 0x80));
                } else {
                    mark += 3;
                }
            } else if ((i1 >> 3) == -2) {
                if (limit - mark < 4) {
                    return -1;
                }
                mark += 4;
            } else {
                return -1;
            }
        }
        mark = bb.position();
        bb.rewind();
        return mark;
    }

    public static char lowSurrogate(int codePoint) {
        return (char) ((codePoint & 0x3ff) + Character.MIN_LOW_SURROGATE);
    }

    public final static char highSurrogate(int codePoint) {
        return (char) ((codePoint >>> 10) + (Character.MIN_HIGH_SURROGATE - (Character.MIN_SUPPLEMENTARY_CODE_POINT >>> 10)));
    }

    private static void createTraditionalSimpleMap() {
        final String file = "traditional2simple.txt";
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(PinyinHelper.class.getResourceAsStream("/"
                    + file)));
            String line;
            while (null != (line = reader.readLine())) {
                String[] parts = line.split(Helper.SEP_PARTS);
                if (parts.length == 2) {
                    String map = parts[1];
                    int length = map.length();
                    if (parts[0].equals("sortedSimple")) {
                        CODEPOINTS_TS_SIMPLE = new int[length];
                        for (int i = 0; i < length; i++) {
                            CODEPOINTS_TS_SIMPLE[i] = map.codePointAt(i);
                        }
                    } else if (parts[0].equals("sortedTraditional")) {
                        CODEPOINTS_TS_TRADITIONAL = new int[length];
                        for (int i = 0; i < length; i++) {
                            CODEPOINTS_TS_TRADITIONAL[i] = map.codePointAt(i);
                        }
                    }
                }
            }
            reader.close();
        } catch (IOException e) {
            System.out.println("Failed to load " + file + "!");
        }
    }

    private static void createSimpleTraditionalMap() {
        final String file = "simple2traditional.txt";
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(PinyinHelper.class.getResourceAsStream("/"
                    + file)));
            String line;
            while (null != (line = reader.readLine())) {
                String[] parts = line.split(Helper.SEP_PARTS);
                if (parts.length == 2) {
                    String map = parts[1];
                    int length = map.length();
                    if (parts[0].equals("sortedSimple")) {
                        CODEPOINTS_ST_SIMPLE = new int[length];
                        for (int i = 0; i < length; i++) {
                            CODEPOINTS_ST_SIMPLE[i] = map.codePointAt(i);
                        }
                    } else if (parts[0].equals("sortedTraditional")) {
                        CODEPOINTS_ST_TRADITIONAL = new int[length];
                        for (int i = 0; i < length; i++) {
                            CODEPOINTS_ST_TRADITIONAL[i] = map.codePointAt(i);
                        }
                    }
                }
            }
            reader.close();
        } catch (IOException e) {
            System.out.println("Failed to load " + file + "!");
        }
    }

    public static final boolean containsChinese(String str) {
        for (int i = 0; i < str.length(); i++) {
            // CJK Unified Ideographs 4E00-9FFF Common
            // CJK Unified Ideographs Extension A 3400-4DFF Rare
            // CJK Unified Ideographs Extension B 20000-2A6DF Rare, historic
            // CJK Compatibility Ideographs F900-FAFF Duplicates, unifiable variants, corporate characters
            // CJK Compatibility Ideographs Supplement 2F800-2FA1F Unifiable variants
            int cp = str.codePointAt(i);
            if ((cp > 0x4e00 && cp < 0x9fff) || (cp > 0x3400 && cp < 0x4Dff) || (cp > 0x20000 && cp < 0x2a6df)
                    || (cp > 0xf900 && cp < 0xfaff) || (cp > 0x2f800 && cp < 0x2fa1f)) {
                return true;
            }
        }
        return false;
    }
}
