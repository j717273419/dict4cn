package cn.kk.dict2go.lib;

/**
 * outIndex.write((byte) trl.getSrcLng()); outIndex.write((byte) trl.getTgtLng()); outIndex.write(srcKey); outIndex.write(ArrayHelper.toBytes(srcValLen));
 * outIndex.write(ArrayHelper.toBytes(this.flowDefOffset));
 * 
 * <pre>
 * SRC_LNG: 1 B
 * TGT_LNG: 1 B
 * KEY_BYTES: 16 B
 * SRC_VAL_LEN: 2 B
 * DATA_OFFSET: 4 B
 * </pre>
 */
public class IndexKey implements Comparable<IndexKey> {
  final byte[]     key        = new byte[CacheKey.KEY_LENGTH];

  static final int KEY_OFFSET = 1 + 1;

  static final int INDEX_SIZE = IndexKey.KEY_OFFSET + 2 + 4 + CacheKey.KEY_LENGTH;

  int              keyLength  = CacheKey.KEY_LENGTH;

  public IndexKey() {
  }

  public void read(byte[] ba, int i) {
    final int startOffset = i * CacheKey.CACHE_SIZE;
    System.arraycopy(ba, startOffset, this.key, 0, CacheKey.KEY_LENGTH);
    this.keyLength = CacheKey.KEY_LENGTH;
    while (this.key[this.keyLength - 1] == 0) {
      this.keyLength--;
    }
  }

  public void setKeyBytes(byte[] keyBytes) {
    System.arraycopy(keyBytes, 0, this.key, 0, keyBytes.length);
    this.keyLength = keyBytes.length;
  }

  @Override
  public String toString() {
    return ClientHelper.toHexString(this.key, 0, this.keyLength, true);
  }

  @Override
  public int compareTo(IndexKey o) {
    return ClientHelper.compareTo(this.key, 0, this.keyLength, o.key, 0, o.keyLength);
  }

}
