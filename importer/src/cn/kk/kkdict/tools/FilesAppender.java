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
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import cn.kk.kkdict.utils.ArrayHelper;
import cn.kk.kkdict.utils.Helper;

public class FilesAppender {
  public final String   outFile;
  public final String[] inFiles;

  public FilesAppender(final String outFile, final String... inFiles) {
    this.outFile = outFile;
    this.inFiles = inFiles;
  }

  public void append() throws IOException {
    final long size = Helper.getFilesSize(this.inFiles);
    System.out.println("合并" + this.inFiles.length + "文件（" + Helper.formatSpace(size) + "）至'" + this.outFile + "'。。。");
    final ByteBuffer bb = ArrayHelper.borrowByteBufferLarge();
    try (final BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(this.outFile), Helper.BUFFER_SIZE);) {
      final byte[] array = bb.array();
      for (final String f : this.inFiles) {
        try (final BufferedInputStream in = new BufferedInputStream(new FileInputStream(f), Helper.BUFFER_SIZE);) {
          int l;
          int lastChar = -1;
          while (-1 != (l = in.read(array))) {
            lastChar = array[l - 1];
            out.write(array, 0, l);
          }
          if (lastChar != Helper.SEP_NEWLINE_CHAR) {
            out.write(Helper.SEP_NEWLINE_CHAR);
          }
        }
      }
    }
    ArrayHelper.giveBack(bb);
    System.out.println("合并成功。合并后文件：'" + this.outFile + "'（" + Helper.formatSpace(new File(this.outFile).length()) + "）");
  }
}
