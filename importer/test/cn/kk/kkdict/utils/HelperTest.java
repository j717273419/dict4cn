package cn.kk.kkdict.utils;

import static org.junit.Assert.fail;

import org.junit.Test;

public class HelperTest {

    @Test
    public void testStripCoreText() {
        fail("Not yet implemented");
    }

    @Test
    public void testStripHtmlText() {
        fail("Not yet implemented");
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
