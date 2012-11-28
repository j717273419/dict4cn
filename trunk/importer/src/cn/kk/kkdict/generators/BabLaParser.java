package cn.kk.kkdict.generators;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cn.kk.kkdict.Configuration;
import cn.kk.kkdict.Configuration.Source;
import cn.kk.kkdict.types.Language;
import cn.kk.kkdict.types.TranslationSource;
import cn.kk.kkdict.types.UriLocation;
import cn.kk.kkdict.utils.Helper;

public class BabLaParser {
  private static final String  URL    = "http://en.bab.la/dictionary/";

  private static final String  OUTDIR = Configuration.IMPORTER_FOLDER_RAW_WORDS.getPath(Source.WORD_BABLA);

  private static final String  PREFIX = "words_";

  private static final boolean DEBUG  = false;

  public static void main(final String[] args) throws IOException, InterruptedException {
    final List<String[]> available = BabLaParser.parseAvailableTranslations();
    for (final String[] entry : available) {
      if (BabLaParser.DEBUG) {
        System.out.println(entry[0] + "->" + entry[1] + ": " + entry[2]);
      }
      BabLaParser.parseTranslationList(entry[0], entry[1], BabLaParser.URL + "/" + entry[2]);
    }
  }

  private static void parseTranslationList(final String from, final String to, final String url) throws IOException, InterruptedException {
    // map lng
    final Language lngFrom = BabLaParser.getLanguage(from);
    final Language lngTo = BabLaParser.getLanguage(to);
    // write file
    final String file = BabLaParser.OUTDIR + "/" + BabLaParser.PREFIX + lngFrom.getKey() + "_" + lngTo.getKey() + "." + TranslationSource.BABLA.key;
    if (Helper.isEmptyOrNotExists(file)) {
      final long start = System.currentTimeMillis();
      System.out.print("创建文件：" + file + " 。。。 ");
      try (final BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file), Helper.BUFFER_SIZE);) {
        long total = 0;

        for (char c = 'a'; c <= 'z'; c++) {
          int pageNr = 1;
          int retries = 0;
          while (true) {
            int words = 0;
            try (final BufferedReader reader = new BufferedReader(new InputStreamReader(Helper.openUrlInputStream(url + c + "/" + pageNr), Helper.CHARSET_UTF8));) {
              String line;
              while (null != (line = reader.readLine())) {
                if (line.contains("<div class=\"result-wrapper\">")) {
                  int nextHrefStart = 0;
                  int nextHrefClose = 0;
                  int nextAnchorClose = 0;
                  while (true) {
                    nextHrefStart = line.indexOf("href=\"", nextAnchorClose);
                    if (nextHrefStart != -1) {
                      nextHrefStart += "href=\"".length();
                      nextHrefClose = line.indexOf("\">", nextHrefStart);
                      final String wordUrl = line.substring(nextHrefStart, nextHrefClose);
                      final int nextAnchorStart = nextHrefClose + "\">".length();
                      nextAnchorClose = line.indexOf("</a>", nextAnchorStart);
                      if ((nextAnchorStart != -1) && (nextAnchorClose != -1)) {
                        try {
                          final String substring = line.substring(nextAnchorStart, nextAnchorClose);
                          final String word = substring.trim();
                          out.write(lngFrom.getKeyBytes());
                          out.write(Helper.SEP_DEFINITION_BYTES);
                          out.write(word.getBytes(Helper.CHARSET_UTF8));
                          out.write(Helper.SEP_ATTRS_BYTES);
                          out.write(UriLocation.TYPE_ID_BYTES);
                          out.write(wordUrl.getBytes(Helper.CHARSET_UTF8));
                          out.write(Helper.SEP_NEWLINE_BYTES);
                          if (BabLaParser.DEBUG) {
                            System.out.println("新词：" + word + "，网址：" + wordUrl);
                          }
                          words++;
                        } catch (final RuntimeException e) {
                          System.err.println(e.toString());
                        }
                        continue;
                      }
                    }
                    break;
                  }
                }
              }
              if (words == 0) {
                break;
              }
              total += words;
              pageNr++;
            } catch (Exception e) {
              retries++;
              if (retries > 3) {
                e.printStackTrace();
                break;
              } else {
                TimeUnit.SECONDS.sleep(10 * retries);
                System.out.println("出现错误。重试。。。 " + e.toString());
              }
            }
          }
        }
        System.out.println("共" + total + "词组，花时：" + Helper.formatDuration(System.currentTimeMillis() - start));
      }
    } else {
      System.out.println("跳过：" + file + "，文件已存在。");
    }
  }

  private final static Language getLanguage(final String lng) {
    if (lng.equals("cn")) {
      return Language.ZH;
    } else {
      return Language.fromKey(lng);
    }
  }

  private static List<String[]> parseAvailableTranslations() throws IOException {
    final List<String[]> available = new LinkedList<>();
    try (final BufferedReader reader = new BufferedReader(new InputStreamReader(Helper.openUrlInputStream(BabLaParser.URL), Helper.CHARSET_UTF8));) {
      String line;
      while (null != (line = reader.readLine())) {
        if (line.contains("</span><span class=\"babFlag ")) {
          int nextSpanClose = 0;
          int lastSpanStart;
          int nextHrefStart;
          int nextHrefClose;

          while (true) {
            nextSpanClose = line.indexOf("</span>", nextSpanClose + 1);
            if (nextSpanClose != -1) {
              lastSpanStart = line.lastIndexOf(">", nextSpanClose) + 1;
              final String from = line.substring(lastSpanStart, nextSpanClose);
              nextSpanClose = line.indexOf("</span>", nextSpanClose + 1);
              if (nextSpanClose != -1) {
                lastSpanStart = line.lastIndexOf(">", nextSpanClose) + 1;
                final String to = line.substring(lastSpanStart, nextSpanClose);
                nextHrefStart = line.indexOf("href=\"", nextSpanClose + 1);
                if (nextHrefStart != -1) {
                  nextHrefStart += "href=\"".length();
                  nextHrefClose = line.indexOf("\">", nextHrefStart);
                  final String url = line.substring(nextHrefStart, nextHrefClose);
                  if (BabLaParser.DEBUG) {
                    System.out.println(from + "->" + to + ": " + url);
                  }
                  available.add(new String[] { from, to, url });
                  continue;
                }
              }
            }
            break;
          }
        }
      }
    }
    return available;
  }
}
