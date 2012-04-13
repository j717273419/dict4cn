package cn.kk.kkdict.utils;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.ByteBuffer;

import org.junit.Test;

import cn.kk.kkdict.types.Language;

public class DictHelperTest {

    @Test
    public void testGetWikiLanguage() {
        assertEquals(Language.DE,
                DictHelper.getWikiLanguage("C:\\TEST\\DICT 1\\" + File.separator + "dewiktionary-test_dict.wiki"));
        assertEquals(Language.DE, DictHelper.getWikiLanguage("C:\\TEST\\DICT 1\\test_wiki-dict.wiki_de"));
        assertEquals(Language.DE,
                DictHelper.getWikiLanguage("C:\\TEST\\DICT 1\\" + File.separator + "dewiki-test_dict.wiki"));
    }

    @Test
    public void testMergeDefinitionsAndAttributes() {
        String test1 = "ks═اَدَب▫krc═Адабият▫koi═Лыддьӧтан▫sl═Seznam jezikovnih družin in jezikov‹源2";
        String test2 = "ks═اَدَب‹源1‹源2▫sl═Seznam jezikovnih družin in jezikov‹源1▫cs═Seznam jazyků a jazykových rodin‹源1‹源2‹源3▫ja═言語のグループの一覧‹源1▫krc═Адабият‹源1";

        ByteBuffer bb1 = ArrayHelper.borrowByteBufferLarge();
        ArrayHelper.copy(ByteBuffer.wrap(test1.getBytes(Helper.CHARSET_UTF8)), bb1);
        ByteBuffer bb2 = ByteBuffer.wrap(test2.getBytes(Helper.CHARSET_UTF8));

        ByteBuffer merged = ArrayHelper.borrowByteBufferLarge();
        assertTrue(DictHelper.mergeDefinitionsAndAttributes(bb1, bb2, merged));
        assertEquals(
                "ks═اَدَب‹源1‹源2▫krc═Адабият‹源1▫koi═Лыддьӧтан▫sl═Seznam jezikovnih družin in jezikov‹源2‹源1▫cs═Seznam jazyků a jazykových rodin‹源1‹源2‹源3▫ja═言語のグループの一覧‹源1",
                ArrayHelper.toStringP(merged));
        ArrayHelper.giveBack(merged);

        assertTrue(DictHelper.mergeDefinitionsAndAttributes(bb1, bb2));
        // System.out.println(ArrayHelper.toString(bb1));
        assertEquals(
                "ks═اَدَب‹源1‹源2▫krc═Адабият‹源1▫koi═Лыддьӧтан▫sl═Seznam jezikovnih družin in jezikov‹源2‹源1▫cs═Seznam jazyků a jazykových rodin‹源1‹源2‹源3▫ja═言語のグループの一覧‹源1",
                ArrayHelper.toStringP(bb1));
        ArrayHelper.giveBack(bb1);

    }
}
