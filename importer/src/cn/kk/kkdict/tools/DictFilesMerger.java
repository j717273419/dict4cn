package cn.kk.kkdict.tools;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

import cn.kk.kkdict.types.Language;
import cn.kk.kkdict.utils.ArrayHelper;
import cn.kk.kkdict.utils.DictHelper;
import cn.kk.kkdict.utils.Helper;

/**
 * Merge sorted dict files
 * TODO refactoring 
 */
public class DictFilesMerger {
    private static boolean noticed = false;
    private final static boolean DEBUG = false;
    private final String[] inFiles;
    private final String outDir;
    private final Language mergeLng;
    public static final String OUT_FILE = "output-dict_mrg-result.wiki";
    public final String outFile;
    private final byte[] mergeLngDefBytes;
    private final String inFileMain;

    public static void main(String[] args) throws IOException, InterruptedException {
        String[] files = new String[2];
        for (int i = 0; i < files.length; i++) {
            files[i] = Helper.DIR_OUT_DICTS + "\\wiki\\work\\output-dict_xtr-result_ddfes-" + i + ".wiki";
        }
        // DictFilesMerger merger = new DictFilesMerger(Language.ZH, Helper.DIR_OUT_DICTS + "\\wiki\\work",
        // "output-dict_ddfs.wiki", files);
        DictFilesMerger merger = new DictFilesMerger(Language.ZH, Helper.DIR_OUT_DICTS + "\\wiki\\work",
                "output-dict_ddfs-test.wiki", Helper.DIR_OUT_DICTS + "\\wiki\\work\\test1.txt", Helper.DIR_OUT_DICTS
                        + "\\wiki\\work\\test2.txt");
        merger.merge();
    }

    public DictFilesMerger(Language mergeLng, String outDir, String outFile, String... inFiles) {
        if (new File(outDir).isDirectory()) {
            if (!noticed) {
                System.out.println("温馨提示：需合并的文件必须事先排序。");
                noticed = true;
            }
            if (inFiles.length != 2) {
                this.inFiles = null;
                this.inFileMain = null;
                this.outDir = null;
                this.mergeLng = null;
                this.outFile = null;
                this.mergeLngDefBytes = null;
                System.err.println("本程序现在只能合并两个文件！");
            } else {
                String maxFile = null;
                long max = -1;
                List<String> files = new LinkedList<String>();
                for (String f : inFiles) {
                    long l = new File(f).length();
                    if (l > max) {
                        max = l;
                        maxFile = f;
                    }
                    files.add(f);
                }
                files.remove(maxFile);
                this.inFiles = files.toArray(new String[files.size()]);
                this.inFileMain = maxFile;
                this.outDir = outDir;
                this.mergeLng = mergeLng;
                if (outFile != null) {
                    this.outFile = outDir + File.separator + outFile;
                } else {
                    this.outFile = outDir + File.separator + OUT_FILE;
                }
                this.mergeLngDefBytes = (mergeLng.key + Helper.SEP_DEFINITION).getBytes(Helper.CHARSET_UTF8);
            }
        } else {
            this.inFiles = null;
            this.inFileMain = null;
            this.outDir = null;
            this.mergeLng = null;
            this.outFile = null;
            this.mergeLngDefBytes = null;
            System.err.println("文件夹不可读：'" + outDir + "'!");
        }
    }

    public void merge() throws IOException {
        final long start = System.currentTimeMillis();
        System.out.println("合并含有'" + mergeLng.key + "'的词典  。。。" + (inFiles.length + 1));
        File f = new File(inFileMain);
        BufferedInputStream inFilesMainIn;
        if (f.isFile()) {
            if (DEBUG) {
                System.out.println("导入主合并文件'" + f.getAbsolutePath() + "'（" + Helper.formatSpace(f.length()) + "）。。。");
            }
            inFilesMainIn = new BufferedInputStream(new FileInputStream(f), Helper.BUFFER_SIZE);
        } else {
            System.err.println("主合并文件'" + f.getAbsolutePath() + "'不存在！");
            return;
        }

        BufferedInputStream[] inFilesIns = new BufferedInputStream[inFiles.length];
        int i = 0;
        for (String inFile : inFiles) {
            f = new File(inFile);
            if (f.isFile()) {
                if (DEBUG) {
                    System.out.println("导入分文件'" + f.getAbsolutePath() + "'（" + Helper.formatSpace(f.length()) + "）。。。");
                }
                inFilesIns[i] = new BufferedInputStream(new FileInputStream(f), Helper.BUFFER_SIZE);
            } else {
                System.err.println("分文件不可读'" + f.getAbsolutePath() + "'！");
            }
            i++;
        }
        if (DEBUG) {
            System.out.println("创建输出文件'" + outFile + "'。。。");
        }
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outFile), Helper.BUFFER_SIZE);

        merge(out, inFilesMainIn, inFilesIns);

        for (BufferedInputStream in : inFilesIns) {
            if (in != null) {
                in.close();
            }
        }
        inFilesMainIn.close();
        out.close();
        System.out.println("合并成功'" + outFile + "'（" + Helper.formatSpace(new File(outFile).length()) + "）。用时："
                + Helper.formatDuration(System.currentTimeMillis() - start));
    }

    private void merge(BufferedOutputStream out, BufferedInputStream inFilesMainIn, BufferedInputStream[] inFilesIns)
            throws IOException {
        ByteBuffer[] inFileBBs = new ByteBuffer[inFilesIns.length];
        for (int i = 0; i < inFilesIns.length; i++) {
            inFileBBs[i] = ArrayHelper.borrowByteBufferMedium();
            inFileBBs[i].limit(0);
        }
        ByteBuffer mergeBB = ArrayHelper.borrowByteBufferLarge();
        ByteBuffer lineBB = ArrayHelper.borrowByteBufferMedium();
        while (-1 != ArrayHelper.readLine(inFilesMainIn, lineBB)) {
            if (DEBUG) {
                System.out.println("合并词组：" + ArrayHelper.toString(lineBB));
            }
            if (-1 == DictHelper.positionSortLng(lineBB, mergeLngDefBytes, true)) {
                // main file has no more sort key
                out.write(lineBB.array(), 0, lineBB.limit());
                out.write('\n');
                break;
            }
            // copy original line
            if (lineBB.position() != 0) {
                System.err.println("没有前置：" + ArrayHelper.toString(lineBB));
            }
            System.arraycopy(lineBB.array(), lineBB.position(), mergeBB.array(), 0, lineBB.limit() - lineBB.position());
            int mergedPosition = lineBB.limit();

            int i = 0;
            int inBBStopPoint = DictHelper.getStopPoint(lineBB.array(), 0, lineBB.limit(), DictHelper.ORDER_ATTRIBUTE);
            for (BufferedInputStream inFileIn : inFilesIns) {
                if (inFileIn != null) {
                    mergedPosition = mergeInFile(lineBB, mergeBB, inFileBBs, inFileIn, out, i, mergedPosition,
                            inBBStopPoint);
                }
                i++;
            }
            out.write(mergeBB.array(), 0, mergedPosition);
            out.write('\n');
        }
        int len;
        while ((len = inFilesMainIn.read(mergeBB.array())) != -1) {
            out.write(mergeBB.array(), 0, len);
        }
        int i = 0;
        for (BufferedInputStream inFileIn : inFilesIns) {
            if (inFileIn != null) {
                while ((len = inFileIn.read(mergeBB.array())) != -1) {
                    out.write(mergeBB.array(), 0, len);
                }
            }
            ArrayHelper.giveBack(inFileBBs[i]);
            i++;
        }
        ArrayHelper.giveBack(mergeBB);
    }

    private int mergeInFile(ByteBuffer lineBB, ByteBuffer mergeBB, ByteBuffer[] inFileBBs,
            BufferedInputStream inFileIn, BufferedOutputStream out, int inFileIdx, int mergedPosition, int inBBStopPoint)
            throws IOException {
        boolean predessor = false;
        do {
            ByteBuffer inFileBB = inFileBBs[inFileIdx];
            if (inFileBB != null) {
                boolean eof = false;
                if (inFileBB.limit() == 0) {
                    if (-1 != ArrayHelper.readLine(inFileIn, inFileBB)) {
                        if (-1 == DictHelper.positionSortLng(inFileBB, mergeLngDefBytes, true)) {
                            // in file has no more sort key
                            eof = true;
                        }
                    } else {
                        eof = true;
                    }
                }
                if (eof) {
                    if (DEBUG) {
                        System.out.println(inFileIdx + ": end");
                    }
                    inFileBBs[inFileIdx] = null;
                    ArrayHelper.giveBack(inFileBB);
                    predessor = false;
                } else {
                    int inFileBBStopPoint = DictHelper.getStopPoint(inFileBB.array(), inFileBB.position(),
                            inFileBB.limit(), DictHelper.ORDER_ATTRIBUTE);
                    if (inFileBBStopPoint < inFileBB.capacity()) {
                        // stop point okay
                        final int sepDefLen = mergeLngDefBytes.length;
                        final int inFileStartIdx = inFileBB.position() + sepDefLen;
                        final int bbStartIdx = lineBB.position() + sepDefLen;
                        final int bbStopLen = inBBStopPoint - sepDefLen;
                        final int inFileStopLen = inFileBBStopPoint - sepDefLen;
                        if (DEBUG) {
                            System.out.println(inFileIdx + ": cmp "
                                    + ArrayHelper.toString(inFileBB.array(), inFileStartIdx, inFileStopLen) + " <> "
                                    + ArrayHelper.toString(lineBB.array(), bbStartIdx, bbStopLen));
                            System.out.println(inFileIdx + ": cmp "
                                    + ArrayHelper.toHexString(inFileBB.array(), inFileStartIdx, inFileStopLen) + " <> "
                                    + ArrayHelper.toHexString(lineBB.array(), bbStartIdx, bbStopLen));
                        }
                        predessor = ArrayHelper.isPredessorEquals(inFileBB, inFileStartIdx, inFileStopLen, lineBB,
                                bbStartIdx, bbStopLen);

                        if (predessor) {
                            if (ArrayHelper.isEquals(inFileBB, inFileStartIdx, inFileStopLen, lineBB, bbStartIdx,
                                    bbStopLen)) {
                                // merge
                                mergeBB.position(mergedPosition);
                                mergedPosition = DictHelper.mergeOneDefinitionAndAttributes(mergeBB, inFileBB);
                                if (DEBUG) {
                                    System.out.println(inFileIdx + ": merge "
                                            + ArrayHelper.toString(inFileBB.array(), inFileStartIdx, inFileStopLen)
                                            + " == " + ArrayHelper.toString(lineBB.array(), bbStartIdx, bbStopLen));
                                }
                            } else {
                                if (DEBUG) {
                                    System.out.println(inFileIdx + ": skip pre "
                                            + ArrayHelper.toString(inFileBB.array(), inFileStartIdx, inFileStopLen)
                                            + " < " + ArrayHelper.toString(lineBB.array(), bbStartIdx, bbStopLen));
                                }
                                // System.out.println(i + ": skip " + Helper.toHexString(bb.array(), 0,
                                // inBBStopPoint)
                                // + " < " + Helper.toHexString(inFileBB.array(), 0, inFileBBStopPoint));
                                out.write(inFileBB.array(), 0, inFileBB.limit());
                                out.write('\n');
                            }
                            inFileBB.limit(0);
                        } else {
                            if (DEBUG) {
                                System.out.println(inFileIdx + ": skip post "
                                        + ArrayHelper.toString(inFileBB.array(), inFileStartIdx, inFileStopLen) + " > "
                                        + ArrayHelper.toString(lineBB.array(), bbStartIdx, bbStopLen));
                                // System.out.println(inFileIdx + ": skip post "
                                // + ArrayHelper.toHexString(inFileBB.array(), sepDefLen, inFileStopLen) + " > "
                                // + ArrayHelper.toHexString(lineBB.array(), sepDefLen, bbStopLen));
                            }
                        }
                    }
                }
            }
        } while (predessor);
        return mergedPosition;
    }

}
