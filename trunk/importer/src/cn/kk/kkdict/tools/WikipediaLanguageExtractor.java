package cn.kk.kkdict.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import cn.kk.kkdict.beans.FormattedArrayList;
import cn.kk.kkdict.beans.FormattedTreeMap;
import cn.kk.kkdict.beans.FormattedTreeSet;
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
public class WikipediaLanguageExtractor {
    public static final String GENERATED_DIR = Helper.DIR_OUT_GENERATED;
    // public static final Language[] RELEVANT_LANGUAGES = { Language.EN, Language.RU, Language.PL, Language.JA,
    // Language.KO, Language.ZH, Language.DE, Language.FR, Language.IT, Language.ES, Language.PT, Language.NL,
    // Language.SV, Language.UK, Language.VI, Language.CA, Language.NO, Language.FI, Language.CS, Language.HU,
    // Language.ID, Language.TR, Language.RO, Language.FA, Language.AR, Language.DA, Language.EO, Language.SR,
    // Language.LT, Language.SK, Language.SL, Language.MS, Language.HE, Language.BG, Language.KK, Language.EU,
    // Language.VO, Language.WAR, Language.HR, Language.HI, Language.LA };

    public static final Language[] RELEVANT_LANGUAGES = { Language.ZH };

    final static class RowInfo implements Comparable<RowInfo> {
        public RowInfo() {
        }

        String key = Helper.EMPTY_STRING;
        String family = Helper.EMPTY_STRING;
        String iso1 = Helper.EMPTY_STRING;
        String iso2 = Helper.EMPTY_STRING;
        String iso3 = Helper.EMPTY_STRING;
        Map<String, String> lngMap = new FormattedTreeMap<String, String>();

        String direction = Helper.EMPTY_STRING;
        String original = Helper.EMPTY_STRING;
        String comment = Helper.EMPTY_STRING;

        public void invalidate() {
            key = Helper.EMPTY_STRING;
            iso1 = Helper.EMPTY_STRING;
            iso2 = Helper.EMPTY_STRING;
            iso3 = Helper.EMPTY_STRING;
        }

        public boolean isValid() {
            if (key.isEmpty()) {
                if (Helper.isNotEmptyOrNull(iso1)) {
                    key = iso1;
                } else if (Helper.isNotEmptyOrNull(iso2)) {
                    key = iso2;
                } else if (Helper.isNotEmptyOrNull(iso3)) {
                    key = iso3;
                }
            }
            return Helper.isNotEmptyOrNull(key);
        }

        @Override
        public int compareTo(RowInfo o) {
            return key.compareTo(o.key);
        }

        public void put(Language lng, String value) {
            this.lngMap.put(lng.key, value);
        }

        public String get(Language lng) {
            return this.lngMap.get(lng.key);
        }
    }

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        new File(GENERATED_DIR).mkdirs();
        // full english: http://s23.org/wikistats/wikipedias_html.php?sort=prefix_asc
        // actual iso template: http://zh.wikipedia.org/wiki/Template:ISO639
        String listOfWikipediasUrlZh = "http://zh.wikipedia.org/zh-cn/Wikipedia:%E7%BB%B4%E5%9F%BA%E7%99%BE%E7%A7%91%E8%AF%AD%E8%A8%80%E5%88%97%E8%A1%A8";
        List<RowInfo> wikiInfos = parseListOfWikipediasHtml(Helper.download(listOfWikipediasUrlZh));

        String listOfWiktionariesUrl = "http://meta.wikimedia.org/wiki/Wiktionary#List_of_Wiktionaries";
        List<RowInfo> wiktInfos = parseListOfWiktionariesHtml(Helper.download(listOfWiktionariesUrl));

        List<RowInfo> allInfos = new FormattedArrayList<RowInfo>(wikiInfos);

        String wikiTranslationSource = GENERATED_DIR + "\\TranslationSource.java";
        generateInitialTranslationSourceJava(wikiInfos, wiktInfos, wikiTranslationSource);

        // merges infos into allInfos
        mergeWikiWiktLanguages(allInfos, wiktInfos);

        String iso639MainPageUrl = "http://en.wiktionary.org/wiki/Transwiki:ISO_639";
        enrichWithIso639Languages(allInfos, iso639MainPageUrl);

        enrichWithJavaLocales(allInfos);

        generateInitialLanguageFamilyJava(allInfos, GENERATED_DIR + "\\LanguageFamily.java");

        generateInitialLanguageJava(allInfos, GENERATED_DIR + "\\Language.java");

        generateInitialLanguageConstantsJava(allInfos, wikiInfos, wiktInfos, GENERATED_DIR + "\\LanguageConstants.java");

    }

    private static void enrichWithJavaLocales(List<RowInfo> wikiInfos) {
        String tmp;
        Locale[] locales = Locale.getAvailableLocales();
        for (int i = 0; i < locales.length; i++) {
            Locale locale = locales[i];
            RowInfo rowInfo = new RowInfo();
            rowInfo.key = locale.getLanguage();
            rowInfo.original = locale.getDisplayName(locale);
            String displayNameEn = locale.getDisplayName(Locale.ENGLISH);
            rowInfo.put(Language.EN, displayNameEn);
            for (Language lng : RELEVANT_LANGUAGES) {
                if (!displayNameEn.equals((tmp = locale.getDisplayName(new Locale(lng.key))))) {
                    rowInfo.put(lng, tmp);
                }
            }
            enrichByRowInfo(wikiInfos, rowInfo);
        }
    }

    private static void enrichWithIso639Languages(final List<RowInfo> wikiInfos, final String iso639MainPageUrl)
            throws IOException {
        for (char c = 'a'; c <= 'z'; c++) {
            enrichWithIso639LanguagesPage(wikiInfos, Helper.download(iso639MainPageUrl + ":" + c));
        }
        System.out.println("“Transwiki ISO 639”合并后总语言信息：" + wikiInfos.size());
        Collections.sort(wikiInfos);
    }

    private static void enrichWithIso639LanguagesPage(List<RowInfo> wikiInfos, String file) throws IOException {
        System.out.println("分析“Transwiki ISO 639”HTML文件'" + file + "' 。。。");
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        int row = 0;
        boolean started = false;
        String startTag = ">deu<";
        String stopTag = "</table>";
        RowInfo rowInfo = new RowInfo();
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (!started) {
                if (line.contains(startTag)) {
                    started = true;
                }
            } else {
                if (line.contains(stopTag)) {
                    enrichByRowInfo(wikiInfos, rowInfo);
                    break;
                } else if (line.contains("</tr>")) {
                    enrichByRowInfo(wikiInfos, rowInfo);
                    rowInfo = new RowInfo();
                    row = 0;
                    rowInfo.invalidate();
                }
                if (line.contains("<td") || line.contains("<th")) {
                    String value = Helper.unescapeHtml(Helper.stripHtmlText(line, false)).trim();
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
                    case 6:
                        rowInfo.put(Language.EN, value);
                    case 7:
                        rowInfo.put(Language.FR, value);
                    case 8:
                        rowInfo.put(Language.ES, value);
                    case 9:
                        rowInfo.put(Language.ZH, value);
                    case 10:
                        rowInfo.put(Language.RU, value);
                    case 11:
                        rowInfo.put(Language.DE, value);
                    }
                    row++;
                }
            }
        }
    }

    private static boolean enrichByRowInfo(List<RowInfo> wikiInfos, RowInfo rowInfo) {
        boolean found = false;
        if (rowInfo.isValid()) {
            String tmp;
            for (RowInfo j : wikiInfos) {
                if (j.key.equals(rowInfo.key)) {
                    if (null != (tmp = getNewContent(j.original, rowInfo.original))) {
                        j.original = tmp;
                    }
                    for (Language lng : RELEVANT_LANGUAGES) {
                        if (null != (tmp = getNewContent(j.get(lng), rowInfo.get(lng)))) {
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

    private static String getNewContent(String oldInfo, String newInfo) {
        if (Helper.isNotEmptyOrNull(oldInfo) && Helper.isNotEmptyOrNull(newInfo) && !oldInfo.equals(newInfo)
                && !oldInfo.contains(newInfo)) {
            return oldInfo + "; " + newInfo;
        } else if (Helper.isEmptyOrNull(oldInfo) && Helper.isNotEmptyOrNull(newInfo)) {
            return newInfo;
        }
        return null;
    }

    private static void mergeWikiWiktLanguages(List<RowInfo> wikiInfos, List<RowInfo> wiktInfos) {
        System.out.println("合并语言信息 。。。");
        for (RowInfo i : wiktInfos) {
            enrichByRowInfo(wikiInfos, i);
        }
        System.out.println("合并后总共语言：" + wikiInfos.size());
    }

    private static void generateInitialTranslationSourceJava(List<RowInfo> wikiInfos, List<RowInfo> wiktInfos,
            String file) throws IOException {
        System.out.println("创建TranslationSourceWiki.java文件'" + file + "' 。。。");
        StringBuilder sb = new StringBuilder(1024 * 32);
        sb.append("package cn.kk.kkdict.types;").append(Helper.SEP_NEWLINE);
        sb.append("public enum TranslationSource {").append(Helper.SEP_NEWLINE);
        for (RowInfo i : wikiInfos) {
            sb.append("WIKI_").append(getEnumKey(i)).append("(\"").append("wiki_").append(getEnumKey(i).toLowerCase())
                    .append("\"),").append(Helper.SEP_NEWLINE);
        }
        for (RowInfo i : wiktInfos) {
            sb.append("WIKT_").append(getEnumKey(i)).append("(\"").append("wikt_").append(getEnumKey(i).toLowerCase())
                    .append("\"),").append(Helper.SEP_NEWLINE);
        }
        sb.append(';').append(Helper.SEP_NEWLINE).append(Helper.SEP_NEWLINE);
        sb.append("public static final String TYPE_ID = \"源\";").append(Helper.SEP_NEWLINE);
        sb.append("public final String key;").append(Helper.SEP_NEWLINE).append(Helper.SEP_NEWLINE);
        sb.append("TranslationSource(final String key) {").append(Helper.SEP_NEWLINE);
        sb.append("this.key = key;").append(Helper.SEP_NEWLINE);
        sb.append("}").append(Helper.SEP_NEWLINE).append(Helper.SEP_NEWLINE);
        sb.append("}").append(Helper.SEP_NEWLINE);
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(sb.toString());
        writer.close();
        System.out.println("创建TranslationSourceWiki.java文件成功。");
    }

    private static void generateInitialLanguageFamilyJava(List<RowInfo> rowInfos, String file) throws IOException {
        Set<String> families = new FormattedTreeSet<String>();
        for (RowInfo i : rowInfos) {
            String enumName = Helper.toConstantName(i.family);
            if (Helper.isNotEmptyOrNull(enumName)) {
                families.add(enumName);
            }
        }
        Collections.sort(rowInfos);
        System.out.println("创建LanguageFamily.java文件'" + file + "'（语系：" + families.size() + "） 。。。");
        StringBuilder sb = new StringBuilder(1024 * 32);
        sb.append("package cn.kk.kkdict.types;").append(Helper.SEP_NEWLINE);
        sb.append("public enum LanguageFamily {").append(Helper.SEP_NEWLINE);

        sb.append("NONE,").append(Helper.SEP_NEWLINE);
        for (String f : families) {
            sb.append(f.toUpperCase()).append(",").append(Helper.SEP_NEWLINE);
        }
        sb.append('}').append(Helper.SEP_NEWLINE).append(Helper.SEP_NEWLINE);

        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(sb.toString());
        writer.close();
        System.out.println("创建LanguageFamily.java文件成功。");
    }

    private static void generateInitialLanguageJava(List<RowInfo> rowInfos, String file) throws IOException {
        Set<String> families = new FormattedTreeSet<String>();
        for (RowInfo i : rowInfos) {
            String enumName = Helper.toConstantName(i.family);
            if (Helper.isNotEmptyOrNull(enumName)) {
                families.add(enumName);
            }
        }
        Collections.sort(rowInfos);
        System.out
                .println("创建Language.java文件'" + file + "'（语言：" + rowInfos.size() + "，语系：" + families.size() + "） 。。。");
        StringBuilder sb = new StringBuilder(1024 * 32);
        sb.append("package cn.kk.kkdict.types;").append(Helper.SEP_NEWLINE);
        sb.append("public enum Language {").append(Helper.SEP_NEWLINE);
        // lng enums
        for (RowInfo i : rowInfos) {
            sb.append(getEnumKey(i)).append("(\"").append(i.key).append("\", LanguageFamily.")
                    .append(getLanguageFamily(i)).append("),").append(Helper.SEP_NEWLINE);
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
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(sb.toString());
        writer.close();
        System.out.println("创建Language.java文件成功。");
    }

    private static void generateInitialLanguageConstantsJava(List<RowInfo> rowInfos, List<RowInfo> wikiInfos,
            List<RowInfo> wiktInfos, String file) throws IOException {
        Set<String> families = new FormattedTreeSet<String>();
        for (RowInfo i : rowInfos) {
            String enumName = Helper.toConstantName(i.family);
            if (Helper.isNotEmptyOrNull(enumName)) {
                families.add(enumName);
            }
        }
        Collections.sort(rowInfos);
        System.out.println("创建LanguageConstants.java文件'" + file + "'（语言：" + rowInfos.size() + "，语系：" + families.size()
                + "） 。。。");
        StringBuilder sb = new StringBuilder(1024 * 32);
        sb.append("package cn.kk.kkdict.types;\n\nimport java.util.*;").append(Helper.SEP_NEWLINE);
        sb.append("public final class LanguageConstants {").append(Helper.SEP_NEWLINE);

        String tmp;
        for (Language lng : RELEVANT_LANGUAGES) {
            String name = lng.name();
            sb.append("private static final String[][] getLng").append(name).append("() {\nreturn new String[][] {")
                    .append(Helper.SEP_NEWLINE);
            for (RowInfo i : rowInfos) {
                if (Helper.isNotEmptyOrNull(tmp = i.get(lng))) {
                    String[] names = tmp.split("[、;/]");
                    for (String n : names) {
                        n = n.trim();
                        if (Helper.isNotEmptyOrNull(n)) {
                            sb.append("{ Language.").append(getEnumKey(i)).append(".key, \"").append(n).append("\" },")
                                    .append(Helper.SEP_NEWLINE);
                        }
                    }
                }
            }
            sb.append("};\n}").append(Helper.SEP_NEWLINE).append(Helper.SEP_NEWLINE);
            sb.append("public static final String[] LANGUAGES_").append(name).append("_ISO;")
                    .append(Helper.SEP_NEWLINE);
            sb.append("public static final String[] LANGUAGES_").append(name).append(";").append(Helper.SEP_NEWLINE);
            sb.append("static {").append(Helper.SEP_NEWLINE);
            sb.append("String[][] lngs = getLng").append(name).append("();").append(Helper.SEP_NEWLINE);
            sb.append("LANGUAGES_").append(name).append("_ISO = new String[lngs.length];").append(Helper.SEP_NEWLINE);
            sb.append("LANGUAGES_").append(name).append(" = new String[lngs.length];").append(Helper.SEP_NEWLINE);
            sb.append("int i = 0;").append(Helper.SEP_NEWLINE);
            sb.append("for (String[] lng : lngs) {").append(Helper.SEP_NEWLINE);
            sb.append("LANGUAGES_").append(name).append("_ISO[i] = lng[0];").append(Helper.SEP_NEWLINE);
            sb.append("LANGUAGES_").append(name).append("[i] = lng[1];").append(Helper.SEP_NEWLINE);
            sb.append("i++;").append(Helper.SEP_NEWLINE);
            sb.append("}").append(Helper.SEP_NEWLINE);
            sb.append("}").append(Helper.SEP_NEWLINE).append(Helper.SEP_NEWLINE);
        }

        // original
        sb.append("private static final String[][] getLngORIGINAL () {\nreturn new String[][] {").append(
                Helper.SEP_NEWLINE);
        for (RowInfo i : rowInfos) {
            String[] names = i.original.split("[;/、]");
            for (String n : names) {
                n = n.trim();
                if (Helper.isNotEmptyOrNull(n)) {
                    sb.append("{ Language.").append(getEnumKey(i)).append(".key, \"").append(n).append("\" },")
                            .append(Helper.SEP_NEWLINE);
                }
            }
        }
        sb.append("};\n}").append(Helper.SEP_NEWLINE).append(Helper.SEP_NEWLINE);
        sb.append("public static final String[] LANGUAGES_ORIGINAL_ISO;").append(Helper.SEP_NEWLINE);
        sb.append("public static final String[] LANGUAGES_ORIGINAL;").append(Helper.SEP_NEWLINE);
        sb.append("static {").append(Helper.SEP_NEWLINE);
        sb.append("String[][] lngs = getLngORIGINAL();").append(Helper.SEP_NEWLINE);
        sb.append("LANGUAGES_ORIGINAL_ISO = new String[lngs.length];").append(Helper.SEP_NEWLINE);
        sb.append("LANGUAGES_ORIGINAL = new String[lngs.length];").append(Helper.SEP_NEWLINE);
        sb.append("int i = 0;").append(Helper.SEP_NEWLINE);
        sb.append("for (String[] lng : lngs) {").append(Helper.SEP_NEWLINE);
        sb.append("LANGUAGES_ORIGINAL_ISO[i] = lng[0];").append(Helper.SEP_NEWLINE);
        sb.append("LANGUAGES_ORIGINAL[i] = lng[1];").append(Helper.SEP_NEWLINE);
        sb.append("i++;").append(Helper.SEP_NEWLINE);
        sb.append("}").append(Helper.SEP_NEWLINE);
        sb.append("}").append(Helper.SEP_NEWLINE).append(Helper.SEP_NEWLINE);

        sb.append("public static final String[] KEYS_ZH;").append(Helper.SEP_NEWLINE);
        sb.append("static {").append(Helper.SEP_NEWLINE);
        sb.append("Set<String> keys = new HashSet<String>(LANGUAGES_ZH_ISO.length);").append(Helper.SEP_NEWLINE);
        sb.append("for (String k : LANGUAGES_ZH_ISO) {").append(Helper.SEP_NEWLINE);
        sb.append("keys.add(k);").append(Helper.SEP_NEWLINE);
        sb.append("}").append(Helper.SEP_NEWLINE);
        sb.append("KEYS_ZH = keys.toArray(new String[keys.size()]);\njava.util.Arrays.sort(KEYS_ZH);").append(
                Helper.SEP_NEWLINE);
        sb.append("}").append(Helper.SEP_NEWLINE);

        sb.append("private static final String[] getWikiLngs() {").append(Helper.SEP_NEWLINE);
        sb.append("return new String[] {").append(Helper.SEP_NEWLINE);
        for (RowInfo i : wikiInfos) {
            sb.append("Language.").append(getEnumKey(i)).append(".key,").append(Helper.SEP_NEWLINE);
        }
        sb.append("};").append(Helper.SEP_NEWLINE);
        sb.append("}").append(Helper.SEP_NEWLINE);
        sb.append("public static final String[] KEYS_WIKI;").append(Helper.SEP_NEWLINE);
        sb.append("static {").append(Helper.SEP_NEWLINE);
        sb.append("KEYS_WIKI = getWikiLngs();\njava.util.Arrays.sort(KEYS_WIKI);").append(Helper.SEP_NEWLINE);
        sb.append("}").append(Helper.SEP_NEWLINE);

        sb.append("private static final String[] getWiktLngs() {").append(Helper.SEP_NEWLINE);
        sb.append("return new String[] {").append(Helper.SEP_NEWLINE);
        for (RowInfo i : wiktInfos) {
            sb.append("Language.").append(getEnumKey(i)).append(".key,").append(Helper.SEP_NEWLINE);
        }
        sb.append("};").append(Helper.SEP_NEWLINE);
        sb.append("}").append(Helper.SEP_NEWLINE);
        sb.append("public static final String[] KEYS_WIKT;").append(Helper.SEP_NEWLINE);
        sb.append("static {").append(Helper.SEP_NEWLINE);
        sb.append("KEYS_WIKT = getWiktLngs();\njava.util.Arrays.sort(KEYS_WIKT);").append(Helper.SEP_NEWLINE);
        sb.append("}").append(Helper.SEP_NEWLINE);

        sb.append("}").append(Helper.SEP_NEWLINE);
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(sb.toString());
        writer.close();
        System.out.println("创建LanguageConstants.java文件成功。");
    }

    private static String getLanguageFamily(RowInfo i) {
        String tmp = Helper.toConstantName(i.family);
        if (Helper.isNotEmptyOrNull(tmp)) {
            return tmp;
        } else {
            return "NONE";
        }
    }

    private static String getEnumKey(RowInfo i) {
        return Helper.toConstantName(i.key);
    }

    public static List<RowInfo> parseListOfWikipediasHtml(String file) throws IOException {
        System.out.println("分析“维基百科语言列表”HTML文件'" + file + "' 。。。");
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        int row = 0;
        boolean started = false;
        String startTag = "<th>备注</th>";
        String stopTag = "</table>";
        List<RowInfo> rowInfos = new FormattedArrayList<RowInfo>();
        RowInfo rowInfo = new RowInfo();
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
                    rowInfo = new RowInfo();
                    row = 0;
                    rowInfo.invalidate();
                }
                if (line.contains("<td>")) {
                    String value = Helper.unescapeHtml(Helper.stripHtmlText(line, false)).trim();
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
                        ;
                        break;
                    case 5:
                        rowInfo.comment = value;
                        break;
                    }
                    row++;
                }
            }
        }
        System.out.println("从“维基百科语言列表”HTML文件总共读取" + rowInfos.size() + "种语言信息。");
        Collections.sort(rowInfos);
        return rowInfos;
    }

    private static List<RowInfo> parseListOfWiktionariesHtml(String file) throws IOException {
        System.out.println("分析“List of Wiktionaries”HTML文件'" + file + "' 。。。");
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        int row = 0;
        boolean started = false;
        String startTag = ">List of Wiktionaries by language family<";
        String stopTag = "</table>";
        List<RowInfo> rowInfos = new FormattedArrayList<RowInfo>();
        RowInfo rowInfo = new RowInfo();
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
                    rowInfo = new RowInfo();
                    row = 0;
                    rowInfo.invalidate();
                }
                if (line.contains("<td>")) {
                    String value = Helper.unescapeHtml(Helper.stripHtmlText(line, false)).trim();
                    switch (row) {
                    case 3:
                        rowInfo.key = value;
                        break;
                    case 1:
                        rowInfo.put(Language.EN, value.replace(',', ';'));
                        break;
                    case 2:
                        rowInfo.original = value.replace(',', ';').replace('、', ';');
                        ;
                        break;
                    }
                    row++;
                }
            }
        }
        System.out.println("从“List of Wiktionaries”HTML文件总共读取" + rowInfos.size() + "种语言信息。");
        Collections.sort(rowInfos);
        return rowInfos;
    }

}
