package cn.kk.kkdict.database;

public class Translation {
  public Translation(long trlId, String srcKeyHex, int srcLng, int tgtLng, String srcVal, String tgtVal, int srcGender, int srcCategory, int srcType,
      int srcUsage) {
    super();
    this.trlId = trlId;
    this.srcKeyHex = srcKeyHex;
    this.srcLng = srcLng;
    this.tgtLng = tgtLng;
    this.srcVal = srcVal;
    this.tgtVal = tgtVal;
    this.srcGender = srcGender;
    this.srcCategory = srcCategory;
    this.srcType = srcType;
    this.srcUsage = srcUsage;
  }

  private final long   trlId;
  private final String srcKeyHex;
  private final int    srcLng;
  private final int    tgtLng;
  private final String srcVal;
  private final String tgtVal;
  private final int    srcGender;
  private final int    srcCategory;
  private final int    srcType;
  private final int    srcUsage;

  @Override
  public String toString() {
    return "Translation [trlId=" + this.trlId + ", srcKeyHex=" + this.srcKeyHex + ", srcLng=" + this.srcLng + ", tgtLng=" + this.tgtLng + ", srcVal="
        + this.srcVal + ", tgtVal=" + this.tgtVal + ", srcGender=" + this.srcGender + ", srcCategory=" + this.srcCategory + ", srcType=" + this.srcType
        + ", srcUsage=" + this.srcUsage + "]";
  }

}
