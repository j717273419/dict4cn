package cn.kk.kkdict.extraction.dict;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import cn.kk.kkdict.beans.ByteArrayPairs;
import cn.kk.kkdict.beans.WikiParseStep;
import cn.kk.kkdict.types.Language;
import cn.kk.kkdict.types.LanguageConstants;
import cn.kk.kkdict.utils.ArrayHelper;
import cn.kk.kkdict.utils.ChineseHelper;
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

    public static final byte[] KEY_TRANSLATION_BYTES = "翻译".getBytes(Helper.CHARSET_UTF8);

    byte[] sourceLng;
    byte[] targetLng;
    byte[] sourceWordType;
    byte[] sourceGender;
    byte[] targetWordType;
    byte[] targetGender;
    ByteArrayPairs languageNames;

    private final static ByteArrayPairs languageAlternatives = LanguageConstants.createByteArrayPairs(LanguageConstants
            .getLngProperties("lng2alt.txt"));

    public static void main(String args[]) throws IOException {
        final String LNG = "zh";
        WiktionaryPagesMetaCurrentChineseExtractor extractor = new WiktionaryPagesMetaCurrentChineseExtractor();
        ArrayHelper.WARN = false;
        extractor.extractWiktionaryPagesMetaCurrent(IN_DIR + File.separator + LNG
                + "wiktionary-latest-pages-meta-current.xml.bz2");
    }

    private int extractWiktionaryPagesMetaCurrent(String f) throws FileNotFoundException, IOException {
        initialize(f, OUT_DIR, "output-dict.wikt_", null, null);
        ParserResult pr;
        byte[] tmp;
        while (-1 != (len = ArrayHelper.readLine(in, lineBB))) {
            signal();
            if (WikiParseStep.HEADER == step) {
                parseHeader();
            } else {
                if (ArrayHelper.substringBetween(lineArray, 0, len, PREFIX_TITLE_BYTES, SUFFIX_TITLE_BYTES, tmpBB) > 0) {
                    // new title found
                    // write old definition
                    writeDefinition();
                    clearAttributes();
                    handleContentTitle();
                } else if (isValid()) {
                    if (step == WikiParseStep.TITLE) {
                        int idx;
                        if (-1 != (idx = ArrayHelper.indexOf(lineArray, 0, len, TAG_TEXT_BEGIN_BYTES))) {
                            int offset = idx + TAG_TEXT_BEGIN_BYTES.length;
                            len = len - offset;
                            System.arraycopy(lineArray, offset, lineArray, 0, len);
                            lineBB.limit(len);
                            step = WikiParseStep.CONTENT;
                        }
                    }
                    if (step == WikiParseStep.CONTENT) {
                        // within content
                        if (ArrayHelper.substringBetween(lineArray, 0, len, categoryKeyBytes, SUFFIX_WIKI_TAG_BYTES,
                                tmpBB) > 0
                                || ArrayHelper.substringBetween(lineArray, 0, len, categoryKeyBytes2,
                                        SUFFIX_WIKI_TAG_BYTES, tmpBB) > 0) {
                            // new category found for current name
                            // addCategory();
                        } else if (ParserResult.NO_RESULT != (pr = parseSubTitle())) {
                            if (chinese) {
                                ChineseHelper.toSimplifiedChinese(tmpBB);
                            }
                            if (DEBUG && TRACE) {
                                System.out.println(">标题（" + pr + "）：" + ArrayHelper.toString(tmpBB));
                            }
                            if (null != (tmp = parseSourceLanguage(pr))) {
                                if (DEBUG) {
                                    System.out.println(">语言：" + ArrayHelper.toString(tmp) + ", "
                                            + ArrayHelper.toString(lineBB));

                                }
                                clearAttributes();
                                this.sourceLng = tmp;
                            }
                        }
                    }
                }
            }
        }
        // write last definition
        writeDefinition();
        cleanup();
        return statOk;
    }

    private byte[] parseSourceLanguage(ParserResult pr) {
        byte[] result;
        switch (pr) {
        case VALUE:
            return languageNames.findKey(tmpBB);
        case KEY:
            result = languageNames.containsKey(tmpBB);
            if (result == null) {
                result = languageAlternatives.findKey(tmpBB);
            }
            return result;
        case ABBR:
            result = languageNames.findKey(tmpBB);
            if (result == null) {
                System.err.println(ArrayHelper.toString(tmpBB));
            }
            return result;
        default:
            return null;
        }
    }

    /**
     * 
     * @return -1: not found, 1: tag, 2: lng, result saved in tmpBB
     */
    private ParserResult parseSubTitle() {
        if (len > 8 && len < 50) {
            final byte b0 = lineArray[0];
            final byte b1 = lineArray[1];
            final byte b2 = lineArray[2];
            final byte b3 = lineArray[3];
            final byte b4 = lineArray[4];
            final byte b5 = lineArray[5];

            byte b;
            // approx. length of source language line
            if (b0 == '=' && b1 == '=') {
                if (b2 == '[' && b3 == '[') {
                    // ==[[德语]]==
                    for (int i = 6; i < len; i++) {
                        b = lineArray[i];
                        if (b == ']' || b == '|') {
                            System.arraycopy(lineArray, 4, tmpArray, 0, i - 4);
                            tmpBB.limit(i - 4);
                            return ParserResult.VALUE;
                        }
                    }
                } else if (b2 != '=') {
                    // ==德语==
                    for (int i = 4; i < len; i++) {
                        b = lineArray[i];
                        if (b == '=' || b == '[') {
                            System.arraycopy(lineArray, 2, tmpArray, 0, i - 2);
                            tmpBB.limit(i - 2);
                            return ParserResult.VALUE;
                        }
                    }
                }
            } else if (b0 == '{' && b1 == '{') {
                if (b2 == '-') {
                    // {{-jbo-|cmauo}}, {{-mnc-}}, {{-fra-}}, {{-eng-|}}
                    for (int i = 4; i < len; i++) {
                        b = lineArray[i];
                        if (b == '-' || b == '}') {
                            System.arraycopy(lineArray, 3, tmpArray, 0, i - 3);
                            tmpBB.limit(i - 3);
                            return ParserResult.KEY;
                        }
                    }
                } else if (b5 == '|' && b4 == '=' && b2 == '=') {
                    // {{=n=|英|mail}}
                    for (int i = 7; i < len; i++) {
                        b = lineArray[i];
                        if (b == '|' || b == '}') {
                            System.arraycopy(lineArray, 6, tmpArray, 0, i - 6);
                            tmpBB.limit(i - 6);
                            return ParserResult.ABBR;
                        }
                    }
                }
            }
        }
        return ParserResult.NO_RESULT;
    }

    @Override
    protected void clearAttributes() {
        super.clearAttributes();
        sourceLng = null;
        targetLng = null;
        sourceWordType = null;
        sourceGender = null;
        targetWordType = null;
        targetGender = null;
    }

    @Override
    protected void initialize(final String f, final String outDir, final String outPrefix,
            final String outPrefixCategories, final String outPrefixRelated) throws IOException {
        super.initialize(f, outDir, outPrefix, outPrefixCategories, outPrefixRelated);
        this.languageNames = LanguageConstants.getLanguageNamesBytes(Language.valueOf(Helper.toConstantName(fileLng)));
    }

    @Override
    protected void writeDefinition() throws IOException {
        if (DEBUG) {
            if (isValid()) {
                String n = ArrayHelper.toString(name);
                if (!ChineseHelper.containsChinese(n) && sourceLng == null) {
                    System.out.println("=> 没找到源语言：" + n);
                }
            }
        }
    }

    public static enum ParserResult {
        NO_RESULT, VALUE, KEY, ABBR
    }
}
