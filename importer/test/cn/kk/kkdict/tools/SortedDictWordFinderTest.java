package cn.kk.kkdict.tools;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.ByteBuffer;

import org.junit.Before;
import org.junit.Test;

import cn.kk.kkdict.types.Language;
import cn.kk.kkdict.utils.ArrayHelper;
import cn.kk.kkdict.utils.Helper;

public class SortedDictWordFinderTest {
    private static final String TMP_DIR = System.getProperty("java.io.tmpdir");
    private final String testFile1 = TMP_DIR + File.separator + "test1.txt";
    private final String testFile1Sorted = TMP_DIR + File.separator + "test1_srt.txt";

    private SortedDictRowFinder finder;

    @Before
    public void setUp() throws Exception {
        Helper.writeBytes(("de═Bombe‹源wiki_ang▫hi═बम‹源wiki_ang▫ar═قنبلة‹源wiki_ang▫th═ระเบิด‹源wiki_angzh═炸弹‹源wiki_ang\n"
                + "ar═فضاء خارجي‹源wiki_ang▫zh═外层空间‹源wiki_ang▫yo═Òfurufú\n"
                + "zh═z1‹源a1‹源a2▫th═อวกาศ‹源wiki_ang▫t2═t2‹源a1‹源a2‹源a3▫t3═t3\n"
                + "zh═z10‹源a1‹源a2▫th═อวกาศ‹源wiki_ang▫t2═t2‹源a1‹源a2‹源a3▫t3═t3\n"
                + "zh═z11‹源a1‹源a2▫th═อวกาศ‹源wiki_ang▫t2═t2‹源a1‹源a2‹源a3▫t3═t3\n"
                + "zh═z12‹源a1‹源a2▫th═อวกาศ‹源wiki_ang▫t2═t2‹源a1‹源a2‹源a3▫t3═t3\n"
                + "zh═z13‹源a1‹源a2▫th═อวกาศ‹源wiki_ang▫t2═t2‹源a1‹源a2‹源a3▫t3═t3\n"
                + "zh═z14‹源a1‹源a2▫th═อวกาศ‹源wiki_ang▫t2═t2‹源a1‹源a2‹源a3▫t3═t3\n"
                + "zh═z15‹源a1‹源a2▫th═อวกาศ‹源wiki_ang▫t2═t2‹源a1‹源a2‹源a3▫t3═t3\n"
                + "zh═z16‹源a1‹源a2▫th═อวกาศ‹源wiki_ang▫t2═t2‹源a1‹源a2‹源a3▫t3═t3\n"
                + "zh═z17‹源a1‹源a2▫th═อวกาศ‹源wiki_ang▫t2═t2‹源a1‹源a2‹源a3▫t3═t3\n"
                + "zh═z18‹源a1‹源a2▫th═อวกาศ‹源wiki_ang▫t2═t2‹源a1‹源a2‹源a3▫t3═t3\n"
                + "zh═z19‹源a1‹源a2▫th═อวกาศ‹源wiki_ang▫t2═t2‹源a1‹源a2‹源a3▫t3═t3\n"
                + "zh═z20‹源a1‹源a2▫t1═t1‹源a2‹源a4‹源a3▫t5═t5▫t4═t4\n").getBytes(Helper.CHARSET_UTF8), testFile1);

        new DictFilesMergedSorter(Language.ZH, TMP_DIR, "test1_srt.txt", true, false, testFile1).sort();
        finder = new SortedDictRowFinder(Language.ZH, testFile1Sorted).prepare();
    }

    @Test
    public void testFindByteBuffer() {
        assertEquals(0, finder.find(ByteBuffer.wrap("外层空间".getBytes(Helper.CHARSET_UTF8))));
        assertEquals(8, finder.find(ByteBuffer.wrap("z16".getBytes(Helper.CHARSET_UTF8))));
        assertEquals(12, finder.find(ByteBuffer.wrap("z20".getBytes(Helper.CHARSET_UTF8))));
        assertEquals(-1, finder.find(ByteBuffer.wrap("z21".getBytes(Helper.CHARSET_UTF8))));
    }

    @Test
    public void testFindRow() {
        assertEquals("zh═外层空间‹源wiki_ang", ArrayHelper.toStringP(finder.findRow(ByteBuffer.wrap("外层空间".getBytes(Helper.CHARSET_UTF8))).getDefinitionWithAttributes(0)));
    }
}
