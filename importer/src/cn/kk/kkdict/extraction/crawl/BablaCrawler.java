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
package cn.kk.kkdict.extraction.crawl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import cn.kk.kkdict.Configuration;
import cn.kk.kkdict.Configuration.Source;
import cn.kk.kkdict.beans.DictByteBufferRow;
import cn.kk.kkdict.types.Category;
import cn.kk.kkdict.types.Example;
import cn.kk.kkdict.types.Gender;
import cn.kk.kkdict.types.Language;
import cn.kk.kkdict.types.Related;
import cn.kk.kkdict.types.Synonym;
import cn.kk.kkdict.types.TranslationSource;
import cn.kk.kkdict.types.UriLocation;
import cn.kk.kkdict.types.Usage;
import cn.kk.kkdict.types.WordType;
import cn.kk.kkdict.utils.ArrayHelper;
import cn.kk.kkdict.utils.Helper;

/**
 * TODO: http://en.bab.la/tools/similarWords.php?l1=test&l2=test&language=EnEs
 * http://en.bab.la/dictionary/english-chinese/worst
 * 
 * <pre>
 * <table id="simWords" cellspacing="0"><tr class="odd"><td>test</td><td>test {m}</td></tr><tr><td>to test</td><td>probar</td></tr><tr class="odd"><td>to test</td><td>graduar {v.t.}</td></tr></table>
 * </pre>
 * 
 * <pre>
 * 
 * 翻译开始：">Results:"
 * 翻译结束："id=\"babAdBottom\""
 * 
 * 类开始："<h3>", "<span class="babFlag babFlag-uk"></span>worst<span class="muted"> {adjective}</span></h3>"
 * 类结束：""
 * <div class="babH4Gr babR">Results: 1-7 of 7</div><div class="babB"></div><div class="bab5"></div><h3><span class="babFlag babFlag-uk"></span>worst<span class="muted"> {adjective}</span></h3><a id="tb0"></a><div class="result-block"><div class="result-wrapper">
 * 
 * 解释开始："result-left\"><p>"
 * <div class="span6 result-left"><p><a href="javascript:babSpeakIt('english',134399);" class="tooltipLink" rel="tooltip" data-original-title="Listen"><i class="icon-volume-up"></i></a> <a class="result-link" href="/dictionary/english-chinese/worst"><strong>worst</strong></a><span> {<abbr title="adjective">adj.</abbr>}</span></p></div>
 * <div class="span6 result-right row-fluid"> <a class="result-link" href="/dictionary/chinese-english/%E6%9C%80-%E5%9D%8F-%E7%9A%84">最坏的</a><span> [zuì huài de] {<abbr title="adjective">adj.</abbr>}</span></div>
 * 
 * <div class="span6 result-left"><p><a href="javascript:babSpeakIt('german',189238);" class="tooltipLink" rel="tooltip" data-original-title="Listen"><i class="icon-volume-up"></i></a> <a class="result-link" href="/dictionary/german-english/titel"><strong>Titel</strong></a><span> {<abbr title="noun">noun</abbr>}<span> (also: Bezeichnungen, Namen, Überschriften, Sachtitel)</span></span></p></div>
 * <div class="span6 result-right row-fluid"><a href="javascript:babSpeakIt('english',406582);" class="tooltipLink" rel="tooltip" data-original-title="Listen"><i class="icon-volume-up"></i></a> <a class="result-link" href="/dictionary/english-german/titles">titles</a><span></span></div>
 * 
 * 
 * 例子开始："result-left"><p "
 * <div class="span6 result-left"><p class="babCSColor">Hope for the best, but prepare for the <b>worst</b>.</p></div>
 * <div class="span6 result-right row-fluid babCSColor"><ul class="nav result-entry-menu pull-right"><li><a href="/dictionary/english-chinese/" class="tooltipLink" rel="tooltip" data-original-title="Source - bab.la" target="_blank" ><i class="icon-share-alt"></i></a></li></ul>抱最好的愿望,做<b>最坏的</b>打算。</div>
 * 
 * <div id="babCS113235" class="collapse result-more-wrapper"><div class="row-fluid result-row-more"><div class="span6 result-left"><p class="babCSColor"><b>Titel</b> und Opticals</p></div><div class="span6 result-right row-fluid babCSColor"><b>titles</b> and opticals</div></div><div class="row-fluid result-row-more"><div class="span6 result-left"><p class="babCSColor">Hier erhalten Sie einen Dialog zum Eingeben und Ändern der Beschriftung für die <b>Titel</b>.</p></div><div class="span6 result-right row-fluid babCSColor"><ul class="nav result-entry-menu pull-right"><li><a href="/dictionary/german-english/" class="tooltipLink" rel="tooltip" data-original-title="Source - bab.la" target="_blank" ><i class="icon-share-alt"></i></a></li></ul>Opens a dialog to enter or modify the <b>titles</b> in a chart.</div></div><div class="row-fluid result-row-more"><div class="span6 result-left"><p class="babCSColor">Hiermit ändern Sie die Eigenschaften des markierten Titels oder aller <b>Titel</b> gemeinsam.</p></div><div class="span6 result-right row-fluid babCSColor"><ul class="nav result-entry-menu pull-right"><li><a href="/dictionary/german-english/" class="tooltipLink" rel="tooltip" data-original-title="Source - bab.la" target="_blank" ><i class="icon-share-alt"></i></a></li></ul>Modifies the properties of the selected title or of all <b>titles</b> together.</div></div></div></div>
 * 
 * 同义词开始："<h4 class=\"section-block-head\">Synonyms</h4>"
 * <h2 class="babH2Gr">Synonyms (English) for "worst":</h2><div class="result-block"><div class="result-wrapper"><p><span class="babR"><a href="http://wordnet.princeton.edu" class="muted" rel="nofollow" target="_blank">&copy;&nbsp;Princeton University</a></span><a href="/dictionary/english-chinese/pip">pip</a>&nbsp· <a href="/dictionary/english-chinese/mop-up">mop up</a>&nbsp· <a href="/dictionary/english-chinese/whip">whip</a>&nbsp· <a href="/dictionary/english-chinese/rack-up">rack up</a></p></div></div></section>
 * <h2 class="babH2Gr">Synonyms (German) for "Titel":</h2><div class="result-block"><div class="result-wrapper"><p><span class="babR"><a href="http://www.openthesaurus.de" class="muted" rel="nofollow" target="_blank">&copy;&nbsp;OpenThesaurus.de</a></span><a href="/dictionary/german-english/lied">Lied</a>&nbsp· <a href="/dictionary/german-english/musikstueck">Musikstück</a>&nbsp· <a href="/dictionary/german-english/song">Song</a>&nbsp· <a href="/dictionary/german-english/stueck">Stück</a>&nbsp· <a href="/dictionary/german-english/ueberschrift">Überschrift</a>&nbsp· <a href="/dictionary/german-english/kopfzeile">Kopfzeile</a>&nbsp· <a href="/dictionary/german-english/bezeichnung">Bezeichnung</a>&nbsp· <a href="/dictionary/german-english/bezeichner">Bezeichner</a>&nbsp· <a href="/dictionary/german-english/name">Name</a>&nbsp· <a href="/dictionary/german-english/buch">Buch</a>&nbsp· <a href="/dictionary/german-english/lektuere">Lektüre</a>&nbsp· <a href="/dictionary/german-english/schinken">Schinken</a>&nbsp· <a href="/dictionary/german-english/schmoeker">Schmöker</a>&nbsp· <a href="/dictionary/german-english/band">Band</a>&nbsp· <a href="/dictionary/german-english/bd">Bd.</a>&nbsp· <a href="/dictionary/german-english/amtstitel">Amtstitel</a>&nbsp· <a href="/dictionary/german-english/anrede">Anrede</a>&nbsp· <a href="/dictionary/german-english/berufstitel">Berufstitel</a></p></div></div></section>
 *
 * 例句开始："<h4 class=\"section-block-head\">Usage examples</h4>"
 * <div class="span6 result-left"><p><a href="/dictionary/english-chinese/things-at-the-worst-will-mend" class="muted-link">Things at the <b>worst</b> will mend.</a></p></div>
 * <div class="span6 result-right row-fluid"><span class="span9"><a href="/dictionary/chinese-english/否极泰来" class="muted-link">否极泰来。</a></span><ul class="nav result-entry-menu pull-right"><li><a href="/dictionary/english-chinese/" class="tooltipLink" rel="tooltip" data-original-title="Source - bab.la" target="_blank"><i class="icon-share-alt"></i></a></li></ul></div>
 * 
 * <div class="span6 result-left"><p>Worum geht es bei diesem komplizierten <b>Titel</b>?</p></div>
 * <div class="span6 result-right row-fluid"><span class="span9">Exactly what is it all about?</span><ul class="nav result-entry-menu pull-right"><li><a href="http://www.europarl.europa.eu/" class="tooltipLink" rel="tooltip" data-original-title="Source - European Parliament" target="_blank" ><i class="icon-share-alt"></i></a></li></ul></div>
 * </pre>
 */
public class BablaCrawler {
  public static final String                 IN_DIR           = Configuration.IMPORTER_FOLDER_SELECTED_WORDS.getPath(Source.WORD_BABLA);

  public static final String                 IN_STATUS        = Configuration.IMPORTER_FOLDER_SELECTED_WORDS.getFile(Source.WORD_BABLA,
                                                                  "babla_extractor_status.txt");

  public static final String                 OUT_DIR          = Configuration.IMPORTER_FOLDER_EXTRACTED_CRAWLED.getPath(Source.WORD_BABLA);

  public static final String                 OUT_DIR_FINISHED = BablaCrawler.OUT_DIR + "/finished";

  private static final String                URL              = "http://en.bab.la";

  private static final boolean               DEBUG            = false;

  private static final Map<String, WordType> WORD_TYPES_MAP   = new HashMap<>();

  private static final Map<String, Gender>   GENDER_MAP       = new HashMap<>();

  private static final Map<String, Usage>    USAGE_MAP        = new HashMap<>();

  private static final String                SUFFIX_EXAMPLES  = "_examples";

  private static final String                SUFFIX_RELATED   = "_related";

  private static final String                SUFFIX_SYNONYM   = "_synonym";

  static {
    BablaCrawler.WORD_TYPES_MAP.put("noun", WordType.NOUN);
    BablaCrawler.WORD_TYPES_MAP.put("verb", WordType.VERB);
    BablaCrawler.WORD_TYPES_MAP.put("adjective", WordType.ADJECTIVE);
    BablaCrawler.WORD_TYPES_MAP.put("adverb", WordType.ADVERB);
    BablaCrawler.WORD_TYPES_MAP.put("preposition", WordType.PREPOSITION);
    BablaCrawler.WORD_TYPES_MAP.put("conjunction", WordType.CONJUNCTION);
    BablaCrawler.WORD_TYPES_MAP.put("pronoun", WordType.PRONOUN);
    BablaCrawler.WORD_TYPES_MAP.put("interjection", WordType.INTERJECTION);
    BablaCrawler.WORD_TYPES_MAP.put("article", WordType.ARTICLE);
    BablaCrawler.WORD_TYPES_MAP.put("numeral", WordType.NUMERAL);
    BablaCrawler.WORD_TYPES_MAP.put("particle", WordType.PARTICLE);
    BablaCrawler.WORD_TYPES_MAP.put("contraction", WordType.CONTRACTION);

    BablaCrawler.WORD_TYPES_MAP.put("only singular", WordType.SINGULAR);
    BablaCrawler.WORD_TYPES_MAP.put("plural", WordType.PLURAL);
    BablaCrawler.WORD_TYPES_MAP.put("only plural", WordType.PLURAL);
    BablaCrawler.WORD_TYPES_MAP.put("proper noun", WordType.PROPER_NOUN);

    BablaCrawler.WORD_TYPES_MAP.put("transitive verb", WordType.VERB_TRANSITIVE);
    BablaCrawler.WORD_TYPES_MAP.put("intransitive verb", WordType.VERB_INTRANSITIVE);
    BablaCrawler.WORD_TYPES_MAP.put("reflexive verb", WordType.VERB_REFLEXIVE);
    BablaCrawler.WORD_TYPES_MAP.put("past participle", WordType.VERB_PAST_PARTICIPLE);
    BablaCrawler.WORD_TYPES_MAP.put("gerund", WordType.VERB_GERUND);

    BablaCrawler.WORD_TYPES_MAP.put("comparative", WordType.AD_COMPARATIVE);
    BablaCrawler.WORD_TYPES_MAP.put("superlative", WordType.AD_SUPERLATIVE);

    BablaCrawler.WORD_TYPES_MAP.put("abbreviation", WordType.ABBREVIATION);
    BablaCrawler.WORD_TYPES_MAP.put("proverb", WordType.PROVERB);
    BablaCrawler.WORD_TYPES_MAP.put("idiom", WordType.IDIOM);
    BablaCrawler.WORD_TYPES_MAP.put("compound word", WordType.COMPOUND_WORD);
    BablaCrawler.WORD_TYPES_MAP.put("example", WordType.EXAMPLE);

    BablaCrawler.GENDER_MAP.put("masculine", Gender.MASCULINE);
    BablaCrawler.GENDER_MAP.put("feminine", Gender.FEMININE);
    BablaCrawler.GENDER_MAP.put("neuter", Gender.NEUTER);

    BablaCrawler.USAGE_MAP.put("archaic", Usage.OBSOLETE);
    BablaCrawler.USAGE_MAP.put("children's language", Usage.CHILDRENS);
    BablaCrawler.USAGE_MAP.put("colloquial", Usage.COLLOQUIAL);
    BablaCrawler.USAGE_MAP.put("dialect", Usage.DIALECT);
    BablaCrawler.USAGE_MAP.put("diminutive", Usage.DIMINUTIVE);
    BablaCrawler.USAGE_MAP.put("elevated", Usage.ELEVATED);
    BablaCrawler.USAGE_MAP.put("familiar", Usage.FAMILIAR);
    BablaCrawler.USAGE_MAP.put("figurative", Usage.FIGURATIVE);
    BablaCrawler.USAGE_MAP.put("formal", Usage.FORMAL);
    BablaCrawler.USAGE_MAP.put("humble", Usage.HUMBLE);
    BablaCrawler.USAGE_MAP.put("humorous", Usage.HUMOROUS);
    BablaCrawler.USAGE_MAP.put("ironical", Usage.METAPHORICAL);
    BablaCrawler.USAGE_MAP.put("literal", Usage.FORMAL);
    BablaCrawler.USAGE_MAP.put("obsolete", Usage.OBSOLETE);
    BablaCrawler.USAGE_MAP.put("old spelling", Usage.OBSOLETE);
    BablaCrawler.USAGE_MAP.put("old-fashioned", Usage.OBSOLETE);
    BablaCrawler.USAGE_MAP.put("pejorative", Usage.PEJORATIVE);
    BablaCrawler.USAGE_MAP.put("poetic", Usage.POETIC);
    BablaCrawler.USAGE_MAP.put("polite", Usage.POLITE);
    BablaCrawler.USAGE_MAP.put("rare", Usage.RARE);
    BablaCrawler.USAGE_MAP.put("respectful", Usage.RESPECTFUL);
    BablaCrawler.USAGE_MAP.put("slang", Usage.SLANG);
    BablaCrawler.USAGE_MAP.put("taboo", Usage.TABOO);
    BablaCrawler.USAGE_MAP.put("vulgar", Usage.VULGAR);
  }

  private static final Map<String, Category> CAT_MAPPER       = new TreeMap<>();

  static {
    final File bablaCategories = Helper.findResource("babla_categories.txt");
    System.out.print("导入类型文件：" + bablaCategories.getAbsolutePath() + " ... ");
    int counter = 0;
    try (final BufferedReader reader = new BufferedReader(new FileReader(bablaCategories));) {
      String line;
      while (null != (line = BablaCrawler.safeReadLine(reader))) {
        final String[] parts = line.split("=");
        if (parts.length == 2) {
          if (Helper.isNotEmptyOrNull(parts[0]) && Helper.isNotEmptyOrNull(parts[1])) {
            if (BablaCrawler.DEBUG) {
              System.out.println("类：" + parts[1] + " -> " + Category.valueOf(parts[0]).key);
            }
            BablaCrawler.CAT_MAPPER.put(parts[1], Category.valueOf(parts[0]));
            counter++;
          } else {
            if (BablaCrawler.DEBUG) {
              System.out.println("类：" + parts[1] + " -> null");
            }
            BablaCrawler.CAT_MAPPER.put(parts[1], null);
          }
        }
      }
      System.out.println(counter);
    } catch (final Exception e) {
      System.err.println("导入错误：" + e.toString());
    }
  }

  public BablaCrawler() {
    new File(BablaCrawler.OUT_DIR_FINISHED).mkdirs();
  }

  public static void main(final String[] args) throws IOException {
    final BablaCrawler extractor = new BablaCrawler();
    BablaCrawler.extract();
  }

  public static void extract() throws IOException {
    final File directory = new File(BablaCrawler.IN_DIR);
    if (directory.isDirectory()) {
      System.out.print("搜索babla词组文件'" + BablaCrawler.IN_DIR + "' ... ");

      final File[] files = directory.listFiles(new FilenameFilter() {
        @Override
        public boolean accept(final File dir, final String name) {
          return name.endsWith("." + TranslationSource.BABLA.key) && name.startsWith("words_");
        }
      });
      System.out.println(files.length);

      long total = 0;
      for (final File f : files) {
        final long start = System.currentTimeMillis();
        final int skipLines = (int) Helper.readStatsFile(BablaCrawler.IN_STATUS);
        System.out.print("分析'" + f + " [" + skipLines + "] ... ");
        final File outFile = new File(BablaCrawler.OUT_DIR, f.getName());
        final File outFileExamples = new File(BablaCrawler.OUT_DIR, Helper.appendFileName(f.getName(), BablaCrawler.SUFFIX_EXAMPLES));
        final File outFileRelated = new File(BablaCrawler.OUT_DIR, Helper.appendFileName(f.getName(), BablaCrawler.SUFFIX_RELATED));
        final File outFileSynonyms = new File(BablaCrawler.OUT_DIR, Helper.appendFileName(f.getName(), BablaCrawler.SUFFIX_SYNONYM));
        if (BablaCrawler.DEBUG) {
          System.out.println("写出：" + outFile + "（同义词： " + outFileRelated + "，相关词：" + outFileExamples + "） 。。。");
        }
        int counter = 0;
        try (final BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outFile, skipLines > 0), Helper.BUFFER_SIZE);
            final BufferedOutputStream outRelated = new BufferedOutputStream(new FileOutputStream(outFileRelated, skipLines > 0), Helper.BUFFER_SIZE);
            final BufferedOutputStream outSynonyms = new BufferedOutputStream(new FileOutputStream(outFileSynonyms, skipLines > 0), Helper.BUFFER_SIZE);
            final BufferedOutputStream outExamples = new BufferedOutputStream(new FileOutputStream(outFileExamples, skipLines > 0), Helper.BUFFER_SIZE);) {

          int retries = 0;
          boolean success = false;
          while ((retries++ < 5) && !success) {
            try {
              counter = BablaCrawler.crawl(f, out, outRelated, outExamples, outSynonyms, skipLines);

              success = true;
            } catch (final Throwable e) {
              e.printStackTrace();
              try {
                Thread.sleep(10 * 1000 * retries);
              } catch (final InterruptedException e1) {
                // ignore
              }
            }
          }

        }
        System.out.println(counter + "，用时：" + Helper.formatDuration(System.currentTimeMillis() - start));
        total += counter;
        f.renameTo(new File(BablaCrawler.OUT_DIR_FINISHED, f.getName()));
        Helper.writeStatsFile(BablaCrawler.IN_STATUS, 0L);
      }

      System.out.println("\n=====================================");
      System.out.println("成功读取了" + files.length + "个termwiki文件");
      System.out.println("总共单词：" + total);
      System.out.println("=====================================");
    }
  }

  private static enum State {
    PARSE,
    PARSE_EXAMPLE,
    PARSE_DEFINITION,
    PARSE_SYNONYM
  }

  private static int crawl(final File f, final BufferedOutputStream out, final BufferedOutputStream outRels, final BufferedOutputStream outExamples,
      BufferedOutputStream outSynonyms, final int skipLines) throws IOException {
    int toSkip = skipLines;
    if (toSkip < 0) {
      toSkip = 0;
    }
    int count = toSkip;
    final String[] lngs = f.getName().substring("words_".length(), f.getName().length() - ".babla".length()).split("_");
    final Language srcLng = Language.fromKey(lngs[0]);
    final Language tgtLng = Language.fromKey(lngs[1]);
    try (final BufferedInputStream in = new BufferedInputStream(new FileInputStream(f), Helper.BUFFER_SIZE);) {
      final ByteBuffer lineBB = ArrayHelper.borrowByteBufferSmall();
      final DictByteBufferRow row = new DictByteBufferRow();
      while (-1 != ArrayHelper.readLineTrimmed(in, lineBB)) {
        if (toSkip == 0) {
          row.parseFrom(lineBB);
          if (row.size() == 1) {
            // final Language srcLng = Language.fromKey(ArrayHelper.toStringP(row.getLanguage(0)));
            // final Language tgtLng = Language.fromKey(ArrayHelper.toStringP(row.getLanguage(1)));
            final String name = ArrayHelper.toStringP(row.getValue(0, 0));
            final byte[] nameBytes = ArrayHelper.toBytesP(row.getValue(0, 0));
            String path = ArrayHelper.toStringP(row.getFirstAttributeValue(0, 0, UriLocation.TYPE_ID_BYTES));
            final int sep = path.lastIndexOf('/');
            path = path.substring(0, sep + 1) + URLEncoder.encode(path.substring(sep + 1), Helper.CHARSET_UTF8.name());
            final Category cat = Category.fromKey(ArrayHelper.toStringP(row.getFirstAttributeValue(0, 0, Category.TYPE_ID_BYTES)));
            if (BablaCrawler.DEBUG) {
              System.out.println("语言：" + srcLng.getKey() + "，单词：" + name + "，地址：" + path + (cat != null ? "，类别：" + cat.key : Helper.EMPTY_STRING));
            }

            // this.parseMainHtml(out, outExamples, outSyms, srcLng, tgtLng, nameBytes, path);

            int retries = 0;
            boolean success = false;
            while ((retries++ < 5) && !success) {
              try {
                BablaCrawler.parseMainHtml(out, outExamples, outRels, outSynonyms, srcLng, tgtLng, nameBytes, path);
                System.out.print(".");
                success = true;
              } catch (Exception e) {
                if (retries > 3) {
                  e.printStackTrace();
                }
                System.err.println("错误：" + e.toString());
                try {
                  TimeUnit.SECONDS.sleep(10 * retries);
                } catch (InterruptedException e1) {
                  // silent
                }
              }
            }
            count++;
            if ((count > 0) && ((count % 10) == 0)) {
              out.flush();
              outExamples.flush();
              outSynonyms.flush();
              outRels.flush();
              Helper.writeStatsFile(BablaCrawler.IN_STATUS, count);
            }
          }
        } else {
          toSkip--;
        }
      }
      ArrayHelper.giveBack(lineBB);
    }
    return count;
  }

  // private Map<String, String> parseMainHtml(final BufferedOutputStream out, BufferedOutputStream outExamples, BufferedOutputStream outSyms, final Language
  // lng,
  // Language tgtLng, final byte[] nameBytes, final String path) throws MalformedURLException, IOException {
  //
  // String urlPath = BablaCrawler.URL + path;
  // try (final BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("d:\\temp\\苹果.htm"), Helper.CHARSET_UTF8));) {
  // return this.parseMainHtml(reader, out, outExamples, outSyms, lng, tgtLng, nameBytes);
  // }
  // }

  private static Map<String, String> parseMainHtml(final BufferedOutputStream out, BufferedOutputStream outExamples, BufferedOutputStream outRels,
      BufferedOutputStream outSynonyms, final Language lng, Language tgtLng, final byte[] nameBytes, final String path) throws MalformedURLException,
      IOException {
    Helper.putConnectionHeader("X-Requested-With", null);
    Helper.putConnectionHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
    Helper.putConnectionHeader("Content-Type", null);
    // HttpURLConnection.setFollowRedirects(true);

    String urlPath = BablaCrawler.URL + path;
    final HttpURLConnection conn = Helper.getUrlConnection(urlPath);
    // System.out.println("Connecting to: " + urlPath);
    conn.connect();
    try (final BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), Helper.CHARSET_UTF8));) {
      return BablaCrawler.parseMainHtml(reader, out, outExamples, outRels, outSynonyms, lng, tgtLng, nameBytes);
    } finally {
      conn.disconnect();
    }
  }

  private static Map<String, String> parseMainHtml(BufferedReader reader, BufferedOutputStream out, BufferedOutputStream outExamples,
      BufferedOutputStream outRelated, BufferedOutputStream outSynonyms, Language lng, Language tgtLng, byte[] nameBytes) throws IOException {
    final Map<String, String> params = new HashMap<>();

    String line;
    State state = State.PARSE;
    final StringBuffer sb = new StringBuffer();

    String srcVal = null;
    WordType srcWordType = null;
    Category srcCategory = null;
    Set<String> srcExamples = new HashSet<>();
    Set<String> srcRelatives = new HashSet<>();
    Set<String> srcSynonyms = new HashSet<>();
    String tgtVal = null;
    Category tgtCategory = null;
    WordType tgtWordType = null;

    while (null != (line = BablaCrawler.safeReadLine(reader))) {
      if (line.contains("Sorry, no exact translations found.")) {
        throw new RuntimeException("No translation found: " + ArrayHelper.toString(nameBytes));
      } else if (line.contains(">Results:")) {
        state = State.PARSE_DEFINITION;
      } else if (state == State.PARSE_EXAMPLE) {
        if (line.contains("</section>")) {
          state = State.PARSE;
        }
      }

      if (line.contains(" result-left\"><p ") || line.contains("Usage examples</h4>")) {
        state = State.PARSE_EXAMPLE;
      } else if (line.contains("Synonyms</h4>")) {
        state = State.PARSE_SYNONYM;
      }

      if (state == State.PARSE) {
        continue;
      }
      switch (state) {
        case PARSE_DEFINITION:
          if (line.contains(" result-left")) {
            srcVal = Helper.substringBetweenNarrow(line, ">", "</strong></a><span>");
            srcWordType = BablaCrawler.parseWordType(line);
            srcCategory = BablaCrawler.parseCategory(line);
            BablaCrawler.parseAlso(srcRelatives, line);

          } else if (line.contains(" result-right")) {
            tgtVal = Helper.substringBetweenNarrow(line, ">", "</a><span>");
            tgtWordType = BablaCrawler.parseWordType(line);
            tgtCategory = BablaCrawler.parseCategory(line);
            if ((srcVal != null) && (tgtVal != null)) {
              if (BablaCrawler.DEBUG) {
                System.out.println("def: " + srcVal);
                // System.out.println("srcVal: " + srcVal);
                // System.out.println("srcWordType: " + srcWordType);
                // System.out.println("srcCategory: " + srcCategory);
                // System.out.println("tgtVal: " + tgtVal);
                // System.out.println("tgtWordType: " + tgtWordType);
                // System.out.println("tgtCategory: " + tgtCategory);
              }

              sb.append(lng.getKey());
              sb.append(Helper.SEP_DEFINITION);
              sb.append(srcVal);
              if (srcCategory != null) {
                sb.append(Helper.SEP_ATTRIBUTE);
                sb.append(Category.TYPE_ID);
                sb.append(srcCategory.key);
              }
              if (srcWordType != null) {
                sb.append(Helper.SEP_ATTRIBUTE);
                sb.append(WordType.TYPE_ID);
                sb.append(srcWordType.key);
              }
              if (srcCategory != null) {
                sb.append(Helper.SEP_ATTRIBUTE);
                sb.append(Category.TYPE_ID);
                sb.append(srcCategory.key);
              }
              sb.append(Helper.SEP_LIST);
              sb.append(tgtLng.getKey());
              sb.append(Helper.SEP_DEFINITION);
              sb.append(tgtVal);
              if (tgtCategory != null) {
                sb.append(Helper.SEP_ATTRIBUTE);
                sb.append(Category.TYPE_ID);
                sb.append(tgtCategory.key);
              }
              if (tgtWordType != null) {
                sb.append(Helper.SEP_ATTRIBUTE);
                sb.append(WordType.TYPE_ID);
                sb.append(tgtWordType.key);
              }
              if (tgtCategory != null) {
                sb.append(Helper.SEP_ATTRIBUTE);
                sb.append(Category.TYPE_ID);
                sb.append(tgtCategory.key);
              }
              sb.append(Helper.SEP_NEWLINE_CHAR);
              out.write(sb.toString().getBytes(Helper.CHARSET_UTF8));
            }

            // clear
            srcVal = null;
            srcWordType = null;
            srcCategory = null;
            tgtVal = null;
            tgtWordType = null;
            tgtCategory = null;
          }
          break;
        case PARSE_EXAMPLE:
          if (line.contains(" result-left")) {
            String rel = Helper.substringBetweenNarrow(line, "muted-link\">", "</a>");
            if (Helper.isEmptyOrNull(rel)) {
              rel = Helper.substringBetweenNarrow(line, "<p>", "</p></div>");
            }
            if (Helper.isEmptyOrNull(rel)) {
              rel = Helper.substringBetweenNarrow(line, "\">", "</p></div>");
            }
            if (Helper.isNotEmptyOrNull(rel)) {
              if (!rel.contains("<span>")) {
                rel = rel.replaceAll("(\\(.+\\))?", "").replaceAll("(（.+）)?", "");
                // System.out.println(rel);
                if (rel.contains("<b>")) {
                  rel = rel.replace("<b>", "").replace("</b>", "");
                  srcExamples.add(rel);
                } else {
                  srcRelatives.add(rel);
                }
              }
            }
          }
          break;
        case PARSE_SYNONYM:
          final String lngSyn = Helper.substringBetween(line, "Synonyms (", ")");
          if (Helper.isNotEmptyOrNull(lngSyn)) {
            srcVal = Helper.substringBetween(line, " for \"", "\":");
            final StringTokenizer st = new StringTokenizer(line, "·");
            while (st.hasMoreTokens()) {
              String syn = Helper.substringBetweenLast(st.nextToken(), ">", "</a>&nbsp");
              if (Helper.isNotEmptyOrNull(syn)) {
                srcSynonyms.add(syn);
                // srcAlsos.add(syn);
              }
            }
            break;
          }
          break;
        case PARSE:
          // silent
          break;
        default:
          // silent
      }
    }

    if (!srcRelatives.isEmpty()) {
      outRelated.write(lng.getKeyBytes());
      outRelated.write(Helper.SEP_DEFINITION_BYTES);
      outRelated.write(nameBytes);
      outRelated.write(Helper.SEP_ATTRS_BYTES);
      outRelated.write(Related.TYPE_ID_BYTES);
      boolean first = true;
      for (final String s : srcRelatives) {
        if (first) {
          first = false;
        } else {
          outRelated.write(Helper.SEP_WORDS_BYTES);
        }
        outRelated.write(s.getBytes(Helper.CHARSET_UTF8));
      }
      outRelated.write(Helper.SEP_NEWLINE_BYTES);
    }

    if (!srcExamples.isEmpty()) {
      outExamples.write(lng.getKeyBytes());
      outExamples.write(Helper.SEP_DEFINITION_BYTES);
      outExamples.write(nameBytes);
      outExamples.write(Helper.SEP_ATTRS_BYTES);
      outExamples.write(Example.TYPE_ID_BYTES);
      boolean first = true;
      for (final String s : srcExamples) {
        if (first) {
          first = false;
        } else {
          outExamples.write(Helper.SEP_WORDS_BYTES);
        }
        outExamples.write(s.getBytes(Helper.CHARSET_UTF8));
      }
      outExamples.write(Helper.SEP_NEWLINE_BYTES);
    }

    if (!srcSynonyms.isEmpty()) {
      outSynonyms.write(lng.getKeyBytes());
      outSynonyms.write(Helper.SEP_DEFINITION_BYTES);
      outSynonyms.write(nameBytes);
      outSynonyms.write(Helper.SEP_ATTRS_BYTES);
      outSynonyms.write(Synonym.TYPE_ID_BYTES);
      boolean first = true;
      for (final String s : srcSynonyms) {
        if (first) {
          first = false;
        } else {
          outSynonyms.write(Helper.SEP_WORDS_BYTES);
        }
        outSynonyms.write(s.getBytes(Helper.CHARSET_UTF8));
      }
      outSynonyms.write(Helper.SEP_NEWLINE_BYTES);
    }
    return params;

  }

  private static void parseAlso(Collection<String> alsoSet, String line) {
    final String alsoText = Helper.substringBetweenNarrow(line, " (also: ", ")");
    if (alsoText != null) {
      StringTokenizer st = new StringTokenizer(alsoText, ", ");

      while (st.hasMoreTokens()) {
        String token = st.nextToken();
        alsoSet.add(token);
      }
    }
  }

  private static WordType parseWordType(String line) {
    WordType wordType = null;
    for (String key : BablaCrawler.WORD_TYPES_MAP.keySet()) {
      if (line.contains("{<abbr title=\"" + key + "\"")) {
        wordType = BablaCrawler.WORD_TYPES_MAP.get(key);
        break;
      }
    }
    return wordType;
  }

  private static Category parseCategory(String line) {
    Category cat = null;
    for (String key : BablaCrawler.CAT_MAPPER.keySet()) {
      if (line.contains("[<abbr title=\"" + key + "\"") || line.contains("[<abbr title=\"" + key + "&")) {
        cat = BablaCrawler.CAT_MAPPER.get(key);
        break;
      }
    }
    return cat;
  }

  private static String safeReadLine(final BufferedReader reader) throws IOException {
    try {
      return reader.readLine();
    } catch (final IOException e) {
      throw e;
    }
  }
}
