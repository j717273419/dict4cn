package cn.kk.dict2go;

public class IndexKey extends CacheKey {
  private boolean valid;
  private int     srcLngId;
  private int     tgtLngId;
  private int     srcValLen;
  private int     dataOffset;

  public IndexKey() {
    this.clear();
  }

  @Override
  public void clear() {
    super.clear();
    this.valid = false;
    this.srcLngId = -1;
    this.tgtLngId = -1;
    this.srcValLen = -1;
    this.dataOffset = -1;
  }
}
