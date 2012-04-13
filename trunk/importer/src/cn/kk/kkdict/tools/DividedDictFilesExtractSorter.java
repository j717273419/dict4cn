package cn.kk.kkdict.tools;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import cn.kk.kkdict.types.Language;
import cn.kk.kkdict.utils.DictHelper;
import cn.kk.kkdict.utils.Helper;

public class DividedDictFilesExtractSorter {
    private final Language sortLng;
    private final String outDir;
    public final String outFile;
    private final String[] inFiles;
    private final long MAX_SIZE = 1024 * 1024 * 500;
    private final boolean DEBUG = false;
    private final boolean writeSkippedExtracted;

    public DividedDictFilesExtractSorter(Language sortLng, String outDir, String outFile,
            boolean writeSkippedExtracted, String... inFiles) {
        this.sortLng = sortLng;
        this.outDir = outDir;
        this.outFile = outDir + File.separator + outFile;
        this.inFiles = inFiles;
        this.writeSkippedExtracted = writeSkippedExtracted;
    }

    public void sort() throws IOException, InterruptedException {
        LinkedList<String> files = new LinkedList<String>(Arrays.asList(this.inFiles));
        LinkedList<String> working = new LinkedList<String>();
        boolean first = true;
        File outFileFile = new File(outFile);
        outFileFile.delete();
        String tmpOutFile = Helper.appendFileName(outFile, "_ddfes-tmp");
        File tmpFile = new File(tmpOutFile);
        TimeUnit.SECONDS.sleep(1);
        while (!files.isEmpty()) {
            working.clear();
            long size = 0;
            while (size < MAX_SIZE && !files.isEmpty()) {
                String f = files.get(0);
                long s = new File(f).length();
                if (working.isEmpty() || s + size < MAX_SIZE) {
                    if (DEBUG) {
                        System.out.println("加入文件：'" + f + "'（" + Helper.formatSpace(s) + "）。。。");
                    }
                    if (s > Helper.SEP_LIST_BYTES.length) {
                        working.add(f);
                    }
                    size += s;
                    files.remove(f);
                } else {
                    break;
                }
            }

            DictFilesExtractor extractor = new DictFilesExtractor(sortLng, this.outDir, tmpFile.getName(),
                    writeSkippedExtracted, working.toArray(new String[working.size()]));
            extractor.extract();
            if (tmpFile.length() > Helper.SEP_LIST_BYTES.length) {
                DictFilesSorter sorter = new DictFilesSorter(sortLng, this.outDir, true, false, extractor.outFile);
                sorter.sort();
                File sorterOutFile = new File(sorter.outFile);
                if (sorterOutFile.length() > Helper.SEP_LIST_BYTES.length) {
                    if (first) {
                        first = false;
                        tmpFile.delete();
                        sorterOutFile.renameTo(outFileFile);
                    } else {
                        tmpFile.delete();
                        outFileFile.renameTo(tmpFile);
                        TimeUnit.SECONDS.sleep(1);
                        DictFilesMerger merger = new DictFilesMerger(sortLng, outDir, outFileFile.getName(),
                                tmpOutFile, sorter.outFile);
                        merger.merge();
                        tmpFile.delete();
                        sorterOutFile.delete();
                    }
                    TimeUnit.SECONDS.sleep(1);
                    System.out.println("排序临时文件完成：'" + outFile + "'（" + Helper.formatSpace(outFileFile.length())
                            + "）");
                } else {
                    sorterOutFile.delete();
                }
            } else {
                tmpFile.delete();
            }
        }
        System.out.println("排序完成.输出文件：'" + this.outFile + "'（" + Helper.formatSpace(new File(this.outFile).length())
                + "）");

    }
}
