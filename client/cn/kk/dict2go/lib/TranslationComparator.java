package cn.kk.dict2go.lib;

import java.util.Comparator;

public final class TranslationComparator implements Comparator<Translation> {

  private final int[]   lngOrder;
  private final Context context;

  public TranslationComparator(final Context context, final int[] lngs) {
    this.lngOrder = lngs;
    this.context = context;
  }

  @Override
  public int compare(Translation o1, Translation o2) {
    final int lng1 = o1.srcLng != Language.ZH ? o1.srcLng : o1.tgtLng;
    final int lng2 = o2.srcLng != Language.ZH ? o2.srcLng : o2.tgtLng;
    final int idx1 = ClientHelper.indexOf(this.lngOrder, lng1);
    final int idx2 = ClientHelper.indexOf(this.lngOrder, lng2);
    if (idx1 != idx2) {
      if (idx1 == -1) {
        return 1;
      } else if (idx2 == -1) {
        return -1;
      }
      return idx1 - idx2;
    } else if (o1.srcLng != o2.srcLng) {
      if (o1.srcLng == Language.ZH) {
        return 1;
      } else {
        return -1;
      }
    }
    final String inputLower = this.context.inputLower;
    final int cmp1 = o1.srcValLowerCase.compareTo(inputLower);
    final int cmp2 = o2.srcValLowerCase.compareTo(inputLower);
    if ((cmp1 == 0) && (cmp2 != 0)) {
      return -1;
    } else if ((cmp2 == 0) && (cmp1 != 0)) {
      return 1;
    }
    final boolean startsWith1 = o1.srcValLowerCase.startsWith(inputLower);
    final boolean startsWith2 = o2.srcValLowerCase.startsWith(inputLower);
    if (startsWith1 && !startsWith2) {
      return -1;
    } else if (!startsWith1 && startsWith2) {
      return 1;
    }
    final boolean endsWith1 = o1.srcValLowerCase.endsWith(inputLower);
    final boolean endsWith2 = o2.srcValLowerCase.endsWith(inputLower);
    if (endsWith1 && !endsWith2) {
      return -1;
    } else if (!endsWith1 && endsWith2) {
      return 1;
    }
    return o1.srcValLowerCase.compareTo(o2.srcValLowerCase);
  }

}
