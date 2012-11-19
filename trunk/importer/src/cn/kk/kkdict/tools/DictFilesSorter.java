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

import cn.kk.kkdict.beans.DictByteBufferRow;
import cn.kk.kkdict.types.Language;
import cn.kk.kkdict.utils.ArrayHelper;
import cn.kk.kkdict.utils.DictHelper;
import cn.kk.kkdict.utils.Helper;

public class DictFilesSorter {
  public static final String     SUFFIX_SKIPPED = "_srt-skipped";
  private final static boolean   CHECK          = true;
  protected final static boolean DEBUG          = true;
  protected final static boolean TRACE          = true;

  private static final void swap(final IntBuffer linePosArray, final int a, final int b) {
    final int t = linePosArray.get(a);
    linePosArray.put(a, linePosArray.get(b)).put(b, t);
  }

  private static final void vecswap(final IntBuffer sortedPosArray, int a, int b, final int n) {
    int j = a;
    int k = b;
    for (int i = 0; i < n; i++, j++, k++) {
      DictFilesSorter.swap(sortedPosArray, j, k);
    }
  }

  protected final String[]        inFiles;
  protected final int[]           inFilesLengths;
  protected final int[]           inFilesStartPos;
  public final String             outFile;
  protected final ByteBuffer[]    fileRawBytes;
  private final String            skippedFile;
  private final DictByteBufferRow row1;
  private final DictByteBufferRow row2;
  private final DictByteBufferRow row3;
  private final ByteBuffer        sortLngBB;
  private ByteBuffer              bb1;
  private ByteBuffer              bb2;
  private ByteBuffer              bb3;
  protected int                   totalSorted;
  private final boolean           mergeSame;

  public DictFilesSorter(final String outFile, final Language sortLanguage, final boolean mergeSameDefinitions, final boolean writeIrrelevantToExtraFile,
      final String... inFiles) {
    this.inFiles = inFiles;
    this.outFile = outFile;
    this.mergeSame = mergeSameDefinitions;
    this.sortLngBB = ByteBuffer.wrap(sortLanguage.keyBytes);
    if (writeIrrelevantToExtraFile) {
      this.skippedFile = Helper.appendFileName(this.outFile, DictFilesSorter.SUFFIX_SKIPPED);
    } else {
      this.skippedFile = null;
    }
    this.inFilesLengths = new int[inFiles.length];
    this.inFilesStartPos = new int[inFiles.length + 1];
    this.fileRawBytes = new ByteBuffer[inFiles.length];
    this.row1 = new DictByteBufferRow();
    this.row2 = new DictByteBufferRow();
    this.row3 = new DictByteBufferRow();
  }

  private final void checkSorted(final IntBuffer linePosBuffer, final int offset, final int limit) {
    System.out.println("检查排列顺序。。。");
    final long start = System.currentTimeMillis();
    boolean check = false;
    for (int lineIdx = offset + 1; lineIdx < limit; lineIdx++) {
      if (check) {
        this.bb1 = this.readDefinition(linePosBuffer, lineIdx, this.row1);
        this.bb2 = this.readDefinition(linePosBuffer, lineIdx - 1, this.row2);
        boolean failure = false;
        if ((this.bb1.limit() != 0) && (this.bb2.limit() == 0)) {
          failure = true;
        }
        if (!failure && !(ArrayHelper.isSuccessorP(this.bb1, this.bb2) || ArrayHelper.equalsP(this.bb1, this.bb2))) {
          failure = true;
        }
        if (failure) {
          System.err.println("\n" + (lineIdx - 1) + "：" + ArrayHelper.toStringP(this.bb2) + " > " + lineIdx + "：" + ArrayHelper.toStringP(this.bb1));
        }
      }
      check = true;
    }
    System.out.println("检查排列顺序成功。用时：" + Helper.formatDuration(System.currentTimeMillis() - start) + "。");
  }

  private final String dump(final IntBuffer linePosBuffer) {
    final StringBuilder sb = new StringBuilder();
    boolean first = true;
    for (int i = 0; i < linePosBuffer.limit(); i++) {
      if (first) {
        first = false;
      } else {
        sb.append(", ");
      }
      if ((this.bb3 = this.readDefinition(linePosBuffer, i, this.row3)).limit() != 0) {
        sb.append(ArrayHelper.toStringP(this.bb3));
      }
    }
    return sb.toString();
  }

  protected final int getFileIdxByLinePos(final int linePos) {
    int i = 0;
    for (final int l : this.inFilesStartPos) {
      if (linePos < l) {
        break;
      }
      i++;
    }
    return i - 1;
  }

  protected final ByteBuffer getFilePosBuffer(final IntBuffer linePosBuffer, final int lineIdx) {
    final int linePosition = linePosBuffer.get(lineIdx);
    final int inFileIdx = this.getFileIdxByLinePos(linePosition);
    if ((inFileIdx != -1) && (inFileIdx < this.inFiles.length)) {
      final int lastBytesLimit = this.inFilesStartPos[inFileIdx];
      final int start = linePosition - lastBytesLimit;
      return (ByteBuffer) this.fileRawBytes[inFileIdx].clear().position(start);
    } else {
      System.err.println("err: " + lineIdx + ", " + linePosBuffer.get(lineIdx) + ", " + inFileIdx + "/" + this.inFiles.length);
      return null;
    }
  }

  public final int getTotalSorted() {
    return this.totalSorted;
  }

  private final int med3(final IntBuffer sortedPosArray, final int a, final int b, final int c) {
    this.bb1 = this.readDefinition(sortedPosArray, a, this.row1);
    this.bb2 = this.readDefinition(sortedPosArray, b, this.row2);
    this.bb3 = this.readDefinition(sortedPosArray, c, this.row3);
    return (ArrayHelper.isSuccessorP(this.bb2, this.bb1) ? (ArrayHelper.isSuccessorP(this.bb3, this.bb2) ? b : ArrayHelper.isSuccessorP(this.bb3, this.bb1) ? c
        : a) : (ArrayHelper.isSuccessorP(this.bb2, this.bb3) ? b : ArrayHelper.isSuccessorP(this.bb1, this.bb3) ? c : a));
  }

  private final ByteBuffer readDefinition(final IntBuffer linePosBuffer, final int lineIdx, final DictByteBufferRow row) {
    final ByteBuffer filePosBuffer = this.getFilePosBuffer(linePosBuffer, lineIdx);
    row.parseFrom(filePosBuffer);
    final int idx = row.indexOfLanguage(this.sortLngBB);
    if (idx == -1) {
      return row.lastResult();
    } else {
      return row.getFirstValue(idx);
    }
  }

  public void sort() throws IOException {
    long start = System.currentTimeMillis();

    System.out.println("缓存文件...");
    int fileIdx = 0;
    long totalSize = 0;
    for (final String inFile : this.inFiles) {
      if (new File(inFile).isFile()) {
        try (RandomAccessFile f = new RandomAccessFile(inFile, "r"); final FileChannel fileChannel = f.getChannel();) {
          final long size = fileChannel.size();
          if (size > Integer.MAX_VALUE) {
            System.err.println("文件不能超过2GB：" + inFile);
            return;
          }
          final ByteBuffer bb = ByteBuffer.allocate((int) size);
          if (DictFilesSorter.DEBUG) {
            System.out.println("导入文件'" + inFile + "'，文件大小：" + Helper.formatSpace(bb.limit()));
          }
          fileChannel.read(bb);
          bb.rewind();
          this.fileRawBytes[fileIdx++] = bb;
          totalSize += size;
        }
      } else {
        System.err.println("文件不存在（不可读）'" + inFile + "'");
      }
    }
    if (totalSize > Integer.MAX_VALUE) {
      throw new RuntimeException("文件太大。请使用其他排序算法。如mergesort。");
    }
    IntBuffer linePosBuffer = IntBuffer.allocate(Math.max(100, (int) (totalSize / 80)));
    Arrays.fill(linePosBuffer.array(), 0, linePosBuffer.limit() - 1, -1);

    System.out.println("分析文件行序...");
    byte b = Helper.SEP_NEWLINE_CHAR;
    int linesCounter;
    fileIdx = 0;
    int maxLen = 0;
    linesCounter = 0;
    int lastPos = 0;
    int tmp = 0;
    for (final ByteBuffer bb : this.fileRawBytes) {
      bb.rewind();
      int p;
      linePosBuffer.put(linesCounter, lastPos);
      tmp = lastPos;
      while (bb.hasRemaining()) {
        b = bb.get();
        if (Helper.SEP_NEWLINE_CHAR == b) {
          if (++linesCounter == linePosBuffer.capacity()) {
            final IntBuffer buf = IntBuffer.allocate(linePosBuffer.capacity() * 2);
            System.arraycopy(linePosBuffer.array(), 0, buf.array(), 0, linePosBuffer.capacity());
            linePosBuffer = buf;
          }
          p = lastPos + bb.position();
          maxLen = Math.max(p - tmp, maxLen);
          linePosBuffer.put(linesCounter, p);
          tmp = p;
        }
      }
      if ((bb.limit() > 0) && (b != Helper.SEP_NEWLINE_CHAR) && (b != '\r')) {
        linesCounter++;
        p = lastPos + bb.position();
        maxLen = Math.max(p - tmp, maxLen);
      }
      lastPos += bb.limit();
      this.inFilesLengths[fileIdx] = linesCounter;
      this.inFilesStartPos[fileIdx + 1] = lastPos;
      fileIdx++;
    }
    if (linesCounter > 0) {
      linePosBuffer.limit(linesCounter);
      System.out.println("预读" + this.inFiles.length + "个文件。总共" + Helper.formatSpace(totalSize) + "。用时" + (System.currentTimeMillis() - start) + " ms。");
      start = System.currentTimeMillis();
      System.out.print("排序总共" + linePosBuffer.limit() + "行，最长" + maxLen + "字节。平均" + (totalSize / linePosBuffer.limit()) + "字节 。。。");
      this.sort(linePosBuffer, 0, linePosBuffer.limit());
      System.out.print("用时：" + Helper.formatDuration(System.currentTimeMillis() - start) + "，");

      try (final BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(this.outFile), Helper.BUFFER_SIZE);
          BufferedOutputStream skippedOut = this.getSkippedOut(out);) {
        if (DictFilesSorter.CHECK) {
          this.checkSorted(linePosBuffer, 0, linePosBuffer.limit());
        }
        start = System.currentTimeMillis();
        if (DictFilesSorter.DEBUG) {
          System.out.println("写出'" + this.outFile + "'文件。。。");
        }
        this.totalSorted = this.write(out, skippedOut, linePosBuffer);
      }
      System.out.println("写出" + this.totalSorted + "行。文件：" + this.outFile + "（" + Helper.formatSpace(new File(this.outFile).length()) + "）。用时："
          + (System.currentTimeMillis() - start) + " ms。");
    } else {
      System.err.println("排序失败。输入文件为空！");
    }
  }

  @SuppressWarnings("resource")
  private BufferedOutputStream getSkippedOut(final BufferedOutputStream out) throws FileNotFoundException {
    BufferedOutputStream skippedOut = null;
    if (this.skippedFile != null) {
      skippedOut = new BufferedOutputStream(new FileOutputStream(this.skippedFile), Helper.BUFFER_SIZE);
    } else {
      skippedOut = out;
    }
    return skippedOut;
  }

  private final void sort(final IntBuffer linePosArray, final int offset, final int length) throws UnsupportedEncodingException {
    if (DictFilesSorter.DEBUG && DictFilesSorter.TRACE) {
      System.out.println("sort(" + offset + ", " + length + ")");
      if (DictFilesSorter.TRACE) {
        System.out.println("in: " + this.dump(linePosArray));
      }
    }
    if (length < 7) {
      for (int i = offset; i < (length + offset); i++) {
        for (int j = i; j > offset; j--) {
          this.bb1 = this.readDefinition(linePosArray, j - 1, this.row1);
          this.bb2 = this.readDefinition(linePosArray, j, this.row2);
          if (ArrayHelper.isSuccessorP(this.bb1, this.bb2)) {
            DictFilesSorter.swap(linePosArray, j, j - 1);
          } else {
            break;
          }
        }
      }
      if (DictFilesSorter.DEBUG && DictFilesSorter.TRACE) {
        System.out.println("io: " + this.dump(linePosArray));
      }
      return;
    }

    int m = offset + (length >> 1);
    if (length > 7) {
      int l = offset;
      int n = (offset + length) - 1;
      if (length > 40) { // Big arrays, pseudomedian of 9
        final int s = length / 8;
        l = this.med3(linePosArray, l, l + s, l + (2 * s));
        m = this.med3(linePosArray, m - s, m, m + s);
        n = this.med3(linePosArray, n - (2 * s), n - s, n);
      }
      m = this.med3(linePosArray, l, m, n); // Mid-size, med of 3
    }

    this.bb3 = this.readDefinition(linePosArray, m, this.row3);
    if (DictFilesSorter.DEBUG && DictFilesSorter.TRACE) {
      System.out.println("v(" + m + ")=" + ArrayHelper.toStringP(this.bb3));
    }

    int a = offset;
    int b = a;
    int c = (offset + length) - 1;
    int d = c;
    while (true) {
      while (b <= c) {
        this.bb2 = this.readDefinition(linePosArray, b, this.row2);
        if (ArrayHelper.isPredessorEqualsP(this.bb2, this.bb3)) {
          if (ArrayHelper.equalsP(this.bb2, this.bb3)) {
            if (DictFilesSorter.DEBUG && DictFilesSorter.TRACE) {
              System.out.println("swap(a:" + a + ", b:" + b + ")");
              if (DictFilesSorter.TRACE) {
                System.out.println(this.dump(linePosArray));
              }
            }
            DictFilesSorter.swap(linePosArray, a++, b);
            if (DictFilesSorter.DEBUG && DictFilesSorter.TRACE) {
              if (DictFilesSorter.TRACE) {
                System.out.println(this.dump(linePosArray));
              }
            }
          }
          b++;
        } else {
          break;
        }
      }
      while (c >= b) {
        this.bb2 = this.readDefinition(linePosArray, c, this.row2);
        if (ArrayHelper.isPredessorEquals(this.bb3, this.bb2)) {
          if (ArrayHelper.equals(this.bb2, this.bb3)) {
            if (DictFilesSorter.DEBUG && DictFilesSorter.TRACE) {
              System.out.println("swap(c:" + c + ", d:" + d + ")");
              if (DictFilesSorter.TRACE) {
                System.out.println(this.dump(linePosArray));
              }
            }
            DictFilesSorter.swap(linePosArray, c, d--);
            if (DictFilesSorter.DEBUG && DictFilesSorter.TRACE) {
              if (DictFilesSorter.TRACE) {
                System.out.println(this.dump(linePosArray));
              }
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
      DictFilesSorter.swap(linePosArray, b++, c--);
    }

    // Swap partition elements back to middle
    int s;
    final int n = offset + length;
    s = Math.min(a - offset, b - a);
    DictFilesSorter.vecswap(linePosArray, offset, b - s, s);
    s = Math.min(d - c, n - d - 1);
    DictFilesSorter.vecswap(linePosArray, b, n - s, s);

    // Recursively sort non-partition-elements
    if ((s = b - a) > 1) {
      this.sort(linePosArray, offset, s);
    }
    if ((s = d - c) > 1) {
      this.sort(linePosArray, n - s, s);
    }

    if (DictFilesSorter.DEBUG && DictFilesSorter.TRACE) {
      System.out.println("so: " + this.dump(linePosArray));
    }
  }

  protected int write(final BufferedOutputStream out, final BufferedOutputStream skippedOut, final IntBuffer linePosBuffer) throws IOException {
    int lines = 0;
    int skippedDuplicated = 0;
    int skippedNoKeyFound = 0;
    DictByteBufferRow rowPrev = this.row2;
    ByteBuffer bbPrev = ArrayHelper.wrap(this.readDefinition(linePosBuffer, 0, rowPrev));
    // TODO use quick algorithm to get first sorted value
    // rowPrev.sortValues();
    DictByteBufferRow rowCurr = this.row1;
    ByteBuffer bbCurr;

    final ByteBuffer tmpBB = ArrayHelper.borrowByteBufferLarge();
    for (int i = 1; i < linePosBuffer.limit(); i++) {
      bbCurr = ArrayHelper.wrap(this.readDefinition(linePosBuffer, i, rowCurr));
      if (!rowCurr.equals(rowPrev)) {
        if (bbPrev.limit() == 0) {
          // last row has no sort lng
          bbPrev = this.writeRow(skippedOut, rowPrev);
          skippedNoKeyFound++;
        } else {
          if (!this.mergeSame || (bbCurr.limit() == 0) || !ArrayHelper.equalsP(bbCurr, bbPrev)) {
            bbPrev = this.writeRow(out, rowPrev);
            lines++;
          } else {
            // last row == current row --> merge rows
            DictHelper.mergeDefinitionsAndAttributes(rowCurr, rowPrev, tmpBB);
            rowCurr.parseFrom(tmpBB);
            // rowCurr.parseFrom(tmpBB).sortValues();
            final int idx = rowCurr.indexOfLanguage(this.sortLngBB);
            bbCurr = ArrayHelper.wrap(rowCurr.getFirstValue(idx));
            skippedDuplicated++;
          }
        }

        // switch
        final DictByteBufferRow rowTmp = rowPrev;
        final ByteBuffer bbTmp = bbPrev;
        bbPrev = bbCurr;
        rowPrev = rowCurr;
        bbCurr = bbTmp;
        rowCurr = rowTmp;
      }
    }
    if (bbPrev.limit() == 0) {
      bbPrev = this.writeRow(skippedOut, rowPrev);
      skippedNoKeyFound++;
    } else {
      bbPrev = this.writeRow(out, rowPrev);
      lines++;
    }
    System.out.println("跳过" + skippedDuplicated + "重复行，" + skippedNoKeyFound + "垃圾行。");
    ArrayHelper.giveBack(tmpBB);
    return lines;
  }

  private final ByteBuffer writeRow(final BufferedOutputStream out, final DictByteBufferRow row) throws IOException {
    int idx;
    if (-1 != (idx = row.indexOfLanguage(this.sortLngBB))) {
      // has sort lng
      row.write(out, idx);
    } else {
      final ByteBuffer bb = row.getByteBuffer();
      ArrayHelper.writeP(out, bb);
    }
    out.write(Helper.SEP_NEWLINE_BYTES);
    return row.getByteBuffer();
  }
}
