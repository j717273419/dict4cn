package cn.kk.kkdict.utils;

import static org.junit.Assert.*;

import org.junit.Test;

public class PinyinHelperTest {

    @Test
    public void testCheckValidPinyin() {
        assertTrue(PinyinHelper.checkValidPinyin("e"));
        assertTrue(PinyinHelper.checkValidPinyin("i"));
        assertTrue(PinyinHelper.checkValidPinyin("ni"));
        assertTrue(PinyinHelper.checkValidPinyin("men"));
        assertTrue(PinyinHelper.checkValidPinyin("sheng"));
        assertTrue(PinyinHelper.checkValidPinyin("jiong"));
        assertTrue(PinyinHelper.checkValidPinyin("zhuang"));

        assertFalse(PinyinHelper.checkValidPinyin("zh"));
        assertFalse(PinyinHelper.checkValidPinyin("zhzh"));
        assertFalse(PinyinHelper.checkValidPinyin("z"));
        assertFalse(PinyinHelper.checkValidPinyin("jp"));
    }

    @Test
    public void testGetShenMuYunMu() {
        String[] test0 = PinyinHelper.getShenMuYunMu("i");
        assertArrayEquals(new String[] { "", "i" }, test0);
        String[] test1 = PinyinHelper.getShenMuYunMu("ni");
        assertArrayEquals(new String[] { "n", "i" }, test1);
        String[] test2 = PinyinHelper.getShenMuYunMu("men");
        assertArrayEquals(new String[] { "m", "en" }, test2);
        String[] test3 = PinyinHelper.getShenMuYunMu("sheng");
        assertArrayEquals(new String[] { "sh", "eng" }, test3);
        String[] test4 = PinyinHelper.getShenMuYunMu("jiong");
        assertArrayEquals(new String[] { "j", "iong" }, test4);
        String[] test5 = PinyinHelper.getShenMuYunMu("zhuang");
        assertArrayEquals(new String[] { "zh", "uang" }, test5);
    }

    @Test
    public void testGetGooglePinyin() {
        // fail("Not yet implemented");
    }

    @Test
    public void testGetPinyin() {
        assertEquals("ni'hao", PinyinHelper.getPinyin("你好"));
    }

}
