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

public class WiktionaryPagesMetaCurrentChineseExtractor extends WikiExtractorBase {

  private static final byte[]         LNG_FCT_KEY_BYTES     = "{{-".getBytes(Helper.CHARSET_UTF8);

  // TODO: http://zh.wiktionary.org/w/index.php?title=apa&action=edit&section=6
  public static final String          IN_DIR                = Configuration.IMPORTER_FOLDER_SELECTED_DICTS.getPath(Source.DICT_WIKTIONARY);

  public static final String          OUT_DIR               = Configuration.IMPORTER_FOLDER_EXTRACTED_DICTS.getPath(Source.DICT_WIKTIONARY);

  public static final String          OUT_DIR_FINISHED      = WiktionaryPagesMetaCurrentChineseExtractor.OUT_DIR + "/finished";

  public static final Language[]      RELEVANT_LANGUAGES    = { Language.EN, Language.RU, Language.PL, Language.JA, Language.KO, Language.ZH, Language.DE,
      Language.FR, Language.IT, Language.ES, Language.PT, Language.NL, Language.SV, Language.UK, Language.VI, Language.CA, Language.NO, Language.FI,
      Language.CS, Language.HU, Language.ID, Language.TR, Language.RO, Language.FA, Language.AR, Language.DA, Language.EO, Language.SR, Language.LT,
      Language.SK, Language.SL, Language.MS, Language.HE, Language.BG, Language.KK, Language.EU, Language.VO, Language.WAR, Language.HR, Language.HI,
      Language.LA                                          };

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

  public static final byte[]          KEY_TRANSLATION_BYTES = "翻译".getBytes(Helper.CHARSET_UTF8);

  byte[]                              sourceLng;
  byte[]                              targetLng;
  List<WordType>                      sourceWordTypes       = new ArrayList<WordType>();
  Gender                              sourceGender;
  List<WordType>                      targetWordTypes       = new ArrayList<WordType>();
  Gender                              targetGender;
  ByteArrayPairs                      languageNames;
  ByteArrayPairs                      categoriesNames;

  private final static ByteArrayPairs languageAlternatives  = LanguageConstants.createByteArrayPairs(LanguageConstants.getLngProperties("lng2alt.txt"));

  public static void main(final String args[]) throws IOException {
    final String LNG = "zh";
    final WiktionaryPagesMetaCurrentChineseExtractor extractor = new WiktionaryPagesMetaCurrentChineseExtractor();
    ArrayHelper.WARN = false;
    final String f = WiktionaryPagesMetaCurrentChineseExtractor.IN_DIR + File.separator + LNG + "wiktionary-latest-pages-meta-current.xml.bz2";
    extractor.extractWiktionaryPagesMetaCurrent(f);
    final File file = new File(f);
    file.renameTo(new File(WiktionaryPagesMetaCurrentChineseExtractor.OUT_DIR_FINISHED, file.getName()));
  }

  private int extractWiktionaryPagesMetaCurrent(final String f) throws FileNotFoundException, IOException {
    this.initialize(f, WiktionaryPagesMetaCurrentChineseExtractor.OUT_DIR, "output-dict.wikt_", null, null, null, "output-dict_redirects.wikt_", null, null,
        "output-dict_src.wikt_", null);
    ParserResult pr;
    byte[] tmp;
    while (-1 != (this.lineLen = ArrayHelper.readLineTrimmed(this.in, this.lineBB))) {
      if ((++this.lineCount % WikiExtractorBase.OK_NOTICE) == 0) {
        this.signal();
      }
      if (WikiParseStep.HEADER == this.step) {
        this.parseDocumentHeader();
      } else if (WikiParseStep.BEFORE_TITLE == this.step) {
        if ((this.lineLen > WikiExtractorBase.MIN_TITLE_LINE_BYTES)
            && (ArrayHelper.substringBetween(this.lineArray, 0, this.lineLen, WikiExtractorBase.PREFIX_TITLE_BYTES, WikiExtractorBase.SUFFIX_TITLE_BYTES,
                this.tmpBB) > 0)) {
          // new title found
          // write old definition
          this.writeDefinition();
          this.handleContentTitle();
        }
      } else if (this.step == WikiParseStep.TITLE_FOUND) {
        int idx;
        if (-1 != (idx = ArrayHelper.indexOf(this.lineArray, 0, this.lineLen, WikiExtractorBase.TAG_TEXT_BEGIN_BYTES))) {
          this.handleTextBeginLine(idx);
          if ((this.step == WikiParseStep.PAGE) && (this.lineLen > 14) && (this.lineArray[0] == '#')) {
            this.checkRedirectLine();
          }
        } else if ((this.lineLen > WikiExtractorBase.MIN_REDIRECT_LINE_BYTES)
            && (ArrayHelper.substringBetween(this.lineArray, 0, this.lineLen, WikiExtractorBase.TAG_REDIRECT_BEGIN_BYTES,
                WikiExtractorBase.SUFFIX_REDIRECT_BYTES, this.tmpBB) > 0)) {
          this.writeRedirectLine();
        }
      }
      if (this.step == WikiParseStep.PAGE) {
        // System.out.println(ArrayHelper.toString(lineBB));
        int idx;
        if (-1 != (idx = ArrayHelper.indexOf(this.lineArray, 0, this.lineLen, WikiExtractorBase.TAG_TEXT_END_BYTES))) {
          this.handleTextEndLine(idx);
        }
        // within content
        if ((this.lineLen > this.minCatBytes)
            && ((ArrayHelper.substringBetween(this.lineArray, 0, this.lineLen, this.catKeyBytes, WikiExtractorBase.SUFFIX_WIKI_TAG_BYTES, this.tmpBB) > 0) || (ArrayHelper
                .substringBetween(this.lineArray, 0, this.lineLen, this.catKeyBytes2, WikiExtractorBase.SUFFIX_WIKI_TAG_BYTES, this.tmpBB) > 0))) {
          // new category found for current name
          this.addCategory();
        } else if (ParserResult.NO_RESULT != (pr = this.parseSubTitle())) {
          if (this.chinese) {
            ChineseHelper.toSimplifiedChinese(this.tmpBB);
          }
          if (WikiExtractorBase.DEBUG && WikiExtractorBase.TRACE) {
            System.out.println(ArrayHelper.toString(this.nameBB) + "，标题（" + pr + "）：" + ArrayHelper.toString(this.tmpBB));
          }
          if (null != (tmp = this.parseSourceLanguage(pr))) {
            if (WikiExtractorBase.DEBUG) {
              System.out.println(ArrayHelper.toString(this.nameBB) + "，语言：" + ArrayHelper.toString(tmp) + ", " + ArrayHelper.toString(this.lineBB));

            }
            this.clearAttributes();
            this.sourceLng = tmp;
          }
        }
        // if (parseAbstract) {
        // parseAbstract();
        // }
      }
    }
    // write last definition
    this.writeDefinition();
    this.cleanup();
    return this.statOk;
  }

  private byte[] parseSourceLanguage(final ParserResult pr) {
    byte[] result;
    switch (pr) {
      case VALUE:
        return this.languageNames.findKey(this.tmpBB);
      case KEY:
        // System.out.println(ArrayHelper.toString(tmpBB));
        result = this.languageNames.containsKey(this.tmpBB);
        if (result == null) {
          result = WiktionaryPagesMetaCurrentChineseExtractor.languageAlternatives.findKey(this.tmpBB);
        }
        return result;
      case ABBR:
        result = this.languageNames.findKey(this.tmpBB);
        if (result == null) {
          // System.err.println(ArrayHelper.toString(this.tmpBB));
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
    if ((this.lineLen > 7) && (this.lineLen < 50)) {
      final byte b0 = this.lineArray[0];
      final byte b1 = this.lineArray[1];
      final byte b2 = this.lineArray[2];
      final byte b3 = this.lineArray[3];
      final byte b4 = this.lineArray[4];
      final byte b5 = this.lineArray[5];

      byte b;
      int idx;
      // approx. length of source language line
      if ((b0 == '=') && (b1 == '=')) {
        if ((b2 == '[') && (b3 == '[')) {
          // ==[[德语]]==
          for (int i = 6; i < this.lineLen; i++) {
            b = this.lineArray[i];
            if ((b == ']') || (b == '|')) {
              System.arraycopy(this.lineArray, 4, this.tmpArray, 0, i - 4);
              this.tmpBB.limit(i - 4);
              return ParserResult.VALUE;
            }
          }
        } else if (b2 != '=') {
          // ==德语==
          for (int i = 4; i < this.lineLen; i++) {
            b = this.lineArray[i];
            if ((b == '=') || (b == '[')) {
              System.arraycopy(this.lineArray, 2, this.tmpArray, 0, i - 2);
              this.tmpBB.limit(i - 2);
              return ParserResult.VALUE;
            }
          }
        }
      } else if ((b0 == '{') && (b1 == '{')) {
        if (b2 == '-') {
          // {{-jbo-|cmauo}}, {{-mnc-}}, {{-fra-}}, {{-eng-|}}
          for (int i = 4; i < this.lineLen; i++) {
            b = this.lineArray[i];
            if ((b == '-') || (b == '}')) {
              System.arraycopy(this.lineArray, 3, this.tmpArray, 0, i - 3);
              this.tmpBB.limit(i - 3);
              return ParserResult.KEY;
            }
          }
        } else if ((b5 == '|') && (b4 == '=') && (b2 == '=')) {
          // {{=n=|英|mail}}
          for (int i = 7; i < this.lineLen; i++) {
            b = this.lineArray[i];
            if ((b == '|') || (b == '}')) {
              System.arraycopy(this.lineArray, 6, this.tmpArray, 0, i - 6);
              this.tmpBB.limit(i - 6);
              return ParserResult.ABBR;
            }
          }
        } else if (-1 != (idx = ArrayHelper.indexOf(this.lineArray, 5, this.lineLen - 5, WiktionaryPagesMetaCurrentChineseExtractor.LNG_FCT_KEY_BYTES))) {
          // {{also|-faction}}{{-en-}}
          final int offset = idx + WiktionaryPagesMetaCurrentChineseExtractor.LNG_FCT_KEY_BYTES.length;
          for (int i = offset; i < this.lineLen; i++) {
            b = this.lineArray[i];
            if ((b == '-') || (b == '}')) {
              System.arraycopy(this.lineArray, offset, this.tmpArray, 0, i - offset);
              this.tmpBB.limit(i - offset);
              return ParserResult.KEY;
            }
          }
        }
        /*
         * else { // {{en-noun}} for (int i = 3; i < 6; i++) { b = lineArray[i]; if (b == '-' || b == '}') { System.arraycopy(lineArray, 2, tmpArray, 0, i - 2);
         * tmpBB.limit(i - 2); return ParserResult.KEY; } } }
         */
      } else if (b0 == '=') {
        if ((b1 == '[') && (b2 == '[')) {
          // =[[德语]]=
          for (int i = 5; i < this.lineLen; i++) {
            b = this.lineArray[i];
            if ((b == ']') || (b == '|')) {
              System.arraycopy(this.lineArray, 3, this.tmpArray, 0, i - 3);
              this.tmpBB.limit(i - 3);
              return ParserResult.VALUE;
            }
          }
        } else {
          if ((b1 != '[') && (b1 != '{')) {
            // =德语=
            for (int i = 3; i < this.lineLen; i++) {
              b = this.lineArray[i];
              if ((b == '=') || (b == '[')) {
                System.arraycopy(this.lineArray, 1, this.tmpArray, 0, i - 1);
                this.tmpBB.limit(i - 1);
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
    this.sourceLng = null;
    this.targetLng = null;
    this.sourceWordTypes.clear();
    this.sourceGender = null;
    this.targetWordTypes.clear();
    this.targetGender = null;
  }

  @Override
  protected void initialize(final String f, final String outDir, final String outPrefix, final String outPrefixCategories, final String outPrefixRelated,
      final String outPrefixAbstracts, final String outPrefixRedirects, final String outPrefixImages, final String outPrefixCoordinates,
      final String outPrefixSource, final String outPrefixAttributes) throws IOException {
    super.initialize(f, outDir, outPrefix, outPrefixCategories, outPrefixRelated, outPrefixAbstracts, outPrefixRedirects, outPrefixImages,
        outPrefixCoordinates, outPrefixSource, outPrefixAttributes);
    this.languageNames = LanguageConstants.getLanguageNamesBytes(Language.fromKey(this.fileLng));
    this.categoriesNames = LanguageConstants.createByteArrayPairs(LanguageConstants.getLngProperties("cat2lng_" + this.fileLng + ".txt"));
  }

  //
  // @Override
  // protected void writeDefinition() throws IOException {
  // if (this.sourceLng == null) {
  // // read source lng from category
  // byte[] tmp;
  // for (final byte[] c : this.categories) {
  // if ((tmp = this.categoriesNames.findKey(c)) != null) {
  // if (this.sourceLng == null) {
  // this.sourceLng = tmp;
  // } else if (!Arrays.equals(this.sourceLng, tmp)) {
  // System.err.println("找到多种语言：" + ArrayHelper.toString(this.nameBB));
  // this.sourceLng = null;
  // break;
  // }
  // }
  // }
  // }
  // // if (DEBUG) {
  // if (this.isValid()) {
  // final String n = ArrayHelper.toString(this.nameBB);
  // if (!ChineseHelper.containsChinese(n) && (this.sourceLng == null)) {
  // // System.err.println("=> 没找到源语言：" + n);
  // }
  // }
  // // }
  // }

  public static enum ParserResult {
    NO_RESULT,
    VALUE,
    KEY,
    ABBR
  }
}
