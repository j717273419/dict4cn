/*  Copyright (c) 2010 Xiaoyun Zhu
 * 
 *  Permission is hereby granted, free of charge, to any person obtaining a copy  
 *  of this software and associated documentation files (the "Software"), to deal  
 *  in the Software without restriction, including without limitation the rights  
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell  
 *  copies of the Software, and to permit persons to whom the Software is  
 *  furnished to do so, subject to the following conditions:
 *  
 *  The above copyright notice and this permission notice shall be included in  
 *  all copies or substantial portions of the Software.
 *  
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR  
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,  
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE  
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER  
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,  
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN  
 *  THE SOFTWARE.  
 */

package cn.kk.kkdict.tools;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

import cn.kk.kkdict.utils.ArrayHelper;
import cn.kk.kkdict.utils.DictHelper;
import cn.kk.kkdict.utils.Helper;

public class WordFilesMergedSorter {
  public static final String     SUFFIX_SKIPPED = "_srt-skipped";
  private final static boolean   CHECK          = false;
  protected final static boolean DEBUG          = false;
  protected final static boolean TRACE          = false;
  protected static final boolean USE_CACHE      = false;
  protected final static int     CACHE_SIZE     = 10;
  public static final String     OUTFILE        = "output-words_srt-result.words";
  private final boolean          skipIrrelevant;

  private static final void swap(final int[] sortedPosArray, final int a, final int b) {
    final int t = sortedPosArray[a];
    sortedPosArray[a] = sortedPosArray[b];
    sortedPosArray[b] = t;
  }

  private static final void vecswap(final int[] sortedPosArray, int a, int b, final int n) {
    int j = a;
    int k = b;
    for (int i = 0; i < n; i++, j++, k++) {
      WordFilesMergedSorter.swap(sortedPosArray, j, k);
    }
  }

  protected ByteBuffer         cachedBytes1;
  protected ByteBuffer         cachedBytes2;
  protected ByteBuffer         cachedBytes3;
  protected int                cachedIdx    = 0;

  protected final int[]        cachedKeys   = new int[WordFilesMergedSorter.CACHE_SIZE];

  protected final ByteBuffer[] cachedValues = new ByteBuffer[WordFilesMergedSorter.CACHE_SIZE];

  protected final String[]     inFiles;

  protected final int[]        inFilesLengths;

  protected final int[]        inFilesStartPos;

  public final String          outFile;

  protected final ByteBuffer[] rawBytes;

  private final String         skippedFile;

  protected int                totalSorted;

  public WordFilesMergedSorter(final String outDir, final boolean skipIrrelevant, final boolean writeIrrelevant, final String... inFiles) {
    this(outDir, WordFilesMergedSorter.OUTFILE, skipIrrelevant, writeIrrelevant, inFiles);
  }

  public WordFilesMergedSorter(final String outDir, final String outFile, final boolean skipIrrelevant, final boolean writeIrrelevant, final String... inFiles) {
    this.skipIrrelevant = skipIrrelevant;
    if (new File(outDir).isDirectory()) {
      this.inFiles = inFiles;
      this.outFile = outDir + File.separator + outFile;
      if (writeIrrelevant) {
        this.skippedFile = Helper.appendFileName(this.outFile, WordFilesMergedSorter.SUFFIX_SKIPPED);
      } else {
        this.skippedFile = null;
      }
      this.inFilesLengths = new int[inFiles.length];
      this.inFilesStartPos = new int[inFiles.length + 1];
      this.rawBytes = new ByteBuffer[inFiles.length];
      Arrays.fill(this.cachedKeys, -1);
    } else {
      this.skippedFile = null;
      this.inFiles = null;
      this.outFile = null;
      this.inFilesLengths = null;
      this.inFilesStartPos = null;
      this.rawBytes = null;
      System.err.println("文件夹不可读：'" + outDir + "'!");
    }
  }

  private void checkSorted(final int[] sortedPosArray, final int offset, final int limit) {
    System.out.println("检查排列顺序。。。");
    final long start = System.currentTimeMillis();
    int len = -1;
    boolean check = false;
    for (int i = offset; i < limit; i++) {
      if (check) {
        this.read(sortedPosArray, i, this.cachedBytes1);
        this.read(sortedPosArray, i - 1, this.cachedBytes2);
        if (!ArrayHelper.isSuccessor(this.cachedBytes1, this.cachedBytes2) && !ArrayHelper.equals(this.cachedBytes1, this.cachedBytes2)) {
          len = this.read(sortedPosArray, i - 1, this.cachedBytes3);
          System.err.println("\n" + (i - 1) + ": " + new String(this.cachedBytes3.array(), 0, len, Helper.CHARSET_UTF8) + ", "
              + ArrayHelper.toHexString(this.cachedBytes3.array(), 0, len));
          len = this.read(sortedPosArray, i, this.cachedBytes3);
          System.err.println((i) + ": " + new String(this.cachedBytes3.array(), 0, len, Helper.CHARSET_UTF8) + ", "
              + ArrayHelper.toHexString(this.cachedBytes3.array(), 0, len));
        }
      }
      check = true;
    }
    System.out.println("检查排列顺序成功。用时：" + Helper.formatDuration(System.currentTimeMillis() - start) + "。");
  }

  protected static int copyAttribute(final ByteBuffer cachedBytes, int cacheIdx, final int attrPoint, final ByteBuffer bb, final int idx) {
    int i = cacheIdx;
    if (-1 == ArrayHelper.indexOf(cachedBytes.array(), attrPoint, i - attrPoint, bb.array(), bb.position(), idx)) {
      System.arraycopy(bb.array(), bb.position(), cachedBytes.array(), i, idx);
      i += idx;
    }
    return i;
  }

  private String dump(final int[] x) {
    final ByteBuffer bb = ByteBuffer.allocate(this.cachedBytes1.capacity());
    final StringBuilder sb = new StringBuilder();
    boolean first = true;
    for (int i = 0; i < x.length; i++) {
      if (first) {
        first = false;
      } else {
        sb.append(", ");
      }
      final int len = this.read(x, i, bb);
      if (len > 0) {
        sb.append(new String(bb.array(), 0, bb.limit(), Helper.CHARSET_UTF8));
      }
    }
    return sb.toString();
  }

  protected int getInFileIdxByPos(final int startPos) {
    int i = 0;
    for (final int l : this.inFilesStartPos) {
      if (startPos < l) {
        break;
      }
      i++;
    }
    return i - 1;
  }

  protected final ByteBuffer getPosBuffer(final int[] sortedPosArray, final int idx) {
    final int startPosition = sortedPosArray[idx];
    final int inFileIdx = this.getInFileIdxByPos(startPosition);
    if ((inFileIdx != -1) && (inFileIdx < this.inFiles.length)) {
      final ByteBuffer bb = this.rawBytes[inFileIdx];
      bb.clear();
      final int lastBytesLimit = this.inFilesStartPos[inFileIdx];
      final int start = startPosition - lastBytesLimit;
      bb.position(start);
      return bb;
    } else {
      return null;
    }
  }

  public int getTotalSorted() {
    return this.totalSorted;
  }

  private int med3(final int[] sortedPosArray, final int a, final int b, final int c) {
    this.read(sortedPosArray, a, this.cachedBytes1);
    this.read(sortedPosArray, b, this.cachedBytes2);
    this.read(sortedPosArray, c, this.cachedBytes3);
    return (ArrayHelper.isSuccessor(this.cachedBytes2, this.cachedBytes1) ? (ArrayHelper.isSuccessor(this.cachedBytes3, this.cachedBytes2) ? b : ArrayHelper
        .isSuccessor(this.cachedBytes3, this.cachedBytes1) ? c : a) : (ArrayHelper.isSuccessor(this.cachedBytes2, this.cachedBytes3) ? b : ArrayHelper
        .isSuccessor(this.cachedBytes1, this.cachedBytes3) ? c : a));
  }

  protected int read(final int[] sortedPosArray, final int fileIdx, final ByteBuffer cachedBytes) {
    final int startPos = sortedPosArray[fileIdx];
    if (WordFilesMergedSorter.USE_CACHE) {
      int j;
      for (int i = 0; i < WordFilesMergedSorter.CACHE_SIZE; i++) {
        j = this.cachedKeys[i];
        if (j == startPos) {
          final ByteBuffer cached = this.cachedValues[i];
          System.arraycopy(cached.array(), 0, cachedBytes.array(), 0, cached.limit());
          cachedBytes.limit(cached.limit());
          return cached.limit();
        } else if (j == -1) {
          break;
        }
      }
    }
    final ByteBuffer bb = this.getPosBuffer(sortedPosArray, fileIdx);
    if (bb != null) {
      final int len = DictHelper.getNextStopPoint(bb, DictHelper.ORDER_ATTRIBUTE);
      System.arraycopy(bb.array(), bb.position(), cachedBytes.array(), 0, len);
      if (WordFilesMergedSorter.USE_CACHE) {
        if (this.cachedIdx >= WordFilesMergedSorter.CACHE_SIZE) {
          this.cachedIdx = 0;
        }
        this.cachedKeys[this.cachedIdx] = startPos;
        final ByteBuffer cached = this.cachedValues[this.cachedIdx];
        System.arraycopy(cachedBytes.array(), 0, cached.array(), 0, len);
        cached.limit(len);
        this.cachedIdx++;
      }
      cachedBytes.rewind().limit(len);
      if (WordFilesMergedSorter.TRACE) {
        System.out.println("读出：" + ArrayHelper.toString(cachedBytes));
      }
      return len;
    } else {
      cachedBytes.rewind().limit(0);
      return -1;
    }
  }

  protected int readMerged(final int[] sortedPosArray, final int startIdx, final int endIdx, final ByteBuffer cachedBytes) {
    int cacheIdx = 0;
    int stopPoint;
    int attrPoint = 0;
    if (startIdx == endIdx) {
      final ByteBuffer bb = this.getPosBuffer(sortedPosArray, startIdx);
      stopPoint = DictHelper.getNextStopPoint(bb, DictHelper.ORDER_PARTS);
      System.arraycopy(bb.array(), bb.position(), cachedBytes.array(), cacheIdx, stopPoint);
      cacheIdx = stopPoint;
    } else {
      boolean first = true;
      for (int i = startIdx; i <= endIdx; i++) {
        final ByteBuffer bb = this.getPosBuffer(sortedPosArray, i);
        stopPoint = DictHelper.getNextStopPoint(bb, DictHelper.ORDER_PARTS);
        if (attrPoint == 0) {
          attrPoint = DictHelper.getNextStopPoint(bb, DictHelper.ORDER_ATTRIBUTE);
        }
        if (first) {
          System.arraycopy(bb.array(), bb.position(), cachedBytes.array(), cacheIdx, attrPoint);
          cacheIdx = attrPoint;
        }
        if (stopPoint > attrPoint) {
          int idx = attrPoint;
          bb.position(bb.position() + idx);
          int rest = stopPoint - attrPoint;
          while (0 < (rest -= (idx = DictHelper.getNextStopPoint(bb, DictHelper.ORDER_ATTRIBUTE)))) {
            cacheIdx = WordFilesMergedSorter.copyAttribute(cachedBytes, cacheIdx, attrPoint, bb, idx);
            bb.position(bb.position() + idx);
          }
          cacheIdx = WordFilesMergedSorter.copyAttribute(cachedBytes, cacheIdx, attrPoint, bb, idx);
        }
        first = false;
      }
    }
    cachedBytes.limit(cacheIdx);
    return cacheIdx;
  }

  @SuppressWarnings("resource")
  public void sort() throws IOException {
    long start = System.currentTimeMillis();
    int i = 0;
    long totalSize = 0;
    for (final String inFile : this.inFiles) {
      if (new File(inFile).isFile()) {
        final long size = this.loadFile(i, inFile);
        if (size != -1) {
          totalSize += size;
          i++;
        }
      } else {
        System.err.println("文件不存在（不可读）'" + inFile + "'");
      }
    }
    if (totalSize > Integer.MAX_VALUE) {
      throw new RuntimeException("文件太大。请使用其他排序算法。如mergesort。");
    }
    IntBuffer posBuffer = IntBuffer.allocate(Math.max(100, (int) (totalSize / 80)));
    Arrays.fill(posBuffer.array(), 0, posBuffer.limit() - 1, -1);

    byte b = Helper.SEP_NEWLINE_CHAR;
    int linesCounter;
    i = 0;
    int maxLen = 0;
    linesCounter = 0;
    int lastPos = 0;
    int tmp = 0;

    for (final ByteBuffer bb : this.rawBytes) {
      bb.rewind();
      int p;
      posBuffer.put(linesCounter, lastPos);
      tmp = lastPos;
      while (bb.hasRemaining()) {
        b = bb.get();
        if (Helper.SEP_NEWLINE_CHAR == b) {
          if (++linesCounter == posBuffer.capacity()) {
            final IntBuffer buf = IntBuffer.allocate(posBuffer.capacity() * 2);
            System.arraycopy(posBuffer.array(), 0, buf.array(), 0, posBuffer.capacity());
            posBuffer = buf;
          }
          p = lastPos + bb.position();
          maxLen = Math.max(p - tmp, maxLen);
          posBuffer.put(linesCounter, p);
          tmp = p;
        }
      }
      if ((bb.limit() > 0) && (b != Helper.SEP_NEWLINE_CHAR) && (b != '\r')) {
        linesCounter++;
        p = lastPos + bb.position();
        maxLen = Math.max(p - tmp, maxLen);
      }
      lastPos += bb.limit();
      this.inFilesLengths[i] = linesCounter;
      this.inFilesStartPos[i + 1] = lastPos;
      i++;
    }
    if (linesCounter > 0) {
      posBuffer.limit(linesCounter);
      System.out.println("预读" + this.inFiles.length + "个文件。总共" + Helper.formatSpace(totalSize) + "。用时" + (System.currentTimeMillis() - start) + " ms。");
      start = System.currentTimeMillis();
      System.out.print("排序总共" + posBuffer.limit() + "行，最长" + maxLen + "字节。平均" + (totalSize / posBuffer.limit()) + "字节 。。。");
      this.cachedBytes1 = ByteBuffer.allocate(maxLen);
      this.cachedBytes2 = ByteBuffer.allocate(maxLen);
      this.cachedBytes3 = ByteBuffer.allocate(maxLen);
      for (i = 0; i < this.cachedValues.length; i++) {
        this.cachedValues[i] = ByteBuffer.allocate(maxLen);
      }
      this.sort(posBuffer.array(), 0, posBuffer.limit());
      System.out.print("用时：" + Helper.formatDuration(System.currentTimeMillis() - start) + "，");

      try (final BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(this.outFile), Helper.BUFFER_SIZE);) {
        BufferedOutputStream skippedOut = null;
        if (!this.skipIrrelevant) {
          if (this.skippedFile != null) {
            skippedOut = new BufferedOutputStream(new FileOutputStream(this.skippedFile), Helper.BUFFER_SIZE);
          } else {
            skippedOut = out;
          }
        }
        if (WordFilesMergedSorter.CHECK) {
          this.checkSorted(posBuffer.array(), 0, posBuffer.limit());
        }
        start = System.currentTimeMillis();
        if (WordFilesMergedSorter.DEBUG) {
          System.out.println("写出'" + this.outFile + "'文件。。。");
        }
        final int writtenLines = this.write(out, skippedOut, posBuffer.array(), 0, posBuffer.limit());
        if (skippedOut != null) {
          skippedOut.close();
        }

        this.totalSorted = writtenLines;
        System.out.println("写出" + writtenLines + "行。文件：" + this.outFile + "（" + Helper.formatSpace(new File(this.outFile).length()) + "）。用时："
            + (System.currentTimeMillis() - start) + " ms。");
      }

    } else {
      System.err.println("排序失败。输入文件为空！");
    }
  }

  private long loadFile(final int i, final String inFile) throws FileNotFoundException, IOException {
    try (RandomAccessFile f = new RandomAccessFile(inFile, "r"); final FileChannel fileChannel = f.getChannel();) {
      final long size = fileChannel.size();
      if (size > Integer.MAX_VALUE) {
        System.err.println("文件不能超过2GB：" + inFile);
        return -1L;
      }
      final ByteBuffer bb = ByteBuffer.allocate((int) size);
      if (WordFilesMergedSorter.DEBUG) {
        System.out.println("导入文件'" + inFile + "'，文件大小：" + Helper.formatSpace(bb.limit()));
      }
      fileChannel.read(bb);
      bb.rewind();

      this.rawBytes[i] = bb;
      return size;
    }
  }

  private final void sort(final int x[], final int off, final int len) throws UnsupportedEncodingException {
    if (WordFilesMergedSorter.TRACE) {
      System.out.println("sort(" + off + ", " + len + ")");
      if (WordFilesMergedSorter.TRACE) {
        System.out.println("in: " + this.dump(x));
      }
    }
    if (len < 7) {
      // insertion sort
      for (int i = off; i < (len + off); i++) {
        for (int j = i; j > off; j--) {
          this.read(x, j - 1, this.cachedBytes1);
          this.read(x, j, this.cachedBytes2);
          if (ArrayHelper.isSuccessor(this.cachedBytes1, this.cachedBytes2)) {
            WordFilesMergedSorter.swap(x, j, j - 1);
          } else {
            break;
          }
        }
      }
      if (WordFilesMergedSorter.TRACE) {
        System.out.println("io: " + this.dump(x));
      }
      return;
    }

    // Choose a partition element, v
    int m = off + (len >> 1); // Small arrays, middle element
    if (len > 7) {
      int l = off;
      int n = (off + len) - 1;
      if (len > 40) { // Big arrays, pseudomedian of 9
        final int s = len / 8;
        l = this.med3(x, l, l + s, l + (2 * s));
        m = this.med3(x, m - s, m, m + s);
        n = this.med3(x, n - (2 * s), n - s, n);
      }
      m = this.med3(x, l, m, n); // Mid-size, med of 3
    }
    this.read(x, m, this.cachedBytes3);
    if (WordFilesMergedSorter.TRACE) {
      System.out.println("v(" + m + ")=" + ArrayHelper.toString(this.cachedBytes3));
    }
    // Establish Invariant: v* (<v)* (>v)* v*
    int a = off, b = a, c = (off + len) - 1, d = c;
    while (true) {
      while (b <= c) {
        this.read(x, b, this.cachedBytes2);
        if (ArrayHelper.isPredessorEquals(this.cachedBytes2, this.cachedBytes3)) {
          if (ArrayHelper.equals(this.cachedBytes2, this.cachedBytes3)) {
            if (WordFilesMergedSorter.TRACE) {
              System.out.println("swap(a:" + a + ", b:" + b + ")");
              if (WordFilesMergedSorter.TRACE) {
                System.out.println(this.dump(x));
              }
            }
            WordFilesMergedSorter.swap(x, a++, b);
            if (WordFilesMergedSorter.TRACE) {
              System.out.println(this.dump(x));
            }
          }
          b++;
        } else {
          break;
        }
      }
      while (c >= b) {
        this.read(x, c, this.cachedBytes2);
        if (ArrayHelper.isPredessorEquals(this.cachedBytes3, this.cachedBytes2)) {
          if (ArrayHelper.equals(this.cachedBytes2, this.cachedBytes3)) {
            if (WordFilesMergedSorter.TRACE) {
              System.out.println("swap(c:" + c + ", d:" + d + ")");
              if (WordFilesMergedSorter.TRACE) {
                System.out.println(this.dump(x));
              }
            }
            WordFilesMergedSorter.swap(x, c, d--);
            if (WordFilesMergedSorter.TRACE) {
              System.out.println(this.dump(x));
            }
          }
          c--;
        } else {
          break;
        }
      }
      if (b > c) {
        break;
      }
      WordFilesMergedSorter.swap(x, b++, c--);
    }

    // Swap partition elements back to middle
    int s;
    final int n = off + len;
    s = Math.min(a - off, b - a);
    WordFilesMergedSorter.vecswap(x, off, b - s, s);
    s = Math.min(d - c, n - d - 1);
    WordFilesMergedSorter.vecswap(x, b, n - s, s);

    // Recursively sort non-partition-elements
    if ((s = b - a) > 1) {
      this.sort(x, off, s);
    }
    if ((s = d - c) > 1) {
      this.sort(x, n - s, s);
    }
    if (WordFilesMergedSorter.TRACE) {
      System.out.println("so: " + this.dump(x));
    }
  }

  protected int write(final BufferedOutputStream out, final BufferedOutputStream skippedOut, final int[] sortedPosArray, final int offset, final int limit)
      throws IOException {
    int len = -1;
    int lines = 0;
    int skippedDuplicated = 0;
    int skippedNoKeyFound = 0;
    int startEqual = -1;
    final byte[] cachedBytes2Array = this.cachedBytes2.array();
    for (int i = offset; i < limit; i++) {
      len = this.read(sortedPosArray, i, this.cachedBytes2);
      if ((len == this.cachedBytes1.limit()) && (startEqual != -1) && ArrayHelper.equals(cachedBytes2Array, 0, this.cachedBytes1.array(), 0, len)) {
        skippedDuplicated++;
        // System.out.println("continue: " + new String(cachedBytes2.array(), 0, len, Helper.CHARSET_UTF8));
        continue;
      } else {
        // System.out.println("old: " + new String(cachedBytes1.array(), 0, cachedBytes1.limit(),
        // Helper.CHARSET_UTF8));
        // System.out.println("new: " + new String(cachedBytes2.array(), 0, len, Helper.CHARSET_UTF8));
        if (len != -1) {
          System.arraycopy(cachedBytes2Array, 0, this.cachedBytes1.array(), 0, len);
          this.cachedBytes1.limit(len);
        }
        if ((startEqual != -1) && this.writeRange(out, sortedPosArray, startEqual, i - 1)) {
          lines++;
        }
        if (len != -1) {
          startEqual = i;
        } else {
          if (skippedOut != null) {
            final ByteBuffer bb = this.getPosBuffer(sortedPosArray, i);
            if (bb != null) {
              final int l = DictHelper.getNextStopPoint(bb, DictHelper.ORDER_PARTS);
              skippedOut.write(bb.array(), bb.position(), l);
              skippedOut.write(Helper.SEP_NEWLINE_CHAR);
            }
          }
          skippedNoKeyFound++;
          startEqual = -1;
        }
      }
    }
    if (startEqual != -1) {
      if (this.writeRange(out, sortedPosArray, startEqual, limit - 1)) {
        lines++;
      }
    }
    System.out.println("跳过" + skippedDuplicated + "重复行，" + skippedNoKeyFound + "垃圾行。");
    return lines;
  }

  protected final boolean writeRange(final BufferedOutputStream out, final int[] sortedPosArray, final int startIdx, final int endIdx) throws IOException {
    int len;
    if ((startIdx != -1) && (endIdx >= startIdx)) {
      // System.out.println("merge: " + startIdx + ", " + endIdx);
      final ByteBuffer bb = ArrayHelper.borrowByteBufferLarge();
      len = this.readMerged(sortedPosArray, startIdx, endIdx, bb);
      out.write(bb.array(), 0, len);
      out.write(Helper.SEP_NEWLINE_CHAR);
      ArrayHelper.giveBack(bb);
      return true;
    }
    return false;
  }
}
