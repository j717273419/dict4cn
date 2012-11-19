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
package cn.kk.kkdict;

import cn.kk.kkdict.extraction.word.BaiduBcdExtractor;
import cn.kk.kkdict.extraction.word.QQPinyinQpydExtractor;
import cn.kk.kkdict.extraction.word.SogouScelPinyinExtractor;
import cn.kk.kkdict.summarization.PinyinIndexGenerator;
import cn.kk.kkdict.summarization.PinyinOccurrenceCounter;
import cn.kk.kkdict.summarization.WordsMerger;
import cn.kk.kkdict.utils.Helper;

public class ProduceAll {
  public static final Object[] DEFAULT_ARGS = new Object[] { null };

  /**
   * @param args
   * @throws Exception
   */
  public static void main(final String[] args) throws Exception {
    final long timeStarted = System.currentTimeMillis();

    ProduceAll.runJob(QQPinyinQpydExtractor.class);
    ProduceAll.runJob(SogouScelPinyinExtractor.class);
    ProduceAll.runJob(BaiduBcdExtractor.class);
    ProduceAll.runJob(PinyinOccurrenceCounter.class);
    ProduceAll.runJob(PinyinIndexGenerator.class);
    ProduceAll.runJob(WordsMerger.class);

    // runJob(EdictZhDeExtractor.class);
    // WikiPagesMetaCurrentChineseExtractor.main(DEFAULT_ARGS);
    // WikiPagesMetaCurrentGermanExtractor.main(DEFAULT_ARGS);
    // WikiPagesMetaCurrentEnglishExtractor.main(DEFAULT_ARGS);

    System.out.println("\n\n======================================\nTotal Producing Time: " + Helper.formatDuration(System.currentTimeMillis() - timeStarted));
    System.out.println("======================================\n");
  }

  private static void runJob(final Class<?> mainClass) throws Exception {
    final long started = System.currentTimeMillis();

    System.out.println("Starting " + mainClass.getName() + " ...");
    mainClass.getMethod("main", String[].class).invoke(mainClass, ProduceAll.DEFAULT_ARGS);

    System.out.println(mainClass.getName() + " finished in " + Helper.formatDuration(System.currentTimeMillis() - started) + "\n\n");
  }

}
