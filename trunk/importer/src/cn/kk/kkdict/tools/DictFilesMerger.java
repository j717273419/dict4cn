package cn.kk.kkdict.tools;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import cn.kk.kkdict.types.Language;
import cn.kk.kkdict.utils.DictHelper;
import cn.kk.kkdict.utils.Helper;

public class DictFilesMerger {
    private final static boolean DEBUG = false;
    private final String[] inFiles;
    private final String outDir;
    private final Language mergeLng;
    public static final String SUFFIX_SKIPPED = "_mrg-skipped";
    public static final String OUTFILE = "output-dict_mrg-result.wiki";
    public final String outFile;
    private final byte[] mergeLngDefBytes;
    private final String inFileMain;
    private static ByteBuffer bb = ByteBuffer.allocate(Helper.MAX_LINE_BYTES_MEDIUM);
    private static ByteBuffer mergeBB = ByteBuffer.allocate(Helper.MAX_LINE_BYTES_BIG);

    public static void main(String[] args) throws IOException {
        // String inFileTest = "O:\\kkdict\\out\\dicts\\wiki\\test\\test.txt";
        String inFileMain = Helper.DIR_OUT_DICTS + "\\wiki\\test\\test0.txt";
        String inFile0 = Helper.DIR_OUT_DICTS + "\\wiki\\test\\test1.txt";
        String inFile1 = Helper.DIR_OUT_DICTS + "\\wiki\\test\\test2.txt";
        // String inFile1 = "D:\\test1.txt";
        // String inFile2 = "D:\\test2.txt";
        // String inFile3 = "D:\\test3.txt";
        String outDir = Helper.DIR_OUT_DICTS + "\\wiki\\test\\";
        //
        // new DictFilesMerger(Language.ZH, outDir, inFileTest).extract();
        new DictFilesMerger(Language.AF, outDir, inFileMain, inFile0, inFile1).merge();
    }

    public DictFilesMerger(Language mergeLng, String outDir, String inFileMain, String... inFiles) {
        if (new File(outDir).isDirectory()) {
            System.out.println("温馨提示：需合并的文件必须事先排序。");
            this.inFiles = inFiles;
            this.inFileMain = inFileMain;
            this.outDir = outDir;
            this.mergeLng = mergeLng;
            this.outFile = outDir + File.separator + OUTFILE;
            this.mergeLngDefBytes = (mergeLng.key + Helper.SEP_DEFINITION).getBytes(Helper.CHARSET_UTF8);
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

        System.out.println("合并含有'" + mergeLng.key + "'的词典行  。。。");
        File f = new File(inFileMain);
        BufferedInputStream inFilesMainIn;
        if (f.isFile()) {
            System.out.println("导入主合并文件'" + f.getAbsolutePath() + "'（" + Helper.formatSpace(f.length()) + "）。。。");
            inFilesMainIn = new BufferedInputStream(new FileInputStream(f), Helper.BUFFER_SIZE);
        } else {
            System.err.println("主合并文件'" + f.getAbsolutePath() + "'不存在！");
            return;
        }

        BufferedInputStream[] inFilesIns = new BufferedInputStream[inFiles.length];
        BufferedOutputStream[] skippedOuts = new BufferedOutputStream[inFiles.length];
        int i = 0;
        for (String inFile : inFiles) {
            f = new File(inFile);
            if (f.isFile()) {
                System.out.println("导入分文件'" + f.getAbsolutePath() + "'（" + Helper.formatSpace(f.length()) + "）。。。");
                String skippedOutFile = Helper.appendFileName(outDir + File.separator + f.getName(), SUFFIX_SKIPPED);

                skippedOuts[i] = new BufferedOutputStream(new FileOutputStream(skippedOutFile), Helper.BUFFER_SIZE);
                inFilesIns[i] = new BufferedInputStream(new FileInputStream(f), Helper.BUFFER_SIZE);

            } else {
                System.err.println("分文件不可读'" + f.getAbsolutePath() + "'！");
            }
            i++;
        }
        System.out.println("创建输出文件'" + outFile + "'。。。");
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outFile), Helper.BUFFER_SIZE);

        merge(out, inFilesMainIn, inFilesIns, skippedOuts);

        for (BufferedInputStream in : inFilesIns) {
            if (in != null) {
                in.close();
            }
        }
        for (BufferedOutputStream o : skippedOuts) {
            if (o != null) {
                o.close();
            }
        }
        inFilesMainIn.close();
        out.close();
        System.out.println("合并成功'" + outFile + "'（" + Helper.formatSpace(new File(outFile).length()) + "）。");
    }

    private void merge(BufferedOutputStream out, BufferedInputStream inFilesMainIn, BufferedInputStream[] inFilesIns,
            BufferedOutputStream[] skippedOuts) throws IOException {
        ByteBuffer[] inFileBBs = new ByteBuffer[inFilesIns.length];
        for (int i = 0; i < inFilesIns.length; i++) {
            inFileBBs[i] = ByteBuffer.allocate(Helper.MAX_LINE_BYTES_MEDIUM);
            inFileBBs[i].limit(0);
        }
        while (-1 != Helper.readLine(inFilesMainIn, bb)) {
            if (DEBUG) {
                System.out.println("合并词组：" + Helper.toString(bb));
            }
            if (-1 == DictHelper.positionSortLng(bb, mergeLngDefBytes, true)) {
                // main file has no more sort key
                out.write(bb.array(), 0, bb.limit());
                out.write('\n');
                break;
            }
            // copy original line
            System.arraycopy(bb.array(), bb.position(), mergeBB.array(), 0, bb.limit());
            int mergedPosition = bb.limit();

            int i = 0;
            int inBBStopPoint = DictHelper.getStopPoint(bb.array(), 0, bb.limit(), DictHelper.ORDER_ATTRIBUTE);
            for (BufferedInputStream inFileIn : inFilesIns) {
                if (inFileIn != null) {
                    mergedPosition = mergeInFile(inFileBBs, inFileIn, skippedOuts[i], i, mergedPosition, inBBStopPoint);
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
                BufferedOutputStream skippedOut = skippedOuts[i];
                while ((len = inFileIn.read(mergeBB.array())) != -1) {
                    skippedOut.write(mergeBB.array(), 0, len);
                }
            }
            i++;
        }        
    }

    private int mergeInFile(ByteBuffer[] inFileBBs, BufferedInputStream inFileIn, BufferedOutputStream skippedOut, int inFileIdx, int mergedPosition,
            int inBBStopPoint) throws IOException {
        boolean predessor = false;
        do {
            ByteBuffer inFileBB = inFileBBs[inFileIdx];
            if (inFileBB != null) {
                boolean eof = false;
                if (inFileBB.limit() == 0) {
                    if (-1 != Helper.readLine(inFileIn, inFileBB)) {
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
                    predessor = false;
                } else {
                    int inFileBBStopPoint = DictHelper.getStopPoint(inFileBB.array(), 0, inFileBB.limit(),
                            DictHelper.ORDER_ATTRIBUTE);
                    if (inFileBBStopPoint != Helper.MAX_LINE_BYTES_MEDIUM) {
                        // stop point okay
                        int sepDefLen = mergeLngDefBytes.length;
                        int bbStopLen = inBBStopPoint - sepDefLen;
                        int inFileStopLen = inFileBBStopPoint - sepDefLen;
                        predessor = DictHelper.isPredessorEquals(inFileBB, sepDefLen, inFileStopLen, bb, sepDefLen,
                                bbStopLen);

                        if (predessor) {
                            if (DictHelper.isEquals(inFileBB, sepDefLen, inFileStopLen, bb, sepDefLen, bbStopLen)) {
                                // merge
                                mergeBB.position(mergedPosition);
                                mergedPosition = DictHelper.mergeDefinitionsAndAttributes(mergeBB, inFileBB);
                                if (DEBUG) {
                                    System.out.println(inFileIdx + ": merge "
                                            + Helper.toString(bb.array(), sepDefLen, bbStopLen) + " == "
                                            + Helper.toString(inFileBB.array(), sepDefLen, inFileStopLen));
                                }
                            } else {
                                if (DEBUG) {
                                    System.out.println(inFileIdx + ": skip "
                                            + Helper.toString(inFileBB.array(), sepDefLen, inFileStopLen) + " < "
                                            + Helper.toString(bb.array(), sepDefLen, bbStopLen));
                                }
                                // System.out.println(i + ": skip " + Helper.toHexString(bb.array(), 0,
                                // inBBStopPoint)
                                // + " < " + Helper.toHexString(inFileBB.array(), 0, inFileBBStopPoint));
                                skippedOut.write(inFileBB.array(), 0, inFileBB.limit());
                                skippedOut.write('\n');
                            }
                            inFileBB.limit(0);
                        } else {
                            if (DEBUG) {
                                System.out.println(inFileIdx + ": skip "
                                        + Helper.toString(inFileBB.array(), sepDefLen, inFileStopLen) + " > "
                                        + Helper.toString(bb.array(), sepDefLen, bbStopLen));
                                System.out.println(inFileIdx + ": skip "
                                        + Helper.toHexString(inFileBB.array(), sepDefLen, inFileStopLen) + " > "
                                        + Helper.toHexString(bb.array(), sepDefLen, bbStopLen));
                            }
                        }
                    }
                }
            }
        } while (predessor);
        return mergedPosition;
    }

}
