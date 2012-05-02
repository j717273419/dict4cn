package cn.kk.kkdict.extraction.dict;

import static org.junit.Assert.assertEquals;

import java.nio.ByteBuffer;

import org.junit.Before;
import org.junit.Test;

import cn.kk.kkdict.utils.ArrayHelper;
import cn.kk.kkdict.utils.ChineseHelper;
import cn.kk.kkdict.utils.Helper;

public class WikiPagesMetaCurrentExtractorTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testStripWikiText() {
        String test1 = "&lt;noinclude&gt;{{cite web |url=http://www.canadianheritage.gc.ca/progs/cpsc-ccsp/sc-cs/df7-eng.cfm |title=Canadian Heritage â€“ You were asking... |publisher=[[åŠ æ‹¿å¤§æ–‡åŒ–é.ºç”¢éƒ¨|Department of Canadian Heritage]] |date=2008-11-17 |accessdate=2011-6-6 |language=en}}'''China''' [{{IPA|'çi na}}] ([[Oberdeutsche Dialekte|oberdt.]]: [{{IPA|'ki na}}]) ist ein kultureller Raum in [[Ostasien]], der vor über 3500 Jahren entstand und politisch-geographisch von 221 v. Chr. bis 1912 das [[Kaiserreich China]], dann die [[Republik China]] umfasste und seit 1949 die [[Volksrepublik China]] (VR) und die [[Republik China]] (ROC, letztere seitdem nur noch auf der [[Taiwan (Insel)|Insel Taiwan]], vgl. [[Taiwan-Konflikt]]) beinhaltet.";
        String test2 = "'''Итерзен''' ({{lang-de|Uetersen}}; [ˈyːtɐzən]){{—}} [[Алмантәыла]] а'қалақь. Атерриториа{{—}} {{км²|11.43}}; иаланхо{{—}} {{иаланхо|17865|2006}}.";
        String test3 = "__NOTOC__{{lang|en|Personal computer games}}, {{lang|en|Computer games}}, &lt;br /&gt;[[Афаил:William-Adolphe Bouguereau (1825-1905) - The Birth of Venus (1879).jpg|thumb|250px|&quot;Венера лиира&quot; Вилиам Адольф Бужеро]]'''Венера''' ([[алаҭын бызшәа|алаҭ.]] ''venus'' &quot;абзиабара&quot;) - абырзен мифологиаҿ аҧшӡареи абзиабареи рынцәаху ҳәа дыҧхьаӡоуп.";
        String test4 = "{{portal|Uetersen}}[http://readtw.ncl.edu.tw/readtw/town_html/10016/1001603/personalprofile/personalprofile10.htm][http://tw.myblog.yahoo.com/jw!I4OGXIeTFBNPTXxb13R2UA--/article?mid=3&amp;prev=7&amp;next=-1][http://www.newtaiwan.com.tw/bulletinview.jsp?period=385&amp;bulletinid=12061]";
        String test5 = "[http://test.de] [http://test.de test] ";
        String test6 = "'''首都'''，又稱'''國都'''，以[[現代]][[政治]]角度而言，通常指一個[[國家]]的[[中央政府]]所在的首要[[城市]]&lt;ref&gt;{{cite book|title=國語辭典（節本）|author=臺灣商務印書館編審部|publisher=[[臺灣商務印書館]]|origyear=1937|origmonth=3|year=1976|edition =台三版|quote =國都：一國中央政府之所在地|pages =369}}&lt;/ref&gt;&lt;ref&gt;{{cite web|title=中華人民共和國行政區劃|quote=在歷史上和習慣上，各省級行政區都有簡稱。省級人民政府駐地稱省會（首府），中央人民政府所在地是首都。北京就是中國的首都。|url=http://www.gov.cn/test/2005-06/15/content_18253.htm|date=2005-06-15|accessdate=2009-08-13|publisher=中國政府網}}&lt;/ref&gt;，也是[[政治]]活动的中心城市、各类[[国家]]级机关集中駐紮地、国家[[主权]]的象征城市。在大部分国家，首都是國家最大的城市，如[[法國]][[巴黎]]、[[墨西哥]][[墨西哥城]]等；大部份的國家也未在憲法和法律內訂定首都地點&lt;ref&gt;{{cite news|title=近日修改教科書　游揆：中華民國首都在台北市|date=2002/03/29|url=http://www.nownews.com/2002/03/29/328-1282773.htm|publisher=Ettoday|accessdate=2010-12-15|author=林獻堂|quote=陳定南沒有明確回答，只說世界大部分國家都沒有在憲法、法律訂定首都地點，中央機關在那裡，首都就在那}}&lt;/ref&gt;，而在部分国家，政治中心与经济中心分离，例如美国首都[[華盛頓哥倫比亞特區]]、土耳其首都[[安卡拉]]等。有时一个国家有多个首都，如'''行政首都'''、'''司法首都'''，分别是该国的行政与司法中心。";

        ByteBuffer bb1 = ByteBuffer.wrap(test1.getBytes(Helper.CHARSET_UTF8));
        ByteBuffer bb2 = ByteBuffer.wrap(test2.getBytes(Helper.CHARSET_UTF8));
        ByteBuffer bb3 = ByteBuffer.wrap(test3.getBytes(Helper.CHARSET_UTF8));
        ByteBuffer bb4 = ByteBuffer.wrap(test4.getBytes(Helper.CHARSET_UTF8));
        ByteBuffer bb5 = ByteBuffer.wrap(test5.getBytes(Helper.CHARSET_UTF8));
        ByteBuffer bb6 = ByteBuffer.wrap(test6.getBytes(Helper.CHARSET_UTF8));
        ByteBuffer tmpBB = ArrayHelper.borrowByteBufferMedium();

        tmpBB.clear();
        WikiPagesMetaCurrentExtractor extractor = new WikiPagesMetaCurrentExtractor();
        extractor.chinese = true;
        extractor.stripWikiLineP(bb6, tmpBB, 500);
        // System.out.println(ArrayHelper.toStringP(tmpBB));
        // System.out.println(ChineseHelper.toSimplifiedChinese(ArrayHelper.toStringP(tmpBB)));
        ChineseHelper.toSimplifiedChinese(tmpBB);
        System.out.println(ArrayHelper.toStringP(tmpBB));
        assertEquals(
                "首都，又称国都，以现代政治角度而言，通常指一个国家的中央政府所在的首要城市，也是政治活动的中心城市、各类国家级机关集中驻扎地、国家主权的象征城市。在大部分国家，首都是国家最大的城市，如法国巴黎、墨西哥墨西哥城等；大部份的国家也未在宪法和法律内订定首都地点，而在部分国家，政治中心与经济中心分离 …",
                ArrayHelper.toStringP(tmpBB));

        tmpBB.clear();
        extractor.stripWikiLineP(bb5, tmpBB, 1000);
        System.out.println(ArrayHelper.toStringP(tmpBB));
        assertEquals("test", ArrayHelper.toStringP(tmpBB));

        tmpBB.clear();
        extractor.stripWikiLineP(bb4, tmpBB, 1000);
        System.out.println(ArrayHelper.toStringP(tmpBB));
        assertEquals("", ArrayHelper.toStringP(tmpBB));

        tmpBB.clear();
        extractor.stripWikiLineP(bb2, tmpBB, 1000);
        System.out.println(ArrayHelper.toStringP(tmpBB));
        assertEquals(
                "Итерзен (Uetersen; ˈyːtɐzən) Алмантәыла ақалақь. Атерриториа 11.43 (км²); иаланхо 17865 (иаланхо, 2006).",
                ArrayHelper.toStringP(tmpBB));

        tmpBB.clear();
        extractor.stripWikiLineP(bb3, tmpBB, 1000);
        System.out.println(ArrayHelper.toStringP(tmpBB));
        assertEquals(
                "Personal computer games, Computer games, Венера (алаҭ. venus абзиабара) - абырзен мифологиаҿ аҧшӡареи абзиабареи рынцәаху ҳәа дыҧхьаӡоуп.",
                ArrayHelper.toStringP(tmpBB));

        tmpBB.clear();
        extractor.stripWikiLineP(bb1, tmpBB, 1000);
        System.out.println(ArrayHelper.toStringP(tmpBB));
        assertEquals(
                "China çi na (oberdt.: ki na) ist ein kultureller Raum in Ostasien, der vor über 3500 Jahren entstand und politisch-geographisch von 221 v. Chr. bis 1912 das Kaiserreich China, dann die Republik China umfasste und seit 1949 die Volksrepublik China (VR) und die Republik China (ROC, letztere seitdem nur noch auf der Insel Taiwan, vgl. Taiwan-Konflikt) beinhaltet.",
                ArrayHelper.toStringP(tmpBB));

        ArrayHelper.giveBack(tmpBB);
    }

}
