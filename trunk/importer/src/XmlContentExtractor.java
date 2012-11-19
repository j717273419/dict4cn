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
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import cn.kk.kkdict.utils.Helper;

public class XmlContentExtractor {
  private static final String FILE             = "C:\\Program Files (x86)\\Dehelper\\dic\\combined.bin.raw";
  private static final String FILE_XML_CONTENT = XmlContentExtractor.FILE + ".xtxt";

  public static void main(final String[] args) throws IOException {
    try (final BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(XmlContentExtractor.FILE_XML_CONTENT, false));
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(XmlContentExtractor.FILE));) {
      int ch = -1;
      boolean open = false;
      boolean justOpen = false;
      boolean lastNewLine = false;
      boolean closeTag = false;
      StringBuilder sb = new StringBuilder();

      while ((ch = in.read()) != -1) {
        if (justOpen && (ch == '/')) {
          justOpen = false;
          closeTag = true;
          continue;
        }
        justOpen = false;
        if (open && (ch == '>')) {
          open = false;
          if (closeTag) {
            if (!lastNewLine) {
              out.write(Helper.SEP_NEWLINE_BYTES);
              lastNewLine = true;
            }
          }
          // System.err.println(sb.toString());
        } else if (ch == '<') {
          open = true;
          justOpen = true;
          closeTag = false;
          sb.setLength(0);
        } else if (!open) {
          if (ch != '\n') {
            out.write((char) ch);
            lastNewLine = false;
          } else if (!lastNewLine) {
            out.write((char) ch);
            lastNewLine = true;
          }
        } else {
          sb.append((char) ch);
        }
      }
    }
    System.out.println("完成：" + Helper.formatTime(System.currentTimeMillis()));
  }
}
