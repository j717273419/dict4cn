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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.zip.InflaterOutputStream;

import cn.kk.kkdict.Configuration;
import cn.kk.kkdict.Configuration.Source;
import cn.kk.kkdict.types.Category;
import cn.kk.kkdict.types.Language;
import cn.kk.kkdict.types.WordSource;
import cn.kk.kkdict.utils.ChineseHelper;
import cn.kk.kkdict.utils.Helper;
import cn.kk.kkdict.utils.PinyinHelper;

public class QQPinyinQpydExtractor {
    public static final String IN_DIR = Configuration.IMPORTER_FOLDER_SELECTED_WORDS.getPath(Source.WORD_QQ);
    public static final String OUT_FILE = Configuration.IMPORTER_FOLDER_EXTRACTED_WORDS.getFile(Source.WORD_QQ, "output-words." + WordSource.QQ_QPYD.key);

    public static void main(String[] args) throws IOException {
        File directory = new File(IN_DIR);
        if (directory.isDirectory()) {
            System.out.print("搜索QPYD文件'" + IN_DIR + "' ... ");
            BufferedWriter writer = new BufferedWriter(new FileWriter(OUT_FILE), Helper.BUFFER_SIZE);

            File[] files = directory.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".qpyd");
                }
            });
            System.out.println(files.length);
            
            int total = 0;
            String tmp;
            for (File f : files) {
                System.out.print("读取QPYD文件'" + f + "' ... ");
                Set<String> categories = Collections.emptySet();
                if (null != (tmp = Helper.substringBetween(f.getName(), "_", ".qpyd"))) {
                    categories = Category.parseValid(tmp.split("_"));
                }
                int counter = extractQpydToFile(f, writer, categories);
                System.out.println(counter);
                total += counter;
            }

            writer.close();
            System.out.println("\n=====================================");
            System.out.println("总共读取了" + files.length + "个QQ输入法文件");
            System.out.println("总共词汇：" + total);
            System.out.println("=====================================");
        }
    }

    private static int extractQpydToFile(File qpydFile, BufferedWriter writer, Set<String> categories) throws IOException {
        int counter = 0;

        // read qpyd into byte array
        FileChannel fChannel = new RandomAccessFile(qpydFile, "r").getChannel();
        ByteBuffer dataRawBytes = ByteBuffer.allocate((int) fChannel.size());
        fChannel.read(dataRawBytes);
        fChannel.close();
        dataRawBytes.order(ByteOrder.LITTLE_ENDIAN);
        dataRawBytes.rewind();

        // read info of compressed data
        int startZippedDictAddr = dataRawBytes.getInt(0x38);
        int zippedDictLength = dataRawBytes.limit() - startZippedDictAddr;

        // read zipped qqyd dictionary into byte array
        ByteArrayOutputStream dataOut = new ByteArrayOutputStream();
        Channels.newChannel(new InflaterOutputStream(dataOut)).write(
                ByteBuffer.wrap(dataRawBytes.array(), startZippedDictAddr, zippedDictLength));

        // uncompressed qqyd dictionary as bytes
        ByteBuffer dataUnzippedBytes = ByteBuffer.wrap(dataOut.toByteArray());
        dataUnzippedBytes.order(ByteOrder.LITTLE_ENDIAN);

        // stores the start address of actual dictionary data
        int unzippedDictStartAddr = -1;
        byte[] byteArray = dataUnzippedBytes.array();
        dataUnzippedBytes.position(0);
        while (unzippedDictStartAddr == -1 || dataUnzippedBytes.position() < unzippedDictStartAddr) {
            // read word
            int pinyinLength = dataUnzippedBytes.get() & 0xff;
            int wordLength = dataUnzippedBytes.get() & 0xff;
            dataUnzippedBytes.getInt(); // garbage
            int pinyinStartAddr = dataUnzippedBytes.getInt();
            
            int wordStartAddr = pinyinStartAddr + pinyinLength;            
            if (unzippedDictStartAddr == -1) {
                unzippedDictStartAddr = pinyinStartAddr;
            }

            String pinyin = new String(Arrays.copyOfRange(byteArray, pinyinStartAddr, pinyinStartAddr + pinyinLength),
                    Helper.CHARSET_UTF8);
            String word = new String(Arrays.copyOfRange(byteArray, wordStartAddr, wordStartAddr + wordLength),
                    Helper.CHARSET_UTF16LE);
            if (PinyinHelper.checkValidPinyin(pinyin)) {
                writer.write(Language.ZH.key);
                writer.write(Helper.SEP_DEFINITION);
                writer.write(Helper.appendCategories(ChineseHelper.toSimplifiedChinese(cleanWord(word)), categories));
                writer.write(Helper.SEP_ATTRIBUTE);
                writer.write(WordSource.TYPE_ID);
                writer.write(WordSource.QQ_QPYD.key);
                writer.write(Helper.SEP_PARTS);
                writer.write(pinyin);
                writer.write(Helper.SEP_NEWLINE);
                counter++;
            }
        }
        return counter;
    }

    private static String cleanWord(String word) {
        return word.replaceAll("\\(.*\\)", Helper.EMPTY_STRING);
    }

}
