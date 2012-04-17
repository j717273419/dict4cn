package cn.kk.kkdict.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;

import org.junit.Before;
import org.junit.Test;

import cn.kk.kkdict.utils.ArrayHelper;
import cn.kk.kkdict.utils.Helper;

public class DictByteBufferRowTest {
    private static ByteBuffer bb1;
    private static ByteBuffer bb2;
    private static ByteBuffer bb3;
    private static ByteBuffer bb4;
    private static ByteBuffer bb5;
    private static ByteBuffer bb6;
    private static DictByteBufferRow row1;
    private static DictByteBufferRow row2;
    private static DictByteBufferRow row3;
    private static DictByteBufferRow row4;
    private static DictByteBufferRow row5;
    private static DictByteBufferRow row6;
    private static String test1 = "ks═اَدَب▫krc═Адабият▫koi═Лыддьӧтан▫kn═ಸಾಹಿತ್ಯ▫kk═Әдебиет▫kab═Tasekla▫kaa═A'debiyat▫ka═ლიტერატურა▫jv═Sastra▫is═Bókmenntir▫io═Literaturo▫ie═Literatura▫ia═Litteratura▫hy═Գրականություն▫ht═Literati▫hr═Književnost▫hif═Literature▫hi═साहित्य▫he═ספרות▫haw═Moʻokalaleo▫hak═Vùn-ho̍k▫gv═Lettyraght▫gu═સાહિત્ય▫gl═Literatura▫gd═Litreachas▫gan═文學▫ga═Litríocht▫fy═Literatuer▫fur═Leterature▫frr═Literatuur▫frp═Litèratura▫fo═Bókmentir▫fiu-vro═Kirändüs▫fa═ادبیات▫ext═Literatura▫eu═Literatura▫et═Kirjandus▫eo═Literaturo▫el═Λογοτεχνία▫dv═އަދަބިއްޔާތު▫diq═Edebiyat▫da═Litteratur▫cy═Llenyddiaeth▫cv═Литература▫csb═Lëteratura▫co═Littiratura▫ceb═Katitikan▫bs═Književnost▫br═Lennegezh▫bo═རྩོམ་རིག་ཡི་གེའི་རིག་པ།▫bn═সাহিত্য▫bg═Литература▫be-x-old═Літаратура▫be═Літаратура▫bat-smg═Literatūra▫bar═Literadua▫ba═Әҙәбиәт▫az═Ədəbiyyat▫ay═Qillqatata▫ast═Lliteratura▫as═সাহিত্য▫ar═أدب▫an═Literatura▫am═ሥነ ጽሑፍ▫af═Letterkunde▫tr═Edebiyat▫id═Sastra▫ko═문학▫hu═Irodalom▫cs═Literatura▫fi═Kirjallisuus▫no═Litteratur▫ca═Literatura▫uk═Література▫vi═Văn chương▫sv═Litteratur▫pt═Literatura▫ja═文学▫ru═Литература▫es═Literatura▫pl═Literatura▫it═Letteratura▫nl═Literatuur▫fr═Littérature▫de═Literatur▫en═Literature▫zh-yue═文學▫zh-min-nan═Bûn-ha̍k▫zea═Literatuur▫za═Vwnzyoz▫yo═Lítíréṣọ̀▫yi═ליטעראטור▫xmf═ლიტერატურა▫wuu═文学▫wo═Njàngat▫war═Panuratan▫wa═Belès letes▫vec═Łiteratura▫ur═ادب▫tt═Ädäbiät▫tl═Panitikan▫th═วรรณกรรม▫tg═Адабиёт▫te═సాహిత్యం▫ta═இலக்கியம்▫sw═Fasihi▫su═Sastra▫stq═Flugge Literatuur▫sr═Књижевност▫sq═Letërsia▫so═Suugaan▫sl═Književnost▫sk═Literatúra▫simple═Literature▫sh═Književnost▫sco═Leiteratur▫scn═Littiratura▫sa═प्राचीन साहित्यम्▫rw═Ubuvanganzo▫rue═Література▫roa-rup═Literatura▫ro═Literatură▫qu═Simi kapchiy▫ps═ادبيات▫pnb═ساہت▫pap═Literatura▫pam═Literatura▫os═Литературæ▫oc═Literatura▫nrm═Littéthatuthe▫nov═Literature▫nn═Litteratur▫new═साहित्य▫ne═साहित्य▫n▫nds═Literatur▫nap═Litteratura▫nah═Tlahcuilōcāyōtl▫my═စာပေ▫mwl═Literatura▫ms═Kesusasteraan▫mr═साहित्य▫mn═Утга зохиол▫ml═സാഹിത്യം▫mk═Литература▫map-bms═Sastra▫lv═Literatūra▫lt═Literatūra▫lo═ວັນນະຄະດີ▫li═Literatuur▫lbe═Адабият▫lb═Literatur▫lad═Literatura▫la═Litterae▫ky═Адабият▫kv═Гижӧмбур▫ku═Wêje▫nl═Literatuur▫zh═文学▫bat_smg═Literatūra▫zh_yue═文學▫sw═Fasihi andishi▫stq═Literatuur (Begriepskläärengssiede)";
    private static String test2 = "sl═Seznam jezikovnih družin in jezikov‹源wiki_sl▫cs═Seznam jazyků a jazykových rodin‹源1‹源2‹源3▫ja═言語のグループの一覧‹源wiki_sl";
    private static String test3 = "   sl═Seznam jezikovnih družin in jezikov‹源1‹源2‹源3  ";
    private static String test4 = "sl═Seznam jezikovnih družin in jezikov";
    private static String test5 = "";
    private static String test6 = "  sl═Seznam jezikovnih družin in jezikov‹源wiki_sl▫cs═Seznam jazyků a jazykových rodin‹源1‹源2‹源3▫ja═言語のグループの一覧‹源wiki_sl▫krc═Адабият";

    @Before
    public void setUp() {
        bb1 = ByteBuffer.wrap(test1.getBytes(Helper.CHARSET_UTF8));
        bb2 = ByteBuffer.wrap(test2.getBytes(Helper.CHARSET_UTF8));
        bb3 = ByteBuffer.wrap(test3.getBytes(Helper.CHARSET_UTF8));
        bb4 = ByteBuffer.wrap(test4.getBytes(Helper.CHARSET_UTF8));
        bb5 = ByteBuffer.wrap(test5.getBytes(Helper.CHARSET_UTF8));
        bb6 = ByteBuffer.wrap(test6.getBytes(Helper.CHARSET_UTF8));
        row1 = DictByteBufferRow.parse(bb1);
        row2 = DictByteBufferRow.parse(bb2);
        row3 = DictByteBufferRow.parse(ArrayHelper.trimP(bb3));
        row4 = DictByteBufferRow.parse(bb4);
        row5 = DictByteBufferRow.parse(bb5);
        row6 = DictByteBufferRow.parse(bb6);
    }

    @Test
    public void testHasAttribute() {
        assertTrue(row3.hasFirstValueAttributes(0));
        assertTrue(row3.hasFirstValueAttribute(0, ByteBuffer.wrap("源2".getBytes(Helper.CHARSET_UTF8))));
        assertTrue(row3.hasFirstValueAttribute(0, ByteBuffer.wrap("源1".getBytes(Helper.CHARSET_UTF8))));
        
        assertFalse(row3.hasFirstValueAttribute(0, ByteBuffer.wrap("源".getBytes(Helper.CHARSET_UTF8))));
        assertFalse(row3.hasFirstValueAttribute(0, ByteBuffer.wrap("‹源2‹源3".getBytes(Helper.CHARSET_UTF8))));
    }

    @Test
    public void testEquals() {
        String t1 = "sl═Seznam jezikovnih družin in jezikov‹源wiki_sl▫cs═Seznam jazyků a jazykových rodin‹源1‹源2‹源3▫ja═言語のグループの一覧‹源wiki_sl";
        String t2 = "sl═Seznam jezikovnih družin in jezikov‹源wiki_sl▫cs═Seznam jazyků a jazykových rodin‹源1‹源2▫ja═言語のグループの一覧‹源wiki_sl";
        String t3 = "  sl═Seznam jezikovnih družin in jezikov‹源wiki_sl▫cs═Seznam jazyků a jazykových rodin‹源1‹源2‹源3▫ja═言語のグループの一覧‹源wiki_sl▫krc═Адабият";
        String t4 = "";
        String t5 = "   sl═Seznam jezikovnih družin in jezikov‹源1‹源2‹源3  ";
        String t6 = "  sl═Seznam jezikovnih družin in jezikov‹源1‹源2‹源3";
        String t7 = "sl═Seznam jezikovnih družin in jezikov‹源wiki_sl▫ja═言語のグループの一覧‹源wiki_sl▫cs═Seznam jazyků a jazykových rodin‹源1‹源2‹源3";
        ByteBuffer b1 = ByteBuffer.wrap(t1.getBytes(Helper.CHARSET_UTF8));
        ByteBuffer b2 = ByteBuffer.wrap(t2.getBytes(Helper.CHARSET_UTF8));
        ByteBuffer b3 = ArrayHelper.trimP(ByteBuffer.wrap(t3.getBytes(Helper.CHARSET_UTF8)));
        ByteBuffer b4 = ByteBuffer.wrap(t4.getBytes(Helper.CHARSET_UTF8));
        ByteBuffer b5 = ArrayHelper.trimP(ByteBuffer.wrap(t5.getBytes(Helper.CHARSET_UTF8)));
        ByteBuffer b6 = ArrayHelper.trimP(ByteBuffer.wrap(t6.getBytes(Helper.CHARSET_UTF8)));
        ByteBuffer b7 = ArrayHelper.trimP(ByteBuffer.wrap(t7.getBytes(Helper.CHARSET_UTF8)));
        DictByteBufferRow r1 = DictByteBufferRow.parse(b1);
        DictByteBufferRow r1c = DictByteBufferRow.parse(b1, true);
        DictByteBufferRow r2 = DictByteBufferRow.parse(b2);
        DictByteBufferRow r2c = DictByteBufferRow.parse(b2, true);
        DictByteBufferRow r3 = DictByteBufferRow.parse(b3);
        DictByteBufferRow r3c = DictByteBufferRow.parse(b3, true);
        DictByteBufferRow r4 = DictByteBufferRow.parse(b4);
        DictByteBufferRow r4c = DictByteBufferRow.parse(b4, true);
        DictByteBufferRow r5 = DictByteBufferRow.parse(b5);
        DictByteBufferRow r5c = DictByteBufferRow.parse(b5, true);
        DictByteBufferRow r6 = DictByteBufferRow.parse(b6);
        DictByteBufferRow r7 = DictByteBufferRow.parse(b7);

        assertFalse(r1.equals(r2));
        assertFalse(r2.equals(r1));
        assertFalse(r1.equals(r4));
        assertFalse(r4.equals(r1));
        assertFalse(r3.equals(r2));
        assertFalse(r2.equals(r3));
        assertFalse(r4.equals(r5));
        assertFalse(r5.equals(r4));
        assertFalse(r1.equals(r3));
        assertFalse(r3.equals(r1));

        assertTrue(r1.equals(r7));
        assertTrue(r7.equals(r1));
        assertTrue(r1.equals(r1));
        assertTrue(r1.equals(r1c));
        assertTrue(r2.equals(r2c));
        assertTrue(r3.equals(r3c));
        assertTrue(r4.equals(r4c));
        assertTrue(r5.equals(r5c));
        assertTrue(r1c.equals(r1));
        assertTrue(r5.equals(r5));
        assertTrue(r6.equals(r5));

    }

    @Test
    public void testGetAttribute() {
        assertEquals("", ArrayHelper.toStringP(row1.getFirstValueAttribute(0, 0)));
        assertEquals("", ArrayHelper.toStringP(row1.getFirstValueAttribute(3, 0)));
        assertEquals("源1", ArrayHelper.toStringP(row3.getFirstValueAttribute(0, 0)));
        assertEquals("源2", ArrayHelper.toStringP(row3.getFirstValueAttribute(0, 1)));
        assertEquals("源3", ArrayHelper.toStringP(row3.getFirstValueAttribute(0, 2)));
        assertEquals("源wiki_sl", ArrayHelper.toStringP(row2.getFirstValueAttribute(2, 0)));
        assertEquals("源1", ArrayHelper.toStringP(row2.getFirstValueAttribute(1, 0)));
        assertEquals("源2", ArrayHelper.toStringP(row2.getFirstValueAttribute(1, 1)));
        assertEquals("源3", ArrayHelper.toStringP(row2.getFirstValueAttribute(1, 2)));
        assertEquals("", ArrayHelper.toStringP(row5.getFirstValueAttribute(3, 0)));
        assertEquals("", ArrayHelper.toStringP(row6.getFirstValueAttribute(3, 0)));
    }

    @Test
    public void testGetAttributes() {
        assertEquals("", ArrayHelper.toStringP(row1.getFirstValueAttributes(0)));
        assertEquals("", ArrayHelper.toStringP(row1.getFirstValueAttributes(3)));
        assertEquals("源1‹源2‹源3", ArrayHelper.toStringP(row3.getFirstValueAttributes(0)));
        assertEquals("源wiki_sl", ArrayHelper.toStringP(row2.getFirstValueAttributes(2)));
        assertEquals("", ArrayHelper.toStringP(row5.getFirstValueAttributes(3)));
    }

    @Test
    public void testGetAttributesSize() {
        assertEquals(0, row1.getFirstValueAttributesSize(0));
        assertEquals(0, row1.getFirstValueAttributesSize(3));
        assertEquals(3, row3.getFirstValueAttributesSize(0));
        assertEquals(3, row2.getFirstValueAttributesSize(1));
        assertEquals(1, row2.getFirstValueAttributesSize(2));
        assertEquals(0, row5.getFirstValueAttributesSize(3));
        assertEquals(0, row6.getFirstValueAttributesSize(3));
    }

    @Test
    public void testGetDefinitionWithAttributes() {
        assertEquals("ks═اَدَب", ArrayHelper.toStringP(row1.getDefinitionWithAttributes(0)));
        assertEquals("kn═ಸಾಹಿತ್ಯ", ArrayHelper.toStringP(row1.getDefinitionWithAttributes(3)));
        assertEquals("sl═Seznam jezikovnih družin in jezikov‹源1‹源2‹源3",
                ArrayHelper.toStringP(row3.getDefinitionWithAttributes(0)));
        assertEquals("ja═言語のグループの一覧‹源wiki_sl", ArrayHelper.toStringP(row2.getDefinitionWithAttributes(2)));
        assertEquals("", ArrayHelper.toStringP(row5.getDefinitionWithAttributes(3)));
        assertEquals("krc═Адабият", ArrayHelper.toStringP(row6.getDefinitionWithAttributes(3)));
    }

    @Test
    public void testGetLanguage() {
        assertEquals("ks", ArrayHelper.toStringP(row1.getLanguage(0)));
        assertEquals("kn", ArrayHelper.toStringP(row1.getLanguage(3)));
        assertEquals("sl", ArrayHelper.toStringP(row3.getLanguage(0)));
        assertEquals("ja", ArrayHelper.toStringP(row2.getLanguage(2)));
        assertEquals("", ArrayHelper.toStringP(row5.getLanguage(3)));
        assertEquals("krc", ArrayHelper.toStringP(row6.getLanguage(3)));
    }

    @Test
    public void testGetValue() {
        assertEquals("اَدَب", ArrayHelper.toStringP(row1.getFirstValue(0)));
        assertEquals("ಸಾಹಿತ್ಯ", ArrayHelper.toStringP(row1.getFirstValue(3)));
        assertEquals("Seznam jezikovnih družin in jezikov", ArrayHelper.toStringP(row3.getFirstValue(0)));
        assertEquals("言語のグループの一覧", ArrayHelper.toStringP(row2.getFirstValue(2)));
        assertEquals("", ArrayHelper.toStringP(row5.getFirstValue(3)));
        assertEquals("Адабият", ArrayHelper.toStringP(row6.getFirstValue(3)));
    }

    @Test
    public void testGetValueWithAttributes() {
        assertEquals("اَدَب", ArrayHelper.toStringP(row1.getFirstValueWithAttributes(0)));
        assertEquals("ಸಾಹಿತ್ಯ", ArrayHelper.toStringP(row1.getFirstValueWithAttributes(3)));
        assertEquals("Seznam jezikovnih družin in jezikov‹源1‹源2‹源3",
                ArrayHelper.toStringP(row3.getFirstValueWithAttributes(0)));
        assertEquals("言語のグループの一覧‹源wiki_sl", ArrayHelper.toStringP(row2.getFirstValueWithAttributes(2)));
        assertEquals("", ArrayHelper.toStringP(row5.getFirstValueWithAttributes(3)));
        assertEquals("Адабият", ArrayHelper.toStringP(row6.getFirstValueWithAttributes(3)));
    }

    @Test
    public void testIsLinkedBy() {
        String t1 = "sl═Seznam jezikovnih družin in jezikov‹源wiki_sl▫cs═Seznam jazyků a jazykových rodin‹源1‹源2‹源3▫ja═言語ープの一覧‹源wiki_sl";
        String t2 = "sl═Seznam in jezikov‹源wiki_sl▫cs═Seznam jazyků a jazykových rodin‹源1‹源2▫ja═言語のグループ‹源wiki_sl";
        String t3 = "  sl═Seznam jezikovnih družin in jezikov‹源wiki_sl▫cs═Seznam rodin‹源1‹源2‹源3▫ja═ープの一覧‹源wiki_sl▫krc═Адабият";
        String t4 = "";
        String t5 = "   sl═Seznam jezikovnih družin in jezikov‹源1‹源2‹源3  ";
        String t6 = "  sl═Seznam jezikovnih družin in jezikov‹源1‹源2‹源3";
        ByteBuffer b1 = ByteBuffer.wrap(t1.getBytes(Helper.CHARSET_UTF8));
        ByteBuffer b2 = ByteBuffer.wrap(t2.getBytes(Helper.CHARSET_UTF8));
        ByteBuffer b3 = ArrayHelper.trimP(ByteBuffer.wrap(t3.getBytes(Helper.CHARSET_UTF8)));
        ByteBuffer b4 = ByteBuffer.wrap(t4.getBytes(Helper.CHARSET_UTF8));
        ByteBuffer b5 = ArrayHelper.trimP(ByteBuffer.wrap(t5.getBytes(Helper.CHARSET_UTF8)));
        ByteBuffer b6 = ArrayHelper.trimP(ByteBuffer.wrap(t6.getBytes(Helper.CHARSET_UTF8)));
        DictByteBufferRow r1 = DictByteBufferRow.parse(b1);
        DictByteBufferRow r1c = DictByteBufferRow.parse(b1, true);
        DictByteBufferRow r2 = DictByteBufferRow.parse(b2);
        DictByteBufferRow r2c = DictByteBufferRow.parse(b2, true);
        DictByteBufferRow r3 = DictByteBufferRow.parse(b3);
        DictByteBufferRow r3c = DictByteBufferRow.parse(b3, true);
        DictByteBufferRow r4 = DictByteBufferRow.parse(b4);
        DictByteBufferRow r4c = DictByteBufferRow.parse(b4, true);
        DictByteBufferRow r5 = DictByteBufferRow.parse(b5);
        DictByteBufferRow r5c = DictByteBufferRow.parse(b5, true);
        DictByteBufferRow r6 = DictByteBufferRow.parse(b6);

        assertFalse(r2.isLinkedBy(r3));
        assertFalse(r3.isLinkedBy(r2));
        assertFalse(r4.isLinkedBy(r4c));
        assertFalse(r4.isLinkedBy(r1));
        assertFalse(r1.isLinkedBy(r4));
        assertFalse(r5.isLinkedBy(r2));
        assertFalse(r2.isLinkedBy(r5));

        assertTrue(r5.isLinkedBy(r1));
        assertTrue(r1.isLinkedBy(r5));
        assertTrue(r6.isLinkedBy(r1));
        assertTrue(r1.isLinkedBy(r6));
        assertTrue(r1.isLinkedBy(r2));
        assertTrue(r2.isLinkedBy(r1));
        assertTrue(r3.isLinkedBy(r1));
        assertTrue(r1.isLinkedBy(r3));
        assertTrue(r1.isLinkedBy(r1));
        assertTrue(r1.isLinkedBy(r1c));
        assertTrue(r2.isLinkedBy(r2c));
        assertTrue(r3.isLinkedBy(r3c));
        assertTrue(r5.isLinkedBy(r5c));
        assertTrue(r1c.isLinkedBy(r1));
        assertTrue(r5.isLinkedBy(r5));
        assertTrue(r6.isLinkedBy(r5));

    }

    @Test
    public void testParseByteBuffer() {
        assertEquals(163, row1.size());
        assertEquals(3, row2.size());
        assertEquals(1, row3.size());
        assertEquals(1, row4.size());
        assertEquals(0, row5.size());
        assertEquals(4, row6.size());
    }
}
