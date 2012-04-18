package cn.kk.kkdict.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class HelperTest {

    @Test
    public void testStripCoreText() {
        String test1 = "11(, )2{, }3[, ]4（,）5《,》66［', '］";
        // System.out.println(Helper.stripCoreText(test1));
        assertEquals("11234566", Helper.stripCoreText(test1));
    }

    @Test
    public void testStripHtmlText() {
        String test1 = "<tt>test</tt> <b>wawa</b>";
        assertEquals("test wawa", Helper.stripHtmlText(test1, true));
    }

    @Test
    public void testStripWikiText() {
        System.out
                .println(Helper
                        .stripWikiText("'''China''' [http://test.de] [http://test.de test] [{{IPA|'çi na}}] ([[Oberdeutsche Dialekte|oberdt.]]: [{{IPA|'ki na}}]) ist ein kultureller Raum in [[Ostasien]], der vor über 3500 Jahren entstand und politisch-geographisch von 221 v. Chr. bis 1912 das [[Kaiserreich China]], dann die [[Republik China]] umfasste und seit 1949 die [[Volksrepublik China]] (VR) und die [[Republik China]] (ROC, letztere seitdem nur noch auf der [[Taiwan (Insel)|Insel Taiwan]], vgl. [[Taiwan-Konflikt]]) beinhaltet."));

        System.out
                .println(Helper
                        .stripWikiText("'''Итерзен''' ({{lang-de|Uetersen}}; [ˈyːtɐzən]){{—}} [[Алмантәыла]] а'қалақь. Атерриториа{{—}} {{км²|11.43}}; иаланхо{{—}} {{иаланхо|17865|2006}}."));
    }
}
