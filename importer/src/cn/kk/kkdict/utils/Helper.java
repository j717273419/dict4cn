package cn.kk.kkdict.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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
import cn.kk.kkdict.types.Category;

public final class Helper {
    public static final int BUFFER_SIZE = 1024 * 1024 * 4;
    public static final Charset CHARSET_EUCJP = Charset.forName("EUC-JP");
    public static final Charset CHARSET_UTF16LE = Charset.forName("UTF-16LE");
    public static final Charset CHARSET_UTF8 = Charset.forName("UTF-8");
    public static final String DIR_IN_DICTS = "X:\\kkdict\\in\\dicts";
    public static final String DIR_IN_WORDS = "X:\\kkdict\\in\\words";
    // public static final String DIR_OUT_DICTS = "O:\\kkdict\\out\\dicts";
    // public static final String DIR_OUT_GENERATED = "O:\\kkdict\\out\\generated";
    // public static final String DIR_OUT_WORDS = "O:\\kkdict\\out\\words";
    public static final String DIR_OUT_DICTS = "C:\\usr\\kkdict\\out\\dicts";
    public static final String DIR_OUT_GENERATED = "C:\\usr\\kkdict\\out\\generated";
    public static final String DIR_OUT_WORDS = "C:\\usr\\kkdict\\out\\words";

    public final static String EMPTY_STRING = "";
    public final static List<String> EMPTY_STRING_LIST = Collections.emptyList();
    public static final String[] FEN_MU = { "c", "d", "b", "f", "g", "h", "ch", "j", "k", "l", "m", "n", "", "p", "q",
            "r", "s", "t", "sh", "zh", "w", "x", "y", "z" };
    private static final String[][] HTML_ENTITIES = { { "fnof", "402" }, { "Alpha", "913" }, { "Beta", "914" },
            { "Gamma", "915" }, { "Delta", "916" }, { "Epsilon", "917" }, { "Zeta", "918" }, { "Eta", "919" },
            { "Theta", "920" }, { "Iota", "921" }, { "Kappa", "922" }, { "Lambda", "923" }, { "Mu", "924" },
            { "Nu", "925" }, { "Xi", "926" }, { "Omicron", "927" }, { "Pi", "928" }, { "Rho", "929" },
            { "Sigma", "931" }, { "Tau", "932" }, { "Upsilon", "933" }, { "Phi", "934" }, { "Chi", "935" },
            { "Psi", "936" }, { "Omega", "937" }, { "alpha", "945" }, { "beta", "946" }, { "gamma", "947" },
            { "delta", "948" }, { "epsilon", "949" }, { "zeta", "950" }, { "eta", "951" }, { "theta", "952" },
            { "iota", "953" }, { "kappa", "954" }, { "lambda", "955" }, { "mu", "956" }, { "nu", "957" },
            { "xi", "958" }, { "omicron", "959" }, { "pi", "960" }, { "rho", "961" }, { "sigmaf", "962" },
            { "sigma", "963" }, { "tau", "964" }, { "upsilon", "965" }, { "phi", "966" }, { "chi", "967" },
            { "psi", "968" }, { "omega", "969" }, { "thetasym", "977" }, { "upsih", "978" }, { "piv", "982" },
            { "bull", "8226" }, { "hellip", "8230" }, { "prime", "8242" }, { "Prime", "8243" }, { "oline", "8254" },
            { "frasl", "8260" }, { "weierp", "8472" }, { "image", "8465" }, { "real", "8476" }, { "trade", "8482" },
            { "alefsym", "8501" }, { "larr", "8592" }, { "uarr", "8593" }, { "rarr", "8594" }, { "darr", "8595" },
            { "harr", "8596" }, { "crarr", "8629" }, { "lArr", "8656" }, { "uArr", "8657" }, { "rArr", "8658" },
            { "dArr", "8659" }, { "hArr", "8660" }, { "forall", "8704" }, { "part", "8706" }, { "exist", "8707" },
            { "empty", "8709" }, { "nabla", "8711" }, { "isin", "8712" }, { "notin", "8713" }, { "ni", "8715" },
            { "prod", "8719" }, { "sum", "8721" }, { "minus", "8722" }, { "lowast", "8727" }, { "radic", "8730" },
            { "prop", "8733" }, { "infin", "8734" }, { "ang", "8736" }, { "and", "8743" }, { "or", "8744" },
            { "cap", "8745" }, { "cup", "8746" }, { "int", "8747" }, { "there4", "8756" }, { "sim", "8764" },
            { "cong", "8773" }, { "asymp", "8776" }, { "ne", "8800" }, { "equiv", "8801" }, { "le", "8804" },
            { "ge", "8805" }, { "sub", "8834" }, { "sup", "8835" }, { "sube", "8838" }, { "supe", "8839" },
            { "oplus", "8853" }, { "otimes", "8855" }, { "perp", "8869" }, { "sdot", "8901" }, { "lceil", "8968" },
            { "rceil", "8969" }, { "lfloor", "8970" }, { "rfloor", "8971" }, { "lang", "9001" }, { "rang", "9002" },
            { "loz", "9674" }, { "spades", "9824" }, { "clubs", "9827" }, { "hearts", "9829" }, { "diams", "9830" },
            { "OElig", "338" }, { "oelig", "339" }, { "Scaron", "352" }, { "scaron", "353" }, { "Yuml", "376" },
            { "circ", "710" }, { "tilde", "732" }, { "ensp", "8194" }, { "emsp", "8195" }, { "thinsp", "8201" },
            { "zwnj", "8204" }, { "zwj", "8205" }, { "lrm", "8206" }, { "rlm", "8207" }, { "ndash", "8211" },
            { "mdash", "8212" }, { "lsquo", "8216" }, { "rsquo", "8217" }, { "sbquo", "8218" }, { "ldquo", "8220" },
            { "rdquo", "8221" }, { "bdquo", "8222" }, { "dagger", "8224" }, { "Dagger", "8225" }, { "permil", "8240" },
            { "lsaquo", "8249" }, { "rsaquo", "8250" }, { "euro", "8364" }, };
    private static final String[] HTML_KEYS;

    private static final int[] HTML_VALS;
    public final static int MAX_CONNECTIONS = 2;

    public static final int MAX_LINE_BYTES = 1024;

    public static final int MAX_LINE_BYTES_NORMAL = 1024 * 4;

    public static final int MAX_LINE_BYTES_MEDIUM = 1024 * 16;

    public static final int MAX_LINE_BYTES_BIG = 1024 * 32;

    public final static String SEP_ATTRIBUTE = "‹";

    public final static String SEP_DEFINITION = "═";

    public final static String SEP_LIST = "▫";

    public final static String SEP_NEWLINE = "\n";

    public final static String SEP_PARTS = "║";

    public final static String SEP_PINYIN = "'";

    public static final String SEP_SAME_MEANING = "¶";

    public static final String SEP_SPACE = " ";

    public final static String SEP_WORDS = "│";

    public static final String[] YUN_MU = { "uang", "iang", "iong", "ang", "eng", "ian", "iao", "ing", "ong", "uai",
            "uan", "ai", "an", "ao", "ei", "en", "er", "ua", "ie", "in", "iu", "ou", "ia", "ue", "ui", "un", "uo", "a",
            "e", "i", "o", "u", "v" };
    public static final byte[] SEP_DEFINITION_BYTES = Helper.SEP_DEFINITION.getBytes(Helper.CHARSET_UTF8);
    public static final byte[] SEP_LIST_BYTES = Helper.SEP_LIST.getBytes(Helper.CHARSET_UTF8);

    static {
        Arrays.sort(HTML_ENTITIES, new Comparator<String[]>() {
            @Override
            public int compare(String[] o1, String[] o2) {
                return o1[0].compareTo(o2[0]);
            }
        });
        HTML_KEYS = new String[HTML_ENTITIES.length];
        HTML_VALS = new int[HTML_ENTITIES.length];
        int i = 0;
        for (String[] pair : HTML_ENTITIES) {
            HTML_KEYS[i] = pair[0];
            HTML_VALS[i] = Integer.parseInt(pair[1]);
            i++;
        }
    }

    public static void add(Map<String, Integer> statMap, String key) {
        Integer counter = statMap.get(key);
        if (counter == null) {
            statMap.put(key, Integer.valueOf(1));
        } else {
            statMap.put(key, Integer.valueOf(counter.intValue() + 1));
        }
    }

    public static String appendCategories(String word, Set<String> categories) {
        if (categories.isEmpty()) {
            return word;
        } else {
            StringBuilder sb = new StringBuilder(word.length() * 2);
            sb.append(word);
            sb.append(Helper.SEP_ATTRIBUTE).append(Category.TYPE_ID);
            for (String c : categories) {
                sb.append(c);
            }
            return sb.toString();
        }
    }

    public static final String appendFileName(String file, String suffix) {
        int indexOf = file.indexOf('.');
        return file.substring(0, indexOf) + suffix + file.substring(indexOf);
    }

    public static final void changeWordLanguage(Word word, String currentLng, Map<String, String> translations) {
        if (translations.containsKey(currentLng)) {
            translations.put(currentLng, word.getName());
            String enDef = translations.get(currentLng);
            word.setName(enDef);
            translations.remove(currentLng);
        }
    }

    public static final boolean checkValidPinyin(String pinyin) {
        final String[] parts = pinyin.split(Helper.SEP_PINYIN);
        for (String part : parts) {
            if (null == Helper.getShenMuYunMu(part)) {
                return false;
            }
        }
        return true;
    }

    public static ByteBuffer compressFile(String rawFile, int level) throws IOException {
        InputStream in = new FileInputStream(rawFile);
        ByteArrayOutputStream dataOut = new ByteArrayOutputStream(BUFFER_SIZE);
        Deflater def = new Deflater(level);
        OutputStream out = new DeflaterOutputStream(dataOut, def, BUFFER_SIZE);
        writeInputStream(in, out);
        in.close();
        def.end();
        return ByteBuffer.wrap(dataOut.toByteArray());
    }

    public static ByteBuffer compressFile(String rawFile, String dictionaryFile, int level) throws IOException {
        InputStream in = new FileInputStream(rawFile);
        ByteArrayOutputStream dataOut = new ByteArrayOutputStream(BUFFER_SIZE);
        Deflater def = new Deflater(level);
        def.setDictionary(readBytes(dictionaryFile).array());
        OutputStream out = new DeflaterOutputStream(dataOut, def, BUFFER_SIZE);
        writeInputStream(in, out);
        in.close();
        return ByteBuffer.wrap(dataOut.toByteArray());
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

    public static ByteBuffer decompressFile(String compressedFile) throws IOException {
        InflaterInputStream in = new InflaterInputStream(new FileInputStream(compressedFile));
        ByteArrayOutputStream dataOut = new ByteArrayOutputStream(BUFFER_SIZE);
        writeInputStream(in, dataOut);
        in.close();
        return ByteBuffer.wrap(dataOut.toByteArray());
    }

    public static ByteBuffer decompressFile(String compressedFile, String dictionaryFile) throws IOException {
        Inflater inf = new Inflater();
        InputStream in = new InflaterInputStream(new FileInputStream(compressedFile), inf, BUFFER_SIZE);
        System.out.println(in.read());
        if (inf.needsDictionary()) {
            try {
                inf.setDictionary(readBytes(dictionaryFile).array());
                ByteArrayOutputStream dataOut = new ByteArrayOutputStream(BUFFER_SIZE);
                writeInputStream(in, dataOut);
                in.close();
                return ByteBuffer.wrap(dataOut.toByteArray());
            } catch (IllegalArgumentException e) {
                System.err.println("Invalid dictionary!");
                return null;
            } finally {
                inf.end();
            }
        } else {
            System.err.println("No dictionary needed!");
            inf.end();
            return null;
        }

    }

    public static String download(String url) throws IOException {
        return download(url, File.createTempFile("kkdl", null).getAbsolutePath(), true);
    }

    public static String download(String url, String file, boolean overwrite) throws IOException {
        System.out.println("下载'" + url + "'到'" + file + "'。。。");
        if (!overwrite && new File(file).exists()) {
            System.err.println("文件'" + file + "'已存在。跳过下载程序。");
            return null;
        } else {
            long start = System.currentTimeMillis();
            URL urlObj = new URL(url);
            URLConnection conn = urlObj.openConnection();
            conn.addRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:10.0.1) Gecko/20100101 Firefox/10.0.1");
            conn.addRequestProperty("Cache-Control", "no-cache");
            conn.addRequestProperty("Pragma", "no-cache");
            OutputStream out = new BufferedOutputStream(new FileOutputStream(file), BUFFER_SIZE);
            InputStream in = new BufferedInputStream(conn.getInputStream(), BUFFER_SIZE);
            writeInputStream(in, out);
            in.close();
            out.close();
            long duration = System.currentTimeMillis() - start;
            System.out.println("下载文件'" + url + "'用时" + Helper.formatDuration(duration) + "（"
                    + Math.round(new File(file).length() / (duration / 1000.0) / 1024.0) + "kbps）。");
            return file;
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

    /**
     * 
     * @param text
     *            text to analyze
     * @param definitions
     *            array of array of definitions with the first element as its key
     * @return {cutted text, definition key } if found definition otherwise null
     */
    public static String[] findAndCut(String text, String[][] definitions) {
        String def = null;
        int indexOf;
        for (String[] defArray : definitions) {
            for (int g = 1; g < defArray.length; g++) {
                String k = defArray[g];
                if ((indexOf = text.indexOf(k)) != -1) {
                    def = defArray[0];
                    if (indexOf + k.length() == text.length()) {
                        text = text.substring(0, indexOf);
                    } else {
                        text = text.substring(0, indexOf) + text.substring(indexOf + k.length());
                    }
                    break;
                }
            }
        }
        if (def != null) {
            return new String[] { text, def };
        } else {
            return null;
        }
    }

    public final static String formatDuration(final long duration) {
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

    public static final String formatSpace(final long limit) {
        if (limit < 1024) {
            return limit + " B";
        }
        if (limit < 1024 * 1024) {
            return Math.round(limit / 10.24) / 100.0 + " KB";
        }
        if (limit < 1024 * 1024 * 1024) {
            return Math.round(limit / 1024 / 10.24) / 100.0 + " MB";
        }
        if (limit < 1024 * 1024 * 1024 * 1024) {
            return Math.round(limit / 1024.0 / 1024.0 / 10.24) / 100.0 + " MB";
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

    private final static int indexOf(final char[][] pairs, final char c) {
        int i = 0;
        for (final char[] pair : pairs) {
            if (c == pair[0]) {
                return i;
            }
            i++;
        }
        return -1;
    }

    public static boolean isEmptyOrNull(String text) {
        return text == null || text.isEmpty();
    }

    public final static boolean isNotEmptyOrNull(String text) {
        return text != null && text.length() > 0;
    }

    private static boolean isNumber(char c) {
        return c >= '0' && c <= '9';
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
                .getBytes(CHARSET_UTF8);
        System.out.println(contains(text, 0, text.length, "</namespace>".getBytes(CHARSET_UTF8)));
        System.out.println(substringBetween(text, 0, text.length, "\">".getBytes(CHARSET_UTF8),
                "</namespace>".getBytes(CHARSET_UTF8)));
        ByteBuffer bbBuffer = ByteBuffer.allocate(MAX_LINE_BYTES);
        substringBetween(text, 0, text.length, "\">".getBytes(CHARSET_UTF8), "</namespace>".getBytes(CHARSET_UTF8),
                bbBuffer);
        System.out.println(toString(bbBuffer));

        substringBetweenLast(text, 0, text.length, "\">".getBytes(CHARSET_UTF8), "</namespace>".getBytes(CHARSET_UTF8),
                bbBuffer);
        System.out.println(toString(bbBuffer));

        System.out.println(substringBetweenLast(text, 0, text.length, "\">".getBytes(CHARSET_UTF8),
                "</namespace>".getBytes(CHARSET_UTF8)));
        System.out.println(indexOf(text, 1, text.length - 1, "namespace".getBytes(CHARSET_UTF8), 0, 4));
        bbBuffer.put((byte) 1);
        bbBuffer.put((byte) 2);
        bbBuffer.put((byte) 3);
        bbBuffer.put((byte) 4);
        bbBuffer.put((byte) 5);
        bbBuffer.put((byte) 6);
        bbBuffer.put((byte) 7);
        bbBuffer.put((byte) 8);
        substring(bbBuffer, 1);
        System.out.println(Helper.toHexString(bbBuffer));
    }

    public final static String padding(long value, int len, char c) {
        return padding(String.valueOf(value), len, c);
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

    public static Set<String> parseCategories(File file) {
        String cats = Helper.substringBetween(file.getName(), "(", ")");
        Set<String> categories = new FormattedTreeSet<String>();
        if (Helper.isNotEmptyOrNull(cats)) {
            String[] cs = cats.split(",");
            for (String c : cs) {
                categories.add(c);
            }
        }
        return categories;
    }

    public static String[] parseLanguages(File file) {
        String base = file.getName().substring(0, file.getName().indexOf('.'));
        int idx2 = base.lastIndexOf('_');
        int idx1 = base.lastIndexOf('_', idx2 - 1);
        String lng1 = base.substring(idx1 + 1, idx2);
        String lng2 = base.substring(idx2 + 1);
        return new String[] { lng1, lng2 };
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

    public static ByteBuffer readBytes(String file) throws IOException {
        ByteArrayOutputStream dataOut = new ByteArrayOutputStream();
        FileChannel fChannel = new RandomAccessFile(file, "r").getChannel();
        fChannel.transferTo(0, fChannel.size(), Channels.newChannel(dataOut));
        fChannel.close();
        return ByteBuffer.wrap(dataOut.toByteArray());
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
        bb.rewind().limit(bb.capacity());
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
                // if (--len > bb.limit()) {
                // System.err.println("跳过超长部分：总" + len + "字符，跳过" + (len - bb.limit()) + "字符");
                // }
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

    public final static Map<String, String> readMapStringString(String text) {
        final String[] many = text.split(Helper.SEP_LIST);
        final Map<String, String> map = new FormattedTreeMap<String, String>();
        for (String d : many) {
            String[] defs = d.split(Helper.SEP_DEFINITION);
            if (defs.length == 2) {
                map.put(defs[0], defs[1]);
            }
        }
        return map;
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

    public final static Set<String> readSetString(String text) {
        final String[] many = text.split(Helper.SEP_LIST);
        final Set<String> set = new FormattedTreeSet<String>();
        for (String d : many) {
            set.add(d);
        }
        return set;
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

    public static String stripCoreText(String line) {
        final int count = line.length();
        StringBuilder sb = new StringBuilder(count);
        int pairIdx = -1;
        char[][] pairs = { { '(', ')' }, { '{', '}' }, { '[', ']' }, { '（', '）' }, { '《', '》' }, { '［', '］' } };

        for (int i = 0; i < count; i++) {
            char c = line.charAt(i);
            if (i == count - 1 && c == ' ') {
                break;
            } else if (c == ' ' && (i + 1 < count && -1 != (pairIdx = indexOf(pairs, line.charAt(i + 1))))) {
                i++;
            } else if (-1 != (pairIdx = indexOf(pairs, line.charAt(i + 1)))) {
            } else if (-1 != pairIdx && c == pairs[pairIdx][1]) {
                pairIdx = -1;
                if (i + 1 < count && line.charAt(i + 1) == ' ') {
                    i++;
                }
            } else if (pairIdx == -1) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static String stripHtmlText(final String line, final boolean startOk) {
        final int count = line.length();
        StringBuilder sb = new StringBuilder(count);
        boolean ok = startOk;
        for (int i = 0; i < count; i++) {
            char c = line.charAt(i);
            if (c == '>') {
                ok = true;
            } else if (c == '<') {
                ok = false;
            } else if (ok) {
                sb.append(c);
            }
        }
        return sb.toString();
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

    public static final String substringBetween(final String text, final String start, final String end) {
        return substringBetween(text, start, end, true);
    }

    public static final String substringBetween(final String text, final String start, final String end,
            final boolean trim) {
        final int nStart = text.indexOf(start);
        final int nEnd = text.indexOf(end, nStart + start.length() + 1);
        if (nStart != -1 && nEnd > nStart) {
            if (trim) {
                return text.substring(nStart + start.length(), nEnd).trim();
            } else {
                return text.substring(nStart + start.length(), nEnd);
            }
        } else {
            return null;
        }
    }

    public static String substringBetweenEnclose(String text, String start, String end) {
        final int nStart = text.indexOf(start);
        final int nEnd = text.lastIndexOf(end);
        if (nStart != -1 && nEnd != -1 && nEnd > nStart + start.length()) {
            return text.substring(nStart + start.length(), nEnd);
        } else {
            return null;
        }
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

    public final static String substringBetweenLast(final String text, final String start, final String end) {
        return substringBetweenLast(text, start, end, true);
    }

    public final static String substringBetweenLast(final String text, final String start, final String end,
            final boolean trim) {
        int nEnd = text.lastIndexOf(end);
        int nStart = -1;
        if (nEnd > 1) {
            nStart = text.lastIndexOf(start, nEnd - 1);
        } else {
            return null;
        }
        if (nStart < nEnd && nStart != -1 && nEnd != -1) {
            if (trim) {
                return text.substring(nStart + start.length(), nEnd).trim();
            } else {
                return text.substring(nStart + start.length(), nEnd);
            }
        } else {
            return null;
        }

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

    public static String toConstantName(String line) {
        final int count = line.length();
        StringBuilder sb = new StringBuilder(count);
        line = line.replace("ß", "_");
        line = line.toUpperCase();
        for (int i = 0; i < count; i++) {
            char c = line.charAt(i);
            if (c == '.' || c == ':' || c == '?' || c == ',' || c == '*' || c == '=' || c == '„' || c == '“'
                    || c == ')' || c == '1' || c == '2' || c == '3' || c == '4') {
                continue;
            } else if (c == ' ' || c == '\'' || c == '-') {
                sb.append('_');
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static String toHexString(final byte[] data) {
        return toHexString(data, 0, data.length);
    }

    private static String toHexString(ByteBuffer bb) {
        return toHexString(bb.array(), 0, bb.limit());
    }

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

    private static void unescape(Writer writer, String str, int firstAmp) throws IOException {
        writer.write(str, 0, firstAmp);
        int len = str.length();
        for (int i = firstAmp; i < len; i++) {
            char c = str.charAt(i);
            if (c == '&') {
                int nextIdx = i + 1;
                int semiColonIdx = str.indexOf(';', nextIdx);
                if (semiColonIdx == -1) {
                    while (isNumber(str.charAt(i + 1))) {
                        i++;
                    }
                    semiColonIdx = i + 1;
                }
                int amphersandIdx = str.indexOf('&', i + 1);
                if (amphersandIdx != -1 && amphersandIdx < semiColonIdx) {
                    writer.write(c);
                    continue;
                }
                String entityContent = str.substring(nextIdx, semiColonIdx);
                int entityValue = -1;
                int entityContentLen = entityContent.length();
                if (entityContentLen > 0) {
                    if (entityContent.charAt(0) == '#') {
                        if (entityContentLen > 1) {
                            char isHexChar = entityContent.charAt(1);
                            try {
                                switch (isHexChar) {
                                case 'X':
                                case 'x': {
                                    entityValue = Integer.parseInt(entityContent.substring(2), 16);
                                    break;
                                }
                                default: {
                                    entityValue = Integer.parseInt(entityContent.substring(1), 10);
                                }
                                }
                                if (entityValue > 0xFFFF) {
                                    entityValue = -1;
                                }
                            } catch (NumberFormatException e) {
                                entityValue = -1;
                            }
                        }
                    } else {
                        int idx = Arrays.binarySearch(HTML_KEYS, entityContent);
                        if (idx >= 0) {
                            entityValue = HTML_VALS[idx];
                        }
                    }
                } else {
                    writer.write(c);
                    continue;
                }

                if (entityValue > -1) {
                    writer.write(entityValue);
                }
                i = semiColonIdx;
            } else {
                writer.write(c);
            }
        }

    }

    public static String unescapeHtml(String str) {
        try {
            int firstAmp = str.indexOf('&');
            if (firstAmp < 0) {
                return str;
            }

            StringWriter writer = new StringWriter((int) (str.length() * 1.1));
            unescape(writer, str, firstAmp);

            return writer.toString();
        } catch (IOException ioe) {
            return str;
        }
    }

    public static void writeBytes(byte[] data, String file) throws IOException {
        FileOutputStream f = new FileOutputStream(file);
        f.write(data);
        f.close();
    }

    private static void writeInputStream(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        int len;
        while ((len = in.read(buffer)) > 0) {
            out.write(buffer, 0, len);
        }
    }

    public static final boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return (path.delete());
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

    public final static byte[] toBytes(final ByteBuffer bb, int len) {
        byte[] result = new byte[len];
        System.arraycopy(bb.array(), 0, result, 0, len);
        return result;
    }

    public static int indexOf(ByteBuffer bb, byte b) {
        final int l = bb.limit();
        for (int i = 0; i < l; i++) {
            if (bb.get(i) == b) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 
     * @param tmpBB
     * @param startIdx
     * @return new bb limit
     */
    public static final int substring(final ByteBuffer tmpBB, final int startIdx) {
        final int limit = tmpBB.limit();
        if (startIdx >= limit) {
            tmpBB.limit(0);
            return 0;
        } else {
            byte[] array = tmpBB.array();
            int i = 0;
            int s = startIdx;
            while (s < limit) {
                array[i] = array[s];
                s++;
                i++;
            }
            tmpBB.limit(limit - startIdx);
            return tmpBB.limit();
        }
    }

    public static final String[] getFileNames(final File[] files) {
        String[] filePaths = new String[files.length];
        int i = 0;
        for (File f : files) {
            // System.out.println((i + 1) + ". " + f.getAbsolutePath());
            filePaths[i++] = f.getAbsolutePath();
        }
        return filePaths;
   }
}
