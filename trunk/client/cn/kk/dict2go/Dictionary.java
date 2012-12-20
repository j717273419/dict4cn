package cn.kk.dict2go;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.tukaani.xz.SeekableXZInputStream;

import cn.kk.kkdict.utils.ArrayHelper;
import cn.kk.kkdict.utils.CompressHelper;
import cn.kk.kkdict.utils.Helper;

public final class Dictionary
{
  private static final int MAX_INDEXES_CACHED = 200;

  private static final int MAX_DATA_SIZE = 8000 + 2000;

  private static final String DXZ = "D:\\dict2go.dxz";

  public static final int HEADER_OFFSET = 2;

  public static final int MAX_CACHE_SIZE = 1024 * 1024;

  static byte[] INDEX_CACHE_DATA = new byte[(MAX_CACHE_SIZE / CacheKey.CACHE_SIZE) * CacheKey.CACHE_SIZE];

  private static int headerLen;

  private static int totalDefs;

  private static int cachedLen;

  private static int idxLen;

  private static int dataLen;

  private static int cachedOffset;

  private static int idxOffset;

  private static int dataOffset;

  private static SeekableXZInputStream inMeta;

  private static SeekableXZInputStream inIndex;

  private static SeekableXZInputStream inData;

  final static CacheKey[] INDEX_CACHE = new CacheKey[MAX_CACHE_SIZE / CacheKey.CACHE_SIZE];
  static
  {

    for (int i = 0; i < INDEX_CACHE.length; i++)
    {
      INDEX_CACHE[i] = new CacheKey();
    }
  }


  private Dictionary()
  {

  }


  /**
   * <pre>
   * TOTAL_DEFS: 4 B
   * CACHE_LEN:  4 B
   * INDEX_LEN:  4 B
   * DATA_LEN:   4 B (up to 4 GB)
   * LNGS: 
   *       outHeader.write(ArrayHelper.toBytes((int) cachedLen));
   *       outHeader.write(ArrayHelper.toBytes((int) idxLen));
   *       outHeader.write(ArrayHelper.toBytes((int) dataLen));
   *       for (int i = 0; i < DatabaseReader.LANGUAGES_SIZE; i++)
   *       {
   *         outHeader.write(this.defsCount[i]);
   *       }
   * </pre>
   * 
   * @throws IOException
   */
  static void loadMeta() throws IOException
  {
    try (DataInputStream in = new DataInputStream(new FileInputStream(Dictionary.DXZ)))
    {
      headerLen = in.readUnsignedShort();
    }

    inMeta = CompressHelper.getDecompressedInputStream(Dictionary.DXZ, Dictionary.HEADER_OFFSET, headerLen);
    inMeta.read(CACHE, 0, 16);
    totalDefs = ClientHelper.toInt(CACHE, 0);
    cachedLen = ClientHelper.toInt(CACHE, 4);
    idxLen = ClientHelper.toInt(CACHE, 8);
    dataLen = ClientHelper.toInt(CACHE, 12);

    cachedOffset = Dictionary.HEADER_OFFSET + headerLen;
    idxOffset = cachedOffset + cachedLen;
    dataOffset = idxOffset + idxLen;

    inIndex = CompressHelper.getDecompressedInputStream(Dictionary.DXZ, idxOffset, idxLen);
    inData = CompressHelper.getDecompressedInputStream(Dictionary.DXZ, dataOffset, dataLen);
  }


  public static void loadCachedIndexes() throws IOException
  {
    int realIndexCacheSize = 0;
    try (InputStream inCached = CompressHelper.getDecompressedInputStream(Dictionary.DXZ, cachedOffset, cachedLen);)
    {
      final int size = inCached.read(INDEX_CACHE_DATA);
      System.out.println("idx cache size: " + size);
      for (int i = 0; i < MAX_CACHE_SIZE / CacheKey.CACHE_SIZE; i++)
      {
        realIndexCacheSize += Dictionary.INDEX_CACHE[i].read(i);
      }
    }
    final byte[] realIndexCache = new byte[realIndexCacheSize];
    int realOffset = 0;
    for (int i = 0; i < MAX_CACHE_SIZE / CacheKey.CACHE_SIZE; i++)
    {
      realOffset += Dictionary.INDEX_CACHE[i].write(realIndexCache, realOffset);
    }
    INDEX_CACHE_DATA = null;
    INDEX_CACHE_DATA = realIndexCache;
  }


  public static void close()
  {
    Helper.close(inMeta, inIndex, inData);
  }


  public static int findCachedStartIdx(IndexKey inputKey)
  {
    int i = CacheKey.binarySearch(inputKey);
    if (i < 0)
    {
      i = -(i + 1);
    }
    return INDEX_CACHE[i].readIndexKeyOffset();
  }

  // test only
  static final byte[] CACHE = new byte[MAX_DATA_SIZE];

  static final byte[] CACHE_INDEX = new byte[IndexKey.INDEX_SIZE * MAX_INDEXES_CACHED];

  static int cachedIdxStart = -1;


  public static void findIndexesByStartIdx(IndexKey inputKey, int idx)
  {
    try
    {
      int cmp;
      while (idx < totalDefs)
      {
        cmp = compare(inputKey, idx);
        if (cmp == 0)
        {
          Context.searchResult.idxExacts.add(idx);
        } else if (cmp == Integer.MAX_VALUE)
        {
          Context.searchResult.idxContains.add(idx);
        } else if (cmp > 0)
        {
          break;
        }
        idx++;
      }
    } catch (IOException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }


  /**
   * 
   * @param inputKey
   * @param idx
   * @return 0: key bytes are the same, MAX_VALUE: src key contains input key,
   *         <0: before, >0: after
   * @throws IOException
   */
  private static int compare(IndexKey inputKey, int idx) throws IOException
  {
    readIndex(idx);
    System.out.println("cache: "
        + ArrayHelper.toHexString(CACHE_INDEX, getCachedIndexOffset(idx) + IndexKey.KEY_OFFSET, CacheKey.KEY_LENGTH));
    System.out.println("input: " + ArrayHelper.toHexString(inputKey.key, 0, inputKey.keyLength));
    return ClientHelper.compareTo(CACHE_INDEX, getCachedIndexOffset(idx) + IndexKey.KEY_OFFSET, CacheKey.KEY_LENGTH,
        inputKey.key, 0, inputKey.keyLength);
  }


  private static int getCachedIndexOffset(int idx)
  {
    return (idx - cachedIdxStart) * IndexKey.INDEX_SIZE;
  }


  private static final void readIndex(int idx) throws IOException
  {
    if (!isIdxIndexCached(idx))
    {
      inIndex.seek(idx * IndexKey.INDEX_SIZE);
      inIndex.read(CACHE_INDEX);
      cachedIdxStart = idx;
    }
  }


  private static final boolean isIdxIndexCached(int idx)
  {
    return idx >= cachedIdxStart && idx < cachedIdxStart + MAX_INDEXES_CACHED;
  }


  // test only
  static String readIndexKey(int idx)
  {
    try
    {
      inIndex.seek(idx * IndexKey.INDEX_SIZE + IndexKey.KEY_OFFSET);
      inIndex.read(CACHE, 0, CacheKey.KEY_LENGTH);
      return ArrayHelper.toHexString(CACHE, 0, CacheKey.KEY_LENGTH);
    } catch (IOException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return null;
    }

  }

  /**
   * <pre>
   * DATA_OFFSET
   * DATA_LEN
   * SRC_VAL_LEN
   * TGT_VAL_LEN
   * </pre>
   */
  static final int[] DATA_OFFSETS = new int[4];


  // test only
  public static String readDataKey(int idxIndex) throws IOException
  {
    readDataOffsets(idxIndex);

    inData.seek(DATA_OFFSETS[0]);
    inData.read(CACHE, 0, DATA_OFFSETS[1]);
    return "src: " + ArrayHelper.toString(CACHE, 0, DATA_OFFSETS[2]) + " -> tgt: "
        + ArrayHelper.toString(CACHE, DATA_OFFSETS[2], Math.max(0, DATA_OFFSETS[3]));
  }


  private static void readDataOffsets(int idxIndex) throws IOException
  {
    if (idxIndex != 0)
    {
      readIndex(idxIndex - 1);
      DATA_OFFSETS[0] =
          ClientHelper.toInt(CACHE_INDEX, getCachedIndexOffset(idxIndex - 1) + IndexKey.KEY_OFFSET
              + CacheKey.KEY_LENGTH + 2);
    } else
    {
      DATA_OFFSETS[0] = 0;
    }
    readIndex(idxIndex);
    final int dataOffNext =
        ClientHelper.toInt(CACHE_INDEX, getCachedIndexOffset(idxIndex) + IndexKey.KEY_OFFSET + CacheKey.KEY_LENGTH + 2);
    DATA_OFFSETS[2] =
        ClientHelper.toShort(CACHE_INDEX, getCachedIndexOffset(idxIndex) + IndexKey.KEY_OFFSET + CacheKey.KEY_LENGTH);
    DATA_OFFSETS[1] = dataOffNext - DATA_OFFSETS[0];
    DATA_OFFSETS[3] = DATA_OFFSETS[1] - DATA_OFFSETS[2];
  }
}
