package cn.kk.kkdict.tools;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import cn.kk.kkdict.beans.DictByteBufferRow;
import cn.kk.kkdict.extraction.dict.WikiPagesMetaCurrentExtractor;
import cn.kk.kkdict.types.Language;
import cn.kk.kkdict.utils.ArrayHelper;
import cn.kk.kkdict.utils.DictHelper;
import cn.kk.kkdict.utils.Helper;

/**
 * 
 * extract lines containing certain definition to another file
 * 
 */
public class DictFilesExtractor {
    private final String[] inFiles;
    private final String outDir;
    private final Language extractLng;
    public static final String SUFFIX_SKIPPED = "_xtr-skipped";
    public static final String OUTDIR = WikiPagesMetaCurrentExtractor.OUT_DIR;
    public static final String OUTFILE = "output-dict_xtr-result.wiki";
    private static final boolean DEBUG = false;
    public final String outFile;
    private final ByteBuffer lngBB;
    private boolean writeSkipped;

    public static void main(String[] args) throws IOException {
        String outDir = Helper.DIR_OUT_DICTS + File.separator + "wiki" + File.separator + "test";
        String inFileTest = outDir + File.separator + "output-dict.wiki_ang";
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

        new DictFilesExtractor(Language.ZH, outDir, OUTFILE, true, inFileTest).extract();
        // new DictFilesExtractor(Language.ZH, outDir, OUTFILE, false, inFile0, inFile1, inFile2, inFile3, inFile4,
        // inFile5, inFile6, inFile7, inFile8, inFile9).extract();
    }

    public DictFilesExtractor(Language extractLng, String outDir, String outFile, boolean writeSkipped,
            String... inFiles) {
        if (new File(outDir).isDirectory()) {
            this.inFiles = inFiles;
            this.outDir = outDir;
            this.extractLng = extractLng;
            this.outFile = outDir + File.separator + outFile;
            this.lngBB = ByteBuffer.wrap(extractLng.key.getBytes(Helper.CHARSET_UTF8));
            this.writeSkipped = writeSkipped;
        } else {
            this.inFiles = null;
            this.outDir = null;
            this.extractLng = null;
            this.outFile = null;
            this.lngBB = null;
            System.err.println("文件夹不可读：'" + outDir + "'!");
        }
    }

    public void extract() throws IOException {
        System.out.println("截取含有'" + extractLng.key + "'的词典   。。。" + inFiles.length);
        if (DEBUG) {
            System.out.println("创建输出文件'" + outFile + "'。。。");
        }
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outFile), Helper.BUFFER_SIZE);
        for (String inFile : inFiles) {
            File f = new File(inFile);
            if (f.isFile()) {
                if (DEBUG) {
                    System.out
                            .println("处理截取文件'" + f.getAbsolutePath() + "'（" + Helper.formatSpace(f.length()) + "）。。。");
                }
                String skippedOutFile = null;
                if (writeSkipped) {
                    skippedOutFile = Helper.appendFileName(outDir + File.separator + f.getName(), SUFFIX_SKIPPED);
                }
                extract(out, f, skippedOutFile);
                if (DEBUG) {
                    System.out.println("处理wiki文件成功：'" + f.getAbsolutePath() + "'，不符合条件文件：'" + skippedOutFile + "'（"
                            + Helper.formatSpace(new File(skippedOutFile).length()) + "）");
                }
            } else {
                System.err.println("wiki文件不可读'" + f.getAbsolutePath() + "'！");
            }
        }
        out.close();
        System.out.println("截取成功'" + outFile + "'（" + Helper.formatSpace(new File(outFile).length()) + "）。");

    }

    private void extract(BufferedOutputStream out, File f, String skippedOutFile) throws IOException {
        BufferedOutputStream skippedOut = null;
        if (skippedOutFile != null) {
            skippedOut = new BufferedOutputStream(new FileOutputStream(skippedOutFile), Helper.BUFFER_SIZE);
        }
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(f), Helper.BUFFER_SIZE);
        final ByteBuffer bb = ArrayHelper.borrowByteBufferMedium();
        final byte[] array = bb.array();
        int limit;
        DictByteBufferRow row = new DictByteBufferRow();
        while (-1 != ArrayHelper.readLine(in, bb)) {
            limit = bb.limit();
            row.parseFrom(bb);
            if (-1 != row.indexOfLanguage(lngBB)) {
                out.write(array, 0, limit);
                out.write('\n');
            } else if (skippedOut != null) {
                skippedOut.write(array, 0, limit);
                skippedOut.write('\n');
            }
        }
        if (skippedOut != null) {
            skippedOut.close();
        }
        ArrayHelper.giveBack(bb);
    }
}
