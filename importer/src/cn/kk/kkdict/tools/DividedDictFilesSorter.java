/*  Copyright (c) 2010 Xiaoyun Zhu
 * 
 *  Permission is hereby granted, free of charge, to any person obtaining a copy  
 *  of this software and associated documentation files (the "Software"), to deal  
 *  in the Software without restriction, including without limitation the rights  
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell  
 *  copies of the Software, and to permit persons to whom the Software is  
 *  furnished to do so, subject to the following conditions:
 *  
 *  The above copyright notice and this permission notice shall be included in  
 *  all copies or substantial portions of the Software.
 *  
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR  
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,  
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE  
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER  
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,  
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN  
 *  THE SOFTWARE.  
 */
package cn.kk.kkdict.tools;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import cn.kk.kkdict.Configuration;
import cn.kk.kkdict.Configuration.Source;
import cn.kk.kkdict.types.Language;
import cn.kk.kkdict.utils.Helper;

/**
 * refactor
 * 
 * 渐进式的排序词典文件
 * 
 * @author x_kez
 * 
 */
public class DividedDictFilesSorter {
    private final Language sortLng;
    private final String outDir;
    public final String outFile;
    private final String[] inFiles;
    private final long MAX_SIZE = 1024 * 1024 * 500;
    private final boolean DEBUG = true;

    public static void main(String[] args) throws IOException, InterruptedException {
        String[] files = new String[21];
        for (int i = 0; i <= 20; i++) {
            files[i] = Configuration.IMPORTER_FOLDER_FILTERED_DICTS.getFile(Source.DICT_WIKIPEDIA,
                    "output-dict_xtr-result_ddfes-" + i + ".wiki");
        }
        DividedDictFilesSorter sorter = new DividedDictFilesSorter(Language.ZH,
                Configuration.IMPORTER_FOLDER_FILTERED_DICTS.getPath(Source.DICT_WIKIPEDIA), "output-dict_ddfs.wiki",
                files);
        sorter.sort();
    }

    public DividedDictFilesSorter(Language sortLng, String outDir, String outFile, String... inFiles) {
        this.sortLng = sortLng;
        this.outDir = outDir;
        this.outFile = outDir + File.separator + outFile;
        this.inFiles = inFiles;
    }

    public void sort() throws IOException, InterruptedException {
        LinkedList<String> files = new LinkedList<String>(Arrays.asList(this.inFiles));
        LinkedList<String> working = new LinkedList<String>();
        boolean first = true;

        String tmpOutFile = Helper.appendFileName(outFile, "_ddfs-tmp");
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
                    working.add(f);
                    size += s;
                    files.remove(f);
                } else {
                    break;
                }
            }
            if (first) {
                first = false;
            } else {
                working.add(this.outFile);
            }
            DictFilesMergedSorter sorter = new DictFilesMergedSorter(sortLng, this.outDir,
                    new File(tmpOutFile).getName(), true, false, working.toArray(new String[working.size()]));
            sorter.sort();
            new File(this.outFile).delete();
            new File(sorter.outFile).renameTo(new File(this.outFile));
            TimeUnit.SECONDS.sleep(1);
            if (DEBUG) {
                System.out.println("排序临时文件完成：'" + this.outFile + "'（"
                        + Helper.formatSpace(new File(this.outFile).length()) + "）");
            }
        }
        System.out.println("排序完成.输出文件：'" + this.outFile + "'（" + Helper.formatSpace(new File(this.outFile).length())
                + "）");

    }
}
