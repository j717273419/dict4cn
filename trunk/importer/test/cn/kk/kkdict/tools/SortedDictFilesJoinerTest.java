package cn.kk.kkdict.tools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import cn.kk.kkdict.types.Language;
import cn.kk.kkdict.utils.ArrayHelper;
import cn.kk.kkdict.utils.Helper;

public class SortedDictFilesJoinerTest {
    private static final String TMP_DIR = System.getProperty("java.io.tmpdir");
    private final String testFile1 = TMP_DIR + File.separator + "test1.txt";
    private final String testFile2 = TMP_DIR + File.separator + "test2.txt";
    private final String testFile3 = TMP_DIR + File.separator + "test3.txt";
    private final String testFile1Sorted = TMP_DIR + File.separator + "test1_srt.txt";
    private final String testFile2Sorted = TMP_DIR + File.separator + "test2_srt.txt";
    private final String testFile3Sorted = TMP_DIR + File.separator + "test3_srt.txt";

    @Before
    public void setUp() throws Exception {
        Helper.writeBytes(("de═Bombe‹源wiki_ang▫hi═बम‹源wiki_ang▫ar═قنبلة‹源wiki_ang▫th═ระเบิด‹源wiki_angzh═炸弹‹源wiki_ang\n"
                + "ar═فضاء خارجي‹源wiki_ang▫zh═外层空间‹源wiki_ang▫yo═Òfurufú\n"
                + "zh═z1‹源a1‹源a2▫th═อวกาศ‹源wiki_ang▫t2═t2‹源a1‹源a2‹源a3▫t3═t3\n"
                + "zh═z2‹源a1‹源a2▫t1═t1‹源a2‹源a4‹源a3▫t5═t5▫t4═t4\n").getBytes(Helper.CHARSET_UTF8), testFile1);
        Helper.writeBytes(("de═Fenster‹源wiki_ang▫nrm═F'nêt'‹源wiki_ang▫tr═Pencere‹源wiki_ang\n"
                + "zh═z2‹源wiki_ang▫kk═Вольфсбург‹源wiki_ang").getBytes(Helper.CHARSET_UTF8), testFile2);
        Helper.writeBytes(
                ("zh═华捷伍德‹源wiki_ang\n"
                        + "hi═नेपियर‹源wiki_ang▫zh═内皮尔 (纽西兰)‹源wiki_ang▫nl═Napier (Nieuw-Zeeland)‹源wiki_ang▫ru═Нейпир‹源wiki_ang▫af═Napier, Nieu-Seeland‹源wiki_ang\n"
                        + "br═Rinkin‹源wiki_ang▫th═อวกาศ‹源wiki_ang▫zh═外层空间").getBytes(Helper.CHARSET_UTF8), testFile3);

        new DictFilesMergedSorter(Language.ZH, TMP_DIR, "test1_srt.txt", true, false, testFile1).sort();
        new DictFilesMergedSorter(Language.ZH, TMP_DIR, "test2_srt.txt", true, false, testFile2).sort();
        new DictFilesMergedSorter(Language.ZH, TMP_DIR, "test3_srt.txt", true, false, testFile3).sort();
    }

    @Test
    public void testJoin() {
        SortedDictFilesJoiner joiner = new SortedDictFilesJoiner(Language.ZH, TMP_DIR, "result.txt", testFile1Sorted,
                testFile2Sorted, testFile3Sorted);
        try {
            joiner.join();
            // System.out.println("结果：" + ArrayHelper.toString(Helper.readBytes(TMP_DIR + File.separator +
            // "result.txt")));
            assertEquals(
                    "zh═外层空间‹源wiki_ang▫ar═فضاء خارجي‹源wiki_ang▫yo═Òfurufú▫br═Rinkin‹源wiki_ang▫th═อวกาศ‹源wiki_ang\n"
                            + "zh═z1‹源a1‹源a2▫th═อวกาศ‹源wiki_ang▫t2═t2‹源a1‹源a2‹源a3▫t3═t3\n"
                            + "zh═z2‹源a1‹源a2‹源wiki_ang▫t1═t1‹源a2‹源a4‹源a3▫t5═t5▫t4═t4▫kk═Вольфсбург‹源wiki_ang\n",
                    ArrayHelper.toString(Helper.readBytes(TMP_DIR + File.separator + "result.txt")));
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

}
