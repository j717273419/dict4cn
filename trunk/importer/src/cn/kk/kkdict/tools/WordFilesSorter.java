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

import cn.kk.kkdict.utils.Helper;

public class WordFilesSorter {
    protected final static int CACHE_SIZE = 10;
    protected final String[] inFiles;
    private final String skippedFile;
    protected final int[] inFilesStartPos;
    protected final int[] inFilesLengths;
    protected final ByteBuffer[] rawBytes;
    protected final String outFile;
    protected final int[] cachedKeys = new int[CACHE_SIZE];
    protected final ByteBuffer[] cachedValues = new ByteBuffer[CACHE_SIZE];
    protected ByteBuffer cachedBytes1;
    protected ByteBuffer cachedBytes2;
    protected ByteBuffer cachedBytesBig1;
    protected ByteBuffer cachedBytesBig2;
    protected int cachedIdx = 0;
    protected int totalSorted;
    protected static final byte[] SEP_NEWLINE_BYTES = Helper.SEP_NEWLINE.getBytes(Helper.CHARSET_UTF8);
    protected static final byte[] SEP_PARTS_BYTES = Helper.SEP_PARTS.getBytes(Helper.CHARSET_UTF8);
    protected static final byte[] SEP_LIST_BYTES = Helper.SEP_LIST.getBytes(Helper.CHARSET_UTF8);
    protected static final byte[] SEP_ATTRS_BYTES = Helper.SEP_ATTRIBUTE.getBytes(Helper.CHARSET_UTF8);

    protected static final int ORDER_NEWLINE = 1;
    protected static final int ORDER_PARTS = 2;
    protected static final int ORDER_LIST = 3;
    protected static final int ORDER_ATTRIBUTE = 4;

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        String inFile0 = Helper.DIR_OUT_WORDS + "/output-words.test";
        String inFile1 = Helper.DIR_OUT_WORDS + "/output-words.baidu_bcd";
        String inFile2 = Helper.DIR_OUT_WORDS + "/output-words.qq_qpyd";
        String inFile3 = Helper.DIR_OUT_WORDS + "/output-words.sogou_scel";
        String outFile = Helper.DIR_OUT_WORDS + "/output-words-sorted.words";
        // String inFile1 = "D:\\test1.txt";
        // String inFile2 = "D:\\test2.txt";
        // String inFile3 = "D:\\test3.txt";
        // String outFile = "D:\\test_sorted.txt";

        new WordFilesSorter(outFile, false, inFile0).sort();
        // new WordFilesSorter(outFile, inFile1, inFile2).sort();
    }

    public WordFilesSorter(String outFile, boolean skipIrrelevant, String... inFiles) {
        this.inFiles = inFiles;
        this.outFile = outFile;
        if (skipIrrelevant) {
            this.skippedFile = Helper.appendFileName(outFile, "_skipped");
        } else {
            this.skippedFile = null;
        }
        this.inFilesLengths = new int[inFiles.length];
        this.inFilesStartPos = new int[inFiles.length + 1];
        this.rawBytes = new ByteBuffer[inFiles.length];
        Arrays.fill(this.cachedKeys, -1);
    }

    private static final int compareTo(byte[] bs1, int len1, byte[] bs2, int len2) {
        int n = Math.min(len1, len2);
        int k = 0;
        while (k < n) {
            byte c1 = bs1[k];
            byte c2 = bs2[k];
            if (c1 != c2) {
                return c1 - c2;
            }
            k++;
        }
        return len1 - len2;
    }

    private boolean isEquals(final int[] sortedPosArray, final int i, final int j) {
        int l1 = read(sortedPosArray, i, cachedBytes1);
        int l2 = read(sortedPosArray, j, cachedBytes2);
        // return v1.equalsIgnoreCase(v2);
        if (l1 == l2) {
            while (l1-- != 0) {
                if (cachedBytes1.get(l1) != cachedBytes2.get(l1)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * 
     * @param array
     * @param start
     * @param limit
     * @param innerstSep
     * @return absolute idx of stop separator
     */
    protected static final int getStopPoint(byte[] array, final int start, final int limit, final int innerstSep) {
        byte b;
        int l = 0;
        for (int i = start; i < limit;) {
            b = array[i++];
            if (b == '\n') {
                break;
            } else if (l != 0 && b == SEP_PARTS_BYTES[0] && i + 1 < limit) {
                b = array[i++];
                if (innerstSep >= ORDER_ATTRIBUTE && b == SEP_ATTRS_BYTES[1] && l + 1 < limit) {
                    b = array[i++];
                    if (b == SEP_ATTRS_BYTES[2]) {
                        break;
                    } else {
                        l += 3;
                        continue;
                    }
                } else if (innerstSep >= ORDER_LIST && b == SEP_LIST_BYTES[1] && i + 1 < limit) {
                    b = array[i++];
                    if (b == SEP_LIST_BYTES[2]) {
                        break;
                    } else {
                        l += 3;
                        continue;
                    }
                } else if (innerstSep >= ORDER_PARTS && b == SEP_PARTS_BYTES[1] && i + 1 < limit) {
                    b = array[i++];
                    if (b == SEP_PARTS_BYTES[2]) {
                        break;
                    } else {
                        l += 3;
                        continue;
                    }
                } else {
                    l += 2;
                    continue;
                }
            } else {
                l++;
                continue;
            }
        }
        return start + l;
    }

    private boolean isPredessorEquals(final int[] sortedPosArray, final int i, final int j) {
        int l1 = read(sortedPosArray, i, cachedBytes1);
        int l2 = read(sortedPosArray, j, cachedBytes2);
        // return v1.compareToIgnoreCase(v2) <= 0;
        if (l1 == -1) {
            return false;
        } else if (l2 == -1) {
            return true;
        }
        return compareTo(cachedBytes1.array(), l1, cachedBytes2.array(), l2) <= 0;
    }

    private boolean isSuccessor(final int[] sortedPosArray, final int i, final int j) {
        int l1 = read(sortedPosArray, i, cachedBytes1);
        int l2 = read(sortedPosArray, j, cachedBytes2);
        // return v1.compareToIgnoreCase(v2) > 0;
        if (l1 == -1) {
            return true;
        } else if (l2 == -1) {
            return false;
        }
        return compareTo(cachedBytes1.array(), l1, cachedBytes2.array(), l2) > 0;
    }

    public void sort() throws IOException {
        long start = System.currentTimeMillis();
        FileChannel[] fChannels = new FileChannel[inFiles.length];
        int i = 0;
        long totalSize = 0;
        for (String inFile : inFiles) {
            FileChannel fileChannel = new RandomAccessFile(inFile, "r").getChannel();
            ByteBuffer bb = ByteBuffer.allocate((int) fileChannel.size());
            System.out.println("导入文件'" + inFile + "'，文件大小：" + Helper.formatSpace(bb.limit()));
            fileChannel.read(bb);
            bb.rewind();
            fChannels[i] = fileChannel;
            rawBytes[i++] = bb;
            totalSize += fileChannel.size();
        }
        if (totalSize > Integer.MAX_VALUE) {
            throw new RuntimeException("文件太大。请使用其他排序算法。如mergesort。");
        }
        IntBuffer posBuffer = IntBuffer.allocate(Math.max(100, (int) (totalSize / 80)));
        byte b = '\n';
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
                if ('\n' == b) {
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
            if (bb.limit() > 0 && b != '\n') {
                linesCounter++;
                p = lastPos + bb.position();
                maxLen = Math.max(p - tmp, maxLen);
            }
            lastPos += bb.limit();
            inFilesLengths[i] = linesCounter;
            inFilesStartPos[i + 1] = lastPos;
            i++;
        }
        posBuffer.limit(linesCounter);
        System.out.println("预读" + inFiles.length + "个文件。总共" + Helper.formatSpace(totalSize) + "。用时"
                + (System.currentTimeMillis() - start) + "ms。");
        start = System.currentTimeMillis();
        System.out.println("排序总共" + posBuffer.limit() + "行，最长" + maxLen + "字节。平均" + (totalSize / posBuffer.limit())
                + "字节。。。");
        cachedBytes1 = ByteBuffer.allocate(maxLen);
        cachedBytes2 = ByteBuffer.allocate(maxLen);
        cachedBytesBig1 = ByteBuffer.allocate(maxLen * 10);
        cachedBytesBig2 = ByteBuffer.allocate(maxLen * 10);
        for (i = 0; i < cachedValues.length; i++) {
            cachedValues[i] = ByteBuffer.allocate(maxLen);
        }
        sort(posBuffer.array(), 0, posBuffer.limit());
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outFile), Helper.BUFFER_SIZE);
        BufferedOutputStream skippedOut;
        if (skippedFile != null) {
            skippedOut = new BufferedOutputStream(new FileOutputStream(skippedFile), Helper.BUFFER_SIZE);
        } else {
            skippedOut = out;
        }
        System.out.println("排序用时：" + (System.currentTimeMillis() - start) + "ms");
        start = System.currentTimeMillis();
        System.out.println("写出'" + outFile + "'文件。。。");
        int writtenLines = write(out, skippedOut, posBuffer.array(), 0, posBuffer.limit());
        out.close();
        if (skippedFile != null) {
            skippedOut.close();
        }
        for (FileChannel channel : fChannels) {
            channel.close();
        }
        totalSorted = writtenLines;
        System.out.println("写出" + writtenLines + "行。文件大小：" + Helper.formatSpace(new File(outFile).length()) + "。用时："
                + (System.currentTimeMillis() - start) + "ms。");
    }

    protected int write(BufferedOutputStream out, BufferedOutputStream skippedOut, int[] sortedPosArray, int offset,
            final int limit) throws IOException {
        int len = -1;
        int lines = 0;
        int skippedEquals = 0;
        int skippedIrrelevant = 0;
        int startEqual = -1;
        for (int i = offset; i < limit; i++) {
            len = read(sortedPosArray, i, cachedBytes2);
            if (len == cachedBytes1.limit() && startEqual != -1
                    && Helper.equals(cachedBytes2.array(), 0, cachedBytes1.array(), 0, len)) {
                skippedEquals++;
                // System.out.println("continue: " + new String(cachedBytes2.array(), 0, len, Helper.CHARSET_UTF8));
                continue;
            } else {
                // System.out.println("old: " + new String(cachedBytes1.array(), 0, cachedBytes1.limit(), Helper.CHARSET_UTF8));
                // System.out.println("new: " + new String(cachedBytes2.array(), 0, len, Helper.CHARSET_UTF8));
                if (len != -1) {
                    System.arraycopy(cachedBytes2.array(), 0, cachedBytes1.array(), 0, len);
                    cachedBytes1.limit(len);
                }
                if (startEqual != -1 && writeRange(out, sortedPosArray, startEqual, i - 1)) {
                    lines++;
                }
                if (len != -1) {
                    startEqual = i;
                } else {
                    ByteBuffer bb = getPosBuffer(sortedPosArray, i);
                    if (bb != null) {
                        int l = getStopPoint(bb, ORDER_PARTS);
                        skippedOut.write(bb.array(), bb.position(), l);
                        skippedOut.write('\n');
                    }
                    skippedIrrelevant++;
                    startEqual = -1;
                }
            }
        }
        if (startEqual != -1) {
            if (writeRange(out, sortedPosArray, startEqual, limit - 1)) {
                lines++;
            }
        }
        System.out.println("跳过" + skippedEquals + "重复行。");
        System.out.println("跳过" + skippedIrrelevant + "垃圾行。");
        return lines;
    }

    protected final boolean writeRange(BufferedOutputStream out, int[] sortedPosArray, int startIdx, int endIdx)
            throws IOException {
        int len;
        if (startIdx != -1 && endIdx >= startIdx) {
            // System.out.println("merge: " + startIdx + ", " + endIdx);
            len = readMerged(sortedPosArray, startIdx, endIdx, cachedBytesBig1, cachedBytes2);
            out.write(cachedBytesBig1.array(), 0, len);
            out.write('\n');
            return true;
        }
        return false;
    }

    protected int readMerged(int[] sortedPosArray, int startIdx, int endIdx, ByteBuffer cachedBytes,
            ByteBuffer tmpCachedBytes) {
        int cacheIdx = 0;
        int stopPoint;
        int attrPoint = 0;
        if (startIdx == endIdx) {
            ByteBuffer bb = getPosBuffer(sortedPosArray, startIdx);
            stopPoint = getStopPoint(bb, ORDER_PARTS);
            System.arraycopy(bb.array(), bb.position(), cachedBytes.array(), cacheIdx, stopPoint);
            cacheIdx = stopPoint;
        } else {
            boolean first = true;
            for (int i = startIdx; i <= endIdx; i++) {
                ByteBuffer bb = getPosBuffer(sortedPosArray, i);
                stopPoint = getStopPoint(bb, ORDER_PARTS);
                if (attrPoint == 0) {
                    attrPoint = getStopPoint(bb, ORDER_ATTRIBUTE);
                }
                if (first) {
                    System.arraycopy(bb.array(), bb.position(), cachedBytes.array(), cacheIdx, attrPoint);
                    cacheIdx = attrPoint;
                }
                if (stopPoint > attrPoint) {
                    int idx = attrPoint;
                    bb.position(bb.position() + idx);
                    int rest = stopPoint - attrPoint;
                    while (0 < (rest -= (idx = getStopPoint(bb, ORDER_ATTRIBUTE)))) {
                        cacheIdx = copyAttribute(cachedBytes, tmpCachedBytes, cacheIdx, attrPoint, bb, idx);
                        bb.position(bb.position() + idx);
                    }
                    cacheIdx = copyAttribute(cachedBytes, tmpCachedBytes, cacheIdx, attrPoint, bb, idx);
                }
                first = false;
            }
        }
        cachedBytes.limit(cacheIdx);
        return cacheIdx;
    }

    protected int copyAttribute(ByteBuffer cachedBytes, ByteBuffer tmpCachedBytes, int cacheIdx, int attrPoint,
            ByteBuffer bb, int idx) {
        System.arraycopy(bb.array(), bb.position(), tmpCachedBytes.array(), 0, idx);
        if (-1 == Helper.indexOf(cachedBytes.array(), attrPoint, cacheIdx - attrPoint, tmpCachedBytes.array(), 0, idx)) {
            System.arraycopy(bb.array(), bb.position(), cachedBytes.array(), cacheIdx, idx);
            cacheIdx += idx;
        }
        return cacheIdx;
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

    private void sort(int x[], int off, int len) throws UnsupportedEncodingException {
        // Insertion sort on smallest arrays
        if (len < CACHE_SIZE) {
            for (int i = off; i < len + off; i++) {
                for (int j = i; j > off && isSuccessor(x, j - 1, j); j--) {
                    swap(x, j, j - 1);
                }
            }
            return;
        }

        // Choose a partition element, v
        int m = off + (len >> 1); // Small arrays, middle element
        if (len > CACHE_SIZE) {
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

        // Establish Invariant: v* (<v)* (>v)* v*
        int a = off, b = a, c = off + len - 1, d = c;
        while (true) {
            while (b <= c && isPredessorEquals(x, b, m)) {
                if (isEquals(x, b, m)) {
                    swap(x, a++, b);
                }
                b++;
            }
            while (c >= b && isPredessorEquals(x, m, c)) {
                if (isEquals(x, c, m)) {
                    swap(x, c, d--);
                }
                c--;
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
    }

    protected int read(int[] sortedPosArray, int idx, ByteBuffer cachedBytes) {
        int startPos = sortedPosArray[idx];
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
        ByteBuffer bb = getPosBuffer(sortedPosArray, idx);
        if (bb != null) {
            int len = getStopPoint(bb, ORDER_ATTRIBUTE);
            System.arraycopy(bb.array(), bb.position(), cachedBytes.array(), 0, len);
            if (cachedIdx >= CACHE_SIZE) {
                cachedIdx = 0;
            }
            cachedKeys[cachedIdx] = startPos;
            ByteBuffer cached = cachedValues[cachedIdx];
            System.arraycopy(cachedBytes.array(), 0, cached.array(), 0, len);
            cached.limit(len);
            cachedBytes.limit(len);
            cachedIdx++;
            // System.out.println(str);
            return len;
        } else {
            return -1;
        }
    }

    /**
     * 
     * @param bb
     * @param innerstSep
     * @return relative length to stop separator
     */
    protected static final int getStopPoint(ByteBuffer bb, int innerstSep) {
        int start = bb.position();
        return getStopPoint(bb.array(), start, bb.limit(), innerstSep) - start;
    }

    protected final ByteBuffer getPosBuffer(int[] sortedPosArray, int idx) {
        int startPosition = sortedPosArray[idx];
        int inFileIdx = getInFileIdxByPos(startPosition);
        if (inFileIdx < inFiles.length) {
            ByteBuffer bb = rawBytes[inFileIdx];
            int lastBytesLimit = inFilesStartPos[inFileIdx];
            int start = startPosition - lastBytesLimit;
            bb.position(start);
            return bb;
        } else {
            return null;
        }
    }

    private static final void swap(int[] sortedPosArray, int a, int b) {
        int t = sortedPosArray[a];
        sortedPosArray[a] = sortedPosArray[b];
        sortedPosArray[b] = t;
    }

    private static final void vecswap(int[] sortedPosArray, int a, int b, int n) {
        for (int i = 0; i < n; i++, a++, b++)
            swap(sortedPosArray, a, b);
    }

    private int med3(int[] sortedPosArray, int a, int b, int c) throws UnsupportedEncodingException {
        return (isSuccessor(sortedPosArray, b, a) ? (isSuccessor(sortedPosArray, c, b) ? b : isSuccessor(
                sortedPosArray, c, a) ? c : a) : (isSuccessor(sortedPosArray, b, c) ? b : isSuccessor(sortedPosArray,
                a, c) ? c : a));
    }

    public int getTotalSorted() {
        return totalSorted;
    }
}
