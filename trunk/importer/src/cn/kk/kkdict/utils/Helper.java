package cn.kk.kkdict.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import cn.kk.kkdict.beans.FormattedTreeMap;
import cn.kk.kkdict.beans.FormattedTreeSet;
import cn.kk.kkdict.beans.Word;

public final class Helper {
    public final static List<String> EMPTY_STRING_LIST = Collections.emptyList();
    public final static String EMPTY_STRING = "";
    public final static String SEP_NEWLINE = "\n";
    public final static String SEP_PARTS = "║";
    public final static String SEP_WORDS = "│";
    public final static String SEP_LIST = "▫";
    public final static String SEP_DEF = "═";
    public final static String SEP_PY = "'";

    public static final String[] FEN_MU = { "c", "d", "b", "f", "g", "h", "ch", "j", "k", "l", "m", "n", "", "p", "q",
            "r", "s", "t", "sh", "zh", "w", "x", "y", "z" };
    public static final String[] YUN_MU = { "uang", "iang", "iong", "ang", "eng", "ian", "iao", "ing", "ong", "uai",
            "uan", "ai", "an", "ao", "ei", "en", "er", "ua", "ie", "in", "iu", "ou", "ia", "ue", "ui", "un", "uo", "a",
            "e", "i", "o", "u", "v" };

    public static final String substringBetween(String text, String start, String end) {
        final int nStart = text.indexOf(start);
        final int nEnd = text.indexOf(end, nStart + 1);
        if (nStart < nEnd && nStart != -1 && nEnd != -1) {
            return text.substring(nStart + start.length(), nEnd);
        } else {
            return null;
        }
    }

    public final static String padding(String text, int len, char c) {
        if (text != null && len > text.length()) {
            char[] spaces = new char[len - text.length()];
            Arrays.fill(spaces, c);
            return new String(spaces) + text;
        } else {
            return text;
        }
    }

    public final static String padding(long value, int len, char c) {
        return padding(String.valueOf(value), len, c);
    }

    public final static String formatDuration(long duration) {
        final long v = Math.abs(duration);
        final long days = v / 1000 / 60 / 60 / 24;
        final long hours = (v / 1000 / 60 / 60) % 24;
        final long mins = (v / 1000 / 60) % 60;
        final long secs = (v / 1000) % 60;
        final long millis = v % 1000;
        StringBuilder out = new StringBuilder();
        if (days > 0) {
            out.append(days).append(':').append(padding(hours, 2, '0')).append(':').append(padding(mins, 2, '0'))
                    .append(":").append(padding(secs, 2, '0')).append(".").append(padding(millis, 3, '0'));
        } else if (hours > 0) {
            out.append(hours).append(':').append(padding(mins, 2, '0')).append(":").append(padding(secs, 2, '0'))
                    .append(".").append(padding(millis, 3, '0'));
        } else if (mins > 0) {
            out.append(mins).append(":").append(padding(secs, 2, '0')).append(".").append(padding(millis, 3, '0'));
        } else {
            out.append(secs).append(".").append(padding(millis, 3, '0'));
        }
        return out.toString();

    }

    public final static String substringBetweenLast(String text, String start, String end) {
        int nEnd = text.lastIndexOf(end);
        int nStart = -1;
        if (nEnd > 1) {
            nStart = text.lastIndexOf(start, nEnd - 1);
        } else {
            return null;
        }
        if (nStart < nEnd && nStart != -1 && nEnd != -1) {
            return text.substring(nStart + start.length(), nEnd);
        } else {
            return null;
        }

    }

    public final static boolean isNotEmptyOrNull(String text) {
        return text != null && text.length() > 0;
    }

    public final static void precheck(String inFile, String outDirectory) {
        if (!new File(inFile).isFile()) {
            System.err.println("Could not read input file: " + inFile);
            System.exit(-100);
        }
        if (!(new File(outDirectory).isDirectory() || new File(outDirectory).mkdirs())) {
            System.err.println("Could not create output directory: " + outDirectory);
            System.exit(-101);
        }
    }

    public final static Word readWikiWord(String line) {
        if (Helper.isNotEmptyOrNull(line)) {
            final String[] parts = line.split(Helper.SEP_PARTS);
            if (parts.length > 1) {
                final String name = parts[0];
                if (Helper.isNotEmptyOrNull(name)) {
                    final Word w = new Word();
                    w.setName(name);
                    final Map<String, String> translations = readMapStringString(parts[1]);
                    w.setTranslations(translations);
                    if (parts.length > 2) {
                        Set<String> categories = readSetString(parts[2]);
                        w.setCategories(categories);
                    }
                    return w;
                }
            }
        }
        return null;
    }

    public final static Set<String> readSetString(String text) {
        final String[] many = text.split(Helper.SEP_LIST);
        final Set<String> set = new FormattedTreeSet<String>();
        for (String d : many) {
            set.add(d);
        }
        return set;
    }

    public final static Map<String, String> readMapStringString(String text) {
        final String[] many = text.split(Helper.SEP_LIST);
        final Map<String, String> map = new FormattedTreeMap<String, String>();
        for (String d : many) {
            String[] defs = d.split(Helper.SEP_DEF);
            if (defs.length == 2) {
                map.put(defs[0], defs[1]);
            }
        }
        return map;
    }

    public static final void changeWordLanguage(Word word, String currentLng, Map<String, String> translations) {
        if (translations.containsKey(currentLng)) {
            translations.put(currentLng, word.getName());
            String enDef = translations.get(currentLng);
            word.setName(enDef);
            translations.remove(currentLng);
        }
    }

    public final static Word readPinyinWord(String line) {
        if (Helper.isNotEmptyOrNull(line)) {
            String[] parts = line.split(Helper.SEP_PARTS);
            if (parts.length == 2) {
                String name = parts[0];
                String pinyin = parts[1];
                if (Helper.isNotEmptyOrNull(name) && Helper.isNotEmptyOrNull(pinyin)) {
                    Word w = new Word();
                    w.setName(name);
                    w.setPronounciation(pinyin);
                    return w;
                }
            }
        }
        return null;
    }

    public static final String[] getShenMuYunMu(String pinyin) {
        for (String s : FEN_MU) {
            if (pinyin.startsWith(s)) {
                for (String y : YUN_MU) {
                    if (pinyin.endsWith(y)) {
                        if (pinyin.equals(s + y)) {
                            return new String[] { s, y };
                        }
                    }
                }
            }
        }
        return null;
    }

    public static final boolean checkValidPinyin(String pinyin) {
        final String[] parts = pinyin.split(Helper.SEP_PY);
        for (String part : parts) {
            if (null == Helper.getShenMuYunMu(part)) {
                return false;
            }
        }
        return true;
    }

    public static String substringBetweenNarrow(String text, String start, String end) {
        final int nEnd = text.indexOf(end);
        int nStart = -1;
        if (nEnd != -1) {
            nStart = text.lastIndexOf(start, nEnd - 1);
        }
        if (nStart < nEnd && nStart != -1 && nEnd != -1) {
            return text.substring(nStart + start.length(), nEnd);
        } else {
            return null;
        }
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
            System.out.print("BYTES: ");
            for (byte b : data) {
                int i = b & 0xff;
                if (i < 0xf) {
                    System.out.print(0);
                }
                System.out.print(Integer.toHexString(i) + " ");
            }
            System.out.println();
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

    public static ByteBuffer readBytes(String file) throws IOException {
        ByteArrayOutputStream dataOut = new ByteArrayOutputStream();
        FileChannel fChannel = new RandomAccessFile(file, "r").getChannel();
        fChannel.transferTo(0, fChannel.size(), Channels.newChannel(dataOut));
        fChannel.close();
        return ByteBuffer.wrap(dataOut.toByteArray());
    }

    public static ByteBuffer compressFile(String rawFile, int level) throws IOException {
        InputStream in = new FileInputStream(rawFile);
        ByteArrayOutputStream dataOut = new ByteArrayOutputStream(1024 * 8);
        OutputStream out = new DeflaterOutputStream(dataOut, new Deflater(level), 1024 * 8);
        writeInputStream(in, out);
        in.close();
        return ByteBuffer.wrap(dataOut.toByteArray());
    }

    public static ByteBuffer compressFile(String rawFile, String dictionaryFile, int level) throws IOException {
        InputStream in = new FileInputStream(rawFile);
        ByteArrayOutputStream dataOut = new ByteArrayOutputStream(1024 * 8);
        Deflater def = new Deflater(level);
        def.setDictionary(readBytes(dictionaryFile).array());
        OutputStream out = new DeflaterOutputStream(dataOut, def, 1024 * 8);
        writeInputStream(in, out);
        in.close();
        return ByteBuffer.wrap(dataOut.toByteArray());
    }

    public static ByteBuffer decompressFile(String compressedFile) throws IOException {
        InflaterInputStream in = new InflaterInputStream(new FileInputStream(compressedFile), new Inflater(), 1024 * 8);
        ByteArrayOutputStream dataOut = new ByteArrayOutputStream(1024 * 8);
        writeInputStream(in, dataOut);
        in.close();
        return ByteBuffer.wrap(dataOut.toByteArray());
    }

    public static ByteBuffer decompressFile(String compressedFile, String dictionaryFile)
            throws IOException {
        Inflater inf = new Inflater();
        InputStream in = new InflaterInputStream(new FileInputStream(compressedFile), inf, 1024 * 8);
        System.out.println(in.read());
        if (inf.needsDictionary()) {
            try {
                inf.setDictionary(readBytes(dictionaryFile).array());
                ByteArrayOutputStream dataOut = new ByteArrayOutputStream(1024 * 8);
                writeInputStream(in, dataOut);
                in.close();
                return ByteBuffer.wrap(dataOut.toByteArray());
            } catch (IllegalArgumentException e) {
                System.err.println("Invalid dictionary!");
                return null;
            }
        } else {
            System.err.println("No dictionary needed!");
            return null;
        }

    }

    private static void writeInputStream(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024 * 8];
        int len;
        while ((len = in.read(buffer)) > 0) {
            out.write(buffer, 0, len);
        }
    }

    public static void writeBytes(byte[] data, String file) throws IOException {
        FileOutputStream f = new FileOutputStream(file);
        f.write(data);
        f.close();
    }
}
