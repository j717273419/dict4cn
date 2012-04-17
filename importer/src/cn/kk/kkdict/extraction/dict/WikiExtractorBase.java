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

import cn.kk.kkdict.beans.WikiParseStep;
import cn.kk.kkdict.types.Language;
import cn.kk.kkdict.types.LanguageConstants;
import cn.kk.kkdict.types.TranslationSource;
import cn.kk.kkdict.utils.ArrayHelper;
import cn.kk.kkdict.utils.ChineseHelper;
import cn.kk.kkdict.utils.DictHelper;
import cn.kk.kkdict.utils.Helper;

/**
 * TODO 
 * - Redirects: <redirect title="Ҭенгиз Лакербаиа" />
 * - Abstracts: until first empty line or starting ==title==. remove ''', {}, [], [|(text)]
 *
 */
class WikiExtractorBase {

    static final boolean DEBUG = true;

    static final boolean TRACE = true;

    static final byte[] TAG_TEXT_BEGIN_BYTES = "space=\"preserve\">".getBytes(Helper.CHARSET_UTF8);
    
    static final byte[] KEY_ZH_BYTES = Language.ZH.key.getBytes(Helper.CHARSET_UTF8);

    static final byte[] PREFIX_WIKI_TAG_BYTES = "[[".getBytes(Helper.CHARSET_UTF8);

    static final byte[] SUFFIX_WIKI_TAG_BYTES = "]]".getBytes(Helper.CHARSET_UTF8);

    static final byte[] SUFFIX_TITLE_BYTES = "</title>".getBytes(Helper.CHARSET_UTF8);

    static final byte[] PREFIX_TITLE_BYTES = "<title>".getBytes(Helper.CHARSET_UTF8);
    
    static final int MIN_TITLE_LINE_BYTES = SUFFIX_TITLE_BYTES.length + PREFIX_TITLE_BYTES.length;

    static final byte[] SUFFIX_XML_TAG_BYTES = ">".getBytes(Helper.CHARSET_UTF8);

    static final byte[] ATTR_CATEGORY_KEY_BYTES = "key=\"14\"".getBytes(Helper.CHARSET_UTF8);

    static final byte[] SUFFIX_NAMESPACE_BYTES = "</namespace>".getBytes(Helper.CHARSET_UTF8);
    static final byte[] SUFFIX_NAMESPACES_BYTES = "</namespaces>".getBytes(Helper.CHARSET_UTF8);

    static final byte[] PREFIX_CATEGORY_KEY_EN_BYTES = "[[Category:".getBytes(Helper.CHARSET_UTF8);
    static final byte[] PREFIX_CATEGORY_KEY_EN2_BYTES = "[[:Category:".getBytes(Helper.CHARSET_UTF8);
    static final byte[] CATEGORY_KEY_BYTES = "Category:".getBytes(Helper.CHARSET_UTF8);

    static final int OK_NOTICE = 100000;

    public String inFile;
    public String outFile;
    public String outFileCategories;
    public String outFileRelated;

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
    byte[] name = null;
    final Set<byte[]> categories = new HashSet<byte[]>();
    final Map<byte[], byte[]> languages = new HashMap<byte[], byte[]>();
    final List<byte[]> relatedWords = new ArrayList<byte[]>();
    int statSkipped;
    int statOk;
    int statOkCategory;
    int statSkippedCategory;
    int statRelated;
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

    @Override
    protected void finalize() throws Throwable {
        ArrayHelper.giveBack(lineBB);
        ArrayHelper.giveBack(tmpBB);
        super.finalize();
    }
    
    void parseHeader() {
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
            step = WikiParseStep.TITLE;
        } else if (ArrayHelper.substringBetweenLast(lineArray, 0, lineLen, SUFFIX_XML_TAG_BYTES, SUFFIX_NAMESPACE_BYTES,
                tmpBB) > 0) {
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

    void signal() {
        if (++lineCount % OK_NOTICE == 0) {
            if (lineCount % (OK_NOTICE * 100) == 0 && lineCount != 0) {
                System.out.println(".");
            } else {
                System.out.print(".");
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
            System.out.println();
        }
        in = null;
        out = null;
        outRelated = null;
        outCategories = null;
    }

    boolean isValid() {
        return name != null;
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

    void initialize(final String f, final String outDir, final String outPrefix, final String outPrefixCategories,
            final String outPrefixRelated) throws IOException {
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

        name = null;
        categories.clear();
        languages.clear();
        relatedWords.clear();
        statSkipped = 0;
        statOk = 0;
        statOkCategory = 0;
        statSkippedCategory = 0;
        statRelated = 0;
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
    }

    void handleContentTitle() throws IOException {
        // name found
        boolean relevant = true;
        if (relevant) {
            for (byte[] prefix : irrelevantPrefixesBytes) {
                if (ArrayHelper.startsWith(tmpArray, tmpBB.limit(), prefix, prefix.length)) {
                    relevant = false;
                    break;
                }
            }
        }
        if (relevant) {
            if (chinese) {
                lineLen = ChineseHelper.toSimplifiedChinese(tmpBB);
            }
            name = ArrayHelper.toBytes(tmpBB);
            if (outFileCategories != null) {
                catName = isCategory();
            }
            if (DEBUG && !catName) {
                System.out.println("新词：" + ArrayHelper.toString(tmpBB));
                if ("faction".equals(ArrayHelper.toString(tmpBB))) {
                    System.out.println("break");
                }
            }
        } else {
            invalidate();
            statSkipped++;
        }
        step = WikiParseStep.TITLE;
    }

    protected void clearAttributes() {
        categories.clear();
        languages.clear();
        relatedWords.clear();
    }

    void invalidate() {
        name = null;
    }

    private boolean writeRelated() throws IOException {
        if (!relatedWords.isEmpty()) {
            if (DEBUG) {
                System.out.println(ArrayHelper.toString(name) + "：写出" + relatedWords.size() + "个相关词汇。");
            }
            outRelated.write(name);
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
            outRelated.write('\n');
            return true;
        } else {
            return false;
        }
    }

    final boolean isCategory() {
        return ArrayHelper.startsWith(name, catNameBytes);
    }

    boolean write() throws IOException {
        if (name != null && !languages.isEmpty()) {
            if (catName) {
                return writeCategory();
            } else {
                return writeDef();
            }
        }
        return false;
    }

    private boolean writeDef() throws IOException {
        languages.put(fileLngBytes, name);
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
        out.write('\n');
        return true;

    }

    private boolean writeCategory() throws IOException {
        languages.put(fileLngBytes, name);
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
        outCategories.write('\n');
        return true;
    }

    void addRelated() {
        int idx;
        idx = ArrayHelper.indexOf(tmpArray, 0, tmpBB.limit(), (byte) '|');
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

    void addTranslation(int idx) {
        if (chinese) {
            lineLen = ChineseHelper.toSimplifiedChinese(tmpBB);
        }
        byte[] tmpLngBytes = ArrayHelper.toBytes(tmpBB, idx);
        int i = 0;
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
                    languages.put(l, ArrayHelper.toBytes(tmpBB));
                }
                break;
            }
            i++;
        }
    }

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
}
