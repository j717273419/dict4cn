package cn.kk.kkdict.database;

import java.io.IOException;

import cn.kk.kkdict.utils.ArrayHelper;

public class Translation {
  private static final int MAX_SIDX_SIZE = 50;

  public Translation(byte[] srcKey, int srcLng, int tgtLng, String srcVal, String tgtVal) {
    this(-1, srcKey, srcLng, tgtLng, srcVal, tgtVal, 0, 0, 0, 0);
  }

  public Translation(long trlId, byte[] srcKey, int srcLng, int tgtLng, String srcVal, String tgtVal, int srcGender, int srcCategory, int srcType, int srcUsage) {
    this.trlId = trlId;
    this.srcLng = srcLng;
    this.tgtLng = tgtLng;
    this.srcVal = srcVal;
    this.tgtVal = tgtVal;
    this.srcGender = srcGender;
    this.srcCategory = srcCategory;
    this.srcType = srcType;
    this.srcUsage = srcUsage;
    if (srcKey.length > Translation.MAX_SIDX_SIZE) {
      this.srcKey = new byte[Translation.MAX_SIDX_SIZE];
      System.arraycopy(srcKey, 0, this.srcKey, 0, Translation.MAX_SIDX_SIZE);
    } else {
      this.srcKey = srcKey;
    }
  }

  public Translation(long trlId, String srcKeyHex, int srcLng, int tgtLng, String srcVal, String tgtVal, int srcGender, int srcCategory, int srcType,
      int srcUsage) {
    this(trlId, ArrayHelper.fromHex(srcKeyHex), srcLng, tgtLng, srcVal, tgtVal, srcGender, srcCategory, srcType, srcUsage);
  }

  private final long   trlId;
  private final int    srcLng;
  private final int    tgtLng;
  private final String srcVal;
  private final String tgtVal;
  private final int    srcGender;
  private final int    srcCategory;
  private final int    srcType;
  private final int    srcUsage;

  private final byte[] srcKey;

  public static Translation from(int srcLngId, String srcVal, int tgtLngId, String tgtVal) throws IOException {
    Translation trl = new Translation(SuperIndexGenerator.createPhonetecIdx(srcVal), srcLngId, tgtLngId, srcVal, tgtVal);
    return trl;
  }

  @Override
  public String toString() {
    return "Translation [srcLng=" + this.srcLng + ", tgtLng=" + this.tgtLng + ", srcVal=" + this.srcVal + ", tgtVal=" + this.tgtVal + "]";
  }

  public byte[] getSrcKey() {
    return this.srcKey;
  }

  public long getTrlId() {
    return this.trlId;
  }

  public int getSrcLng() {
    return this.srcLng;
  }

  public int getTgtLng() {
    return this.tgtLng;
  }

  public String getSrcVal() {
    return this.srcVal;
  }

  public String getTgtVal() {
    return this.tgtVal;
  }

  public int getSrcGender() {
    return this.srcGender;
  }

  public int getSrcCategory() {
    return this.srcCategory;
  }

  public int getSrcType() {
    return this.srcType;
  }

  public int getSrcUsage() {
    return this.srcUsage;
  }
}
