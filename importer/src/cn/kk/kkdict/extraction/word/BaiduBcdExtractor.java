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
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.Collections;
import java.util.Set;

import cn.kk.kkdict.Configuration;
import cn.kk.kkdict.Configuration.Source;
import cn.kk.kkdict.types.Category;
import cn.kk.kkdict.types.Language;
import cn.kk.kkdict.types.WordSource;
import cn.kk.kkdict.utils.ChineseHelper;
import cn.kk.kkdict.utils.Helper;
import cn.kk.kkdict.utils.PhoneticTranscriptionHelper;

public class BaiduBcdExtractor {
  public static final String IN_DIR   = Configuration.IMPORTER_FOLDER_SELECTED_WORDS.getPath(Source.WORD_BAIDU);
  public static final String OUT_FILE = Configuration.IMPORTER_FOLDER_EXTRACTED_WORDS.getFile(Source.WORD_BAIDU, "output-words." + WordSource.BAIDU_BDICT.key);

  public static void main(final String[] args) throws IOException {
    final File directory = new File(BaiduBcdExtractor.IN_DIR);
    if (directory.isDirectory()) {
      System.out.print("搜索百度BCD文件'" + BaiduBcdExtractor.IN_DIR + "' ... ");
      final File[] files = directory.listFiles(new FilenameFilter() {
        @Override
        public boolean accept(final File dir, final String name) {
          return name.endsWith(".bcd");
        }
      });
      System.out.println(files.length);
      int total = 0;
      try (final BufferedWriter writer = new BufferedWriter(new FileWriter(BaiduBcdExtractor.OUT_FILE), Helper.BUFFER_SIZE);) {
        String tmp;
        for (final File f : files) {
          System.out.print("读取BCD文件'" + f + "' ... ");
          Set<String> categories = Collections.emptySet();
          if (null != (tmp = Helper.substringBetween(f.getName(), "_", ".bcd"))) {
            categories = Category.parseValid(tmp.split("_"));
          }
          final int counter = BaiduBcdExtractor.extractBdictToFile(f, writer, categories);
          System.out.println(counter);
          total += counter;
        }

      }
      System.out.println("\n=====================================");
      System.out.println("总共读取了" + files.length + "个百度BCD文件");
      System.out.println("有效词组：" + total);
      System.out.println("=====================================");
    }
  }

  private static int extractBdictToFile(final File bcdFile, final BufferedWriter writer, final Set<String> categories) throws IOException {
    int counter = 0;

    // read bcds into byte array
    try (RandomAccessFile f = new RandomAccessFile(bcdFile, "r"); final FileChannel fChannel = f.getChannel();) {
      final ByteBuffer dataRawBytes = ByteBuffer.allocate((int) fChannel.size());
      fChannel.read(dataRawBytes);
      fChannel.close();
      dataRawBytes.order(ByteOrder.LITTLE_ENDIAN);
      dataRawBytes.rewind();

      final byte[] buf = new byte[1024];
      final int total = dataRawBytes.getInt(0x250);
      // dictionary offset
      dataRawBytes.position(0x350);
      for (int i = 0; i < total; i++) {
        final int length = dataRawBytes.getShort();
        dataRawBytes.getShort();
        boolean first = true;
        final StringBuilder pinyin = new StringBuilder();
        for (int j = 0; j < length; j++) {
          if (first) {
            first = false;
          } else {
            pinyin.append('\'');
          }
          pinyin.append(PhoneticTranscriptionHelper.FEN_MU[dataRawBytes.get()] + PhoneticTranscriptionHelper.YUN_MU[dataRawBytes.get()]);
        }
        dataRawBytes.get(buf, 0, 2 * length);
        final String word = new String(buf, 0, 2 * length, "UTF-16LE");

        writer.write(Language.ZH.key);
        writer.write(Helper.SEP_DEFINITION);
        writer.write(Helper.appendCategories(ChineseHelper.toSimplifiedChinese(word), categories));
        writer.write(Helper.SEP_ATTRIBUTE);
        writer.write(WordSource.TYPE_ID);
        writer.write(WordSource.BAIDU_BDICT.key);
        writer.write(Helper.SEP_PARTS);
        writer.write(pinyin.toString());
        writer.write(Helper.SEP_NEWLINE);

        counter++;
      }
    }
    return counter;
  }

}
