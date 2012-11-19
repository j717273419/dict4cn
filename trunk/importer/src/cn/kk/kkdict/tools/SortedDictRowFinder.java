package cn.kk.kkdict.tools;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import cn.kk.kkdict.beans.DictByteBufferRow;
import cn.kk.kkdict.types.Language;
import cn.kk.kkdict.utils.ArrayHelper;
import cn.kk.kkdict.utils.Helper;

public final class SortedDictRowFinder {
  private static final boolean    DEBUG   = true;
  private ByteBuffer              cachedBytes;
  private ByteBuffer              fileBB;
  private final String            sortedDictFile;
  private final DictByteBufferRow mainRow = new DictByteBufferRow();
  private final ByteBuffer        lngBB;
  private int[]                   linePos;

  public SortedDictRowFinder(final Language sortLng, final String file) {
    this.sortedDictFile = file;
    this.lngBB = ByteBuffer.wrap(sortLng.keyBytes);
  }

  public SortedDictRowFinder prepare() throws IOException {
    this.loadFile(this.sortedDictFile);
    this.indexNewLines();
    final int maxLen = this.indexLines();
    this.cachedBytes = ArrayHelper.borrowByteBuffer(maxLen);
    return this;
  }

  @Override
  public void finalize() throws Throwable {
    ArrayHelper.giveBack(this.cachedBytes);
    super.finalize();
  }

  private void indexNewLines() {
    final int lines = ArrayHelper.count(this.fileBB, Helper.SEP_NEWLINE_BYTES);
    this.linePos = new int[lines + 1];
  }

  private final int indexLines() {
    this.fileBB.clear();
    byte b;
    int lineCounter = 0;
    int len = 0;
    int maxLen = 0;
    while (this.fileBB.hasRemaining()) {
      b = this.fileBB.get();
      if (Helper.SEP_NEWLINE_CHAR == b) {
        if (len > maxLen) {
          maxLen = len;
        }
        len = 0;
        this.linePos[++lineCounter] = this.fileBB.position();
      } else {
        len++;
      }
    }
    return maxLen;
  }

  private final long loadFile(final String inFile) throws IOException {
    try (RandomAccessFile f = new RandomAccessFile(inFile, "r"); final FileChannel fileChannel = f.getChannel();) {
      final long size = fileChannel.size();
      if (size > Integer.MAX_VALUE) {
        System.err.println("文件不能超过2GB：" + inFile);
        return -1L;
      }
      this.fileBB = ByteBuffer.allocate((int) size);
      if (SortedDictRowFinder.DEBUG) {
        System.out.println("导入文件'" + inFile + "'，文件大小：" + Helper.formatSpace(this.fileBB.limit()));
      }
      fileChannel.read(this.fileBB);
      this.fileBB.rewind();

      return size;
    }
  }

  public final int find(final ByteBuffer key) {
    return this.find(0, this.linePos.length, key);
  }

  public final int find(final int fromIndex, final int toIndex, final ByteBuffer key) {
    int low = fromIndex;
    int high = toIndex - 1;

    while (low <= high) {
      final int mid = (low + high) >>> 1;
      this.read(mid, this.cachedBytes);
      final int cmp = ArrayHelper.compareTo(this.cachedBytes, key);

      if (cmp < 0) {
        low = mid + 1;
      } else if (cmp > 0) {
        high = mid - 1;
      } else {
        return mid; // key found
      }
    }
    return -1;
  }

  public final DictByteBufferRow findRow(final ByteBuffer key) {
    return this.findRow(0, this.linePos.length, key);
  }

  public final DictByteBufferRow findRow(final int fromIndex, final int toIndex, final ByteBuffer key) {
    final int idx = this.find(fromIndex, toIndex, key);
    if (idx != -1) {
      return this.mainRow;
    } else {
      return null;
    }
  }

  private final int read(final int fileIdx, final ByteBuffer transferBB) {
    this.positionFileBB(fileIdx);
    this.mainRow.parseFrom(this.fileBB);
    // mainRow.debug(0);
    int defIdx;
    if (-1 != (defIdx = this.mainRow.indexOfLanguage(this.lngBB))) {
      transferBB.clear();
      return ArrayHelper.copyP(this.mainRow.getFirstValue(defIdx), transferBB);
    }
    transferBB.limit(0);
    return -1;
  }

  protected final ByteBuffer positionFileBB(final int idx) {
    this.fileBB.position(this.linePos[idx]);
    return this.fileBB;
  }

}
