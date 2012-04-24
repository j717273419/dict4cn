package cn.kk.kkdict;

import cn.kk.kkdict.extraction.dict.WikiPagesMetaCurrentExtractor;
import cn.kk.kkdict.summarization.WikiDictCategoriesMerger;
import cn.kk.kkdict.summarization.WikiDictRelatedCorrector;
import cn.kk.kkdict.summarization.WikiDictsMerger;
import cn.kk.kkdict.tools.WikipediaDumpPagesMetaCurrentXmlDownloader;
import cn.kk.kkdict.utils.Helper;

public class ProduceDictWiki {
    public static final Object[] DEFAULT_ARGS = new Object[] { null };

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        long timeStarted = System.currentTimeMillis();
        
        runJob(WikipediaDumpPagesMetaCurrentXmlDownloader.class);        
        runJob(WikiPagesMetaCurrentExtractor.class);
        runJob(WikiDictCategoriesMerger.class);
        runJob(WikiDictRelatedCorrector.class);
        runJob(WikiDictsMerger.class);

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
