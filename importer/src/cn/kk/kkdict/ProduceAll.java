package cn.kk.kkdict;

import cn.kk.kkdict.extraction.BaiduBdictExtractor;
import cn.kk.kkdict.extraction.HanDeDictExtractor;
import cn.kk.kkdict.extraction.QQPinyinQpydExtractor;
import cn.kk.kkdict.extraction.SogouScelPinyinExtractor;
import cn.kk.kkdict.summarization.PinyinIndexGenerator;
import cn.kk.kkdict.summarization.PinyinOccurrenceCounter;
import cn.kk.kkdict.utils.Helper;

public class ProduceAll {
    public static final Object[] DEFAULT_ARGS = new Object[] { null };

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        long timeStarted = System.currentTimeMillis();

        runJob(HanDeDictExtractor.class);
        runJob(QQPinyinQpydExtractor.class);
        runJob(SogouScelPinyinExtractor.class);
        runJob(BaiduBdictExtractor.class);
        runJob(PinyinOccurrenceCounter.class);
        runJob(PinyinIndexGenerator.class);

        //
        // WikiPagesMetaCurrentChineseExtractor.main(DEFAULT_ARGS);
        // WikiPagesMetaCurrentGermanExtractor.main(DEFAULT_ARGS);
        // WikiPagesMetaCurrentEnglishExtractor.main(DEFAULT_ARGS);

        System.out.println("\n\n======================================\nTotal Producing Time: "
                + Helper.formatDuration(System.currentTimeMillis() - timeStarted));
        System.out.println("======================================\n");
    }

    private static void runJob(Class<?> mainClass) throws Exception {
        long started = System.currentTimeMillis();

        System.out.println("Starting " + mainClass.getName() + " ...");
        mainClass.getMethod("main", String[].class).invoke(mainClass, DEFAULT_ARGS);
        
        System.out.println(mainClass.getName() + " finished in "
                + Helper.formatDuration(System.currentTimeMillis() - started) + "\n\n");
    }

}