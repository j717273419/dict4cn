package cn.kk.kkdict.utils;

import static org.junit.Assert.*;
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

    @Test
    public void testToFixed() {
        assertEquals(54321.12345, Helper.toFixed(54321.1234467890, 5), 0d);
        assertEquals(54321.0, Helper.toFixed(54321.1234467890, 0), 0d);
    }

    @Test
    public void testSubstringAfter() {
        assertEquals("sdfs", Helper.substringAfter("asdf>sdfs", ">"));
        assertEquals("sdf>s", Helper.substringAfter("asdf>sdf>s", ">"));
    }

    @Test
    public void testSubstringAfterLast() {
        assertEquals("sdfs", Helper.substringAfterLast("asdf>sdfs", ">"));
        assertEquals("s", Helper.substringAfterLast("asdf>sdf>s", ">"));
    }

    @Test
    public void testFindResource() {
        assertTrue(Helper.findResource("simple2traditional.txt") != null);
        assertTrue(Helper.findResource("config.properties") != null);
    }
}
