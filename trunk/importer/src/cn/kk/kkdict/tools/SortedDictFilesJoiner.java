package cn.kk.kkdict.tools;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import cn.kk.kkdict.beans.DictByteBufferRow;
import cn.kk.kkdict.types.Language;
import cn.kk.kkdict.utils.ArrayHelper;
import cn.kk.kkdict.utils.DictHelper;
import cn.kk.kkdict.utils.Helper;

/**
 * 将多个已排序的文件中的数据合并为进主文件。如果主文件与分文件的单词相同，它们的行将合并为一行。（主文件不会多出行。）如一定义里一种语言有含有多个单词，最小的单词将拿已比较。 TODO test
 */
public class SortedDictFilesJoiner {
    private static boolean noticed = false;
    private final static boolean DEBUG = false;
    private final String[] inFiles;
    private final String outDir;
    private final Language mergeLng;
    public static final String SUFFIX_SKIPPED = "_jnr-skipped";
    public static final String OUT_FILE = "output-dict_jnr-result.wiki";
    public final String outFile;
    private final ByteBuffer lngBB;
    private final String inFileMain;
    private final DictByteBufferRow otherRow = new DictByteBufferRow();
    private final DictByteBufferRow mainRow = new DictByteBufferRow();
    private boolean writeSkipped = true;
    private int mainIdx;
    private int otherIdx;

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
        new SortedDictFilesJoiner(Language.AF, outDir, OUT_FILE, inFileMain, inFile0, inFile1).join();
    }

    public SortedDictFilesJoiner(Language mergeLng, String outDir, String outFile, String inFileMain, String... inFiles) {
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
            this.lngBB = ByteBuffer.wrap(mergeLng.key.getBytes(Helper.CHARSET_UTF8));
        } else {
            this.inFiles = null;
            this.inFileMain = null;
            this.outDir = null;
            this.mergeLng = null;
            this.outFile = null;
            this.lngBB = null;
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

                if (writeSkipped) {
                    skippedOuts[i] = new BufferedOutputStream(new FileOutputStream(skippedOutFile), Helper.BUFFER_SIZE);
                }
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
        final ByteBuffer[] inFileBBs = new ByteBuffer[inFilesIns.length];
        for (int i = 0; i < inFilesIns.length; i++) {
            inFileBBs[i] = ArrayHelper.borrowByteBufferMedium();
            inFileBBs[i].limit(0);
        }
        final ByteBuffer mergeBB = ArrayHelper.borrowByteBufferLarge();
        final ByteBuffer lineBB = ArrayHelper.borrowByteBufferMedium();
        while (-1 != ArrayHelper.readLine(inFilesMainIn, lineBB)) {
            if (DEBUG) {
                System.out.println("兼并词组：" + ArrayHelper.toString(lineBB));
            }
            mainRow.parseFrom(lineBB).sortValues();
            if (-1 == (mainIdx = mainRow.indexOfLanguage(lngBB))) {
                // main file has no more sort key
                out.write(mainRow.array(), 0, mainRow.limit());
                out.write('\n');
                break;
            }
            mergeBB.clear();
            ArrayHelper.copyP(mainRow.getByteBuffer(), mergeBB);
            for (int i = 0; i < inFilesIns.length; i++) {
                mergeInFile(mergeBB, inFileBBs, inFilesIns, skippedOuts, i);
            }
            out.write(mergeBB.array(), 0, mergeBB.limit());
            out.write('\n');
        }
        ArrayHelper.giveBack(lineBB);
        int len;
        while ((len = inFilesMainIn.read(mergeBB.array())) != -1) {
            out.write(mergeBB.array(), 0, len);
        }
        int i = 0;
        for (BufferedInputStream inFileIn : inFilesIns) {
            final ByteBuffer inBB = inFileBBs[i];
            if (inBB != null && inBB.hasRemaining()) {
                out.write(inBB.array(), 0, inBB.limit());
                out.write('\n');
                inFileBBs[i] = null;
                ArrayHelper.giveBack(inBB);
            }
            if (inFileIn != null) {
                BufferedOutputStream skippedOut = skippedOuts[i];
                if (skippedOut != null) {
                    while ((len = inFileIn.read(mergeBB.array())) != -1) {
                        skippedOut.write(mergeBB.array(), 0, len);
                    }
                }
            }
            i++;
        }
        ArrayHelper.giveBack(mergeBB);
    }

    private void mergeInFile(ByteBuffer mergeBB, ByteBuffer[] inFileBBs, BufferedInputStream[] inFileIns,
            BufferedOutputStream[] skippedOuts, int idx) throws IOException {
        BufferedInputStream in = inFileIns[idx];
        if (in != null) {
            boolean predessor = false;
            do {
                ByteBuffer inBB = inFileBBs[idx];
                if (inBB != null) {
                    boolean eof = false;
                    if (inBB.limit() == 0) {
                        if (-1 != ArrayHelper.readLine(in, inBB)) {
                            otherRow.parseFrom(inBB).sortValues();
                            if (-1 == (otherIdx = otherRow.indexOfLanguage(lngBB))) {
                                // in file has no more sort key
                                eof = true;
                            }
                        } else {
                            eof = true;
                        }
                    } else {
                        otherRow.parseFrom(inBB).sortValues();
                    }
                    if (eof) {
                        if (DEBUG) {
                            System.out.println(idx + ": end");
                        }
                        inFileBBs[idx] = null;
                        ArrayHelper.giveBack(inBB);
                        predessor = false;
                    } else {
                        predessor = ArrayHelper.isPredessorEqualsP(otherRow.getFirstValue(otherIdx),
                                mainRow.getFirstValue(mainIdx));
                        if (predessor) {
                            if (ArrayHelper.equalsP(otherRow.getFirstValue(otherIdx), mainRow.getFirstValue(mainIdx))) {
                                // merge
                                DictHelper.mergeDefinitionsAndAttributes(mainRow, otherRow, mergeBB);
                                if (DEBUG) {
                                    System.out.println(idx + ": merge "
                                            + ArrayHelper.toStringP(otherRow.getByteBuffer()) + " == "
                                            + ArrayHelper.toStringP(mainRow.getByteBuffer()));
                                }
                                mainRow.parseFrom(mergeBB, true);
                            } else {
                                if (DEBUG) {
                                    System.out.println(idx + ": skip "
                                            + ArrayHelper.toStringP(otherRow.getByteBuffer()) + " < "
                                            + ArrayHelper.toStringP(mainRow.getByteBuffer()));
                                }
                                // System.out.println(i + ": skip " + Helper.toHexString(bb.array(), 0,
                                // inBBStopPoint)
                                // + " < " + Helper.toHexString(inFileBB.array(), 0, inFileBBStopPoint));
                                if (skippedOuts[idx] != null) {
                                    skippedOuts[idx].write(otherRow.array(), otherRow.position(), otherRow.limit());
                                    skippedOuts[idx].write('\n');
                                }
                            }
                            inBB.limit(0);
                        } else {
                            if (DEBUG) {
                                System.out.println(idx + ": skip " + ArrayHelper.toStringP(otherRow.getByteBuffer())
                                        + " > " + ArrayHelper.toStringP(mainRow.getByteBuffer()));
                            }
                            // reset inBB limit and position
                            otherRow.getByteBuffer();
                        }
                    }
                }
            } while (predessor);
        }
    }
}
