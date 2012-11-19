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
package cn.kk.kkdict.generators;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import cn.kk.kkdict.beans.FormattedArrayList;
import cn.kk.kkdict.extraction.dict.WiktionaryPagesMetaCurrentChineseExtractor;
import cn.kk.kkdict.types.TranslationSource;
import cn.kk.kkdict.utils.Helper;

public class WiktionaryDumpPagesMetaCurrentXmlDownloader {
  public final static String OUTPUT_DIR = WiktionaryPagesMetaCurrentChineseExtractor.IN_DIR;

  public static void main(final String[] args) throws InterruptedException {
    final long start = System.currentTimeMillis();
    new File(WiktionaryDumpPagesMetaCurrentXmlDownloader.OUTPUT_DIR).mkdirs();
    final ExecutorService executor = Executors.newFixedThreadPool(Helper.MAX_CONNECTIONS);

    final List<String> wikis = new FormattedArrayList<>();
    for (final TranslationSource source : TranslationSource.values()) {
      // http://dumps.wikimedia.org/mkwiktionary/latest
      final String name = source.name();
      final String key = source.key;
      if (name.startsWith("WIKT_")) {
        final int idx = key.indexOf('_');
        final String wiktname = key.substring(idx + 1) + "wiktionary";
        final String dumpUrl = "http://dumps.wikimedia.org/" + wiktname + "/latest/" + wiktname + "-latest-pages-meta-current.xml.bz2";
        wikis.add(dumpUrl);
      }
    }
    Collections.sort(wikis);
    final Semaphore lock = new Semaphore(wikis.size());
    final AtomicInteger successCounter = new AtomicInteger();
    final AtomicInteger failureCounter = new AtomicInteger();
    for (final String w : wikis) {
      lock.acquire();
      final String url = w;
      executor.execute(new Runnable() {
        @Override
        public void run() {
          try {
            final String file = WiktionaryDumpPagesMetaCurrentXmlDownloader.OUTPUT_DIR + File.separator + url.substring(url.lastIndexOf('/') + 1);
            if (Helper.isEmptyOrNotExists(file)) {
              if (null != Helper.download(url, file, false)) {
                System.out.println("下载'" + url + "'成功。");
                successCounter.incrementAndGet();
              }
            } else {
              System.out.println("跳过：" + url + "，文件已存在。");
            }
          } catch (final Throwable e) {
            System.err.println("下载'" + url + "'失败：" + e.toString());
            failureCounter.incrementAndGet();
          } finally {
            lock.release();
          }
        }
      });
    }
    executor.shutdown();
    lock.acquire(wikis.size());
    System.out.println("==========================");
    System.out.println("成功下载：" + successCounter.get() + "wiki备份文件");
    System.out.println("下载失败：" + failureCounter.get());
    System.out.println("总共用时：" + ((System.currentTimeMillis() - start) / 1000) + "s");
    System.out.println("==========================");
  }
}
