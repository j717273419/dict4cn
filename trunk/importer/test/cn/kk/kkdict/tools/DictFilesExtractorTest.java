package cn.kk.kkdict.tools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import cn.kk.kkdict.types.Language;
import cn.kk.kkdict.utils.ArrayHelper;
import cn.kk.kkdict.utils.Helper;

public class DictFilesExtractorTest {
    private static final String TMP_DIR = System.getProperty("java.io.tmpdir");
    private final String testFile1 = TMP_DIR + File.separator + "test1.txt";
    private final String testFile2 = TMP_DIR + File.separator + "test2.txt";
    private final String testFile3 = TMP_DIR + File.separator + "test3.txt";

    @Before
    public void setUp() throws Exception {
        Helper.writeBytes(("de═Bombe‹源wiki_ang▫hi═बम‹源wiki_ang▫ar═قنبلة‹源wiki_ang▫th═ระเบิด‹源wiki_angzh═炸弹‹源wiki_ang\n"
                + "ar═فضاء خارجي‹源wiki_ang▫th═อวกาศ‹源wiki_ang▫zh═外层空间‹源wiki_ang▫yo═Òfurufú\n")
                .getBytes(Helper.CHARSET_UTF8), testFile1);
        Helper.writeBytes(("de═Fenster‹源wiki_ang▫nrm═F'nêt'‹源wiki_ang▫tr═Pencere‹源wiki_ang\n"
                + "zh═沃尔夫斯堡‹源wiki_ang▫kk═Вольфсбург‹源wiki_ang").getBytes(Helper.CHARSET_UTF8), testFile2);
        Helper.writeBytes(
                ("zh═华捷伍德‹源wiki_ang\n"
                        + "hi═नेपियर‹源wiki_ang▫zh═内皮尔 (纽西兰)‹源wiki_ang▫nl═Napier (Nieuw-Zeeland)‹源wiki_ang▫ru═Нейпир‹源wiki_ang▫af═Napier, Nieu-Seeland‹源wiki_ang\n"
                        + "br═Rinkin‹源wiki_ang").getBytes(Helper.CHARSET_UTF8), testFile3);
    }

    @Test
    public void testExtract() {
        DictFilesExtractor extractor = new DictFilesExtractor(Language.ZH, TMP_DIR, "result.txt", true, testFile1,
                testFile2, testFile3);
        try {
            extractor.extract();
            // System.out.println(ArrayHelper.toString(Helper.readBytes(TMP_DIR + File.separator + "result.txt")));
            assertEquals(
                    "ar═فضاء خارجي‹源wiki_ang▫th═อวกาศ‹源wiki_ang▫zh═外层空间‹源wiki_ang▫yo═Òfurufú\n"
                            + "zh═沃尔夫斯堡‹源wiki_ang▫kk═Вольфсбург‹源wiki_ang\n"
                            + "zh═华捷伍德‹源wiki_ang\n"
                            + "hi═नेपियर‹源wiki_ang▫zh═内皮尔 (纽西兰)‹源wiki_ang▫nl═Napier (Nieuw-Zeeland)‹源wiki_ang▫ru═Нейпир‹源wiki_ang▫af═Napier, Nieu-Seeland‹源wiki_ang\n",
                    ArrayHelper.toString(Helper.readBytes(TMP_DIR + File.separator + "result.txt")));

            assertEquals(
                    "de═Bombe‹源wiki_ang▫hi═बम‹源wiki_ang▫ar═قنبلة‹源wiki_ang▫th═ระเบิด‹源wiki_angzh═炸弹‹源wiki_ang\n",
                    ArrayHelper.toString(Helper.readBytes(TMP_DIR + File.separator
                            + Helper.appendFileName(new File(testFile1).getName(), DictFilesExtractor.SUFFIX_SKIPPED))));
            assertEquals(
                    "de═Fenster‹源wiki_ang▫nrm═F'nêt'‹源wiki_ang▫tr═Pencere‹源wiki_ang\n",
                    ArrayHelper.toString(Helper.readBytes(TMP_DIR + File.separator
                            + Helper.appendFileName(new File(testFile2).getName(), DictFilesExtractor.SUFFIX_SKIPPED))));
            assertEquals(
                    "br═Rinkin‹源wiki_ang\n",
                    ArrayHelper.toString(Helper.readBytes(TMP_DIR + File.separator
                            + Helper.appendFileName(new File(testFile3).getName(), DictFilesExtractor.SUFFIX_SKIPPED))));
        } catch (Throwable t) {
            fail(t.toString());
        }
    }

}
