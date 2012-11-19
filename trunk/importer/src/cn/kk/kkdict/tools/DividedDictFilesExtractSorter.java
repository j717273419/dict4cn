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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import cn.kk.kkdict.types.Language;
import cn.kk.kkdict.utils.Helper;

/**
 * 
 * 排序多个字典文件中的指定语言。
 * 
 */
public class DividedDictFilesExtractSorter {
  private final Language sortLng;
  private final String   outDir;
  public final String    outFile;
  private final String[] inFiles;
  private final long     MAX_SIZE = 1024 * 1024 * 500;
  private final boolean  DEBUG    = false;
  private final boolean  writeSkippedExtracted;

  public DividedDictFilesExtractSorter(final Language sortLng, final String outDir, final String outFile, final boolean writeSkippedExtracted,
      final String... inFiles) {
    this.sortLng = sortLng;
    this.outDir = outDir;
    this.outFile = outDir + File.separator + outFile;
    this.inFiles = inFiles;
    this.writeSkippedExtracted = writeSkippedExtracted;
  }

  public void sort() throws IOException, InterruptedException {
    final LinkedList<String> files = new LinkedList<>(Arrays.asList(this.inFiles));
    final LinkedList<String> working = new LinkedList<>();
    boolean first = true;
    final File outFileFile = new File(this.outFile);
    outFileFile.delete();
    final String tmpOutFile = Helper.appendFileName(this.outFile, "_ddfes-tmp");
    final File tmpFile = new File(tmpOutFile);
    TimeUnit.SECONDS.sleep(1);
    while (!files.isEmpty()) {
      working.clear();
      long size = 0;
      while ((size < this.MAX_SIZE) && !files.isEmpty()) {
        final String f = files.get(0);
        final long s = new File(f).length();
        if (working.isEmpty() || ((s + size) < this.MAX_SIZE)) {
          if (this.DEBUG) {
            System.out.println("加入文件：'" + f + "'（" + Helper.formatSpace(s) + "）。。。");
          }
          if (s > Helper.SEP_LIST_BYTES.length) {
            working.add(f);
          }
          size += s;
          files.remove(f);
        } else {
          break;
        }
      }

      final DictFilesExtractor extractor = new DictFilesExtractor(this.sortLng, this.outDir, tmpFile.getName(), this.writeSkippedExtracted,
          working.toArray(new String[working.size()]));
      extractor.extract();
      if (tmpFile.length() > Helper.SEP_LIST_BYTES.length) {
        final DictFilesMergedSorter sorter = new DictFilesMergedSorter(this.sortLng, this.outDir, true, false, extractor.outFile);
        sorter.sort();
        final File sorterOutFile = new File(sorter.outFile);
        if (sorterOutFile.length() > Helper.SEP_LIST_BYTES.length) {
          if (first) {
            first = false;
            tmpFile.delete();
            sorterOutFile.renameTo(outFileFile);
          } else {
            tmpFile.delete();
            outFileFile.renameTo(tmpFile);
            TimeUnit.SECONDS.sleep(1);
            final SortedDictFilesMerger merger = new SortedDictFilesMerger(this.sortLng, this.outDir, outFileFile.getName(), tmpOutFile, sorter.outFile);
            merger.merge();
            tmpFile.delete();
            sorterOutFile.delete();
          }
          TimeUnit.SECONDS.sleep(1);
          System.out.println("排序临时文件完成：'" + this.outFile + "'（" + Helper.formatSpace(outFileFile.length()) + "）");
        } else {
          sorterOutFile.delete();
        }
      } else {
        tmpFile.delete();
      }
    }
    System.out.println("排序完成.输出文件：'" + this.outFile + "'（" + Helper.formatSpace(new File(this.outFile).length()) + "）");

  }
}
