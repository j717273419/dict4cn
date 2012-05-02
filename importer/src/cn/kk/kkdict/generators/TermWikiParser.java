package cn.kk.kkdict.generators;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import cn.kk.kkdict.Configuration;
import cn.kk.kkdict.Configuration.Source;
import cn.kk.kkdict.types.Category;
import cn.kk.kkdict.types.Language;
import cn.kk.kkdict.types.TranslationSource;
import cn.kk.kkdict.types.UriLocation;
import cn.kk.kkdict.utils.ChineseHelper;
import cn.kk.kkdict.utils.Helper;

public class TermWikiParser {
    private static final boolean DEBUG = false;
    private String sessionCookie;
    private static final Map<Language, String[]> LNGS;
    private static final String PREFIX = "words_";
    private static final String OUTFILE_CATEGORY = Configuration.IMPORTER_FOLDER_GENERATED.getFile(Source.NULL,
            "termwiki_categories.txt");
    private static final String OUTDIR = Configuration.IMPORTER_FOLDER_RAW_WORDS.getPath(Source.WORD_TERMWIKI);
    private static final String URL_TERMWIKI = "http://en.termwiki.com";
    private Set<String> categories = new TreeSet<String>();
    private static final Map<String, Category> CAT_MAPPER = new HashMap<String, Category>();

    static {
        final File termwikiCategories = Helper.findResource("termwiki_categories.txt");
        System.out.println("导入类型文件：" + termwikiCategories.getAbsolutePath());
        try {
            BufferedReader reader = new BufferedReader(new FileReader(termwikiCategories));
            String line;
            while (null != (line = reader.readLine())) {
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

        LNGS = new HashMap<Language, String[]>();
        LNGS.put(Language.ZH, new String[] { URL_TERMWIKI + "/Language:Chinese,_Simplified_%28ZS%29",
                URL_TERMWIKI + "/Language:Chinese,_Hong_Kong_%28ZH%29",
                URL_TERMWIKI + "/Language:Chinese,_Traditional_%28ZT%29" });
        LNGS.put(Language.AF, new String[] { URL_TERMWIKI + "/Language:Afrikaans_%28AF%29" });
        LNGS.put(Language.SQ, new String[] { URL_TERMWIKI + "/Language:Albanian_%28SQ%29" });
        LNGS.put(Language.AM, new String[] { URL_TERMWIKI + "/Language:Amharic_%28AM%29" });
        LNGS.put(Language.AR, new String[] { URL_TERMWIKI + "/Language:Arabic_%28AR%29" });
        LNGS.put(Language.HY, new String[] { URL_TERMWIKI + "/Language:Armenian_%28HY%29" });
        LNGS.put(Language.EU, new String[] { URL_TERMWIKI + "/Language:Basque_%28EU%29" });
        LNGS.put(Language.BN, new String[] { URL_TERMWIKI + "/Language:Bengali_%28BN%29" });
        LNGS.put(Language.BS, new String[] { URL_TERMWIKI + "/Language:Bosnian_%28BS%29" });
        LNGS.put(Language.BR, new String[] { URL_TERMWIKI + "/Language:Breton_%28BR%29" });
        LNGS.put(Language.BG, new String[] { URL_TERMWIKI + "/Language:Bulgarian_%28BG%29" });
        LNGS.put(Language.KM, new String[] { URL_TERMWIKI + "/Language:Cambodian_%28KM%29" });
        LNGS.put(Language.CA, new String[] { URL_TERMWIKI + "/Language:Catalan_%28CA%29" });
        LNGS.put(Language.CV, new String[] { URL_TERMWIKI + "/Language:Chuvash_%28CV%29" });
        LNGS.put(Language.HR, new String[] { URL_TERMWIKI + "/Language:Croatian_%28HR%29" });
        LNGS.put(Language.CS, new String[] { URL_TERMWIKI + "/Language:Czech_%28CS%29" });
        LNGS.put(Language.DA, new String[] { URL_TERMWIKI + "/Language:Danish_%28DA%29" });
        LNGS.put(Language.NL, new String[] { URL_TERMWIKI + "/Language:Dutch_%28NL%29" });
        LNGS.put(Language.EN, new String[] { URL_TERMWIKI + "/Language:English_%28EN%29",
                URL_TERMWIKI + "/Language:English,_UK_%28UE%29" });
        LNGS.put(Language.EO, new String[] { URL_TERMWIKI + "/Language:Esperanto_%28EO%29" });
        LNGS.put(Language.ET, new String[] { URL_TERMWIKI + "/Language:Estonian_%28ET%29" });
        LNGS.put(Language.FO, new String[] { URL_TERMWIKI + "/Language:Faroese_%28FO%29" });
        LNGS.put(Language.TL, new String[] { URL_TERMWIKI + "/Language:Filipino_%28TL%29" });
        LNGS.put(Language.FI, new String[] { URL_TERMWIKI + "/Language:Finnish_%28FI%29" });
        LNGS.put(Language.FR, new String[] { URL_TERMWIKI + "/Language:French_%28FR%29",
                URL_TERMWIKI + "/Language:French,_Canadian_%28CF%29" });
        LNGS.put(Language.GL, new String[] { URL_TERMWIKI + "/Language:Galician_%28GL%29" });
        LNGS.put(Language.KA, new String[] { URL_TERMWIKI + "/Language:Georgian_%28KA%29" });
        LNGS.put(Language.DE, new String[] { URL_TERMWIKI + "/Language:German_%28DE%29" });
        LNGS.put(Language.EL, new String[] { URL_TERMWIKI + "/Language:Greek_%28EL%29" });
        LNGS.put(Language.GU, new String[] { URL_TERMWIKI + "/Language:Gujarati_%28GU%29" });
        LNGS.put(Language.HA, new String[] { URL_TERMWIKI + "/Language:Hausa_%28HA%29" });
        LNGS.put(Language.IW, new String[] { URL_TERMWIKI + "/Language:Hebrew_%28IW%29" });
        LNGS.put(Language.HI, new String[] { URL_TERMWIKI + "/Language:Hindi_%28HI%29" });
        LNGS.put(Language.HU, new String[] { URL_TERMWIKI + "/Language:Hungarian_%28HU%29" });
        LNGS.put(Language.IS, new String[] { URL_TERMWIKI + "/Language:Icelandic_%28IS%29" });
        LNGS.put(Language.IG, new String[] { URL_TERMWIKI + "/Language:Igbo_%28IG%29" });
        LNGS.put(Language.ID, new String[] { URL_TERMWIKI + "/Language:Indonesian_%28ID%29" });
        LNGS.put(Language.GA, new String[] { URL_TERMWIKI + "/Language:Irish_%28GA%29" });
        LNGS.put(Language.IT, new String[] { URL_TERMWIKI + "/Language:Italian_%28IT%29" });
        LNGS.put(Language.JA, new String[] { URL_TERMWIKI + "/Language:Japanese_%28JA%29" });
        LNGS.put(Language.JV, new String[] { URL_TERMWIKI + "/Language:Javanese_%28JW%29" });
        LNGS.put(Language.KN, new String[] { URL_TERMWIKI + "/Language:Kannada_%28KN%29" });
        LNGS.put(Language.KK, new String[] { URL_TERMWIKI + "/Language:Kazakh_%28KK%29" });
        LNGS.put(Language.KO, new String[] { URL_TERMWIKI + "/Language:Korean_%28KO%29" });
        LNGS.put(Language.KU, new String[] { URL_TERMWIKI + "/Language:Kurdish_%28KU%29" });
        LNGS.put(Language.LO, new String[] { URL_TERMWIKI + "/Language:Laothian_%28LO%29" });
        LNGS.put(Language.LA, new String[] { URL_TERMWIKI + "/Language:Latin_%28LA%29" });
        LNGS.put(Language.LV, new String[] { URL_TERMWIKI + "/Language:Latvian_%28LV%29" });
        LNGS.put(Language.LT, new String[] { URL_TERMWIKI + "/Language:Lithuanian_%28LT%29" });
        LNGS.put(Language.MK, new String[] { URL_TERMWIKI + "/Language:Macedonian_%28MK%29" });
        LNGS.put(Language.MS, new String[] { URL_TERMWIKI + "/Language:Malay_%28MS%29" });
        LNGS.put(Language.ML, new String[] { URL_TERMWIKI + "/Language:Malayalam_%28ML%29" });
        LNGS.put(Language.MT, new String[] { URL_TERMWIKI + "/Language:Maltese_%28MT%29" });
        LNGS.put(Language.MR, new String[] { URL_TERMWIKI + "/Language:Marathi_%28MR%29" });
        LNGS.put(Language.MFE, new String[] { URL_TERMWIKI + "/Language:Mauritian_Creole_%28MC%29" });
        LNGS.put(Language.MN, new String[] { URL_TERMWIKI + "/Language:Mongolian_%28MN%29" });
        LNGS.put(Language.NE, new String[] { URL_TERMWIKI + "/Language:Nepali_%28NE%29" });
        LNGS.put(Language.NO, new String[] { URL_TERMWIKI + "/Language:Norwegian_Bokm%C3%A5l_%28NO%29" });
        LNGS.put(Language.NN, new String[] { URL_TERMWIKI + "/Language:Norwegian_Nynorsk_%28NN%29" });
        LNGS.put(Language.OR, new String[] { URL_TERMWIKI + "/Language:Oriya_%28OR%29" });
        LNGS.put(Language.OM, new String[] { URL_TERMWIKI + "/Language:Oromo_%28OM%29" });
        LNGS.put(Language.PS, new String[] { URL_TERMWIKI + "/Language:Pashto_%28PS%29" });
        LNGS.put(Language.FA, new String[] { URL_TERMWIKI + "/Language:Persian_%28FA%29" });
        LNGS.put(Language.PRS, new String[] { URL_TERMWIKI + "/Language:Persian,_Dari_%28DR%29" });
        LNGS.put(Language.PL, new String[] { URL_TERMWIKI + "/Language:Polish_%28PL%29" });
        LNGS.put(Language.PT, new String[] { URL_TERMWIKI + "/Language:Portuguese_%28PT%29",
                URL_TERMWIKI + "/Language:Portuguese,_Brazilian_%28PB%29" });
        LNGS.put(Language.RO, new String[] { URL_TERMWIKI + "/Language:Romanian_%28RO%29" });
        LNGS.put(Language.RM, new String[] { URL_TERMWIKI + "/Language:Romansh_%28RM%29" });
        LNGS.put(Language.RU, new String[] { URL_TERMWIKI + "/Language:Russian_%28RU%29" });
        LNGS.put(Language.SA, new String[] { URL_TERMWIKI + "/Language:Sanskrit_%28SA%29" });
        LNGS.put(Language.GD, new String[] { URL_TERMWIKI + "/Language:Scots_Gaelic_%28GD%29" });
        LNGS.put(Language.SR, new String[] { URL_TERMWIKI + "/Language:Serbian_%28SR%29" });
        LNGS.put(Language.SH, new String[] { URL_TERMWIKI + "/Language:Serbo_Croatian_%28SH%29" });
        LNGS.put(Language.SI, new String[] { URL_TERMWIKI + "/Language:Sinhalese_%28SI%29" });
        LNGS.put(Language.SK, new String[] { URL_TERMWIKI + "/Language:Slovak_%28SK%29" });
        LNGS.put(Language.SL, new String[] { URL_TERMWIKI + "/Language:Slovenian_%28SL%29" });
        LNGS.put(Language.SO, new String[] { URL_TERMWIKI + "/Language:Somali_%28SO%29" });
        LNGS.put(Language.ES, new String[] { URL_TERMWIKI + "/Language:Spanish_%28ES%29",
                URL_TERMWIKI + "/Language:Spanish,_Latin_American_%28XL%29" });
        LNGS.put(Language.SW, new String[] { URL_TERMWIKI + "/Language:Swahili_%28SW%29" });
        LNGS.put(Language.SV, new String[] { URL_TERMWIKI + "/Language:Swedish_%28SV%29" });
        LNGS.put(Language.TG, new String[] { URL_TERMWIKI + "/Language:Tajik_%28TG%29" });
        LNGS.put(Language.TA, new String[] { URL_TERMWIKI + "/Language:Tamil_%28TA%29" });
        LNGS.put(Language.TH, new String[] { URL_TERMWIKI + "/Language:Thai_%28TH%29" });
        LNGS.put(Language.TO, new String[] { URL_TERMWIKI + "/Language:Tonga_%28TO%29" });
        LNGS.put(Language.TR, new String[] { URL_TERMWIKI + "/Language:Turkish_%28TR%29" });
        LNGS.put(Language.TK, new String[] { URL_TERMWIKI + "/Language:Turkmen_%28TK%29" });
        LNGS.put(Language.UG, new String[] { URL_TERMWIKI + "/Language:Uighur_%28UG%29" });
        LNGS.put(Language.UK, new String[] { URL_TERMWIKI + "/Language:Ukrainian_%28UK%29" });
        LNGS.put(Language.UR, new String[] { URL_TERMWIKI + "/Language:Urdu_%28UR%29" });
        LNGS.put(Language.VI, new String[] { URL_TERMWIKI + "/Language:Vietnamese_%28VI%29" });
        LNGS.put(Language.CY, new String[] { URL_TERMWIKI + "/Language:Welsh_%28CY%29" });
        LNGS.put(Language.YO, new String[] { URL_TERMWIKI + "/Language:Yoruba_%28YO%29" });
    }

    public TermWikiParser() {
        System.setProperty("http.keepAlive", "false");
        CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
        HttpURLConnection.setFollowRedirects(false);
    }

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        TermWikiParser parser = new TermWikiParser();
        if (parser.login("xinxinxin", "Ji0\"§SSFwez")) {
            parser.start();
        }
    }

    private void start() throws IOException {
        final long startTotal = System.currentTimeMillis();
        categories.clear();
        Set<Language> lngs = LNGS.keySet();
        long total = 0;
        for (Language lng : lngs) {
            final long start = System.currentTimeMillis();
            String[] urls = LNGS.get(lng);
            final String file = OUTDIR + "/" + PREFIX + lng.key + "." + TranslationSource.TERMWIKI.key;
            if (Helper.isEmptyOrNotExists(file)) {
                System.out.print("创建文件：" + file + " 。。。 ");
                BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file), Helper.BUFFER_SIZE);
                long count = 0;
                for (String url : urls) {
                    count += parseUrl(lng, url, out);
                }
                System.out.println("共" + count + "词组，花时：" + Helper.formatDuration(System.currentTimeMillis() - start));
                total += count;
                out.close();
            } else {
                System.out.println("跳过：" + file + "，文件已存在。");
            }
        }
        BufferedOutputStream outCategories = new BufferedOutputStream(new FileOutputStream(OUTFILE_CATEGORY),
                Helper.BUFFER_SIZE);
        outCategories.write(("# termwiki categories generated at " + new Date() + "\n").getBytes(Helper.CHARSET_UTF8));
        for (String category : categories) {
            outCategories.write('=');
            outCategories.write(category.getBytes(Helper.CHARSET_UTF8));
            outCategories.write(Helper.SEP_NEWLINE_BYTES);
        }
        outCategories.close();
        System.out.println("完成termwiki词汇生成。共" + total + "词组，总共花时："
                + Helper.formatDuration(System.currentTimeMillis() - startTotal));
    }

    private int parseUrl(Language lng, String url, BufferedOutputStream out) throws IOException {
        int total = 0;
        int page = 1;
        int count;
        do {
            count = 0;
            URL parseUrl = new URL(url + "?page=" + page++);
            URLConnection conn = parseUrl.openConnection();
            conn.setRequestProperty("Cookie", sessionCookie);
            conn.setRequestProperty("Referer", URL_TERMWIKI + "/mainpage.php");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1)");
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), Helper.CHARSET_UTF8));
            String line;
            while (null != (line = reader.readLine())) {
                if (line.startsWith("<li>") && line.endsWith("</li>") && line.indexOf("\"term-list-ind\"") != -1) {
                    final String href = Helper.substringBetween(line, "<a href=\"", "\">");
                    String definition = Helper.substringBetween(line, "\">", "</a>");
                    final String category = Helper.unescapeHtml(Helper.substringBetweenLast(line, "\">(", ")</span>"));
                    if (Helper.isNotEmptyOrNull(href) && Helper.isNotEmptyOrNull(definition)
                            && Helper.isNotEmptyOrNull(category)) {
                        categories.add(category);
                        Category cat;
                        if (CAT_MAPPER.containsKey(category.toUpperCase())) {
                            cat = CAT_MAPPER.get(category.toUpperCase());
                        } else {
                            cat = CAT_MAPPER.get(category.toUpperCase());
                            System.err.println("发现未知类：" + category);
                        }
                        if (lng == Language.ZH) {
                            definition = ChineseHelper.toSimplifiedChinese(definition);
                        }

                        out.write(lng.key.getBytes(Helper.CHARSET_UTF8));
                        out.write(Helper.SEP_DEFINITION_BYTES);
                        out.write(definition.getBytes(Helper.CHARSET_UTF8));
                        out.write(Helper.SEP_ATTRS_BYTES);
                        out.write(UriLocation.TYPE_ID_BYTES);
                        out.write(href.getBytes(Helper.CHARSET_UTF8));
                        if (cat != null) {
                            out.write(Helper.SEP_ATTRS_BYTES);
                            out.write(Category.TYPE_ID_BYTES);
                            out.write(cat.key.getBytes(Helper.CHARSET_UTF8));
                        }
                        out.write(Helper.SEP_NEWLINE_BYTES);
                        if (DEBUG) {
                            System.out.println("新词：" + definition + "，网址：" + href + "，类型：" + category);
                        }
                        count++;
                    }
                }
            }
            total += count;
        } while (count != 0);
        return total;
    }

    private boolean login(String user, String pass) throws IOException {

        Map<String, List<String>> headers = new URL(URL_TERMWIKI + "/Home").openConnection().getHeaderFields();
        Set<String> keys = headers.keySet();
        if (DEBUG) {
            for (String k : keys) {
                System.out.println(k + "=" + headers.get(k));
            }
        }
        StringBuffer cookie = new StringBuffer();
        addCookies(cookie, headers);

        if (cookie.length() == 0) {
            System.err.println("网路错误：" + headers.get(null));
            return false;
        } else {
            String encUser = URLEncoder.encode(user, Helper.CHARSET_UTF8.name());
            String encPass = URLEncoder.encode(pass, Helper.CHARSET_UTF8.name());
            String postData = "wpName=" + encUser + "&wpPassword=" + encPass + "&wpLoginattempt=Log+in";
            System.out.println("登录：" + postData + "，甜饼：" + cookie.toString());

            // login
            URL loginUrl = new URL(URL_TERMWIKI
                    + "/index.php?title=Special:UserLogin&action=submitlogin&type=login&returnto=Home");
            HttpURLConnection conn = (HttpURLConnection) loginUrl.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; utf-8");
            conn.setRequestProperty("Cookie", cookie.toString());
            conn.setRequestProperty("Referer", URL_TERMWIKI + "/mainpage.php?returnto=Home&p=Login");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1)");

            conn.setUseCaches(false);

            OutputStream out = null;
            try {
                out = conn.getOutputStream();
                out.write(postData.getBytes(Helper.CHARSET_UTF8));
            } finally {
                Helper.close(out);
            }

            headers = conn.getHeaderFields();
            keys = headers.keySet();
            if (DEBUG) {
                for (String k : keys) {
                    System.out.println(k + "=" + headers.get(k));
                }
            }
            addCookies(cookie, headers);

            if (cookie.indexOf(user) != -1) {
                sessionCookie = cookie.toString();
            } else {
                sessionCookie = null;
            }
            if (sessionCookie != null) {
                System.out.println("登录成功：" + sessionCookie);
                return true;
            } else {
                System.out.println("登录失败：" + user + " / " + pass);
                return false;
            }
        }
    }

    private static final void addCookies(StringBuffer cookie, Map<String, List<String>> headers) {
        List<String> values = headers.get("Set-Cookie");
        if (values != null) {
            for (String v : values) {
                if (v.indexOf("deleted") == -1) {
                    if (cookie.length() > 0) {
                        cookie.append("; ");
                    }
                    cookie.append(v.split(";")[0]);
                }
            }
        }
    }
}
