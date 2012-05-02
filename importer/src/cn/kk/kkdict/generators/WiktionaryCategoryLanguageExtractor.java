/*  Copyright (c) 2010 Xiaoyun Zhu
 * 
 *  Permission is hereby granted, free of charge, to any person obtaining a copy  
 *  of this software and associated documentation files (the "Software"), to deal  
 *  in the Software without restriction, including without limitation the rights  
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell  
 *  copies of the Software, and to permit persons to whom the Software is  
 *  furnished to do so, subject to the following conditions:
 *  
 *  The above copyright notice and this permission notice shall be included in  
 *  all copies or substantial portions of the Software.
 *  
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR  
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,  
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE  
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER  
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,  
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN  
 *  THE SOFTWARE.  
 */
package cn.kk.kkdict.generators;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import cn.kk.kkdict.Configuration;
import cn.kk.kkdict.Configuration.Source;
import cn.kk.kkdict.beans.TranslationInfo;
import cn.kk.kkdict.extraction.dict.WiktionaryPagesMetaCurrentChineseExtractor;
import cn.kk.kkdict.types.Language;
import cn.kk.kkdict.types.LanguageConstants;
import cn.kk.kkdict.utils.ChineseHelper;
import cn.kk.kkdict.utils.Helper;

/**
 * <pre>
 * 1。开始：https://en.wiktionary.org/wiki/Wiktionary:List_of_languages
 * 2。摘取所有类别链接：
 * </pre>
 * 
 */
public class WiktionaryCategoryLanguageExtractor {
    public static final String GENERATED_DIR = Configuration.IMPORTER_FOLDER_GENERATED.getPath(Source.NULL);
    public static final Language[] RELEVANT_LANGUAGES = WiktionaryPagesMetaCurrentChineseExtractor.RELEVANT_LANGUAGES;
    private static final Properties LNG2NAME_EN = new Properties();
    private static final String URL_CATEGORY_PREFIX_EN = "https://en.wiktionary.org/wiki/Category:";
    private static final boolean DEBUG = true;
    static {
        try {
            LNG2NAME_EN.load(new InputStreamReader(LanguageConstants.class.getResourceAsStream("/lng2name_EN.txt"),
                    Helper.CHARSET_UTF8));
        } catch (IOException e) {
            System.err.println("导入英文语言数据失败：" + e.toString());
        }
    }

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        long start = System.currentTimeMillis();
        new File(GENERATED_DIR).mkdirs();
        String urlAllLanguages = "https://en.wiktionary.org/wiki/Wiktionary:List_of_languages";
        String allCatFile = Helper.download(urlAllLanguages);
        Set<String> lngCategories = parseCategories(allCatFile, "<h2><span class=\"editsection\">",
                "<h2><span class=\"editsection\">", "wiki/Category:");
        // Set<String> allCategories =
        // parseAllCategories("C:\\Users\\x_kez\\AppData\\Local\\Temp\\kkdl5139596254287798107.tmp");
        System.out.println("共找到" + lngCategories.size() + "语言类。");
        Map<Language, BufferedWriter> catWriters = new HashMap<Language, BufferedWriter>();
        Map<Language, BufferedWriter> lngWriters = new HashMap<Language, BufferedWriter>();
        Helper.DEBUG = false;
        for (Language l : RELEVANT_LANGUAGES) {
            catWriters.put(l, new BufferedWriter(new FileWriter(GENERATED_DIR + File.separator + "cat2lng_" + l.name()
                    + ".txt")));
            lngWriters.put(l,
                    new BufferedWriter(new FileWriter(GENERATED_DIR + File.separator + "lng2name_cat_" + l.name()
                            + ".txt")));
        }
        BufferedWriter transWriter = new BufferedWriter(new FileWriter(GENERATED_DIR + File.separator
                + "output-dict.wiki_catlng"));
        TranslationInfo lngTrans;
        TranslationInfo catTrans = new TranslationInfo();
        catTrans.key = "Category";
        catTrans.put(Language.EN, "Category");
        for (String c : lngCategories) {
            lngTrans = new TranslationInfo();
            lngTrans.key = c;
            lngTrans.put(Language.EN, c.replace("_language", "").replace('_', ' '));
            Language catLng = getLanguageFromCategory(c);
            String lngCatFile = Helper.download(URL_CATEGORY_PREFIX_EN + c);
            writeCategoryLanguages(lngCatFile, Language.EN, getWriter(catWriters, Language.EN, "cat2lng_"), catLng,
                    "wiki/Category:");
            writeOtherLanguages(lngCatFile, catLng, catWriters, lngWriters, lngTrans, catTrans);
            transWriter.write(lngTrans.lngMap.toString());
            transWriter.write(Helper.SEP_NEWLINE_CHAR);
        }
        transWriter.write(catTrans.lngMap.toString());
        transWriter.write(Helper.SEP_NEWLINE_CHAR);
        transWriter.close();
        Set<Language> keys = catWriters.keySet();
        for (Language l : keys) {
            // if (DEBUG) {
            // System.out.println("写出：Language." + l.name());
            // }
            catWriters.get(l).close();
        }
        keys = lngWriters.keySet();
        for (Language l : keys) {
            lngWriters.get(l).close();
        }
        System.out.println("共用时：" + Helper.formatDuration(System.currentTimeMillis() - start));
    }

    private static BufferedWriter getWriter(Map<Language, BufferedWriter> writers, Language lng, String prefix)
            throws IOException {
        BufferedWriter writer = writers.get(lng);
        if (writer == null) {
            System.err.println("没有找到写出流：" + lng.key);
            writer = new BufferedWriter(new FileWriter(GENERATED_DIR + File.separator + "cat2lng_" + lng.name()
                    + ".txt"));
            writers.put(lng, writer);
        }
        return writer;
    }

    private static void writeOtherLanguages(String file, Language catLng, Map<Language, BufferedWriter> catWriters,
            Map<Language, BufferedWriter> lngWriters, TranslationInfo lngTrans, TranslationInfo catTrans)
            throws IOException {
        Set<String> urlOtherLanguages = new HashSet<String>();
        String startTag = "p-lang";
        String stopTag = "footer";
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        String tmp;
        boolean started = false;
        String listKey = "<li class=\"interwiki";
        while ((line = reader.readLine()) != null) {
            if (!started) {
                if (line.contains(startTag)) {
                    started = true;
                }
            } else {
                if (line.contains(stopTag)) {
                    started = false;
                }
                if (line.contains(listKey)) {
                    tmp = URLDecoder.decode(Helper.substringBetween(line, "<a href=\"", "\""),
                            Helper.CHARSET_UTF8.name());
                    urlOtherLanguages.add(tmp);
                    // System.out.println(tmp);
                }
            }
        }
        reader.close();
        for (String url : urlOtherLanguages) {
            if (url.startsWith("//")) {
                url = "https:" + url;
            }
            String f = Helper.download(url);
            String categoryPrefix = getCategoryKeyFromUrl(url);
            String categoryName = getCategoryNameFromUrl(url);
            Language fileLng = getWiktionaryUrlLanguage(url);
            if (fileLng == Language.ZH) {
                categoryName = ChineseHelper.toSimplifiedChinese(categoryName);
            }
            lngTrans.put(fileLng, categoryName);
            catTrans.put(fileLng,
                    categoryPrefix.substring(categoryPrefix.lastIndexOf('/') + 1, categoryPrefix.length() - 1));
            writeCategoryLanguages(f, fileLng, getWriter(catWriters, fileLng, "cat2lng_"), catLng, categoryPrefix);
            writeCategoryName(getWriter(lngWriters, fileLng, "lng2name_cat_"), categoryName, catLng);
        }
    }

    private static void writeCategoryName(BufferedWriter lngWriter, String categoryName, Language catLng)
            throws IOException {
        lngWriter.write(categoryName.replace(" ", "\\ "));
        lngWriter.write('=');
        lngWriter.write(catLng.key);
        lngWriter.write(Helper.SEP_NEWLINE_CHAR);
    }

    private static String getCategoryNameFromUrl(String url) {
        // https://es.wiktionary.org/wiki/Categoría:Alto_sórabo
        return Helper.substringAfterLast(url, ":").replace('_', ' ');
    }

    private static String getCategoryKeyFromUrl(String url) {
        // https://es.wiktionary.org/wiki/Categoría:Alto_sórabo
        return Helper.substringBetween(url, "wiktionary.org/", ":") + ":";
    }

    private static Language getWiktionaryUrlLanguage(String file) {
        // //en.
        return Language.valueOf(Helper.toConstantName(Helper.substringBetween(file, "//", ".")));
    }

    private static void writeCategoryLanguages(String file, Language fileLng, BufferedWriter writer, Language catLng,
            final String categoryKey) throws IOException {
        Set<String> categories = parseCategories(file, "mw-content-text", "printfooter", categoryKey);
        for (String c : categories) {
            if (fileLng == Language.ZH) {
                c = ChineseHelper.toSimplifiedChinese(c);
            }
            writeCategoryName(writer, c, catLng);
        }
    }

    private static Language getLanguageFromCategory(final String c) {
        int idx;
        String categoryLanguageSuffix = "_language";
        if (-1 == (idx = c.lastIndexOf(categoryLanguageSuffix))) {
            idx = c.lastIndexOf("_Language");
        }
        if (-1 != idx) {
            String lng = c.substring(0, idx).replace('_', ' ');
            String lngKey = LNG2NAME_EN.getProperty(lng);
            if (lngKey == null) {
                System.err.println("没找到类别语言：" + lng);
            } else {
                try {
                    return Language.valueOf(Helper.toConstantName(lngKey));
                } catch (IllegalArgumentException e) {
                    String l = Helper.toConstantName(lngKey);
                    System.err
                            .println("新语言：" + l.toUpperCase() + "(\"" + l.toLowerCase() + "\", LanguageFamily.NONE),");
                }
            }
        } else {
            System.err.println("类别链接不符合规格：" + c);
        }
        return null;
    }

    private static Set<String> parseCategories(final String file, final String startTag, final String stopTag,
            final String categoryKey) throws IOException {
        Set<String> result = new HashSet<String>();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        String tmp;
        boolean started = false;
        while ((line = reader.readLine()) != null) {
            if (!started) {
                if (line.contains(startTag)) {
                    started = true;
                }
            } else {
                if (line.contains(stopTag)) {
                    started = false;
                }
                try {
                    line = URLDecoder.decode(
                            line.replace("%0\"", "0\"").replace("%<", "<").replace("%.", ".").replace("%\"", "\"")
                                    .replace("%'", "'").replace("% ", " ").replace("%;", ";"),
                            Helper.CHARSET_UTF8.name());
                } catch (IllegalArgumentException e) {
                    System.err.println("错误：" + e.toString());
                }
                if (null != (tmp = Helper.substringBetween(line, categoryKey, "\""))) {
                    result.add(tmp);
                    // System.out.println(tmp);
                }
            }
        }
        reader.close();
        return result;
    }

}
