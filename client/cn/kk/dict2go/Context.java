package cn.kk.dict2go;

import java.util.Arrays;
import java.util.LinkedList;

public final class Context
{

  public static Step step;

  private static final int[] lngsFlags = new int[8];

  public static final int[] lngsSorted = new int[256];

  public static String input;

  public static LinkedList<String> lastInputs;

  public static int selectedIdx;

  public static IndexResult searchResult = new IndexResult();


  public static boolean isUserLanguage(int lngTest)
  {
    if (lngTest == 1)
    {
      // ZH
      return true;
    } else
    {
      return (lngsFlags[lngTest / 32] & (1 << lngTest % 32)) > 0;
    }
  }


  public static void updateUserLanguages()
  {
    Arrays.fill(lngsFlags, 0);
    int lng;
    for (int i = 0; i < lngsSorted.length; i++)
    {
      lng = lngsSorted[i];
      if (lng == 0)
      {
        break;
      } else
      {
        lngsFlags[lng / 32] = lngsFlags[lng / 32] | (1 << lng % 32);
      }
    }
  }


  public static void change(Step step)
  {
    Context.step = step;
  }

}
