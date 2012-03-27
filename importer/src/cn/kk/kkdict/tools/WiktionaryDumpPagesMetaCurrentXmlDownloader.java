package cn.kk.kkdict.tools;

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
    public final static String OUTPUT_DIR = WiktionaryPagesMetaCurrentChineseExtractor.WIKT_PAGES_META_CURRENT_XML;

    public static void main(String[] args) throws InterruptedException {
        long start = System.currentTimeMillis();
        new File(OUTPUT_DIR).mkdirs();
        ExecutorService executor = Executors.newFixedThreadPool(Helper.MAX_CONNECTIONS);

        List<String> wikis = new FormattedArrayList<String>();
        for (TranslationSource source : TranslationSource.values()) {
            // http://dumps.wikimedia.org/mkwiktionary/latest
            String name = source.name();
            String key = source.key;
            if (name.startsWith("WIKT_")) {
                int idx = key.indexOf('_');
                String wiktname = key.substring(idx + 1) + "wiktionary";
                String dumpUrl = "http://dumps.wikimedia.org/" + wiktname + "/latest/" + wiktname
                        + "-latest-pages-meta-current.xml.bz2";
                wikis.add(dumpUrl);
            }
        }
        Collections.sort(wikis);
        final Semaphore lock = new Semaphore(wikis.size());
        final AtomicInteger successCounter = new AtomicInteger();
        final AtomicInteger failureCounter = new AtomicInteger();
        for (String w : wikis) {
            lock.acquire();
            final String url = w;
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (null != Helper.download(url,
                                OUTPUT_DIR + File.separator + url.substring(url.lastIndexOf('/') + 1), false)) {
                            System.out.println("下载'" + url + "'成功。");
                            successCounter.incrementAndGet();
                        }
                    } catch (Throwable e) {
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
