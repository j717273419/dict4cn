package cn.kk.dict2go;

public class Translation {
  public static final int MAX_SRCVAL_SIZE = 2000;
  public static final int MAX_TGTVAL_SIZE = 8000;

  int                     idx;
  int                     srcLng;
  int                     tgtLng;
  String                  srcVal;
  String                  tgtVal;

  public void update(final int idxIndex, final int srcLng, final int tgtLng, final String srcVal, final String tgtVal) {
    this.idx = idxIndex;
    this.srcLng = srcLng;
    this.tgtLng = tgtLng;
    this.srcVal = srcVal;
    this.tgtVal = tgtVal;
  }

  @Override
  public String toString() {
    return "trl-" + this.idx + " [" + Language.get(this.srcLng) + "=" + this.srcVal + ", " + Language.get(this.tgtLng) + "=" + this.tgtVal + "]";
  }
}
