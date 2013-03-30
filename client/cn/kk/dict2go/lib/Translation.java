package cn.kk.dict2go.lib;

public final class Translation {
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
  public int              srcLng;
  public int              tgtLng;
  public String           srcVal;
  String                  srcValLowerCase;
  public String           tgtVal;

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

}
