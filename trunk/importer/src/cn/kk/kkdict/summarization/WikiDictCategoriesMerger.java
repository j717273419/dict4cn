package cn.kk.kkdict.summarization;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cn.kk.kkdict.extraction.dict.WikiPagesMetaCurrentExtractor;
import cn.kk.kkdict.tools.DictFilesSorter;
import cn.kk.kkdict.tools.FilesAppender;
import cn.kk.kkdict.tools.LongestLineFinder;
import cn.kk.kkdict.types.Language;
import cn.kk.kkdict.utils.Helper;

public class WikiDictCategoriesMerger {
    private static final int PROCESS_LIMIT = 1024 * 1024 * 200;
    public static final String IN_DIR = WikiPagesMetaCurrentExtractor.OUT_DIR;
    public static final String OUT_DIR = WikiPagesMetaCurrentExtractor.OUT_DIR;
    public static final String OUT_FILE = OUT_DIR + "\\output-dict_categories-merged.wiki";
    private static final boolean DEBUG = false;

    /**
     * @param args
     * @throws IOException
     * @throws InterruptedException
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        new File(OUT_DIR).mkdirs();
        File inDirFile = new File(IN_DIR);
        if (inDirFile.isDirectory()) {
            System.out.print("合并wiki类别文件'" + IN_DIR + "' ... ");

            File[] files = inDirFile.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.startsWith("output-dict_categories.");
                }
            });
            System.out.println(files.length);

            String[] filePaths = Helper.getFileNames(files);
            List<String> fileList = new ArrayList<String>(Arrays.asList(filePaths));
            List<String> workFiles = new ArrayList<String>();
            boolean first = true;
            String outFileTmp = Helper.appendFileName(OUT_FILE, "_tmp");
            while (!fileList.isEmpty()) {
                workFiles.clear();
                long size = 0;
                while (size < PROCESS_LIMIT && !fileList.isEmpty()) {
                    String f = fileList.get(0);
                    long s = new File(f).length();
                    if (workFiles.isEmpty() || s + size < PROCESS_LIMIT) {
                        if (DEBUG) {
                            System.out.println("加入文件：'" + f + "'（" + Helper.formatSpace(s) + "）。。。");
                        }
                        workFiles.add(f);
                        size += s;
                        fileList.remove(f);
                    } else {
                        break;
                    }
                }
                if (first) {
                    first = false;
                } else {
                    new File(outFileTmp).delete();
                    new File(OUT_FILE).renameTo(new File(outFileTmp));
                    TimeUnit.SECONDS.sleep(1);
                    workFiles.add(outFileTmp);
                }
                String mergedOutFile = Helper.appendFileName(OUT_FILE, "_mrg-tmp");
                FilesAppender merger = new FilesAppender(mergedOutFile, workFiles.toArray(new String[workFiles.size()]));
                merger.append();

                if (DEBUG) {
                    System.out.println("排序类别文件：'" + mergedOutFile + "'（"
                            + Helper.formatSpace(new File(mergedOutFile).length()) + "）");
                }
                DictFilesSorter sorter = new DictFilesSorter(Language.EN, OUT_DIR, new File(OUT_FILE).getName(), true,
                        false, mergedOutFile);
                sorter.sort();
                new File(mergedOutFile).delete();
                new File(outFileTmp).delete();
                TimeUnit.SECONDS.sleep(1);
                if (DEBUG) {
                    System.out.println("排序分步骤完成.输出文件：'" + OUT_FILE + "'（"
                            + Helper.formatSpace(new File(OUT_FILE).length()) + "）");
                    String longestLine = new LongestLineFinder(OUT_FILE).find();
                    System.out.println("最长行：" + longestLine + "（" + longestLine.length() + "字符）");
                }
            }
            new File(outFileTmp).delete();
            new File(OUT_FILE).renameTo(new File(outFileTmp));
            TimeUnit.SECONDS.sleep(1);
            DictFilesSorter sorter = new DictFilesSorter(Language.DE, OUT_DIR, new File(OUT_FILE).getName(), false,
                    false, outFileTmp);
            sorter.sort();
            
            new File(outFileTmp).delete();
            new File(OUT_FILE).renameTo(new File(outFileTmp));
            TimeUnit.SECONDS.sleep(1);
            sorter = new DictFilesSorter(Language.ZH, OUT_DIR, new File(OUT_FILE).getName(), false,
                    false, outFileTmp);
            sorter.sort();
            new File(outFileTmp).delete();
            System.out.println("排序中文完成.输出文件：'" + OUT_FILE + "'（" + Helper.formatSpace(new File(OUT_FILE).length())
                    + "）");
        }
    }
    
}
