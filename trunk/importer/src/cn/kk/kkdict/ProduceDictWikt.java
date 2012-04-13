package cn.kk.kkdict;

import cn.kk.kkdict.extraction.dict.WiktionaryPagesMetaCurrentChineseExtractor;
import cn.kk.kkdict.tools.WiktionaryDumpPagesMetaCurrentXmlDownloader;
import cn.kk.kkdict.utils.Helper;

public class ProduceDictWikt {
    public static final Object[] DEFAULT_ARGS = new Object[] { null };

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        long timeStarted = System.currentTimeMillis();

        runJob(WiktionaryDumpPagesMetaCurrentXmlDownloader.class);
        runJob(WiktionaryPagesMetaCurrentChineseExtractor.class);
        // runJob(WikiDictCategoriesMerger.class);
        // runJob(WikiDictRelatedRepairman.class);
        // runJob(WikiDictsMerger.class);

        // runJob(EdictZhDeExtractor.class);
        // WikiPagesMetaCurrentChineseExtractor.main(DEFAULT_ARGS);
        // WikiPagesMetaCurrentGermanExtractor.main(DEFAULT_ARGS);
        // WikiPagesMetaCurrentEnglishExtractor.main(DEFAULT_ARGS);

        System.out.println("\n\n======================================\n总共用时："
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
