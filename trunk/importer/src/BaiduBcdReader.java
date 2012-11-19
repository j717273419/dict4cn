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
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

import cn.kk.kkdict.utils.Helper;

/**
 * Baidu Pinyin IME BDICT File Reader
 * 
 * <pre>
 * BDICT Format overview:
 * 
 * General Information:
 * - Chinese characters and pinyin are all encoded with UTF-16LE.
 * - Numbers are using little endian byte order.
 * 
 * BDICT hex analysis:
 * - 0x250         total number of words
 * - 0x350         dictionary offset
 * - 0x<Offset>    Dictionary
 * 
 * Dictionary format:
 * - It can interpreted as a list of 
 *   [amount of characters (short not integer!)
 *       pinyin construction using fenmu and yunmu,
 *       word as string 
 *   ].
 * 
 * </pre>
 * 
 * @author keke
 */
public class BaiduBcdReader {
  private static final String[] FEN_MU = { "c", "d", "b", "f", "g", "h", "ch", "j", "k", "l", "m", "n", "", "p", "q", "r", "s", "t", "sh", "zh", "w", "x", "y",
      "z"                             };
  private static final String[] YUN_MU = { "uang", "iang", "ong", "ang", "eng", "ian", "iao", "ing", "ong", "uai", "uan", "ai", "an", "ao", "ei", "en", "er",
      "ua", "ie", "in", "iu", "ou", "ia", "ue", "ui", "un", "uo", "a", "e", "i", "a", "u", "v" };

  public static void main(final String[] args) {
    // download from http://r6.mo.baidu.com/web/iw/index/
    final String bdictFile = "d:\\sysdict.dat";

    BaiduBcdReader.analyze(bdictFile);
  }

  @SuppressWarnings("resource")
  private static void analyze(final String bdictFile) {
    // read bdict into byte array
    RandomAccessFile file = null;
    try {
      file = new RandomAccessFile(bdictFile, "r");

      final FileChannel fChannel = file.getChannel();
      final ByteBuffer dataRawBytes = ByteBuffer.allocate((int) fChannel.size());
      fChannel.read(dataRawBytes);
      dataRawBytes.order(ByteOrder.LITTLE_ENDIAN);
      dataRawBytes.rewind();
      fChannel.close();

      System.out.println("文件: " + bdictFile);

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
          pinyin.append(BaiduBcdReader.FEN_MU[dataRawBytes.get()] + BaiduBcdReader.YUN_MU[dataRawBytes.get()]);
        }
        dataRawBytes.get(buf, 0, 2 * length);
        final String word = new String(buf, 0, 2 * length, "UTF-16LE");
        System.out.println(word + "\t" + pinyin);
      }

      System.out.println("\nExtracted '" + bdictFile + "': " + total);
    } catch (IOException e) {
      System.err.println("Error: " + e);
    } finally {
      Helper.close(file);
    }
  }
}
