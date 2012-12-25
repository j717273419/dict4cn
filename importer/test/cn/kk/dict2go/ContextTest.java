package cn.kk.dict2go;

import static org.junit.Assert.*;

import org.junit.Test;

public class ContextTest
{

  @Test
  public void test()
  {
    assertTrue(Context.isUserLanguage(1));
    assertFalse(Context.isUserLanguage(2));
    Context.lngs[0] = 1;
    Context.lngs[1] = 2;
    Context.lngs[2] = 3;
    Context.updateUserLanguages();
    assertTrue(Context.isUserLanguage(2));
    Context.lngs[1] = 4;
    Context.updateUserLanguages();
    assertFalse(Context.isUserLanguage(2));
  }

}
