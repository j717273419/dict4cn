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
package cn.kk.dict2go.lib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.TreeMap;

public class PhoneticTranscriptionHelper {
  private static final Map<Integer, String> CODEPOINT_2_PINYIN_MAP = new TreeMap<>();
  public static final String[]              FEN_MU                 = { "c", "d", "b", "f", "g", "h", "ch", "j", "k", "l", "m", "n", "", "p", "q", "r", "s",
      "t", "sh", "zh", "w", "x", "y", "z"                         };

  public static final String[]              YUN_MU                 = { "uang", "iang", "iong", "ang", "eng", "ian", "iao", "ing", "ong", "uai", "uan", "ai",
      "an", "ao", "ei", "en", "er", "ua", "ie", "in", "iu", "ou", "ia", "ue", "ui", "un", "uo", "a", "e", "i", "o", "u", "v" };

  public final static String                SEP_PARTS              = "║";

  private static void createCharToPinyinMap() {
    final String char2pinyinFile = "char2pinyin.txt";
    try (final BufferedReader reader = new BufferedReader(new InputStreamReader(PhoneticTranscriptionHelper.class.getResourceAsStream("/" + char2pinyinFile)));) {
      String line;
      while (null != (line = reader.readLine())) {
        final String[] parts = line.split(PhoneticTranscriptionHelper.SEP_PARTS);
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
}
