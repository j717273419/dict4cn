package cn.kk.dict2go;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.tukaani.xz.SeekableXZInputStream;

import cn.kk.kkdict.utils.ArrayHelper;
import cn.kk.kkdict.utils.CompressHelper;
import cn.kk.kkdict.utils.Helper;

public final class Dictionary {
  private static final int             MAX_INDEXES_CACHED               = 200;

  private static final int             MAX_DATA_SIZE                    = 8000 + 2000;

  private static final int             MIN_INDEXES_STARTSWITH_KEYLENGTH = 1;

  private static final int             MIN_INDEXES_CONTAINS_KEYLENGTH   = 1;

  private static final String          DXZ                              = "D:\\dict2go.dxz";

  public static final int              HEADER_OFFSET                    = 2;

  public static final int              MAX_CACHE_SIZE                   = 1024 * 1024;

  static byte[]                        INDEX_CACHE_DATA                 = new byte[(Dictionary.MAX_CACHE_SIZE / CacheKey.CACHE_SIZE) * CacheKey.CACHE_SIZE];

  private static int                   headerLen;

  private static int                   totalDefs;

  private static int                   cachedLen;

  private static int                   idxLen;

  private static int                   dataLen;

  private static int                   cachedOffset;

  private static int                   idxOffset;

  private static int                   dataOffset;

  private static SeekableXZInputStream inMeta;

  private static SeekableXZInputStream inIndex;

  private static SeekableXZInputStream inData;

  final static CacheKey[]              INDEX_CACHE                      = new CacheKey[Dictionary.MAX_CACHE_SIZE / CacheKey.CACHE_SIZE];
  static {

    for (int i = 0; i < Dictionary.INDEX_CACHE.length; i++) {
      Dictionary.INDEX_CACHE[i] = new CacheKey();
    }
  }

  private Dictionary() {

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
  static void loadMeta() throws IOException {
    try (DataInputStream in = new DataInputStream(new FileInputStream(Dictionary.DXZ))) {
      Dictionary.headerLen = in.readUnsignedShort();
    }

    Dictionary.inMeta = CompressHelper.getDecompressedInputStream(Dictionary.DXZ, Dictionary.HEADER_OFFSET, Dictionary.headerLen);
    Dictionary.inMeta.read(Dictionary.CACHE, 0, 16);
    Dictionary.totalDefs = ClientHelper.toInt(Dictionary.CACHE, 0);
    Dictionary.cachedLen = ClientHelper.toInt(Dictionary.CACHE, 4);
    Dictionary.idxLen = ClientHelper.toInt(Dictionary.CACHE, 8);
    Dictionary.dataLen = ClientHelper.toInt(Dictionary.CACHE, 12);

    Dictionary.cachedOffset = Dictionary.HEADER_OFFSET + Dictionary.headerLen;
    Dictionary.idxOffset = Dictionary.cachedOffset + Dictionary.cachedLen;
    Dictionary.dataOffset = Dictionary.idxOffset + Dictionary.idxLen;

    Dictionary.inIndex = CompressHelper.getDecompressedInputStream(Dictionary.DXZ, Dictionary.idxOffset, Dictionary.idxLen);
    Dictionary.inData = CompressHelper.getDecompressedInputStream(Dictionary.DXZ, Dictionary.dataOffset, Dictionary.dataLen);
  }

  public static void loadCachedIndexes() throws IOException {
    int realIndexCacheSize = 0;
    try (InputStream inCached = CompressHelper.getDecompressedInputStream(Dictionary.DXZ, Dictionary.cachedOffset, Dictionary.cachedLen);) {
      final int size = inCached.read(Dictionary.INDEX_CACHE_DATA);
      System.out.println("idx cache size: " + size);
      for (int i = 0; i < (Dictionary.MAX_CACHE_SIZE / CacheKey.CACHE_SIZE); i++) {
        realIndexCacheSize += Dictionary.INDEX_CACHE[i].read(i);
      }
    }
    final byte[] realIndexCache = new byte[realIndexCacheSize];
    int realOffset = 0;
    for (int i = 0; i < (Dictionary.MAX_CACHE_SIZE / CacheKey.CACHE_SIZE); i++) {
      realOffset += Dictionary.INDEX_CACHE[i].write(realIndexCache, realOffset);
    }
    Dictionary.INDEX_CACHE_DATA = null;
    Dictionary.INDEX_CACHE_DATA = realIndexCache;
  }

  public static void close() {
    Helper.close(Dictionary.inMeta, Dictionary.inIndex, Dictionary.inData);
  }

  public static int findCachedStartIdx(IndexKey inputKey) {
    int i = CacheKey.binarySearch(inputKey);
    if (i < 0) {
      i = -(i + 1);
    }
    return Dictionary.INDEX_CACHE[i].readIndexKeyOffset();
  }

  // test only
  static final byte[] CACHE          = new byte[Dictionary.MAX_DATA_SIZE];

  static final byte[] CACHE_INDEX    = new byte[IndexKey.INDEX_SIZE * Dictionary.MAX_INDEXES_CACHED];

  static int          cachedIdxStart = -1;

  public static void findExactAndStartsWithIndexes(IndexKey inputKey, final int idx) {
    try {
      int cmp;
      int i = idx;
      while (i < Dictionary.totalDefs) {
        final int lng = Dictionary.getIndexLanguage(i);
        if (Context.isUserLanguage(lng)) {
          cmp = Dictionary.compare(inputKey, i);
          if (cmp == 0) {
            // exact match
            Context.searchResult.idxResult.add(i);
          } else if (cmp == Integer.MAX_VALUE) {
            // starts with
            if (inputKey.keyLength > Dictionary.MIN_INDEXES_STARTSWITH_KEYLENGTH) {
              Context.searchResult.idxResult.add(i);
            } else {
              break;
            }
          } else if (cmp > 0) {
            break;
          }
        }
        i++;
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  private static int getIndexLanguage(final int idx) throws IOException {
    Dictionary.readIndex(idx);
    int lng = 0xff & Dictionary.CACHE_INDEX[Dictionary.getCachedIndexOffset(idx)];
    if (lng == Language.ZH) {
      lng = 0xff & Dictionary.CACHE_INDEX[Dictionary.getCachedIndexOffset(idx) + 1];
    }
    return lng;
  }

  /**
   * 
   * @param inputKey
   * @param idx
   * @return 0: key bytes are the same, MAX_VALUE: src key starts with input key, <0: before, >0: after
   * @throws IOException
   */
  private static final int compare(IndexKey inputKey, int idx) throws IOException {
    Dictionary.readIndex(idx);
    // System.out.println("cache: "
    // + ArrayHelper.toHexString(Dictionary.CACHE_INDEX,
    // Dictionary.getCachedIndexOffset(idx) + IndexKey.KEY_OFFSET,
    // CacheKey.KEY_LENGTH));
    // System.out.println("input: " + ArrayHelper.toHexString(inputKey.key, 0,
    // inputKey.keyLength));
    return ClientHelper.compareExtended(Dictionary.CACHE_INDEX, Dictionary.getCachedIndexOffset(idx) + IndexKey.KEY_OFFSET, CacheKey.KEY_LENGTH, inputKey.key,
        0, inputKey.keyLength);
  }

  private static int getCachedIndexOffset(int idx) {
    return (idx - Dictionary.cachedIdxStart) * IndexKey.INDEX_SIZE;
  }

  private static final void readIndex(int idx) throws IOException {
    if (!Dictionary.isIdxIndexCached(idx)) {
      Dictionary.inIndex.seek(idx * IndexKey.INDEX_SIZE);
      Dictionary.inIndex.read(Dictionary.CACHE_INDEX);
      Dictionary.cachedIdxStart = idx;
    }
  }

  private static final boolean isIdxIndexCached(int idx) {
    return (idx >= Dictionary.cachedIdxStart) && (idx < (Dictionary.cachedIdxStart + Dictionary.MAX_INDEXES_CACHED));
  }

  // test only
  static String readIndexKey(int idx) {
    try {
      Dictionary.inIndex.seek((idx * IndexKey.INDEX_SIZE) + IndexKey.KEY_OFFSET);
      Dictionary.inIndex.read(Dictionary.CACHE, 0, CacheKey.KEY_LENGTH);
      return ArrayHelper.toHexString(Dictionary.CACHE, 0, CacheKey.KEY_LENGTH);
    } catch (IOException e) {
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
  public static Translation readDataKey(int idxIndex, Translation trl) throws IOException {
    Dictionary.readDataOffsets(idxIndex);

    Dictionary.inData.seek(Dictionary.DATA_OFFSETS[0]);
    Dictionary.inData.read(Dictionary.CACHE, 0, Dictionary.DATA_OFFSETS[1]);

    trl.update(idxIndex, 0xff & Dictionary.CACHE_INDEX[Dictionary.getCachedIndexOffset(idxIndex)],
        0xff & Dictionary.CACHE_INDEX[Dictionary.getCachedIndexOffset(idxIndex) + 1], ArrayHelper.toString(Dictionary.CACHE, 0, Dictionary.DATA_OFFSETS[2]),
        ArrayHelper.toString(Dictionary.CACHE, Dictionary.DATA_OFFSETS[2], Math.max(0, Dictionary.DATA_OFFSETS[3])));
    return trl;
  }

  private static void readDataOffsets(int idxIndex) throws IOException {
    if (idxIndex != 0) {
      Dictionary.readIndex(idxIndex - 1);
      Dictionary.DATA_OFFSETS[0] = ClientHelper.toInt(Dictionary.CACHE_INDEX, Dictionary.getCachedIndexOffset(idxIndex - 1) + IndexKey.KEY_OFFSET
          + CacheKey.KEY_LENGTH + 2);
    } else {
      Dictionary.DATA_OFFSETS[0] = 0;
    }
    Dictionary.readIndex(idxIndex);
    final int dataOffNext = ClientHelper.toInt(Dictionary.CACHE_INDEX, Dictionary.getCachedIndexOffset(idxIndex) + IndexKey.KEY_OFFSET + CacheKey.KEY_LENGTH
        + 2);
    Dictionary.DATA_OFFSETS[2] = ClientHelper.toShort(Dictionary.CACHE_INDEX, Dictionary.getCachedIndexOffset(idxIndex) + IndexKey.KEY_OFFSET
        + CacheKey.KEY_LENGTH);
    Dictionary.DATA_OFFSETS[1] = dataOffNext - Dictionary.DATA_OFFSETS[0];
    Dictionary.DATA_OFFSETS[3] = Dictionary.DATA_OFFSETS[1] - Dictionary.DATA_OFFSETS[2];
  }

  public static void findContainsAndEndsWithIndexes(IndexKey inputKey) {
    final boolean findContains = inputKey.keyLength > Dictionary.MIN_INDEXES_CONTAINS_KEYLENGTH;
    if (findContains) {
      try {
        int cmp;
        int i = 0;
        while (i < Dictionary.totalDefs) {
          final int lng = Dictionary.getIndexLanguage(i);
          if (Context.isUserLanguage(lng)) {
            cmp = Dictionary.contains(inputKey, i);

            if (cmp == Integer.MAX_VALUE) {
              // ends with
              Context.searchResult.idxResult.add(i);
            } else if (cmp >= 0) {
              // contains
              Context.searchResult.idxResult.add(i);
            }
          }
          i++;
        }
        System.out.println("total: " + Dictionary.totalDefs + ", i: " + i);
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

  /**
   * Starts with and equals are excluded.
   * 
   * @param inputKey
   * @param idx
   * @return
   * @throws IOException
   */
  private static final int contains(IndexKey inputKey, int idx) throws IOException {
    Dictionary.readIndex(idx);
    // if (idx == 28109)
    // {
    // System.out.println("cache: "
    // + ArrayHelper.toHexString(Dictionary.CACHE_INDEX,
    // Dictionary.getCachedIndexOffset(idx) + IndexKey.KEY_OFFSET,
    // CacheKey.KEY_LENGTH));
    // System.out.println("input: " + ArrayHelper.toHexString(inputKey.key, 0,
    // inputKey.keyLength));
    // }
    // INFO: + 1 to skip starts with and equals
    return ClientHelper.containsExtended(Dictionary.CACHE_INDEX, Dictionary.getCachedIndexOffset(idx) + IndexKey.KEY_OFFSET + 1, CacheKey.KEY_LENGTH,
        inputKey.key, 0, inputKey.keyLength);

  }

  public static Translation[] readTranslations() {
    final int size = Context.searchResult.idxResult.size();
    Translation[] trls = new Translation[size];
    try {
      Translation trl;
      for (int i = 0; i < size; i++) {
        trl = ClientHelper.borrowTranslation();
        int idxData = Context.searchResult.idxResult.get(i);
        Dictionary.readDataKey(idxData, trl);
        trls[i] = trl;
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return trls;
  }
}
