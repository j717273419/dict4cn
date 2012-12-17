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
package cn.kk.kkdict.summarization;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import cn.kk.kkdict.Configuration;
import cn.kk.kkdict.Configuration.Source;
import cn.kk.kkdict.tools.WordFilesMergedSorter;
import cn.kk.kkdict.utils.Helper;

public class WordsMerger {
  private static final Configuration IN_DIR_PARENT = Configuration.IMPORTER_FOLDER_EXTRACTED_WORDS;

  /**
   * @param args
   * @throws IOException
   */
  public static void main(final String[] args) throws IOException {
    final File parent = new File(WordsMerger.IN_DIR_PARENT.getPath(Source.NULL));
    if (parent.isDirectory()) {
      for (final Source src : Source.WORDS) {
        final File directory = new File(WordsMerger.IN_DIR_PARENT.getPath(src));
        if (directory.isDirectory()) {
          System.out.print("搜索词组文件'" + directory.getAbsolutePath() + "' ... ");
          final File[] files = directory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(final File dir, final String name) {
              return true;
            }
          });
          System.out.println(files.length);
          if (files.length > 0) {
            final String[] filePaths = Helper.getFileNames(files);
            final File f = new File(Helper.appendFileName(filePaths[0], "_merged"));
            final WordFilesMergedSorter sorter = new WordFilesMergedSorter(f.getParent(), f.getName(), false, false, filePaths);
            sorter.sort();

            System.out.println("总共读取词语文件：" + files.length + "，" + "词语数目：" + sorter.getTotalSorted());
          } else {
            System.err.println("没有找到单词文件：" + directory.getAbsolutePath());
          }
        } else {
          System.err.println("文件夹不可读：" + directory.getAbsolutePath());
        }
      }
    }
  }
}
