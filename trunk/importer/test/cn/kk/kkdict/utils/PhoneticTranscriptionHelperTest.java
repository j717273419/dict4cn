package cn.kk.kkdict.utils;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import cn.kk.kkdict.types.Language;

public class PhoneticTranscriptionHelperTest {

    @Test
    public void testCheckValidPinyin() {
        assertTrue(PhoneticTranscriptionHelper.checkValidPinyin("e"));
        assertTrue(PhoneticTranscriptionHelper.checkValidPinyin("i"));
        assertTrue(PhoneticTranscriptionHelper.checkValidPinyin("ni"));
        assertTrue(PhoneticTranscriptionHelper.checkValidPinyin("men"));
        assertTrue(PhoneticTranscriptionHelper.checkValidPinyin("sheng"));
        assertTrue(PhoneticTranscriptionHelper.checkValidPinyin("jiong"));
        assertTrue(PhoneticTranscriptionHelper.checkValidPinyin("zhuang"));

        assertFalse(PhoneticTranscriptionHelper.checkValidPinyin("zh"));
        assertFalse(PhoneticTranscriptionHelper.checkValidPinyin("zhzh"));
        assertFalse(PhoneticTranscriptionHelper.checkValidPinyin("z"));
        assertFalse(PhoneticTranscriptionHelper.checkValidPinyin("jp"));
    }

    @Test
    public void testGetShenMuYunMu() {
        String[] test0 = PhoneticTranscriptionHelper.getShenMuYunMu("i");
        assertArrayEquals(new String[] { "", "i" }, test0);
        String[] test1 = PhoneticTranscriptionHelper.getShenMuYunMu("ni");
        assertArrayEquals(new String[] { "n", "i" }, test1);
        String[] test2 = PhoneticTranscriptionHelper.getShenMuYunMu("men");
        assertArrayEquals(new String[] { "m", "en" }, test2);
        String[] test3 = PhoneticTranscriptionHelper.getShenMuYunMu("sheng");
        assertArrayEquals(new String[] { "sh", "eng" }, test3);
        String[] test4 = PhoneticTranscriptionHelper.getShenMuYunMu("jiong");
        assertArrayEquals(new String[] { "j", "iong" }, test4);
        String[] test5 = PhoneticTranscriptionHelper.getShenMuYunMu("zhuang");
        assertArrayEquals(new String[] { "zh", "uang" }, test5);
    }

    @Test
    public void testGetPhoneticTranscriptionDanish() {
        // System.out.println(PhoneticTranscriptionHelper.getPhoneticTranscription(Language.DA, "test"));
        assertEquals("tɛsd", PhoneticTranscriptionHelper.getPhoneticTranscription(Language.DA, "test"));
    }

    @Test
    public void testGetPhoneticTranscriptionGerman() {
        // System.out.println(PhoneticTranscriptionHelper.getPhoneticTranscription(Language.DE, "schlau"));
        assertEquals("ʃlaʊ", PhoneticTranscriptionHelper.getPhoneticTranscription(Language.DE, "schlau"));
    }

    @Test
    public void testGetPhoneticTranscriptionChinese() {
        // System.out.println(PhoneticTranscriptionHelper.getPhoneticTranscription(Language.ZH, "你好"));
        assertEquals("nǐhǎo", PhoneticTranscriptionHelper.getPhoneticTranscription(Language.ZH, "你好"));
    }

    @Test
    public void testGetPinyin() {
        assertEquals("ni'hao", PhoneticTranscriptionHelper.getPinyin("你好"));
    }

}
