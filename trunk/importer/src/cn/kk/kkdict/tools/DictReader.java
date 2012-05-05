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
package cn.kk.kkdict.tools;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import cn.kk.kkdict.Configuration;
import cn.kk.kkdict.Configuration.Source;
import cn.kk.kkdict.beans.DictByteBufferRow;
import cn.kk.kkdict.utils.ArrayHelper;

/**
 * 显示词典文件
 */
public class DictReader {
    // public static final String IN_FILE = "O:\\handedict\\output-dict_zh_de.handedict_u8";
    // public static final String IN_FILE = "O:\\handedict\\output-dict_zh_en.cedict_u8";
    public static final String IN_FILE = Configuration.IMPORTER_FOLDER_EXTRACTED_DICTS.getFile(Source.DICT_WIKIPEDIA,
            "output-dict.wiki_ak");

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(IN_FILE));
        ByteBuffer bb = ArrayHelper.borrowByteBufferLarge();

        DictByteBufferRow row = new DictByteBufferRow();
        while (-1 != ArrayHelper.readLine(in, bb)) {
            row.parseFrom(bb);

            for (int defIdx = 0; defIdx < row.size(); defIdx++) {
                System.out.println(row.toString(defIdx));
            }

            System.out.println();
        }
    }

}
