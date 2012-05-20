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
    public static final String SUFFIX_SKIPPED = "_srt-skipped";
    private final static boolean CHECK = false;
    protected final static boolean DEBUG = false;
    protected final static boolean TRACE = false;
    protected static final boolean USE_CACHE = false;
    protected final static int CACHE_SIZE = 10;
    public static final String OUTFILE = "output-words_srt-result.words";
    private final boolean skipIrrelevant;

    private static final void swap(final int[] sortedPosArray, final int a, final int b) {
        final int t = sortedPosArray[a];
        sortedPosArray[a] = sortedPosArray[b];
        sortedPosArray[b] = t;
    }

    private static final void vecswap(final int[] sortedPosArray, int a, int b, final int n) {
        for (int i = 0; i < n; i++, a++, b++) {
            swap(sortedPosArray, a, b);
        }
    }

    protected ByteBuffer cachedBytes1;
    protected ByteBuffer cachedBytes2;
    protected ByteBuffer cachedBytes3;
    protected int cachedIdx = 0;

    protected final int[] cachedKeys = new int[CACHE_SIZE];

    protected final ByteBuffer[] cachedValues = new ByteBuffer[CACHE_SIZE];

    protected final String[] inFiles;

    protected final int[] inFilesLengths;

    protected final int[] inFilesStartPos;

    public final String outFile;

    protected final ByteBuffer[] rawBytes;

    private final String skippedFile;

    protected int totalSorted;

    public WordFilesMergedSorter(String outDir, boolean skipIrrelevant, boolean writeIrrelevant, String... inFiles) {
        this(outDir, OUTFILE, skipIrrelevant, writeIrrelevant, inFiles);
    }

    public WordFilesMergedSorter(String outDir, String outFile, boolean skipIrrelevant, boolean writeIrrelevant,
            String... inFiles) {
        this.skipIrrelevant = skipIrrelevant;
        if (new File(outDir).isDirectory()) {
            this.inFiles = inFiles;
            this.outFile = outDir + File.separator + outFile;
            if (writeIrrelevant) {
                this.skippedFile = Helper.appendFileName(this.outFile, SUFFIX_SKIPPED);
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

    private void checkSorted(int[] sortedPosArray, int offset, int limit) {
        System.out.println("检查排列顺序。。。");
        long start = System.currentTimeMillis();
        int len = -1;
        boolean check = false;
        for (int i = offset; i < limit; i++) {
            if (check) {
                read(sortedPosArray, i, cachedBytes1);
                read(sortedPosArray, i - 1, cachedBytes2);
                if (!ArrayHelper.isSuccessor(cachedBytes1, cachedBytes2)
                        && !ArrayHelper.equals(cachedBytes1, cachedBytes2)) {
                    len = read(sortedPosArray, i - 1, cachedBytes3);
                    System.err.println("\n" + (i - 1) + ": "
                            + new String(cachedBytes3.array(), 0, len, Helper.CHARSET_UTF8) + ", "
                            + ArrayHelper.toHexString(cachedBytes3.array(), 0, len));
                    len = read(sortedPosArray, i, cachedBytes3);
                    System.err.println((i) + ": " + new String(cachedBytes3.array(), 0, len, Helper.CHARSET_UTF8)
                            + ", " + ArrayHelper.toHexString(cachedBytes3.array(), 0, len));
                }
            }
            check = true;
        }
        System.out.println("检查排列顺序成功。用时：" + Helper.formatDuration(System.currentTimeMillis() - start) + "。");
    }

    protected int copyAttribute(ByteBuffer cachedBytes, int cacheIdx, int attrPoint, ByteBuffer bb, int idx) {
        if (-1 == ArrayHelper.indexOf(cachedBytes.array(), attrPoint, cacheIdx - attrPoint, bb.array(), bb.position(),
                idx)) {
            System.arraycopy(bb.array(), bb.position(), cachedBytes.array(), cacheIdx, idx);
            cacheIdx += idx;
        }
        return cacheIdx;
    }

    private String dump(int[] x) {
        ByteBuffer bb = ByteBuffer.allocate(cachedBytes1.capacity());
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (int i = 0; i < x.length; i++) {
            if (first) {
                first = false;
            } else {
                sb.append(", ");
            }
            int len = read(x, i, bb);
            if (len > 0) {
                sb.append(new String(bb.array(), 0, bb.limit(), Helper.CHARSET_UTF8));
            }
        }
        return sb.toString();
    }

    protected int getInFileIdxByPos(int startPos) {
        int i = 0;
        for (int l : inFilesStartPos) {
            if (startPos < l) {
                break;
            }
            i++;
        }
        return i - 1;
    }

    protected final ByteBuffer getPosBuffer(int[] sortedPosArray, int idx) {
        int startPosition = sortedPosArray[idx];
        int inFileIdx = getInFileIdxByPos(startPosition);
        if (inFileIdx != -1 && inFileIdx < inFiles.length) {
            ByteBuffer bb = rawBytes[inFileIdx];
            bb.clear();
            int lastBytesLimit = inFilesStartPos[inFileIdx];
            int start = startPosition - lastBytesLimit;
            bb.position(start);
            return bb;
        } else {
            return null;
        }
    }

    public int getTotalSorted() {
        return totalSorted;
    }

    private int med3(int[] sortedPosArray, int a, int b, int c) throws UnsupportedEncodingException {
        read(sortedPosArray, a, cachedBytes1);
        read(sortedPosArray, b, cachedBytes2);
        read(sortedPosArray, c, cachedBytes3);
        return (ArrayHelper.isSuccessor(cachedBytes2, cachedBytes1) ? (ArrayHelper.isSuccessor(cachedBytes3,
                cachedBytes2) ? b : ArrayHelper.isSuccessor(cachedBytes3, cachedBytes1) ? c : a) : (ArrayHelper
                .isSuccessor(cachedBytes2, cachedBytes3) ? b : ArrayHelper.isSuccessor(cachedBytes1, cachedBytes3) ? c
                : a));
    }

    protected int read(int[] sortedPosArray, int fileIdx, ByteBuffer cachedBytes) {
        int startPos = sortedPosArray[fileIdx];
        if (USE_CACHE) {
            int j;
            for (int i = 0; i < CACHE_SIZE; i++) {
                j = cachedKeys[i];
                if (j == startPos) {
                    ByteBuffer cached = cachedValues[i];
                    System.arraycopy(cached.array(), 0, cachedBytes.array(), 0, cached.limit());
                    cachedBytes.limit(cached.limit());
                    return cached.limit();
                } else if (j == -1) {
                    break;
                }
            }
        }
        ByteBuffer bb = getPosBuffer(sortedPosArray, fileIdx);
        if (bb != null) {
            int len = DictHelper.getNextStopPoint(bb, DictHelper.ORDER_ATTRIBUTE);
            System.arraycopy(bb.array(), bb.position(), cachedBytes.array(), 0, len);
            if (USE_CACHE) {
                if (cachedIdx >= CACHE_SIZE) {
                    cachedIdx = 0;
                }
                cachedKeys[cachedIdx] = startPos;
                ByteBuffer cached = cachedValues[cachedIdx];
                System.arraycopy(cachedBytes.array(), 0, cached.array(), 0, len);
                cached.limit(len);
                cachedIdx++;
            }
            cachedBytes.rewind().limit(len);
            if (DEBUG && TRACE) {
                System.out.println("读出：" + ArrayHelper.toString(cachedBytes));
            }
            return len;
        } else {
            cachedBytes.rewind().limit(0);
            return -1;
        }
    }

    protected int readMerged(int[] sortedPosArray, int startIdx, int endIdx, ByteBuffer cachedBytes) {
        int cacheIdx = 0;
        int stopPoint;
        int attrPoint = 0;
        if (startIdx == endIdx) {
            ByteBuffer bb = getPosBuffer(sortedPosArray, startIdx);
            stopPoint = DictHelper.getNextStopPoint(bb, DictHelper.ORDER_PARTS);
            System.arraycopy(bb.array(), bb.position(), cachedBytes.array(), cacheIdx, stopPoint);
            cacheIdx = stopPoint;
        } else {
            boolean first = true;
            for (int i = startIdx; i <= endIdx; i++) {
                ByteBuffer bb = getPosBuffer(sortedPosArray, i);
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
                        cacheIdx = copyAttribute(cachedBytes, cacheIdx, attrPoint, bb, idx);
                        bb.position(bb.position() + idx);
                    }
                    cacheIdx = copyAttribute(cachedBytes, cacheIdx, attrPoint, bb, idx);
                }
                first = false;
            }
        }
        cachedBytes.limit(cacheIdx);
        return cacheIdx;
    }

    public void sort() throws IOException {
        long start = System.currentTimeMillis();
        int i = 0;
        long totalSize = 0;
        for (String inFile : inFiles) {
            if (new File(inFile).isFile()) {
                long size = loadFile(i, inFile);
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

        for (ByteBuffer bb : rawBytes) {
            bb.rewind();
            int p;
            posBuffer.put(linesCounter, lastPos);
            tmp = lastPos;
            while (bb.hasRemaining()) {
                b = bb.get();
                if (Helper.SEP_NEWLINE_CHAR == b) {
                    if (++linesCounter == posBuffer.capacity()) {
                        IntBuffer buf = IntBuffer.allocate(posBuffer.capacity() * 2);
                        System.arraycopy(posBuffer.array(), 0, buf.array(), 0, posBuffer.capacity());
                        posBuffer = buf;
                    }
                    p = lastPos + bb.position();
                    maxLen = Math.max(p - tmp, maxLen);
                    posBuffer.put(linesCounter, p);
                    tmp = p;
                }
            }
            if (bb.limit() > 0 && b != Helper.SEP_NEWLINE_CHAR && b != '\r') {
                linesCounter++;
                p = lastPos + bb.position();
                maxLen = Math.max(p - tmp, maxLen);
            }
            lastPos += bb.limit();
            inFilesLengths[i] = linesCounter;
            inFilesStartPos[i + 1] = lastPos;
            i++;
        }
        if (linesCounter > 0) {
            posBuffer.limit(linesCounter);
            System.out.println("预读" + inFiles.length + "个文件。总共" + Helper.formatSpace(totalSize) + "。用时"
                    + (System.currentTimeMillis() - start) + " ms。");
            start = System.currentTimeMillis();
            System.out.print("排序总共" + posBuffer.limit() + "行，最长" + maxLen + "字节。平均" + (totalSize / posBuffer.limit())
                    + "字节 。。。");
            cachedBytes1 = ByteBuffer.allocate(maxLen);
            cachedBytes2 = ByteBuffer.allocate(maxLen);
            cachedBytes3 = ByteBuffer.allocate(maxLen);
            for (i = 0; i < cachedValues.length; i++) {
                cachedValues[i] = ByteBuffer.allocate(maxLen);
            }
            sort(posBuffer.array(), 0, posBuffer.limit());
            System.out.print("用时：" + Helper.formatDuration(System.currentTimeMillis() - start) + "，");

            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outFile), Helper.BUFFER_SIZE);
            BufferedOutputStream skippedOut = null;
            if (!skipIrrelevant) {
                if (skippedFile != null) {
                    skippedOut = new BufferedOutputStream(new FileOutputStream(skippedFile), Helper.BUFFER_SIZE);
                } else {
                    skippedOut = out;
                }
            }
            if (CHECK) {
                checkSorted(posBuffer.array(), 0, posBuffer.limit());
            }
            start = System.currentTimeMillis();
            if (DEBUG) {
                System.out.println("写出'" + outFile + "'文件。。。");
            }
            int writtenLines = write(out, skippedOut, posBuffer.array(), 0, posBuffer.limit());
            out.close();
            if (skippedOut != null) {
                skippedOut.close();
            }
            totalSorted = writtenLines;
            System.out.println("写出" + writtenLines + "行。文件：" + outFile + "（"
                    + Helper.formatSpace(new File(outFile).length()) + "）。用时：" + (System.currentTimeMillis() - start)
                    + " ms。");
        } else {
            System.err.println("排序失败。输入文件为空！");
        }
    }

    private long loadFile(int i, String inFile) throws FileNotFoundException, IOException {
        FileChannel fileChannel = new RandomAccessFile(inFile, "r").getChannel();
        long size = fileChannel.size();
        if (size > Integer.MAX_VALUE) {
            System.err.println("文件不能超过2GB：" + inFile);
            return -1L;
        }
        ByteBuffer bb = ByteBuffer.allocate((int) size);
        if (DEBUG) {
            System.out.println("导入文件'" + inFile + "'，文件大小：" + Helper.formatSpace(bb.limit()));
        }
        fileChannel.read(bb);
        bb.rewind();
        fileChannel.close();
        rawBytes[i] = bb;
        return size;
    }

    private final void sort(final int x[], final int off, final int len) throws UnsupportedEncodingException {
        if (DEBUG && TRACE) {
            System.out.println("sort(" + off + ", " + len + ")");
            if (TRACE) {
                System.out.println("in: " + dump(x));
            }
        }
        if (len < 7) {
            // insertion sort
            for (int i = off; i < len + off; i++) {
                for (int j = i; j > off; j--) {
                    read(x, j - 1, cachedBytes1);
                    read(x, j, cachedBytes2);
                    if (ArrayHelper.isSuccessor(cachedBytes1, cachedBytes2)) {
                        swap(x, j, j - 1);
                    } else {
                        break;
                    }
                }
            }
            if (DEBUG && TRACE) {
                System.out.println("io: " + dump(x));
            }
            return;
        }

        // Choose a partition element, v
        int m = off + (len >> 1); // Small arrays, middle element
        if (len > 7) {
            int l = off;
            int n = off + len - 1;
            if (len > 40) { // Big arrays, pseudomedian of 9
                int s = len / 8;
                l = med3(x, l, l + s, l + 2 * s);
                m = med3(x, m - s, m, m + s);
                n = med3(x, n - 2 * s, n - s, n);
            }
            m = med3(x, l, m, n); // Mid-size, med of 3
        }
        read(x, m, cachedBytes3);
        if (DEBUG && TRACE) {
            System.out.println("v(" + m + ")=" + ArrayHelper.toString(cachedBytes3));
        }
        // Establish Invariant: v* (<v)* (>v)* v*
        int a = off, b = a, c = off + len - 1, d = c;
        while (true) {
            while (b <= c) {
                read(x, b, cachedBytes2);
                if (ArrayHelper.isPredessorEquals(cachedBytes2, cachedBytes3)) {
                    if (ArrayHelper.equals(cachedBytes2, cachedBytes3)) {
                        if (DEBUG && TRACE) {
                            System.out.println("swap(a:" + a + ", b:" + b + ")");
                            if (TRACE) {
                                System.out.println(dump(x));
                            }
                        }
                        swap(x, a++, b);
                        if (DEBUG && TRACE) {
                            if (TRACE) {
                                System.out.println(dump(x));
                            }
                        }
                    }
                    b++;
                } else {
                    break;
                }
            }
            while (c >= b) {
                read(x, c, cachedBytes2);
                if (ArrayHelper.isPredessorEquals(cachedBytes3, cachedBytes2)) {
                    if (ArrayHelper.equals(cachedBytes2, cachedBytes3)) {
                        if (DEBUG && TRACE) {
                            System.out.println("swap(c:" + c + ", d:" + d + ")");
                            if (TRACE) {
                                System.out.println(dump(x));
                            }
                        }
                        swap(x, c, d--);
                        if (DEBUG && TRACE) {
                            if (TRACE) {
                                System.out.println(dump(x));
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
            swap(x, b++, c--);
        }

        // Swap partition elements back to middle
        int s, n = off + len;
        s = Math.min(a - off, b - a);
        vecswap(x, off, b - s, s);
        s = Math.min(d - c, n - d - 1);
        vecswap(x, b, n - s, s);

        // Recursively sort non-partition-elements
        if ((s = b - a) > 1) {
            sort(x, off, s);
        }
        if ((s = d - c) > 1) {
            sort(x, n - s, s);
        }
        if (DEBUG && TRACE) {
            System.out.println("so: " + dump(x));
        }
    }

    protected int write(BufferedOutputStream out, BufferedOutputStream skippedOut, int[] sortedPosArray, int offset,
            final int limit) throws IOException {
        int len = -1;
        int lines = 0;
        int skippedDuplicated = 0;
        int skippedNoKeyFound = 0;
        int startEqual = -1;
        byte[] cachedBytes2Array = cachedBytes2.array();
        for (int i = offset; i < limit; i++) {
            len = read(sortedPosArray, i, cachedBytes2);
            if (len == cachedBytes1.limit() && startEqual != -1
                    && ArrayHelper.equals(cachedBytes2Array, 0, cachedBytes1.array(), 0, len)) {
                skippedDuplicated++;
                // System.out.println("continue: " + new String(cachedBytes2.array(), 0, len, Helper.CHARSET_UTF8));
                continue;
            } else {
                // System.out.println("old: " + new String(cachedBytes1.array(), 0, cachedBytes1.limit(),
                // Helper.CHARSET_UTF8));
                // System.out.println("new: " + new String(cachedBytes2.array(), 0, len, Helper.CHARSET_UTF8));
                if (len != -1) {
                    System.arraycopy(cachedBytes2Array, 0, cachedBytes1.array(), 0, len);
                    cachedBytes1.limit(len);
                }
                if (startEqual != -1 && writeRange(out, sortedPosArray, startEqual, i - 1)) {
                    lines++;
                }
                if (len != -1) {
                    startEqual = i;
                } else {
                    if (skippedOut != null) {
                        ByteBuffer bb = getPosBuffer(sortedPosArray, i);
                        if (bb != null) {
                            int l = DictHelper.getNextStopPoint(bb, DictHelper.ORDER_PARTS);
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
            if (writeRange(out, sortedPosArray, startEqual, limit - 1)) {
                lines++;
            }
        }
        System.out.println("跳过" + skippedDuplicated + "重复行，" + skippedNoKeyFound + "垃圾行。");
        return lines;
    }

    protected final boolean writeRange(BufferedOutputStream out, int[] sortedPosArray, int startIdx, int endIdx)
            throws IOException {
        int len;
        if (startIdx != -1 && endIdx >= startIdx) {
            // System.out.println("merge: " + startIdx + ", " + endIdx);
            ByteBuffer bb = ArrayHelper.borrowByteBufferLarge();
            len = readMerged(sortedPosArray, startIdx, endIdx, bb);
            out.write(bb.array(), 0, len);
            out.write(Helper.SEP_NEWLINE_CHAR);
            ArrayHelper.giveBack(bb);
            return true;
        }
        return false;
    }
}
