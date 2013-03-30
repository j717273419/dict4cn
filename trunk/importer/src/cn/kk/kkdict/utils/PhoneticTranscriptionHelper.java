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
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.Map;

import cn.kk.kkdict.beans.FormattedTreeMap;
import cn.kk.kkdict.types.GoogleLanguage;
import cn.kk.kkdict.types.Language;
import cn.kk.kkdict.utils.Helper;

public class PhoneticTranscriptionHelper {
  private static final Map<Integer, String> CODEPOINT_2_PINYIN_MAP = new FormattedTreeMap<>();
  public static final String[]              FEN_MU                 = { "c", "d", "b", "f", "g", "h", "ch", "j", "k", "l", "m", "n", "", "p", "q", "r", "s",
      "t", "sh", "zh", "w", "x", "y", "z"                         };

  public static final String[]              YUN_MU                 = { "uang", "iang", "iong", "ang", "eng", "ian", "iao", "ing", "ong", "uai", "uan", "ai",
      "an", "ao", "ei", "en", "er", "ua", "ie", "in", "iu", "ou", "ia", "ue", "ui", "un", "uo", "a", "e", "i", "o", "u", "v" };

  public static final boolean checkValidPinyin(final String pinyin) {
    final String[] parts = pinyin.split(Helper.SEP_PINYIN);
    for (final String part : parts) {
      if (null == PhoneticTranscriptionHelper.getShenMuYunMu(part)) {
        return false;
      }
    }
    return true;
  }

  public static final String[] getShenMuYunMu(final String pinyin) {
    for (final String s : PhoneticTranscriptionHelper.FEN_MU) {
      if (pinyin.startsWith(s)) {
        for (final String y : PhoneticTranscriptionHelper.YUN_MU) {
          if (pinyin.endsWith(y)) {
            if (pinyin.equals(s + y)) {
              return new String[] { s, y };
            }
          }
        }
      }
    }
    return null;
  }

  /**
   * Converts input text to hanyu pinyin or whatever google returns
   * 
   * @param input
   *          chinese input
   * @return pinyin (with tones)
   */
  private static final String getPhoneticTranscriptionGoogleTranslation(final Language lng, final String input) {
    final GoogleLanguage gLng = GoogleLanguage.LNG_MAPPING.get(lng);
    if (gLng != null) {
      BufferedReader reader = null;
      try {
        final String url = String.format(PhoneticTranscriptionHelper.PT_GOOGLE_REQUEST, input, gLng.key);
        reader = new BufferedReader(new InputStreamReader(Helper.openUrlInputStream(url), Helper.CHARSET_UTF8));
        final String line = reader.readLine();
        if (line != null) {
          return Helper.substringBetweenNarrow(line, "\"", "\"]]").toLowerCase().replace(" ", Helper.EMPTY_STRING);
        }
      } catch (final Throwable e) {
        System.err.println("在Google翻译上查询'" + input + "'的注音时出错：" + e.toString());
      } finally {
        Helper.close(reader);
      }
    }
    return null;
  }

  private static void createCharToPinyinMap() {
    final String char2pinyinFile = "char2pinyin.txt";
    try (final BufferedReader reader = new BufferedReader(new InputStreamReader(PhoneticTranscriptionHelper.class.getResourceAsStream("/" + char2pinyinFile)));) {
      String line;
      while (null != (line = reader.readLine())) {
        final String[] parts = line.split(Helper.SEP_PARTS);
        // System.out.println(line + "/" + parts[0] + ": " + parts.length + ", " + parts[0].trim().length());
        if (parts.length == 2) {
          if (parts[0].trim().length() == 1) {
            PhoneticTranscriptionHelper.CODEPOINT_2_PINYIN_MAP.put(Integer.valueOf(parts[0].trim().codePointAt(0)), parts[1].trim());
          } else {
            System.err.println("Invalid entry in " + char2pinyinFile + ": " + line);
          }
        }
      }
      reader.close();
    } catch (final IOException e) {
      System.out.println("载入" + char2pinyinFile + "文件时出错：" + e.toString());
    }
  }

  /**
   * Gets pinyin per character
   * 
   * @param input
   * @return
   */
  public static final String getPinyin(final String input) {
    if (PhoneticTranscriptionHelper.CODEPOINT_2_PINYIN_MAP.isEmpty()) {
      PhoneticTranscriptionHelper.createCharToPinyinMap();
    }
    final StringBuilder sb = new StringBuilder();
    boolean first = true;
    for (int i = 0; i < input.length(); i++) {
      if (first) {
        first = false;
      }
      final String pinyin = PhoneticTranscriptionHelper.CODEPOINT_2_PINYIN_MAP.get(Integer.valueOf(input.codePointAt(i)));
      if (pinyin != null) {
        sb.append(pinyin);
      } else {
        sb.append(input.substring(i, i + 1));
      }
    }
    return sb.toString();
  }

  // alternative sites: http://upodn.com/phon.php, http://tom.brondsted.dk/text2phoneme/,
  // http://www.photransedit.com/Online/Text2Phonetics.aspx
  private static final String PT_MODELINO_REQUEST   = "http://project-modelino.com/english-phonetic-transcription-converter.php?site_language=english&initial_text=%s&submit=Submit&MM_update2=form2";
  private static final String PT_MODELINO_FOUND_KEY = "final_transcription";

  private static final String PT_GOOGLE_REQUEST     = "http://translate.google.com/translate_a/t?client=t&text=%s&sl=%s&tl=de";

  private static final String PT_TOM_REQUEST        = "http://tom.brondsted.dk/text2phoneme/transcribeit.php";
  private static final String PT_TOM_PARAMS         = "txt=%s&language=%s&alphabet=IPA";
  private static final String PT_TOM_FOUND_KEY      = "IPA transcription (phonemes):";

  /**
   * 
   * @return IPA string
   * @throws IOException
   */
  public static final String getPhoneticTranscription(final Language lng, final String input) {
    String ipa;
    try {
      switch (lng) {
        case EN:
          ipa = PhoneticTranscriptionHelper.getPhoneticTranscriptionModelino(input);
          if (ipa == null) {
            ipa = PhoneticTranscriptionHelper.getPhoneticTranscriptionTom(lng, input);
          }
          break;
        case DA:
          ipa = PhoneticTranscriptionHelper.getPhoneticTranscriptionTom(lng, input);
          break;
        case DE:
          ipa = PhoneticTranscriptionHelper.getPhoneticTranscriptionTom(lng, input);
          break;
        default:
          ipa = null;
      }
    } catch (final Throwable e) {
      System.err.println("查询'" + input + "'的注音时出错：" + e.toString());
      return null;
    }
    if (ipa == null) {
      ipa = PhoneticTranscriptionHelper.getPhoneticTranscriptionGoogleTranslation(lng, input);
    }
    return ipa;
  }

  private static final String getPhoneticTranscriptionTom(final Language lng, final String input) throws IOException {
    BufferedReader in = null;
    try {
      final String key;
      switch (lng) {
        case EN:
          key = "english";
          break;
        case DE:
          key = "german";
          break;
        case DA:
          key = "danish";
          break;
        default:
          key = null;
      }
      if (key != null) {
        final String request = PhoneticTranscriptionHelper.PT_TOM_REQUEST;
        final String params = String.format(PhoneticTranscriptionHelper.PT_TOM_PARAMS, URLEncoder.encode(input, Helper.CHARSET_UTF8.name()), key);
        // System.out.println(request + "; " + params);
        in = new BufferedReader(new InputStreamReader(Helper.openUrlInputStream(request, true, params), "ISO-8859-1"));
        String line;
        while (null != (line = in.readLine())) {
          if (-1 != line.indexOf(PhoneticTranscriptionHelper.PT_TOM_FOUND_KEY)) {
            String ipa = Helper.substringBetweenLast(line, "<br />", "<br />");
            if (ipa != null) {
              ipa = Helper.unescapeHtml(ipa.replaceAll(" ", Helper.EMPTY_STRING));
              System.out.println(ipa);
              return ipa;
            }
            break;
          }
        }
      }
    } finally {
      Helper.close(in);
    }
    System.err.println("在Tom上查询'" + input + "'的注音时出错。");
    return null;
  }

  private static final String getPhoneticTranscriptionModelino(final String input) throws IOException {
    final String request = String.format(PhoneticTranscriptionHelper.PT_MODELINO_REQUEST, URLEncoder.encode(input, Helper.CHARSET_UTF8.name()));
    BufferedReader in = null;
    try {
      in = new BufferedReader(new InputStreamReader(Helper.openUrlInputStream(request), Helper.CHARSET_UTF8));
      String line;
      boolean found = false;
      while (null != (line = in.readLine())) {
        if (found) {
          int openIdx = line.indexOf('>');
          final int closeIdx = line.indexOf('<');
          if (closeIdx != -1) {
            if (closeIdx < openIdx) {
              openIdx = 0;
            }
            final String ipa = line.substring(openIdx, closeIdx).trim();
            return ipa;
          }
        }
        if (-1 != line.indexOf(PhoneticTranscriptionHelper.PT_MODELINO_FOUND_KEY)) {
          found = true;
          continue;
        }
      }
    } finally {
      Helper.close(in);
    }
    System.err.println("在Modelino上查询'" + input + "'的注音时出错。");
    return null;
  }
}
