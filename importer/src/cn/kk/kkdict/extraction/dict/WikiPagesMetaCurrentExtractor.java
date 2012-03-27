package cn.kk.kkdict.extraction.dict;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

import cn.kk.kkdict.beans.FormattedTreeMap;
import cn.kk.kkdict.beans.FormattedTreeSet;
import cn.kk.kkdict.types.Language;
import cn.kk.kkdict.types.LanguageConstants;
import cn.kk.kkdict.types.TranslationSource;
import cn.kk.kkdict.utils.ChineseHelper;
import cn.kk.kkdict.utils.Helper;

public class WikiPagesMetaCurrentExtractor {
    private static final byte[] KEY_ZH_BYTES = Language.ZH.key.getBytes(Helper.CHARSET_UTF8);

    private static final byte[] PREFIX_WIKI_TAG_BYTES = "[[".getBytes(Helper.CHARSET_UTF8);

    private static final byte[] SUFFIX_WIKI_TAG_BYTES = "]]".getBytes(Helper.CHARSET_UTF8);

    private static final byte[] SUFFIX_TITLE_BYTES = "</title>".getBytes(Helper.CHARSET_UTF8);

    private static final byte[] PREFIX_TITLE_BYTES = "<title>".getBytes(Helper.CHARSET_UTF8);

    private static final byte[] SUFFIX_XML_TAG_BYTES = ">".getBytes(Helper.CHARSET_UTF8);

    private static final byte[] ATTR_CATEGORY_KEY_BYTES = "key=\"14\"".getBytes(Helper.CHARSET_UTF8);

    private static final byte[] SUFFIX_NAMESPACE_BYTES = "</namespace>".getBytes(Helper.CHARSET_UTF8);

    private static final byte[] SUFFIX_NAMESPACES_BYTES = "</namespaces>".getBytes(Helper.CHARSET_UTF8);

    private static final String PREFIX_CATEGORY_KEY_EN = "[[Category:";

    private static final int OK_NOTICE = 100000;

    public static final String IN_DIR = Helper.DIR_IN_DICTS + "\\wiki";

    public static final String OUT_DIR = Helper.DIR_OUT_DICTS + "\\wiki";

    private static final boolean DEBUG = false;

    private static final byte[][] DISPLAYABLE_LNGS;
    static {
        DISPLAYABLE_LNGS = new byte[LanguageConstants.KEYS_WIKI.length][];
        int i = 0;
        for (String k : LanguageConstants.KEYS_WIKI) {
            DISPLAYABLE_LNGS[i++] = k.getBytes(Helper.CHARSET_UTF8);
        }
    }

    private static ByteBuffer bb = ByteBuffer.allocate(Helper.MAX_LINE_BYTES);

    public static void main(String args[]) throws IOException {

        File directory = new File(IN_DIR);
        if (directory.isDirectory()) {
            System.out.print("搜索维基百科pages-meta-current.xml文件'" + IN_DIR + "' ... ");
            new File(OUT_DIR).mkdirs();

            File[] files = directory.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return (name.endsWith("-pages-meta-current.xml") || name.endsWith("-pages-meta-current.xml.bz2"))
                            && name.contains("wiki-");
                }
            });
            System.out.println(files.length);

            long total = 0;
            for (File f : files) {
                total += extractWikipediaPagesMetaCurrent(f);
            }

            System.out.println("\n=====================================");
            System.out.println("总共读取了" + files.length + "个wiki文件");
            System.out.println("有效词组：" + total);
            System.out.println("=====================================");
        }
    }

    private static int extractWikipediaPagesMetaCurrent(final File file) throws FileNotFoundException, IOException {
        System.out.println("\n读取wiki文件'" + file.getAbsolutePath() + "' 。。。");
        long timeStarted = System.currentTimeMillis();
        Helper.precheck(file.getAbsolutePath(), OUT_DIR);
        String LNG = file.getName().substring(0, file.getName().indexOf("wiki-"));
        TranslationSource translationSource = TranslationSource.valueOf(("wiki_" + LNG).toUpperCase());
        final boolean isChinese = Language.ZH.key.equalsIgnoreCase(LNG);
        BufferedInputStream in;
        if (file.getAbsolutePath().endsWith(".bz2")) {
            in = new BufferedInputStream(new BZip2CompressorInputStream((new BufferedInputStream(new FileInputStream(
                    file), Helper.BUFFER_SIZE))), Helper.BUFFER_SIZE);
        } else {
            in = new BufferedInputStream(new FileInputStream(file), Helper.BUFFER_SIZE);
        }
        String dictFile = OUT_DIR + File.separator + "output-dict_" + LNG + ".wiki_" + LNG;
        System.out.println("分析并写出：" + dictFile + " 。。。");
        BufferedWriter writer = new BufferedWriter(new FileWriter(dictFile), Helper.BUFFER_SIZE);

        int len;
        String name = null;
        Set<String> globalCategories = new FormattedTreeSet<String>();
        Set<String> categories = null;
        Map<String, String> languages = null;
        String tmp;

        int statSkipped = 0;
        int statOk = 0;
        long lineCount = 0;
        Set<String> irrelevantPrefixes = new HashSet<String>();
        boolean irrelevantPrefixesNeeded = true;
        byte[] categoryKeyBytes = PREFIX_CATEGORY_KEY_EN.getBytes(Helper.CHARSET_UTF8);

        while (-1 != (len = Helper.readLine(in, bb))) {
            if (DEBUG) {
                String str = new String(bb.array(), 0, len, Helper.CHARSET_UTF8);
                // if (str.contains("</namespace>")) {
                // System.out.println(str);
                // }
            }
            // System.out.println(new String(bb.array(), 0, len, Helper.CHARSET_UTF8));
            if (lineCount % OK_NOTICE == 0) {
                if (lineCount % (OK_NOTICE * 100) == 0 && lineCount != 0) {
                    System.out.println(".");
                } else {
                    System.out.print(".");
                }
            }
            if (isChinese) {
                len = ChineseHelper.toSimplifiedChinese(bb);
            }

            if (irrelevantPrefixesNeeded && Helper.contains(bb.array(), 0, len, SUFFIX_NAMESPACES_BYTES)) {
                irrelevantPrefixesNeeded = false;
            } else if (irrelevantPrefixesNeeded
                    && Helper.isNotEmptyOrNull(tmp = Helper.substringBetweenLast(bb.array(), 0, len,
                            SUFFIX_XML_TAG_BYTES, SUFFIX_NAMESPACE_BYTES))) {
                irrelevantPrefixes.add(tmp + ":");
                if (DEBUG) {
                    System.out.println("找到域码：" + tmp);
                }
                if (Helper.contains(bb.array(), 0, len, ATTR_CATEGORY_KEY_BYTES)) {
                    categoryKeyBytes = ("[[" + tmp + ":").getBytes(Helper.CHARSET_UTF8);
                    if (DEBUG) {
                        System.out.println("找到category代码：" + tmp);
                    }
                }

            } else if (Helper.isNotEmptyOrNull(tmp = Helper.substringBetween(bb.array(), 0, len, PREFIX_TITLE_BYTES,
                    SUFFIX_TITLE_BYTES))) {
                if (write(writer, LNG, translationSource, name, categories, languages)) {
                    statOk++;
                } else {
                    statSkipped++;
                }
                boolean relevant = true;
                for (String prefix : irrelevantPrefixes) {
                    if (tmp.startsWith(prefix)) {
                        relevant = false;
                        break;
                    }
                }
                if (!relevant) {
                    name = null;
                    statSkipped++;
                } else {
                    name = tmp;
                    categories = new FormattedTreeSet<String>();
                    languages = new FormattedTreeMap<String, String>();
                }
            } else if (name != null) {
                if (Helper.isNotEmptyOrNull(tmp = Helper.substringBetween(bb.array(), 0, len, categoryKeyBytes,
                        SUFFIX_WIKI_TAG_BYTES))) {
                    int wildcardIdx = tmp.indexOf('|');
                    if (wildcardIdx != -1) {
                        tmp = tmp.substring(0, wildcardIdx);
                    }
                    categories.add(tmp);
                    globalCategories.add(tmp);
                } else if (Helper.isNotEmptyOrNull(tmp = Helper.substringBetween(bb.array(), 0, len,
                        PREFIX_WIKI_TAG_BYTES, SUFFIX_WIKI_TAG_BYTES))) {
                    int idx = tmp.indexOf(':');
                    if (idx == 1 || idx == 2) {
                        byte[] lngBytes = tmp.substring(0, idx).getBytes(Helper.CHARSET_UTF8);
                        int i = 0;
                        for (byte[] lng : DISPLAYABLE_LNGS) {
                            if (Arrays.equals(lng, lngBytes)) {
                                tmp = tmp.substring(idx + 1);
                                if (Helper.isNotEmptyOrNull(tmp)) {
                                    if (Arrays.equals(KEY_ZH_BYTES, lng)) {
                                        tmp = ChineseHelper.toSimplifiedChinese(tmp);
                                    }
                                    languages.put(LanguageConstants.KEYS_ZH[i], tmp);
                                }
                                break;
                            }
                            i++;
                        }
                    }
                }
            }
            lineCount++;
        }
        if (write(writer, LNG, translationSource, name, categories, languages)) {
            statOk++;
        } else {
            statSkipped++;
        }
        in.close();
        writer.close();

        String categoriesFile = OUT_DIR + File.separator + "output-categories." + translationSource.key;
        System.out.println("\n写出类别文件：" + categoriesFile + "。。。");
        BufferedWriter categoriesWriter = new BufferedWriter(new FileWriter(categoriesFile), Helper.BUFFER_SIZE);
        for (String c : globalCategories) {
            categoriesWriter.write(c);
            categoriesWriter.write(Helper.SEP_NEWLINE);
        }
        categoriesWriter.close();
        System.out.println("\n==============\n成功读取 wiki_" + LNG + "文件，用时： "
                + Helper.formatDuration(System.currentTimeMillis() - timeStarted));
        System.out.println("总共类别：" + globalCategories.size());
        System.out.println("有效词组：" + statOk);
        System.out.println("跳过词组：" + statSkipped);
        System.out.println("文件行数：" + lineCount + "\n==============\n");
        return statOk;
    }

    private static boolean write(BufferedWriter writer, final String LNG, TranslationSource translationSource,
            String name, Set<String> categories, Map<String, String> languages) throws IOException {
        if (name != null) {
            if (!languages.isEmpty()) {
                languages.put(LNG, name);
                Set<String> lngs = languages.keySet();
                String sourceString = Helper.SEP_ATTRIBUTE + TranslationSource.TYPE_ID + translationSource.key;
                for (String lng : lngs) {
                    String trans = languages.get(lng) + sourceString;
                    // TODO
                    if (!categories.isEmpty()) {
                        // trans += Helper.SEP_ATTRIBUTE + Category.TYPE_ID;
                    }
                    languages.put(lng, trans);
                }
                writer.write(languages + Helper.SEP_NEWLINE);
                return true;
            }
        }
        return false;
    }
    //
    // private static boolean hasTranslations(Map<String, String> languages, String lng) {
    // if (languages.isEmpty()) {
    // return false;
    // }
    // if (lng.equals(Language.ZH.key)) {
    // return true;
    // }
    // Set<String> lngs = languages.keySet();
    // lngs.remove(lng);
    // for (String l : lngs) {
    // if (Arrays.binarySearch(LanguageConstants.KEYS_WIKI, l) >= 0) {
    // return true;
    // }
    // }
    // System.err.println("Unknown language: "+lngs);
    // return false;
    // }
}
