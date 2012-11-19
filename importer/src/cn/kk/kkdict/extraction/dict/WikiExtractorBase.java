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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.tools.bzip2.CBZip2InputStream;

import cn.kk.kkdict.beans.WikiParseStep;
import cn.kk.kkdict.types.Abstract;
import cn.kk.kkdict.types.Category;
import cn.kk.kkdict.types.GeoLocation;
import cn.kk.kkdict.types.ImageLocation;
import cn.kk.kkdict.types.Language;
import cn.kk.kkdict.types.LanguageConstants;
import cn.kk.kkdict.types.Redirect;
import cn.kk.kkdict.types.TranslationSource;
import cn.kk.kkdict.types.WordType;
import cn.kk.kkdict.utils.ArrayHelper;
import cn.kk.kkdict.utils.ChineseHelper;
import cn.kk.kkdict.utils.DictHelper;
import cn.kk.kkdict.utils.Helper;

/**
 * TODO - Redirects: <redirect title="?енгиз Лакербаиа" /> - Abstracts: until first empty line or starting ==title==. remove ''', {}, [], [|(text)]
 * 
 */
public class WikiExtractorBase {
  private static final byte[][] IMAGE_SUFFIX_BYTES_UPPER = { ".jpg".toUpperCase().getBytes(Helper.CHARSET_UTF8),
      ".jpeg".toUpperCase().getBytes(Helper.CHARSET_UTF8), ".png".toUpperCase().getBytes(Helper.CHARSET_UTF8),
      ".gif".toUpperCase().getBytes(Helper.CHARSET_UTF8), ".svg".toUpperCase().getBytes(Helper.CHARSET_UTF8),
      ".bmp".toUpperCase().getBytes(Helper.CHARSET_UTF8), ".tif".toUpperCase().getBytes(Helper.CHARSET_UTF8) };

  private static final byte[][] IMAGE_SUFFIX_BYTES_LOWER = { ".jpg".getBytes(Helper.CHARSET_UTF8), ".jpeg".getBytes(Helper.CHARSET_UTF8),
      ".png".getBytes(Helper.CHARSET_UTF8), ".gif".getBytes(Helper.CHARSET_UTF8), ".svg".getBytes(Helper.CHARSET_UTF8), ".bmp".getBytes(Helper.CHARSET_UTF8),
      ".tif".getBytes(Helper.CHARSET_UTF8)              };

  private static final byte[][] COORD_TAG_BYTES_LOWER    = { "{{coor".getBytes(Helper.CHARSET_UTF8), "{{geolinks".getBytes(Helper.CHARSET_UTF8),
      "{{mapit".getBytes(Helper.CHARSET_UTF8), "{{koordinate".getBytes(Helper.CHARSET_UTF8), "{{좌표".getBytes(Helper.CHARSET_UTF8),
      "{{location".getBytes(Helper.CHARSET_UTF8)        };

  private static final byte[][] COORD_TAG_BYTES_UPPER    = { "{{coor".toUpperCase().getBytes(Helper.CHARSET_UTF8),
      "{{geolinks".toUpperCase().getBytes(Helper.CHARSET_UTF8), "{{mapit".toUpperCase().getBytes(Helper.CHARSET_UTF8),
      "{{koordinate".toUpperCase().getBytes(Helper.CHARSET_UTF8), "{{좌표".toUpperCase().getBytes(Helper.CHARSET_UTF8),
      "{{location".toUpperCase().getBytes(Helper.CHARSET_UTF8) };

  private static final byte[][] INFOBOX_GEO_BYTES        = { "lat_deg".getBytes(Helper.CHARSET_UTF8), "lat_min".getBytes(Helper.CHARSET_UTF8),
      "lat_sec".getBytes(Helper.CHARSET_UTF8), "lat_NS".getBytes(Helper.CHARSET_UTF8), "lon_deg".getBytes(Helper.CHARSET_UTF8),
      "lon_min".getBytes(Helper.CHARSET_UTF8), "lon_sec".getBytes(Helper.CHARSET_UTF8), "long_EW".getBytes(Helper.CHARSET_UTF8),
      "lat_d".getBytes(Helper.CHARSET_UTF8), "lat_m".getBytes(Helper.CHARSET_UTF8), "lat_s".getBytes(Helper.CHARSET_UTF8),
      "lat_NS".getBytes(Helper.CHARSET_UTF8), "long_d".getBytes(Helper.CHARSET_UTF8), "long_m".getBytes(Helper.CHARSET_UTF8),
      "long_s".getBytes(Helper.CHARSET_UTF8), "lon_EW".getBytes(Helper.CHARSET_UTF8), "lat-deg".getBytes(Helper.CHARSET_UTF8),
      "lat-min".getBytes(Helper.CHARSET_UTF8), "lat-sec".getBytes(Helper.CHARSET_UTF8), "lat".getBytes(Helper.CHARSET_UTF8),
      "long".getBytes(Helper.CHARSET_UTF8), "lon-deg".getBytes(Helper.CHARSET_UTF8), "lon-min".getBytes(Helper.CHARSET_UTF8),
      "lon-sec".getBytes(Helper.CHARSET_UTF8), "lon".getBytes(Helper.CHARSET_UTF8), "latd".getBytes(Helper.CHARSET_UTF8), "latm".getBytes(Helper.CHARSET_UTF8),
      "lats".getBytes(Helper.CHARSET_UTF8), "latNS".getBytes(Helper.CHARSET_UTF8), "longd".getBytes(Helper.CHARSET_UTF8),
      "longm".getBytes(Helper.CHARSET_UTF8), "longs".getBytes(Helper.CHARSET_UTF8), "longEW".getBytes(Helper.CHARSET_UTF8),
      "lat_hem".getBytes(Helper.CHARSET_UTF8), "lon_d".getBytes(Helper.CHARSET_UTF8), "lon_m".getBytes(Helper.CHARSET_UTF8),
      "lon_s".getBytes(Helper.CHARSET_UTF8), "lon_hem".getBytes(Helper.CHARSET_UTF8), "lat_degrees".getBytes(Helper.CHARSET_UTF8),
      "lat_minutes".getBytes(Helper.CHARSET_UTF8), "lat_seconds".getBytes(Helper.CHARSET_UTF8), "lat_direction".getBytes(Helper.CHARSET_UTF8),
      "long_degrees".getBytes(Helper.CHARSET_UTF8), "long_minutes".getBytes(Helper.CHARSET_UTF8), "long_seconds".getBytes(Helper.CHARSET_UTF8),
      "long_direction".getBytes(Helper.CHARSET_UTF8), "Koordinate_Breitengrad".getBytes(Helper.CHARSET_UTF8),
      "Koordinate_Breitenminute".getBytes(Helper.CHARSET_UTF8), "Koordinate_Breitensekunde".getBytes(Helper.CHARSET_UTF8),
      "Koordinate_Breite".getBytes(Helper.CHARSET_UTF8), "Koordinate_Längengrad".getBytes(Helper.CHARSET_UTF8),
      "Koordinate_Längenminute".getBytes(Helper.CHARSET_UTF8), "Koordinate_Längensekunde".getBytes(Helper.CHARSET_UTF8),
      "Koordinate_Länge".getBytes(Helper.CHARSET_UTF8), "N".getBytes(Helper.CHARSET_UTF8), "E".getBytes(Helper.CHARSET_UTF8),
      "LatDeg".getBytes(Helper.CHARSET_UTF8), "LatMin".getBytes(Helper.CHARSET_UTF8), "LatSec".getBytes(Helper.CHARSET_UTF8),
      "north coord".getBytes(Helper.CHARSET_UTF8), "west coord".getBytes(Helper.CHARSET_UTF8), "N".getBytes(Helper.CHARSET_UTF8),
      "W".getBytes(Helper.CHARSET_UTF8), "latitude".getBytes(Helper.CHARSET_UTF8), "longitude".getBytes(Helper.CHARSET_UTF8) };

  private static final byte[]   TAG_IMAGEMAP_NAME_BYTES  = "imagemap".getBytes(Helper.CHARSET_UTF8);

  private static final byte[][] OPEN_ENDS                = { { '<', '>' }, { '{', '}' }, { '[', ']' } };

  private static void writeWikiVariable(final byte[] array, final int start, final int stop, final int walls, final int[] wallsIdx, final ByteBuffer outBB) {
    final int start0 = start;
    final int end0 = wallsIdx[0];
    final int start1 = end0 + 1;
    final int end1;
    final int start2;
    final int end2;
    if (walls > 2) {
      end1 = wallsIdx[1];
      start2 = end1 + 1;
      end2 = wallsIdx[2];
    } else if (walls > 1) {
      end1 = wallsIdx[1];
      start2 = end1 + 1;
      end2 = stop;
    } else {
      end1 = stop;
      start2 = -1;
      end2 = -1;
    }

    boolean reversed = false;
    boolean details = true;
    final int[] skipIdx = new int[WikiExtractorBase.ABSTRACT_SIMPLE_VARS_BYTES_LOWER.length];
    byte b;
    CHECK_SIMPLE: for (int i = start0; i < end0; i++) {
      final int len = end0 - start0;
      b = array[i];
      for (int j = 0; j < skipIdx.length; j++) {
        final int idx = skipIdx[j];
        final byte[] varKeyLower = WikiExtractorBase.ABSTRACT_SIMPLE_VARS_BYTES_LOWER[j];
        final byte[] varKeyUpper = WikiExtractorBase.ABSTRACT_SIMPLE_VARS_BYTES_UPPER[j];
        if (varKeyLower.length <= len) {
          if ((b == varKeyLower[idx]) || (b == varKeyUpper[idx])) {
            if ((idx + 1) < varKeyLower.length) {
              skipIdx[j] = idx + 1;
            } else {
              details = false;
              break CHECK_SIMPLE;
            }
          } else {
            skipIdx[j] = 0;
          }
        }
      }
    }
    if (ArrayHelper.equals(array, start0, WikiExtractorBase.BYTES_WIKI_ATTR_LANG)) {
      if (walls > 1) {
        reversed = true;
      }
    }

    // main word
    if (!reversed) {
      ArrayHelper.write(array, start1, end1, outBB);
    } else {
      ArrayHelper.write(array, start2, end2, outBB);
    }
    if (details) {
      outBB.put((byte) ' ').put((byte) '(');
      // first word
      ArrayHelper.write(array, start0, end0, outBB);
      // further words
      if (walls > 1) {
        outBB.put((byte) ',').put((byte) ' ');
        if (!reversed) {
          ArrayHelper.write(array, start2, end2, outBB);
        } else {
          ArrayHelper.write(array, start1, end1, outBB);
        }
        // else if (wallsIdx.length > 2) {
        // outBB.put((byte) ',').put((byte) ' ');
        // write(array, wallsIdx[2] + 1, (wallsIdx.length > 3 ? wallsIdx[3] : stop),
        // outBB);
        // }
      }
      outBB.put((byte) ')');
    }
  }

  private int                         opened                           = 0;

  private int                         openIdx                          = 0;
  public static final byte[]          BYTES_WIKI_ANNOTATION_UNDERSCORE = { '_', '_' };

  public static final byte[]          BYTES_WIKI_ANNOTATION_MINUS      = { '-', '-', '-' };

  public static final byte[]          BYTES_WIKI_ATTR_LANG             = "lang".getBytes(Helper.CHARSET_UTF8);

  private static final byte[]         COMMA_BYTES                      = "，".getBytes(Helper.CHARSET_UTF8);

  private static final byte[]         POINT_BYTES                      = "。".getBytes(Helper.CHARSET_UTF8);

  private static final byte[]         REDIRECT_UPPER_BYTES             = "#redirect ".toUpperCase().getBytes(Helper.CHARSET_UTF8);

  private static final byte[]         REDIRECT_LOWER_BYTES             = "#redirect ".getBytes(Helper.CHARSET_UTF8);

  protected static final boolean      INFO                             = true;

  protected static final boolean      DEBUG                            = false;

  protected static final boolean      TRACE                            = false;

  static final byte[]                 TAG_TEXT_BEGIN_BYTES             = "space=\"preserve\">".getBytes(Helper.CHARSET_UTF8);

  static final byte[]                 TAG_TEXT_END_BYTES               = "</text>".getBytes(Helper.CHARSET_UTF8);

  static final byte[]                 TAG_REDIRECT_BEGIN_BYTES         = "<redirect title=\"".getBytes(Helper.CHARSET_UTF8);

  static final byte[]                 KEY_ZH_BYTES                     = Language.ZH.keyBytes;

  static final byte[]                 PREFIX_WIKI_TAG_BYTES            = "[[".getBytes(Helper.CHARSET_UTF8);

  static final byte[]                 SUFFIX_WIKI_TAG_BYTES            = "]]".getBytes(Helper.CHARSET_UTF8);

  protected static final byte[]       SUFFIX_TITLE_BYTES               = "</title>".getBytes(Helper.CHARSET_UTF8);
  protected static final byte[]       PREFIX_TITLE_BYTES               = "<title>".getBytes(Helper.CHARSET_UTF8);

  static final byte[]                 SUFFIX_REDIRECT_BYTES            = "\"".getBytes(Helper.CHARSET_UTF8);

  protected static final int          MIN_TITLE_LINE_BYTES             = WikiExtractorBase.SUFFIX_TITLE_BYTES.length
                                                                           + WikiExtractorBase.PREFIX_TITLE_BYTES.length;

  static final int                    MIN_REDIRECT_LINE_BYTES          = WikiExtractorBase.TAG_REDIRECT_BEGIN_BYTES.length
                                                                           + WikiExtractorBase.SUFFIX_REDIRECT_BYTES.length;

  protected static final byte[]       SUFFIX_XML_TAG_BYTES             = ">".getBytes(Helper.CHARSET_UTF8);

  static final byte[]                 ATTR_CATEGORY_KEY_BYTES          = "key=\"14\"".getBytes(Helper.CHARSET_UTF8);
  protected static final byte[]       SUFFIX_NAMESPACE_BYTES           = "</namespace>".getBytes(Helper.CHARSET_UTF8);

  protected static final byte[]       SUFFIX_NAMESPACES_BYTES          = "</namespaces>".getBytes(Helper.CHARSET_UTF8);
  static final byte[]                 PREFIX_CATEGORY_KEY_EN_BYTES     = "[[Category:".getBytes(Helper.CHARSET_UTF8);
  static final byte[]                 PREFIX_CATEGORY_KEY_EN2_BYTES    = "[[:Category:".getBytes(Helper.CHARSET_UTF8);
  static final byte[]                 CATEGORY_KEY_BYTES               = "Category:".getBytes(Helper.CHARSET_UTF8);

  static final byte[][]               ABSTRACT_SKIP_VARS_BYTES_LOWER   = { "infobox".getBytes(Helper.CHARSET_UTF8), "portal".getBytes(Helper.CHARSET_UTF8),
      "soft".getBytes(Helper.CHARSET_UTF8), "month".getBytes(Helper.CHARSET_UTF8), "day".getBytes(Helper.CHARSET_UTF8),
      "redirect".getBytes(Helper.CHARSET_UTF8), "not".getBytes(Helper.CHARSET_UTF8), "path".getBytes(Helper.CHARSET_UTF8),
      "dablink".getBytes(Helper.CHARSET_UTF8), "dis".getBytes(Helper.CHARSET_UTF8), "merge".getBytes(Helper.CHARSET_UTF8),
      "year".getBytes(Helper.CHARSET_UTF8), "century".getBytes(Helper.CHARSET_UTF8), "c20".getBytes(Helper.CHARSET_UTF8),
      "other".getBytes(Helper.CHARSET_UTF8), "audio".getBytes(Helper.CHARSET_UTF8) };

  static final byte[][]               ABSTRACT_SKIP_VARS_BYTES_UPPER   = { "infobox".toUpperCase().getBytes(Helper.CHARSET_UTF8),
      "portal".toUpperCase().getBytes(Helper.CHARSET_UTF8), "soft".toUpperCase().getBytes(Helper.CHARSET_UTF8),
      "month".toUpperCase().getBytes(Helper.CHARSET_UTF8), "day".toUpperCase().getBytes(Helper.CHARSET_UTF8),
      "redirect".toUpperCase().getBytes(Helper.CHARSET_UTF8), "not".toUpperCase().getBytes(Helper.CHARSET_UTF8),
      "path".toUpperCase().getBytes(Helper.CHARSET_UTF8), "dablink".toUpperCase().getBytes(Helper.CHARSET_UTF8),
      "dis".toUpperCase().getBytes(Helper.CHARSET_UTF8), "merge".toUpperCase().getBytes(Helper.CHARSET_UTF8),
      "year".toUpperCase().getBytes(Helper.CHARSET_UTF8), "century".toUpperCase().getBytes(Helper.CHARSET_UTF8),
      "c20".toUpperCase().getBytes(Helper.CHARSET_UTF8), "other".toUpperCase().getBytes(Helper.CHARSET_UTF8),
      "audio".toUpperCase().getBytes(Helper.CHARSET_UTF8)             };

  static final byte[][]               ABSTRACT_SIMPLE_VARS_BYTES_LOWER = { "lang".getBytes(Helper.CHARSET_UTF8), "notetag".getBytes(Helper.CHARSET_UTF8),
      "bd".getBytes(Helper.CHARSET_UTF8)                              };

  static final byte[][]               ABSTRACT_SIMPLE_VARS_BYTES_UPPER = { "lang".toUpperCase().getBytes(Helper.CHARSET_UTF8),
      "notetag".toUpperCase().getBytes(Helper.CHARSET_UTF8), "bd".toUpperCase().getBytes(Helper.CHARSET_UTF8) };

  protected boolean                   insideInfobox;
  protected static final byte[]       INFOBOX_BYTES_LOWER              = "infobox".getBytes(Helper.CHARSET_UTF8);
  protected static final byte[]       INFOBOX_BYTES_UPPER              = "infobox".getBytes(Helper.CHARSET_UTF8);

  protected static final int          OK_NOTICE                        = 100000;
  public String                       inFile;
  public String                       outFile;
  public String                       outFileSource;
  public String                       outFileAttributes;
  public String                       outFileCategories;
  public String                       outFileRelated;
  public String                       outFileRedirects;
  public String                       outFileImageLocations;
  public String                       outFileGeoLocations;

  public String                       outFileAbstracts;
  protected long                      started;
  protected byte[][]                  displayableLngs;
  protected byte[][]                  irrelevantPrefixesBytes;
  protected byte[]                    catKeyBytes;
  protected byte[]                    catKeyBytes2;
  protected byte[]                    catNameBytes;
  protected int                       minCatBytes                      = 0;
  protected WikiParseStep             step                             = WikiParseStep.HEADER;
  protected int                       lineLen;
  protected int                       lineOffset;
  protected ByteBuffer                nameBB                           = ArrayHelper.borrowByteBufferSmall();
  protected ByteBuffer                geoLocationBB                    = ArrayHelper.borrowByteBufferSmall();
  protected ByteBuffer                geoLocationInfoboxBB             = ArrayHelper.borrowByteBufferSmall();
  protected ByteBuffer                imgLocationBB                    = ArrayHelper.borrowByteBufferSmall();
  protected final Set<byte[]>         categories                       = new HashSet<byte[]>();
  protected final Map<byte[], byte[]> languages                        = new HashMap<byte[], byte[]>();
  protected final List<byte[]>        relatedWords                     = new ArrayList<byte[]>();
  protected int                       statSkipped;
  protected int                       statOk;
  protected int                       statOkSource;
  protected int                       statOkAttributes;
  protected int                       statOkCategory;
  protected int                       statSkippedCategory;
  protected int                       statRelated;
  protected int                       statRedirects;
  protected int                       statAbstracts;
  protected int                       statImageLocations;
  protected int                       statGeoLocations;
  protected boolean                   parseAbstract;
  protected long                      lineCount;
  protected boolean                   catName                          = false;
  protected final Set<byte[]>         irrelevantPrefixes               = new HashSet<byte[]>();
  protected ByteBuffer                tmpBB;
  protected byte[]                    tmpArray;
  protected ByteBuffer                lineBB;
  protected byte[]                    lineArray;
  protected boolean                   chinese                          = false;
  protected String                    fileLng;
  protected byte[]                    fileLngBytes;
  protected TranslationSource         translationSource;
  protected BufferedInputStream       in;
  protected BufferedOutputStream      out;
  protected BufferedOutputStream      outSource;
  protected BufferedOutputStream      outAttributes;
  protected BufferedOutputStream      outCategories;
  protected BufferedOutputStream      outRelated;
  protected BufferedOutputStream      outAbstracts;
  protected BufferedOutputStream      outRedirects;
  protected BufferedOutputStream      outImageLocations;
  protected BufferedOutputStream      outGeoLocations;
  protected WordType                  wordType                         = null;

  protected ByteBuffer                abstractBB                       = ArrayHelper.borrowByteBufferMedium();

  void addCategory() {
    final int wildcardIdx = ArrayHelper.indexOf(this.tmpBB, (byte) '|');
    if (wildcardIdx != -1) {
      this.tmpBB.limit(wildcardIdx);
    }
    if (this.chinese) {
      this.lineLen = ChineseHelper.toSimplifiedChinese(this.tmpBB);
    }
    final byte[] category = ArrayHelper.toBytes(this.tmpBB);
    this.categories.add(category);
    if (WikiExtractorBase.DEBUG) {
      System.out.println(ArrayHelper.toString(this.nameBB) + "，类别：" + ArrayHelper.toString(category));
    }
  }

  void addRelated() {
    if (ArrayHelper.indexOf(this.tmpArray, 0, this.tmpBB.limit(), (byte) ':') == -1) {
      final int idx = ArrayHelper.indexOf(this.tmpArray, 0, this.tmpBB.limit(), (byte) '|');
      if (idx != -1) {
        this.lineLen = idx;
      } else {
        this.lineLen = this.tmpBB.limit();
      }
      if (this.lineLen > 2) {
        this.tmpBB.limit(this.lineLen);
        final byte[] relatedBytes;
        if (this.chinese) {
          ChineseHelper.toSimplifiedChinese(this.tmpBB);
          relatedBytes = ArrayHelper.toBytes(this.tmpBB);
        } else {
          relatedBytes = ArrayHelper.toBytes(this.tmpBB);
        }
        this.relatedWords.add(relatedBytes);
        if (WikiExtractorBase.DEBUG && WikiExtractorBase.TRACE) {
          System.out.println("相关：" + ArrayHelper.toString(relatedBytes));
        }
      }
    }
  }

  void addTranslation(final int idx) {
    final byte[] tmpLngBytes = ArrayHelper.toBytes(this.tmpBB, idx);
    for (final byte[] l : this.displayableLngs) {
      if (Arrays.equals(l, tmpLngBytes)) {
        if (ArrayHelper.substring(this.tmpBB, idx + 1) > 0) {
          if (Arrays.equals(WikiExtractorBase.KEY_ZH_BYTES, l)) {
            ChineseHelper.toSimplifiedChinese(this.tmpBB);
            if (WikiExtractorBase.DEBUG) {
              System.out.println("语言：" + ArrayHelper.toString(l) + "/" + ArrayHelper.toString(tmpLngBytes) + "，翻译：" + ArrayHelper.toString(this.tmpBB));
            }
          }
          this.languages.put(l, ArrayHelper.toBytes(this.tmpBB));
        }
        break;
      }
    }
  }

  final void checkRedirectLine() throws IOException {
    boolean redirect = true;
    for (int i = 1; i < WikiExtractorBase.REDIRECT_LOWER_BYTES.length; i++) {
      final byte b = this.lineArray[i];
      if ((b != WikiExtractorBase.REDIRECT_LOWER_BYTES[i]) && (b != WikiExtractorBase.REDIRECT_UPPER_BYTES[i])) {
        redirect = false;
        break;
      }
    }
    if (redirect
        && (-1 != ArrayHelper.substringBetween(this.lineArray, WikiExtractorBase.REDIRECT_LOWER_BYTES.length, this.lineLen, (byte) '[', (byte) ']', this.tmpBB,
            true))) {
      this.writeRedirectLine();
    }
  }

  protected void cleanup() throws IOException {
    ArrayHelper.giveBack(this.tmpBB);
    ArrayHelper.giveBack(this.lineBB);
    this.in.close();
    if (this.out != null) {
      this.out.close();
    }
    if (this.outSource != null) {
      this.outRelated.close();
    }
    if (this.outAttributes != null) {
      this.outAttributes.close();
    }
    if (this.outRelated != null) {
      this.outRelated.close();
    }
    if (this.outCategories != null) {
      this.outCategories.close();
    }
    if (this.outRedirects != null) {
      this.outRedirects.close();
    }
    if (this.outAbstracts != null) {
      this.outAbstracts.close();
    }
    if (this.outImageLocations != null) {
      this.outImageLocations.close();
    }
    if (this.outGeoLocations != null) {
      this.outGeoLocations.close();
    }

    if (WikiExtractorBase.INFO) {
      System.out.println("\n> 成功分析'" + new File(this.inFile).getName() + "'（" + Helper.formatSpace(new File(this.inFile).length()) + "）文件，行数：" + this.lineCount
          + "，语言：" + this.fileLng + "，用时： " + Helper.formatDuration(System.currentTimeMillis() - this.started));
    }
    if (this.outFile != null) {
      System.out.print("> 字典文件：'" + this.outFile + "'（" + Helper.formatSpace(new File(this.outFile).length()) + "）");
      System.out.print("，定义：" + this.statOk);
      System.out.println("，跳过：" + this.statSkipped);
    }
    if (this.outFileSource != null) {
      System.out.print("> 来源文件：'" + this.outFileSource + "'（" + Helper.formatSpace(new File(this.outFileSource).length()) + "）");
      System.out.println("，有效：" + this.statOkSource);
    }
    if (this.outFileAttributes != null) {
      System.out.print("> 属性文件：'" + this.outFileAttributes + "'（" + Helper.formatSpace(new File(this.outFileAttributes).length()) + "）");
      System.out.println("，有效：" + this.statOkAttributes);
    }
    if (this.outFileCategories != null) {
      System.out.print("> 类别文件：'" + this.outFileCategories + "'（" + Helper.formatSpace(new File(this.outFileCategories).length()) + "）");
      System.out.print("，有效：" + this.statOkCategory);
      System.out.println("，跳过：" + this.statSkippedCategory);
    }
    if (this.outFileRelated != null) {
      System.out.print("> 相关文件：'" + this.outFileRelated + "'（" + Helper.formatSpace(new File(this.outFileRelated).length()) + "）");
      System.out.println("，定义：" + this.statRelated);

    }
    if (this.outFileRedirects != null) {
      System.out.print("> 重定向文件：'" + this.outFileRedirects + "'（" + Helper.formatSpace(new File(this.outFileRedirects).length()) + "）");
      System.out.println("，重定向：" + this.statRedirects);
    }
    if (this.outFileAbstracts != null) {
      System.out.print("> 概要文件：'" + this.outFileAbstracts + "'（" + Helper.formatSpace(new File(this.outFileAbstracts).length()) + "）");
      System.out.println("，概要：" + this.statAbstracts);
    }
    if (this.outFileGeoLocations != null) {
      System.out.print("> 坐标文件：'" + this.outFileGeoLocations + "'（" + Helper.formatSpace(new File(this.outFileGeoLocations).length()) + "）");
      System.out.println("，坐标：" + this.statGeoLocations);
    }
    if (this.outFileImageLocations != null) {
      System.out.print("> 图像文件：'" + this.outFileImageLocations + "'（" + Helper.formatSpace(new File(this.outFileImageLocations).length()) + "）");
      System.out.println("，图像：" + this.statImageLocations);
    }
    System.out.println();
    this.in = null;
    this.out = null;
    this.outRelated = null;
    this.outCategories = null;
  }

  protected void clearAttributes() {
    this.categories.clear();
    this.languages.clear();
    this.relatedWords.clear();
    this.parseAbstract = this.outAbstracts != null;
    this.wordType = null;
    this.opened = 0;
    this.insideInfobox = false;
    if (this.parseAbstract) {
      this.abstractBB.clear();
      this.geoLocationBB.clear();
      this.geoLocationInfoboxBB.clear();
      this.imgLocationBB.clear();
    }
  }

  @Override
  protected void finalize() throws Throwable {
    ArrayHelper.giveBack(this.lineBB);
    ArrayHelper.giveBack(this.tmpBB);
    ArrayHelper.giveBack(this.abstractBB);
    ArrayHelper.giveBack(this.nameBB);
    ArrayHelper.giveBack(this.geoLocationBB);
    ArrayHelper.giveBack(this.imgLocationBB);
    ArrayHelper.giveBack(this.geoLocationInfoboxBB);
    super.finalize();
  }

  void handleContentTitle() throws IOException {
    // name found
    boolean relevant = true;
    if (relevant) {
      for (final byte[] prefix : this.irrelevantPrefixesBytes) {
        if (ArrayHelper.startsWith(this.tmpBB, prefix)) {
          relevant = false;
          break;
        }
      }
    }
    if (relevant) {
      if (this.chinese) {
        this.lineLen = ChineseHelper.toSimplifiedChinese(this.tmpBB);
      }
      ArrayHelper.copy(this.tmpBB, this.nameBB);
      if (this.outFileCategories != null) {
        this.catName = this.isCategory();
      }
      if (WikiExtractorBase.DEBUG && !this.catName) {
        System.out.println("新词：" + ArrayHelper.toString(this.tmpBB));
        if ("faction".equals(ArrayHelper.toString(this.tmpBB))) {
          System.out.println("break");
        }
      }
      this.clearAttributes();
      this.step = WikiParseStep.TITLE_FOUND;
    } else {
      this.invalidate();
      this.statSkipped++;
    }
  }

  protected void handleTextBeginLine(final int idx) throws IOException {
    final int offset = idx + WikiExtractorBase.TAG_TEXT_BEGIN_BYTES.length;
    final int len = this.lineLen - offset;
    // boolean foundPageLayout = false;
    // if (len > 2 && lineArray[offset] == PAGE_LAYOUT_START_BYTES[0]
    // && lineArray[offset + 1] == PAGE_LAYOUT_START_BYTES[1]) {
    // foundPageLayout = true;
    // }
    // if (foundPageLayout) {
    // // content starting with layout
    // int stopPageLayoutIdx = ArrayHelper.indexOf(lineArray, offset, len, PAGE_LAYOUT_STOP_BYTES);
    // if (stopPageLayoutIdx == -1) {
    // // end tag not in same line
    // while (-1 != (lineLen = ArrayHelper.readLineTrimmed(in, lineBB))) {
    // ArrayHelper.trimP(lineBB);
    // if (ArrayHelper.equals(lineArray, lineBB.position(), PAGE_LAYOUT_STOP_BYTES)) {
    // break;
    // }
    // }
    // if (lineLen != -1) {
    // int off = lineBB.position() + PAGE_LAYOUT_STOP_BYTES.length;
    // lineLen = lineLen - off;
    // System.arraycopy(lineArray, off, lineArray, 0, lineLen);
    // lineBB.limit(lineLen);
    // }
    // } else {
    // int off = stopPageLayoutIdx + PAGE_LAYOUT_STOP_BYTES.length;
    // lineLen = lineLen - off;
    // System.arraycopy(lineArray, off, lineArray, 0, lineLen);
    // lineBB.limit(lineLen);
    // }
    // } else {
    this.lineLen = len;
    System.arraycopy(this.lineArray, offset, this.lineArray, 0, this.lineLen);
    this.lineBB.limit(this.lineLen);
    // }
    this.step = WikiParseStep.PAGE;
  }

  final void handleTextEndLine(final int idx) {
    this.lineBB.limit(idx);
    this.lineLen = this.lineBB.limit();
    this.step = WikiParseStep.BEFORE_TITLE;
  }

  protected void initialize(final String f, final String outDir, final String outPrefix, final String outPrefixCategories, final String outPrefixRelated,
      final String outPrefixAbstracts, final String outPrefixRedirects, final String outPrefixImages, final String outPrefixCoordinates,
      final String outPrefixSource, final String outPrefixAttributes) throws IOException {
    this.started = System.currentTimeMillis();

    this.displayableLngs = new byte[LanguageConstants.KEYS_WIKI.length][];
    int i = 0;
    for (final String k : LanguageConstants.KEYS_WIKI) {
      this.displayableLngs[i++] = k.getBytes(Helper.CHARSET_UTF8);
    }

    this.inFile = null;
    this.outFile = null;
    this.outFileCategories = null;
    this.outFileRelated = null;
    this.outFileSource = null;
    this.outFileAttributes = null;

    this.irrelevantPrefixesBytes = null;
    this.catKeyBytes = WikiExtractorBase.PREFIX_CATEGORY_KEY_EN_BYTES;
    this.catKeyBytes2 = WikiExtractorBase.PREFIX_CATEGORY_KEY_EN2_BYTES;
    this.minCatBytes = Math.min(this.catKeyBytes.length, this.catKeyBytes2.length) + WikiExtractorBase.SUFFIX_WIKI_TAG_BYTES.length;
    this.catNameBytes = WikiExtractorBase.CATEGORY_KEY_BYTES;
    this.step = WikiParseStep.HEADER;

    this.nameBB.clear();
    this.categories.clear();
    this.languages.clear();
    this.relatedWords.clear();
    this.statSkipped = 0;
    this.statOk = 0;
    this.statOkSource = 0;
    this.statOkAttributes = 0;
    this.statOkCategory = 0;
    this.statSkippedCategory = 0;
    this.statRelated = 0;
    this.statRedirects = 0;
    this.statAbstracts = 0;
    this.statGeoLocations = 0;
    this.statImageLocations = 0;
    this.lineCount = 0;
    this.catName = false;
    this.irrelevantPrefixes.clear();
    this.tmpBB = ArrayHelper.borrowByteBufferNormal();
    this.tmpArray = this.tmpBB.array();
    this.lineBB = ArrayHelper.borrowByteBufferNormal();
    this.lineArray = this.lineBB.array();
    this.chinese = false;

    this.inFile = f;
    if (WikiExtractorBase.INFO) {
      System.out.println("< 分析'" + f + "' （" + Helper.formatSpace(new File(f).length()) + "）。。。");
    }
    this.fileLng = DictHelper.getWikiLanguage(f).key;
    this.fileLngBytes = this.fileLng.getBytes(Helper.CHARSET_UTF8);
    this.chinese = Language.ZH.key.equalsIgnoreCase(this.fileLng);
    this.translationSource = TranslationSource.valueOf(Helper.toConstantName("wiki_" + this.fileLng));
    if (f.endsWith(".bz2")) {
      this.in = new BufferedInputStream(new CBZip2InputStream((new BufferedInputStream(new FileInputStream(f), Helper.BUFFER_SIZE))), Helper.BUFFER_SIZE);
    } else {
      this.in = new BufferedInputStream(new FileInputStream(f), Helper.BUFFER_SIZE);
    }
    if (outPrefix != null) {
      this.outFile = outDir + File.separator + outPrefix + this.fileLng;
      if (WikiExtractorBase.DEBUG) {
        System.out.println("写出：" + this.outFile + " 。。。");
      }
      this.out = new BufferedOutputStream(new FileOutputStream(this.outFile), Helper.BUFFER_SIZE);
    }
    if (outPrefixCategories != null) {
      this.outFileCategories = outDir + File.separator + outPrefixCategories + this.fileLng;
      this.outCategories = new BufferedOutputStream(new FileOutputStream(this.outFileCategories), Helper.BUFFER_SIZE);
    }
    if (outPrefixRelated != null) {
      this.outFileRelated = outDir + File.separator + outPrefixRelated + this.fileLng;
      this.outRelated = new BufferedOutputStream(new FileOutputStream(this.outFileRelated), Helper.BUFFER_SIZE);
    }
    if (outPrefixRedirects != null) {
      this.outFileRedirects = outDir + File.separator + outPrefixRedirects + this.fileLng;
      this.outRedirects = new BufferedOutputStream(new FileOutputStream(this.outFileRedirects), Helper.BUFFER_SIZE);
    }
    if (outPrefixAbstracts != null) {
      this.outFileAbstracts = outDir + File.separator + outPrefixAbstracts + this.fileLng;
      this.outAbstracts = new BufferedOutputStream(new FileOutputStream(this.outFileAbstracts), Helper.BUFFER_SIZE);
    }
    if (outPrefixImages != null) {
      this.outFileImageLocations = outDir + File.separator + outPrefixImages + this.fileLng;
      this.outImageLocations = new BufferedOutputStream(new FileOutputStream(this.outFileImageLocations), Helper.BUFFER_SIZE);
    }
    if (outPrefixCoordinates != null) {
      this.outFileGeoLocations = outDir + File.separator + outPrefixCoordinates + this.fileLng;
      this.outGeoLocations = new BufferedOutputStream(new FileOutputStream(this.outFileGeoLocations), Helper.BUFFER_SIZE);
    }
    if (outPrefixSource != null) {
      this.outFileSource = outDir + File.separator + outPrefixSource + this.fileLng;
      this.outSource = new BufferedOutputStream(new FileOutputStream(this.outFileSource), Helper.BUFFER_SIZE);
    }
    if (outPrefixAttributes != null) {
      this.outFileAttributes = outDir + File.separator + outPrefixAttributes + this.fileLng;
      this.outAttributes = new BufferedOutputStream(new FileOutputStream(this.outFileAttributes), Helper.BUFFER_SIZE);
    }
    this.parseAbstract = this.outAbstracts != null;
  }

  final void invalidate() {
    this.nameBB.clear();
  }

  final boolean isCategory() {
    return ArrayHelper.startsWith(this.nameBB, this.catNameBytes);
  }

  final boolean isValid() {
    return !ArrayHelper.isEmpty(this.nameBB);
  }

  protected void parseAbstract() {
    /*
     * if (-1 != ArrayHelper.indexOf(lineArray, 0, lineLen, "Abkhazia Gali map.svg".getBytes(Helper.CHARSET_UTF8))) { System.out.println("debug"); }
     */
    final byte firstByte = this.lineArray[0];
    final boolean hasContent = !ArrayHelper.isEmpty(this.abstractBB) && this.categories.isEmpty();
    if (firstByte == '=') {
      this.parseAbstract = false;
    }
    if (this.parseAbstract) {
      if (this.opened != 0) {
        // previous line not ended yet
        if (this.openIdx == 0) {
          final int stop = ArrayHelper.indexOf(this.lineArray, 0, this.lineLen, Helper.BYTES_XML_TAG_STOP);
          if (stop != -1) {
            this.opened = 0;
            this.lineBB.position(stop + Helper.BYTES_XML_TAG_STOP.length);
          }
        } else {
          byte b;
          final byte open = WikiExtractorBase.OPEN_ENDS[this.openIdx][0];
          final byte end = WikiExtractorBase.OPEN_ENDS[this.openIdx][1];
          for (int i = 0; i < this.lineLen; i++) {
            b = this.lineArray[i];
            if (b == end) {
              this.opened--;
            } else if (b == open) {
              this.opened++;
            }
            if (this.opened == 0) {
              if ((i + 1) < this.lineLen) {
                this.lineBB.position(i + 1);
              } else {
                this.lineBB.position(i);
              }
              break;
            }
          }
        }
        if (this.opened == 0) {
          this.insideInfobox = false;
        } else if (this.insideInfobox) {
          this.parseInfobox();
        }
      }
      if ((this.opened == 0) && this.lineBB.hasRemaining()) {
        if (hasContent) {
          this.abstractBB.position(this.abstractBB.limit()).limit(this.abstractBB.capacity());
          if (this.abstractBB.remaining() > 10) {
            this.abstractBB.put((byte) ' ');
          }
        } else {
          this.abstractBB.clear();
        }
        this.stripWikiLineP(this.lineBB, this.abstractBB, Abstract.MAX_ABSTRACT_CHARS);
        if (this.abstractBB.limit() > Abstract.MIN_ABSTRACT_CHARS) {
          this.parseAbstract = false;
        }
      }
      // if ("生物学".equals(ArrayHelper.toString(name))) {
      // System.out.println(ArrayHelper.toString(lineBB));
      // System.out.println(ArrayHelper.toString(abstractBB) + ", opened: " + opened);
      // }
      this.parseImageLocation();
      this.parseGeoLocation();
    }

  }

  protected void parseDocumentHeader() {
    if (ArrayHelper.contains(this.lineArray, 0, this.lineLen, WikiExtractorBase.SUFFIX_NAMESPACES_BYTES)) {
      // finish prefixes
      this.irrelevantPrefixesBytes = new byte[this.irrelevantPrefixes.size()][];
      int i = 0;
      for (final byte[] prefix : this.irrelevantPrefixes) {
        this.irrelevantPrefixesBytes[i] = prefix;
        i++;
      }
      if (WikiExtractorBase.DEBUG) {
        System.out.println("所有过滤前缀：");
        for (final byte[] prefix : this.irrelevantPrefixesBytes) {
          System.out.println("- " + ArrayHelper.toString(prefix));
        }
      }
      this.step = WikiParseStep.BEFORE_TITLE;
    } else if (ArrayHelper.substringBetweenLast(this.lineArray, 0, this.lineLen, WikiExtractorBase.SUFFIX_XML_TAG_BYTES,
        WikiExtractorBase.SUFFIX_NAMESPACE_BYTES, this.tmpBB) > 0) {
      // add prefix
      final int limit = this.tmpBB.limit();
      this.tmpBB.limit(limit + 1);
      this.tmpBB.put(limit, (byte) ':');
      if (WikiExtractorBase.DEBUG) {
        System.out.println("找到域码：" + ArrayHelper.toString(this.tmpBB));
      }
      // System.out.println(ArrayHelper.toString(lineBB));
      if (ArrayHelper.contains(this.lineArray, 0, this.lineLen, WikiExtractorBase.ATTR_CATEGORY_KEY_BYTES)) {
        this.catKeyBytes = new byte[this.tmpBB.limit() + 2];
        this.catKeyBytes[0] = '[';
        this.catKeyBytes[1] = '[';
        System.arraycopy(this.tmpArray, 0, this.catKeyBytes, 2, this.tmpBB.limit());

        this.catKeyBytes2 = new byte[this.tmpBB.limit() + 3];
        this.catKeyBytes2[0] = '[';
        this.catKeyBytes2[1] = '[';
        this.catKeyBytes2[2] = ':';
        System.arraycopy(this.tmpArray, 0, this.catKeyBytes2, 3, this.tmpBB.limit());

        this.minCatBytes = Math.min(this.catKeyBytes.length, this.catKeyBytes2.length) + WikiExtractorBase.SUFFIX_WIKI_TAG_BYTES.length;
        this.catNameBytes = ArrayHelper.toBytes(this.tmpBB);
        if (WikiExtractorBase.DEBUG) {
          System.out.println("找到类别代码：" + ArrayHelper.toString(this.tmpBB));
        }
        if (this.outFileCategories == null) {
          this.irrelevantPrefixes.add(ArrayHelper.toBytes(this.tmpBB));
          this.tmpBB.limit(this.tmpBB.limit() + 1);
          final byte[] catBytes2 = ArrayHelper.toBytes(this.tmpBB, 1, this.tmpBB.limit() - 1);
          catBytes2[0] = ':';
          this.irrelevantPrefixes.add(catBytes2);
        }
      } else {
        this.irrelevantPrefixes.add(ArrayHelper.toBytes(this.tmpBB));
      }
    }
  }

  protected void parseGeoLocation() {
    // http://dbpedia.hg.sourceforge.net/hgweb/dbpedia/dbpedia/file/945c24bdc54c/extraction/extractors/GeoExtractor.php
    if ((this.outGeoLocations != null) && ArrayHelper.isEmpty(this.geoLocationBB) && ArrayHelper.isEmpty(this.geoLocationInfoboxBB)) {
      int idx;
      if (-1 != (idx = ArrayHelper.indexOfP(this.lineBB, WikiExtractorBase.COORD_TAG_BYTES_LOWER, WikiExtractorBase.COORD_TAG_BYTES_UPPER))) {
        final int start = idx;
        int end = -1;
        int brackets = 2;
        byte b;
        for (int i = idx + 5; i < this.lineBB.limit(); i++) {
          b = this.lineArray[i];
          if (b == '}') {
            brackets--;
          } else if (b == '{') {
            break;
          }
          if (brackets == 0) {
            end = i + 1;
            break;
          }
        }
        if (end != -1) {
          final int len = end - start;
          if ((len > 0) && (len < this.geoLocationBB.remaining())) {
            ArrayHelper.copy(this.lineBB, start, this.geoLocationBB, 0, len);
            this.geoLocationBB.limit(len);
          }
          // System.out.println(ArrayHelper.toString(geoLocation));
        }

      }
    }
  }

  protected void parseImageLocation() {
    if ((this.outImageLocations != null) && ArrayHelper.isEmpty(this.imgLocationBB)) {
      // http://dbpedia.hg.sourceforge.net/hgweb/dbpedia/dbpedia/file/945c24bdc54c/extraction/extractors/ImageExtractor.php
      // (check non-free)
      int idx;
      if (-1 != (idx = ArrayHelper.indexOfP(this.lineBB, WikiExtractorBase.IMAGE_SUFFIX_BYTES_LOWER, WikiExtractorBase.IMAGE_SUFFIX_BYTES_UPPER))) {
        int start = this.lineBB.position();
        int end = this.lineBB.limit();
        byte b;
        for (int i = idx; i < this.lineBB.limit(); i++) {
          b = this.lineArray[i];
          if ((b == '|') || (b == ']') || (b == '&') || (b == ' ') || (b == '}')) {
            end = i;
            break;
          }
        }
        for (int i = idx; i >= this.lineBB.position(); i--) {
          b = this.lineArray[i];
          if ((b == ':') && (this.lineArray[i + 1] != '/')) {
            break;
          } else if ((b == '=') || (b == '[') || (b == '|') || (b == '{')) {
            break;
          } else if (b != ' ') {
            start = i;
          }
        }
        final int len = end - start;
        if (len < this.imgLocationBB.remaining()) {
          // System.out.println(idx + " - " + ArrayHelper.toString(lineBB.array(), start, end) + " - "
          // + ArrayHelper.toString(lineBB));
          // System.out.println(ArrayHelper.toString(imgLocationBB));
          ArrayHelper.copy(this.lineBB, start, this.imgLocationBB, 0, len);
          this.imgLocationBB.limit(len);
        }
        // System.out.println(ArrayHelper.toString(imgLocation));
      }
    }
  }

  protected void parseInfobox() {
    // parseImageLocation();
    this.parseGeoLocationInfobox();
  }

  protected void parseGeoLocationInfobox() {
    if ((this.outGeoLocations != null) && ArrayHelper.isEmpty(this.geoLocationBB)) {
      // http://dbpedia.hg.sourceforge.net/hgweb/dbpedia/dbpedia/file/945c24bdc54c/extraction/extractors/GeoExtractor.php
      // (check bounds)
      boolean found = false;
      int start = -1;
      int end = -1;
      byte b;
      while (!found) {
        final int startPos = Math.max(end + 1, this.lineBB.position());
        start = -1;
        end = -1;
        for (int i = startPos; i < this.lineBB.limit(); i++) {
          b = this.lineArray[i];
          if (b == '|') {
            start = i + 1;
          } else if ((start != -1) && (b != ' ')) {
            start = i;
            break;
          } else if ((start == -1) && (b == '=')) {
            // include lines not starting with |
            for (int j = startPos; j < i; j++) {
              if (b != ' ') {
                start = j;
                for (int k = i - 1; k > start; k--) {
                  if (b != ' ') {
                    end = k + 1;
                    break;
                  }
                }
                break;
              }
            }
          }
        }
        if ((start != -1) && (end == -1)) {
          for (int i = start + 1; i < this.lineBB.limit(); i++) {
            b = this.lineArray[i];
            if ((b == ' ') || (b == '=')) {
              end = i;
              break;
            } else if (b == '|') {
              break;
            }
          }
        }

        if ((start != -1) && (end != -1)) {
          final int len = end - start;
          // System.out.println(ArrayHelper.toString(lineBB) + ", s=" + start + ", e=" + end + ", l=" + len);
          for (int i = 0; i < WikiExtractorBase.INFOBOX_GEO_BYTES.length; i++) {
            final byte[] text = WikiExtractorBase.INFOBOX_GEO_BYTES[i];
            if ((text.length == len) && ArrayHelper.equals(this.lineArray, start, text)) {
              if (!((text.length == 1) && (this.lineLen > 3) && (this.lineArray[3] == '{'))) {
                found = true;
                break;
              }
            }
          }
        } else {
          break;
        }
      }
      if (found) {
        final int lod = this.lineBB.limit() - start;
        if (!ArrayHelper.isEmpty(this.geoLocationInfoboxBB)) {
          final int remains = this.geoLocationInfoboxBB.capacity() - this.geoLocationInfoboxBB.limit();
          if (remains > (lod + 1)) {
            this.geoLocationInfoboxBB.position(this.geoLocationInfoboxBB.limit()).limit(this.geoLocationInfoboxBB.capacity());
            this.geoLocationInfoboxBB.put((byte) '|');
            ArrayHelper.copy(this.lineBB, start, this.geoLocationInfoboxBB, this.geoLocationInfoboxBB.position(), lod);
            this.geoLocationInfoboxBB.limit(this.geoLocationInfoboxBB.position() + lod);
          } else {
            System.err.println("geoLocationInfoboxBB过长（" + lod + "，" + this.geoLocationInfoboxBB.position() + "，" + this.geoLocationInfoboxBB.limit() + "）："
                + ArrayHelper.toString(this.geoLocationInfoboxBB) + " --- " + ArrayHelper.toString(this.lineBB));
          }
        } else {
          if (this.geoLocationInfoboxBB.remaining() > lod) {
            ArrayHelper.copy(this.lineBB, start, this.geoLocationInfoboxBB, this.geoLocationInfoboxBB.position(), lod);
            this.geoLocationInfoboxBB.limit(this.geoLocationInfoboxBB.position() + lod);
          } else {
            System.err.println("geoLocationInfoboxBB-n过长：" + ArrayHelper.toString(this.geoLocationInfoboxBB) + " --- " + ArrayHelper.toString(this.lineBB));
          }
        }
        // System.out.println(ArrayHelper.toString(geoLocationInfobox));
      }
    }
  }

  protected final void signal() {
    if (((this.lineCount % (WikiExtractorBase.OK_NOTICE * 100)) == 0) && (this.lineCount != 0)) {
      System.out.println(".");
    } else {
      System.out.print(".");
    }
  }

  protected int stripWikiLine(final byte[] array, final int offset, final int limit, final ByteBuffer outBB, final int maxChars) {
    byte b;
    int countEquals = 0;
    int countQuos = 0;
    int countSpaces = 0;
    byte lastByte = -1;
    final int startPos = outBB.position();
    STRIP: for (int i = offset; i < limit; i++) {
      if (outBB.position() > maxChars) {
        break;
      }
      b = array[i];
      final boolean hasNext = (i + 1) < limit;
      if ((b != ' ') && (countSpaces > 0)) {
        if ((lastByte != ' ') && (lastByte != '(') && (lastByte != -1)) {
          outBB.put((byte) ' ');
          lastByte = ' ';
        }
        countSpaces = 0;
      }
      if ((b != '\'') && (countQuos == 1)) {
        outBB.put((byte) '\'');
        lastByte = '\'';
        countQuos = 0;
      }
      if ((b != '=') && (countEquals == 1)) {
        outBB.put((byte) '=');
        lastByte = '=';
        countEquals = 0;
      }
      switch (b) {
        case ' ':
          countSpaces++;
          continue;
        case '&':
          if (((i + 8) < limit) && ArrayHelper.equals(array, i, Helper.BYTES_XML_TAG_START)) {
            i += Helper.BYTES_XML_TAG_START.length - 1;
            int start = i + 1;
            int stop = ArrayHelper.indexOf(array, start, limit - start, Helper.BYTES_XML_TAG_STOP);
            if (stop != -1) {
              if (((stop - start) == WikiExtractorBase.TAG_IMAGEMAP_NAME_BYTES.length)
                  && ArrayHelper.equals(array, start, WikiExtractorBase.TAG_IMAGEMAP_NAME_BYTES)) {
                // <imagemap>...</imagemap>
                start = stop + Helper.BYTES_XML_TAG_STOP.length;
                stop = ArrayHelper.indexOf(array, start, limit - start, Helper.BYTES_XML_TAG_STOP);
                if (stop != -1) {
                  i = (stop + Helper.BYTES_XML_TAG_STOP.length) - 1;
                } else {
                  this.opened = 1;
                  this.openIdx = 0;
                  break STRIP;
                }
              } else {
                i = (stop + Helper.BYTES_XML_TAG_STOP.length) - 1;
              }
            } else {
              this.opened = 1;
              this.openIdx = 0;
              break STRIP;
            }
          } else if ((i + 3) < limit) {
            final int stop = ArrayHelper.indexOf(array, i, limit, (byte) ';');
            if (stop != -1) {
              i = stop;
            }
          }
          continue;
        case '{':
          boolean infobox = false;
          if (hasNext && ((infobox = array[i + 1] == '{') || (array[i + 1] == '|'))) {
            // begins with {{ or {|
            this.opened = 1;
            boolean writeValue = false;
            if (infobox) {
              this.opened = 2;
              writeValue = true;
            }

            final int start = i + 2;
            int stop;
            int walls = 0;
            final int[] wallsIdx = new int[3];
            final int[] skipIdx = new int[WikiExtractorBase.ABSTRACT_SKIP_VARS_BYTES_LOWER.length];
            for (stop = start; stop < limit; stop++) {
              b = array[stop];
              if (b == '}') {
                this.opened--;
              } else if (b == '{') {
                this.opened++;
              }

              if (writeValue) {
                if ((b == '=') || (b == ':')) {
                  writeValue = false;
                } else if (b == '|') {
                  if (walls < wallsIdx.length) {
                    wallsIdx[walls] = stop;
                  }
                  walls++;
                }
                for (int j = 0; j < skipIdx.length; j++) {
                  final int idx = skipIdx[j];
                  final byte[] varKeyLower = WikiExtractorBase.ABSTRACT_SKIP_VARS_BYTES_LOWER[j];
                  final byte[] varKeyUpper = WikiExtractorBase.ABSTRACT_SKIP_VARS_BYTES_UPPER[j];
                  if ((b == varKeyLower[idx]) || (b == varKeyUpper[idx])) {
                    if ((idx + 1) < varKeyLower.length) {
                      skipIdx[j] = idx + 1;
                    } else {
                      skipIdx[j] = -1;
                      writeValue = false;
                      break;
                    }
                  } else {
                    skipIdx[j] = 0;
                  }
                }
              }
              if (this.opened == 0) {
                break;
              }
            }
            i = stop;
            if ((skipIdx[0] == -1) || (infobox && (this.opened == 2))) {
              // found infobox
              this.insideInfobox = true;
            }
            if (this.insideInfobox) {
              this.parseInfobox();
            }
            if (this.opened != 0) {
              this.openIdx = 1;
              break STRIP;
            } else {
              this.insideInfobox = false;
              if (writeValue && (walls < wallsIdx.length) && (walls > 0)) {
                // write e.g. 100km2
                // System.out.println(ArrayHelper.toString(outBB.array(), 0, outBB.position()));
                WikiExtractorBase.writeWikiVariable(array, start, stop - 1, walls, wallsIdx, outBB);
                // System.out.println(ArrayHelper.toString(outBB.array(), 0, outBB.position()));
              }
            }
          }
          continue;
        case '}':
          continue;
        case '[':
          if (hasNext) {
            this.opened = 1;
            boolean writeValue = true;
            boolean link = false;

            final int start = i + 1;
            int stop;
            int walls = 0;
            int relStart = -1;
            int relStop = -1;
            for (stop = start; stop < limit; stop++) {
              b = array[stop];
              if (b == '|') {
                // wiki link title
                walls++;
                if (walls == 1) {
                  i = stop;
                  if (relStop == -1) {
                    relStop = stop;
                  }
                }
              } else if (b == ']') {
                this.opened--;
                if (relStop == -1) {
                  relStop = stop;
                }
              } else if (b == '[') {
                this.opened++;
                if ((this.opened == 2) && (stop == (start + 1))) {
                  // move
                  i = stop;
                  if (relStart == -1) {
                    relStart = stop;
                  }
                }
              } else if (link && (b == ' ')) {
                // link title
                i = stop;
                link = false;
              }
              if (walls == 0) {
                if (b == '=') {
                  writeValue = false;
                } else if (b == ':') {
                  if (this.opened == 1) {
                    link = true;
                  } else {
                    writeValue = false;
                  }
                }
              }
              if (this.opened == 0) {
                break;
              }
            }
            if (this.opened == 0) {
              if ((relStart != -1) && (relStop != -1) && (relStop > relStart) && !link && writeValue && (walls < 2)) {
                final byte[] relatedBytes = ArrayHelper.toBytes(outBB, relStart, relStop - relStart);
                this.relatedWords.add(relatedBytes);
                if (WikiExtractorBase.DEBUG && WikiExtractorBase.TRACE) {
                  System.out.println("相关：" + ArrayHelper.toString(relatedBytes));
                }
              }
              if (!writeValue || link || (walls > 1)) {
                i = stop;
              }
            } else {
              this.openIdx = 2;
              break STRIP;
            }
          }
          continue;
        case '|':
          continue;
        case ']':
          continue;
        case '\'':
          countQuos++;
          continue;
        case '=':
          countEquals++;
          continue;
          // case '*':
          // continue;
          // case '#':
          // continue;
        case '_':
          if (hasNext && (array[i + 1] == '_')) {
            i++;
            final int stop = ArrayHelper.indexOf(array, i, limit - i, WikiExtractorBase.BYTES_WIKI_ANNOTATION_UNDERSCORE);
            if (stop != -1) {
              i = (stop + WikiExtractorBase.BYTES_WIKI_ANNOTATION_UNDERSCORE.length) - 1;
            }
          }
          continue;
        case '-':
          if (((i + 6) < limit) && (array[i + 1] == '-') && (array[i + 2] == '-')) {
            i = i + 2;
            final int stop = ArrayHelper.indexOf(array, i, limit - i, WikiExtractorBase.BYTES_WIKI_ANNOTATION_MINUS);
            if (stop != -1) {
              i = (stop + WikiExtractorBase.BYTES_WIKI_ANNOTATION_MINUS.length) - 1;
            }
            continue;
          }
          break;
      }
      outBB.put(b);
      lastByte = b;
    }
    if (outBB.position() > maxChars) {
      int idx = -1;
      if (this.chinese) {
        idx = ArrayHelper.lastIndexOf(outBB.array(), startPos, outBB.position(), WikiExtractorBase.COMMA_BYTES);
        if (idx == -1) {
          idx = ArrayHelper.lastIndexOf(outBB.array(), startPos, outBB.position(), WikiExtractorBase.POINT_BYTES);
        }
      }
      if (idx == -1) {
        idx = ArrayHelper.lastIndexOf(outBB.array(), startPos, outBB.position(), (byte) ',');
        if (idx == -1) {
          idx = ArrayHelper.lastIndexOf(outBB.array(), startPos, outBB.position(), (byte) '.');
          if (idx == -1) {
            idx = ArrayHelper.lastIndexOf(outBB.array(), startPos, outBB.position(), (byte) ' ');
          }
        }
      }

      if (idx != -1) {
        outBB.position(idx);
      }
      outBB.put((byte) ' ').put(Helper.SEP_ETC_BYTES);
    }
    outBB.limit(outBB.position()).rewind();
    return outBB.limit();
  }

  public int stripWikiLine(final ByteBuffer inBB, final ByteBuffer outBB, final int maxChars) {
    final byte[] array = inBB.array();
    final int limit = inBB.limit();
    outBB.clear();
    return this.stripWikiLine(array, 0, limit, outBB, maxChars);
  }

  public int stripWikiLineP(final ByteBuffer inBB, final ByteBuffer outBB, final int maxChars) {
    final byte[] array = inBB.array();
    final int offset = inBB.position();
    final int limit = inBB.limit();
    return this.stripWikiLine(array, offset, limit, outBB, maxChars);
  }

  private boolean writeAbstract() throws IOException {
    if ((this.outAbstracts != null) && !ArrayHelper.isEmpty(this.abstractBB)) {
      if (WikiExtractorBase.DEBUG && WikiExtractorBase.TRACE) {
        System.out.println(ArrayHelper.toString(this.nameBB) + "的概要：" + ArrayHelper.toString(this.abstractBB));
      }
      if (this.chinese) {
        ChineseHelper.toSimplifiedChinese(this.abstractBB);
      }
      // System.out.println(ArrayHelper.toString(name) + "的概要：" + ArrayHelper.toString(abstractBB));
      this.outAbstracts.write(this.fileLngBytes);
      this.outAbstracts.write(Helper.SEP_DEFINITION_BYTES);
      this.outAbstracts.write(this.nameBB.array(), 0, this.nameBB.limit());
      this.outAbstracts.write(Helper.SEP_ATTRS_BYTES);
      this.outAbstracts.write(Abstract.TYPE_ID_BYTES);
      this.outAbstracts.write(this.abstractBB.array(), 0, this.abstractBB.limit());
      this.outAbstracts.write(Helper.SEP_NEWLINE_CHAR);
      return true;
    }
    return false;
  }

  private boolean writeCategory() throws IOException {
    this.outCategories.write(this.fileLngBytes);
    this.outCategories.write(Helper.SEP_DEFINITION_BYTES);
    this.outCategories.write(this.nameBB.array(), ArrayHelper.indexOf(this.nameBB.array(), 0, this.nameBB.limit(), (byte) ':') + 1, this.nameBB.limit());

    if (!this.languages.isEmpty()) {
      final Iterator<Entry<byte[], byte[]>> i = this.languages.entrySet().iterator();
      for (;;) {
        this.outCategories.write(Helper.SEP_LIST_BYTES);
        final Entry<byte[], byte[]> e = i.next();
        final byte[] key = e.getKey();
        final byte[] value = e.getValue();
        final int offset = ArrayHelper.indexOf(value, 0, value.length, (byte) ':') + 1;
        this.outCategories.write(key);
        this.outCategories.write(Helper.SEP_DEFINITION_BYTES);
        this.outCategories.write(value, offset, value.length - offset);
        if (!i.hasNext()) {
          break;
        }
      }
      this.outCategories.write(Helper.SEP_NEWLINE_CHAR);
    }
    return true;
  }

  protected boolean writeDef() throws IOException {
    this.out.write(this.fileLngBytes);
    this.out.write(Helper.SEP_DEFINITION_BYTES);
    this.out.write(this.nameBB.array(), 0, this.nameBB.limit());
    if (!this.languages.isEmpty()) {
      final Iterator<Entry<byte[], byte[]>> i = this.languages.entrySet().iterator();
      for (;;) {
        this.out.write(Helper.SEP_LIST_BYTES);
        final Entry<byte[], byte[]> e = i.next();
        final byte[] key = e.getKey();
        final byte[] value = e.getValue();
        this.out.write(key);
        this.out.write(Helper.SEP_DEFINITION_BYTES);
        this.out.write(value);
        if (!i.hasNext()) {
          break;
        }
      }
    }
    this.out.write(Helper.SEP_NEWLINE_CHAR);
    return true;
  }

  private final boolean writeAttributes() throws IOException {
    if (this.outAttributes != null) {
      boolean first = true;
      if (!this.categories.isEmpty()) {
        for (final byte[] c : this.categories) {
          if (first) {
            first = false;
            this.outAttributes.write(this.fileLngBytes);
            this.outAttributes.write(Helper.SEP_DEFINITION_BYTES);
            this.outAttributes.write(this.nameBB.array(), 0, this.nameBB.limit());
          }
          this.outAttributes.write(Helper.SEP_ATTRS_BYTES);
          this.outAttributes.write(Category.TYPE_ID_BYTES);
          this.outAttributes.write(c);
        }
      }
      if (!first) {
        this.outAttributes.write(Helper.SEP_NEWLINE_CHAR);
        return true;
      }
    }
    return false;
  }

  private final boolean writeSource() throws IOException {
    if (this.outSource != null) {
      this.outSource.write(this.fileLngBytes);
      this.outSource.write(Helper.SEP_DEFINITION_BYTES);
      this.outSource.write(this.nameBB.array(), 0, this.nameBB.limit());
      this.outSource.write(Helper.SEP_ATTRS_BYTES);
      this.outSource.write(TranslationSource.TYPE_ID_BYTES);
      this.outSource.write(this.translationSource.keyBytes);
      this.outSource.write(Helper.SEP_NEWLINE_CHAR);
      if (this.wordType != null) {
        this.outSource.write(Helper.SEP_ATTRS_BYTES);
        this.outSource.write(WordType.TYPE_ID_BYTES);
        this.outSource.write(this.wordType.keyBytes);
      }
      return true;
    }
    return false;
  }

  void writeDefinition() throws IOException {
    if (this.isValid()) {
      if (this.catName) {
        if ((this.outCategories != null) && !this.languages.isEmpty()) {
          if (this.writeCategory()) {
            if (WikiExtractorBase.DEBUG) {
              System.out.println("类：" + ArrayHelper.toString(this.nameBB));
              System.out.print("翻译：");
              final Set<Entry<byte[], byte[]>> entrySet = this.languages.entrySet();
              for (final Entry<byte[], byte[]> entry : entrySet) {
                System.out.print(ArrayHelper.toString(entry.getKey()) + "=" + ArrayHelper.toString(entry.getValue()) + ", ");
              }
              System.out.println();
            }
            this.statOkCategory++;
            return;
          }
        }
        this.statSkippedCategory++;
      } else if (!this.languages.isEmpty()) {
        if ((this.out != null) && this.writeDef()) {
          if (this.writeSource()) {
            this.statOkSource++;
          }
          if (this.writeAttributes()) {
            this.statOkAttributes++;
          }
          if (this.writeAbstract()) {
            this.statAbstracts++;
          }
          if (this.writeImageLocation()) {
            this.statImageLocations++;
          }
          if (this.writeGeoLocation()) {
            this.statGeoLocations++;
          }
          if (this.writeRelated()) {
            this.statRelated++;
          }
          this.statOk++;
        } else {
          this.statSkipped++;
        }
      }
    }
  }

  private boolean writeGeoLocation() throws IOException {
    if (this.outGeoLocations != null) {
      ByteBuffer geocode = null;
      if (!ArrayHelper.isEmpty(this.geoLocationBB)) {
        geocode = this.geoLocationBB;
      } else if (!ArrayHelper.isEmpty(this.geoLocationInfoboxBB)) {
        geocode = this.geoLocationInfoboxBB;
      }
      if (geocode != null) {
        if (WikiExtractorBase.DEBUG) {
          System.out.println(ArrayHelper.toString(this.nameBB) + "的坐标：" + ArrayHelper.toString(geocode));
        }
        this.outGeoLocations.write(this.fileLngBytes);
        this.outGeoLocations.write(Helper.SEP_DEFINITION_BYTES);
        this.outGeoLocations.write(this.nameBB.array(), 0, this.nameBB.limit());
        this.outGeoLocations.write(Helper.SEP_ATTRS_BYTES);
        this.outGeoLocations.write(GeoLocation.TYPE_ID_BYTES);
        this.outGeoLocations.write(geocode.array(), 0, geocode.limit());
        this.outGeoLocations.write(Helper.SEP_NEWLINE_CHAR);
        return true;
      }
    }
    return false;
  }

  private boolean writeImageLocation() throws IOException {
    if ((this.outImageLocations != null) && !ArrayHelper.isEmpty(this.imgLocationBB)) {
      if (WikiExtractorBase.DEBUG) {
        System.out.println(ArrayHelper.toString(this.nameBB) + "的图像：" + ArrayHelper.toString(this.imgLocationBB));
      }
      this.outImageLocations.write(this.fileLngBytes);
      this.outImageLocations.write(Helper.SEP_DEFINITION_BYTES);
      this.outImageLocations.write(this.nameBB.array(), 0, this.nameBB.limit());
      this.outImageLocations.write(Helper.SEP_ATTRS_BYTES);
      this.outImageLocations.write(ImageLocation.TYPE_ID_BYTES);
      this.outImageLocations.write(this.imgLocationBB.array(), 0, this.imgLocationBB.limit());
      this.outImageLocations.write(Helper.SEP_NEWLINE_CHAR);
      return true;
    }
    return false;
  }

  protected void writeRedirectLine() throws IOException {
    if (this.chinese) {
      ChineseHelper.toSimplifiedChinese(this.tmpBB);
      if (ArrayHelper.equals(this.nameBB, this.tmpBB)) {
        this.tmpBB.limit(0);
      }
    }
    if ((this.outRedirects != null) && this.tmpBB.hasRemaining()) {
      this.outRedirects.write(this.fileLngBytes);
      this.outRedirects.write(Helper.SEP_DEFINITION_BYTES);
      this.outRedirects.write(this.nameBB.array(), 0, this.nameBB.limit());
      this.outRedirects.write(Helper.SEP_ATTRS_BYTES);
      this.outRedirects.write(Redirect.TYPE_ID_BYTES);
      this.outRedirects.write(this.tmpArray, 0, this.tmpBB.limit());
      this.outRedirects.write(Helper.SEP_NEWLINE_CHAR);
      this.statRedirects++;
      if (WikiExtractorBase.DEBUG) {
        System.out.println("重定向：" + ArrayHelper.toString(this.tmpBB) + " -> " + ArrayHelper.toString(this.nameBB));
      }
    }
    this.step = WikiParseStep.BEFORE_TITLE;
    this.invalidate();
  }

  private boolean writeRelated() throws IOException {
    if ((this.outRelated != null) && !this.relatedWords.isEmpty()) {
      if (WikiExtractorBase.DEBUG) {
        System.out.println(ArrayHelper.toString(this.nameBB) + "：写出" + this.relatedWords.size() + "个相关词汇。");
      }
      this.outRelated.write(this.nameBB.array(), 0, this.nameBB.limit());
      this.outRelated.write(Helper.SEP_DEFINITION_BYTES);
      boolean first = true;
      for (final byte[] w : this.relatedWords) {
        if (first) {
          first = false;
        } else {
          this.outRelated.write(Helper.SEP_WORDS_BYTES);
        }

        this.outRelated.write(w);
      }
      this.outRelated.write(Helper.SEP_NEWLINE_CHAR);
      return true;
    } else {
      return false;
    }
  }
}
