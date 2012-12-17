package cn.kk.dict2go;

import cn.kk.kkdict.utils.ArrayHelper;

public class CacheKey implements Comparable<CacheKey> {
  public static final int KEY_LENGTH = 16;
  public static final int CACHE_SIZE = CacheKey.KEY_LENGTH + 4;
  private final byte[]    key        = new byte[CacheKey.KEY_LENGTH];
  private int             keyLength  = CacheKey.KEY_LENGTH;
  int                     idx;

  public CacheKey() {
    this.clear();
  }

  public void clear() {
    this.idx = -1;
  }

  public void read(byte[] ba, int i) {
    final int startOffset = i * CacheKey.CACHE_SIZE;
    final int idxOffset = startOffset + CacheKey.KEY_LENGTH;
    System.arraycopy(ba, startOffset, this.key, 0, CacheKey.KEY_LENGTH);
    this.idx = ClientHelper.readInt(ba, idxOffset);
    this.keyLength = CacheKey.KEY_LENGTH;
    while (this.key[this.keyLength - 1] == 0) {
      this.keyLength--;
    }
  }

  public void setKey(byte[] keyBytes) {
    System.arraycopy(keyBytes, 0, this.key, 0, keyBytes.length);
    this.keyLength = keyBytes.length;
  }

  @Override
  public String toString() {
    return ArrayHelper.toHexString(this.key);
  }

  @Override
  public int compareTo(CacheKey o) {
    return ArrayHelper.compareTo(this.key, 0, this.keyLength, o.key, 0, o.keyLength);
  }
}
