package cn.kk.kkdict.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;

import org.junit.Test;

public class ChineseHelperTest {
    private String testZh1 = "不看Ａ片的女人绝不能娶 一般在人们的印象中，一个爱看A片的女人绝不是个好女人，不是灵魂肮脏，就是心理阴暗，不是性欲强烈，就是好色成瘾，不是变态发狂，就是放荡不羁，反正横竖不是个正经货色。";
    private String testTw1 = "不看Ａ片的女人絕不能娶 一般在人們的印象中，一個愛看A片的女人絕不是個好女人，不是靈魂肮臟，就是心理陰暗，不是性欲強烈，就是好色成癮，不是變態發狂，就是放蕩不羈，反正橫豎不是個正經貨色。";
    private String testZh2 = "国§äöüß";
    private String testTw2 = "國§äöüß";
    private String testZh3 = "Müller，拉丁文名称：Regiomontanus），德国天文学家。 缪勒生于巴伐利亚，年仅13随即成为莱比锡大学学生，三年后转往奥地利维也纳大学就读，为乔治·普尔巴赫的学生，曾经到意大利学习托勒密的天文学。缪勒后来回到德国，在纽伦堡定居下来后，和他的朋友兼赞助人柏那德·瓦尔特（Bernhard Walther）一起进行天文观测。两人一同编印航海历书 …";
    private String testTw3 = "Müller，拉丁文名稱：Regiomontanus），德國天文学家。 缪勒生於巴伐利亞，年僅13隨即成為萊比錫大學學生，三年後轉往奧地利维也纳大学就讀，為乔治·普尔巴赫的学生，曾经到意大利学习托勒密的天文学。缪勒後來回到德國，在纽伦堡定居下来后，和他的朋友兼赞助人柏那德·瓦尔特（Bernhard Walther）一起进行天文观测。两人一同编印航海历书 …";

    private String testTw4 = "'''首都'''，又稱'''國都'''，以[[現代]][[政治]]角度而言，通常指一個[[國家]]的[[中央政府]]所在的首要[[城市]]<ref>{{cite book|title=國語辭典（節本）|author=臺灣商務印書館編審部|publisher=[[臺灣商務印書館]]|origyear=1937|origmonth=3|year=1976|edition =台三版|quote =國都：一國中央政府之所在地|pages =369}}</ref><ref>{{cite web|title=中華人民共和國行政區劃|quote=在歷史上和習慣上，各省級行政區都有簡稱。省級人民政府駐地稱省會（首府），中央人民政府所在地是首都。北京就是中國的首都。|url=http://www.gov.cn/test/2005-06/15/content_18253.htm|date=2005-06-15|accessdate=2009-08-13|publisher=中國政府網}}</ref>，也是[[政治]]活动的中心城市、各类[[国家]]级机关集中駐紮地、国家[[主权]]的象征城市。在大部分国家，首都是國家最大的城市，如[[法國]][[巴黎]]、[[墨西哥]][[墨西哥城]]等；大部份的國家也未在憲法和法律內訂定首都地點<ref>{{cite news|title=近日修改教科書　游揆：中華民國首都在台北市|date=2002/03/29|url=http://www.nownews.com/2002/03/29/328-1282773.htm|publisher=Ettoday|accessdate=2010-12-15|author=林獻堂|quote=陳定南沒有明確回答，只說世界大部分國家都沒有在憲法、法律訂定首都地點，中央機關在那裡，首都就在那}}</ref>，而在部分国家，政治中心与经济中心分离，例如美国首都[[華盛頓哥倫比亞特區]]、土耳其首都[[安卡拉]]等。有时一个国家有多个首都，如'''行政首都'''、'''司法首都'''，分别是该国的行政与司法中心。";
    private String testZh4 = "'''首都'''，又称'''国都'''，以[[现代]][[政治]]角度而言，通常指一个[[国家]]的[[中央政府]]所在的首要[[城市]]<ref>{{cite book|title=国语辞典（节本）|author=臺湾商务印书馆编审部|publisher=[[臺湾商务印书馆]]|origyear=1937|origmonth=3|year=1976|edition =台三版|quote =国都：一国中央政府之所在地|pages =369}}</ref><ref>{{cite web|title=中华人民共和国行政区划|quote=在歷史上和习惯上，各省级行政区都有简称。省级人民政府驻地称省会（首府），中央人民政府所在地是首都。北京就是中国的首都。|url=http://www.gov.cn/test/2005-06/15/content_18253.htm|date=2005-06-15|accessdate=2009-08-13|publisher=中国政府网}}</ref>，也是[[政治]]活动的中心城市、各类[[国家]]级机关集中驻扎地、国家[[主权]]的象征城市。在大部分国家，首都是国家最大的城市，如[[法国]][[巴黎]]、[[墨西哥]][[墨西哥城]]等；大部份的国家也未在宪法和法律内订定首都地点<ref>{{cite news|title=近日修改教科书　游揆：中华民国首都在台北市|date=2002/03/29|url=http://www.nownews.com/2002/03/29/328-1282773.htm|publisher=Ettoday|accessdate=2010-12-15|author=林献堂|quote=陈定南没有明确回答，只说世界大部分国家都没有在宪法、法律订定首都地点，中央机关在那里，首都就在那}}</ref>，而在部分国家，政治中心与经济中心分离，例如美国首都[[华盛顿哥伦比亚特区]]、土耳其首都[[安卡拉]]等。有时一个国家有多个首都，如'''行政首都'''、'''司法首都'''，分别是该国的行政与司法中心。";

    @Test
    public void testToSimplifiedChineseByteBufferCorrupted() {
        byte[] test1 = { -26, -99, -79, -26, -106, -81, -26, -117, -119, -27, -92, -85, -28, -70, -70, -17, -68, -120,
                -28, -65, -124, -26, -106, -121, -17, -68, -102, -48, -110, -48, -66, -47, -127, -47, -126, -48, -66,
                -47, -121, -48, -67, -47, -117, -48, -75, 32, -47, -127, -48, -69, -48, -80, -48, -78, -47, -113, -48,
                -67, -48, -17, -68, -119, -27, -114, -97, -27, -79, -84, -26, -106, -68, -26, -106, -81, -26, -117,
                -119, -27, -92, -85, -24, -86, -98, -26, -105, -113, -25, -66, -92, -17, -68, -116, -27, -66, -116,
                -28, -66, -122, -24, -67, -119, -24, -82, -118, -26, -120, -112, -28, -65, -124, -25, -67, -105, -26,
                -106, -81, -28, -70, -70, -29, -128, -127, -25, -125, -113, -27, -123, -117, -24, -104, -83, -28, -70,
                -70, -27, -110, -116, -25, -103, -67, -28, -65, -124, -25, -66, -123, -26, -106, -81, -28, -70, -70,
                -29, -128, -126, -28, -65, -124, -27, -100, -117, -27, -110, -116, -25, -125, -113, -27, -123, -117,
                -24, -104, -83, -27, -100, -117, -27, -123, -89, -25, -102, -124, -28, -70, -70, -27, -100, -117, -25,
                -79, -115, -25, -71, -127, -27, -92, -102, -28, -65, -125, -28, -67, -65, -26, -83, -73, -27, -113,
                -78, -27, -110, -116, -24, -75, -73, -26, -70, -112, -26, -101, -76, -27, -118, -96, -24, -92, -121,
                -23, -101, -100, -29, -128, -126, -98, -25, -102, -124, -23, -90, -106, -23, -125, -67, -27, -97, -125,
                -23, -121, -116, -26, -70, -85, -17, -68, -116, -27, -123, -74, -27, -101, -96, -27, -97, -70, -25,
                -99, -93, -26, -107, -103, -25, -102, -124, -24, -127, -106, -25, -74, -109, -29, -128, -118, -27,
                -119, -75, -28, -72, -106, -25, -76, -128, -29, -128, -117, -28, -72, -128, -25, -81, -121, -28, -72,
                -83, -24, -88, -104, -24, -68, -119, -17, -68, -116, -24, -111, -105, -27, -112, -115, -25, -102, -124,
                -24, -85, -66, -28, -70, -98, -26, -106, -71, -24, -120, -97, -27, -100, -88, -27, -92, -89, -26, -76,
                -86, -26, -80, -76, -27, -66, -116, -17, -68, -116, -26, -100, -128, -27, -66, -116, -27, -127, -100,
                -26, -77, -118, -25, -102, -124, -27, -100, -80, -26, -106, -71, -27, -80, -79, -27, -100, -88, -28,
                -70, -98, -26, -117, -119, -26, -117, -119, -25, -119, -71, -27, -79, -79, -28, -72, -118, -17, -68,
                -116, -27, -101, -96, -26, -83, -92, -28, -71, -97, -28, -67, -65, -27, -66, -105, -28, -70, -98, -26,
                -117, -119, -26, -117, -119, -25, -119, -71, -27, -79, -79, -27, -100, -88, -26, -83, -112, -26, -76,
                -78, -29, -128, -127, -24, -91, -65, -28, -70, -98, -25, -102, -124, -27, -97, -70, -25, -99, -93, -26,
                -107, -103, -28, -72, -106, -25, -107, -116, -23, -127, -96, -24, -65, -111, -23, -90, -77, -27, -112,
                -115, -29, -128, -126, -24, -112, -84, -23, -102, -69, -29, -128, -126, -26, -109, -127, -26, -100,
                -119, -25, -102, -124, -24, -73, -81 };
        ByteBuffer bb = ByteBuffer.wrap(test1);
        System.out.println(ArrayHelper.toString(bb));
        ChineseHelper.toSimplifiedChinese(bb);
        System.out.println(ArrayHelper.toString(bb));
        assertEquals(
                "东斯拉夫人（俄文：Восточные славян）原属于斯拉夫语族群，后来转变成俄罗斯人、乌克兰人和白俄罗斯人。俄国和乌克兰国内的人国籍繁多促使歷史和起源更加复杂。的首都埃里温，其因基督教的圣经《创世纪》一篇中记载，着名的诺亚方舟在大洪水后，最后停泊的地方就在亚拉拉特山上，因此也使得亚拉拉特山在欧洲、西亚的基督教世界远近驰名。万只。拥有的路",
                ArrayHelper.toString(bb));
    }

    @Test
    public void testToTraditionalChineseString() {
        assertEquals(testTw1, ChineseHelper.toTraditionalChinese(testZh1));
        assertEquals(testTw2, ChineseHelper.toTraditionalChinese(testZh2));
    }

    @Test
    public void testToSimplifiedChineseString() {
        assertEquals(testZh1, ChineseHelper.toSimplifiedChinese(testTw1));
        assertEquals(testZh2, ChineseHelper.toSimplifiedChinese(testTw2));
    }

    @Test
    public void testToSimplifiedChineseByteBuffer() {
        ByteBuffer bb = ByteBuffer.wrap(testTw1.getBytes(Helper.CHARSET_UTF8));
        ChineseHelper.toSimplifiedChinese(bb);
        System.out.println(ArrayHelper.toString(bb));
        assertTrue(ArrayHelper.equalsP(ByteBuffer.wrap(testZh1.getBytes(Helper.CHARSET_UTF8)), bb));

        bb = ByteBuffer.wrap(testTw2.getBytes(Helper.CHARSET_UTF8));
        ChineseHelper.toSimplifiedChinese(bb);
        assertTrue(ArrayHelper.equalsP(ByteBuffer.wrap(testZh2.getBytes(Helper.CHARSET_UTF8)), bb));

        bb = ByteBuffer.wrap(testTw3.getBytes(Helper.CHARSET_UTF8));
        ChineseHelper.toSimplifiedChinese(bb);
        System.out.println(ArrayHelper.toString(bb));
        assertEquals(ChineseHelper.toSimplifiedChinese(testTw3), ArrayHelper.toString(bb));
        assertTrue(ArrayHelper.equalsP(ByteBuffer.wrap(testZh3.getBytes(Helper.CHARSET_UTF8)), bb));

        bb = ByteBuffer.wrap(testTw4.getBytes(Helper.CHARSET_UTF8));
        ChineseHelper.toSimplifiedChinese(bb);
        System.out.println(ArrayHelper.toString(bb));
        assertEquals(ChineseHelper.toSimplifiedChinese(testTw4), ArrayHelper.toString(bb));
        assertTrue(ArrayHelper.equalsP(ByteBuffer.wrap(testZh4.getBytes(Helper.CHARSET_UTF8)), bb));
    }

    @Test
    public void testToTraditionalChineseByteBuffer() {
        ByteBuffer bb = ByteBuffer.wrap(testZh1.getBytes(Helper.CHARSET_UTF8));
        ChineseHelper.toTraditionalChinese(bb);
        assertTrue(ArrayHelper.equalsP(ByteBuffer.wrap(testTw1.getBytes(Helper.CHARSET_UTF8)), bb));

        bb = ByteBuffer.wrap(testZh2.getBytes(Helper.CHARSET_UTF8));
        System.out.println(ArrayHelper.toHexString(bb));
        ChineseHelper.toTraditionalChinese(bb);

        assertEquals(testTw2, ArrayHelper.toStringP(bb));

    }

    @Test
    public void testContainsChinese() {
        assertTrue(ChineseHelper.containsChinese(testZh1));
        assertTrue(ChineseHelper.containsChinese(testZh2));
        assertTrue(ChineseHelper.containsChinese(testTw1));
        assertTrue(ChineseHelper.containsChinese(testTw2));

        assertFalse(ChineseHelper.containsChinese("abcde"));
        assertFalse(ChineseHelper.containsChinese("äöü§?éí^_!\"§%&/()=?`*';µ€@'"));
    }

}
