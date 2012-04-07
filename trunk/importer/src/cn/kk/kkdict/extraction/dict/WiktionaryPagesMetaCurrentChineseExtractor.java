package cn.kk.kkdict.extraction.dict;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.regex.Pattern;

import cn.kk.kkdict.beans.WikiParseStep;
import cn.kk.kkdict.types.Gender;
import cn.kk.kkdict.types.Language;
import cn.kk.kkdict.types.LanguageConstants;
import cn.kk.kkdict.types.WordType;
import cn.kk.kkdict.utils.ArrayHelper;
import cn.kk.kkdict.utils.Helper;

/**
 * Grammatik, Aussprache, Plural, Abkürzung, Beispiel, Übersetzungen, Wortart TODO: eine Zeile eine Übersetzung
 * 
 * @author x_kez
 * 
 */
public class WiktionaryPagesMetaCurrentChineseExtractor extends WikiExtractorBase {

    // TODO: http://zh.wiktionary.org/w/index.php?title=apa&action=edit&section=6
    public static final String IN_DIR = Helper.DIR_IN_DICTS + "\\wiktionary";

    public static final String OUT_DIR = Helper.DIR_OUT_DICTS + "\\wiktionary";

    public static final String KEY_TRANSLATION = "翻译";

    public static final String KEY_SYNONYMS = "近义词";

    public static final String KEY_ANTONYMS = "反义词";

    public static final String[] RELEVANT_LANGUAGES_ISO = LanguageConstants.LANGUAGES_ZH_ISO;

    public static final String[] RELEVANT_LANGUAGES_ZH = LanguageConstants.LANGUAGES_ZH;

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
        final String LNG = "zh";
        WiktionaryPagesMetaCurrentChineseExtractor extractor = new WiktionaryPagesMetaCurrentChineseExtractor();
        extractor.extractWiktionaryPagesMetaCurrent(IN_DIR + File.separator + LNG
                + "wiktionary-latest-pages-meta-current.xml.bz2");
    }

    private int extractWiktionaryPagesMetaCurrent(String f) throws FileNotFoundException, IOException {
        initialize(f, OUT_DIR, "output-dict.wikt_", null, null);

        while (-1 != (len = ArrayHelper.readLine(in, lineBB))) {
            signal();
            if (WikiParseStep.HEADER == step) {
                parseHeader();
            } else {
                if (ArrayHelper.substringBetween(lineBBArray, 0, len, PREFIX_TITLE_BYTES, SUFFIX_TITLE_BYTES, tmpBB) > 0) {
                    // new title found
                    handleContentTitle();
                } else if (name != null) {
                    // within content
                    if (ArrayHelper.substringBetween(lineBBArray, 0, len, categoryKeyBytes, SUFFIX_WIKI_TAG_BYTES,
                            tmpBB) > 0
                            || ArrayHelper.substringBetween(lineBBArray, 0, len, categoryKeyBytes2,
                                    SUFFIX_WIKI_TAG_BYTES, tmpBB) > 0) {
                        // new category found for current name
                        // addCategory();
                    } else if (ArrayHelper.substringBetween(lineBBArray, 0, len, PREFIX_WIKI_TAG_BYTES,
                            SUFFIX_WIKI_TAG_BYTES, tmpBB) > 0) {
                        // found wiki tag
                        int idx = ArrayHelper.indexOf(tmpBB, (byte) ':');
                        if (idx > 0 && idx < 13) {
                            // has : in tag, perhaps translation
                            addTranslation(idx);
                        } else if (idx == -1 && !isCategoryName) {
                            // something else
                            if (-1 != (idx = ArrayHelper.indexOf(lineBBArray, 0, len, tmpBBArray, 0, tmpBB.limit()))
                                    && idx < 6) {
                                // tag at beginning of line, perhaps related word
                                // addRelated();
                            }
                        }
                    }
                }
            }
        }
        cleanup();
        return statOk;
    }
}
