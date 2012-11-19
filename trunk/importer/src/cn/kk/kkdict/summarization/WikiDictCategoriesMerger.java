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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cn.kk.kkdict.Configuration;
import cn.kk.kkdict.Configuration.Source;
import cn.kk.kkdict.tools.DictFilesMergedSorter;
import cn.kk.kkdict.tools.FilesAppender;
import cn.kk.kkdict.tools.LongestLineFinder;
import cn.kk.kkdict.types.Language;
import cn.kk.kkdict.utils.Helper;

public class WikiDictCategoriesMerger {
  private static final int     PROCESS_LIMIT = 1024 * 1024 * 200;
  public static final String   IN_DIR        = Configuration.IMPORTER_FOLDER_EXTRACTED_DICTS.getPath(Source.DICT_WIKIPEDIA);
  public static final String   OUT_DIR       = Configuration.IMPORTER_FOLDER_MERGED_DICTS.getPath(Source.DICT_WIKIPEDIA);
  public static final String   OUT_FILE      = Configuration.IMPORTER_FOLDER_MERGED_DICTS.getFile(Source.DICT_WIKIPEDIA, "output-dict_categories-merged.wiki");
  private static final boolean DEBUG         = false;

  /**
   * @param args
   * @throws IOException
   * @throws InterruptedException
   */
  public static void main(final String[] args) throws IOException, InterruptedException {
    new File(WikiDictCategoriesMerger.OUT_DIR).mkdirs();
    final File inDirFile = new File(WikiDictCategoriesMerger.IN_DIR);
    if (inDirFile.isDirectory()) {
      System.out.print("合并wiki类别文件'" + WikiDictCategoriesMerger.IN_DIR + "' ... ");

      final File[] files = inDirFile.listFiles(new FilenameFilter() {
        @Override
        public boolean accept(final File dir, final String name) {
          return name.startsWith("output-dict_categories.");
        }
      });
      System.out.println(files.length);

      final String[] filePaths = Helper.getFileNames(files);
      final List<String> fileList = new ArrayList<>(Arrays.asList(filePaths));
      final List<String> workFiles = new ArrayList<>();
      boolean first = true;
      final String outFileTmp = Helper.appendFileName(WikiDictCategoriesMerger.OUT_FILE, "_tmp");
      while (!fileList.isEmpty()) {
        workFiles.clear();
        long size = 0;
        while ((size < WikiDictCategoriesMerger.PROCESS_LIMIT) && !fileList.isEmpty()) {
          final String f = fileList.get(0);
          final long s = new File(f).length();
          if (workFiles.isEmpty() || ((s + size) < WikiDictCategoriesMerger.PROCESS_LIMIT)) {
            if (WikiDictCategoriesMerger.DEBUG) {
              System.out.println("加入文件：'" + f + "'（" + Helper.formatSpace(s) + "）。。。");
            }
            workFiles.add(f);
            size += s;
            fileList.remove(f);
          } else {
            break;
          }
        }
        if (first) {
          first = false;
        } else {
          new File(outFileTmp).delete();
          new File(WikiDictCategoriesMerger.OUT_FILE).renameTo(new File(outFileTmp));
          TimeUnit.SECONDS.sleep(1);
          workFiles.add(outFileTmp);
        }
        final String mergedOutFile = Helper.appendFileName(WikiDictCategoriesMerger.OUT_FILE, "_mrg-tmp");
        final FilesAppender merger = new FilesAppender(mergedOutFile, workFiles.toArray(new String[workFiles.size()]));
        merger.append();

        if (WikiDictCategoriesMerger.DEBUG) {
          System.out.println("排序类别文件：'" + mergedOutFile + "'（" + Helper.formatSpace(new File(mergedOutFile).length()) + "）");
        }
        final DictFilesMergedSorter sorter = new DictFilesMergedSorter(Language.EN, WikiDictCategoriesMerger.OUT_DIR, new File(
            WikiDictCategoriesMerger.OUT_FILE).getName(), true, false, mergedOutFile);
        sorter.sort();
        new File(mergedOutFile).delete();
        new File(outFileTmp).delete();
        TimeUnit.SECONDS.sleep(1);
        if (WikiDictCategoriesMerger.DEBUG) {
          System.out.println("排序分步骤完成.输出文件：'" + WikiDictCategoriesMerger.OUT_FILE + "'（"
              + Helper.formatSpace(new File(WikiDictCategoriesMerger.OUT_FILE).length()) + "）");
          final String longestLine = new LongestLineFinder(WikiDictCategoriesMerger.OUT_FILE).find();
          System.out.println("最长行：" + longestLine + "（" + longestLine.length() + "字符）");
        }
      }
      new File(outFileTmp).delete();
      new File(WikiDictCategoriesMerger.OUT_FILE).renameTo(new File(outFileTmp));
      TimeUnit.SECONDS.sleep(1);
      DictFilesMergedSorter sorter = new DictFilesMergedSorter(Language.DE, WikiDictCategoriesMerger.OUT_DIR,
          new File(WikiDictCategoriesMerger.OUT_FILE).getName(), false, false, outFileTmp);
      sorter.sort();

      new File(outFileTmp).delete();
      new File(WikiDictCategoriesMerger.OUT_FILE).renameTo(new File(outFileTmp));
      TimeUnit.SECONDS.sleep(1);
      sorter = new DictFilesMergedSorter(Language.ZH, WikiDictCategoriesMerger.OUT_DIR, new File(WikiDictCategoriesMerger.OUT_FILE).getName(), false, false,
          outFileTmp);
      sorter.sort();
      new File(outFileTmp).delete();
      System.out.println("排序中文完成.输出文件：'" + WikiDictCategoriesMerger.OUT_FILE + "'（" + Helper.formatSpace(new File(WikiDictCategoriesMerger.OUT_FILE).length())
          + "）");
    }
  }

}
