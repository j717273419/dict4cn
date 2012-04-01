package cn.kk.kkdict.tools;

import java.io.IOException;
import java.nio.ByteBuffer;

import cn.kk.kkdict.types.Language;
import cn.kk.kkdict.utils.DictHelper;
import cn.kk.kkdict.utils.Helper;

public class DictFilesSorter extends WordFilesSorter {
    private final Language sortLng;
    private final byte[] sortLngDefBytes;
    public static final String OUTFILE = "output-words_sort-result.words";
    public final static boolean WRITE_SORT_LNG_DEF_FIRST = true;

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        String inFileTest = "O:\\kkdict\\out\\dicts\\wiki\\test\\output.txt.out";
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
        String outDir = "O:\\kkdict\\out\\dicts\\wiki\\test";
        //
        // new DictFilesSorter(Language.ZH, outFile, false, inFile0, inFile1, inFile2, inFile3, inFile4, inFile5,
        // inFile6,
        // inFile7, inFile8, inFile9).sort();
        // new WordFilesSorter(outFile, inFile1, inFile2).sort();
        new DictFilesSorter(Language.ZH, outDir, false, inFileTest).sort();
    }

    public DictFilesSorter(Language sortLng, String outDir, String outFile, boolean writeIrrelevant, String... inFiles) {
        super(outDir, outFile, writeIrrelevant, inFiles);
        this.sortLng = sortLng;
        this.sortLngDefBytes = (sortLng.key + Helper.SEP_DEFINITION).getBytes(Helper.CHARSET_UTF8);        
    }
    
    public DictFilesSorter(Language sortLng, String outDir, boolean writeIrrelevant, String... inFiles) {
        super(outDir, OUTFILE, writeIrrelevant, inFiles);
        this.sortLng = sortLng;
        this.sortLngDefBytes = (sortLng.key + Helper.SEP_DEFINITION).getBytes(Helper.CHARSET_UTF8);
    }

    @Override
    protected int readMerged(int[] sortedPosArray, int startIdx, int endIdx, ByteBuffer cachedBytes) {
        ByteBuffer mergeBB = cachedBytes;
        if (WRITE_SORT_LNG_DEF_FIRST) {
            mergeBB = cachedBytesBig2;
        }
        mergeBB.limit(mergeBB.capacity());
        // TODO: merge attributes
        int mergePos;
        int stopPoint;
        if (startIdx == endIdx) {
            // only one, no same key doubles
            ByteBuffer bb = getPosBuffer(sortedPosArray, startIdx);
            stopPoint = DictHelper.getStopPoint(bb, DictHelper.ORDER_PARTS);
            System.arraycopy(bb.array(), bb.position(), mergeBB.array(), 0, stopPoint);
            mergePos = stopPoint;
            // System.out.println("single: "
            // + new String(cachedBytesBig2.array(), 0, cachedBytesBig2.limit(), Helper.CHARSET_UTF8));
        } else {
            // more than one with same key
            ByteBuffer bb = getPosBuffer(sortedPosArray, startIdx);
            stopPoint = DictHelper.getStopPoint(bb, DictHelper.ORDER_PARTS);
            System.arraycopy(bb.array(), bb.position(), mergeBB.array(), 0, stopPoint);
            mergeBB.position(stopPoint);
            bb.position(bb.position() + stopPoint);
            mergePos = DictHelper.mergeDefinitionsAndAttributes(mergeBB, bb);
            // System.out.println("multi 2: " + new String(cachedBytesBig2.array(), 0, cacheIdx, Helper.CHARSET_UTF8));
        }
        mergeBB.limit(mergePos);
        mergeBB.rewind();
        if (WRITE_SORT_LNG_DEF_FIRST) {
            int s0 = mergeBB.position();
            int s1 = DictHelper.positionSortLng(mergeBB, sortLngDefBytes, true);
            int s2 = DictHelper.getStopPoint(mergeBB, DictHelper.ORDER_LIST);
            int s3 = DictHelper.getStopPoint(mergeBB, DictHelper.ORDER_PARTS) - s2;
            // copy sort lng part
            System.arraycopy(mergeBB.array(), s1, cachedBytes.array(), 0, s2);
            mergePos = s2;
            // System.out.println("result 1: " + new String(cachedBytes.array(), 0, cacheIdx, Helper.CHARSET_UTF8));
            if (s1 - s0 > 0) {
                // copy part before sort lng
                System.arraycopy(DictHelper.SEP_LIST_BYTES, 0, cachedBytes.array(), mergePos,
                        DictHelper.SEP_LIST_BYTES.length);
                mergePos += DictHelper.SEP_LIST_BYTES.length;
                System.arraycopy(mergeBB.array(), 0, cachedBytes.array(), mergePos, s1
                        - DictHelper.SEP_LIST_BYTES.length);
                mergePos += s1 - DictHelper.SEP_LIST_BYTES.length;
                // System.out.println("result 2 (" + s1 + "): "
                // + new String(cachedBytes.array(), 0, cacheIdx, Helper.CHARSET_UTF8));
                if (s3 > 0) {
                    // copy part after sort lng
                    System.arraycopy(mergeBB.array(), s1 + s2, cachedBytes.array(), mergePos, s3);
                    mergePos += s3;
                    // System.out.println("result 3: " + new String(cachedBytes.array(), 0, cacheIdx,
                    // Helper.CHARSET_UTF8));
                }
            } else {
                // copy part after sort lng
                System.arraycopy(mergeBB.array(), s2, cachedBytes.array(), mergePos, s3);
                mergePos += s3;
                // System.out.println("result 4: " + new String(cachedBytes.array(), 0, cacheIdx, Helper.CHARSET_UTF8));
            }
            cachedBytes.limit(mergePos);
            // System.out.println("result: " + new String(cachedBytes.array(), 0, cacheIdx, Helper.CHARSET_UTF8));
        }
        return mergePos;
    }

    @Override
    protected int read(int[] sortedPosArray, int idx, ByteBuffer cachedBytes) {
        if (USE_CACHE) {
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
        }
        ByteBuffer bb = getPosBuffer(sortedPosArray, idx);
        if (bb != null && DictHelper.positionSortLng(bb, sortLngDefBytes, false) != -1) {
            int len = DictHelper.getStopPoint(bb, DictHelper.ORDER_ATTRIBUTE);
            System.arraycopy(bb.array(), bb.position(), cachedBytes.array(), 0, len);
            if (USE_CACHE) {
                int startPos = sortedPosArray[idx];
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
            // System.out.println(str);
            return len;
        } else {
            cachedBytes.rewind().limit(0);
            return -1;
        }
    }

}
