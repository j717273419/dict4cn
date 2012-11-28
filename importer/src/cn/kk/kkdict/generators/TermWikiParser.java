package cn.kk.kkdict.generators;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Date;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import sun.net.www.protocol.http.HttpURLConnection;
import cn.kk.kkdict.Configuration;
import cn.kk.kkdict.Configuration.Source;
import cn.kk.kkdict.types.Category;
import cn.kk.kkdict.types.Language;
import cn.kk.kkdict.types.TranslationSource;
import cn.kk.kkdict.types.UriLocation;
import cn.kk.kkdict.utils.ChineseHelper;
import cn.kk.kkdict.utils.Helper;

public class TermWikiParser {
  private static final boolean                 DEBUG            = false;
  private String                               sessionCookie;
  private static final Map<Language, String[]> LNGS;
  private static final String                  PREFIX           = "words_";
  private static final String                  OUTFILE_CATEGORY = Configuration.IMPORTER_FOLDER_GENERATED.getFile(Source.NULL, "termwiki_categories.txt");
  private static final String                  OUTDIR           = Configuration.IMPORTER_FOLDER_RAW_WORDS.getPath(Source.WORD_TERMWIKI);
  private static final String                  URL_TERMWIKI     = "http://en.termwiki.com";
  private final Set<String>                    categories       = new TreeSet<>();
  private static final Map<String, Category>   CAT_MAPPER       = new TreeMap<>();

  static {
    final File termwikiCategories = Helper.findResource("termwiki_categories.txt");
    System.out.println("导入类型文件：" + termwikiCategories.getAbsolutePath());
    try (final BufferedReader reader = new BufferedReader(new FileReader(termwikiCategories));) {
      String line;
      while (null != (line = reader.readLine())) {
        final String[] parts = line.split("=");
        if (parts.length == 2) {
          if (Helper.isNotEmptyOrNull(parts[0]) && Helper.isNotEmptyOrNull(parts[1])) {
            if (TermWikiParser.DEBUG) {
              System.out.println("类：" + parts[1] + " -> " + Category.valueOf(parts[0]).key);
            }
            TermWikiParser.CAT_MAPPER.put(parts[1].toUpperCase(), Category.valueOf(parts[0]));
          } else {
            if (TermWikiParser.DEBUG) {
              System.out.println("类：" + parts[1] + " -> null");
            }
            TermWikiParser.CAT_MAPPER.put(parts[1].toUpperCase(), null);
          }
        }
      }
    } catch (final Exception e) {
      System.err.println("导入错误：" + e.toString());
    }

    LNGS = new EnumMap<>(Language.class);
    TermWikiParser.LNGS.put(Language.ZH, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Chinese,_Simplified_%28ZS%29",
      TermWikiParser.URL_TERMWIKI + "/Language:Chinese,_Hong_Kong_%28ZH%29", TermWikiParser.URL_TERMWIKI + "/Language:Chinese,_Traditional_%28ZT%29" });
    TermWikiParser.LNGS.put(Language.AF, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Afrikaans_%28AF%29" });
    TermWikiParser.LNGS.put(Language.SQ, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Albanian_%28SQ%29" });
    TermWikiParser.LNGS.put(Language.AM, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Amharic_%28AM%29" });
    TermWikiParser.LNGS.put(Language.AR, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Arabic_%28AR%29" });
    TermWikiParser.LNGS.put(Language.HY, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Armenian_%28HY%29" });
    TermWikiParser.LNGS.put(Language.EU, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Basque_%28EU%29" });
    TermWikiParser.LNGS.put(Language.BN, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Bengali_%28BN%29" });
    TermWikiParser.LNGS.put(Language.BS, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Bosnian_%28BS%29" });
    TermWikiParser.LNGS.put(Language.BR, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Breton_%28BR%29" });
    TermWikiParser.LNGS.put(Language.BG, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Bulgarian_%28BG%29" });
    TermWikiParser.LNGS.put(Language.KM, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Cambodian_%28KM%29" });
    TermWikiParser.LNGS.put(Language.CA, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Catalan_%28CA%29" });
    TermWikiParser.LNGS.put(Language.CV, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Chuvash_%28CV%29" });
    TermWikiParser.LNGS.put(Language.HR, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Croatian_%28HR%29" });
    TermWikiParser.LNGS.put(Language.CS, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Czech_%28CS%29" });
    TermWikiParser.LNGS.put(Language.DA, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Danish_%28DA%29" });
    TermWikiParser.LNGS.put(Language.NL, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Dutch_%28NL%29" });
    TermWikiParser.LNGS.put(Language.EN, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:English_%28EN%29",
      TermWikiParser.URL_TERMWIKI + "/Language:English,_UK_%28UE%29" });
    TermWikiParser.LNGS.put(Language.EO, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Esperanto_%28EO%29" });
    TermWikiParser.LNGS.put(Language.ET, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Estonian_%28ET%29" });
    TermWikiParser.LNGS.put(Language.FO, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Faroese_%28FO%29" });
    TermWikiParser.LNGS.put(Language.TL, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Filipino_%28TL%29" });
    TermWikiParser.LNGS.put(Language.FI, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Finnish_%28FI%29" });
    TermWikiParser.LNGS.put(Language.FR, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:French_%28FR%29",
      TermWikiParser.URL_TERMWIKI + "/Language:French,_Canadian_%28CF%29" });
    TermWikiParser.LNGS.put(Language.GL, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Galician_%28GL%29" });
    TermWikiParser.LNGS.put(Language.KA, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Georgian_%28KA%29" });
    TermWikiParser.LNGS.put(Language.DE, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:German_%28DE%29" });
    TermWikiParser.LNGS.put(Language.EL, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Greek_%28EL%29" });
    TermWikiParser.LNGS.put(Language.GU, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Gujarati_%28GU%29" });
    TermWikiParser.LNGS.put(Language.HA, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Hausa_%28HA%29" });
    TermWikiParser.LNGS.put(Language.IW, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Hebrew_%28IW%29" });
    TermWikiParser.LNGS.put(Language.HI, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Hindi_%28HI%29" });
    TermWikiParser.LNGS.put(Language.HU, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Hungarian_%28HU%29" });
    TermWikiParser.LNGS.put(Language.IS, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Icelandic_%28IS%29" });
    TermWikiParser.LNGS.put(Language.IG, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Igbo_%28IG%29" });
    TermWikiParser.LNGS.put(Language.ID, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Indonesian_%28ID%29" });
    TermWikiParser.LNGS.put(Language.GA, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Irish_%28GA%29" });
    TermWikiParser.LNGS.put(Language.IT, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Italian_%28IT%29" });
    TermWikiParser.LNGS.put(Language.JA, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Japanese_%28JA%29" });
    TermWikiParser.LNGS.put(Language.JV, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Javanese_%28JW%29" });
    TermWikiParser.LNGS.put(Language.KN, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Kannada_%28KN%29" });
    TermWikiParser.LNGS.put(Language.KK, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Kazakh_%28KK%29" });
    TermWikiParser.LNGS.put(Language.KO, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Korean_%28KO%29" });
    TermWikiParser.LNGS.put(Language.KU, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Kurdish_%28KU%29" });
    TermWikiParser.LNGS.put(Language.LO, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Laothian_%28LO%29" });
    TermWikiParser.LNGS.put(Language.LA, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Latin_%28LA%29" });
    TermWikiParser.LNGS.put(Language.LV, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Latvian_%28LV%29" });
    TermWikiParser.LNGS.put(Language.LT, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Lithuanian_%28LT%29" });
    TermWikiParser.LNGS.put(Language.MK, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Macedonian_%28MK%29" });
    TermWikiParser.LNGS.put(Language.MS, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Malay_%28MS%29" });
    TermWikiParser.LNGS.put(Language.ML, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Malayalam_%28ML%29" });
    TermWikiParser.LNGS.put(Language.MT, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Maltese_%28MT%29" });
    TermWikiParser.LNGS.put(Language.MR, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Marathi_%28MR%29" });
    TermWikiParser.LNGS.put(Language.MFE, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Mauritian_Creole_%28MC%29" });
    TermWikiParser.LNGS.put(Language.MN, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Mongolian_%28MN%29" });
    TermWikiParser.LNGS.put(Language.NE, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Nepali_%28NE%29" });
    TermWikiParser.LNGS.put(Language.NO, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Norwegian_Bokm%C3%A5l_%28NO%29" });
    TermWikiParser.LNGS.put(Language.NN, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Norwegian_Nynorsk_%28NN%29" });
    TermWikiParser.LNGS.put(Language.OR, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Oriya_%28OR%29" });
    TermWikiParser.LNGS.put(Language.OM, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Oromo_%28OM%29" });
    TermWikiParser.LNGS.put(Language.PS, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Pashto_%28PS%29" });
    TermWikiParser.LNGS.put(Language.FA, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Persian_%28FA%29" });
    TermWikiParser.LNGS.put(Language.PRS, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Persian,_Dari_%28DR%29" });
    TermWikiParser.LNGS.put(Language.PL, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Polish_%28PL%29" });
    TermWikiParser.LNGS.put(Language.PT, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Portuguese_%28PT%29",
      TermWikiParser.URL_TERMWIKI + "/Language:Portuguese,_Brazilian_%28PB%29" });
    TermWikiParser.LNGS.put(Language.RO, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Romanian_%28RO%29" });
    TermWikiParser.LNGS.put(Language.RM, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Romansh_%28RM%29" });
    TermWikiParser.LNGS.put(Language.RU, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Russian_%28RU%29" });
    TermWikiParser.LNGS.put(Language.SA, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Sanskrit_%28SA%29" });
    TermWikiParser.LNGS.put(Language.GD, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Scots_Gaelic_%28GD%29" });
    TermWikiParser.LNGS.put(Language.SR, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Serbian_%28SR%29" });
    TermWikiParser.LNGS.put(Language.SH, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Serbo_Croatian_%28SH%29" });
    TermWikiParser.LNGS.put(Language.SI, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Sinhalese_%28SI%29" });
    TermWikiParser.LNGS.put(Language.SK, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Slovak_%28SK%29" });
    TermWikiParser.LNGS.put(Language.SL, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Slovenian_%28SL%29" });
    TermWikiParser.LNGS.put(Language.SO, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Somali_%28SO%29" });
    TermWikiParser.LNGS.put(Language.ES, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Spanish_%28ES%29",
      TermWikiParser.URL_TERMWIKI + "/Language:Spanish,_Latin_American_%28XL%29" });
    TermWikiParser.LNGS.put(Language.SW, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Swahili_%28SW%29" });
    TermWikiParser.LNGS.put(Language.SV, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Swedish_%28SV%29" });
    TermWikiParser.LNGS.put(Language.TG, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Tajik_%28TG%29" });
    TermWikiParser.LNGS.put(Language.TA, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Tamil_%28TA%29" });
    TermWikiParser.LNGS.put(Language.TH, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Thai_%28TH%29" });
    TermWikiParser.LNGS.put(Language.TO, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Tonga_%28TO%29" });
    TermWikiParser.LNGS.put(Language.TR, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Turkish_%28TR%29" });
    TermWikiParser.LNGS.put(Language.TK, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Turkmen_%28TK%29" });
    TermWikiParser.LNGS.put(Language.UG, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Uighur_%28UG%29" });
    TermWikiParser.LNGS.put(Language.UK, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Ukrainian_%28UK%29" });
    TermWikiParser.LNGS.put(Language.UR, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Urdu_%28UR%29" });
    TermWikiParser.LNGS.put(Language.VI, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Vietnamese_%28VI%29" });
    TermWikiParser.LNGS.put(Language.CY, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Welsh_%28CY%29" });
    TermWikiParser.LNGS.put(Language.YO, new String[] { TermWikiParser.URL_TERMWIKI + "/Language:Yoruba_%28YO%29" });
  }

  public TermWikiParser() {
  }

  /**
   * @param args
   * @throws IOException
   * @throws InterruptedException
   */
  public static void main(final String[] args) throws IOException, InterruptedException {
    final TermWikiParser parser = new TermWikiParser();
    if (parser.login("xinxinxin", "Ji0\"§SSFwez")) {
      parser.start();
    }
  }

  private void start() throws IOException, InterruptedException {
    final long startTotal = System.currentTimeMillis();
    this.categories.clear();
    final Set<Language> lngs = TermWikiParser.LNGS.keySet();
    long total = 0;
    for (final Language lng : lngs) {
      final long start = System.currentTimeMillis();
      final String[] urls = TermWikiParser.LNGS.get(lng);
      final String file = TermWikiParser.OUTDIR + "/" + TermWikiParser.PREFIX + lng.getKey() + "." + TranslationSource.TERMWIKI.key;
      if (Helper.isEmptyOrNotExists(file)) {
        System.out.print("创建文件：" + file + " 。。。 ");
        try (final BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file), Helper.BUFFER_SIZE);) {
          long count = 0;
          for (final String url : urls) {
            count += this.parseUrl(lng, url, out);
          }
          System.out.println("共" + count + "词组，花时：" + Helper.formatDuration(System.currentTimeMillis() - start));
          total += count;
        }
      } else {
        System.out.println("跳过：" + file + "，文件已存在。");
      }
    }
    try (final BufferedOutputStream outCategories = new BufferedOutputStream(new FileOutputStream(TermWikiParser.OUTFILE_CATEGORY), Helper.BUFFER_SIZE);) {
      outCategories.write(("# termwiki categories generated at " + new Date() + "\n").getBytes(Helper.CHARSET_UTF8));
      for (final String category : this.categories) {
        outCategories.write('=');
        outCategories.write(category.getBytes(Helper.CHARSET_UTF8));
        outCategories.write(Helper.SEP_NEWLINE_BYTES);
      }
    }
    System.out.println("完成termwiki词汇生成。共" + total + "词组，总共花时：" + Helper.formatDuration(System.currentTimeMillis() - startTotal));
  }

  private int parseUrl(final Language lng, final String url, final BufferedOutputStream out) throws IOException, InterruptedException {
    int total = 0;
    int page = 1;
    int count;

    do {
      count = 0;
      final String parseUrl = url + "?page=" + page++;
      final URLConnection conn = Helper.getUrlConnection(parseUrl);
      conn.setRequestProperty("Cookie", this.sessionCookie);
      conn.setRequestProperty("Referer", TermWikiParser.URL_TERMWIKI + "/mainpage.php");
      conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1)");
      boolean finished = false;
      int retries = 0;
      while (!finished) {
        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), Helper.CHARSET_UTF8));) {
          String line;
          while (null != (line = reader.readLine())) {
            if (line.startsWith("<li>") && line.endsWith("</li>") && (line.indexOf("\"term-list-ind\"") != -1)) {
              final String href = Helper.substringBetween(line, "<a href=\"", "\">");
              String definition = Helper.substringBetween(line, "\">", "</a>");
              String category = Helper.unescapeHtml(Helper.substringBetweenLast(line, "\">(", ")</span>"));
              int idx;
              if ((-1 != (idx = category.indexOf(','))) || (-1 != (idx = category.indexOf(';')))) {
                category = category.substring(0, idx);
              }
              if (Helper.isNotEmptyOrNull(href) && Helper.isNotEmptyOrNull(definition) && Helper.isNotEmptyOrNull(category)) {
                this.categories.add(category);
                Category cat;
                if (TermWikiParser.CAT_MAPPER.containsKey(category.toUpperCase())) {
                  cat = TermWikiParser.CAT_MAPPER.get(category.toUpperCase());
                } else {
                  cat = TermWikiParser.CAT_MAPPER.get(category.toUpperCase());
                  System.err.println("发现未知类：" + category);
                }
                if (lng == Language.ZH) {
                  definition = ChineseHelper.toSimplifiedChinese(definition);
                }

                out.write(lng.getKeyBytes());
                out.write(Helper.SEP_DEFINITION_BYTES);
                out.write(definition.getBytes(Helper.CHARSET_UTF8));
                out.write(Helper.SEP_ATTRS_BYTES);
                out.write(UriLocation.TYPE_ID_BYTES);
                out.write(href.getBytes(Helper.CHARSET_UTF8));
                if (cat != null) {
                  out.write(Helper.SEP_ATTRS_BYTES);
                  out.write(Category.TYPE_ID_BYTES);
                  out.write(cat.keyBytes);
                }
                out.write(Helper.SEP_NEWLINE_BYTES);
                if (TermWikiParser.DEBUG) {
                  System.out.println("新词：" + definition + "，网址：" + href + "，类型：" + category);
                }
                count++;
              }
            }
          }
          total += count;
          finished = true;
        } catch (Exception e) {
          retries++;
          if (retries > 5) {
            e.printStackTrace();
            break;
          } else {
            TimeUnit.SECONDS.sleep(10 * retries);
            System.out.println("出现错误。重试。。。 " + e.toString());
          }
        }
      }
    } while (count != 0);
    return total;
  }

  private boolean login(final String user, final String pass) throws IOException {

    HttpURLConnection conn = (HttpURLConnection) Helper.getUrlConnection(TermWikiParser.URL_TERMWIKI
        + "/index.php?title=Special:UserLogin&wpCookieCheck=login&returnto=Home");

    // Helper.consume(conn.getInputStream(), new FileOutputStream(System.getProperty("java.io.tmpdir") + "/dict-termwiki-gen_tmp.txt"));

    if (TermWikiParser.DEBUG) {
      Helper.debug(conn);
    }
    final StringBuffer cookie = new StringBuffer();
    Helper.appendCookies(cookie, conn);

    if (cookie.length() == 0) {
      System.err.println("网路错误：" + conn.getHeaderField(null));
      return false;
    } else {
      final String encUser = URLEncoder.encode(user, Helper.CHARSET_UTF8.name());
      final String encPass = URLEncoder.encode(pass, Helper.CHARSET_UTF8.name());
      final String postData = "wpName=" + encUser + "&wpPassword=" + encPass + "&wpLoginattempt=Log+in";
      System.out.println("登录：" + postData + "，甜饼：" + cookie.toString());

      // login
      final String loginUrl = TermWikiParser.URL_TERMWIKI + "/index.php?title=Special:UserLogin&action=submitlogin&type=login&returnto=Home";
      conn = (HttpURLConnection) Helper.getUrlConnection(loginUrl);
      conn.setRequestMethod("POST");
      conn.setDoOutput(true);
      conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; utf-8");
      conn.setRequestProperty("Cookie", cookie.toString());
      conn.setRequestProperty("Referer", TermWikiParser.URL_TERMWIKI + "/mainpage.php?returnto=Home&p=Login");
      conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1)");

      conn.setUseCaches(false);

      try (OutputStream out = conn.getOutputStream();) {
        out.write(postData.getBytes(Helper.CHARSET_UTF8));
      }

      if (TermWikiParser.DEBUG) {
        Helper.debug(conn);
      }
      Helper.appendCookies(cookie, conn);

      if (cookie.indexOf(user) != -1) {
        this.sessionCookie = cookie.toString();
      } else {
        this.sessionCookie = null;
      }
      if (this.sessionCookie != null) {
        System.out.println("登录成功：" + this.sessionCookie);
        return true;
      } else {
        System.out.println("登录失败：" + user + " / " + pass);
        return false;
      }
    }
  }
}
