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
import java.util.EnumMap;
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
  public static final String      GENERATED_DIR          = Configuration.IMPORTER_FOLDER_GENERATED.getPath(Source.NULL);
  public static final Language[]  RELEVANT_LANGUAGES     = WiktionaryPagesMetaCurrentChineseExtractor.RELEVANT_LANGUAGES;
  private static final Properties LNG2NAME_EN            = new Properties();
  private static final String     URL_CATEGORY_PREFIX_EN = "https://en.wiktionary.org/wiki/Category:";
  @SuppressWarnings("unused")
  private static final boolean    DEBUG                  = true;

  static {
    try {
      WiktionaryCategoryLanguageExtractor.LNG2NAME_EN.load(new InputStreamReader(LanguageConstants.class.getResourceAsStream("/lng2name_EN.txt"),
          Helper.CHARSET_UTF8));
    } catch (final IOException e) {
      System.err.println("导入英文语言数据失败：" + e.toString());
    }
  }

  /**
   * @param args
   * @throws IOException
   */
  public static void main(final String[] args) throws IOException {
    final long start = System.currentTimeMillis();
    new File(WiktionaryCategoryLanguageExtractor.GENERATED_DIR).mkdirs();
    final String urlAllLanguages = "https://en.wiktionary.org/wiki/Wiktionary:List_of_languages";
    final String allCatFile = Helper.download(urlAllLanguages);
    final Set<String> lngCategories = WiktionaryCategoryLanguageExtractor.parseCategories(allCatFile, "<h2><span class=\"editsection\">",
        "<h2><span class=\"editsection\">", "wiki/Category:");
    // Set<String> allCategories =
    // parseAllCategories("C:\\Users\\x_kez\\AppData\\Local\\Temp\\kkdl5139596254287798107.tmp");
    System.out.println("共找到" + lngCategories.size() + "语言类。");
    final Map<Language, BufferedWriter> catWriters = new EnumMap<>(Language.class);
    final Map<Language, BufferedWriter> lngWriters = new EnumMap<>(Language.class);
    Helper.DEBUG = false;
    for (final Language l : WiktionaryCategoryLanguageExtractor.RELEVANT_LANGUAGES) {
      catWriters
          .put(l, new BufferedWriter(new FileWriter(WiktionaryCategoryLanguageExtractor.GENERATED_DIR + File.separator + "cat2lng_" + l.name() + ".txt")));
      lngWriters.put(l, new BufferedWriter(new FileWriter(WiktionaryCategoryLanguageExtractor.GENERATED_DIR + File.separator + "lng2name_cat_" + l.name()
          + ".txt")));
    }
    try (final BufferedWriter transWriter = new BufferedWriter(new FileWriter(WiktionaryCategoryLanguageExtractor.GENERATED_DIR + File.separator
        + "output-dict.wiki_catlng"));) {
      TranslationInfo lngTrans;
      final TranslationInfo catTrans = new TranslationInfo();
      catTrans.key = "Category";
      catTrans.put(Language.EN, "Category");
      for (final String c : lngCategories) {
        lngTrans = new TranslationInfo();
        lngTrans.key = c;
        lngTrans.put(Language.EN, c.replace("_language", "").replace('_', ' '));
        final Language catLng = WiktionaryCategoryLanguageExtractor.getLanguageFromCategory(c);
        final String lngCatFile = Helper.download(WiktionaryCategoryLanguageExtractor.URL_CATEGORY_PREFIX_EN + c);
        WiktionaryCategoryLanguageExtractor.writeCategoryLanguages(lngCatFile, Language.EN,
            WiktionaryCategoryLanguageExtractor.getWriter(catWriters, Language.EN, "cat2lng_"), catLng, "wiki/Category:");
        WiktionaryCategoryLanguageExtractor.writeOtherLanguages(lngCatFile, catLng, catWriters, lngWriters, lngTrans, catTrans);
        transWriter.write(lngTrans.lngMap.toString());
        transWriter.write(Helper.SEP_NEWLINE_CHAR);
      }
      transWriter.write(catTrans.lngMap.toString());
      transWriter.write(Helper.SEP_NEWLINE_CHAR);
    }
    Set<Language> keys = catWriters.keySet();
    for (final Language l : keys) {
      // if (DEBUG) {
      // System.out.println("写出：Language." + l.name());
      // }
      catWriters.get(l).close();
    }
    keys = lngWriters.keySet();
    for (final Language l : keys) {
      lngWriters.get(l).close();
    }
    System.out.println("共用时：" + Helper.formatDuration(System.currentTimeMillis() - start));
  }

  private static BufferedWriter getWriter(final Map<Language, BufferedWriter> writers, final Language lng, final String prefix) throws IOException {
    BufferedWriter writer = writers.get(lng);
    if (writer == null) {
      System.err.println("没有找到写出流：" + lng.getKey());
      writer = new BufferedWriter(new FileWriter(WiktionaryCategoryLanguageExtractor.GENERATED_DIR + File.separator + prefix + lng.name() + ".txt"));
      writers.put(lng, writer);
    }
    return writer;
  }

  private static void writeOtherLanguages(final String file, final Language catLng, final Map<Language, BufferedWriter> catWriters,
      final Map<Language, BufferedWriter> lngWriters, final TranslationInfo lngTrans, final TranslationInfo catTrans) throws IOException {
    final Set<String> urlOtherLanguages = new HashSet<>();
    final String startTag = "p-lang";
    final String stopTag = "footer";
    try (final BufferedReader reader = new BufferedReader(new FileReader(file));) {
      String line;
      String tmp;
      boolean started = false;
      final String listKey = "<li class=\"interwiki";
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
            tmp = URLDecoder.decode(Helper.substringBetween(line, "<a href=\"", "\""), Helper.CHARSET_UTF8.name());
            urlOtherLanguages.add(tmp);
            // System.out.println(tmp);
          }
        }
      }
    }
    for (String url : urlOtherLanguages) {
      if (url.startsWith("//")) {
        url = "https:" + url;
      }
      final String f = Helper.download(url);
      final String categoryPrefix = WiktionaryCategoryLanguageExtractor.getCategoryKeyFromUrl(url);
      String categoryName = WiktionaryCategoryLanguageExtractor.getCategoryNameFromUrl(url);
      final Language fileLng = WiktionaryCategoryLanguageExtractor.getWiktionaryUrlLanguage(url);
      if (fileLng == Language.ZH) {
        categoryName = ChineseHelper.toSimplifiedChinese(categoryName);
      }
      lngTrans.put(fileLng, categoryName);
      catTrans.put(fileLng, categoryPrefix.substring(categoryPrefix.lastIndexOf('/') + 1, categoryPrefix.length() - 1));
      WiktionaryCategoryLanguageExtractor.writeCategoryLanguages(f, fileLng, WiktionaryCategoryLanguageExtractor.getWriter(catWriters, fileLng, "cat2lng_"),
          catLng, categoryPrefix);
      WiktionaryCategoryLanguageExtractor.writeCategoryName(WiktionaryCategoryLanguageExtractor.getWriter(lngWriters, fileLng, "lng2name_cat_"), categoryName,
          catLng);
    }
  }

  private static void writeCategoryName(final BufferedWriter lngWriter, final String categoryName, final Language catLng) throws IOException {
    lngWriter.write(categoryName.replace(" ", "\\ "));
    lngWriter.write('=');
    lngWriter.write(catLng.getKey());
    lngWriter.write(Helper.SEP_NEWLINE_CHAR);
  }

  private static String getCategoryNameFromUrl(final String url) {
    // https://es.wiktionary.org/wiki/Categoría:Alto_sórabo
    return Helper.substringAfterLast(url, ":").replace('_', ' ');
  }

  private static String getCategoryKeyFromUrl(final String url) {
    // https://es.wiktionary.org/wiki/Categoría:Alto_sórabo
    return Helper.substringBetween(url, "wiktionary.org/", ":") + ":";
  }

  private static Language getWiktionaryUrlLanguage(final String file) {
    // //en.
    return Language.fromKey(Helper.substringBetween(file, "//", "."));
  }

  private static void writeCategoryLanguages(final String file, final Language fileLng, final BufferedWriter writer, final Language catLng,
      final String categoryKey) throws IOException {
    final Set<String> categories = WiktionaryCategoryLanguageExtractor.parseCategories(file, "mw-content-text", "printfooter", categoryKey);
    for (String c : categories) {
      if (fileLng == Language.ZH) {
        c = ChineseHelper.toSimplifiedChinese(c);
      }
      WiktionaryCategoryLanguageExtractor.writeCategoryName(writer, c, catLng);
    }
  }

  private static Language getLanguageFromCategory(final String c) {
    int idx;
    final String categoryLanguageSuffix = "_language";
    if (-1 == (idx = c.lastIndexOf(categoryLanguageSuffix))) {
      idx = c.lastIndexOf("_Language");
    }
    if (-1 != idx) {
      final String lng = c.substring(0, idx).replace('_', ' ');
      final String lngKey = WiktionaryCategoryLanguageExtractor.LNG2NAME_EN.getProperty(lng);
      if (lngKey == null) {
        System.err.println("没找到类别语言：" + lng);
      } else {
        try {
          return Language.fromKey(lngKey);
        } catch (final IllegalArgumentException e) {
          final String l = Helper.toConstantName(lngKey);
          System.err.println("新语言：" + l.toUpperCase() + "(\"" + l.toLowerCase() + "\", LanguageFamily.NONE),");
        }
      }
    } else {
      System.err.println("类别链接不符合规格：" + c);
    }
    return null;
  }

  private static Set<String> parseCategories(final String file, final String startTag, final String stopTag, final String categoryKey) throws IOException {
    final Set<String> result = new HashSet<>();
    try (final BufferedReader reader = new BufferedReader(new FileReader(file));) {
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
            line = URLDecoder
                .decode(
                    line.replace("%0\"", "0\"").replace("%<", "<").replace("%.", ".").replace("%\"", "\"").replace("%'", "'").replace("% ", " ")
                        .replace("%;", ";"), Helper.CHARSET_UTF8.name());
          } catch (final IllegalArgumentException e) {
            System.err.println("错误：" + e.toString());
          }
          if (null != (tmp = Helper.substringBetween(line, categoryKey, "\""))) {
            result.add(tmp);
            // System.out.println(tmp);
          }
        }
      }
    }
    return result;
  }

}
