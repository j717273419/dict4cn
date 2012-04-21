package cn.kk.kkdict.extraction.dict;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.tools.bzip2.CBZip2InputStream;

import cn.kk.kkdict.beans.ByteArray;
import cn.kk.kkdict.beans.WikiParseStep;
import cn.kk.kkdict.types.Abstract;
import cn.kk.kkdict.types.Language;
import cn.kk.kkdict.types.LanguageConstants;
import cn.kk.kkdict.types.TranslationSource;
import cn.kk.kkdict.utils.ArrayHelper;
import cn.kk.kkdict.utils.ChineseHelper;
import cn.kk.kkdict.utils.DictHelper;
import cn.kk.kkdict.utils.Helper;

/**
 * TODO - Redirects: <redirect title="Ҭенгиз Лакербаиа" /> - Abstracts: until first empty line or starting ==title==.
 * remove ''', {}, [], [|(text)]
 * 
 */
class WikiExtractorBase {
    private static final int MIN_ABSTRACT_CHARS = 250;

    private static final byte[] TAG_IMAGEMAP_NAME_BYTES = "imagemap".getBytes(Helper.CHARSET_UTF8);

    private static final byte[][] OPEN_ENDS = { { '<', '>' }, { '{', '}' }, { '[', ']' } };

    private static void writeWikiVariable(final byte[] array, final int start, final int stop, final int walls,
            final int[] wallsIdx, final ByteBuffer outBB) {
        final int start0 = start;
        final int end0 = wallsIdx[0];
        final int start1 = end0 + 1;
        final int end1;
        final int start2;
        final int end2;
        if (walls > 2) {
            end1 = wallsIdx[1];
            start2 = end1 + 1;
            end2 = wallsIdx[2];
        } else if (walls > 1) {
            end1 = wallsIdx[1];
            start2 = end1 + 1;
            end2 = stop;
        } else {
            end1 = stop;
            start2 = -1;
            end2 = -1;
        }

        boolean reversed = false;
        boolean details = true;
        final int[] skipIdx = new int[ABSTRACT_SIMPLE_VARS_BYTES_LOWER.length];
        byte b;
        CHECK_SIMPLE: for (int i = start0; i < end0; i++) {
            final int len = end0 - start0;
            b = array[i];
            for (int j = 0; j < skipIdx.length; j++) {
                final int idx = skipIdx[j];
                final byte[] varKeyLower = ABSTRACT_SIMPLE_VARS_BYTES_LOWER[j];
                final byte[] varKeyUpper = ABSTRACT_SIMPLE_VARS_BYTES_UPPER[j];
                if (varKeyLower.length <= len) {
                    if (b == varKeyLower[idx] || b == varKeyUpper[idx]) {
                        if (idx + 1 < varKeyLower.length) {
                            skipIdx[j] = idx + 1;
                        } else {
                            details = false;
                            break CHECK_SIMPLE;
                        }
                    } else {
                        skipIdx[j] = 0;
                    }
                }
            }
        }
        if (ArrayHelper.equals(array, start0, BYTES_WIKI_ATTR_LANG)) {
            if (walls > 1) {
                reversed = true;
            }
        }

        // main word
        if (!reversed) {
            ArrayHelper.write(array, start1, end1, outBB);
        } else {
            ArrayHelper.write(array, start2, end2, outBB);
        }
        if (details) {
            outBB.put((byte) ' ').put((byte) '(');
            // first word
            ArrayHelper.write(array, start0, end0, outBB);
            // further words
            if (walls > 1) {
                outBB.put((byte) ',').put((byte) ' ');
                if (!reversed) {
                    ArrayHelper.write(array, start2, end2, outBB);
                } else {
                    ArrayHelper.write(array, start1, end1, outBB);
                }
                // else if (wallsIdx.length > 2) {
                // outBB.put((byte) ',').put((byte) ' ');
                // write(array, wallsIdx[2] + 1, (wallsIdx.length > 3 ? wallsIdx[3] : stop),
                // outBB);
                // }
            }
            outBB.put((byte) ')');
        }
    }

    private int opened = 0;

    private int openIdx = 0;
    public static final byte[] BYTES_WIKI_ANNOTATION_UNDERSCORE = { '_', '_' };

    public static final byte[] BYTES_WIKI_ANNOTATION_MINUS = { '-', '-', '-' };

    public static final byte[] BYTES_WIKI_ATTR_LANG = "lang".getBytes(Helper.CHARSET_UTF8);

    private static final byte[] COMMA_BYTES = "，".getBytes(Helper.CHARSET_UTF8);

    private static final byte[] POINT_BYTES = "。".getBytes(Helper.CHARSET_UTF8);

    private static final byte[] REDIRECT_UPPER_BYTES = "#redirect ".toUpperCase().getBytes(Helper.CHARSET_UTF8);

    private static final byte[] REDIRECT_LOWER_BYTES = "#redirect ".getBytes(Helper.CHARSET_UTF8);

    private static final int MAX_ABSTRACT_CHARS = 500;

    static final boolean DEBUG = false;

    static final boolean TRACE = false;
    static final byte[] TAG_TEXT_BEGIN_BYTES = "space=\"preserve\">".getBytes(Helper.CHARSET_UTF8);

    static final byte[] TAG_TEXT_END_BYTES = "</text>".getBytes(Helper.CHARSET_UTF8);

    static final byte[] TAG_REDIRECT_BEGIN_BYTES = "<redirect title=\"".getBytes(Helper.CHARSET_UTF8);

    static final byte[] KEY_ZH_BYTES = Language.ZH.key.getBytes(Helper.CHARSET_UTF8);

    static final byte[] PREFIX_WIKI_TAG_BYTES = "[[".getBytes(Helper.CHARSET_UTF8);

    static final byte[] SUFFIX_WIKI_TAG_BYTES = "]]".getBytes(Helper.CHARSET_UTF8);

    static final byte[] SUFFIX_TITLE_BYTES = "</title>".getBytes(Helper.CHARSET_UTF8);
    static final byte[] PREFIX_TITLE_BYTES = "<title>".getBytes(Helper.CHARSET_UTF8);

    static final byte[] SUFFIX_REDIRECT_BYTES = "\"".getBytes(Helper.CHARSET_UTF8);

    static final int MIN_TITLE_LINE_BYTES = SUFFIX_TITLE_BYTES.length + PREFIX_TITLE_BYTES.length;

    static final int MIN_REDIRECT_LINE_BYTES = TAG_REDIRECT_BEGIN_BYTES.length + SUFFIX_REDIRECT_BYTES.length;

    static final byte[] SUFFIX_XML_TAG_BYTES = ">".getBytes(Helper.CHARSET_UTF8);

    static final byte[] ATTR_CATEGORY_KEY_BYTES = "key=\"14\"".getBytes(Helper.CHARSET_UTF8);
    static final byte[] SUFFIX_NAMESPACE_BYTES = "</namespace>".getBytes(Helper.CHARSET_UTF8);

    static final byte[] SUFFIX_NAMESPACES_BYTES = "</namespaces>".getBytes(Helper.CHARSET_UTF8);
    static final byte[] PREFIX_CATEGORY_KEY_EN_BYTES = "[[Category:".getBytes(Helper.CHARSET_UTF8);
    static final byte[] PREFIX_CATEGORY_KEY_EN2_BYTES = "[[:Category:".getBytes(Helper.CHARSET_UTF8);
    static final byte[] CATEGORY_KEY_BYTES = "Category:".getBytes(Helper.CHARSET_UTF8);

    static final byte[][] ABSTRACT_SKIP_VARS_BYTES_LOWER = { "portal".getBytes(Helper.CHARSET_UTF8),
            "soft".getBytes(Helper.CHARSET_UTF8), "month".getBytes(Helper.CHARSET_UTF8),
            "day".getBytes(Helper.CHARSET_UTF8), "redirect".getBytes(Helper.CHARSET_UTF8),
            "info".getBytes(Helper.CHARSET_UTF8), "not".getBytes(Helper.CHARSET_UTF8),
            "path".getBytes(Helper.CHARSET_UTF8), "dablink".getBytes(Helper.CHARSET_UTF8),
            "dis".getBytes(Helper.CHARSET_UTF8), "merge".getBytes(Helper.CHARSET_UTF8),
            "year".getBytes(Helper.CHARSET_UTF8), "century".getBytes(Helper.CHARSET_UTF8),
            "c20".getBytes(Helper.CHARSET_UTF8), "other".getBytes(Helper.CHARSET_UTF8),
            "audio".getBytes(Helper.CHARSET_UTF8) };

    static final byte[][] ABSTRACT_SKIP_VARS_BYTES_UPPER = { "portal".toUpperCase().getBytes(Helper.CHARSET_UTF8),
            "soft".toUpperCase().getBytes(Helper.CHARSET_UTF8), "month".toUpperCase().getBytes(Helper.CHARSET_UTF8),
            "day".toUpperCase().getBytes(Helper.CHARSET_UTF8), "redirect".toUpperCase().getBytes(Helper.CHARSET_UTF8),
            "info".toUpperCase().getBytes(Helper.CHARSET_UTF8), "not".toUpperCase().getBytes(Helper.CHARSET_UTF8),
            "path".toUpperCase().getBytes(Helper.CHARSET_UTF8), "dablink".toUpperCase().getBytes(Helper.CHARSET_UTF8),
            "dis".toUpperCase().getBytes(Helper.CHARSET_UTF8), "merge".toUpperCase().getBytes(Helper.CHARSET_UTF8),
            "year".toUpperCase().getBytes(Helper.CHARSET_UTF8), "century".toUpperCase().getBytes(Helper.CHARSET_UTF8),
            "c20".toUpperCase().getBytes(Helper.CHARSET_UTF8), "other".toUpperCase().getBytes(Helper.CHARSET_UTF8),
            "audio".toUpperCase().getBytes(Helper.CHARSET_UTF8) };

    static final byte[][] ABSTRACT_SIMPLE_VARS_BYTES_LOWER = { "lang".getBytes(Helper.CHARSET_UTF8),
            "notetag".getBytes(Helper.CHARSET_UTF8), "bd".getBytes(Helper.CHARSET_UTF8) };

    static final byte[][] ABSTRACT_SIMPLE_VARS_BYTES_UPPER = { "lang".toUpperCase().getBytes(Helper.CHARSET_UTF8),
            "notetag".toUpperCase().getBytes(Helper.CHARSET_UTF8), "bd".toUpperCase().getBytes(Helper.CHARSET_UTF8) };

    static final int OK_NOTICE = 100000;
    public String inFile;
    public String outFile;
    public String outFileCategories;
    public String outFileRelated;
    public String outFileRedirects;

    public String outFileAbstracts;
    long started;
    byte[][] displayableLngs;
    byte[][] irrelevantPrefixesBytes;
    byte[] catKeyBytes;
    byte[] catKeyBytes2;
    byte[] catNameBytes;
    int minCatBytes = 0;
    WikiParseStep step = WikiParseStep.HEADER;
    int lineLen;
    int lineOffset;
    ByteBuffer name = ArrayHelper.borrowByteBufferSmall();
    ByteBuffer geoLocation = ArrayHelper.borrowByteBufferSmall();
    ByteBuffer imgLocation = ArrayHelper.borrowByteBufferSmall();
    final Set<byte[]> categories = new HashSet<byte[]>();
    final Map<byte[], byte[]> languages = new HashMap<byte[], byte[]>();
    final List<byte[]> relatedWords = new ArrayList<byte[]>();
    int statSkipped;
    int statOk;
    int statOkCategory;
    int statSkippedCategory;
    int statRelated;
    int statRedirects;
    int statAbstracts;
    boolean parseAbstract = true;
    long lineCount;
    boolean catName = false;
    final Set<byte[]> irrelevantPrefixes = new HashSet<byte[]>();
    ByteBuffer tmpBB;
    byte[] tmpArray;
    ByteBuffer lineBB;
    byte[] lineArray;
    boolean chinese = false;
    String fileLng;
    byte[] fileLngBytes;
    TranslationSource translationSource;
    BufferedInputStream in;
    BufferedOutputStream out;
    BufferedOutputStream outCategories;
    BufferedOutputStream outRelated;
    BufferedOutputStream outAbstracts;
    BufferedOutputStream outRedirects;

    ByteBuffer abstractBB = ArrayHelper.borrowByteBufferMedium();

    void addCategory() {
        int wildcardIdx = ArrayHelper.indexOf(tmpBB, (byte) '|');
        if (wildcardIdx != -1) {
            tmpBB.limit(wildcardIdx);
        }
        if (chinese) {
            lineLen = ChineseHelper.toSimplifiedChinese(tmpBB);
        }
        byte[] category = ArrayHelper.toBytes(tmpBB);
        categories.add(category);
        if (DEBUG) {
            System.out.println(">类别：" + ArrayHelper.toString(category));
        }
    }

    void addRelated() {
        if (ArrayHelper.indexOf(tmpArray, 0, tmpBB.limit(), (byte) ':') == -1) {
            final int idx = ArrayHelper.indexOf(tmpArray, 0, tmpBB.limit(), (byte) '|');
            if (idx != -1) {
                lineLen = idx;
            } else {
                lineLen = tmpBB.limit();
            }
            tmpBB.limit(lineLen);
            final byte[] relatedBytes;
            if (chinese) {
                ChineseHelper.toSimplifiedChinese(tmpBB);
                relatedBytes = ArrayHelper.toBytes(tmpBB);
            } else {
                relatedBytes = ArrayHelper.toBytes(tmpBB);
            }
            relatedWords.add(relatedBytes);
            if (DEBUG && TRACE) {
                System.out.println("相关：" + ArrayHelper.toString(relatedBytes));
            }
        }
    }

    void addTranslation(int idx) {
        byte[] tmpLngBytes = ArrayHelper.toBytes(tmpBB, idx);
        for (byte[] l : displayableLngs) {
            if (Arrays.equals(l, tmpLngBytes)) {
                if (ArrayHelper.substring(tmpBB, idx + 1) > 0) {
                    if (Arrays.equals(KEY_ZH_BYTES, l)) {
                        ChineseHelper.toSimplifiedChinese(tmpBB);
                        if (DEBUG) {
                            System.out.println("语言：" + ArrayHelper.toString(l) + "/"
                                    + ArrayHelper.toString(tmpLngBytes) + "，翻译：" + ArrayHelper.toString(tmpBB));
                        }
                    }
                    // TODO check if needed
                    // if (chinese) {
                    // lineLen = ChineseHelper.toSimplifiedChinese(tmpBB);
                    // }
                    languages.put(l, ArrayHelper.toBytes(tmpBB));
                }
                break;
            }
        }
    }

    protected void checkRedirectLine() throws IOException {
        if (step == WikiParseStep.PAGE) {
            if (lineLen > 14) {
                if (lineArray[0] == '#') {
                    boolean redirect = true;
                    for (int i = 1; i < REDIRECT_LOWER_BYTES.length; i++) {
                        final byte b = lineArray[i];
                        if (b != REDIRECT_LOWER_BYTES[i] && b != REDIRECT_UPPER_BYTES[i]) {
                            redirect = false;
                            break;
                        }
                    }
                    if (redirect
                            && -1 != ArrayHelper.substringBetween(lineArray, REDIRECT_LOWER_BYTES.length, lineLen,
                                    (byte) '[', (byte) ']', tmpBB, true)) {
                        writeRedirectLine();
                    }
                }
            }
        }
    }

    void cleanup() throws IOException {
        ArrayHelper.giveBack(tmpBB);
        ArrayHelper.giveBack(lineBB);
        in.close();
        out.close();
        if (outRelated != null) {
            outRelated.close();
        }
        if (outCategories != null) {
            outCategories.close();
        }
        if (outRedirects != null) {
            outRedirects.close();
        }
        if (outAbstracts != null) {
            outAbstracts.close();
        }

        System.out.println("\n> 成功分析'" + new File(inFile).getName() + "'（"
                + Helper.formatSpace(new File(inFile).length()) + "）文件，行数：" + lineCount + "，语言：" + fileLng + "，用时： "
                + Helper.formatDuration(System.currentTimeMillis() - started));
        System.out.print("> 字典文件：'" + outFile + "'（" + Helper.formatSpace(new File(outFile).length()) + "）");
        System.out.print("，定义：" + statOk);
        System.out.println("，跳过：" + statSkipped);
        if (outFileCategories != null) {
            System.out.print("> 类别文件：'" + outFileCategories + "'（"
                    + Helper.formatSpace(new File(outFileCategories).length()) + "）");
            System.out.print("，有效：" + statOkCategory);
            System.out.println("，跳过：" + statSkippedCategory);
        }
        if (outFileRelated != null) {
            System.out.print("> 相关文件：'" + outFileRelated + "'（" + Helper.formatSpace(new File(outFileRelated).length())
                    + "）");
            System.out.println("，定义：" + statRelated);

        }
        if (outFileRedirects != null) {
            System.out.print("> 重定向文件：'" + outFileRedirects + "'（"
                    + Helper.formatSpace(new File(outFileRedirects).length()) + "）");
            System.out.println("，重定向：" + statRedirects);
        }
        if (outFileAbstracts != null) {
            System.out.print("> 概要文件：'" + outFileAbstracts + "'（"
                    + Helper.formatSpace(new File(outFileAbstracts).length()) + "）");
            System.out.println("，概要：" + statAbstracts);
        }
        System.out.println();
        in = null;
        out = null;
        outRelated = null;
        outCategories = null;
    }

    protected void clearAttributes() {
        categories.clear();
        languages.clear();
        relatedWords.clear();
        parseAbstract = outAbstracts != null;
        opened = 0;
        geoLocation = null;
        if (parseAbstract) {
            abstractBB.clear();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        ArrayHelper.giveBack(lineBB);
        ArrayHelper.giveBack(tmpBB);
        ArrayHelper.giveBack(abstractBB);
        ArrayHelper.giveBack(name);
        ArrayHelper.giveBack(geoLocation);
        ArrayHelper.giveBack(imgLocation);
        super.finalize();
    }

    void handleContentTitle() throws IOException {
        // name found
        boolean relevant = true;
        if (relevant) {
            for (byte[] prefix : irrelevantPrefixesBytes) {
                if (ArrayHelper.startsWith(tmpBB, prefix)) {
                    relevant = false;
                    break;
                }
            }
        }
        if (relevant) {
            if (chinese) {
                lineLen = ChineseHelper.toSimplifiedChinese(tmpBB);
            }
            ArrayHelper.copy(tmpBB, name);
            if (outFileCategories != null) {
                catName = isCategory();
            }
            if (DEBUG && !catName) {
                System.out.println("新词：" + ArrayHelper.toString(tmpBB));
                if ("faction".equals(ArrayHelper.toString(tmpBB))) {
                    System.out.println("break");
                }
            }
            clearAttributes();
            step = WikiParseStep.TITLE_FOUND;
        } else {
            invalidate();
            statSkipped++;
        }
    }

    protected void writeRedirectLine() throws IOException {
        if (chinese) {
            ChineseHelper.toSimplifiedChinese(tmpBB);
            if (ArrayHelper.equals(name, tmpBB)) {
                tmpBB.limit(0);
            }
        }
        if (outRedirects != null && tmpBB.hasRemaining()) {
            outRedirects.write(fileLngBytes);
            outRedirects.write(Helper.SEP_DEFINITION_BYTES);
            outRedirects.write(name.array(), 0, name.limit());
            outRedirects.write(Helper.SEP_WORDS_BYTES);
            outRedirects.write(tmpArray, 0, tmpBB.limit());
            outRedirects.write(Helper.SEP_NEWLINE_CHAR);
            statRedirects++;
        }
        if (DEBUG) {
            System.out.println("重定向：" + ArrayHelper.toString(tmpBB) + " -> " + ArrayHelper.toString(name));
        }
        step = WikiParseStep.BEFORE_TITLE;
        invalidate();
    }

    protected void handleTextBeginLine(int idx) throws IOException {
        final int offset = idx + TAG_TEXT_BEGIN_BYTES.length;
        final int len = lineLen - offset;
        // boolean foundPageLayout = false;
        // if (len > 2 && lineArray[offset] == PAGE_LAYOUT_START_BYTES[0]
        // && lineArray[offset + 1] == PAGE_LAYOUT_START_BYTES[1]) {
        // foundPageLayout = true;
        // }
        // if (foundPageLayout) {
        // // content starting with layout
        // int stopPageLayoutIdx = ArrayHelper.indexOf(lineArray, offset, len, PAGE_LAYOUT_STOP_BYTES);
        // if (stopPageLayoutIdx == -1) {
        // // end tag not in same line
        // while (-1 != (lineLen = ArrayHelper.readLineTrimmed(in, lineBB))) {
        // ArrayHelper.trimP(lineBB);
        // if (ArrayHelper.equals(lineArray, lineBB.position(), PAGE_LAYOUT_STOP_BYTES)) {
        // break;
        // }
        // }
        // if (lineLen != -1) {
        // int off = lineBB.position() + PAGE_LAYOUT_STOP_BYTES.length;
        // lineLen = lineLen - off;
        // System.arraycopy(lineArray, off, lineArray, 0, lineLen);
        // lineBB.limit(lineLen);
        // }
        // } else {
        // int off = stopPageLayoutIdx + PAGE_LAYOUT_STOP_BYTES.length;
        // lineLen = lineLen - off;
        // System.arraycopy(lineArray, off, lineArray, 0, lineLen);
        // lineBB.limit(lineLen);
        // }
        // } else {
        lineLen = len;
        System.arraycopy(lineArray, offset, lineArray, 0, lineLen);
        lineBB.limit(lineLen);
        // }
        step = WikiParseStep.PAGE;
    }

    protected void handleTextEndLine(int idx) {
        lineBB.limit(idx);
        lineLen = lineBB.limit();
        step = WikiParseStep.BEFORE_TITLE;
    }

    void initialize(final String f, final String outDir, final String outPrefix, final String outPrefixCategories,
            final String outPrefixRelated, final String outPrefixAbstracts, final String outPrefixRedirects)
            throws IOException {
        started = System.currentTimeMillis();

        displayableLngs = new byte[LanguageConstants.KEYS_WIKI.length][];
        int i = 0;
        for (String k : LanguageConstants.KEYS_WIKI) {
            displayableLngs[i++] = k.getBytes(Helper.CHARSET_UTF8);
        }

        inFile = null;
        outFile = null;
        outFileCategories = null;
        outFileRelated = null;

        irrelevantPrefixesBytes = null;
        catKeyBytes = PREFIX_CATEGORY_KEY_EN_BYTES;
        catKeyBytes2 = PREFIX_CATEGORY_KEY_EN2_BYTES;
        minCatBytes = Math.min(catKeyBytes.length, catKeyBytes2.length) + SUFFIX_WIKI_TAG_BYTES.length;
        catNameBytes = CATEGORY_KEY_BYTES;
        step = WikiParseStep.HEADER;

        name.clear();
        categories.clear();
        languages.clear();
        relatedWords.clear();
        statSkipped = 0;
        statOk = 0;
        statOkCategory = 0;
        statSkippedCategory = 0;
        statRelated = 0;
        statRedirects = 0;
        statAbstracts = 0;
        lineCount = 0;
        catName = false;
        irrelevantPrefixes.clear();
        tmpBB = ArrayHelper.borrowByteBufferNormal();
        tmpArray = tmpBB.array();
        lineBB = ArrayHelper.borrowByteBufferNormal();
        lineArray = lineBB.array();
        chinese = false;

        inFile = f;
        System.out.println("< 分析'" + f + "' （" + Helper.formatSpace(new File(f).length()) + "）。。。");
        Helper.precheck(f, outDir);
        fileLng = DictHelper.getWikiLanguage(f).key;
        fileLngBytes = fileLng.getBytes(Helper.CHARSET_UTF8);
        chinese = Language.ZH.key.equalsIgnoreCase(fileLng);
        translationSource = TranslationSource.valueOf(Helper.toConstantName("wiki_" + fileLng));
        if (f.endsWith(".bz2")) {
            in = new BufferedInputStream(new CBZip2InputStream((new BufferedInputStream(new FileInputStream(f),
                    Helper.BUFFER_SIZE))), Helper.BUFFER_SIZE);
        } else {
            in = new BufferedInputStream(new FileInputStream(f), Helper.BUFFER_SIZE);
        }
        outFile = outDir + File.separator + outPrefix + fileLng;
        if (DEBUG) {
            System.out.println("写出：" + outFile + " 。。。");
        }
        out = new BufferedOutputStream(new FileOutputStream(outFile), Helper.BUFFER_SIZE);
        if (outPrefixCategories != null) {
            outFileCategories = outDir + File.separator + outPrefixCategories + fileLng;
            outCategories = new BufferedOutputStream(new FileOutputStream(outFileCategories), Helper.BUFFER_SIZE);
        }
        if (outPrefixRelated != null) {
            outFileRelated = outDir + File.separator + outPrefixRelated + fileLng;
            outRelated = new BufferedOutputStream(new FileOutputStream(outFileRelated), Helper.BUFFER_SIZE);
        }
        if (outPrefixRedirects != null) {
            outFileRedirects = outDir + File.separator + outPrefixRedirects + fileLng;
            outRedirects = new BufferedOutputStream(new FileOutputStream(outFileRedirects), Helper.BUFFER_SIZE);
        }
        if (outPrefixAbstracts != null) {
            outFileAbstracts = outDir + File.separator + outPrefixAbstracts + fileLng;
            outAbstracts = new BufferedOutputStream(new FileOutputStream(outFileAbstracts), Helper.BUFFER_SIZE);
        }
    }

    void invalidate() {
        name.clear();
    }

    final boolean isCategory() {
        return ArrayHelper.startsWith(name, catNameBytes);
    }

    boolean isValid() {
        return name.limit() != name.capacity();
    }

    protected void parseAbstract() {
        /*
         * if (-1 != ArrayHelper.indexOf(lineArray, 0, lineLen, "Abkhazia Gali map.svg".getBytes(Helper.CHARSET_UTF8)))
         * { System.out.println("debug"); }
         */
        byte firstByte = lineArray[0];
        final boolean hasContent = !ArrayHelper.isEmpty(abstractBB) && categories.isEmpty();
        if (firstByte == '=') {
            parseAbstract = false;
        }
        if (parseAbstract) {
            if (opened != 0) {
                // previous line not ended yet
                if (openIdx == 0) {
                    final int stop = ArrayHelper.indexOf(lineArray, 0, lineLen, Helper.BYTES_XML_TAG_STOP);
                    if (stop != -1) {
                        opened = 0;
                        lineBB.position(stop + Helper.BYTES_XML_TAG_STOP.length);
                    }
                } else {
                    byte b;
                    byte open = OPEN_ENDS[openIdx][0];
                    byte end = OPEN_ENDS[openIdx][1];
                    for (int i = 0; i < lineLen; i++) {
                        b = lineArray[i];
                        if (b == end) {
                            opened--;
                        } else if (b == open) {
                            opened++;
                        }
                        if (opened == 0) {
                            if (i + 1 < lineLen) {
                                lineBB.position(i + 1);
                            } else {
                                lineBB.position(i);
                            }
                            break;
                        }
                    }
                }
            }
            if (opened == 0 && lineBB.hasRemaining()) {
                if (hasContent) {
                    abstractBB.position(abstractBB.limit()).limit(abstractBB.capacity());
                    if (abstractBB.remaining() > 10) {
                        abstractBB.put((byte) ' ');
                    }
                } else {
                    abstractBB.clear();
                }
                stripWikiLineP(lineBB, abstractBB, MAX_ABSTRACT_CHARS);
                if (abstractBB.limit() > MIN_ABSTRACT_CHARS) {
                    parseAbstract = false;
                }
            }
            // if ("生物学".equals(ArrayHelper.toString(name))) {
            // System.out.println(ArrayHelper.toString(lineBB));
            // System.out.println(ArrayHelper.toString(abstractBB) + ", opened: " + opened);
            // }
        }
    }

    void parseDocumentHeader() {
        if (ArrayHelper.contains(lineArray, 0, lineLen, SUFFIX_NAMESPACES_BYTES)) {
            // finish prefixes
            irrelevantPrefixesBytes = new byte[irrelevantPrefixes.size()][];
            int i = 0;
            for (byte[] prefix : irrelevantPrefixes) {
                irrelevantPrefixesBytes[i] = prefix;
                i++;
            }
            if (DEBUG) {
                System.out.println("所有过滤前缀：");
                for (byte[] prefix : irrelevantPrefixesBytes) {
                    System.out.println("- " + ArrayHelper.toString(prefix));
                }
            }
            step = WikiParseStep.BEFORE_TITLE;
        } else if (ArrayHelper.substringBetweenLast(lineArray, 0, lineLen, SUFFIX_XML_TAG_BYTES,
                SUFFIX_NAMESPACE_BYTES, tmpBB) > 0) {
            // add prefix
            int limit = tmpBB.limit();
            tmpBB.limit(limit + 1);
            tmpBB.put(limit, (byte) ':');
            if (DEBUG) {
                System.out.println("找到域码：" + ArrayHelper.toString(tmpBB));
            }
            // System.out.println(ArrayHelper.toString(lineBB));
            if (ArrayHelper.contains(lineArray, 0, lineLen, ATTR_CATEGORY_KEY_BYTES)) {
                catKeyBytes = new byte[tmpBB.limit() + 2];
                catKeyBytes[0] = '[';
                catKeyBytes[1] = '[';
                System.arraycopy(tmpArray, 0, catKeyBytes, 2, tmpBB.limit());

                catKeyBytes2 = new byte[tmpBB.limit() + 3];
                catKeyBytes2[0] = '[';
                catKeyBytes2[1] = '[';
                catKeyBytes2[2] = ':';
                System.arraycopy(tmpArray, 0, catKeyBytes2, 3, tmpBB.limit());

                minCatBytes = Math.min(catKeyBytes.length, catKeyBytes2.length) + SUFFIX_WIKI_TAG_BYTES.length;
                catNameBytes = ArrayHelper.toBytes(tmpBB);
                if (DEBUG) {
                    System.out.println("找到类别代码：" + ArrayHelper.toString(tmpBB));
                }
                if (outFileCategories == null) {
                    irrelevantPrefixes.add(ArrayHelper.toBytes(tmpBB));
                    tmpBB.limit(tmpBB.limit() + 1);
                    byte[] catBytes2 = ArrayHelper.toBytes(tmpBB, 1, tmpBB.limit() - 1);
                    catBytes2[0] = ':';
                    irrelevantPrefixes.add(catBytes2);
                }
            } else {
                irrelevantPrefixes.add(ArrayHelper.toBytes(tmpBB));
            }
        }
    }

    protected void parseGeoLocation() {
        // TODO
        // http://dbpedia.hg.sourceforge.net/hgweb/dbpedia/dbpedia/file/945c24bdc54c/extraction/extractors/GeoExtractor.php
        // static $knownTemplates = array(
        //
        // '/\{\{coor [^|}]*(?:d|dm|dms)\|([^}]+)\}\}/i',
        //
        // '/\{\{coord\|([^}]+)\}\}/i',
        //
        // '/\{\{Geolinks[^|}]*\|([^}]*)\}\}/i',
        //
        // '/\{\{Mapit[^|}]*\|([^}]+)\}\}/i', /* redirect to Geolinks, always create titles */
        //
        // '/\{\{Koordinate[^|}]*\|([^}]+)\}\}/i',
        //
        // '/\{\{Coordinate[^|}]*\|([^}]+)\}\}/i',
        //
        // '/\{\{좌표[^|}]*\|([^}]+)\}\}/i',
        //
        // );
    }

    protected void parseImageLocation() {
        // TODO
        // http://dbpedia.hg.sourceforge.net/hgweb/dbpedia/dbpedia/file/945c24bdc54c/extraction/extractors/ImageExtractor.php
        // if (preg_match_all("~\[\[[a-zA-Z0-9-]+:([^]|]*\.(?:jpe?g|png|gif|svg))(\|[^]]*)?\]\]~i", $text, $match,
        // PREG_OFFSET_CAPTURE)) {
    }

    void signal() {
        if (++lineCount % OK_NOTICE == 0) {
            if (lineCount % (OK_NOTICE * 100) == 0 && lineCount != 0) {
                System.out.println(".");
            } else {
                System.out.print(".");
            }
        }
    }

    protected int stripWikiLine(final byte[] array, final int offset, final int limit, final ByteBuffer outBB,
            final int maxChars) {
        byte b;
        int countEquals = 0;
        int countQuos = 0;
        int countSpaces = 0;
        byte lastByte = -1;
        final int startPos = outBB.position();
        STRIP: for (int i = offset; i < limit; i++) {
            if (outBB.position() > maxChars) {
                break;
            }
            b = array[i];
            final boolean hasNext = i + 1 < limit;
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
                countSpaces++;
                continue;
            case '&':
                if (i + 8 < limit && ArrayHelper.equals(array, i, Helper.BYTES_XML_TAG_START)) {
                    i += Helper.BYTES_XML_TAG_START.length - 1;
                    int start = i + 1;
                    int stop = ArrayHelper.indexOf(array, start, limit - start, Helper.BYTES_XML_TAG_STOP);
                    if (stop != -1) {
                        if (stop - start == TAG_IMAGEMAP_NAME_BYTES.length
                                && ArrayHelper.equals(array, start, TAG_IMAGEMAP_NAME_BYTES)) {
                            // <imagemap>...</imagemap>
                            start = stop + Helper.BYTES_XML_TAG_STOP.length;
                            stop = ArrayHelper.indexOf(array, start, limit - start, Helper.BYTES_XML_TAG_STOP);
                            if (stop != -1) {
                                i = stop + Helper.BYTES_XML_TAG_STOP.length - 1;
                            } else {
                                opened = 1;
                                openIdx = 0;
                                break STRIP;
                            }
                        } else {
                            i = stop + Helper.BYTES_XML_TAG_STOP.length - 1;
                        }
                    } else {
                        opened = 1;
                        openIdx = 0;
                        break STRIP;
                    }
                } else if (i + 3 < limit) {
                    final int stop = ArrayHelper.indexOf(array, i, limit, (byte) ';');
                    if (stop != -1) {
                        i = stop;
                    }
                }
                continue;
            case '{':
                if (hasNext && array[i + 1] == '{' || array[i + 1] == '|') {
                    // begins with {{ or {|
                    opened = 1;
                    boolean writeValue = false;
                    if (array[i + 1] == '{') {
                        opened = 2;
                        writeValue = true;
                    }

                    final int start = i + 2;
                    int stop;
                    int walls = 0;
                    int[] wallsIdx = new int[3];
                    int[] skipIdx = new int[ABSTRACT_SKIP_VARS_BYTES_LOWER.length];
                    for (stop = start; stop < limit; stop++) {
                        b = array[stop];
                        if (b == '}') {
                            opened--;
                        } else if (b == '{') {
                            opened++;
                        }

                        if (writeValue) {
                            if (b == '=' || b == ':') {
                                writeValue = false;
                            } else if (b == '|') {
                                if (walls < wallsIdx.length) {
                                    wallsIdx[walls] = stop;
                                }
                                walls++;
                            }
                            for (int j = 0; j < skipIdx.length; j++) {
                                final int idx = skipIdx[j];
                                final byte[] varKeyLower = ABSTRACT_SKIP_VARS_BYTES_LOWER[j];
                                final byte[] varKeyUpper = ABSTRACT_SKIP_VARS_BYTES_UPPER[j];
                                if (b == varKeyLower[idx] || b == varKeyUpper[idx]) {
                                    if (idx + 1 < varKeyLower.length) {
                                        skipIdx[j] = idx + 1;
                                    } else {
                                        writeValue = false;
                                        break;
                                    }
                                } else {
                                    skipIdx[j] = 0;
                                }
                            }
                        }
                        if (opened == 0) {
                            break;
                        }
                    }
                    i = stop;
                    if (opened != 0) {
                        openIdx = 1;
                        break STRIP;
                    } else {
                        if (writeValue && walls < wallsIdx.length && walls > 0) {
                            // write e.g. 100km²
                            // System.out.println(ArrayHelper.toString(outBB.array(), 0, outBB.position()));
                            writeWikiVariable(array, start, stop - 1, walls, wallsIdx, outBB);
                            // System.out.println(ArrayHelper.toString(outBB.array(), 0, outBB.position()));
                        }
                    }
                }
                continue;
            case '}':
                continue;
            case '[':
                if (hasNext) {
                    opened = 1;
                    boolean writeValue = true;
                    boolean link = false;
                    boolean linkTitle = false;

                    final int start = i + 1;
                    int stop;
                    int walls = 0;
                    for (stop = start; stop < limit; stop++) {
                        b = array[stop];
                        if (b == '|') {
                            // wiki link title
                            walls++;
                            if (walls == 1) {
                                i = stop;
                            }
                        } else if (b == ']') {
                            opened--;
                        } else if (b == '[') {
                            opened++;
                            if (opened == 2 && stop == start + 1) {
                                // move
                                i = stop;
                            }
                        } else if (link && b == ' ') {
                            // link title
                            i = stop;
                            linkTitle = true;
                            link = false;
                        }
                        if (walls == 0) {
                            if (b == '=') {
                                writeValue = false;
                            } else if (b == ':') {
                                if (opened == 1) {
                                    link = true;
                                } else {
                                    writeValue = false;
                                }
                            }
                        }
                        if (opened == 0) {
                            break;
                        }
                    }
                    if (opened == 0) {
                        if (!writeValue || link || walls > 1) {
                            i = stop;
                        }
                    } else {
                        openIdx = 2;
                        break STRIP;
                    }
                }
                continue;
            case '|':
                continue;
            case ']':
                continue;
            case '\'':
                countQuos++;
                continue;
            case '=':
                countEquals++;
                continue;
                // case '*':
                // continue;
                // case '#':
                // continue;
            case '_':
                if (hasNext && array[i + 1] == '_') {
                    i++;
                    final int stop = ArrayHelper.indexOf(array, i, limit - i, BYTES_WIKI_ANNOTATION_UNDERSCORE);
                    if (stop != -1) {
                        i = stop + BYTES_WIKI_ANNOTATION_UNDERSCORE.length - 1;
                    }
                }
                continue;
            case '-':
                if (i + 6 < limit && array[i + 1] == '-' && array[i + 2] == '-') {
                    i = i + 2;
                    final int stop = ArrayHelper.indexOf(array, i, limit - i, BYTES_WIKI_ANNOTATION_MINUS);
                    if (stop != -1) {
                        i = stop + BYTES_WIKI_ANNOTATION_MINUS.length - 1;
                    }
                    continue;
                }
                break;
            }
            outBB.put(b);
            lastByte = b;
        }
        if (outBB.position() > maxChars) {
            int idx = -1;
            if (chinese) {
                idx = ArrayHelper.lastIndexOf(outBB.array(), startPos, outBB.position(), COMMA_BYTES);
                if (idx == -1) {
                    idx = ArrayHelper.lastIndexOf(outBB.array(), startPos, outBB.position(), POINT_BYTES);
                }
            }
            if (idx == -1) {
                idx = ArrayHelper.lastIndexOf(outBB.array(), startPos, outBB.position(), (byte) ',');
                if (idx == -1) {
                    idx = ArrayHelper.lastIndexOf(outBB.array(), startPos, outBB.position(), (byte) '.');
                    if (idx == -1) {
                        idx = ArrayHelper.lastIndexOf(outBB.array(), startPos, outBB.position(), (byte) ' ');
                    }
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

    public int stripWikiLine(final ByteBuffer inBB, final ByteBuffer outBB, final int maxChars) {
        final byte[] array = inBB.array();
        final int limit = inBB.limit();
        outBB.clear();
        return stripWikiLine(array, 0, limit, outBB, maxChars);
    }

    public int stripWikiLineP(final ByteBuffer inBB, final ByteBuffer outBB, final int maxChars) {
        final byte[] array = inBB.array();
        final int offset = inBB.position();
        final int limit = inBB.limit();
        return stripWikiLine(array, offset, limit, outBB, maxChars);
    }

    boolean write() throws IOException {
        if (isValid() && !languages.isEmpty()) {
            if (catName) {
                return writeCategory();
            } else {
                return writeDef();
            }
        }
        return false;
    }

    private boolean writeAbstract() throws IOException {
        if (outAbstracts != null && !ArrayHelper.isEmpty(abstractBB)) {
            if (DEBUG && TRACE) {
                System.out.println(ArrayHelper.toString(name) + "的概要：" + ArrayHelper.toString(abstractBB));
            }
            if (chinese) {
                ChineseHelper.toSimplifiedChinese(abstractBB);
            }
            // System.out.println(ArrayHelper.toString(name) + "的概要：" + ArrayHelper.toString(abstractBB));
            outAbstracts.write(fileLngBytes);
            outAbstracts.write(Helper.SEP_DEFINITION_BYTES);
            outAbstracts.write(name.array(), 0, name.limit());
            outAbstracts.write(Helper.SEP_ATTRS_BYTES);
            outAbstracts.write(Abstract.TYPE_ID_BYTES);
            outAbstracts.write(abstractBB.array(), 0, abstractBB.limit());
            outAbstracts.write(Helper.SEP_NEWLINE_CHAR);
            return true;
        }
        return false;
    }

    private boolean writeCategory() throws IOException {
        languages.put(fileLngBytes, name.array());
        Set<byte[]> lngs = languages.keySet();
        byte[] sourceStringBytes = (Helper.SEP_ATTRIBUTE + TranslationSource.TYPE_ID + translationSource.key)
                .getBytes(Helper.CHARSET_UTF8);
        ByteBuffer bb = ArrayHelper.borrowByteBufferMedium();
        for (byte[] l : lngs) {
            bb.clear();
            bb.put(languages.get(l)).put(sourceStringBytes);
            bb.limit(bb.position());
            languages.put(l, ArrayHelper.toBytes(bb));
        }
        ArrayHelper.giveBack(bb);

        Iterator<Entry<byte[], byte[]>> i = languages.entrySet().iterator();
        for (;;) {
            Entry<byte[], byte[]> e = i.next();
            byte[] key = e.getKey();
            byte[] value = e.getValue();
            int offset = ArrayHelper.indexOf(value, 0, value.length, (byte) ':') + 1;
            outCategories.write(key);
            outCategories.write(Helper.SEP_DEFINITION_BYTES);
            outCategories.write(value, offset, value.length - offset);
            if (!i.hasNext()) {
                break;
            }
            outCategories.write(Helper.SEP_LIST_BYTES);
        }
        outCategories.write(Helper.SEP_NEWLINE_CHAR);
        return true;
    }

    private boolean writeDef() throws IOException {
        languages.put(fileLngBytes, name.array());
        Set<byte[]> lngs = languages.keySet();
        byte[] sourceStringBytes = (Helper.SEP_ATTRIBUTE + TranslationSource.TYPE_ID + translationSource.key)
                .getBytes(Helper.CHARSET_UTF8);
        ByteBuffer bb = ArrayHelper.borrowByteBufferMedium();
        for (byte[] l : lngs) {
            bb.clear();
            bb.put(languages.get(l)).put(sourceStringBytes);
            // TODO
            if (!categories.isEmpty()) {
                // trans += Helper.SEP_ATTRIBUTE + Category.TYPE_ID;
            }
            bb.limit(bb.position());
            languages.put(l, ArrayHelper.toBytes(bb));
        }
        ArrayHelper.giveBack(bb);

        Iterator<Entry<byte[], byte[]>> i = languages.entrySet().iterator();
        for (;;) {
            Entry<byte[], byte[]> e = i.next();
            byte[] key = e.getKey();
            byte[] value = e.getValue();
            out.write(key);
            out.write(Helper.SEP_DEFINITION_BYTES);
            out.write(value);
            if (!i.hasNext()) {
                break;
            }
            out.write(Helper.SEP_LIST_BYTES);
        }
        out.write(Helper.SEP_NEWLINE_CHAR);
        return true;

    }

    void writeDefinition() throws IOException {
        if (isValid()) {
            if (catName) {
                if (outCategories != null) {
                    if (write()) {
                        if (DEBUG) {
                            System.out.println("类：" + ArrayHelper.toString(name));
                            System.out.print("翻译：");
                            Set<Entry<byte[], byte[]>> entrySet = languages.entrySet();
                            for (Entry<byte[], byte[]> entry : entrySet) {
                                System.out.print(ArrayHelper.toString(entry.getKey()) + "="
                                        + ArrayHelper.toString(entry.getValue()) + ", ");
                            }
                            System.out.println();
                        }
                        statOkCategory++;
                    } else {
                        statSkippedCategory++;
                    }
                }
            } else {
                if (write()) {
                    if (writeAbstract()) {
                        statAbstracts++;
                    }
                    if (writeRelated()) {
                        statRelated++;
                    }
                    statOk++;
                } else {
                    statSkipped++;
                }
            }
        }
    }

    private boolean writeRelated() throws IOException {
        if (!relatedWords.isEmpty()) {
            if (DEBUG) {
                System.out.println(ArrayHelper.toString(name) + "：写出" + relatedWords.size() + "个相关词汇。");
            }
            outRelated.write(name.array(), 0, name.limit());
            outRelated.write(Helper.SEP_DEFINITION_BYTES);
            boolean first = true;
            for (byte[] w : relatedWords) {
                if (first) {
                    first = false;
                } else {
                    outRelated.write(Helper.SEP_WORDS_BYTES);
                }

                outRelated.write(w);
            }
            outRelated.write(Helper.SEP_NEWLINE_CHAR);
            return true;
        } else {
            return false;
        }
    }
}