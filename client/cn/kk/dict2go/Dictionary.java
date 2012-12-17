package cn.kk.dict2go;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.tukaani.xz.SeekableXZInputStream;

import cn.kk.kkdict.utils.CompressHelper;
import cn.kk.kkdict.utils.Helper;

public class Dictionary {
  private static final String   DXZ            = "D:\\dict2cn.dxz";
  public static final int       HEADER_OFFSET  = 2;
  private static final int      MAX_CACHE_SIZE = 1024 * 1024;

  private int                   headerLen;
  private int                   totalDefs;
  private int                   cachedLen;
  private int                   idxLen;
  private int                   dataLen;
  private int                   cachedOffset;
  private int                   idxOffset;
  private int                   dataOffset;
  private DataInputStream       inMeta;
  private SeekableXZInputStream inIndex;
  private SeekableXZInputStream inData;

  private final CacheKey[]      cache;

  public Dictionary() {
    this.cache = new CacheKey[Dictionary.MAX_CACHE_SIZE / CacheKey.CACHE_SIZE];
    for (int i = 0; i < this.cache.length; i++) {
      this.cache[i] = new CacheKey();
    }
  }

  Dictionary loadMeta() throws IOException {
    try (DataInputStream in = new DataInputStream(new FileInputStream(Dictionary.DXZ))) {
      this.headerLen = in.readUnsignedShort();
    }

    this.inMeta = new DataInputStream(CompressHelper.getDecompressedInputStream(Dictionary.DXZ, Dictionary.HEADER_OFFSET, this.headerLen));
    this.totalDefs = this.inMeta.readInt();
    this.cachedLen = this.inMeta.readInt();
    this.idxLen = this.inMeta.readInt();
    this.dataLen = this.inMeta.readInt();

    this.cachedOffset = Dictionary.HEADER_OFFSET + this.headerLen;
    this.idxOffset = this.cachedOffset + this.cachedLen;
    this.dataOffset = this.idxOffset + this.idxLen;

    this.inIndex = CompressHelper.getDecompressedInputStream(Dictionary.DXZ, this.idxOffset, this.idxLen);
    this.inData = CompressHelper.getDecompressedInputStream(Dictionary.DXZ, this.dataOffset, this.dataLen);
    return this;
  }

  public void loadCachedIndexes() throws IOException {
    final byte[] baCacheKey = new byte[CacheKey.CACHE_SIZE * 1024];
    try (InputStream inCached = CompressHelper.getDecompressedInputStream(Dictionary.DXZ, this.cachedOffset, this.cachedLen);) {
      int end;
      int idx = 0;
      while (-1 != (end = inCached.read(baCacheKey))) {
        final int num = end / CacheKey.CACHE_SIZE;
        for (int i = 0; i < num; i++) {
          this.cache[idx++].read(baCacheKey, i);
        }
      }
    }
  }

  public void close() {
    Helper.close(this.inMeta, this.inIndex, this.inData);
  }

  public int findCachedStartIdx(CacheKey inputKey) {
    int i = Arrays.binarySearch(this.cache, inputKey);
    if (i < 0) {
      i = -(i + 1);
    }
    return this.cache[i].idx;

  }

}
