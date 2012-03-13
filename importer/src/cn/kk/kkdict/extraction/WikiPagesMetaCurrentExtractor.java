package cn.kk.kkdict.extraction;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

import cn.kk.kkdict.beans.FormattedTreeMap;
import cn.kk.kkdict.beans.FormattedTreeSet;
import cn.kk.kkdict.types.Language;
import cn.kk.kkdict.types.TranslationSource;
import cn.kk.kkdict.utils.ChineseHelper;
import cn.kk.kkdict.utils.Helper;

public class WikiPagesMetaCurrentExtractor {
    private static final int OK_NOTICE = 100000;

    public static final String WIKI_PAGES_META_CURRENT_XML_DIR = "X:\\kkdict\\dicts\\wiki";

    public static final String OUT_DIR = "O:\\wiki\\extracted";

    public static void main(String args[]) throws IOException {

        File directory = new File(WIKI_PAGES_META_CURRENT_XML_DIR);
        if (directory.isDirectory()) {
            File[] files = directory.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return (name.endsWith("-pages-meta-current.xml") || name.endsWith("-pages-meta-current.xml.bz2"))
                            && name.contains("wiki-");
                }
            });

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
        System.out.println("\n读取wiki文件'" + file.getAbsolutePath() + "'。。。");
        long timeStarted = System.currentTimeMillis();
        Helper.precheck(file.getAbsolutePath(), OUT_DIR);
        String LNG = file.getName().substring(0, file.getName().indexOf("wiki-"));
        TranslationSource translationSource = TranslationSource.valueOf(("wiki_" + LNG).toUpperCase());
        boolean isChinese = Language.ZH.key.equalsIgnoreCase(LNG);
        BufferedReader reader;
        if (file.getAbsolutePath().endsWith(".bz2")) {
            reader = new BufferedReader(new InputStreamReader(new BZip2CompressorInputStream((new BufferedInputStream(
                    new FileInputStream(file), Helper.BUFFER_SIZE)))), Helper.BUFFER_SIZE);
        } else {
            reader = new BufferedReader(new FileReader(file), Helper.BUFFER_SIZE);
        }
        BufferedWriter writer = new BufferedWriter(new FileWriter(OUT_DIR + File.separator + "output-dict_" + LNG
                + ".wiki_" + LNG), Helper.BUFFER_SIZE);

        String line;
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
        String categoryKey = "[[Category:";
        while ((line = reader.readLine()) != null) {
            if (lineCount % OK_NOTICE == 0) {
                if (lineCount % (OK_NOTICE * 100) == 0 && lineCount != 0) {
                    System.out.println(".");
                } else {
                    System.out.print(".");
                }
            }
            if (isChinese) {
                line = ChineseHelper.toSimplifiedChinese(line);
            }
            if (irrelevantPrefixesNeeded && line.contains("</namespaces>")) {

                irrelevantPrefixesNeeded = false;
            } else if (irrelevantPrefixesNeeded
                    && Helper.isNotEmptyOrNull(tmp = Helper.substringBetweenLast(line, ">", "</namespace>"))) {
                irrelevantPrefixes.add(tmp + ":");
                if (line.contains("key=\"14\"")) {
                    categoryKey = "[[" + tmp + ":";
                }
            } else if (Helper.isNotEmptyOrNull(tmp = Helper.substringBetween(line, "<title>", "</title>"))) {
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
                if (Helper.isNotEmptyOrNull(tmp = Helper.substringBetween(line, categoryKey, "]]"))) {
                    int wildcardIdx = tmp.indexOf('|');
                    if (wildcardIdx != -1) {
                        tmp = tmp.substring(0, wildcardIdx);
                    }
                    categories.add(tmp);
                    globalCategories.add(tmp);
                } else if (Helper.isNotEmptyOrNull(tmp = Helper.substringBetween(line, "[[", "]]"))) {
                    for (String lng : Language.LANGUAGES_ZH_ISO) {
                        if ((tmp = Helper.substringBetween(line, "[[" + lng + ":", "]]")) != null) {
                            if (Language.ZH.key.equalsIgnoreCase(lng)) {
                                tmp = ChineseHelper.toSimplifiedChinese(tmp);
                            }
                            languages.put(lng, tmp);
                            break;
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
        reader.close();
        writer.close();

        BufferedWriter categoriesWriter = new BufferedWriter(new FileWriter(OUT_DIR + File.separator
                + "output-categories." + translationSource.key), Helper.BUFFER_SIZE);
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
            if (hasTranslations(languages, LNG)) {
                languages.put(LNG, name);
                Set<String> lngs = languages.keySet();
                for (String lng : lngs) {
                    String trans = languages.get(lng) + Helper.SEP_ATTRIBUTE + TranslationSource.TYPE_ID
                            + translationSource.key;
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

    private static boolean hasTranslations(Map<String, String> languages, String lng) {
        if (languages.isEmpty()) {
            return false;
        }
        if (lng.equals(Language.ZH.key)) {
            return true;
        }
        if (languages.containsKey(Language.ZH.key)) {
            return true;
        }
        if (languages.containsKey(Language.EN.key) || languages.containsKey(Language.DE.key)
                || languages.containsKey(Language.JA.key) || languages.containsKey(Language.RU.key)
                || languages.containsKey(Language.ES.key) || languages.containsKey(Language.FR.key)
                || languages.containsKey(Language.IT.key) || languages.containsKey(Language.PT.key)
                || languages.containsKey(Language.PL.key) || languages.containsKey(Language.KO.key)) {
            return true;
        }
        return false;
    }
}
