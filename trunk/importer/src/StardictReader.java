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
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.zip.GZIPInputStream;

import org.apache.tools.bzip2.CBZip2InputStream;
import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarInputStream;

public class StardictReader {

  /**
   * @param args
   * @throws IOException
   * @throws FileNotFoundException
   */
  public static void main(final String[] args) throws FileNotFoundException, IOException {
    StardictReader.testDict();
    StardictReader.testIdx();
    // BufferedReader reader = new BufferedReader(new InputStreamReader(in));
    // BufferedReader reader = new BufferedReader(new InputStreamReader(new BZip2CompressorInputStream(new
    // FileInputStream("D:\\tmp\\test.tar.bz2"))));

  }

  private static void testDict() throws IOException, FileNotFoundException {
    try (final TarInputStream tarIn = new TarInputStream(new CBZip2InputStream(new FileInputStream(
        "E:\\kkdict\\raw\\dicts\\stardict\\Powerword2005\\stardict-powerword_pwpcehx-2.4.2.tar.bz2")));) {
      TarEntry entry;

      while (null != (entry = tarIn.getNextEntry())) {
        InputStream dictIn = null;
        if (entry.getName().endsWith(".dict.dz")) {
          System.out.println("解读：'" + entry.getName() + "'（" + entry.getSize() + "）。。。");
          dictIn = new GZIPInputStream(tarIn);
        } else if (entry.getName().endsWith(".dict")) {
          dictIn = tarIn;
        }
        if (dictIn != null) {
          final BufferedReader reader = new BufferedReader(new InputStreamReader(dictIn));
          final String startTag = "<单词原型><![CDATA[";
          final String endTag = "]]>";
          String line;
          int startIdx;
          int endIdx;
          while (null != (line = reader.readLine())) {
            if ((-1 != (startIdx = line.indexOf(startTag))) && (-1 != (endIdx = line.indexOf(endTag))) && (endIdx > startIdx)) {
              System.out.println(line.substring(startIdx + startTag.length(), endIdx));
            }
          }
          dictIn.close();
          break;
        }
      }
    }
  }

  private static void testIdx() throws IOException, FileNotFoundException {
    final TarInputStream tarIn = new TarInputStream(new CBZip2InputStream(new FileInputStream(
        "E:\\kkdict\\raw\\dicts\\stardict\\Powerword2005\\stardict-powerword_pwpcehx-2.4.2.tar.bz2")));
    TarEntry entry;
    while (null != (entry = tarIn.getNextEntry())) {
      InputStream dictIn = null;
      if (entry.getName().endsWith(".idx.gz")) {
        System.out.println("解读：'" + entry.getName() + "'（" + entry.getSize() + "）。。。");
        dictIn = new GZIPInputStream(tarIn);
      } else if (entry.getName().endsWith(".idx")) {
        dictIn = tarIn;
      }
      if (dictIn != null) {
        final BufferedInputStream in = new BufferedInputStream(dictIn);
        final ByteBuffer bb = ByteBuffer.allocate(1024);
        int b;
        int skipCount = 0;
        while (-1 != (b = in.read())) {
          if (skipCount == 0) {
            if (b != 0) {
              bb.put((byte) b);
            } else {
              System.out.println(new String(bb.array(), 0, bb.position(), Charset.forName("UTF-8")));
              skipCount++;
              bb.clear();
            }
          } else if (++skipCount > 8) {

            skipCount = 0;
          }
        }
        dictIn.close();
        break;
      }
    }
    tarIn.close();
  }
}
