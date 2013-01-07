package cn.kk.dict2go;

import java.util.Arrays;

public final class Translation implements Comparable<Translation> {
  @Override
  public int hashCode() {
    return this.idx;
  }

  @Override
  public boolean equals(Object obj) {
    final Translation other = (Translation) obj;
    return this.idx == other.idx;
  }

  public static final int MAX_SRCVAL_SIZE = 2000;
  public static final int MAX_TGTVAL_SIZE = 8000;
  public static final int MAX_DATA_SIZE   = Translation.MAX_SRCVAL_SIZE + Translation.MAX_TGTVAL_SIZE;

  int                     idx;
  int                     srcLng;
  int                     tgtLng;
  String                  srcVal;
  String                  srcValLowerCase;
  String                  tgtVal;

  public void update(final int idxIndex, final int sLng, final int tLng, final String sVal, final String tVal) {
    this.idx = idxIndex;
    this.srcLng = sLng;
    this.tgtLng = tLng;
    this.srcVal = sVal;
    this.srcValLowerCase = sVal.toLowerCase();
    this.tgtVal = tVal;
  }

  @Override
  public String toString() {
    return "trl-" + this.idx + " [" + Language.get(this.srcLng) + "=" + this.srcVal + ", " + Language.get(this.tgtLng) + "=" + this.tgtVal + "]";
  }

  @Override
  public int compareTo(Translation o) {
    final int[] sortOrder = Context.lngs;
    final String sortVal = Context.input;

    final int lng1 = this.srcLng != Language.ZH ? this.srcLng : this.tgtLng;
    final int lng2 = o.srcLng != Language.ZH ? o.srcLng : o.tgtLng;
    final int idx1 = Arrays.binarySearch(sortOrder, lng1);
    final int idx2 = Arrays.binarySearch(sortOrder, lng2);
    if (idx1 != idx2) {
      final int cmp = idx1 - idx2;
      if (cmp != 0) {
        return cmp;
      }
    }
    final int cmp1 = this.srcValLowerCase.compareTo(sortVal);
    final int cmp2 = o.srcValLowerCase.compareTo(sortVal);
    if ((cmp1 == 0) && (cmp2 != 0)) {
      return -1;
    } else if ((cmp2 == 0) && (cmp1 != 0)) {
      return 1;
    }
    final boolean startsWith1 = this.srcValLowerCase.startsWith(sortVal);
    final boolean startsWith2 = o.srcValLowerCase.startsWith(sortVal);
    if (startsWith1 && !startsWith2) {
      return -1;
    } else if (!startsWith1 && startsWith2) {
      return 1;
    }
    final boolean endsWith1 = this.srcValLowerCase.endsWith(sortVal);
    final boolean endsWith2 = o.srcValLowerCase.endsWith(sortVal);
    if (endsWith1 && !endsWith2) {
      return -1;
    } else if (!endsWith1 && endsWith2) {
      return 1;
    }
    return this.srcValLowerCase.compareTo(o.srcValLowerCase);
  }
}
