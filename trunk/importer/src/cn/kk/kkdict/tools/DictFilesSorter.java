package cn.kk.kkdict.tools;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import cn.kk.kkdict.beans.DictByteBufferRow;
import cn.kk.kkdict.types.Language;
import cn.kk.kkdict.utils.ArrayHelper;
import cn.kk.kkdict.utils.DictHelper;
import cn.kk.kkdict.utils.Helper;

/**
 * sorts and merges files together depending on the given definition key TODO test
 */
public class DictFilesSorter extends WordFilesSorter {
    protected final Language sortLng;
    protected final ByteBuffer lngBB;
    private boolean filterAttributes = true;
    public static final String OUTFILE = "output-dict_sort-result.dict";
    public final static boolean WRITE_SORT_LNG_DEF_FIRST = true;
    protected final static boolean DEBUG = false;
    private final DictByteBufferRow mainRow = new DictByteBufferRow();
    private final DictByteBufferRow otherRow = new DictByteBufferRow();

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        // String inFileTest =
        // "O:\\kkdict\\out\\dicts\\wiki\\test\\output.txt.out";
        String inFileTest = Helper.DIR_OUT_DICTS + "\\wiki\\output-dict_categories-merged.wiki";
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
        String outDir = "O:\\kkdict\\out\\dicts\\wiki\\work";
        //
        // new DictFilesSorter(Language.ZH, outFile, false, inFile0, inFile1,
        // inFile2, inFile3, inFile4, inFile5,
        // inFile6,
        // inFile7, inFile8, inFile9).sort();
        // new WordFilesSorter(outFile, inFile1, inFile2).sort();
        new DictFilesSorter(Language.ZH, outDir, true, false, inFileTest).sort();
    }

    public DictFilesSorter(Language sortLng, String outDir, String outFile, boolean skipIrrelevant,
            boolean writeIrrelevant, String... inFiles) {
        super(outDir, outFile, skipIrrelevant, writeIrrelevant, inFiles);
        this.sortLng = sortLng;
        this.lngBB = ByteBuffer.wrap(sortLng.key.getBytes(Helper.CHARSET_UTF8));
    }

    public DictFilesSorter(Language sortLng, String outDir, boolean skipIrrelevant, boolean writeIrrelevant,
            String... inFiles) {
        super(outDir, OUTFILE, skipIrrelevant, writeIrrelevant, inFiles);
        this.sortLng = sortLng;
        this.lngBB = ByteBuffer.wrap(sortLng.key.getBytes(Helper.CHARSET_UTF8));
    }

    @Override
    protected int readMerged(int[] sortedPosArray, int startIdx, int endIdx, ByteBuffer mergeBB) {
        // only one, no same key doubles
        ByteBuffer bb = getPosBuffer(sortedPosArray, startIdx);
        mainRow.parseFrom(bb);
        final int i = mainRow.indexOfLanguage(lngBB);
        mergeBB.put(mainRow.getDefinitionWithAttributes(i));
        final int size = mainRow.size();
        for (int j = 0; j < size; j++) {
            if (i == j) {
                // already inserted
                continue;
            }
            mergeBB.put(Helper.SEP_LIST_BYTES);
            mergeBB.put(mainRow.getDefinitionWithAttributes(j));
        }
        mergeBB.limit(mergeBB.position()).rewind();

        if (startIdx < endIdx) {
            for (int j = startIdx + 1; j <= endIdx; j++) {
                mainRow.parseFrom(mergeBB, true);
                DictHelper.mergeDefinitionsAndAttributes(mainRow, otherRow, mergeBB);
            }
        }
        if (DEBUG) {
            System.out.println("合并后: " + ArrayHelper.toString(mergeBB));
        }
        if (filterAttributes) {
            DictHelper.filterAttributes(mergeBB);
            if (DEBUG) {
                System.out.print("过滤后: " + ArrayHelper.toString(mergeBB));
            }
        }
        return mergeBB.limit();
    }

    @Override
    protected int read(int[] sortedPosArray, int idx, ByteBuffer transferBB) {
        if (USE_CACHE) {
            int startPos = sortedPosArray[idx];
            int j;
            for (int i = 0; i < CACHE_SIZE; i++) {
                j = cachedKeys[i];
                if (j == startPos) {
                    ByteBuffer cached = cachedValues[i];
                    System.arraycopy(cached.array(), 0, transferBB.array(), 0, cached.limit());
                    transferBB.limit(cached.limit());
                    return cached.limit();
                } else if (j == -1) {
                    break;
                }
            }
        }
        ByteBuffer bb = getPosBuffer(sortedPosArray, idx);
        if (bb != null) {
            mainRow.parseFrom(bb);
            int i;
            if (-1 != (i = mainRow.indexOfLanguage(lngBB))) {
                int len = ArrayHelper.copy(mainRow.getValue(i), transferBB);
                if (USE_CACHE) {
                    int startPos = sortedPosArray[idx];
                    if (cachedIdx >= CACHE_SIZE) {
                        cachedIdx = 0;
                    }
                    cachedKeys[cachedIdx] = startPos;
                    ByteBuffer cached = cachedValues[cachedIdx];
                    ArrayHelper.copy(transferBB, cached);
                    cachedIdx++;
                }
                transferBB.rewind();
                if (DEBUG && TRACE) {
                    System.out.println("读出：" + ArrayHelper.toString(transferBB));
                }
                return len;
            }
        }
        transferBB.rewind().limit(0);
        return -1;
    }

    public boolean isFilterAttributes() {
        return filterAttributes;
    }

    public void setFilterAttributes(boolean filterAttributes) {
        this.filterAttributes = filterAttributes;
    }

    @Override
    protected final int write(BufferedOutputStream out, BufferedOutputStream skippedOut, int[] sortedPosArray,
            int offset, final int limit) throws IOException {
        System.out.print("，" + sortLng.key + "，");
        return super.write(out, skippedOut, sortedPosArray, offset, limit);
    }

}
