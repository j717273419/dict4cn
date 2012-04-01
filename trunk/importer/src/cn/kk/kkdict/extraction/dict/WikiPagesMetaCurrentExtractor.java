package cn.kk.kkdict.extraction.dict;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

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

    private static ByteBuffer tmpBB = ByteBuffer.allocate(Helper.MAX_LINE_BYTES);
    
    private static ByteBuffer tmpBBWriter = ByteBuffer.allocate(Helper.MAX_LINE_BYTES);

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
        byte[] LNG_BYTES = LNG.getBytes(Helper.CHARSET_UTF8);
        TranslationSource translationSource = TranslationSource.valueOf(("wiki_" + LNG).toUpperCase());
        final boolean isChinese = Language.ZH.key.equalsIgnoreCase(LNG);
        BufferedInputStream in;
        if (file.getAbsolutePath().endsWith(".bz2")) {
            in = new BufferedInputStream(new BZip2CompressorInputStream((new BufferedInputStream(new FileInputStream(
                    file), Helper.BUFFER_SIZE))), Helper.BUFFER_SIZE);
        } else {
            in = new BufferedInputStream(new FileInputStream(file), Helper.BUFFER_SIZE);
        }
        String dictFile = OUT_DIR + File.separator + "output-dict.wiki_" + LNG;
        System.out.println("分析并写出：" + dictFile + " 。。。");
        BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(dictFile), Helper.BUFFER_SIZE);

        int len;
        byte[] name = null;
        Set<byte[]> globalCategories = new HashSet<byte[]>();
        Set<byte[]> categories = null;
        Map<byte[], byte[]> languages = null;

        int statSkipped = 0;
        int statOk = 0;
        long lineCount = 0;
        Set<byte[]> irrelevantPrefixes = new HashSet<byte[]>();
        byte[][] irrelevantPrefixesBytes = null;
        boolean irrelevantPrefixesNeeded = true;
        byte[] categoryKeyBytes = PREFIX_CATEGORY_KEY_EN.getBytes(Helper.CHARSET_UTF8);

        final byte[][] keysZHBytes = new byte[LanguageConstants.KEYS_ZH.length][];
        int i = 0;
        for (String key : LanguageConstants.KEYS_ZH) {
            keysZHBytes[i] = key.getBytes(Helper.CHARSET_UTF8);
            i++;
        }

        while (-1 != (len = Helper.readLine(in, bb))) {
            if (DEBUG) {
                String str = Helper.toString(bb);
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
            if (irrelevantPrefixesNeeded) {
                if (Helper.contains(bb.array(), 0, len, SUFFIX_NAMESPACES_BYTES)) {
                    // finish prefixes
                    irrelevantPrefixesNeeded = false;
                    irrelevantPrefixesBytes = new byte[irrelevantPrefixes.size()][];
                    i = 0;
                    for (byte[] prefix : irrelevantPrefixes) {
                        irrelevantPrefixesBytes[i] = prefix;
                        i++;
                    }
                    if (DEBUG) {
                        System.out.println("所有过滤前缀：");
                        for (byte[] prefix : irrelevantPrefixesBytes) {
                            System.out.println("- " + Helper.toString(prefix));
                        }
                    }
                } else if (Helper.substringBetweenLast(bb.array(), 0, len, SUFFIX_XML_TAG_BYTES,
                        SUFFIX_NAMESPACE_BYTES, tmpBB) > 0) {
                    // add prefix
                    int limit = tmpBB.limit();
                    tmpBB.limit(limit + 1);
                    tmpBB.put(limit, (byte) ':');
                    irrelevantPrefixes.add(Helper.toBytes(tmpBB));
                    if (DEBUG) {
                        System.out.println("找到域码：" + Helper.toString(tmpBB));
                    }
                    if (Helper.contains(bb.array(), 0, len, ATTR_CATEGORY_KEY_BYTES)) {
                        categoryKeyBytes = new byte[tmpBB.limit() + 2];
                        categoryKeyBytes[0] = (byte) '[';
                        categoryKeyBytes[1] = (byte) '[';
                        System.arraycopy(tmpBB.array(), 0, categoryKeyBytes, 2, tmpBB.limit());
                        if (DEBUG) {
                            System.out.println("找到category代码：" + Helper.toString(tmpBB));
                        }
                    }
                }
            } else {
                if (Helper.substringBetween(bb.array(), 0, len, PREFIX_TITLE_BYTES, SUFFIX_TITLE_BYTES, tmpBB) > 0) {
                    // name found
                    if (write(writer, LNG_BYTES, translationSource, name, categories, languages)) {
                        statOk++;
                    } else {
                        statSkipped++;
                    }
                    boolean relevant = true;
                    for (byte[] prefix : irrelevantPrefixesBytes) {
                        if (Helper.startsWith(tmpBB.array(), tmpBB.limit(), prefix, prefix.length)) {
                            relevant = false;
                            break;
                        }
                    }
                    if (relevant) {
                        if (isChinese) {
                            len = ChineseHelper.toSimplifiedChinese(tmpBB);
                        }
                        if (DEBUG) {
                            System.out.println("新词：" + Helper.toString(tmpBB));
                        }
                        name = Helper.toBytes(tmpBB);
                        categories = new HashSet<byte[]>();
                        languages = new HashMap<byte[], byte[]>();
                    } else {
                        name = null;
                        statSkipped++;
                    }
                } else if (name != null) {
                    if (Helper.substringBetween(bb.array(), 0, len, categoryKeyBytes, SUFFIX_WIKI_TAG_BYTES, tmpBB) > 0) {
                        // add category
                        int wildcardIdx = Helper.indexOf(tmpBB, (byte) '|');
                        if (wildcardIdx != -1) {
                            tmpBB.limit(wildcardIdx);
                        }
                        if (isChinese) {
                            len = ChineseHelper.toSimplifiedChinese(tmpBB);
                        }
                        byte[] category = Helper.toBytes(tmpBB);
                        categories.add(category);
                        globalCategories.add(category);
                    } else if (Helper.substringBetween(bb.array(), 0, len, PREFIX_WIKI_TAG_BYTES,
                            SUFFIX_WIKI_TAG_BYTES, tmpBB) > 0) {
                        // found wiki tag
                        int idx = Helper.indexOf(tmpBB, (byte) ':');
                        if (idx > 0 && idx < 9) {
                            if (isChinese) {
                                len = ChineseHelper.toSimplifiedChinese(tmpBB);
                            }
                            byte[] lngBytes = Helper.toBytes(tmpBB, idx);
                            i = 0;
                            for (byte[] lng : DISPLAYABLE_LNGS) {
                                if (Arrays.equals(lng, lngBytes)) {
                                    if (Helper.substring(tmpBB, idx + 1) > 0) {
                                        if (Arrays.equals(KEY_ZH_BYTES, lng)) {
                                            ChineseHelper.toSimplifiedChinese(tmpBB);
                                        }
                                        languages.put(keysZHBytes[i], Helper.toBytes(tmpBB));
                                    }
                                    break;
                                }
                                i++;
                            }
                        }
                    }
                }
            }
            lineCount++;
        }
        if (write(writer, LNG_BYTES, translationSource, name, categories, languages)) {
            statOk++;
        } else {
            statSkipped++;
        }
        in.close();
        writer.close();

        String categoriesFile = OUT_DIR + File.separator + "output-categories." + translationSource.key;
        System.out.println("\n写出类别文件：" + categoriesFile + "。。。");
        BufferedOutputStream categoriesWriter = new BufferedOutputStream(new FileOutputStream(categoriesFile),
                Helper.BUFFER_SIZE);
        for (byte[] c : globalCategories) {
            categoriesWriter.write(c);
            categoriesWriter.write('\n');
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

    private static boolean write(BufferedOutputStream writer, final byte[] lngBytes,
            TranslationSource translationSource, byte[] name, Set<byte[]> categories, Map<byte[], byte[]> languages)
            throws IOException {
        if (name != null) {
            if (!languages.isEmpty()) {
                languages.put(lngBytes, name);
                Set<byte[]> lngs = languages.keySet();
                byte[] sourceStringBytes = (Helper.SEP_ATTRIBUTE + TranslationSource.TYPE_ID + translationSource.key)
                        .getBytes(Helper.CHARSET_UTF8);
                for (byte[] lng : lngs) {
                    tmpBBWriter.rewind().limit(tmpBBWriter.capacity());
                    tmpBBWriter.put(languages.get(lng)).put(sourceStringBytes);
                    // TODO
                    if (!categories.isEmpty()) {
                        // trans += Helper.SEP_ATTRIBUTE + Category.TYPE_ID;
                    }
                    tmpBBWriter.limit(tmpBBWriter.position());
                    languages.put(lng, Helper.toBytes(tmpBBWriter));
                }

                Iterator<Entry<byte[], byte[]>> i = languages.entrySet().iterator();
                for (;;) {
                    Entry<byte[], byte[]> e = i.next();
                    byte[] key = e.getKey();
                    byte[] value = e.getValue();
                    writer.write(key);
                    writer.write(Helper.SEP_DEFINITION_BYTES);
                    writer.write(value);
                    if (!i.hasNext()) {
                        break;
                    }
                    writer.write(Helper.SEP_LIST_BYTES);
                }
                writer.write('\n');
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
