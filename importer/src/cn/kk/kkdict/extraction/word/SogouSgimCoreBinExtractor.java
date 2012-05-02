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
package cn.kk.kkdict.extraction.word;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

import cn.kk.kkdict.Configuration;
import cn.kk.kkdict.Configuration.Source;
import cn.kk.kkdict.types.Language;
import cn.kk.kkdict.types.WordSource;
import cn.kk.kkdict.utils.ChineseHelper;
import cn.kk.kkdict.utils.Helper;

/**
 * Sougou sgim_core.bin Reader
 * 
 * <pre>
 * 地址：
 * 0x0C：单词数量
 * ????：单词长度（byte），单词（编码：UTF-16LE）
 * </pre>
 * 
 * @author keke
 */
public class SogouSgimCoreBinExtractor {
    public static final String IN_FILE = Configuration.IMPORTER_FOLDER_SELECTED_WORDS.getFile(Source.WORD_SOGOU, "sgim_core.bin");
    public static final String OUT_FILE = Configuration.IMPORTER_FOLDER_EXTRACTED_WORDS.getFile(Source.WORD_SOGOU, "output-words." + WordSource.SOGOU_CORE.key);
    
    public static void main(String[] args) throws IOException {
        // read scel into byte array
        FileChannel fChannel = new RandomAccessFile(IN_FILE, "r").getChannel();
        ByteBuffer bb = ByteBuffer.allocate((int) fChannel.size());
        fChannel.read(bb);
        fChannel.close();
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.rewind();

        BufferedWriter writer = new BufferedWriter(new FileWriter(OUT_FILE), Helper.BUFFER_SIZE);
        int words = bb.getInt(0xC);
        System.out.println("读入文件: " + IN_FILE + "，单词：" + words);

        int i;
        int startPos = -1;
        while (bb.hasRemaining()) {
            i = bb.getInt();
            if (i == 0x554a0002) { // core, 6.1.0.6700
                startPos = bb.position() - 4;
                break;
            }
        }

        if (startPos > -1) {
            short s;
            int counter = 0;
            ByteBuffer buffer = ByteBuffer.allocate(Short.MAX_VALUE);
            System.out.println("单词起始位置：0x" + Integer.toHexString(startPos));
            bb.position(startPos);
            while (bb.hasRemaining() && words-- > 0) {
                s = bb.getShort();
                bb.get(buffer.array(), 0, s);                
                String str = new String(buffer.array(), 0, s, "UTF-16LE");
                if (ChineseHelper.containsChinese(str)) {
                    writer.write(Language.ZH.key);
                    writer.write(Helper.SEP_DEFINITION);
                    writer.write(ChineseHelper.toSimplifiedChinese(str));
                    writer.write(Helper.SEP_ATTRIBUTE);
                    writer.write(WordSource.TYPE_ID);
                    writer.write(WordSource.SOGOU_CORE.key);
                    writer.write(Helper.SEP_NEWLINE);
                    counter++;
                }
            }
            int endPos = bb.position();
            int diff = endPos - startPos;
            System.out.println("读出单词'" + IN_FILE + "'：" + counter);
            System.out.println("单词结尾位置：0x" + Integer.toHexString(endPos));
            System.out.println("单词词典长度：0x" + Integer.toHexString(diff)+"，"+diff+" bytes。");
        }

        writer.close();
    }
}
