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
import java.net.URL;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import cn.kk.kkdict.Configuration;
import cn.kk.kkdict.Configuration.Source;
import cn.kk.kkdict.beans.DictByteBufferRow;
import cn.kk.kkdict.types.Abstract;
import cn.kk.kkdict.types.Category;
import cn.kk.kkdict.types.Gender;
import cn.kk.kkdict.types.Language;
import cn.kk.kkdict.types.TranslationSource;
import cn.kk.kkdict.types.UriLocation;
import cn.kk.kkdict.types.Usage;
import cn.kk.kkdict.types.WordType;
import cn.kk.kkdict.utils.ArrayHelper;
import cn.kk.kkdict.utils.ChineseHelper;
import cn.kk.kkdict.utils.DictHelper;
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
    public static final String IN_DIR = Configuration.IMPORTER_FOLDER_SELECTED_WORDS.getPath(Source.WORD_BABLA);

    public static final String IN_STATUS = Configuration.IMPORTER_FOLDER_SELECTED_WORDS.getFile(Source.WORD_BABLA,
            "babla_extractor_status.txt");

    public static final String OUT_DIR = Configuration.IMPORTER_FOLDER_EXTRACTED_CRAWLED.getPath(Source.WORD_BABLA);

    public static final String OUT_DIR_FINISHED = OUT_DIR + "/finished";

    private static final String URL = "http://en.bab.la/";

    private static final boolean DEBUG = false;

    private static final Map<String, WordType> WORD_TYPES_MAP = new HashMap<String, WordType>();

    private static final Map<String, Gender> GENDER_MAP = new HashMap<String, Gender>();

    private static final Map<String, Usage> USAGE_MAP = new HashMap<String, Usage>();

    private static final String SUFFIX_EXAMPLES = "_examples";

    private static final String SUFFIX_RELATED = "_related";
    static {
        WORD_TYPES_MAP.put("noun", WordType.NOUN);
        WORD_TYPES_MAP.put("verb", WordType.VERB);
        WORD_TYPES_MAP.put("adjective", WordType.ADJECTIVE);
        WORD_TYPES_MAP.put("adverb", WordType.ADVERB);
        WORD_TYPES_MAP.put("preposition", WordType.PREPOSITION);
        WORD_TYPES_MAP.put("conjunction", WordType.CONJUNCTION);
        WORD_TYPES_MAP.put("pronoun", WordType.PRONOUN);
        WORD_TYPES_MAP.put("interjection", WordType.INTERJECTION);
        WORD_TYPES_MAP.put("article", WordType.ARTICLE);
        WORD_TYPES_MAP.put("numeral", WordType.NUMERAL);
        WORD_TYPES_MAP.put("particle", WordType.PARTICLE);
        WORD_TYPES_MAP.put("contraction", WordType.CONTRACTION);

        WORD_TYPES_MAP.put("only singular", WordType.SINGULAR);
        WORD_TYPES_MAP.put("plural", WordType.PLURAL);
        WORD_TYPES_MAP.put("only plural", WordType.PLURAL);
        WORD_TYPES_MAP.put("proper noun", WordType.PROPER_NOUN);

        WORD_TYPES_MAP.put("transitive verb", WordType.VERB_TRANSITIVE);
        WORD_TYPES_MAP.put("intransitive verb", WordType.VERB_INTRANSITIVE);
        WORD_TYPES_MAP.put("reflexive verb", WordType.VERB_REFLEXIVE);
        WORD_TYPES_MAP.put("past participle", WordType.VERB_PAST_PARTICIPLE);
        WORD_TYPES_MAP.put("gerund", WordType.VERB_GERUND);

        WORD_TYPES_MAP.put("comparative", WordType.AD_COMPARATIVE);
        WORD_TYPES_MAP.put("superlative", WordType.AD_SUPERLATIVE);

        WORD_TYPES_MAP.put("abbreviation", WordType.ABBREVIATION);
        WORD_TYPES_MAP.put("proverb", WordType.PROVERB);
        WORD_TYPES_MAP.put("idiom", WordType.IDIOM);
        WORD_TYPES_MAP.put("compound word", WordType.COMPOUND_WORD);
        WORD_TYPES_MAP.put("example", WordType.EXAMPLE);

        GENDER_MAP.put("masculine", Gender.MASCULINE);
        GENDER_MAP.put("feminine", Gender.FEMININE);
        GENDER_MAP.put("neuter", Gender.NEUTER);

        USAGE_MAP.put("archaic", Usage.OBSOLETE);
        USAGE_MAP.put("children's language", Usage.CHILDRENS);
        USAGE_MAP.put("colloquial", Usage.COLLOQUIAL);
        USAGE_MAP.put("dialect", Usage.DIALECT);
        USAGE_MAP.put("diminutive", Usage.DIMINUTIVE);
        USAGE_MAP.put("elevated", Usage.ELEVATED);
        USAGE_MAP.put("familiar", Usage.FAMILIAR);
        USAGE_MAP.put("figurative", Usage.FIGURATIVE);
        USAGE_MAP.put("formal", Usage.FORMAL);
        USAGE_MAP.put("humble", Usage.HUMBLE);
        USAGE_MAP.put("humorous", Usage.HUMOROUS);
        USAGE_MAP.put("ironical", Usage.METAPHORICAL);
        USAGE_MAP.put("literal", Usage.FORMAL);
        USAGE_MAP.put("obsolete", Usage.OBSOLETE);
        USAGE_MAP.put("old spelling", Usage.OBSOLETE);
        USAGE_MAP.put("old-fashioned", Usage.OBSOLETE);
        USAGE_MAP.put("pejorative", Usage.PEJORATIVE);
        USAGE_MAP.put("poetic", Usage.POETIC);
        USAGE_MAP.put("polite", Usage.POLITE);
        USAGE_MAP.put("rare", Usage.RARE);
        USAGE_MAP.put("respectful", Usage.RESPECTFUL);
        USAGE_MAP.put("slang", Usage.SLANG);
        USAGE_MAP.put("taboo", Usage.TABOO);
        USAGE_MAP.put("vulgar", Usage.VULGAR);
    }

    private static final Map<String, Category> CAT_MAPPER = new TreeMap<String, Category>();

    static {
        final File termwikiCategories = Helper.findResource("babla_categories.txt");
        System.out.println("导入类型文件：" + termwikiCategories.getAbsolutePath());
        try {
            BufferedReader reader = new BufferedReader(new FileReader(termwikiCategories));
            String line;
            while (null != (line = safeReadLine(reader))) {
                String[] parts = line.split("=");
                if (parts.length == 2) {
                    if (Helper.isNotEmptyOrNull(parts[0]) && Helper.isNotEmptyOrNull(parts[1])) {
                        if (DEBUG) {
                            System.out.println("类：" + parts[1] + " -> " + Category.valueOf(parts[0]).key);
                        }
                        CAT_MAPPER.put(parts[1].toUpperCase(), Category.valueOf(parts[0]));
                    } else {
                        if (DEBUG) {
                            System.out.println("类：" + parts[1] + " -> null");
                        }
                        CAT_MAPPER.put(parts[1].toUpperCase(), null);
                    }
                }
            }
            reader.close();
        } catch (Exception e) {
            System.err.println("导入错误：" + e.toString());
        }
    }

    private WordType wordType = null;

    public BablaCrawler() {
        new File(OUT_DIR_FINISHED).mkdirs();
    }

    public static void main(String[] args) throws IOException {
        BablaCrawler extractor = new BablaCrawler();
        extractor.extract();
    }

    public void extract() throws IOException {
        File directory = new File(IN_DIR);
        if (directory.isDirectory()) {
            System.out.print("搜索babla词组文件'" + IN_DIR + "' ... ");

            File[] files = directory.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith("." + TranslationSource.BABLA.key) && name.startsWith("words_");
                }
            });
            System.out.println(files.length);

            long total = 0;
            for (File f : files) {
                final long start = System.currentTimeMillis();
                final int skipLines = (int) Helper.readStatsFile(IN_STATUS);
                System.out.print("分析'" + f + " [" + skipLines + "] ... ");
                final File outFile = new File(OUT_DIR, f.getName());
                final File outFileExamples = new File(OUT_DIR, Helper.appendFileName(f.getName(), SUFFIX_EXAMPLES));
                final File outFileSynonyms = new File(OUT_DIR, Helper.appendFileName(f.getName(), SUFFIX_RELATED));
                if (DEBUG) {
                    System.out.println("写出：" + outFile + "（同义词： " + outFileSynonyms + "，相关词：" + outFileExamples
                            + "） 。。。");
                }
                BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outFile, skipLines > 0),
                        Helper.BUFFER_SIZE);
                BufferedOutputStream outSyms = new BufferedOutputStream(new FileOutputStream(outFileSynonyms,
                        skipLines > 0), Helper.BUFFER_SIZE);
                BufferedOutputStream outExamples = new BufferedOutputStream(new FileOutputStream(outFileExamples,
                        skipLines > 0), Helper.BUFFER_SIZE);

                int counter = crawl(f, out, outSyms, outExamples, skipLines);
                out.close();
                outSyms.close();
                outExamples.close();
                System.out.println(counter + "，用时：" + Helper.formatDuration(System.currentTimeMillis() - start));
                total += counter;
                f.renameTo(new File(OUT_DIR_FINISHED, f.getName()));
                Helper.writeStatsFile(IN_STATUS, 0L);
            }

            System.out.println("\n=====================================");
            System.out.println("成功读取了" + files.length + "个termwiki文件");
            System.out.println("总共单词：" + total);
            System.out.println("=====================================");
        }
    }

    private static enum State {
        PARSE, PARSE_DEFINITION, PARSE_DEFINITION_FULL
    }

    private int crawl(final File f, final BufferedOutputStream out, BufferedOutputStream outSyms,
            BufferedOutputStream outExamples, int skipLines) throws IOException {
        if (skipLines < 0) {
            skipLines = 0;
        }
        final String[] lngs = f.getName().substring("words_".length(), f.getName().length() - ".babla".length()).split("_");
        final BufferedInputStream in = new BufferedInputStream(new FileInputStream(f), Helper.BUFFER_SIZE);
        ByteBuffer lineBB = ArrayHelper.borrowByteBufferSmall();
        DictByteBufferRow row = new DictByteBufferRow();
        int count = skipLines;
        while (-1 != ArrayHelper.readLineTrimmed(in, lineBB)) {
            if (skipLines == 0) {
                row.parseFrom(lineBB);
                if (row.size() == 1) {
                    final Language srcLng = Language.fromKey(ArrayHelper.toStringP(row.getLanguage(0)));
                    final String name = ArrayHelper.toStringP(row.getValue(0, 0));
                    final byte[] nameBytes = ArrayHelper.toBytesP(row.getValue(0, 0));
                    final String path = ArrayHelper.toStringP(row.getFirstAttributeValue(0, 0,
                            UriLocation.TYPE_ID_BYTES));
                    final Category cat = Category.fromKey(ArrayHelper.toStringP(row.getFirstAttributeValue(0, 0,
                            Category.TYPE_ID_BYTES)));
                    if (DEBUG) {
                        System.out.println("语言：" + srcLng.key + "，单词：" + name + "，地址：" + path
                                + (cat != null ? "，类别：" + cat.key : Helper.EMPTY_STRING));
                    }
                    clear();
                    final Map<String, String> params = parseMainHtml(outSyms, srcLng, nameBytes, path);

                    // System.out.println(params.get("st_term"));
                    // System.out.println(params.get("wgCanonicalNamespace"));
                    // System.out.println(params.get("wgTitle"));

                    final String term = URLEncoder.encode(params.get("st_term"), Helper.CHARSET_UTF8.name());
                    final String ns = URLEncoder.encode(params.get("wgCanonicalNamespace"), Helper.CHARSET_UTF8.name());
                    final String src = URLEncoder.encode(params.get("wgTitle"), Helper.CHARSET_UTF8.name());
                    boolean success = false;
                    int retries = 0;
                    while (!success && retries++ < 3) {
                        success = parseRelatedJson(URL + "/api.php?action=twsearch&search=" + term + "&namespace=" + ns
                                + "&source=" + src + "&limit=50", outExamples, srcLng, name, nameBytes, cat);
                    }

                    final String pageName = URLEncoder.encode(params.get("wgPageName"), Helper.CHARSET_UTF8.name());
                    success = false;
                    retries = 0;
                    while (!success && retries++ < 3) {
                        success = parseLanguagesAjax(URL + "/index.php/Special:LanguageBarAjax", pageName, out, srcLng,
                                name, nameBytes, cat);
                    }
                    // http: //
                    // en.termwiki.com/api.php?action=twsearch&search=additifs&namespace=FR&source=additives+%E2%82%83&limit=50
                    count++;
                    if (count > 0 && count % 100 == 0) {
                        out.flush();
                        outExamples.flush();
                        outSyms.flush();
                        Helper.writeStatsFile(IN_STATUS, count);
                    }
                }
            } else {
                skipLines--;
            }
        }
        ArrayHelper.giveBack(lineBB);
        in.close();
        return count;
    }

    private void clear() {
        wordType = null;
    }

    private boolean parseLanguagesAjax(String url, String pageName, BufferedOutputStream out, Language srcLng,
            String name, byte[] nameBytes, Category cat) throws IOException {
        if (DEBUG) {
            System.out.println("搜索翻译：" + url);
        }
        BufferedReader reader = null;
        try {
            Helper.putConnectionHeader("X-Requested-With", "XMLHttpRequest");
            Helper.putConnectionHeader("Accept", "*/*");
            Helper.putConnectionHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

            reader = new BufferedReader(new InputStreamReader(Helper.openUrlInputStream(url, true,
                    "act=makeotherlanguages&fullpagename=" + pageName), Helper.CHARSET_UTF8));
            String line;
            int idx;
            boolean first = true;
            StringBuffer sb = new StringBuffer(256);
            while (null != (line = safeReadLine(reader))) {
                if (-1 != line.indexOf("Exception:")) {
                    System.err.println("服务器方错误：" + line);
                    return false;
                }
                idx = line.indexOf(" href=\"");
                if (idx != -1) {
                    line = line.substring(idx + " href=\"".length());
                    if (line.length() > 3) {
                        final String language = line.substring(1, 3);
                        final Language targetLng = LNG_MAPPER.get(language.toUpperCase());
                        if (targetLng != null && targetLng != srcLng) {
                            idx = line.indexOf("\">", 3);
                            if (idx != -1) {
                                final String href = line.substring(0, idx);
                                final int translationStart = idx + "\">".length();
                                idx = line.indexOf("</a>", translationStart);
                                if (idx != -1) {
                                    String translation = line.substring(translationStart, idx);
                                    if (targetLng == Language.ZH) {
                                        translation = ChineseHelper.toSimplifiedChinese(translation);
                                    }
                                    if (DEBUG) {
                                        System.out.println(targetLng.key + "=" + translation + ", " + href);
                                    }
                                    if (first) {
                                        first = false;
                                        sb.append(srcLng.key);
                                        sb.append(Helper.SEP_DEFINITION);
                                        sb.append(name);
                                        if (cat != null) {
                                            sb.append(Helper.SEP_ATTRIBUTE);
                                            sb.append(Category.TYPE_ID);
                                            sb.append(cat.key);
                                        }
                                        if (wordType != null) {
                                            sb.append(Helper.SEP_ATTRIBUTE);
                                            sb.append(WordType.TYPE_ID);
                                            sb.append(wordType.key);
                                        }
                                    }
                                    sb.append(Helper.SEP_LIST);
                                    sb.append(targetLng.key);
                                    sb.append(Helper.SEP_DEFINITION);
                                    sb.append(translation);
                                    if (cat != null) {
                                        sb.append(Helper.SEP_ATTRIBUTE);
                                        sb.append(Category.TYPE_ID);
                                        sb.append(cat.keyBytes);
                                    }
                                    if (wordType != null) {
                                        sb.append(Helper.SEP_ATTRIBUTE);
                                        sb.append(WordType.TYPE_ID);
                                        sb.append(wordType.key);
                                    }
                                } else {
                                    System.err.println("ajax: " + line);
                                }
                            } else {
                                System.err.println("ajax: " + line);
                            }
                        }
                    }
                }
            }
            if (!first) {
                out.write(sb.toString().getBytes(Helper.CHARSET_UTF8));
                out.write(Helper.SEP_NEWLINE_BYTES);
            }
        } finally {
            Helper.close(reader);
        }
        return true;
    }

    private boolean parseRelatedJson(String url, BufferedOutputStream outRelsJson, Language lng, String name,
            byte[] nameBytes, Category cat) throws IOException {
        if (DEBUG) {
            System.out.println("搜索相关词汇：" + url);
        }
        BufferedReader reader = null;
        try {
            Helper.putConnectionHeader("X-Requested-With", "XMLHttpRequest");
            Helper.putConnectionHeader("Accept", "application/json, text/javascript, */*");
            Helper.putConnectionHeader("Content-Type", "application/x-www-form-urlencoded");

            reader = new BufferedReader(new InputStreamReader(Helper.openUrlInputStream(url), Helper.CHARSET_UTF8));
            String line;
            int idx;
            while (null != (line = safeReadLine(reader))) {
                if (-1 != line.indexOf("Exception:")) {
                    System.err.println("服务器方错误：" + line);
                    return false;
                } else if ((idx = line.indexOf("\"title\":")) != -1) {
                    line = line.substring(idx + "\"title\":".length());
                    final String[] titles = line.split("\"title\":");
                    boolean first = true;
                    for (String t : titles) {
                        idx = t.indexOf("\",\"industry\":\"");
                        if (idx != -1) {
                            final String title = Helper.unescapeCode(t.substring(1, idx));
                            if (!name.equals(title)) {
                                final int catStart = idx + "\",\"industry\":\"".length();
                                idx = t.indexOf("\",\"", catStart);
                                final String category = Helper.unescapeCode(t.substring(catStart, idx));
                                final Category targetCat = CAT_MAPPER.get(category.toUpperCase());
                                if (DEBUG) {
                                    if (targetCat != null) {
                                        System.out.println("title: " + title + ", cat: " + targetCat.key);
                                    } else if (!CAT_MAPPER.containsKey(category.toUpperCase())) {
                                        System.out.println("title: " + title + ", ?cat?: " + category);
                                    }
                                }
                                if (first) {
                                    first = false;
                                    outRelsJson.write(lng.keyBytes);
                                    outRelsJson.write(Helper.SEP_DEFINITION_BYTES);
                                    outRelsJson.write(nameBytes);
                                    if (cat != null) {
                                        outRelsJson.write(Helper.SEP_ATTRS_BYTES);
                                        outRelsJson.write(Category.TYPE_ID_BYTES);
                                        outRelsJson.write(cat.keyBytes);
                                    }
                                }
                                outRelsJson.write(Helper.SEP_WORDS_BYTES);
                                outRelsJson.write(title.getBytes(Helper.CHARSET_UTF8));
                                if (targetCat != null) {
                                    outRelsJson.write(Helper.SEP_ATTRS_BYTES);
                                    outRelsJson.write(Category.TYPE_ID_BYTES);
                                    outRelsJson.write(targetCat.keyBytes);
                                }
                            }
                        }
                    }
                    if (!first) {
                        outRelsJson.write(Helper.SEP_NEWLINE_BYTES);
                    }
                    break;
                }
            }
        } finally {
            Helper.close(reader);
        }
        return true;
    }

    private Map<String, String> parseMainHtml(BufferedOutputStream outSyms, final Language lng, final byte[] nameBytes,
            final String path) throws MalformedURLException, IOException {
        Helper.putConnectionHeader("X-Requested-With", null);
        Helper.putConnectionHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        Helper.putConnectionHeader("Content-Type", null);

        final Map<String, String> params = new HashMap<String, String>();
        HttpURLConnection conn = Helper.getUrlConnection(URL + path);
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), Helper.CHARSET_UTF8));
        String line;
        State state = State.PARSE;
        StringBuffer sb = new StringBuffer();
        int idx;
        HTML: while (null != (line = safeReadLine(reader))) {
            switch (state) {
            case PARSE_DEFINITION:
            case PARSE_DEFINITION_FULL:
                if ((idx = line.indexOf("</p>")) != -1) {
                    appendDefinition(state, sb, line.substring(0, idx));
                    final String abstractText = sb.toString().trim().replaceAll("[\\t ]+", " ");
                    if (DEBUG) {
                        System.out.println("def: " + abstractText);
                    }
                    outDesc.write(lng.keyBytes);
                    outDesc.write(Helper.SEP_DEFINITION_BYTES);
                    outDesc.write(nameBytes);
                    outDesc.write(Helper.SEP_ATTRS_BYTES);
                    outDesc.write(Abstract.TYPE_ID_BYTES);
                    outDesc.write(abstractText.getBytes(Helper.CHARSET_UTF8));
                    outDesc.write(Helper.SEP_NEWLINE_BYTES);
                    state = State.PARSE;
                } else {
                    appendDefinition(state, sb, line);
                }
                break;
            case PARSE:
            default:
                // System.out.println(line);
                if ((idx = line.indexOf(KEY_ABSTRACT_START)) != -1) {
                    state = State.PARSE_DEFINITION;
                    appendDefinition(state, sb, line.substring(idx + KEY_ABSTRACT_START.length()));
                } else if ((idx = line.indexOf(">Part of Speech:<")) != -1) {
                    final String partOfSpeech = Helper.substringBetweenLast(line, "</span>", "<br");
                    if (Helper.isNotEmptyOrNull(partOfSpeech)) {
                        wordType = WORD_TYPES_MAP.get(partOfSpeech);
                        if (wordType == null && !WORD_TYPES_MAP.containsKey(partOfSpeech)) {
                            System.err.println("未知词类：" + partOfSpeech);
                        }
                    }
                } else if ((idx = line.indexOf(">Synonym(s):<")) != -1) {
                    String synonyms = Helper.substringBetweenLast(line, "</span>", "<br");
                    if (Helper.isNotEmptyOrNull(synonyms)) {
                        if (DEBUG) {
                            System.out.println("syms: " + synonyms);
                        }
                        if ((idx = synonyms.indexOf(" href=\"")) != -1) {
                            final int hrefStart = idx + " href=\"".length();
                            synonyms = synonyms.substring(hrefStart);
                            final String[] syms = synonyms.split(" href=\"");
                            outSyms.write(lng.keyBytes);
                            outSyms.write(Helper.SEP_DEFINITION_BYTES);
                            outSyms.write(nameBytes);
                            for (String s : syms) {
                                final String href = s.substring(0, (idx = s.indexOf("\">")));
                                final int titleStart = idx + "\">".length();
                                final String title = s.substring(titleStart, s.indexOf("</a>", titleStart));
                                outSyms.write(Helper.SEP_WORDS_BYTES);
                                outSyms.write(title.getBytes(Helper.CHARSET_UTF8));
                                outSyms.write(Helper.SEP_ATTRS_BYTES);
                                outSyms.write(UriLocation.TYPE_ID_BYTES);
                                outSyms.write(href.getBytes(Helper.CHARSET_UTF8));
                            }
                            outSyms.write(Helper.SEP_NEWLINE_BYTES);
                        }
                    }
                } else if ((idx = line.indexOf(">See Also:<")) != -1) {
                    String seeAlsos = Helper.substringBetweenLast(line, "</span>", "<br");
                    if (Helper.isNotEmptyOrNull(seeAlsos)) {
                        System.out.println("see also: " + seeAlsos);
                        if ((idx = seeAlsos.indexOf(" href=\"")) != -1) {
                            final int hrefStart = idx + " href=\"".length();
                            seeAlsos = seeAlsos.substring(hrefStart);
                            final String[] sees = seeAlsos.split(" href=\"");
                            outRelsSeeAlso.write(lng.keyBytes);
                            outRelsSeeAlso.write(Helper.SEP_DEFINITION_BYTES);
                            outRelsSeeAlso.write(nameBytes);
                            for (String s : sees) {
                                final String href = s.substring(0, (idx = s.indexOf("\">")));
                                final int titleStart = idx + "\">".length();
                                final String title = s.substring(titleStart, s.indexOf("</a>", titleStart));
                                outRelsSeeAlso.write(Helper.SEP_WORDS_BYTES);
                                outRelsSeeAlso.write(title.getBytes(Helper.CHARSET_UTF8));
                                outRelsSeeAlso.write(Helper.SEP_ATTRS_BYTES);
                                outRelsSeeAlso.write(UriLocation.TYPE_ID_BYTES);
                                outRelsSeeAlso.write(href.getBytes(Helper.CHARSET_UTF8));
                            }
                            outRelsSeeAlso.write(Helper.SEP_NEWLINE_BYTES);
                        }
                    }
                } else if (line.endsWith(";")) {
                    // find parameter
                    line = line.trim();
                    if (line.startsWith("var ") && (line.indexOf('"') != -1 || line.indexOf('\'') != -1)) {
                        String[] parts = line.split(" ");
                        if (parts.length > 3) {
                            final String key = parts[1];
                            String val = parts[3];
                            if (val.length() > 0 && val.indexOf('"') == 0) {
                                val = Helper.substringBetweenEnclose(line, "\"", "\"");
                            } else if (val.length() > 0 && val.indexOf('\'') == 0) {
                                val = Helper.substringBetweenEnclose(line, "'", "'");
                            }
                            if (val != null) {
                                val = Helper.unescapeCode(val);
                                // System.out.println(key + "=" + val);
                                params.put(key, val);
                            }
                        }
                    }
                } else if (line.indexOf("id=\"recently_talks_id\"") != -1) {
                    break HTML;
                }
            }
        }
        reader.close();
        return params;
    }

    private static String safeReadLine(BufferedReader reader) throws IOException {
        try {
            return reader.readLine();
        } catch (IOException e) {
            return Helper.EMPTY_STRING;
        }
    }

    private static void appendDefinition(State state, StringBuffer sb, String line) {
        if (State.PARSE_DEFINITION == state) {
            if (sb.length() > Abstract.MAX_ABSTRACT_CHARS) {
                sb.append(Helper.SEP_ETC);
                state = State.PARSE_DEFINITION_FULL;
            } else {
                sb.append(Helper.unescapeHtml(Helper.stripHtmlText(line, true)));
            }
        }
    }
}
