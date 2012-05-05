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
import java.util.List;

import cn.kk.kkdict.beans.FormattedArrayList;
import cn.kk.kkdict.types.GoogleLanguage;

public final class TranslationHelper {

    private static final int MAX_RESULTS = 3;

    /**
     * Available languages: zh-CN, de, en, it, ja, ko, ru, fr, la (see
     * https://code.google.com/apis/language/translate/v2/using_rest.html#language-params)
     * 
     * @param source
     * @param target
     * @param input
     * @return
     */
    public static List<String> getGoogleTranslations(final GoogleLanguage source, final GoogleLanguage target,
            final String input) {
        BufferedReader reader = null;
        List<String> translations = null;
        try {
            URL url = new URL("http://translate.google.com/translate_a/t?client=t&text=" + input + "&sl=" + source.key
                    + "&tl=" + target.key);
            URLConnection conn = url.openConnection();
            conn.addRequestProperty("User-Agent", "Mozilla/5.0");
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = reader.readLine();
            // System.out.println(line);
            if (line != null) {
                String translationRaw = Helper.substringBetween(line, "]],[[\"", "]]");
                String[] translationParts = translationRaw.split("[\\[\\],\"]+");
                // System.out.println(Arrays.toString(translationParts));
                if (translationParts.length > 5
                        && input.replaceAll("\\b+", Helper.EMPTY_STRING).equals(
                                translationParts[0].replace(" ", Helper.EMPTY_STRING))) {
                    translations = new FormattedArrayList<String>(MAX_RESULTS);
                    for (int i = 2; i < translationParts.length; i += 4) {
                        if (Integer.parseInt(translationParts[i + 1]) > 0) {
                            translations.add(translationParts[i]);
                        }
                        if (translations.size() == MAX_RESULTS) {
                            break;
                        }
                    }
                }
            }
        } catch (Throwable e) {
            System.err.println("Failed to get google translations '" + input + "': " + e.toString());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // silently ignore
                }
            }
        }
        if (translations != null && !translations.isEmpty()) {
            return translations;
        } else {
            return Helper.EMPTY_STRING_LIST;
        }
    }

}
