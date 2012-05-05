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
    public static final String SUFFIX_SKIPPED = "_srt-skipped";
    private final static boolean CHECK = true;
    protected final static boolean DEBUG = true;
    protected final static boolean TRACE = true;

    private static final void swap(final IntBuffer linePosArray, final int a, final int b) {
        final int t = linePosArray.get(a);
        linePosArray.put(a, linePosArray.get(b)).put(b, t);
    }

    private static final void vecswap(final IntBuffer sortedPosArray, int a, int b, final int n) {
        for (int i = 0; i < n; i++, a++, b++) {
            swap(sortedPosArray, a, b);
        }
    }

    protected final String[] inFiles;
    protected final int[] inFilesLengths;
    protected final int[] inFilesStartPos;
    public final String outFile;
    protected final ByteBuffer[] fileRawBytes;
    private final String skippedFile;
    private final DictByteBufferRow row1;
    private final DictByteBufferRow row2;
    private final DictByteBufferRow row3;
    private final ByteBuffer sortLngBB;
    private ByteBuffer bb1;
    private ByteBuffer bb2;
    private ByteBuffer bb3;
    protected int totalSorted;
    private final boolean mergeSame;

    public DictFilesSorter(final String outFile, final Language sortLanguage, final boolean mergeSameDefinitions,
            final boolean writeIrrelevantToExtraFile, final String... inFiles) {
        this.inFiles = inFiles;
        this.outFile = outFile;
        this.mergeSame = mergeSameDefinitions;
        this.sortLngBB = ByteBuffer.wrap(sortLanguage.key.getBytes(Helper.CHARSET_UTF8));
        if (writeIrrelevantToExtraFile) {
            this.skippedFile = Helper.appendFileName(this.outFile, SUFFIX_SKIPPED);
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
        long start = System.currentTimeMillis();
        boolean check = false;
        for (int lineIdx = offset + 1; lineIdx < limit; lineIdx++) {
            if (check) {
                bb1 = readDefinition(linePosBuffer, lineIdx, row1);
                bb2 = readDefinition(linePosBuffer, lineIdx - 1, row2);
                boolean failure = false;
                if (bb1.limit() != 0 && bb2.limit() == 0) {
                    failure = true;
                }
                if (!failure && !(ArrayHelper.isSuccessorP(bb1, bb2) || ArrayHelper.equalsP(bb1, bb2))) {
                    failure = true;
                }
                if (failure) {
                    System.err.println("\n" + (lineIdx - 1) + "：" + ArrayHelper.toStringP(bb2) + " > " + lineIdx + "："
                            + ArrayHelper.toStringP(bb1));
                }
            }
            check = true;
        }
        System.out.println("检查排列顺序成功。用时：" + Helper.formatDuration(System.currentTimeMillis() - start) + "。");
    }

    private final String dump(final IntBuffer linePosBuffer) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (int i = 0; i < linePosBuffer.limit(); i++) {
            if (first) {
                first = false;
            } else {
                sb.append(", ");
            }
            if ((bb3 = readDefinition(linePosBuffer, i, row3)).limit() != 0) {
                sb.append(ArrayHelper.toStringP(bb3));
            }
        }
        return sb.toString();
    }

    protected final int getFileIdxByLinePos(final int linePos) {
        int i = 0;
        for (int l : inFilesStartPos) {
            if (linePos < l) {
                break;
            }
            i++;
        }
        return i - 1;
    }

    protected final ByteBuffer getFilePosBuffer(final IntBuffer linePosBuffer, final int lineIdx) {
        final int linePosition = linePosBuffer.get(lineIdx);
        final int inFileIdx = getFileIdxByLinePos(linePosition);
        if (inFileIdx != -1 && inFileIdx < inFiles.length) {
            final int lastBytesLimit = inFilesStartPos[inFileIdx];
            final int start = linePosition - lastBytesLimit;
            return (ByteBuffer) fileRawBytes[inFileIdx].clear().position(start);
        } else {
            System.err.println("err: " + lineIdx + ", " + linePosBuffer.get(lineIdx) + ", " + inFileIdx + "/"
                    + inFiles.length);
            return null;
        }
    }

    public final int getTotalSorted() {
        return totalSorted;
    }

    private final int med3(final IntBuffer sortedPosArray, final int a, final int b, final int c)
            throws UnsupportedEncodingException {
        bb1 = readDefinition(sortedPosArray, a, row1);
        bb2 = readDefinition(sortedPosArray, b, row2);
        bb3 = readDefinition(sortedPosArray, c, row3);
        return (ArrayHelper.isSuccessorP(bb2, bb1) ? (ArrayHelper.isSuccessorP(bb3, bb2) ? b : ArrayHelper
                .isSuccessorP(bb3, bb1) ? c : a) : (ArrayHelper.isSuccessorP(bb2, bb3) ? b : ArrayHelper.isSuccessorP(
                bb1, bb3) ? c : a));
    }

    private final ByteBuffer readDefinition(final IntBuffer linePosBuffer, final int lineIdx,
            final DictByteBufferRow row) {
        final ByteBuffer filePosBuffer = getFilePosBuffer(linePosBuffer, lineIdx);
        row.parseFrom(filePosBuffer);
        final int idx = row.indexOfLanguage(sortLngBB);
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
        for (String inFile : inFiles) {
            if (new File(inFile).isFile()) {
                FileChannel fileChannel = new RandomAccessFile(inFile, "r").getChannel();
                long size = fileChannel.size();
                if (size > Integer.MAX_VALUE) {
                    System.err.println("文件不能超过2GB：" + inFile);
                    return;
                }
                ByteBuffer bb = ByteBuffer.allocate((int) size);
                if (DEBUG) {
                    System.out.println("导入文件'" + inFile + "'，文件大小：" + Helper.formatSpace(bb.limit()));
                }
                fileChannel.read(bb);
                bb.rewind();
                fileRawBytes[fileIdx++] = bb;
                totalSize += size;
                fileChannel.close();
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
        for (ByteBuffer bb : fileRawBytes) {
            bb.rewind();
            int p;
            linePosBuffer.put(linesCounter, lastPos);
            tmp = lastPos;
            while (bb.hasRemaining()) {
                b = bb.get();
                if (Helper.SEP_NEWLINE_CHAR == b) {
                    if (++linesCounter == linePosBuffer.capacity()) {
                        IntBuffer buf = IntBuffer.allocate(linePosBuffer.capacity() * 2);
                        System.arraycopy(linePosBuffer.array(), 0, buf.array(), 0, linePosBuffer.capacity());
                        linePosBuffer = buf;
                    }
                    p = lastPos + bb.position();
                    maxLen = Math.max(p - tmp, maxLen);
                    linePosBuffer.put(linesCounter, p);
                    tmp = p;
                }
            }
            if (bb.limit() > 0 && b != Helper.SEP_NEWLINE_CHAR && b != '\r') {
                linesCounter++;
                p = lastPos + bb.position();
                maxLen = Math.max(p - tmp, maxLen);
            }
            lastPos += bb.limit();
            inFilesLengths[fileIdx] = linesCounter;
            inFilesStartPos[fileIdx + 1] = lastPos;
            fileIdx++;
        }
        if (linesCounter > 0) {
            linePosBuffer.limit(linesCounter);
            System.out.println("预读" + inFiles.length + "个文件。总共" + Helper.formatSpace(totalSize) + "。用时"
                    + (System.currentTimeMillis() - start) + " ms。");
            start = System.currentTimeMillis();
            System.out.print("排序总共" + linePosBuffer.limit() + "行，最长" + maxLen + "字节。平均"
                    + (totalSize / linePosBuffer.limit()) + "字节 。。。");
            sort(linePosBuffer, 0, linePosBuffer.limit());
            System.out.print("用时：" + Helper.formatDuration(System.currentTimeMillis() - start) + "，");

            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outFile), Helper.BUFFER_SIZE);
            BufferedOutputStream skippedOut = null;
            if (skippedFile != null) {
                skippedOut = new BufferedOutputStream(new FileOutputStream(skippedFile), Helper.BUFFER_SIZE);
            } else {
                skippedOut = out;
            }
            if (CHECK) {
                checkSorted(linePosBuffer, 0, linePosBuffer.limit());
            }
            start = System.currentTimeMillis();
            if (DEBUG) {
                System.out.println("写出'" + outFile + "'文件。。。");
            }
            totalSorted = write(out, skippedOut, linePosBuffer);
            out.close();
            skippedOut.close();
            System.out.println("写出" + totalSorted + "行。文件：" + outFile + "（"
                    + Helper.formatSpace(new File(outFile).length()) + "）。用时：" + (System.currentTimeMillis() - start)
                    + " ms。");
        } else {
            System.err.println("排序失败。输入文件为空！");
        }
    }

    private final void sort(final IntBuffer linePosArray, final int offset, final int length)
            throws UnsupportedEncodingException {
        if (DEBUG && TRACE) {
            System.out.println("sort(" + offset + ", " + length + ")");
            if (TRACE) {
                System.out.println("in: " + dump(linePosArray));
            }
        }
        if (length < 7) {
            for (int i = offset; i < length + offset; i++) {
                for (int j = i; j > offset; j--) {
                    bb1 = readDefinition(linePosArray, j - 1, row1);
                    bb2 = readDefinition(linePosArray, j, row2);
                    if (ArrayHelper.isSuccessorP(bb1, bb2)) {
                        swap(linePosArray, j, j - 1);
                    } else {
                        break;
                    }
                }
            }
            if (DEBUG && TRACE) {
                System.out.println("io: " + dump(linePosArray));
            }
            return;
        }

        int m = offset + (length >> 1);
        if (length > 7) {
            int l = offset;
            int n = offset + length - 1;
            if (length > 40) { // Big arrays, pseudomedian of 9
                final int s = length / 8;
                l = med3(linePosArray, l, l + s, l + 2 * s);
                m = med3(linePosArray, m - s, m, m + s);
                n = med3(linePosArray, n - 2 * s, n - s, n);
            }
            m = med3(linePosArray, l, m, n); // Mid-size, med of 3
        }

        bb3 = readDefinition(linePosArray, m, row3);
        if (DEBUG && TRACE) {
            System.out.println("v(" + m + ")=" + ArrayHelper.toStringP(bb3));
        }

        int a = offset;
        int b = a;
        int c = offset + length - 1;
        int d = c;
        while (true) {
            while (b <= c) {
                bb2 = readDefinition(linePosArray, b, row2);
                if (ArrayHelper.isPredessorEqualsP(bb2, bb3)) {
                    if (ArrayHelper.equalsP(bb2, bb3)) {
                        if (DEBUG && TRACE) {
                            System.out.println("swap(a:" + a + ", b:" + b + ")");
                            if (TRACE) {
                                System.out.println(dump(linePosArray));
                            }
                        }
                        swap(linePosArray, a++, b);
                        if (DEBUG && TRACE) {
                            if (TRACE) {
                                System.out.println(dump(linePosArray));
                            }
                        }
                    }
                    b++;
                } else {
                    break;
                }
            }
            while (c >= b) {
                bb2 = readDefinition(linePosArray, c, row2);
                if (ArrayHelper.isPredessorEquals(bb3, bb2)) {
                    if (ArrayHelper.equals(bb2, bb3)) {
                        if (DEBUG && TRACE) {
                            System.out.println("swap(c:" + c + ", d:" + d + ")");
                            if (TRACE) {
                                System.out.println(dump(linePosArray));
                            }
                        }
                        swap(linePosArray, c, d--);
                        if (DEBUG && TRACE) {
                            if (TRACE) {
                                System.out.println(dump(linePosArray));
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
            swap(linePosArray, b++, c--);
        }

        // Swap partition elements back to middle
        int s;
        final int n = offset + length;
        s = Math.min(a - offset, b - a);
        vecswap(linePosArray, offset, b - s, s);
        s = Math.min(d - c, n - d - 1);
        vecswap(linePosArray, b, n - s, s);

        // Recursively sort non-partition-elements
        if ((s = b - a) > 1) {
            sort(linePosArray, offset, s);
        }
        if ((s = d - c) > 1) {
            sort(linePosArray, n - s, s);
        }

        if (DEBUG && TRACE) {
            System.out.println("so: " + dump(linePosArray));
        }
    }

    protected int write(BufferedOutputStream out, BufferedOutputStream skippedOut, IntBuffer linePosBuffer)
            throws IOException {
        int lines = 0;
        int skippedDuplicated = 0;
        int skippedNoKeyFound = 0;
        DictByteBufferRow rowPrev = row2;
        ByteBuffer bbPrev = ArrayHelper.wrap(readDefinition(linePosBuffer, 0, rowPrev));
        // TODO use quick algorithm to get first sorted value
        // rowPrev.sortValues();
        DictByteBufferRow rowCurr = row1;
        ByteBuffer bbCurr;

        ByteBuffer tmpBB = ArrayHelper.borrowByteBufferLarge();
        for (int i = 1; i < linePosBuffer.limit(); i++) {
            bbCurr = ArrayHelper.wrap(readDefinition(linePosBuffer, i, rowCurr));
            if (!rowCurr.equals(rowPrev)) {
                if (bbPrev.limit() == 0) {
                    // last row has no sort lng
                    bbPrev = writeRow(skippedOut, rowPrev);
                    skippedNoKeyFound++;
                } else {
                    if (!mergeSame || bbCurr.limit() == 0 || !ArrayHelper.equalsP(bbCurr, bbPrev)) {
                        bbPrev = writeRow(out, rowPrev);
                        lines++;
                    } else {
                        // last row == current row --> merge rows
                        DictHelper.mergeDefinitionsAndAttributes(rowCurr, rowPrev, tmpBB);
                        rowCurr.parseFrom(tmpBB);
                        // rowCurr.parseFrom(tmpBB).sortValues();
                        final int idx = rowCurr.indexOfLanguage(sortLngBB);
                        bbCurr = ArrayHelper.wrap(rowCurr.getFirstValue(idx));
                        skippedDuplicated++;
                    }
                }

                // switch
                DictByteBufferRow rowTmp = rowPrev;
                ByteBuffer bbTmp = bbPrev;
                bbPrev = bbCurr;
                rowPrev = rowCurr;
                bbCurr = bbTmp;
                rowCurr = rowTmp;
            }
        }
        if (bbPrev.limit() == 0) {
            bbPrev = writeRow(skippedOut, rowPrev);
            skippedNoKeyFound++;
        } else {
            bbPrev = writeRow(out, rowPrev);
            lines++;
        }
        System.out.println("跳过" + skippedDuplicated + "重复行，" + skippedNoKeyFound + "垃圾行。");
        ArrayHelper.giveBack(tmpBB);
        return lines;
    }

    private final ByteBuffer writeRow(final BufferedOutputStream out, DictByteBufferRow row) throws IOException {
        int idx;
        if (-1 != (idx = row.indexOfLanguage(sortLngBB))) {
            // has sort lng
            row.write(out, idx);
        } else {
            ByteBuffer bb = row.getByteBuffer();
            ArrayHelper.writeP(out, bb);
        }
        out.write(Helper.SEP_NEWLINE_BYTES);
        return row.getByteBuffer();
    }
}
