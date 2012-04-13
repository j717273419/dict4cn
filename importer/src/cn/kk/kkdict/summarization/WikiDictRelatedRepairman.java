package cn.kk.kkdict.summarization;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import cn.kk.kkdict.beans.IndexedByteArray;
import cn.kk.kkdict.extraction.dict.WikiPagesMetaCurrentExtractor;
import cn.kk.kkdict.types.Language;
import cn.kk.kkdict.types.Score;
import cn.kk.kkdict.utils.ArrayHelper;
import cn.kk.kkdict.utils.DictHelper;
import cn.kk.kkdict.utils.Helper;

public class WikiDictRelatedRepairman {
    public static final String IN_DIR = WikiPagesMetaCurrentExtractor.OUT_DIR;
    public static final String OUT_DIR = WikiPagesMetaCurrentExtractor.OUT_DIR + File.separator + "output";
    public static final String SUFFIX_WEIGHTED = "_repaired";
    private static final boolean DEBUG = false;
    private static final boolean TRACE = false;
    private static boolean writeAttributes = true;
    private static final byte[] SCORE_BYTES_ZERO = String.valueOf(0).getBytes(Helper.CHARSET_UTF8);

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        new File(OUT_DIR).mkdirs();
        File inDirFile = new File(IN_DIR);
        if (inDirFile.isDirectory()) {

            System.out.print("修复wiki关联文件'" + IN_DIR + "' ... ");
            File[] files = inDirFile.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.startsWith("output-dict_related.");
                }
            });
            System.out.println(files.length);

            long start = System.currentTimeMillis();
            String[] filePaths = Helper.getFileNames(files);
            ByteBuffer lineBB = ArrayHelper.borrowByteBufferVeryLarge();
            byte[] lineArray = lineBB.array();
            HashMap<IndexedByteArray, HashSet<Integer>> defDict = new HashMap<IndexedByteArray, HashSet<Integer>>();
            // map to itself
            HashMap<IndexedByteArray, IndexedByteArray> defIdent = new HashMap<IndexedByteArray, IndexedByteArray>();
            List<IndexedByteArray> defList = new ArrayList<IndexedByteArray>();
            int idx;
            HashSet<Integer> relatives = null;
            IndexedByteArray defRel;
            IndexedByteArray tester = new IndexedByteArray();
            for (String f : filePaths) {
                String outFile = OUT_DIR + File.separator
                        + Helper.appendFileName(new File(f).getName(), SUFFIX_WEIGHTED);
                Language lng = DictHelper.getWikiLanguage(f);
                if (lng != null) {
                    defDict.clear();
                    defIdent.clear();
                    defList.clear();
                    long startFile = System.currentTimeMillis();

                    BufferedInputStream in = new BufferedInputStream(new FileInputStream(f), Helper.BUFFER_SIZE);
                    while (-1 != ArrayHelper.readLine(in, lineBB)) {
                        if (-1 != (idx = ArrayHelper.indexOf(lineArray, 0, lineBB.limit(), Helper.SEP_DEFINITION_BYTES))) {
                            byte[] def = ArrayHelper.toBytes(lineBB, idx);
                            if (DEBUG) {
                                System.out.print("定义：" + ArrayHelper.toString(def));
                            }

                            tester.setData(def);
                            relatives = defDict.get(tester);
                            int currentId;
                            if (relatives == null) {
                                currentId = defList.size();
                                relatives = putDefinition(defDict, defIdent, defList, def);
                            } else {
                                IndexedByteArray currentDef = defIdent.get(tester);
                                currentId = currentDef.getIdx();
                            }
                            lineBB.position(idx + Helper.SEP_DEFINITION_BYTES.length);

                            byte[] rel;
                            while (null != (rel = DictHelper.findNextWord(lineBB))) {
                                if (DEBUG && TRACE) {
                                    System.out.print("，" + ArrayHelper.toString(rel));
                                }
                                tester.setData(rel);
                                defRel = defIdent.get(tester);
                                if (defRel == null) {
                                    relatives.add(Integer.valueOf(defList.size()));
                                    HashSet<Integer> defRelRelatives = putDefinition(defDict, defIdent, defList, rel);
                                    defRelRelatives.add(Integer.valueOf(currentId));
                                } else {
                                    relatives.add(Integer.valueOf(defRel.getIdx()));
                                    HashSet<Integer> defRelRelatives = defDict.get(defRel);
                                    defRelRelatives.add(Integer.valueOf(currentId));
                                }
                            }
                        }
                        if (DEBUG) {
                            if (relatives != null) {
                                System.out.println("，" + relatives.size());
                            }
                        }
                    }
                    BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outFile),
                            Helper.BUFFER_SIZE);

                    Comparator<IndexedByteArray> weightComparator = new Comparator<IndexedByteArray>() {
                        @Override
                        public int compare(IndexedByteArray o1, IndexedByteArray o2) {
                            return o2.getWeight() - o1.getWeight();
                        }
                    };
                    List<IndexedByteArray> sortedDefs = new LinkedList<IndexedByteArray>(defDict.keySet());
                    for (IndexedByteArray def : sortedDefs) {
                        def.setWeight(defDict.get(def).size());
                    }
                    Collections.sort(sortedDefs, weightComparator);
                    List<IndexedByteArray> sortedRels = new LinkedList<IndexedByteArray>();
                    for (IndexedByteArray def : sortedDefs) {
                        sortedRels.clear();
                        HashSet<Integer> rels = defDict.get(def);
                        out.write(def.getData());
                        out.write(Helper.SEP_DEFINITION_BYTES);

                        for (Integer relId : rels) {
                            IndexedByteArray rel = defList.get(relId.intValue());
                            sortedRels.add(rel);
                        }
                        Collections.sort(sortedRels, weightComparator);
                        boolean first = true;
                        for (IndexedByteArray rel : sortedRels) {
                            if (first) {
                                first = false;
                            } else {
                                out.write(Helper.SEP_WORDS_BYTES);
                            }
                            out.write(rel.getData());
                            if (writeAttributes) {
                                out.write(Helper.SEP_ATTRS_BYTES);
                                out.write(Score.TYPE_ID_BYTES);
                                int score = rel.getWeight();
                                if (score == 0) {
                                    out.write(SCORE_BYTES_ZERO);
                                } else {
                                    byte[] scoreBytes = String.valueOf(score).getBytes(Helper.CHARSET_UTF8);
                                    out.write(scoreBytes);
                                }
                            }
                        }
                        out.write('\n');
                        if (DEBUG) {
                            System.out.println("写入定义：" + ArrayHelper.toString(def.getData()) + "，" + rels.size());
                        }
                    }
                    out.close();
                    System.out.println("完成'" + outFile + "'（" + Helper.formatSpace(new File(outFile).length())
                            + "），用时：" + Helper.formatDuration(System.currentTimeMillis() - startFile));
                }
            }
            ArrayHelper.giveBack(lineBB);
            System.out.println("修复关联文件总共用时：" + Helper.formatDuration(System.currentTimeMillis() - start));
        }
    }

    private static HashSet<Integer> putDefinition(HashMap<IndexedByteArray, HashSet<Integer>> defDict,
            HashMap<IndexedByteArray, IndexedByteArray> defIdent, List<IndexedByteArray> defList, byte[] def) {
        int id = defList.size();
        if (DEBUG && TRACE) {
            System.out.println(id + "。新：" + ArrayHelper.toString(def));
        }
        IndexedByteArray defObj = new IndexedByteArray(id, def);
        HashSet<Integer> relatives = new HashSet<Integer>();
        defDict.put(defObj, relatives);
        defIdent.put(defObj, defObj);
        defList.add(defObj);
        return relatives;
    }
}
