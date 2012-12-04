package cn.kk.kkdict.database;

import java.io.IOException;

import cn.kk.kkdict.utils.ArrayHelper;

public class Translation {
  private static final int MAX_SIDX_SIZE = SuperIndexGenerator.LEN_SRC_KEY;

  public Translation(byte[] srcKey, short srcLng, short tgtLng, String srcVal, String tgtVal) {
    this(-1, srcKey, srcLng, tgtLng, srcVal, tgtVal, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
  }

  public Translation(long trlId, byte[] srcKey, short srcLng, short tgtLng, String srcVal, String tgtVal, byte srcGender, byte srcCategory, byte srcType,
      byte srcUsage) {
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

  public Translation(long trlId, String srcKeyHex, short srcLng, short tgtLng, String srcVal, String tgtVal, byte srcGender, byte srcCategory, byte srcType,
      byte srcUsage) {
    this(trlId, ArrayHelper.fromHex(srcKeyHex), srcLng, tgtLng, srcVal, tgtVal, srcGender, srcCategory, srcType, srcUsage);
  }

  private final long   trlId;
  private final short  srcLng;
  private final short  tgtLng;
  private final String srcVal;
  private final String tgtVal;
  private final byte   srcGender;
  private final byte   srcCategory;
  private final byte   srcType;
  private final byte   srcUsage;

  private final byte[] srcKey;

  public static Translation from(short srcLngId, String srcVal, short tgtLngId, String tgtVal) throws IOException {
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

  public short getSrcLng() {
    return this.srcLng;
  }

  public short getTgtLng() {
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
