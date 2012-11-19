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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.kk.kkdict.Configuration;
import cn.kk.kkdict.Configuration.Source;
import cn.kk.kkdict.beans.FormattedTreeMap;
import cn.kk.kkdict.beans.Stat;
import cn.kk.kkdict.utils.Helper;

public class PinyinOccurrenceCounter {
  private static final String OUT_FILE = Configuration.IMPORTER_FOLDER_MERGED_WORDS.getFile(Source.NULL, "output-occurrences.pinyin");
  private static final String IN_DIR   = Configuration.IMPORTER_FOLDER_EXTRACTED_WORDS.getPath(Source.NULL);

  public static void main(final String args[]) throws IOException {
    final File directory = new File(PinyinOccurrenceCounter.IN_DIR);
    if (directory.isDirectory()) {
      System.out.print("搜索词组文件'" + PinyinOccurrenceCounter.IN_DIR + "' ... ");

      final File[] files = directory.listFiles(new FilenameFilter() {
        @Override
        public boolean accept(final File dir, final String name) {
          return name.startsWith("output-words.");
        }
      });

      System.out.println(files.length);

      final Map<String, Integer> statsMap = new FormattedTreeMap<>();
      int total = 0;
      for (final File f : files) {
        System.out.print("正在读取词组文件'" + f + " 。。。");
        final int counter = PinyinOccurrenceCounter.readPinyinFromFile(f, statsMap);
        System.out.println(counter);
        total += counter;
      }

      final List<Stat> list = new ArrayList<>();
      long totalOccurrences = 0L;
      try (final BufferedWriter writer = new BufferedWriter(new FileWriter(PinyinOccurrenceCounter.OUT_FILE), Helper.BUFFER_SIZE);) {

        final Set<String> keys = statsMap.keySet();
        for (final String k : keys) {
          list.add(new Stat(statsMap.get(k), k));
        }
        Collections.sort(list);

        for (final Stat s : list) {
          writer.write(s.key);
          writer.write(Helper.SEP_PARTS);
          writer.write(s.counter.toString());
          writer.write(Helper.SEP_NEWLINE);
          totalOccurrences += s.counter.longValue();
        }

      }
      System.out.println("\n=====================================");
      System.out.println("总共读取词语文件：" + files.length);
      System.out.println("词语数目：" + total);
      System.out.println("拼音总数：" + list.size());
      System.out.println("拼音出现次数：" + totalOccurrences);
      System.out.println("=====================================");
    }
  }

  private static int readPinyinFromFile(final File f, final Map<String, Integer> statsMap) throws IOException {
    int statOk = 0;
    try (final BufferedReader reader = new BufferedReader(new FileReader(f), Helper.BUFFER_SIZE);) {
      String line;
      while ((line = reader.readLine()) != null) {
        final String[] parts = line.split(Helper.SEP_PARTS);
        if (parts.length == 2) {
          final String[] py = parts[1].split(Helper.SEP_PINYIN);
          if (py.length > 0) {
            for (int i = 0; i < py.length; i++) {
              final String pinyin = py[i].trim();
              final Integer count = statsMap.get(pinyin);
              if (count == null) {
                statsMap.put(pinyin, Integer.valueOf(py.length - i));
              } else {
                statsMap.put(pinyin, Integer.valueOf((py.length - i) + count.intValue()));
              }
              statOk++;
            }
          }
        }
      }
    }
    return statOk;
  }
}
