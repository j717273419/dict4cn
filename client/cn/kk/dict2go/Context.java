package cn.kk.dict2go;

import java.util.Arrays;
import java.util.LinkedList;

public final class Context {

  public static Step               step;

  private static final int[]       lngsFlags    = new int[8];

  public static final int[]        lngs         = new int[256];

  // should be in lower case
  public static String             input;

  public static LinkedList<String> lastInputs;

  public static int                selectedIdx;

  public static IndexResult        searchResult = new IndexResult();

  public static boolean isUserLanguage(int lngTest) {
    if (lngTest == Language.ZH) {
      return true;
    } else {
      return (Context.lngsFlags[lngTest / 32] & (1 << (lngTest % 32))) > 0;
    }
  }

  public static void updateUserLanguages() {
    Arrays.fill(Context.lngsFlags, 0);
    int lng;
    for (int i = 0; i < Context.lngs.length; i++) {
      lng = Context.lngs[i];
      if (lng == 0) {
        break;
      } else {
        Context.lngsFlags[lng / 32] = Context.lngsFlags[lng / 32] | (1 << (lng % 32));
      }
    }
  }

  public static void change(Step s) {
    Context.step = s;
  }

}
