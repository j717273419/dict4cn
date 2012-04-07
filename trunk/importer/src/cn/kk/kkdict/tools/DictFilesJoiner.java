package cn.kk.kkdict.tools;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import cn.kk.kkdict.types.Language;
import cn.kk.kkdict.utils.ArrayHelper;
import cn.kk.kkdict.utils.DictHelper;
import cn.kk.kkdict.utils.Helper;

/**
 * Merge sorted dict files
 * 
 */
public class DictFilesJoiner {
    private static boolean noticed = false;
    private final static boolean DEBUG = false;
    private final String[] inFiles;
    private final String outDir;
    private final Language mergeLng;
    public static final String SUFFIX_SKIPPED = "_jnr-skipped";
    public static final String OUT_FILE = "output-dict_jnr-result.wiki";
    public final String outFile;
    private final byte[] mergeLngDefBytes;
    private final String inFileMain;

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
        new DictFilesJoiner(Language.AF, outDir, OUT_FILE, inFileMain, inFile0, inFile1).join();
    }

    public DictFilesJoiner(Language mergeLng, String outDir, String outFile, String inFileMain, String... inFiles) {
        if (new File(outDir).isDirectory()) {
            if (!noticed) {
                System.out.println("温馨提示：需兼并的文件必须事先排序。");
                noticed = true;
            }
            this.inFiles = inFiles;
            this.inFileMain = inFileMain;
            this.outDir = outDir;
            this.mergeLng = mergeLng;
            if (outFile != null) {
                this.outFile = outDir + File.separator + outFile;
            } else {
                this.outFile = outDir + File.separator + OUT_FILE;
            }
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

    public void join() throws IOException {
        long start = System.currentTimeMillis();
        System.out.println("兼并含有'" + mergeLng.key + "'的词典  。。。" + (inFiles.length + 1));
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
        BufferedOutputStream[] skippedOuts = new BufferedOutputStream[inFiles.length];
        int i = 0;
        for (String inFile : inFiles) {
            f = new File(inFile);
            if (f.isFile()) {
                if (DEBUG) {
                    System.out.println("导入分文件'" + f.getAbsolutePath() + "'（" + Helper.formatSpace(f.length()) + "）。。。");
                }
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

        join(out, inFilesMainIn, inFilesIns, skippedOuts);

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
        System.out.println("兼并成功'" + outFile + "'（" + Helper.formatSpace(new File(outFile).length()) + "），用时："
                + Helper.formatDuration(System.currentTimeMillis() - start));
    }

    private void join(BufferedOutputStream out, BufferedInputStream inFilesMainIn, BufferedInputStream[] inFilesIns,
            BufferedOutputStream[] skippedOuts) throws IOException {
        ByteBuffer[] inFileBBs = new ByteBuffer[inFilesIns.length];
        for (int i = 0; i < inFilesIns.length; i++) {
            inFileBBs[i] = ArrayHelper.getByteBufferMedium();
            inFileBBs[i].limit(0);
        }
        ByteBuffer mergeBB = ArrayHelper.getByteBufferLarge();
        ByteBuffer lineBB = ArrayHelper.getByteBufferMedium();
        while (-1 != ArrayHelper.readLine(inFilesMainIn, lineBB)) {
            if (DEBUG) {
                System.out.println("兼并词组：" + ArrayHelper.toString(lineBB));
            }
            if (-1 == DictHelper.positionSortLng(lineBB, mergeLngDefBytes, true)) {
                // main file has no more sort key
                out.write(lineBB.array(), 0, lineBB.limit());
                out.write('\n');
                break;
            }
            // copy original line
            System.arraycopy(lineBB.array(), lineBB.position(), mergeBB.array(), 0, lineBB.limit());
            int mergedPosition = lineBB.limit();

            int i = 0;
            int inBBStopPoint = DictHelper.getStopPoint(lineBB.array(), 0, lineBB.limit(), DictHelper.ORDER_ATTRIBUTE);
            for (BufferedInputStream inFileIn : inFilesIns) {
                if (inFileIn != null) {
                    mergedPosition = mergeInFile(lineBB, mergeBB, inFileBBs, inFileIn, skippedOuts[i], i,
                            mergedPosition, inBBStopPoint);
                }
                i++;
            }
            out.write(mergeBB.array(), 0, mergedPosition);
            out.write('\n');
        }
        ArrayHelper.giveBack(lineBB);
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
            ArrayHelper.giveBack(inFileBBs[i]);
            i++;
        }
        ArrayHelper.giveBack(mergeBB);
    }

    private int mergeInFile(ByteBuffer lineBB, ByteBuffer mergeBB, ByteBuffer[] inFileBBs,
            BufferedInputStream inFileIn, BufferedOutputStream skippedOut, int inFileIdx, int mergedPosition,
            int inBBStopPoint) throws IOException {
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
                    int inFileBBStopPoint = DictHelper.getStopPoint(inFileBB.array(), 0, inFileBB.limit(),
                            DictHelper.ORDER_ATTRIBUTE);
                    if (inFileBBStopPoint < inFileBB.capacity()) {
                        // stop point okay
                        int sepDefLen = mergeLngDefBytes.length;
                        int bbStopLen = inBBStopPoint - sepDefLen;
                        int inFileStopLen = inFileBBStopPoint - sepDefLen;
                        predessor = ArrayHelper.isPredessorEquals(inFileBB, sepDefLen, inFileStopLen, lineBB,
                                sepDefLen, bbStopLen);

                        if (predessor) {
                            if (ArrayHelper.isEquals(inFileBB, sepDefLen, inFileStopLen, lineBB, sepDefLen, bbStopLen)) {
                                // merge
                                mergeBB.position(mergedPosition);
                                mergedPosition = DictHelper.mergeDefinitionsAndAttributes(mergeBB, inFileBB);
                                if (DEBUG) {
                                    System.out.println(inFileIdx + ": merge "
                                            + ArrayHelper.toString(lineBB.array(), sepDefLen, bbStopLen) + " == "
                                            + ArrayHelper.toString(inFileBB.array(), sepDefLen, inFileStopLen));
                                }
                            } else {
                                if (DEBUG) {
                                    System.out.println(inFileIdx + ": skip "
                                            + ArrayHelper.toString(inFileBB.array(), sepDefLen, inFileStopLen) + " < "
                                            + ArrayHelper.toString(lineBB.array(), sepDefLen, bbStopLen));
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
                                        + ArrayHelper.toString(inFileBB.array(), sepDefLen, inFileStopLen) + " > "
                                        + ArrayHelper.toString(lineBB.array(), sepDefLen, bbStopLen));
                                System.out.println(inFileIdx + ": skip "
                                        + ArrayHelper.toHexString(inFileBB.array(), sepDefLen, inFileStopLen) + " > "
                                        + ArrayHelper.toHexString(lineBB.array(), sepDefLen, bbStopLen));
                            }
                        }
                    }
                }
            }
        } while (predessor);
        return mergedPosition;
    }

}
