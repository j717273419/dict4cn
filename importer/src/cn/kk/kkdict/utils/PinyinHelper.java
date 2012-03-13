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
