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
package cn.kk.kkdict.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import cn.kk.kkdict.types.GoogleLanguage;

public final class TranslationHelper {

  private static int GC = 0;

  /**
   * Available languages: zh-CN, de, en, it, ja, ko, ru, fr, la (see https://code .google.com/apis/language/translate/v2/using_rest.html#language-params)
   * 
   * @param source
   * @param target
   * @param input
   * @return
   * @throws Exception
   */
  public static List<String> getGoogleTranslations(final GoogleLanguage source, final GoogleLanguage target, final String input) throws Exception {
    List<String> translations = new ArrayList<>();

    final String url = String.format("http://translate.google.com/translate_a/t?client=t&text=%s&sl=%s&tl=%s",
        URLEncoder.encode(input, Helper.CHARSET_UTF8.name()), source.key, target.key);
    final HttpURLConnection conn = Helper.getUrlConnection(url);
    conn.setRequestProperty("User-Agent", "Mozilla/5.0 (rev. " + TranslationHelper.GC++ + ")");
    String line = null;
    int counter = 0;

    do {
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));) {
        if (conn.getResponseCode() == 302) {
          Helper.changeIP();
          Thread.sleep(30000);
          throw new RuntimeException("Google limit reached!");
        }
        line = reader.readLine();
      } catch (final Exception e) {
        System.err.println("查询谷歌翻译服务失败：'" + input + "': " + e.toString());
        if (counter > 10) {
          throw e;
        }
      }
      if (counter > 0) {
        try {
          Thread.sleep(10000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    } while ((counter++ < 20) && (null == line));
    if (line != null) {
      final int len = line.length();
      final char[] chars = line.toCharArray();
      int brackets = 0;
      int commas = 0;
      boolean str = false;
      char ch;
      char lastCh = '\n';
      boolean processed;
      int start = -1;
      int end = -1;
      int proStart = -1;
      int proEnd = -1;
      for (int i = 0; i < len; i++) {
        processed = false;
        ch = chars[i];
        if (!str) {
          if (ch == '"') {
            str = true;
            processed = true;
          }
        } else if ((ch == '"') && (lastCh != '\\')) {
          str = false;
          processed = true;
        }
        if (!processed && !str) {
          if (ch == '[') {
            brackets++;
          } else if (ch == ']') {
            brackets--;
          } else if ((ch == ',') && (brackets == 1)) {
            commas++;
            if (commas == 1) {
              proStart = i + 2;
            } else if (commas == 2) {
              proEnd = i - 1;
            } else if (commas == 5) {
              start = i + 2;
            } else if (commas == 6) {
              end = i - 1;
            }
          }
        }

        lastCh = ch;
      }
      boolean found = false;
      if ((proEnd > (proStart + 10)) && (proStart != -1)) {
        String trls = line.substring(proStart, proEnd);
        found = TranslationHelper.findGoogleTranslationsByProposals(translations, input, trls);
      }
      if (!found && (end > (start + 3 + input.length())) && (start != -1)) {
        String trls = line.substring(start, end);
        if (trls.substring(2, 2 + input.length()).equals(input)) {
          TranslationHelper.findGoogleTranslationsBySelections(translations, input, trls, line);
        }
      }
    }

    if ((translations != null) && !translations.isEmpty()) {
      return translations;
    } else {
      return Helper.EMPTY_STRING_LIST;
    }
  }

  private static boolean findGoogleTranslationsByProposals(List<String> translations, String input, String trls) {
    final int len = trls.length();
    final char[] chars = trls.toCharArray();
    int brackets = 0;
    int commas = 0;
    boolean str = false;
    char ch;
    char lastCh = '\n';
    boolean processed;
    boolean found = false;
    int proStart = -1;
    int proEnd = -1;
    for (int i = 0; i < len; i++) {
      processed = false;
      ch = chars[i];
      if (!str) {
        if (ch == '"') {
          str = true;
          processed = true;
        }
      } else if ((ch == '"') && (lastCh != '\\')) {
        str = false;
        processed = true;
      }
      if (!processed && !str) {
        if (ch == '[') {
          brackets++;
        } else if (ch == ']') {
          brackets--;
          if (brackets == 0) {
            commas = 0;
          }
        } else if ((ch == ',') && (brackets == 1)) {
          commas++;
          if (commas == 1) {
            proStart = i + 2;
          } else if (commas == 2) {
            proEnd = i - 1;
            if ((proEnd > proStart) && (proStart != -1)) {
              String proposal = trls.substring(proStart, proEnd);
              found = TranslationHelper.findGoogleTranslationsByProposal(translations, proposal);
            }
          }
        }
      }

      lastCh = ch;
    }
    return found;
  }

  private static boolean findGoogleTranslationsByProposal(List<String> translations, String trls) {
    final int len = trls.length();
    final char[] chars = trls.toCharArray();
    boolean str = false;
    char ch;
    char lastCh = '\n';
    int proStart = -1;
    int proEnd = -1;
    boolean found = false;
    for (int i = 0; i < len; i++) {
      ch = chars[i];
      if (!str) {
        if (ch == '"') {
          str = true;
          proStart = i + 1;
        }
      } else if ((ch == '"') && (lastCh != '\\')) {
        str = false;
        proEnd = i;
        if (proEnd > proStart) {
          translations.add(trls.substring(proStart, proEnd));
          found = true;
        }
      }

      lastCh = ch;
    }
    return found;
  }

  private static void findGoogleTranslationsBySelections(List<String> translations, final String input, String trls, String line) {
    final int len = trls.length();
    final char[] chars = trls.toCharArray();
    int brackets = 0;
    boolean str = false;
    char ch;
    char lastCh = '\n';
    boolean processed;
    int start = -1;
    int end = -1;
    List<String> parts = new LinkedList<String>();
    for (int i = 0; i < len; i++) {
      processed = false;
      ch = chars[i];
      if (!str) {
        if (ch == '"') {
          str = true;
          processed = true;
        }
      } else if ((ch == '"') && (lastCh != '\\')) {
        str = false;
        processed = true;
      }
      if (!processed && !str) {
        if (ch == '[') {
          brackets++;
          if (brackets == 1) {
            start = i + 1;
          }
        } else if (ch == ']') {
          brackets--;
          if (brackets == 0) {
            end = i;
            final String part = trls.substring(start, end);
            // System.out.println(part + ", " + start + ", " + end);
            if ((part.length() > (1 + input.length())) && part.substring(1, 1 + input.length()).equals(input)) {
              int open = part.indexOf("[[");
              int close = part.indexOf("]]");
              if ((open != -1) && (close > open)) {
                parts.add(part.substring(open + 1, close + 1));
              }
            }
          }
        }
      }
      lastCh = ch;
    }
    if (!parts.isEmpty()) {
      TranslationHelper.findGoogleTranslationsBySelection(translations, parts, line);
    }
  }

  private static void findGoogleTranslationsBySelection(List<String> translations, List<String> parts, String line) {
    if (parts.size() == 1) {
      String part = parts.get(0);
      final int size = TranslationHelper.getGoogleTranslationsSize(part);
      for (int i = 0; i < size; i++) {
        String trl = TranslationHelper.getGoogleTranslation(part, i, true);
        if (trl != null) {
          translations.add(trl);
        }
      }
    } else {
      translations.add(TranslationHelper.getGoogleTranslation(line.substring(2), 0, false));
    }
  }

  private static String getGoogleTranslation(String test, int idx, boolean checkPoints) {
    int counter = 0;
    final int len = test.length();
    final char[] chars = test.toCharArray();
    boolean str = false;
    char ch;
    char lastCh = '\n';
    boolean processed;
    int start = -1;
    int end = -1;
    boolean check = false;
    int chStart = -1;
    int chEnd = -1;
    for (int i = 0; i < len; i++) {
      processed = false;
      ch = chars[i];
      if (!str) {
        if (ch == '"') {
          str = true;
          processed = true;
          start = i + 1;
        }
      } else if ((ch == '"') && (lastCh != '\\')) {
        str = false;
        processed = true;
        if (counter == (idx + 1)) {
          end = i;
          if ((start != -1) && (end > start)) {
            if (checkPoints) {
              check = true;
            } else {
              return test.substring(start, end);
            }
          } else {
            return null;
          }
        }
      }
      if (!processed && !str) {
        if (ch == '[') {
          counter++;
        }
        if (check && (ch == ',')) {
          if (chStart == -1) {
            chStart = i + 1;
          } else {
            chEnd = i;
            String pointsStr = test.substring(chStart, chEnd);
            if (!pointsStr.isEmpty() && (Integer.parseInt(pointsStr) > 0)) {
              return test.substring(start, end);
            } else {
              return null;
            }
          }
        }
      }
      lastCh = ch;
    }

    return null;
  }

  private static int getGoogleTranslationsSize(String test) {
    int counter = 0;
    final int len = test.length();
    final char[] chars = test.toCharArray();
    boolean str = false;
    char ch;
    char lastCh = '\n';
    boolean processed;
    for (int i = 0; i < len; i++) {
      processed = false;
      ch = chars[i];
      if (!str) {
        if (ch == '"') {
          str = true;
          processed = true;
        }
      } else if ((ch == '"') && (lastCh != '\\')) {
        str = false;
        processed = true;
      }
      if (!processed && !str) {
        if (ch == '[') {
          counter++;
        }
      }
      lastCh = ch;
    }
    return counter;
  }

}
