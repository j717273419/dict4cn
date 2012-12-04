package cn.kk.kkdict.extraction.crawl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import cn.kk.kkdict.Configuration;
import cn.kk.kkdict.Configuration.Source;
import cn.kk.kkdict.utils.Helper;

public class WoerterbuchUebersetzungCrawler {

  public static final String OUT_DIR            = Configuration.IMPORTER_FOLDER_EXTRACTED_CRAWLED.getPath(Source.WORD_BABLA);
  public static final String OUTPUT_FILE_PREFIX = "output-wud";

  public static final String OUTPUT_FILE_SUFFIX = ".wud";

  public static class Info {
    public final static String SUFFIX = ".html";

    final String               src;

    final String               tgt;

    final String               url;

    public Info(String src, String tgt, String url) {
      this.src = src;
      this.tgt = tgt;
      this.url = url;
    }
  }

  private static Info[] tasks = new Info[] { new Info("zh", "de", "http://www.woerterbuch-uebersetzung.de/chinesisch-deutsch/eintrag-ab-vokabel-"),
      new Info("zh", "it", "http://www.woerterbuch-uebersetzung.de/chinesisch-italienisch/eintrag-ab-vokabel-"),
      new Info("zh", "ko", "http://www.woerterbuch-uebersetzung.de/chinesisch-koreanisch/eintrag-ab-vokabel-"),
      new Info("zh", "pl", "http://www.woerterbuch-uebersetzung.de/chinesisch-polnisch/eintrag-ab-vokabel-"),
      new Info("zh", "ru", "http://www.woerterbuch-uebersetzung.de/chinesisch-russisch/eintrag-ab-vokabel-"),
      new Info("zh", "es", "http://www.woerterbuch-uebersetzung.de/chinesisch-russisch/eintrag-ab-vokabel-"),
      new Info("de", "zh", "http://www.woerterbuch-uebersetzung.de/deutsch-chinesisch/eintrag-ab-vokabel-"),
      new Info("fr", "zh", "http://www.woerterbuch-uebersetzung.de/franzoesisch-chinesisch/eintrag-ab-vokabel-"),
      new Info("ja", "zh", "http://www.woerterbuch-uebersetzung.de/japanisch-chinesisch/eintrag-ab-vokabel-"),
      new Info("nl", "zh", "http://www.woerterbuch-uebersetzung.de/niederlaendisch-chinesisch/eintrag-ab-vokabel-"),
      new Info("pt", "zh", "http://www.woerterbuch-uebersetzung.de/portugiesisch-chinesisch/eintrag-ab-vokabel-"),
      new Info("se", "zh", "http://www.woerterbuch-uebersetzung.de/schwedisch-chinesisch/eintrag-ab-vokabel-"),

                              };

  public static void main(String[] args) throws MalformedURLException, IOException, InterruptedException {
    for (Info info : WoerterbuchUebersetzungCrawler.tasks) {
      File file = new File(WoerterbuchUebersetzungCrawler.OUT_DIR, WoerterbuchUebersetzungCrawler.OUTPUT_FILE_PREFIX + "_" + info.src + "_" + info.tgt
          + WoerterbuchUebersetzungCrawler.OUTPUT_FILE_SUFFIX);
      try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));) {
        TimeUnit.MILLISECONDS.sleep(100);
        System.out.println("产生：" + info.src + "->" + info.tgt);
        System.out.print("输出：" + file.getAbsolutePath());
        int total = 0;
        for (int i = 0;; i += 49) {
          final String url = info.url + i + Info.SUFFIX;
          int count = 0;
          HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
          HttpURLConnection.setFollowRedirects(false);
          conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:16.0) Gecko/20100101 Firefox/16.0");
          try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));) {
            String line;
            boolean started = false;
            while ((line = in.readLine()) != null) {
              if (line.contains("<ul>")) {
                started = true;
              }
              if (started) {
                int endIdx = line.indexOf("</a> = ");
                int startIdx = line.lastIndexOf(".html\">", endIdx);
                if ((endIdx != -1) && (startIdx != -1)) {
                  startIdx += ".html\">".length();
                  String srcVal = line.substring(startIdx, endIdx);

                  startIdx = endIdx + "</a> = ".length();
                  endIdx = line.indexOf("</li>", startIdx);
                  if ((endIdx != -1) && (startIdx != -1)) {
                    String tgtVal = line.substring(startIdx, endIdx);
                    int idx;
                    if ("zh".equals(info.src) && "de".equals(info.tgt) && ((idx = srcVal.indexOf(' ')) > 0)) {
                      srcVal = srcVal.substring(idx);
                    }
                    if ("zh".equals(info.tgt) && "de".equals(info.src) && ((idx = tgtVal.indexOf(' ')) > 0)) {
                      tgtVal = tgtVal.substring(idx);
                    }
                    srcVal = srcVal.replaceAll(" \\(.+?\\)", "").trim();
                    tgtVal = tgtVal.replaceAll(" \\(.+?\\)", "").trim();
                    if ((srcVal.length() > 0) && (tgtVal.length() > 0)) {
                      // System.out.println(result);
                      final String result = info.src + Helper.SEP_DEFINITION + srcVal + Helper.SEP_LIST + info.tgt + Helper.SEP_DEFINITION + tgtVal
                          + Helper.SEP_NEWLINE;
                      out.write(result);
                    }
                    count++;
                  }
                }
              }
              if (line.contains("</ul>")) {
                started = false;
              }
            }
          }
          if (count == 0) {
            break;
          } else {
            total += count;
            System.out.print(".");
          }
          out.flush();
        }
        System.out.println(" - 总：" + total);
      }
    }
  }
}
