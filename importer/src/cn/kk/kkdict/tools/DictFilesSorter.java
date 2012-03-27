package cn.kk.kkdict.tools;

import java.io.IOException;
import java.nio.ByteBuffer;

import cn.kk.kkdict.types.Language;
import cn.kk.kkdict.utils.Helper;

public class DictFilesSorter extends WordFilesSorter {
    private final Language sortLng;
    private final byte[] sortLngDefBytes;

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        String inFileTest = "O:\\kkdict\\out\\dicts\\wiki\\test\\output-dict_ar.wiki_ar.out.merged";
        // String inFileTest = "O:\\kkdict\\out\\dicts\\wiki\\test\\test.txt";
         String inFile0 = "O:\\kkdict\\out\\dicts\\wiki\\test\\output-dict_ar.wiki_ar";
         String inFile1 = "O:\\kkdict\\out\\dicts\\wiki\\test\\output-dict_bg.wiki_bg";
         String inFile2 = "O:\\kkdict\\out\\dicts\\wiki\\test\\output-dict_be.wiki_be";
         String inFile3 = "O:\\kkdict\\out\\dicts\\wiki\\test\\output-dict_az.wiki_az";
         String inFile4 = "O:\\kkdict\\out\\dicts\\wiki\\test\\output-dict_bs.wiki_bs";
         String inFile5 = "O:\\kkdict\\out\\dicts\\wiki\\test\\output-dict_br.wiki_br";
         String inFile6 = "O:\\kkdict\\out\\dicts\\wiki\\test\\output-dict_an.wiki_an";
         String inFile7 = "O:\\kkdict\\out\\dicts\\wiki\\test\\output-dict_af.wiki_af";
         String inFile8 = "O:\\kkdict\\out\\dicts\\wiki\\test\\output-dict_bn.wiki_bn";
         String inFile9 = "O:\\kkdict\\out\\dicts\\wiki\\test\\output-dict_ast.wiki_ast";
        // String inFile1 = "D:\\test1.txt";
        // String inFile2 = "D:\\test2.txt";
        // String inFile3 = "D:\\test3.txt";
        String outFile = inFileTest + ".out";

         new DictFilesSorter(Language.ZH, outFile, false, inFile0, inFile1, inFile2, inFile3, inFile4, inFile5, inFile6,
         inFile7, inFile8, inFile9).sort();
        // new WordFilesSorter(outFile, inFile1, inFile2).sort();
//        new DictFilesSorter(Language.ZH, outFile, false, inFileTest).sort();
    }

    public DictFilesSorter(Language sortLng, String outFile, boolean skipIrrelevant, String... inFiles) {
        super(outFile, skipIrrelevant, inFiles);
        this.sortLng = sortLng;
        this.sortLngDefBytes = (sortLng.key + Helper.SEP_DEFINITION).getBytes(Helper.CHARSET_UTF8);
    }

    @Override
    protected int readMerged(int[] sortedPosArray, int startIdx, int endIdx, ByteBuffer cachedBytes,
            ByteBuffer tmpCachedBytes) {
        // TODO: merge attributes
        int cacheIdx;
        int stopPoint;
        if (startIdx == endIdx) {
            ByteBuffer bb = getPosBuffer(sortedPosArray, startIdx);
            stopPoint = getStopPoint(bb, ORDER_PARTS);
            System.arraycopy(bb.array(), bb.position(), cachedBytesBig2.array(), 0, stopPoint);
            cacheIdx = stopPoint;
            // System.out.println("single: "
            // + new String(cachedBytesBig2.array(), 0, cachedBytesBig2.limit(), Helper.CHARSET_UTF8));
        } else {
            ByteBuffer bb = getPosBuffer(sortedPosArray, startIdx);
            stopPoint = getStopPoint(bb, ORDER_PARTS);
            System.arraycopy(bb.array(), bb.position(), cachedBytesBig2.array(), 0, stopPoint);
            cacheIdx = stopPoint;
            int idx;
            int listIdx;
            // System.out.println("multi 1: " + new String(cachedBytesBig2.array(), 0, cacheIdx, Helper.CHARSET_UTF8));
            for (int i = startIdx + 1; i <= endIdx; i++) {
                bb = getPosBuffer(sortedPosArray, i);
                stopPoint = getStopPoint(bb.array(), bb.position(), bb.limit(), ORDER_PARTS);
                do {
                    idx = getStopPoint(bb, ORDER_ATTRIBUTE);
                    listIdx = getStopPoint(bb, ORDER_LIST);
                    System.arraycopy(bb.array(), bb.position(), tmpCachedBytes.array(), 0, idx);
                    if (-1 == Helper.indexOf(cachedBytesBig2.array(), 0, cacheIdx, tmpCachedBytes.array(), 0, idx)) {
                        System.arraycopy(SEP_LIST_BYTES, 0, cachedBytesBig2.array(), cacheIdx, SEP_LIST_BYTES.length);
                        cacheIdx += SEP_LIST_BYTES.length;
                        System.arraycopy(bb.array(), bb.position(), cachedBytesBig2.array(), cacheIdx, listIdx);
                        cacheIdx += listIdx;
                    }
                    bb.position(bb.position() + listIdx);
                    if (bb.remaining() > SEP_LIST_BYTES.length) {
                        // beginning without sep
                        bb.position(bb.position() + SEP_LIST_BYTES.length);
                    }
                } while (bb.position() < stopPoint);
            }
            // System.out.println("multi 2: " + new String(cachedBytesBig2.array(), 0, cacheIdx, Helper.CHARSET_UTF8));
        }
        cachedBytesBig2.limit(cacheIdx);
        cachedBytesBig2.rewind();

        int s0 = cachedBytesBig2.position();
        int s1 = positionSortLng(cachedBytesBig2, true);
        int s2 = getStopPoint(cachedBytesBig2, ORDER_LIST);
        int s3 = getStopPoint(cachedBytesBig2, ORDER_PARTS) - s2;
        // copy sort lng part
        System.arraycopy(cachedBytesBig2.array(), s1, cachedBytes.array(), 0, s2);
        cacheIdx = s2;
        // System.out.println("result 1: " + new String(cachedBytes.array(), 0, cacheIdx, Helper.CHARSET_UTF8));
        if (s1 - s0 > 0) {
            // copy part before sort lng
            System.arraycopy(SEP_LIST_BYTES, 0, cachedBytes.array(), cacheIdx, SEP_LIST_BYTES.length);
            cacheIdx += SEP_LIST_BYTES.length;
            System.arraycopy(cachedBytesBig2.array(), 0, cachedBytes.array(), cacheIdx, s1 - SEP_LIST_BYTES.length);
            cacheIdx += s1 - SEP_LIST_BYTES.length;
            // System.out.println("result 2 (" + s1 + "): "
            // + new String(cachedBytes.array(), 0, cacheIdx, Helper.CHARSET_UTF8));
            if (s3 > 0) {
                // copy part after sort lng
                System.arraycopy(cachedBytesBig2.array(), s1 + s2, cachedBytes.array(), cacheIdx, s3);
                cacheIdx += s3;
                // System.out.println("result 3: " + new String(cachedBytes.array(), 0, cacheIdx, Helper.CHARSET_UTF8));
            }
        } else {
            // copy part after sort lng
            System.arraycopy(cachedBytesBig2.array(), s2, cachedBytes.array(), cacheIdx, s3);
            cacheIdx += s3;
            // System.out.println("result 4: " + new String(cachedBytes.array(), 0, cacheIdx, Helper.CHARSET_UTF8));
        }
        cachedBytes.limit(cacheIdx);
        // System.out.println("result: " + new String(cachedBytes.array(), 0, cacheIdx, Helper.CHARSET_UTF8));
        return cacheIdx;
    }

    @Override
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
        if (bb != null && positionSortLng(bb, false) != -1) {
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
     * @param includeDef
     * @return absolute position index
     */
    private int positionSortLng(ByteBuffer bb, boolean includeDef) {
        int endIdx = getStopPoint(bb, ORDER_PARTS);
        int lngIdx = bb.position() - 1;
        byte c;
        do {
            c = -1;
            lngIdx = Helper.indexOf(bb.array(), lngIdx + 1, endIdx, sortLngDefBytes);
            if (lngIdx > 0) {
                c = bb.array()[lngIdx - 1];
            }
        } while (c >= 'a' && c <= 'z');
        if (lngIdx == -1) {
            bb.position(endIdx);
            return -1;
        } else {
            if (includeDef) {
                bb.position(lngIdx);
            } else {
                bb.position(lngIdx + sortLngDefBytes.length);
            }
            return bb.position();
        }
    }
}
