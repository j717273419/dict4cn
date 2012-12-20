package cn.kk.dict2go;

import cn.kk.kkdict.utils.ArrayHelper;

/**
 * <pre>
 * KEY_BYTES: 16 B
 * START_IDX: 4 B
 * </pre>
 */
public class CacheKey
{

  public static final int KEY_LENGTH = 16;

  public static final int CACHE_SIZE = CacheKey.KEY_LENGTH + 4;

  int offset;

  int keyLength;


  public CacheKey()
  {
    this.clear();
  }


  public void clear()
  {
  }


  public int read(int i)
  {
    this.offset = i * CacheKey.CACHE_SIZE;
    this.keyLength = KEY_LENGTH;
    while (Dictionary.INDEX_CACHE_DATA[this.offset + keyLength - 1] == 0)
    {
      this.keyLength--;
    }
    return keyLength + 4;
  }


  public int readIndexKeyOffset()
  {
    return ClientHelper.readInt(Dictionary.INDEX_CACHE_DATA, this.offset + keyLength);
  }


  @Override
  public String toString()
  {
    return ArrayHelper.toHexString(Dictionary.INDEX_CACHE_DATA, this.offset, keyLength);
  }


  public int write(byte[] realIndexCache, int realOffset)
  {
    System.arraycopy(Dictionary.INDEX_CACHE_DATA, offset, realIndexCache, realOffset, keyLength);
    System.arraycopy(Dictionary.INDEX_CACHE_DATA, offset + KEY_LENGTH, realIndexCache, realOffset + keyLength, 4);
    this.offset = realOffset;
    return keyLength + 4;
  }


  // public int compareTo(CacheKey o)
  // {
  // return ArrayHelper.compareTo(Dictionary.INDEX_CACHE, this.idx * CACHE_SIZE,
  // this.keyLength, Dictionary.INDEX_CACHE,
  // o.idx * CACHE_SIZE, o.keyLength);
  // }

  public final static int binarySearch(final IndexKey inputKey)
  {
    int low = 0;
    int high = Dictionary.INDEX_CACHE.length - 1;

    while (low <= high)
    {
      int mid = (low + high) >>> 1;
      CacheKey midVal = Dictionary.INDEX_CACHE[mid];
      System.out.println("cache: "
          + ArrayHelper.toHexString(Dictionary.INDEX_CACHE_DATA, midVal.offset, midVal.keyLength) + " (cidx: " + mid
          + ", idx: " + midVal.readIndexKeyOffset() + ")");
      System.out.println("index: " + Dictionary.readIndexKey(midVal.readIndexKeyOffset()));
      System.out.println("input: " + ArrayHelper.toHexString(inputKey.key, 0, inputKey.keyLength));
      final int cmp =
          ClientHelper.compareTo(Dictionary.INDEX_CACHE_DATA, midVal.offset, midVal.keyLength, inputKey.key, 0,
              inputKey.keyLength);
      System.out.println((cmp < 0 ? "cache < input" : "cache > input") + " (" + cmp + ")");
      if (cmp < 0)
        low = mid + 1;
      else if (cmp > 0)
        high = mid - 1;
      else
        return mid; // key found
    }
    return -(low + 1); // key not found.
  }

}
