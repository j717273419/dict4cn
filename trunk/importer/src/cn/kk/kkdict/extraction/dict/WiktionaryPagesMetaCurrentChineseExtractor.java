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
package cn.kk.kkdict.extraction.dict;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.kk.kkdict.Configuration;
import cn.kk.kkdict.Configuration.Source;
import cn.kk.kkdict.beans.ByteArrayPairs;
import cn.kk.kkdict.beans.WikiParseStep;
import cn.kk.kkdict.types.Gender;
import cn.kk.kkdict.types.Language;
import cn.kk.kkdict.types.LanguageConstants;
import cn.kk.kkdict.types.WordType;
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

    private static final byte[] LNG_FCT_KEY_BYTES = "{{-".getBytes(Helper.CHARSET_UTF8);

    // TODO: http://zh.wiktionary.org/w/index.php?title=apa&action=edit&section=6
    public static final String IN_DIR = Configuration.IMPORTER_FOLDER_SELECTED_DICTS.getPath(Source.DICT_WIKTIONARY);

    public static final String OUT_DIR = Configuration.IMPORTER_FOLDER_EXTRACTED_DICTS.getPath(Source.DICT_WIKTIONARY);

    public static final Language[] RELEVANT_LANGUAGES = { Language.EN, Language.RU, Language.PL, Language.JA,
            Language.KO, Language.ZH, Language.DE, Language.FR, Language.IT, Language.ES, Language.PT, Language.NL,
            Language.SV, Language.UK, Language.VI, Language.CA, Language.NO, Language.FI, Language.CS, Language.HU,
            Language.ID, Language.TR, Language.RO, Language.FA, Language.AR, Language.DA, Language.EO, Language.SR,
            Language.LT, Language.SK, Language.SL, Language.MS, Language.HE, Language.BG, Language.KK, Language.EU,
            Language.VO, Language.WAR, Language.HR, Language.HI, Language.LA };

    // extended:
    // Language.BR, Language.LI, Language.LB, Language.HSB, Language.MG, Language.CSB, Language.AST, Language.GL,
    // Language.LV, Language.BS, Language.IO, Language.BE, Language.CY, Language.EL, Language.KL, Language.ET,
    // Language.NAH, Language.GU, Language.AF, Language.GA, Language.FJ, Language.JV, Language.IS, Language.UR,
    // Language.OC, Language.WA, Language.KA, Language.AZ, Language.UZ, Language.FY, Language.SO, Language.TG,
    // Language.ML, Language.LN, Language.TH, Language.SI, Language.KW, Language.ZH_MIN_NAN, Language.CHR, Language.TI,
    // Language.SCN, Language.FO, Language.ZA, Language.SW, Language.NDS, Language.WO, Language.ROA_RUP, Language.SU,
    // Language.LO, Language.MN, Language.AN, Language.AY, Language.MI, Language.TPI, Language.KN, Language.KM,
    // Language.IU, Language.ANG, Language.TL, Language.MY, Language.TE, Language.TA, Language.SH, Language.ZU,
    // Language.TK, Language.UG, Language.KU, Language.OM, Language.NA, Language.CO, Language.KY, Language.SS,
    // Language.GV, Language.SA, Language.SM, Language.MT, Language.SQ, Language.IA, Language.HY, Language.TT,
    // Language.YI, Language.MK, Language.RW, Language.QU

    public static final byte[] KEY_TRANSLATION_BYTES = "翻译".getBytes(Helper.CHARSET_UTF8);

    byte[] sourceLng;
    byte[] targetLng;
    List<WordType> sourceWordTypes = new ArrayList<WordType>();
    Gender sourceGender;
    List<WordType> targetWordTypes = new ArrayList<WordType>();
    Gender targetGender;
    ByteArrayPairs languageNames;
    ByteArrayPairs categoriesNames;

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
        initialize(f, OUT_DIR, "output-dict.wikt_", null, null, null, "output-dict_redirects.wikt_", null, null, "output-dict_src.wikt_", null);
        ParserResult pr;
        byte[] tmp;
        while (-1 != (lineLen = ArrayHelper.readLineTrimmed(in, lineBB))) {
            if (++lineCount % OK_NOTICE == 0) {
                signal();
            }
            if (WikiParseStep.HEADER == step) {
                parseDocumentHeader();
            } else if (WikiParseStep.BEFORE_TITLE == step) {
                if (lineLen > MIN_TITLE_LINE_BYTES
                        && ArrayHelper.substringBetween(lineArray, 0, lineLen, PREFIX_TITLE_BYTES, SUFFIX_TITLE_BYTES,
                                tmpBB) > 0) {
                    // new title found
                    // write old definition
                    writeDefinition();
                    handleContentTitle();
                }
            } else if (step == WikiParseStep.TITLE_FOUND) {
                int idx;
                if (-1 != (idx = ArrayHelper.indexOf(lineArray, 0, lineLen, TAG_TEXT_BEGIN_BYTES))) {
                    handleTextBeginLine(idx);
                    if (step == WikiParseStep.PAGE && lineLen > 14 && lineArray[0] == '#') {
                        checkRedirectLine();
                    }
                } else if (lineLen > MIN_REDIRECT_LINE_BYTES
                        && ArrayHelper.substringBetween(lineArray, 0, lineLen, TAG_REDIRECT_BEGIN_BYTES,
                                SUFFIX_REDIRECT_BYTES, tmpBB) > 0) {
                    writeRedirectLine();
                }
            }
            if (step == WikiParseStep.PAGE) {
                // System.out.println(ArrayHelper.toString(lineBB));
                int idx;
                if (-1 != (idx = ArrayHelper.indexOf(lineArray, 0, lineLen, TAG_TEXT_END_BYTES))) {
                    handleTextEndLine(idx);
                }
                // within content
                if (lineLen > minCatBytes
                        && (ArrayHelper.substringBetween(lineArray, 0, lineLen, catKeyBytes, SUFFIX_WIKI_TAG_BYTES,
                                tmpBB) > 0 || ArrayHelper.substringBetween(lineArray, 0, lineLen, catKeyBytes2,
                                SUFFIX_WIKI_TAG_BYTES, tmpBB) > 0)) {
                    // new category found for current name
                    addCategory();
                } else if (ParserResult.NO_RESULT != (pr = parseSubTitle())) {
                    if (chinese) {
                        ChineseHelper.toSimplifiedChinese(tmpBB);
                    }
                    if (DEBUG && TRACE) {
                        System.out.println(ArrayHelper.toString(nameBB)+"，标题（" + pr + "）：" + ArrayHelper.toString(tmpBB));
                    }
                    if (null != (tmp = parseSourceLanguage(pr))) {
                        if (DEBUG) {
                            System.out
                                    .println(ArrayHelper.toString(nameBB)+"，语言：" + ArrayHelper.toString(tmp) + ", " + ArrayHelper.toString(lineBB));

                        }
                        clearAttributes();
                        this.sourceLng = tmp;
                    }
                }
                // if (parseAbstract) {
                // parseAbstract();
                // }
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
            // System.out.println(ArrayHelper.toString(tmpBB));
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
        if (lineLen > 7 && lineLen < 50) {
            final byte b0 = lineArray[0];
            final byte b1 = lineArray[1];
            final byte b2 = lineArray[2];
            final byte b3 = lineArray[3];
            final byte b4 = lineArray[4];
            final byte b5 = lineArray[5];

            byte b;
            int idx;
            // approx. length of source language line
            if (b0 == '=' && b1 == '=') {
                if (b2 == '[' && b3 == '[') {
                    // ==[[德语]]==
                    for (int i = 6; i < lineLen; i++) {
                        b = lineArray[i];
                        if (b == ']' || b == '|') {
                            System.arraycopy(lineArray, 4, tmpArray, 0, i - 4);
                            tmpBB.limit(i - 4);
                            return ParserResult.VALUE;
                        }
                    }
                } else if (b2 != '=') {
                    // ==德语==
                    for (int i = 4; i < lineLen; i++) {
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
                    for (int i = 4; i < lineLen; i++) {
                        b = lineArray[i];
                        if (b == '-' || b == '}') {
                            System.arraycopy(lineArray, 3, tmpArray, 0, i - 3);
                            tmpBB.limit(i - 3);
                            return ParserResult.KEY;
                        }
                    }
                } else if (b5 == '|' && b4 == '=' && b2 == '=') {
                    // {{=n=|英|mail}}
                    for (int i = 7; i < lineLen; i++) {
                        b = lineArray[i];
                        if (b == '|' || b == '}') {
                            System.arraycopy(lineArray, 6, tmpArray, 0, i - 6);
                            tmpBB.limit(i - 6);
                            return ParserResult.ABBR;
                        }
                    }
                } else if (-1 != (idx = ArrayHelper.indexOf(lineArray, 5, lineLen - 5, LNG_FCT_KEY_BYTES))) {
                    // {{also|-faction}}{{-en-}}
                    final int offset = idx + LNG_FCT_KEY_BYTES.length;
                    for (int i = offset; i < lineLen; i++) {
                        b = lineArray[i];
                        if (b == '-' || b == '}') {
                            System.arraycopy(lineArray, offset, tmpArray, 0, i - offset);
                            tmpBB.limit(i - offset);
                            return ParserResult.KEY;
                        }
                    }
                }
                /*
                 * else { // {{en-noun}} for (int i = 3; i < 6; i++) { b = lineArray[i]; if (b == '-' || b == '}') {
                 * System.arraycopy(lineArray, 2, tmpArray, 0, i - 2); tmpBB.limit(i - 2); return ParserResult.KEY; } }
                 * }
                 */
            } else if (b0 == '=') {
                if (b1 == '[' && b2 == '[') {
                    // =[[德语]]=
                    for (int i = 5; i < lineLen; i++) {
                        b = lineArray[i];
                        if (b == ']' || b == '|') {
                            System.arraycopy(lineArray, 3, tmpArray, 0, i - 3);
                            tmpBB.limit(i - 3);
                            return ParserResult.VALUE;
                        }
                    }
                } else {
                    if (b1 != '[' && b1 != '{') {
                        // =德语=
                        for (int i = 3; i < lineLen; i++) {
                            b = lineArray[i];
                            if (b == '=' || b == '[') {
                                System.arraycopy(lineArray, 1, tmpArray, 0, i - 1);
                                tmpBB.limit(i - 1);
                                return ParserResult.VALUE;
                            }
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
        sourceWordTypes.clear();
        sourceGender = null;
        targetWordTypes.clear();
        targetGender = null;
    }

    @Override
    protected void initialize(final String f, final String outDir, final String outPrefix,
            final String outPrefixCategories, final String outPrefixRelated, final String outPrefixAbstracts,
            final String outPrefixRedirects, final String outPrefixImages, final String outPrefixCoordinates, final String outPrefixSource,
            final String outPrefixAttributes)
            throws IOException {
        super.initialize(f, outDir, outPrefix, outPrefixCategories, outPrefixRelated, outPrefixAbstracts,
                outPrefixRedirects, outPrefixImages, outPrefixCoordinates, outPrefixSource, outPrefixAttributes);
        final String lngName = Helper.toConstantName(fileLng);
        this.languageNames = LanguageConstants.getLanguageNamesBytes(Language.valueOf(lngName));
        categoriesNames = LanguageConstants.createByteArrayPairs(LanguageConstants.getLngProperties("cat2lng_"
                + lngName + ".txt"));
    }

    @Override
    protected void writeDefinition() throws IOException {
        if (sourceLng == null) {
            // read source lng from category
            byte[] tmp;
            for (byte[] c : categories) {
                if ((tmp = categoriesNames.findKey(c)) != null) {
                    if (sourceLng == null) {
                        sourceLng = tmp;
                    } else if (!Arrays.equals(sourceLng, tmp)) {
                        System.err.println("找到多种语言：" + ArrayHelper.toString(nameBB));
                        sourceLng = null;
                        break;
                    }
                }
            }
        }
        // if (DEBUG) {
        if (isValid()) {
            String n = ArrayHelper.toString(nameBB);
            if (!ChineseHelper.containsChinese(n) && sourceLng == null) {
                System.err.println("=> 没找到源语言：" + n);
            }
        }
        // }
    }

    public static enum ParserResult {
        NO_RESULT, VALUE, KEY, ABBR
    }
}
