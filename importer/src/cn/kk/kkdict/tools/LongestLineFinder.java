package cn.kk.kkdict.tools;

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
import java.io.FileReader;
import java.io.IOException;

public class LongestLineFinder {
    private static final boolean DEBUG = false;
    private double showQuantile = 1;
    private int lines = 1;
    private String file;

    public LongestLineFinder(String file) {
        this(1, 1, file);
    }

    public LongestLineFinder(double showQuantile, int lines, String file) {
        super();
        this.showQuantile = showQuantile;
        this.lines = lines;
        this.file = file;
    }

    public static void main(String[] args) throws IOException {
        LongestLineFinder finder = new LongestLineFinder("O:\\kkdict\\out\\dicts\\wiki\\output-dict_categories-merged_mrg-tmp.wiki");
        System.out.println(finder.find());
    }

    public String find() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String l = null;
        int max = 0;
        while ((l = reader.readLine()) != null) {
            max = Math.max(l.length(), max);
        }
        if (DEBUG) {
            System.out.println("找到最长行：" + max + "字符");
        }
        reader.close();

        reader = new BufferedReader(new FileReader(file));
        int i = 0;
        String result = null;
        while ((l = reader.readLine()) != null) {
            if (l.length() == max) {
                result = l;
            }
            if (DEBUG) {
                if (l.length() >= max * showQuantile) {
                    if (i++ < lines) {
                        System.out.println(l.length() + "字符：" + l);
                    } else {
                        break;
                    }
                }
            } else if (result != null) {
                break;
            }
        }
        reader.close();
        return result;
    }
}
