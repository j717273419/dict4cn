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
import java.io.IOException;
import java.nio.ByteBuffer;

import cn.kk.kkdict.Configuration;
import cn.kk.kkdict.Configuration.Source;
import cn.kk.kkdict.beans.DictByteBufferRow;
import cn.kk.kkdict.types.Language;
import cn.kk.kkdict.utils.ArrayHelper;
import cn.kk.kkdict.utils.DictHelper;
import cn.kk.kkdict.utils.Helper;

/**
 * 针对指定的语言排序词典文件。如同个语言含有多个单词，最小的单词将被用来排序。如果多个文件中含有相同的排序单词，含有这个单词的行将被合并。 通过filterAttributes可以在写出时过滤属性数据。
 * 如skipIrrelevant为true，没有指定语言的行将被过滤。 如writeIrrelevantFiles为true，过滤的行讲单独写入输入文件。 TODO test
 */
public class DictFilesMergedSorter extends WordFilesMergedSorter {
    protected final Language sortLng;
    protected final ByteBuffer lngBB;
    private boolean filterAttributes = false;
    public static final String OUTFILE = "output-dict_sort-result.dict";
    private final DictByteBufferRow mainRow = new DictByteBufferRow();
    private final DictByteBufferRow otherRow = new DictByteBufferRow();

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        // String inFileTest =
        // "O:\\kkdict\\out\\dicts\\wiki\\test\\output.txt.out";
        // String inFileTest = Configuration.IMPORTER_FOLDER_SUMMARIZED_DICTS.getFile(Source.DICT_WIKIPEDIA,
        // "output-dict_categories-merged.wiki");
        String inFileTest = Configuration.IMPORTER_FOLDER_EXTRACTED_DICTS.getFile(Source.DICT_EDICT,
                "output-dict_zh_de.edict_hande");
        // String inFile0 = "O:\\kkdict\\out\\dicts\\wiki\\test\\output-dict_ar.wiki_ar";
        // String inFile1 = "O:\\kkdict\\out\\dicts\\wiki\\test\\output-dict_bg.wiki_bg";
        // String inFile2 = "O:\\kkdict\\out\\dicts\\wiki\\test\\output-dict_be.wiki_be";
        // String inFile3 = "O:\\kkdict\\out\\dicts\\wiki\\test\\output-dict_az.wiki_az";
        // String inFile4 = "O:\\kkdict\\out\\dicts\\wiki\\test\\output-dict_bs.wiki_bs";
        // String inFile5 = "O:\\kkdict\\out\\dicts\\wiki\\test\\output-dict_br.wiki_br";
        // String inFile6 = "O:\\kkdict\\out\\dicts\\wiki\\test\\output-dict_an.wiki_an";
        // String inFile7 = "O:\\kkdict\\out\\dicts\\wiki\\test\\output-dict_af.wiki_af";
        // String inFile8 = "O:\\kkdict\\out\\dicts\\wiki\\test\\output-dict_bn.wiki_bn";
        // String inFile9 = "O:\\kkdict\\out\\dicts\\wiki\\test\\output-dict_ast.wiki_ast";
        // String inFile1 = "D:\\test1.txt";
        // String inFile2 = "D:\\test2.txt";
        // String inFile3 = "D:\\test3.txt";
        String outDir = Configuration.IMPORTER_FOLDER_MERGED_DICTS.getPath(Source.DICT_EDICT);

        // new DictFilesSorter(Language.ZH, outFile, false, inFile0, inFile1,
        // inFile2, inFile3, inFile4, inFile5,
        // inFile6,
        // inFile7, inFile8, inFile9).sort();
        // new WordFilesSorter(outFile, inFile1, inFile2).sort();
        new DictFilesMergedSorter(Language.ZH, outDir, true, false, inFileTest).sort();
    }

    public DictFilesMergedSorter(Language sortLng, String outDir, String outFile, boolean skipIrrelevant,
            boolean writeIrrelevant, String... inFiles) {
        super(outDir, outFile, skipIrrelevant, writeIrrelevant, inFiles);
        this.sortLng = sortLng;
        this.lngBB = ByteBuffer.wrap(sortLng.keyBytes);
    }

    public DictFilesMergedSorter(Language sortLng, String outDir, boolean skipIrrelevant, boolean writeIrrelevantFiles,
            String... inFiles) {
        super(outDir, OUTFILE, skipIrrelevant, writeIrrelevantFiles, inFiles);
        this.sortLng = sortLng;
        this.lngBB = ByteBuffer.wrap(sortLng.keyBytes);
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
                System.out.println(j + "前: " + ArrayHelper.toStringP(mergeBB));
                otherRow.parseFrom(getPosBuffer(sortedPosArray, j));
                mainRow.parseFrom(mergeBB, true);
                DictHelper.mergeDefinitionsAndAttributes(mainRow, otherRow, mergeBB);
                System.out.println(j + "后: " + ArrayHelper.toStringP(mergeBB));
            }
        }
        if (DEBUG) {
            System.out.println("从" + startIdx + "到" + endIdx + "合并后: " + ArrayHelper.toString(mergeBB));
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
    protected int read(int[] sortedPosArray, int fileIdx, ByteBuffer transferBB) {
        if (USE_CACHE) {
            int startPos = sortedPosArray[fileIdx];
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
        ByteBuffer bb = getPosBuffer(sortedPosArray, fileIdx);
        if (bb != null) {
            mainRow.parseFrom(bb).sortValues();
            // mainRow.debug(0);
            int defIdx;
            if (-1 != (defIdx = mainRow.indexOfLanguage(lngBB))) {
                transferBB.clear();
                int len = ArrayHelper.copyP(mainRow.getFirstValue(defIdx), transferBB);
                if (USE_CACHE) {
                    int startPos = sortedPosArray[fileIdx];
                    if (cachedIdx >= CACHE_SIZE) {
                        cachedIdx = 0;
                    }
                    cachedKeys[cachedIdx] = startPos;
                    ByteBuffer cached = cachedValues[cachedIdx];
                    ArrayHelper.copy(transferBB, cached);
                    cachedIdx++;
                }
                if (DEBUG && TRACE) {
                    System.out.println("读出：" + ArrayHelper.toString(transferBB));
                }
                return len;
            }
        }
        transferBB.limit(0);
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
