package cn.kk.kkdict.extraction.dict;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.ByteBuffer;

import cn.kk.kkdict.Configuration;
import cn.kk.kkdict.Configuration.Source;
import cn.kk.kkdict.beans.DictByteBufferRow;
import cn.kk.kkdict.types.Category;
import cn.kk.kkdict.types.TranslationSource;
import cn.kk.kkdict.types.UriLocation;
import cn.kk.kkdict.utils.ArrayHelper;
import cn.kk.kkdict.utils.Helper;

public class TermWikiExtractor {
    public static final String IN_DIR = Configuration.IMPORTER_FOLDER_SELECTED_WORDS.getPath(Source.WORD_TERMWIKI);
    public static final String OUT_DIR = Configuration.IMPORTER_FOLDER_EXTRACTED_CRAWLED.getPath(Source.WORD_TERMWIKI);
    public static final String OUT_DIR_FINISHED = OUT_DIR + "/finished";
    private static final boolean DEBUG = false;

    public static void main(String[] args) throws IOException {
        File directory = new File(IN_DIR);
        if (directory.isDirectory()) {
            System.out.print("搜索termwiki词组文件'" + IN_DIR + "' ... ");

            File[] files = directory.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith("." + TranslationSource.TERMWIKI.key) && name.startsWith("words_");
                }
            });
            System.out.println(files.length);

            long total = 0;
            for (File f : files) {
                final long start = System.currentTimeMillis();
                System.out.print("分析'" + f + " ... ");
                File outFile = new File(OUT_DIR, f.getName());
                if (DEBUG) {
                    System.out.println("写出：" + outFile + " 。。。");
                }
                BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outFile), Helper.BUFFER_SIZE);
                int counter = crawl(f, out);
                out.close();
                System.out.println(counter + "，用时：" + Helper.formatDuration(System.currentTimeMillis() - start));
                total += counter;
                f.renameTo(new File(OUT_DIR_FINISHED, f.getName()));
            }

            System.out.println("\n=====================================");
            System.out.println("成功读取了" + files.length + "个termwiki文件");
            System.out.println("总共单词：" + total);
            System.out.println("=====================================");
        }
    }

    private static int crawl(File f, BufferedOutputStream out) throws IOException {
        int count = 0;
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(f), Helper.BUFFER_SIZE);
        ByteBuffer lineBB = ArrayHelper.borrowByteBufferSmall();
        DictByteBufferRow row = new DictByteBufferRow();
        while (-1 != ArrayHelper.readLineTrimmed(in, lineBB)) {
            row.parseFrom(lineBB);
            if (row.size() == 1) {
                System.out.println("语言：" + ArrayHelper.toStringP(row.getLanguage(0)) + "，单词："
                        + ArrayHelper.toStringP(row.getValue(0, 0)) + "，地址："
                        + ArrayHelper.toStringP(row.getFirstAttributeValue(0, 0, UriLocation.TYPE_ID_BYTES)) + "，类别："
                        + ArrayHelper.toStringP(row.getFirstAttributeValue(0, 0, Category.TYPE_ID_BYTES)));
            }
        }
        ArrayHelper.giveBack(lineBB);
        in.close();
        return count;
    }
}
