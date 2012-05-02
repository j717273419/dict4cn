package cn.kk.kkdict.tools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import cn.kk.kkdict.types.Language;
import cn.kk.kkdict.utils.ArrayHelper;
import cn.kk.kkdict.utils.Helper;

public class DictFilesSorterTest {
    private static final String TMP_DIR = System.getProperty("java.io.tmpdir");
    private final String testFile1 = TMP_DIR + File.separator + "test1.txt";
    private final String testFile2 = TMP_DIR + File.separator + "test2.txt";
    private final String testFile3 = TMP_DIR + File.separator + "test3.txt";

    @Before
    public void setUp() throws Exception {
        Helper.writeBytes(("de═Bombe‹源wiki_ang▫hi═बम‹源wiki_ang▫ar═قنبلة‹源wiki_ang▫th═ระเบิด‹源wiki_angzh═炸弹‹源wiki_ang\n"
                + "ar═فضاء خارجي‹源wiki_ang▫th═อวกาศ‹源wiki_ang▫zh═外层空间‹源wiki_ang▫yo═Òfurufú\n"
                + "t1═t1‹源a1‹源a2▫th═อวกาศ‹源wiki_ang▫zh═外层空间‹源wiki_ang▫yo═Òfurufú▫t2═t2‹源a1‹源a2‹源a3▫t3═t3\n"
                + "t2═t2‹源a1‹源a2▫t1═t1‹源a2‹源a4‹源a3▫zh═外层空间‹源wiki_ang▫yo═Òfurufú▫t5═t5▫t4═t4\n")
                .getBytes(Helper.CHARSET_UTF8), testFile1);
        Helper.writeBytes(("de═Fenster‹源wiki_ang▫nrm═F'nêt'‹源wiki_ang▫tr═Pencere‹源wiki_ang\n"
                + "zh═沃尔夫斯堡‹源wiki_ang▫kk═Вольфсбург‹源wiki_ang").getBytes(Helper.CHARSET_UTF8), testFile2);
        Helper.writeBytes(
                ("zh═华捷伍德‹源wiki_ang\n"
                        + "hi═नेपियर‹源wiki_ang▫zh═内皮尔 (纽西兰)‹源wiki_ang▫nl═Napier (Nieuw-Zeeland)‹源wiki_ang▫ru═Нейпир‹源wiki_ang▫af═Napier, Nieu-Seeland‹源wiki_ang\n"
                        + "br═Rinkin‹源wiki_ang").getBytes(Helper.CHARSET_UTF8), testFile3);
    }

    @Test
    public void testSortWriteSkipped() {
        DictFilesMergedSorter sorter = new DictFilesMergedSorter(Language.ZH, TMP_DIR, "result.txt", false, true, testFile1,
                testFile2, testFile3);
        sorter.setFilterAttributes(false);

        try {
            sorter.sort();
            // System.out.println(ArrayHelper.toString(Helper.readBytes(TMP_DIR + File.separator + "result.txt")));
            assertEquals(
                    "zh═内皮尔 (纽西兰)‹源wiki_ang▫hi═नेपियर‹源wiki_ang▫nl═Napier (Nieuw-Zeeland)‹源wiki_ang▫ru═Нейпир‹源wiki_ang▫af═Napier, Nieu-Seeland‹源wiki_ang\n"
                            + "zh═华捷伍德‹源wiki_ang\n"
                            + "zh═外层空间‹源wiki_ang▫t2═t2‹源a1‹源a2‹源a3▫t1═t1‹源a2‹源a4‹源a3‹源a1▫yo═Òfurufú▫t5═t5▫t4═t4▫ar═فضاء خارجي‹源wiki_ang▫th═อวกาศ‹源wiki_ang▫t3═t3\n"
                            + "zh═沃尔夫斯堡‹源wiki_ang▫kk═Вольфсбург‹源wiki_ang\n",
                    ArrayHelper.toString(Helper.readBytes(TMP_DIR + File.separator + "result.txt")));

            assertEquals(
                    "br═Rinkin‹源wiki_ang\n"
                            + "de═Bombe‹源wiki_ang▫hi═बम‹源wiki_ang▫ar═قنبلة‹源wiki_ang▫th═ระเบิด‹源wiki_angzh═炸弹‹源wiki_ang\n"
                            + "de═Fenster‹源wiki_ang▫nrm═F'nêt'‹源wiki_ang▫tr═Pencere‹源wiki_ang\n",
                    ArrayHelper.toString(Helper.readBytes(TMP_DIR + File.separator
                            + Helper.appendFileName(new File("result.txt").getName(), DictFilesMergedSorter.SUFFIX_SKIPPED))));
        } catch (Throwable t) {
            t.printStackTrace();
            fail(t.toString());
        }
    }

    @Test
    public void testSortWithoutSkipped() {
        DictFilesMergedSorter sorter = new DictFilesMergedSorter(Language.ZH, TMP_DIR, "result.txt", true, false, testFile1,
                testFile2, testFile3);
        sorter.setFilterAttributes(false);

        try {
            new File(TMP_DIR + File.separator
                    + Helper.appendFileName(new File("result.txt").getName(), DictFilesMergedSorter.SUFFIX_SKIPPED)).delete();

            sorter.sort();
            // System.out.println(ArrayHelper.toString(Helper.readBytes(TMP_DIR + File.separator + "result.txt")));
            assertEquals(
                    "zh═内皮尔 (纽西兰)‹源wiki_ang▫hi═नेपियर‹源wiki_ang▫nl═Napier (Nieuw-Zeeland)‹源wiki_ang▫ru═Нейпир‹源wiki_ang▫af═Napier, Nieu-Seeland‹源wiki_ang\n"
                            + "zh═华捷伍德‹源wiki_ang\n"
                            + "zh═外层空间‹源wiki_ang▫t2═t2‹源a1‹源a2‹源a3▫t1═t1‹源a2‹源a4‹源a3‹源a1▫yo═Òfurufú▫t5═t5▫t4═t4▫ar═فضاء خارجي‹源wiki_ang▫th═อวกาศ‹源wiki_ang▫t3═t3\n"
                            + "zh═沃尔夫斯堡‹源wiki_ang▫kk═Вольфсбург‹源wiki_ang\n",
                    ArrayHelper.toString(Helper.readBytes(TMP_DIR + File.separator + "result.txt")));

            assertFalse(new File(TMP_DIR + File.separator
                    + Helper.appendFileName(new File("result.txt").getName(), DictFilesMergedSorter.SUFFIX_SKIPPED)).isFile());
        } catch (Throwable t) {
            t.printStackTrace();
            fail(t.toString());
        }
    }

    @Test
    public void testSortWithSkipped() {
        DictFilesMergedSorter sorter = new DictFilesMergedSorter(Language.ZH, TMP_DIR, "result.txt", false, false, testFile1,
                testFile2, testFile3);
        sorter.setFilterAttributes(false);

        try {
            new File(TMP_DIR + File.separator
                    + Helper.appendFileName(new File("result.txt").getName(), DictFilesMergedSorter.SUFFIX_SKIPPED)).delete();

            sorter.sort();
            // System.out.println(ArrayHelper.toString(Helper.readBytes(TMP_DIR + File.separator + "result.txt")));
            assertEquals(
                    "zh═内皮尔 (纽西兰)‹源wiki_ang▫hi═नेपियर‹源wiki_ang▫nl═Napier (Nieuw-Zeeland)‹源wiki_ang▫ru═Нейпир‹源wiki_ang▫af═Napier, Nieu-Seeland‹源wiki_ang\n"
                            + "zh═华捷伍德‹源wiki_ang\n"
                            + "zh═外层空间‹源wiki_ang▫t2═t2‹源a1‹源a2‹源a3▫t1═t1‹源a2‹源a4‹源a3‹源a1▫yo═Òfurufú▫t5═t5▫t4═t4▫ar═فضاء خارجي‹源wiki_ang▫th═อวกาศ‹源wiki_ang▫t3═t3\n"
                            + "zh═沃尔夫斯堡‹源wiki_ang▫kk═Вольфсбург‹源wiki_ang\n"
                            + "br═Rinkin‹源wiki_ang\n"
                            + "de═Bombe‹源wiki_ang▫hi═बम‹源wiki_ang▫ar═قنبلة‹源wiki_ang▫th═ระเบิด‹源wiki_angzh═炸弹‹源wiki_ang\n"
                            + "de═Fenster‹源wiki_ang▫nrm═F'nêt'‹源wiki_ang▫tr═Pencere‹源wiki_ang\n",
                    ArrayHelper.toString(Helper.readBytes(TMP_DIR + File.separator + "result.txt")));

            assertFalse(new File(TMP_DIR + File.separator
                    + Helper.appendFileName(new File("result.txt").getName(), DictFilesMergedSorter.SUFFIX_SKIPPED)).isFile());
        } catch (Throwable t) {
            t.printStackTrace();
            fail(t.toString());
        }
    }

    @Test
    public void testFilterAttributes() {
        DictFilesMergedSorter sorter = new DictFilesMergedSorter(Language.ZH, TMP_DIR, "result.txt", true, false, testFile1,
                testFile2, testFile3);
        sorter.setFilterAttributes(true);

        try {
            new File(TMP_DIR + File.separator
                    + Helper.appendFileName(new File("result.txt").getName(), DictFilesMergedSorter.SUFFIX_SKIPPED)).delete();

            sorter.sort();
            // System.out.println(ArrayHelper.toString(Helper.readBytes(TMP_DIR + File.separator + "result.txt")));
            assertEquals("zh═内皮尔 (纽西兰)▫hi═नेपियर▫nl═Napier (Nieuw-Zeeland)▫ru═Нейпир▫af═Napier, Nieu-Seeland\n"
                    + "zh═华捷伍德\n" + "zh═外层空间▫t2═t2▫t1═t1▫yo═Òfurufú▫t5═t5▫t4═t4▫ar═فضاء خارجي▫th═อวกาศ▫t3═t3\n"
                    + "zh═沃尔夫斯堡▫kk═Вольфсбург\n",
                    ArrayHelper.toString(Helper.readBytes(TMP_DIR + File.separator + "result.txt")));

            assertFalse(new File(TMP_DIR + File.separator
                    + Helper.appendFileName(new File("result.txt").getName(), DictFilesMergedSorter.SUFFIX_SKIPPED)).isFile());
        } catch (Throwable t) {
            t.printStackTrace();
            fail(t.toString());
        }
    }
}
