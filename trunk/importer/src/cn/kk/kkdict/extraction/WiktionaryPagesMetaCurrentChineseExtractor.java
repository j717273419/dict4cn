package cn.kk.kkdict.extraction;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import cn.kk.kkdict.beans.FormattedTreeSet;
import cn.kk.kkdict.beans.WiktContentState;
import cn.kk.kkdict.types.Gender;
import cn.kk.kkdict.types.Language;
import cn.kk.kkdict.types.WordType;
import cn.kk.kkdict.utils.ChineseHelper;
import cn.kk.kkdict.utils.Helper;

/**
 * Grammatik, Aussprache, Plural, Abkürzung, Beispiel, Übersetzungen, Wortart
 * 
 * @author x_kez
 * 
 */
public class WiktionaryPagesMetaCurrentChineseExtractor {

    // TODO: http://zh.wiktionary.org/w/index.php?title=apa&action=edit&section=6
    public static final String WIKT_PAGES_META_CURRENT_XML = "D:\\张克\\dev\\kkdict\\dicts\\wiktionary\\";
    private static final String LNG = "zh";
    public static final String WIKT_PAGES_META_CURRENT_XML_FILE = WIKT_PAGES_META_CURRENT_XML + LNG
            + "wiktionary-20120220-pages-meta-current.xml";

    public static final String OUT_DIR = "C:\\usr\\wiktionary";

    public static final String KEY_TRANSLATION = "翻译";

    public static final String KEY_SYNONYMS = "近义词";

    public static final String KEY_ANTONYMS = "反义词";

    public static final String[] RELEVANT_LANGUAGES_ISO = Language.LANGUAGES_ZH_ISO;

    public static final String[] RELEVANT_LANGUAGES_ZH = Language.LANGUAGES_ZH;

    private static final Pattern PATTERN_IRRELEVANT = Pattern.compile("(^[0-9]+$)|(^-[a-z]*$)");

    private static final Pattern Pattern_SPLIT_DEFINITION = Pattern.compile("(, )|，|/|(; ) |；|．|(\\. )|、");

    private static final Pattern PATTERN_GARBAGE = Pattern
            .compile("([,， ]*''[^']*'')|(\\[\\[:[^:]*:[^\\|]*\\|)|(\\{\\{[^|\\}]*\\|[^|\\}]*\\|)|(\\|[^\\}]*\\}\\})|(-\\{)|(\\}-)|(\\[\\[[^\\|]*\\|)|[\\[\\]\\{\\}［］]");

    private static final Pattern PATTERN_GARBAGE_PRE = Pattern
            .compile("(-\\{\\[\\[[^|]*\\|)|(\\]\\]\\}-)|(<.*?>)|(\\{\\{#if:.*?|\\[\\[)");

    // http://zh.wiktionary.org/wiki/Category:%E8%AF%AD%E8%A8%80%E6%A8%A1%E6%9D%BF
    public static final String[][] RELEVANT_LANGUAGES_ALT = { { Language.EN.key, "eng", "en1", "ang" },
            { Language.DE.key, "deu" }, { Language.FR.key, "fra" }, { Language.IT.key, "ita" },
            { Language.ES.key, "spa", "ca" }, { Language.PL.key, "pol" }, { Language.JA.key, "jpn", "adjn" },
            { Language.LA.key, "lat" }, { Language.RU.key, "rus" }, { Language.SV.key, "swe" },
            { Language.ZH.key, "sino", "zho", "han" }, { Language.PT.key, "por" }, { Language.SR.key, "srb" },
            { Language.HE.key, "iw" }, { Language.EO.key, "ia", "guoji" }, { Language.EL.key, "grc" },
            { Language.DA.key, "Da" }, { Language.CS.key, "Cs" } };

    private static final String[][] GENDERS = {
            { Gender.FEMININE.key, " 阴", " f ", "''{{f}}''", "''{{f|n}}''", "{{f|n}}", "''f''", "''{{f|p}}''", "{{f}}",
                    "{{f|p}}", "{[f}}", "|f}", "|f|p}", "|f|n}", "|f|p|", "|f|n|", "|f|" },
            { Gender.MASCULINE.key, " 阳", " m ", "''m''", "''{{m}}''", "''{{m|f}}''", "''{{m|p}}''", "''{{m|n}}''",
                    "{{m}}", "{[m}}", "{{m|p}}", "{{m|n}}", "{{m|f}}", "|m}", "|m|p}", "|m|f|n}", "|m|n}", "|m|f}",
                    "|m|f|n|", "|m|n|", "|m|f|", "|m|p|", "|m|", },
            { Gender.NEUTER.key, " 中", " n ", "''{{n}}''", "''n''", "''{{n|p}}''", "{{n}}", "{[n}}", "{{n|p}}",
                    "|n|p}", "|n}", "|n|p|", "|n|" },
            { Gender.PLURAL.key, " 复", " p ", "''{{p}}''", "{{p}}", "{[p}}", "''p''", "|p}", "|p|" } };

    private static final String[][] WORDTYPES = {
            { WordType.ADVERB.key, "(''adv'')", "{{-adv-}}", "{{=adv=}}", "{{-advb-}}", "{{副词}}" },
            { WordType.NOUN.key, "{{-n-}}", "''n.''", "{{-n2-}}", "{{=n=}}", "{{名词}}" },
            { WordType.VERB.key, "{{-verb-}}", "{{-v-}}", "{{=v=}}", "''v.''", "{{-vt-}}", "{{-v2-}}", "{{-adjn-}}",
                    "{{动词}}" }, { WordType.PRONOUN.key, "{{-pronoun-}}", "{{-p-}}", "{{=p=}}", "{{代词}}" },
            { WordType.ADJECTIVE.key, "{{-a-}}", "{{-adj-}}", "{{-asyn-}}", "{{-adj2-}}", "{{=a=}}", "{{形容词}}" },
            { WordType.ANTONYM.key, "{{-anton-}}" },
            { WordType.PREPOSITION.key, "{{-prep-}}", "{{=prep=}}", "{{-prep2-}}", "{{介词}}" },
            { WordType.NUMERAL.key, "{{-meas-}}", "{{-meas2-}}" },
            { WordType.CONJUNCTION.key, "{{-c-}}", "{{-conj-}}", "{{=c=}}", "{{-conj2-}}", "{{连词}}" },
            { WordType.INTERJECTION.key, "{{-interj-}}", "{{-intj-}}", "{{=i=}}", "{{-excl-}}", "{{感叹词}}" },
            { WordType.ABBREVIATION.key, "{{-abbr-}}", "{{-sx-}}", "{{缩写}}" },
            { WordType.ARTICLE.key, "{{-art-}}", "{{=art=}}", "{{冠词}}" }, { WordType.DETERMINER.key, "{{-det-}}" },
            { WordType.PARTICLE.key, "{{-part-}}", "{{=part=}}", "{{助词}}" },
            { WordType.PARTICIPE.key, "{{-ptcp-}}", "{{=ptcp=}}" }, { WordType.SINGULAR.key, "无复数" } };

    public static void main(String args[]) throws IOException {
        extractWiktionaryPagesMetaCurrent();
    }

    private static void extractWiktionaryPagesMetaCurrent() throws FileNotFoundException, IOException {
        long timeStarted = System.currentTimeMillis();
        Helper.precheck(WIKT_PAGES_META_CURRENT_XML_FILE, OUT_DIR);
        BufferedReader reader = new BufferedReader(new FileReader(WIKT_PAGES_META_CURRENT_XML_FILE), Helper.BUFFER_SIZE);
        BufferedWriter writer = new BufferedWriter(
                new FileWriter(OUT_DIR + File.separator + "output-dict.wikt_" + LNG), Helper.BUFFER_SIZE);
        final boolean isChinese = Language.ZH.key.equalsIgnoreCase(LNG);

        String tmp;

        int statSkipped = 0;
        int statOk = 0;
        Set<String> irrelevantPrefixes = new HashSet<String>();
        boolean irrelevantPrefixesNeeded = true;
        Set<String> globalCategories = new FormattedTreeSet<String>();
        WiktContentState state = new WiktContentState(LNG);
        String categoryKey = "[[Category:";
        while (state.setLine(reader.readLine()) != null) {
            if (isChinese) {
                state.setLine(ChineseHelper.toSimplifiedChinese(state.getLine().trim()));
            } else {
                state.setLine(state.getLine().trim());
            }
            if (irrelevantPrefixesNeeded && state.getLine().contains("</namespaces>")) {
                irrelevantPrefixesNeeded = false;
            } else if (irrelevantPrefixesNeeded
                    && Helper.isNotEmptyOrNull(tmp = Helper.substringBetweenLast(state.getLine(), ">", "</namespace>"))) {
                irrelevantPrefixes.add(tmp + ":");
                if (state.getLine().contains("key=\"14\"")) {
                    categoryKey = "[[" + tmp + ":";
                }
            } else if (Helper.isNotEmptyOrNull(tmp = Helper.substringBetween(state.getLine(), "<title>", "</title>"))) {
                if (write(writer, state)) {
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
                    state.invalidate();
                    statSkipped++;
                } else {
                    state.init(tmp);
                }
            } else if (state.isValid()) {
                if (Helper.isNotEmptyOrNull(tmp = Helper.substringBetween(state.getLine(), categoryKey, "]]"))) {
                    int wildcardIdx = tmp.indexOf('|');
                    if (wildcardIdx != -1) {
                        tmp = tmp.substring(0, wildcardIdx);
                    }
                    state.addCategory(tmp);
                    globalCategories.add(tmp);
                } else {
                    parseContent(writer, state);
                }
            }
        }
        if (write(writer, state)) {
            statOk++;
        } else {
            statSkipped++;
        }

        reader.close();
        writer.close();

        System.out.println("\n==============\n成功读取wiki_" + LNG + "词典。用时： "
                + Helper.formatDuration(System.currentTimeMillis() - timeStarted));
        System.out.println("有效词组：" + statOk);
        System.out.println("跳过词组：" + statSkipped + "\n==============\n");
    }

    private static void parseContent(BufferedWriter writer, WiktContentState state) throws IOException {
        String tmp;
        if (parseSourceLanguage(state)) {
            // found new source language
            state.clearSourceAttributes();
        } else {
            String line = state.getLine();
            if (state.getSourceLanguage() != null && parseSourceTranslationStartTag(writer, state)) {
                // found translation definition in text content, e.g. starting with {{-n-}} or === 名词 ===
                if (line.startsWith("#") && line.indexOf("#:") == -1) {
                    state.setLine(line.substring(1).trim());
                    parseSourceTranslationRow(state);
                }
            } else if (Helper.isNotEmptyOrNull(tmp = Helper.substringBetween(line, "*", ": "))
                    || Helper.isNotEmptyOrNull(tmp = Helper.substringBetween(line, "*", "："))) {
                // found translation row
                parseTargetTranslationRow(tmp, state);
            }
        }
    }

    private static boolean parseSourceTranslationRow(WiktContentState state) {
        // TODO Auto-generated method stub
        // [[柴火]]
        // [[笨蛋]]
        // （贬损意，主要在美国使用）一個男同性恋者
        state.setTargetLanguage(state.getFileLanguage());
        return putTargetTranslations(state);
    }

    private static boolean parseSourceTranslationStartTag(BufferedWriter writer, WiktContentState state)
            throws IOException {
        if (state.isTranslationContent()) {
            if (state.getLine().startsWith("==")) {
                write(writer, state);
                state.setTranslationContent(false);
                return findSourceTranslationStartTag(state);
            }
            return true;
        } else {
            return findSourceTranslationStartTag(state);
        }
    }

    private static boolean findSourceTranslationStartTag(WiktContentState state) {
        // find start tag
        String[] tmpResult = findAndCutWordType(state);
        if (tmpResult != null) {
            state.setSourceWordType(tmpResult[1]);
            state.setTranslationContent(true);
            return true;
        }
        return false;
    }

    private static String[] findAndCutWordType(WiktContentState state) {
        // find start tag
        String line = state.getLine();
        String[] tmpResult = Helper.findAndCut(line, WORDTYPES);
        if (tmpResult == null) {
            String tmp = Helper.substringBetween(line, "[[", "]]");
            if (Helper.isNotEmptyOrNull(tmp)) {
                for (WordType w : WordType.values()) {
                    if (tmp.equals(w.name) || tmp.equals(w.key)) {
                        String tocut = "[[" + tmp + "]]";
                        int startCut = line.indexOf(tocut);
                        int endCut = startCut + tocut.length();
                        String cutted;
                        if (endCut < line.length()) {
                            cutted = line.substring(0, startCut) + line.substring(endCut);
                        } else {
                            cutted = line.substring(0, startCut);
                        }
                        tmpResult = new String[] { cutted, w.key };
                    }
                }
            }
        }
        return tmpResult;
    }

    private static boolean parseTargetTranslationRow(String tmp, WiktContentState state) {
        boolean found = false;
        if (state.getSourceLanguage() == null) {
            state.setSourceLanguage(state.getFileLanguage());
        }
        if (parseTargetLanguage(state, tmp)) {
            found = putTargetTranslations(state);
        }
        return found;
    }

    private static boolean putTargetTranslations(WiktContentState state) {
        boolean found = false;
        String tmp;
        state.setLine(PATTERN_GARBAGE_PRE.matcher(state.getLine()).replaceAll(Helper.EMPTY_STRING).trim());
        if (Helper.isNotEmptyOrNull(state.getLine())) {
            state.setLine(Helper.unescapeHtml(state.getLine()));
            String[] defs = Pattern_SPLIT_DEFINITION.split(state.getLine(), 0);
            StringBuilder sb = new StringBuilder();
            String trans = state.getTranslation(state.getTargetLanguage());
            if (trans != null) {
                sb.append(trans);
            }
            for (String def : defs) {
                def = parseTargetTranslation(state, def);
                if (Helper.isNotEmptyOrNull(def) && !PATTERN_IRRELEVANT.matcher(def).matches()) {
                    if (sb.length() > 0) {
                        sb.append(Helper.SEP_SAME_MEANING);
                    }
                    sb.append(def);
                    if (state.hasTargetWordType()) {
                        sb.append(Helper.SEP_ATTRIBUTE).append(WordType.TYPE_ID).append(state.getTargetWordType());
                    }
                    if (state.hasTargetGender()) {
                        sb.append(Helper.SEP_ATTRIBUTE).append(Gender.TYPE_ID).append(state.getTargetGender());
                    }
                }
            }
            tmp = sb.toString();
            if (Helper.isNotEmptyOrNull(tmp)) {
                state.setTranslation(state.getTargetLanguage(), tmp);
                found = true;
            }
        }
        return found;
    }

    private static String parseTargetTranslation(WiktContentState state, String def) {
        String[] tmpResult;
        state.clearTargetAttributes();
        if ((tmpResult = Helper.findAndCut(def, GENDERS)) != null) {
            def = tmpResult[0];
            state.setTargetGender(tmpResult[1]);
        }
        if (state.hasTargetGender()) {
            // e.g. ends with " m" -> masculine
            if ((tmpResult = cutGenderSuffix(def)) != null) {
                def = tmpResult[0];
                state.setTargetGender(tmpResult[1]);
            }
        }
        if ((tmpResult = Helper.findAndCut(def, WORDTYPES)) != null) {
            def = tmpResult[0];
            state.setTargetWordType(tmpResult[1]);
        }
        def = def.replace('(', '（').replace(')', '）');
        def = PATTERN_GARBAGE.matcher(def).replaceAll(Helper.EMPTY_STRING).trim();

        return def;
    }

    private static String[] cutGenderSuffix(String def) {
        String gender = null;
        if (def.endsWith(" m")) {
            gender = Gender.MASCULINE.key;
            def = def.substring(0, def.length() - 2);
        } else if (def.endsWith(" f")) {
            gender = Gender.FEMININE.key;
            def = def.substring(0, def.length() - 2);
        } else if (def.endsWith(" n")) {
            gender = Gender.NEUTER.key;
            def = def.substring(0, def.length() - 2);
        } else if (def.endsWith(" p")) {
            gender = Gender.PLURAL.key;
            def = def.substring(0, def.length() - 2);
        }
        if (gender != null) {
            return new String[] { def, gender };
        } else {
            return null;
        }
    }

    private static boolean parseTargetLanguage(WiktContentState state, String testLng) {
        int tmpIdx;
        boolean f = false;
        int idx = -1;
        String key = null;
        if ((tmpIdx = state.getLine().indexOf('<')) != -1) {
            state.setLine(state.getLine().substring(0, tmpIdx).trim());
        }
        int i = 0;
        for (String lng : RELEVANT_LANGUAGES_ZH) {
            state.setTargetLanguage(RELEVANT_LANGUAGES_ISO[i]);
            if (lng.equals(testLng)) {
                key = "*" + testLng + ": ";
                if ((idx = state.getLine().indexOf(key)) != -1) {
                    break;
                }
                key = "*" + lng + "：";
                if ((idx = state.getLine().indexOf(key)) != -1) {
                    break;
                }
            }
            key = "*{{" + state.getTargetLanguage() + "}}: ";
            if ((idx = state.getLine().indexOf(key)) != -1) {
                break;
            }
            key = "*{{" + state.getTargetLanguage() + "}}：";
            idx = state.getLine().indexOf(key);
            if ((idx = state.getLine().indexOf(key)) != -1) {
                break;
            }
            i++;
        }
        if (idx != -1) {
            state.setLine(state.getLine().substring(idx + key.length()));
            f = true;
        } else {
            state.setTargetLanguage(null);
        }
        return f;
    }

    private static boolean parseSourceLanguage(WiktContentState state) {
        boolean found = false;
        String tmp;
        if (Helper.isNotEmptyOrNull(tmp = Helper.substringBetween(state.getLine(), "==", "=="))) {
            // ==德语==
            int i = 0;
            for (String lng : RELEVANT_LANGUAGES_ZH) {
                if (lng.equals(tmp)) {
                    state.setSourceLanguage(RELEVANT_LANGUAGES_ISO[i]);
                    found = true;
                    break;
                }
                i++;
            }
        } else if (Helper.isNotEmptyOrNull(tmp = Helper.substringBetween(state.getLine(), "{{-", "-"))) {
            int i = 0;
            // {{-fra-}}
            // {{-fra-|}}
            for (String lng : RELEVANT_LANGUAGES_ZH) {
                if (lng.equals(tmp)) {
                    state.setSourceLanguage(RELEVANT_LANGUAGES_ISO[i]);
                    found = true;
                    break;
                }
                i++;
            }
            if (state.getSourceLanguage() == null) {
                for (String lng : RELEVANT_LANGUAGES_ISO) {
                    if (lng.equals(tmp)) {
                        state.setSourceLanguage(lng);
                        found = true;
                        break;
                    }
                    i++;
                }
            }
            if (state.getSourceLanguage() == null) {
                for (i = 0; i < RELEVANT_LANGUAGES_ALT.length; i++) {
                    String[] alt = RELEVANT_LANGUAGES_ALT[i];
                    for (int j = 1; j < alt.length; j++) {
                        String lng = alt[j];
                        if (lng.equals(tmp)) {
                            state.setSourceLanguage(alt[0]);
                            found = true;
                            break;
                        }
                    }
                }
            }
        }
        return found;
    }

    private static boolean write(BufferedWriter writer, WiktContentState state) throws IOException {
        boolean success = false;
        if (Helper.isNotEmptyOrNull(state.getName())) {
            if (state.hasTranslations()) {
                String tmp = state.getName();
                if (state.hasSourceGender()) {
                    tmp += Helper.SEP_ATTRIBUTE + Gender.TYPE_ID + state.getSourceGender();
                }
                if (state.hasSourceWordType()) {
                    tmp += Helper.SEP_ATTRIBUTE + WordType.TYPE_ID + state.getSourceWordType();
                }
                if (state.hasCategories()) {
                    // TODO
                    // tmp += Helper.SEP_ATTRIBUTE + Category.ID + state.hasCategories();
                }
                state.getLanguages().put(state.getSourceLanguage(), tmp);
                writer.write(state.getTranslations() + Helper.SEP_NEWLINE);
                System.out.println(state.getTranslations());
                success = true;
            }
            state.init(state.getName());

        }
        return success;
    }

}
