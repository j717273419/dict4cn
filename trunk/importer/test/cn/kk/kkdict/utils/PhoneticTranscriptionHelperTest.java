package cn.kk.kkdict.utils;

import org.junit.Assert;
import org.junit.Test;

import cn.kk.kkdict.types.Language;

public class PhoneticTranscriptionHelperTest {

  @Test
  public void testCheckValidPinyin() {
    Assert.assertTrue(PhoneticTranscriptionHelper.checkValidPinyin("e"));
    Assert.assertTrue(PhoneticTranscriptionHelper.checkValidPinyin("i"));
    Assert.assertTrue(PhoneticTranscriptionHelper.checkValidPinyin("ni"));
    Assert.assertTrue(PhoneticTranscriptionHelper.checkValidPinyin("men"));
    Assert.assertTrue(PhoneticTranscriptionHelper.checkValidPinyin("sheng"));
    Assert.assertTrue(PhoneticTranscriptionHelper.checkValidPinyin("jiong"));
    Assert.assertTrue(PhoneticTranscriptionHelper.checkValidPinyin("zhuang"));

    Assert.assertFalse(PhoneticTranscriptionHelper.checkValidPinyin("zh"));
    Assert.assertFalse(PhoneticTranscriptionHelper.checkValidPinyin("zhzh"));
    Assert.assertFalse(PhoneticTranscriptionHelper.checkValidPinyin("z"));
    Assert.assertFalse(PhoneticTranscriptionHelper.checkValidPinyin("jp"));
  }

  @Test
  public void testGetShenMuYunMu() {
    String[] test0 = PhoneticTranscriptionHelper.getShenMuYunMu("i");
    Assert.assertArrayEquals(new String[] { "", "i" }, test0);
    String[] test1 = PhoneticTranscriptionHelper.getShenMuYunMu("ni");
    Assert.assertArrayEquals(new String[] { "n", "i" }, test1);
    String[] test2 = PhoneticTranscriptionHelper.getShenMuYunMu("men");
    Assert.assertArrayEquals(new String[] { "m", "en" }, test2);
    String[] test3 = PhoneticTranscriptionHelper.getShenMuYunMu("sheng");
    Assert.assertArrayEquals(new String[] { "sh", "eng" }, test3);
    String[] test4 = PhoneticTranscriptionHelper.getShenMuYunMu("jiong");
    Assert.assertArrayEquals(new String[] { "j", "iong" }, test4);
    String[] test5 = PhoneticTranscriptionHelper.getShenMuYunMu("zhuang");
    Assert.assertArrayEquals(new String[] { "zh", "uang" }, test5);
  }

  @Test
  public void testGetPhoneticTranscriptionDanish() {
    // System.out.println(PhoneticTranscriptionHelper.getPhoneticTranscription(Language.DA, "test"));
    Assert.assertEquals("tɛsd", PhoneticTranscriptionHelper.getPhoneticTranscription(Language.DA, "test"));
  }

  @Test
  public void testGetPhoneticTranscriptionGerman() {
    // System.out.println(PhoneticTranscriptionHelper.getPhoneticTranscription(Language.DE, "schlau"));
    Assert.assertEquals("ʃlaʊ", PhoneticTranscriptionHelper.getPhoneticTranscription(Language.DE, "schlau"));
  }

  @Test
  public void testGetPhoneticTranscriptionChinese() {
    // System.out.println(PhoneticTranscriptionHelper.getPhoneticTranscription(Language.ZH, "你好"));
    Assert.assertEquals("nǐhǎo", PhoneticTranscriptionHelper.getPhoneticTranscription(Language.ZH, "你好"));
  }

  @Test
  public void testGetPinyin() {
    Assert.assertEquals("ni'hao", PhoneticTranscriptionHelper.getPinyin("你好"));
  }

}
