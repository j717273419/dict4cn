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

import cn.kk.kkdict.beans.DictByteBufferRow;
import cn.kk.kkdict.beans.IndexedByteArray;
import cn.kk.kkdict.extraction.dict.WikiPagesMetaCurrentExtractor;
import cn.kk.kkdict.types.Language;
import cn.kk.kkdict.types.Score;
import cn.kk.kkdict.utils.ArrayHelper;
import cn.kk.kkdict.utils.DictHelper;
import cn.kk.kkdict.utils.Helper;

public class WikiDictGeoLocationCorrector {
    public static final String IN_DIR = WikiPagesMetaCurrentExtractor.OUT_DIR;
    public static final String OUT_DIR = WikiPagesMetaCurrentExtractor.OUT_DIR + File.separator + "output";
    public static final String SUFFIX_CORRECTED = "_corrected";
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

            System.out.print("修复wiki坐标文件'" + IN_DIR + "' ... ");
            File[] files = inDirFile.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.startsWith("output-dict_images.");
                }
            });
            System.out.println(files.length);

            long start = System.currentTimeMillis();
            String[] filePaths = Helper.getFileNames(files);
            ByteBuffer lineBB = ArrayHelper.borrowByteBufferSmall();
            DictByteBufferRow row = new DictByteBufferRow();
            for (String f : filePaths) {
                if (DEBUG) {
                    System.out.println("处理坐标文件：" + f);
                }
                String outFile = OUT_DIR + File.separator
                        + Helper.appendFileName(new File(f).getName(), SUFFIX_CORRECTED);
                Language lng = DictHelper.getWikiLanguage(f);
                if (lng != null) {
                    long startFile = System.currentTimeMillis();

                    BufferedInputStream in = new BufferedInputStream(new FileInputStream(f), Helper.BUFFER_SIZE);
                    BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outFile),
                            Helper.BUFFER_SIZE);
                    int statValid = 0;
                    int statInvalid = 0;
                    while (-1 != ArrayHelper.readLine(in, lineBB)) {
                        row.parseFrom(lineBB);
                        if (row.size() == 1 && row.getAttributesSize(0, 0) == 1) {
                            if (-1 != getCheckedGeoLocation(row)) {
                                statValid++;
                                out.write(lineBB.array(), 0, lineBB.limit());
                                out.write(Helper.SEP_NEWLINE_CHAR);
                                if (DEBUG) {
                                    System.out.println("写入坐标：" + ArrayHelper.toString(lineBB));
                                }
                            } else {
                                statInvalid++;
                                if (DEBUG) {
                                    System.err.println("跳过坐标：" + ArrayHelper.toString(lineBB));
                                }
                            }
                        }
                    }
                    out.close();
                    in.close();
                    System.out.println("完成'" + outFile + "'，有效：" + statValid + "，无效：" + statInvalid + "（"
                            + Helper.formatSpace(new File(outFile).length()) + "），用时："
                            + Helper.formatDuration(System.currentTimeMillis() - startFile));
                }
            }
            ArrayHelper.giveBack(lineBB);
            System.out.println("修复坐标文件总共用时：" + Helper.formatDuration(System.currentTimeMillis() - start));
        }
    }

    private static int getCheckedGeoLocation(DictByteBufferRow row) {
        // TODO Auto-generated method stub
        return 0;
    }


}
