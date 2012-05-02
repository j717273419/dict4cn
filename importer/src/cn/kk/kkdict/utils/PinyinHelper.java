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
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

import cn.kk.kkdict.beans.FormattedTreeMap;

public class PinyinHelper {
    private static final Map<Integer, String> CODEPOINT_2_PINYIN_MAP = new FormattedTreeMap<Integer, String>();
    public static final String[] FEN_MU = { "c", "d", "b", "f", "g", "h", "ch", "j", "k", "l", "m", "n", "", "p", "q",
            "r", "s", "t", "sh", "zh", "w", "x", "y", "z" };

    public static final String[] YUN_MU = { "uang", "iang", "iong", "ang", "eng", "ian", "iao", "ing", "ong", "uai",
            "uan", "ai", "an", "ao", "ei", "en", "er", "ua", "ie", "in", "iu", "ou", "ia", "ue", "ui", "un", "uo", "a",
            "e", "i", "o", "u", "v" };

    public static final boolean checkValidPinyin(String pinyin) {
        final String[] parts = pinyin.split(Helper.SEP_PINYIN);
        for (String part : parts) {
            if (null == getShenMuYunMu(part)) {
                return false;
            }
        }
        return true;
    }

    public static final String[] getShenMuYunMu(String pinyin) {
        for (String s : FEN_MU) {
            if (pinyin.startsWith(s)) {
                for (String y : YUN_MU) {
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
     *            chinese input
     * @return pinyin (with tones)
     */
    public static String getGooglePinyin(final String input) {
        BufferedReader reader = null;
        try {
            URL url = new URL("http://translate.google.com/translate_a/t?client=t&text=" + input + "&sl=zh-CN&tl=de");
            URLConnection conn = url.openConnection();
            conn.addRequestProperty("User-Agent", "Mozilla/5.0");
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = reader.readLine();
            if (line != null) {
                return Helper.substringBetweenNarrow(line, "\"", "\"]]").toLowerCase()
                        .replace(" ", Helper.EMPTY_STRING);
            }
        } catch (Throwable e) {
            System.err.println("Failed to get google pinyin '" + input + "': " + e.toString());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // silently ignore
                }
            }
        }
        return null;
    }

    private static void createCharToPinyinMap() {
        final String char2pinyinFile = "char2pinyin.txt";
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(PinyinHelper.class.getResourceAsStream("/"
                    + char2pinyinFile)));
            String line;
            while (null != (line = reader.readLine())) {
                String[] parts = line.split(Helper.SEP_PARTS);
                // System.out.println(line + "/" + parts[0] + ": " + parts.length + ", " + parts[0].trim().length());
                if (parts.length == 2) {
                    if (parts[0].trim().length() == 1) {
                        CODEPOINT_2_PINYIN_MAP.put(Integer.valueOf(parts[0].trim().codePointAt(0)), parts[1].trim());
                    } else {
                        System.err.println("Invalid entry in " + char2pinyinFile + ": " + line);
                    }
                }
            }
            reader.close();
        } catch (IOException e) {
            System.out.println("Failed to load " + char2pinyinFile + "!");
        }
    }

    /**
     * Gets pinyin per character
     * 
     * @param input
     * @return
     */
    public static final String getPinyin(final String input) {
        if (CODEPOINT_2_PINYIN_MAP.isEmpty()) {
            createCharToPinyinMap();
        }
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (int i = 0; i < input.length(); i++) {
            if (first) {
                first = false;
            } else {
                sb.append(Helper.SEP_PINYIN);
            }
            String pinyin = CODEPOINT_2_PINYIN_MAP.get(Integer.valueOf(input.codePointAt(i)));
            if (pinyin != null) {
                sb.append(pinyin);
            } else {
                System.err.println("Failed to get pinyin for character: '" + input.substring(i, i + 1) + "'");
            }
        }
        return sb.toString();
    }
}
