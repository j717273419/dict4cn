import java.io.IOException;

import cn.kk.kkdict.Helper;
import cn.kk.kkdict.extraction.HanDeDictExtractor;
import cn.kk.kkdict.extraction.PinyinOccurrenceCounter;
import cn.kk.kkdict.extraction.QQPinyinQpydExtractor;
import cn.kk.kkdict.extraction.SogouScelPinyinExtractor;
import cn.kk.kkdict.extraction.WikiPagesMetaCurrentChineseExtractor;
import cn.kk.kkdict.extraction.WikiPagesMetaCurrentEnglishExtractor;
import cn.kk.kkdict.extraction.WikiPagesMetaCurrentGermanExtractor;

public class ProduceAll {
    public static final String[] DEFAULT_ARGS = new String[0];

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        long timeStarted = System.currentTimeMillis();
        HanDeDictExtractor.main(DEFAULT_ARGS);
        
        QQPinyinQpydExtractor.main(DEFAULT_ARGS);
        SogouScelPinyinExtractor.main(DEFAULT_ARGS);
        PinyinOccurrenceCounter.main(DEFAULT_ARGS);
        
        WikiPagesMetaCurrentChineseExtractor.main(DEFAULT_ARGS);
        WikiPagesMetaCurrentGermanExtractor.main(DEFAULT_ARGS);
        WikiPagesMetaCurrentEnglishExtractor.main(DEFAULT_ARGS);
        
        System.out.println("\n\n==============\nTotal Producing Time: "
                + Helper.formatDuration(System.currentTimeMillis() - timeStarted));
        System.out.println("==============\n");
    }

}
