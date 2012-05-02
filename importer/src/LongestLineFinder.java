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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import cn.kk.kkdict.Configuration;
import cn.kk.kkdict.Configuration.Source;

public class LongestLineFinder {
    private static final String FILE = "C:\\usr\\kkdict\\out\\dicts\\wiki\\work\\output-dict_xtr-result.wiki";
    private static final double showQuantile = 0.5;

    public static void main(String[] args) throws IOException {
        findLongestLines(Configuration.IMPORTER_FOLDER_EXTRACTED_DICTS.getFile(Source.DICT_EDICT, "output-dict_zh_de.edict_hande"), showQuantile);
    }

    /**
     * 
     * @param file
     * @param quantileOfLongest 0=all, anything between, 1=only longest
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void findLongestLines(final String file, final double quantileOfLongest)
            throws FileNotFoundException, IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String l = null;
        int max = 0;
        while ((l = reader.readLine()) != null) {
            max = Math.max(l.length(), max);
        }
        reader.close();
        System.out.println("最长行字符：" + max);

        reader = new BufferedReader(new FileReader(file));
        while ((l = reader.readLine()) != null) {
            if (l.length() >= max * quantileOfLongest) {
                System.out.println(l);
            }
        }
        reader.close();
    }
}
