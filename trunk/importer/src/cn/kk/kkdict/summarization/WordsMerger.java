package cn.kk.kkdict.summarization;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import cn.kk.kkdict.tools.WordFilesSorter;
import cn.kk.kkdict.utils.Helper;

public class WordsMerger {
    private static final String IN_DIR = Helper.DIR_OUT_WORDS;
    private static final String OUT_DIR = Helper.DIR_OUT_WORDS + File.separator + "output";
    private static final String OUT_FILE = OUT_DIR + File.separator + "output-words-merged.words";

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        File directory = new File(IN_DIR);
        if (directory.isDirectory()) {
            new File(OUT_DIR).mkdirs();
            System.out.print("搜索词组文件'" + IN_DIR + "' ... ");

            File[] files = directory.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.startsWith("output-words.");
                }
            });

            System.out.println(files.length);

            String[] filePaths = Helper.getFileNames(files);
            WordFilesSorter sorter = new WordFilesSorter(OUT_DIR, new File(OUT_FILE).getName(), false, false, filePaths);
            sorter.sort();

            System.out.println("\n=====================================");
            System.out.println("总共读取词语文件：" + files.length);
            System.out.println("词语数目：" + sorter.getTotalSorted());
            System.out.println("=====================================");
        }
    }
}
