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
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

import cn.kk.kkdict.Configuration;
import cn.kk.kkdict.Configuration.Source;
import cn.kk.kkdict.beans.FormattedArrayList;
import cn.kk.kkdict.beans.FormattedTreeSet;
import cn.kk.kkdict.beans.TranslationInfo;
import cn.kk.kkdict.extraction.dict.WiktionaryPagesMetaCurrentChineseExtractor;
import cn.kk.kkdict.types.Language;
import cn.kk.kkdict.utils.Helper;

/**
 * <pre>
 * List of wikipedia languages: http://zh.wikipedia.org/zh-cn/Wikipedia:%E7%BB%B4%E5%9F%BA%E7%99%BE%E7%A7%91%E8%AF%AD%E8%A8%80%E5%88%97%E8%A1%A8 (全部语言版本)
 * List of wiktionary languages: http://meta.wikimedia.org/wiki/Wiktionary#List_of_Wiktionaries
 * ISO 639 languages: http://en.wiktionary.org/wiki/Transwiki:ISO_639:a
 * </pre>
 * 
 * @author x_kez
 * 
 */
public class LanguageExtractor {
  public static final String     GENERATED_DIR      = Configuration.IMPORTER_FOLDER_GENERATED.getPath(Source.NULL);
  // public static final Language[] RELEVANT_LANGUAGES = { Language.EN, Language.RU, Language.PL, Language.JA,
  // Language.KO, Language.ZH, Language.DE, Language.FR, Language.IT, Language.ES, Language.PT, Language.NL,
  // Language.SV, Language.UK, Language.VI, Language.CA, Language.NO, Language.FI, Language.CS, Language.HU,
  // Language.ID, Language.TR, Language.RO, Language.FA, Language.AR, Language.DA, Language.EO, Language.SR,
  // Language.LT, Language.SK, Language.SL, Language.MS, Language.HE, Language.BG, Language.KK, Language.EU,
  // Language.VO, Language.WAR, Language.HR, Language.HI, Language.LA };

  // public static final Language[] RELEVANT_LANGUAGES = { Language.ZH };

  public static final Language[] RELEVANT_LANGUAGES = WiktionaryPagesMetaCurrentChineseExtractor.RELEVANT_LANGUAGES;

  /**
   * @param args
   * @throws IOException
   */
  public static void main(final String[] args) throws IOException {
    new File(LanguageExtractor.GENERATED_DIR).mkdirs();
    // full english: http://s23.org/wikistats/wikipedias_html.php?sort=prefix_asc
    // actual iso template: http://zh.wikipedia.org/wiki/Template:ISO639
    final String listOfWikipediasUrlZh = "https://zh.wikipedia.org/zh-cn/Wikipedia:%E7%BB%B4%E5%9F%BA%E7%99%BE%E7%A7%91%E8%AF%AD%E8%A8%80%E5%88%97%E8%A1%A8";
    final List<TranslationInfo> wikiInfos = LanguageExtractor.parseListOfWikipediasHtml(Helper.download(listOfWikipediasUrlZh));

    final String listOfWiktionariesUrl = "https://meta.wikimedia.org/wiki/Wiktionary#List_of_Wiktionaries";
    final List<TranslationInfo> wiktInfos = LanguageExtractor.parseListOfWiktionariesHtml(Helper.download(listOfWiktionariesUrl));

    final List<TranslationInfo> allInfos = new FormattedArrayList<>(wikiInfos);

    final String wikiTranslationSource = LanguageExtractor.GENERATED_DIR + "\\TranslationSource.java";
    LanguageExtractor.generateInitialTranslationSourceJava(wikiInfos, wiktInfos, wikiTranslationSource);

    // merges infos into allInfos
    LanguageExtractor.mergeWikiWiktLanguages(allInfos, wiktInfos);
    final List<TranslationInfo> reducedInfos = new FormattedArrayList<>(allInfos);

    final String iso639MainPageUrl = "https://en.wiktionary.org/wiki/Transwiki:ISO_639";
    LanguageExtractor.enrichWithIso639Languages(allInfos, iso639MainPageUrl);

    LanguageExtractor.enrichWithJavaLocales(allInfos);

    LanguageExtractor.generateInitialLanguageFamilyJava(allInfos, LanguageExtractor.GENERATED_DIR + "\\LanguageFamily.java");

    LanguageExtractor.generateInitialLanguageJava(reducedInfos, LanguageExtractor.GENERATED_DIR + "\\Language.java");

    LanguageExtractor.generateInitialLanguageConstantsJava(allInfos, wikiInfos, wiktInfos, LanguageExtractor.GENERATED_DIR + "\\LanguageConstants.java");

  }

  private static void enrichWithJavaLocales(final List<TranslationInfo> wikiInfos) {
    String tmp;
    final Locale[] locales = Locale.getAvailableLocales();
    for (int i = 0; i < locales.length; i++) {
      final Locale locale = locales[i];
      final TranslationInfo rowInfo = new TranslationInfo();
      rowInfo.key = locale.getLanguage();
      rowInfo.original = locale.getDisplayName(locale);
      final String displayNameEn = locale.getDisplayName(Locale.ENGLISH);
      rowInfo.put(Language.EN, displayNameEn);
      for (final Language lng : LanguageExtractor.RELEVANT_LANGUAGES) {
        if (!displayNameEn.equals((tmp = locale.getDisplayName(new Locale(lng.getKey()))))) {
          rowInfo.put(lng, tmp);
        }
      }
      LanguageExtractor.enrichByRowInfo(wikiInfos, rowInfo);
    }
  }

  private static void enrichWithIso639Languages(final List<TranslationInfo> wikiInfos, final String iso639MainPageUrl) throws IOException {
    for (char c = 'a'; c <= 'z'; c++) {
      LanguageExtractor.enrichWithIso639LanguagesPage(wikiInfos, Helper.download(iso639MainPageUrl + ":" + c));
    }
    System.out.println("“Transwiki ISO 639”合并后总语言信息：" + wikiInfos.size());
    Collections.sort(wikiInfos);
  }

  private static void enrichWithIso639LanguagesPage(final List<TranslationInfo> infos, final String file) throws IOException {
    System.out.println("分析“Transwiki ISO 639”HTML文件'" + file + "' 。。。");
    try (final BufferedReader reader = new BufferedReader(new FileReader(file));) {
      String line;
      int row = 0;
      boolean started = false;
      final String startTag = ">deu<";
      final String stopTag = "</table>";
      TranslationInfo rowInfo = new TranslationInfo();
      while ((line = reader.readLine()) != null) {
        line = line.trim();
        if (!started) {
          if (line.contains(startTag)) {
            started = true;
          }
        } else {
          if (line.contains(stopTag)) {
            LanguageExtractor.enrichByRowInfo(infos, rowInfo);
            break;
          } else if (line.contains("</tr>")) {
            LanguageExtractor.enrichByRowInfo(infos, rowInfo);
            rowInfo = new TranslationInfo();
            row = 0;
            rowInfo.invalidate();
          }
          if (line.contains("<td") || line.contains("<th")) {
            final String value = Helper.unescapeHtml(Helper.stripHtmlText(line, false)).trim();
            switch (row) {
              case 0:
                rowInfo.iso3 = value.replace("(", Helper.EMPTY_STRING).replace(")", Helper.EMPTY_STRING);
                break;
              case 1:
                rowInfo.iso1 = value.replace("(", Helper.EMPTY_STRING).replace(")", Helper.EMPTY_STRING);
                break;
              case 2:
                rowInfo.iso2 = value.replace("(", Helper.EMPTY_STRING).replace(")", Helper.EMPTY_STRING);
                break;
              case 3:
                // scope
                break;
              case 4:
                rowInfo.family = value;
                break;
              case 5:
                rowInfo.original = value;
                break;
              case 6:
                rowInfo.put(Language.EN, value);
                break;
              case 7:
                rowInfo.put(Language.FR, value);
                break;
              case 8:
                rowInfo.put(Language.ES, value);
                break;
              case 9:
                rowInfo.put(Language.ZH, value);
                break;
              case 10:
                rowInfo.put(Language.RU, value);
                break;
              case 11:
                rowInfo.put(Language.DE, value);
                break;
              default:
                break;
            }
            row++;
          }
        }
      }
    }
  }

  private static boolean enrichByRowInfo(final List<TranslationInfo> wikiInfos, final TranslationInfo rowInfo) {
    boolean found = false;
    if (rowInfo.isValid()) {
      String tmp;
      for (final TranslationInfo j : wikiInfos) {
        if (j.key.equals(rowInfo.key)) {
          if (null != (tmp = LanguageExtractor.getNewContent(j.original, rowInfo.original))) {
            j.original = tmp;
          }
          for (final Language lng : LanguageExtractor.RELEVANT_LANGUAGES) {
            if (null != (tmp = LanguageExtractor.getNewContent(j.get(lng), rowInfo.get(lng)))) {
              j.put(lng, tmp);
            }
          }
          if (Helper.isNotEmptyOrNull(rowInfo.family)) {
            j.family = rowInfo.family;
          }
          if (Helper.isNotEmptyOrNull(rowInfo.iso1)) {
            j.iso1 = rowInfo.iso1;
          }
          if (Helper.isNotEmptyOrNull(rowInfo.iso2)) {
            j.iso2 = rowInfo.iso2;
          }
          if (Helper.isNotEmptyOrNull(rowInfo.iso3)) {
            j.iso3 = rowInfo.iso3;
          }
          found = true;
          break;
        }
      }
      if (!found) {
        wikiInfos.add(rowInfo);
      }
    }
    return found;
  }

  private static String getNewContent(final String oldInfo, final String newInfo) {
    if (Helper.isNotEmptyOrNull(oldInfo) && Helper.isNotEmptyOrNull(newInfo) && !oldInfo.equals(newInfo) && !oldInfo.contains(newInfo)) {
      return oldInfo + "; " + newInfo;
    } else if (Helper.isEmptyOrNull(oldInfo) && Helper.isNotEmptyOrNull(newInfo)) {
      return newInfo;
    }
    return null;
  }

  private static void mergeWikiWiktLanguages(final List<TranslationInfo> wikiInfos, final List<TranslationInfo> wiktInfos) {
    System.out.println("合并语言信息 。。。");
    for (final TranslationInfo i : wiktInfos) {
      LanguageExtractor.enrichByRowInfo(wikiInfos, i);
    }
    System.out.println("合并后总共语言：" + wikiInfos.size());
  }

  private static void generateInitialTranslationSourceJava(final List<TranslationInfo> wikiInfos, final List<TranslationInfo> wiktInfos, final String file)
      throws IOException {
    System.out.println("创建TranslationSourceWiki.java文件'" + file + "' 。。。");
    final StringBuilder sb = new StringBuilder(1024 * 32);
    sb.append("package cn.kk.kkdict.types;").append(Helper.SEP_NEWLINE);
    sb.append("public enum TranslationSource {").append(Helper.SEP_NEWLINE);
    for (final TranslationInfo i : wikiInfos) {
      sb.append("WIKI_").append(LanguageExtractor.getEnumKey(i)).append("(\"").append("wiki_").append(LanguageExtractor.getEnumKey(i).toLowerCase())
          .append("\"),").append(Helper.SEP_NEWLINE);
    }
    for (final TranslationInfo i : wiktInfos) {
      sb.append("WIKT_").append(LanguageExtractor.getEnumKey(i)).append("(\"").append("wikt_").append(LanguageExtractor.getEnumKey(i).toLowerCase())
          .append("\"),").append(Helper.SEP_NEWLINE);
    }
    sb.append(';').append(Helper.SEP_NEWLINE).append(Helper.SEP_NEWLINE);
    sb.append("public static final String TYPE_ID = \"源\";").append(Helper.SEP_NEWLINE);
    sb.append("public final String key;").append(Helper.SEP_NEWLINE).append(Helper.SEP_NEWLINE);
    sb.append("TranslationSource(final String key) {").append(Helper.SEP_NEWLINE);
    sb.append("this.key = key;").append(Helper.SEP_NEWLINE);
    sb.append("}").append(Helper.SEP_NEWLINE).append(Helper.SEP_NEWLINE);
    sb.append("}").append(Helper.SEP_NEWLINE);
    try (final BufferedWriter writer = new BufferedWriter(new FileWriter(file));) {
      writer.write(sb.toString());
    }
    System.out.println("创建TranslationSourceWiki.java文件成功。");
  }

  private static void generateInitialLanguageFamilyJava(final List<TranslationInfo> rowInfos, final String file) throws IOException {
    final Set<String> families = new FormattedTreeSet<>();
    for (final TranslationInfo i : rowInfos) {
      final String enumName = Helper.toConstantName(i.family);
      if (Helper.isNotEmptyOrNull(enumName)) {
        families.add(enumName);
      }
    }
    Collections.sort(rowInfos);
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(file + "_sorted"));) {
      for (final String f : families) {
        for (final TranslationInfo i : rowInfos) {
          if (f.equals(Helper.toConstantName(i.family))) {
            writer.write(LanguageExtractor.getEnumKey(i) + "(\"" + i.key + "\", LanguageFamily." + LanguageExtractor.getLanguageFamily(i) + "),"
                + Helper.SEP_NEWLINE);
          }
        }
      }
    }
    System.out.println("创建LanguageFamily.java文件'" + file + "'（语系：" + families.size() + "） 。。。");
    final StringBuilder sb = new StringBuilder(1024 * 32);
    sb.append("package cn.kk.kkdict.types;").append(Helper.SEP_NEWLINE);
    sb.append("public enum LanguageFamily {").append(Helper.SEP_NEWLINE);

    sb.append("NONE,").append(Helper.SEP_NEWLINE);
    for (final String f : families) {
      sb.append(f.toUpperCase()).append(",").append(Helper.SEP_NEWLINE);
    }
    sb.append('}').append(Helper.SEP_NEWLINE).append(Helper.SEP_NEWLINE);

    try (BufferedWriter writer = new BufferedWriter(new FileWriter(file));) {
      writer.write(sb.toString());
    }
    System.out.println("创建LanguageFamily.java文件成功。");
  }

  private static void generateInitialLanguageJava(final List<TranslationInfo> rowInfos, final String file) throws IOException {
    final Set<String> families = new FormattedTreeSet<>();
    for (final TranslationInfo i : rowInfos) {
      final String enumName = Helper.toConstantName(i.family);
      if (Helper.isNotEmptyOrNull(enumName)) {
        families.add(enumName);
      }
    }
    Collections.sort(rowInfos);
    System.out.println("创建Language.java文件'" + file + "'（语言：" + rowInfos.size() + "，语系：" + families.size() + "） 。。。");
    final StringBuilder sb = new StringBuilder(1024 * 320);
    sb.append("package cn.kk.kkdict.types;").append(Helper.SEP_NEWLINE);
    sb.append("public enum Language {").append(Helper.SEP_NEWLINE);
    // lng enums
    for (final TranslationInfo i : rowInfos) {
      sb.append(LanguageExtractor.getEnumKey(i)).append("(\"").append(i.key).append("\", LanguageFamily.").append(LanguageExtractor.getLanguageFamily(i))
          .append("),").append(Helper.SEP_NEWLINE);
    }
    sb.append(';').append(Helper.SEP_NEWLINE).append(Helper.SEP_NEWLINE);

    // body
    sb.append("public static final String TYPE_ID = \"语\";").append(Helper.SEP_NEWLINE);
    sb.append("public final String key;").append(Helper.SEP_NEWLINE);
    sb.append("public final LanguageFamily family;").append(Helper.SEP_NEWLINE).append(Helper.SEP_NEWLINE);
    sb.append("Language(final String key, final LanguageFamily family) {").append(Helper.SEP_NEWLINE);
    sb.append("this.key = key;").append(Helper.SEP_NEWLINE);
    sb.append("this.family = family;").append(Helper.SEP_NEWLINE);
    sb.append("}").append(Helper.SEP_NEWLINE).append(Helper.SEP_NEWLINE);
    sb.append("}").append(Helper.SEP_NEWLINE);
    try (final BufferedWriter writer = new BufferedWriter(new FileWriter(file));) {
      writer.write(sb.toString());
    }
    System.out.println("创建Language.java文件成功。");
  }

  private static void generateInitialLanguageConstantsJava(final List<TranslationInfo> rowInfos, final List<TranslationInfo> wikiInfos,
      final List<TranslationInfo> wiktInfos, final String file) throws IOException {
    final Set<String> families = new FormattedTreeSet<>();
    for (final TranslationInfo i : rowInfos) {
      final String enumName = Helper.toConstantName(i.family);
      if (Helper.isNotEmptyOrNull(enumName)) {
        families.add(enumName);
      }
    }
    Collections.sort(rowInfos);
    System.out.println("创建LanguageConstants.java文件'" + file + "'（语言：" + rowInfos.size() + "，语系：" + families.size() + "） 。。。");
    final StringBuilder sb = new StringBuilder(1024 * 320);

    sb.append(
        "package cn.kk.kkdict.types;\n\nimport java.util.*;\nimport java.io.IOException;\nimport cn.kk.kkdict.beans.ByteArrayPairs;\nimport cn.kk.kkdict.utils.Helper;")
        .append(Helper.SEP_NEWLINE);
    sb.append("public final class LanguageConstants {").append(Helper.SEP_NEWLINE);

    sb.append("private static final LanguageConstants INSTANCE = new LanguageConstants();").append(Helper.SEP_NEWLINE);
    sb.append("public static final ByteArrayPairs getLanguageNamesBytes(final Language lng) {\n").append(Helper.SEP_NEWLINE);
    sb.append("if (lng == null) {").append(Helper.SEP_NEWLINE);
    sb.append("return createByteArrayPairs(INSTANCE.getLngProperties(\"lng2name_ORIGINAL.txt\"));").append(Helper.SEP_NEWLINE);
    sb.append("}").append(Helper.SEP_NEWLINE);
    sb.append("switch (lng) {").append(Helper.SEP_NEWLINE);
    for (final Language lng : LanguageExtractor.RELEVANT_LANGUAGES) {
      final String name = lng.name();
      sb.append("case ").append(name).append(":").append(Helper.SEP_NEWLINE);
      sb.append("return createByteArrayPairs(INSTANCE.getLngProperties(\"lng2name_").append(name).append(".txt\"));").append(Helper.SEP_NEWLINE);
    }
    sb.append("}").append(Helper.SEP_NEWLINE);
    sb.append("return null;").append(Helper.SEP_NEWLINE);
    sb.append("}").append(Helper.SEP_NEWLINE).append(Helper.SEP_NEWLINE);

    sb.append("public final static ByteArrayPairs createByteArrayPairs(Properties lngs) {").append(Helper.SEP_NEWLINE);
    sb.append("ByteArrayPairs result = new ByteArrayPairs(lngs.size());").append(Helper.SEP_NEWLINE);
    sb.append("Set<String> names = lngs.stringPropertyNames();").append(Helper.SEP_NEWLINE);
    sb.append("int i = 0;").append(Helper.SEP_NEWLINE);
    sb.append("for (String n : names) {").append(Helper.SEP_NEWLINE);
    sb.append("result.put(i, lngs.getProperty(n).getBytes(Helper.CHARSET_UTF8), n.getBytes(Helper.CHARSET_UTF8));").append(Helper.SEP_NEWLINE);
    sb.append("i++;").append(Helper.SEP_NEWLINE);
    sb.append("}").append(Helper.SEP_NEWLINE);
    sb.append("return result.sort();").append(Helper.SEP_NEWLINE);
    sb.append("}").append(Helper.SEP_NEWLINE);

    String tmp;
    for (final Language lng : LanguageExtractor.RELEVANT_LANGUAGES) {
      final String name = lng.name();
      final Properties props = new Properties();
      for (final TranslationInfo i : rowInfos) {
        if (Helper.isNotEmptyOrNull(tmp = i.get(lng))) {
          final String[] names = tmp.split("[、;/]");
          for (String n : names) {
            n = n.trim();
            if (Helper.isNotEmptyOrNull(n)) {
              props.put(n, i.key);
            }
          }
        }
      }
      try (BufferedWriter propsWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(file).getParent() + File.separator
          + "lng2name_" + name + ".txt"), Helper.CHARSET_UTF8));) {
        props.store(propsWriter, "lng2name_" + name);
      }
    }

    // original
    final Properties props = new Properties();
    for (final TranslationInfo i : rowInfos) {
      final String[] names = i.original.split("[;/、]");
      for (String n : names) {
        n = n.trim();
        if (Helper.isNotEmptyOrNull(n)) {
          props.put(n, i.key);
        }
      }
    }
    try (BufferedWriter propsWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(file).getParent() + File.separator
        + "lng2name_ORIGINAL.txt"), Helper.CHARSET_UTF8));) {
      props.store(propsWriter, "lng2name_ORIGINAL");
    }
    sb.append("public static final Properties getLngProperties(String f) {").append(Helper.SEP_NEWLINE);
    sb.append("Properties props = null;").append(Helper.SEP_NEWLINE);
    sb.append("try {").append(Helper.SEP_NEWLINE);
    sb.append("props = new Properties();").append(Helper.SEP_NEWLINE);
    sb.append("props.load(new InputStreamReader(LanguageConstants.class.getResourceAsStream(\"/\" + f), Helper.CHARSET_UTF8));").append(Helper.SEP_NEWLINE);
    sb.append("} catch (IOException e) {").append(Helper.SEP_NEWLINE);
    sb.append("System.err.println(\"Failed to load language properties for '\" + f + \"': \" + e);").append(Helper.SEP_NEWLINE);
    sb.append("}").append(Helper.SEP_NEWLINE);
    sb.append("return props;\n}").append(Helper.SEP_NEWLINE).append(Helper.SEP_NEWLINE);

    sb.append("private static final String[] getWikiLngs() {").append(Helper.SEP_NEWLINE);
    sb.append("return new String[] {").append(Helper.SEP_NEWLINE);
    for (final TranslationInfo i : wikiInfos) {
      sb.append("Language.").append(LanguageExtractor.getEnumKey(i)).append(".key,").append(Helper.SEP_NEWLINE);
    }
    sb.append("};").append(Helper.SEP_NEWLINE);
    sb.append("}").append(Helper.SEP_NEWLINE);
    sb.append("public static final String[] KEYS_WIKI;").append(Helper.SEP_NEWLINE);
    sb.append("static {").append(Helper.SEP_NEWLINE);
    sb.append("KEYS_WIKI = getWikiLngs();\njava.util.Arrays.sort(KEYS_WIKI);").append(Helper.SEP_NEWLINE);
    sb.append("}").append(Helper.SEP_NEWLINE);

    sb.append("private static final String[] getWiktLngs() {").append(Helper.SEP_NEWLINE);
    sb.append("return new String[] {").append(Helper.SEP_NEWLINE);
    for (final TranslationInfo i : wiktInfos) {
      sb.append("Language.").append(LanguageExtractor.getEnumKey(i)).append(".key,").append(Helper.SEP_NEWLINE);
    }
    sb.append("};").append(Helper.SEP_NEWLINE);
    sb.append("}").append(Helper.SEP_NEWLINE);
    sb.append("public static final String[] KEYS_WIKT;").append(Helper.SEP_NEWLINE);
    sb.append("static {").append(Helper.SEP_NEWLINE);
    sb.append("KEYS_WIKT = getWiktLngs();\njava.util.Arrays.sort(KEYS_WIKT);").append(Helper.SEP_NEWLINE);
    sb.append("}").append(Helper.SEP_NEWLINE);

    sb.append("}").append(Helper.SEP_NEWLINE);
    try (final BufferedWriter writer = new BufferedWriter(new FileWriter(file));) {
      writer.write(sb.toString());
    }
    System.out.println("创建LanguageConstants.java文件成功。");
  }

  private static String getLanguageFamily(final TranslationInfo i) {
    final String tmp = Helper.toConstantName(i.family);
    if (Helper.isNotEmptyOrNull(tmp)) {
      return tmp;
    } else {
      if (Helper.toConstantName(i.key).startsWith("ZH_")) {
        return "CHINESE";
      }
      return "NONE";
    }
  }

  private static String getEnumKey(final TranslationInfo i) {
    return Helper.toConstantName(i.key);
  }

  public static List<TranslationInfo> parseListOfWikipediasHtml(final String file) throws IOException {
    System.out.println("分析“维基百科语言列表”HTML文件'" + file + "' 。。。");
    final List<TranslationInfo> rowInfos = new FormattedArrayList<>();

    try (final BufferedReader reader = new BufferedReader(new FileReader(file));) {
      String line;
      int row = 0;
      boolean started = false;
      final String startTag = "<th>备注</th>";
      final String stopTag = "</table>";

      TranslationInfo rowInfo = new TranslationInfo();
      while ((line = reader.readLine()) != null) {
        line = line.trim();
        if (!started) {
          if (line.contains(startTag)) {
            started = true;
          }
        } else {
          if (line.contains(stopTag)) {
            if (rowInfo.isValid()) {
              rowInfos.add(rowInfo);
            }
            break;
          } else if (line.contains("</tr>")) {
            if (rowInfo.isValid()) {
              rowInfos.add(rowInfo);
            }
            rowInfo = new TranslationInfo();
            row = 0;
            rowInfo.invalidate();
          }
          if (line.contains("<td>")) {
            final String value = Helper.unescapeHtml(Helper.stripHtmlText(line, false)).trim();
            switch (row) {
              case 0:
                rowInfo.key = value;
                // System.out.println(value);
                break;
              case 1:
                rowInfo.put(Language.ZH, value.replace('、', ';'));
                break;
              case 2:
                rowInfo.put(Language.EN, value.replace(',', ';'));
                break;
              case 3:
                rowInfo.direction = value;
                break;
              case 4:
                rowInfo.original = value.replace(',', ';').replace('、', ';').replace('/', ';');

                break;
              case 5:
                rowInfo.comment = value;
                break;
              default:
                break;
            }
            row++;
          }
        }
      }
      System.out.println("从“维基百科语言列表”HTML文件总共读取" + rowInfos.size() + "种语言信息。");
      Collections.sort(rowInfos);
    }
    return rowInfos;
  }

  private static List<TranslationInfo> parseListOfWiktionariesHtml(final String file) throws IOException {
    System.out.println("分析“List of Wiktionaries”HTML文件'" + file + "' 。。。");

    final List<TranslationInfo> rowInfos = new FormattedArrayList<>();
    try (final BufferedReader reader = new BufferedReader(new FileReader(file));) {
      String line;
      int row = 0;
      boolean started = false;
      final String startTag = ">List of Wiktionaries by language family<";
      final String stopTag = "</table>";
      TranslationInfo rowInfo = new TranslationInfo();
      while ((line = reader.readLine()) != null) {
        line = line.trim();
        if (!started) {
          if (line.contains(startTag)) {
            started = true;
          }
        } else {
          if (line.contains(stopTag)) {
            if (rowInfo.isValid()) {
              rowInfos.add(rowInfo);
            }
            break;
          } else if (line.contains("</tr>")) {
            if (rowInfo.isValid()) {
              rowInfos.add(rowInfo);
            }
            rowInfo = new TranslationInfo();
            row = 0;
            rowInfo.invalidate();
          }
          if (line.contains("<td>")) {
            final String value = Helper.unescapeHtml(Helper.stripHtmlText(line, false)).trim();
            switch (row) {
              case 3:
                rowInfo.key = value;
                break;
              case 1:
                rowInfo.put(Language.EN, value.replace(',', ';'));
                break;
              case 2:
                rowInfo.original = value.replace(',', ';').replace('、', ';');
                break;
              default:
                break;
            }
            row++;
          }
        }
      }
      System.out.println("从“List of Wiktionaries”HTML文件总共读取" + rowInfos.size() + "种语言信息。");
      Collections.sort(rowInfos);
    }
    return rowInfos;
  }

}
