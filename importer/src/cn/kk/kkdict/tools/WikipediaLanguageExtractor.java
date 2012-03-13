package cn.kk.kkdict.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import cn.kk.kkdict.beans.FormattedArrayList;
import cn.kk.kkdict.utils.Helper;

/**
 * <pre>
 * List of wikipedia languages: http://zh.wikipedia.org/zh-cn/Wikipedia:%E7%BB%B4%E5%9F%BA%E7%99%BE%E7%A7%91%E8%AF%AD%E8%A8%80%E5%88%97%E8%A1%A8 (全部语言版本)
 * List of wiktionary languages: http://meta.wikimedia.org/wiki/Wiktionary#List_of_Wiktionaries
 * TODO
 * ISO 639 languages: http://en.wiktionary.org/wiki/Transwiki:ISO_639:a
 * </pre>
 * 
 * @author x_kez
 * 
 */
public class WikipediaLanguageExtractor {
    final static class RowInfo implements Comparable<RowInfo> {
        public RowInfo() {

        }

        String key = Helper.EMPTY_STRING;
        String zh = Helper.EMPTY_STRING;
        String en = Helper.EMPTY_STRING;
        String de = Helper.EMPTY_STRING;
        String fr = Helper.EMPTY_STRING;
        String es = Helper.EMPTY_STRING;
        String ru = Helper.EMPTY_STRING;
        String direction = Helper.EMPTY_STRING;
        String original = Helper.EMPTY_STRING;
        String comment = Helper.EMPTY_STRING;

        public void invalidate() {
            key = Helper.EMPTY_STRING;
        }

        public boolean isValid() {
            return Helper.isNotEmptyOrNull(key);
        }

        @Override
        public int compareTo(RowInfo o) {
            return key.compareTo(o.key);
        }
    }

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        String listOfWikipediasUrlZh = "http://zh.wikipedia.org/zh-cn/Wikipedia:%E7%BB%B4%E5%9F%BA%E7%99%BE%E7%A7%91%E8%AF%AD%E8%A8%80%E5%88%97%E8%A1%A8";
        List<RowInfo> wikiInfos = parseListOfWikipediasHtml(Helper.download(listOfWikipediasUrlZh));

        String listOfWiktionariesUrl = "http://meta.wikimedia.org/wiki/Wiktionary#List_of_Wiktionaries";
        List<RowInfo> wiktInfos = parseListOfWiktionariesHtml(Helper.download(listOfWiktionariesUrl));

        String wikiTranslationSource = "O:\\TranslationSourceWiki.java";
        generateInitialTranslationSourceJava(wikiInfos, wiktInfos, wikiTranslationSource);

        // merges infos into wikiInfos
        mergeWikiWiktLanguages(wikiInfos, wiktInfos);

        String initialLanguageJava = "O:\\LanguageWiki.java";
        generateInitialLanguageJava(wikiInfos, initialLanguageJava);

    }

    private static void mergeWikiWiktLanguages(List<RowInfo> wikiInfos, List<RowInfo> wiktInfos) {
        System.out.println("合并语言信息 。。。");
        for (RowInfo i : wiktInfos) {
            boolean found = false;
            for (RowInfo j : wikiInfos) {
                if (j.key.equals(i.key)) {
                    if (Helper.isNotEmptyOrNull(j.en) && Helper.isNotEmptyOrNull(i.en) && !j.en.equals(i.en)
                            && !j.en.contains(i.en)) {
                        j.en = j.en + ", " + i.en;
                    }
                    if (Helper.isNotEmptyOrNull(j.zh) && Helper.isNotEmptyOrNull(i.zh) && !j.zh.equals(i.zh)
                            && !j.zh.contains(i.zh)) {
                        j.zh = j.zh + "、" + i.zh;
                    }
                    found = true;
                    break;
                }
            }
            if (!found) {
                wikiInfos.add(i);
            }
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

    private static void generateInitialLanguageJava(List<RowInfo> rowInfos, String file) throws IOException {
        System.out.println("创建LanguageWiki.java文件'" + file + "' 。。。");
        StringBuilder sb = new StringBuilder(1024 * 32);
        sb.append("package cn.kk.kkdict.types;").append(Helper.SEP_NEWLINE);
        sb.append("public enum Language {").append(Helper.SEP_NEWLINE);
        for (RowInfo i : rowInfos) {
            sb.append(getEnumKey(i)).append("(\"").append(i.key).append("\"),").append(Helper.SEP_NEWLINE);
        }
        sb.append(';').append(Helper.SEP_NEWLINE).append(Helper.SEP_NEWLINE);
        sb.append("public static final String TYPE_ID = \"语\";").append(Helper.SEP_NEWLINE);
        sb.append("public final String key;").append(Helper.SEP_NEWLINE).append(Helper.SEP_NEWLINE);
        sb.append("Language(final String key) {").append(Helper.SEP_NEWLINE);
        sb.append("this.key = key;").append(Helper.SEP_NEWLINE);
        sb.append("}").append(Helper.SEP_NEWLINE).append(Helper.SEP_NEWLINE);

        // zh
        sb.append("private static final String[][] LNGS_ZH = {").append(Helper.SEP_NEWLINE);
        for (RowInfo i : rowInfos) {
            String[] names = i.zh.split("、");
            for (String n : names) {
                n = n.trim();
                if (Helper.isNotEmptyOrNull(n)) {
                    sb.append("{ ").append(getEnumKey(i)).append(".key, \"").append(n).append("\" },")
                            .append(Helper.SEP_NEWLINE);
                }
            }
        }
        sb.append("};").append(Helper.SEP_NEWLINE).append(Helper.SEP_NEWLINE);
        sb.append("public static final String[] LANGUAGES_ZH_ISO = new String[LNGS_ZH.length];").append(
                Helper.SEP_NEWLINE);
        sb.append("public static final String[] LANGUAGES_ZH = new String[LNGS_ZH.length];").append(Helper.SEP_NEWLINE);
        sb.append("static {").append(Helper.SEP_NEWLINE);
        sb.append("int i = 0;").append(Helper.SEP_NEWLINE);
        sb.append("for (String[] lng : LNGS_ZH) {").append(Helper.SEP_NEWLINE);
        sb.append("LANGUAGES_ZH_ISO[i] = lng[0];").append(Helper.SEP_NEWLINE);
        sb.append("LANGUAGES_ZH[i] = lng[1];").append(Helper.SEP_NEWLINE);
        sb.append("i++;").append(Helper.SEP_NEWLINE);
        sb.append("}").append(Helper.SEP_NEWLINE);
        sb.append("}").append(Helper.SEP_NEWLINE).append(Helper.SEP_NEWLINE);

        // en
        sb.append("private static final String[][] LNGS_EN = {").append(Helper.SEP_NEWLINE);
        for (RowInfo i : rowInfos) {
            String[] names = i.en.split(", ");
            for (String n : names) {
                n = n.trim();
                if (Helper.isNotEmptyOrNull(n)) {
                    sb.append("{ ").append(getEnumKey(i)).append(".key, \"").append(n).append("\" },")
                            .append(Helper.SEP_NEWLINE);
                }
            }
        }
        sb.append("};").append(Helper.SEP_NEWLINE).append(Helper.SEP_NEWLINE);
        sb.append("public static final String[] LANGUAGES_EN_ISO = new String[LNGS_EN.length];").append(
                Helper.SEP_NEWLINE);
        sb.append("public static final String[] LANGUAGES_EN = new String[LNGS_EN.length];").append(Helper.SEP_NEWLINE);
        sb.append("static {").append(Helper.SEP_NEWLINE);
        sb.append("int i = 0;").append(Helper.SEP_NEWLINE);
        sb.append("for (String[] lng : LNGS_EN) {").append(Helper.SEP_NEWLINE);
        sb.append("LANGUAGES_EN_ISO[i] = lng[0];").append(Helper.SEP_NEWLINE);
        sb.append("LANGUAGES_EN[i] = lng[1];").append(Helper.SEP_NEWLINE);
        sb.append("i++;").append(Helper.SEP_NEWLINE);
        sb.append("}").append(Helper.SEP_NEWLINE);
        sb.append("}").append(Helper.SEP_NEWLINE).append(Helper.SEP_NEWLINE);

        // original
        sb.append("private static final String[][] LNGS_ORIGINAL = {").append(Helper.SEP_NEWLINE);
        for (RowInfo i : rowInfos) {
            String[] names = i.original.split("[,/、]");
            for (String n : names) {
                n = n.trim();
                if (Helper.isNotEmptyOrNull(n)) {
                    sb.append("{ ").append(getEnumKey(i)).append(".key, \"").append(n).append("\" },")
                            .append(Helper.SEP_NEWLINE);
                }
            }
        }
        sb.append("};").append(Helper.SEP_NEWLINE).append(Helper.SEP_NEWLINE);

        sb.append("public static final String[] LANGUAGES_ORIGINAL_ISO = new String[LNGS_ORIGINAL.length];").append(
                Helper.SEP_NEWLINE);
        sb.append("public static final String[] LANGUAGES_ORIGINAL = new String[LNGS_ORIGINAL.length];").append(Helper.SEP_NEWLINE);

        sb.append("static {").append(Helper.SEP_NEWLINE);
        sb.append("int i = 0;").append(Helper.SEP_NEWLINE);
        sb.append("for (String[] lng : LNGS_ORIGINAL) {").append(Helper.SEP_NEWLINE);
        sb.append("LANGUAGES_ORIGINAL_ISO[i] = lng[0];").append(Helper.SEP_NEWLINE);
        sb.append("LANGUAGES_ORIGINAL[i] = lng[1];").append(Helper.SEP_NEWLINE);
        sb.append("i++;").append(Helper.SEP_NEWLINE);
        sb.append("}").append(Helper.SEP_NEWLINE);
        sb.append("}").append(Helper.SEP_NEWLINE).append(Helper.SEP_NEWLINE);

        sb.append("}").append(Helper.SEP_NEWLINE);
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(sb.toString());
        writer.close();
        System.out.println("创建LanguageWiki.java文件成功。");
    }

    private static String getEnumKey(RowInfo i) {
        return i.key.toUpperCase().replace('-', '_');
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
                        break;
                    case 1:
                        rowInfo.zh = value;
                        break;
                    case 2:
                        rowInfo.en = value;
                        break;
                    case 3:
                        rowInfo.direction = value;
                        break;
                    case 4:
                        rowInfo.original = value;
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
                        rowInfo.en = value;
                        break;
                    case 2:
                        rowInfo.original = value;
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
