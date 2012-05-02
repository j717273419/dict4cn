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
package cn.kk.kkdict.generators;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;

import cn.kk.kkdict.Configuration;
import cn.kk.kkdict.Configuration.Source;
import cn.kk.kkdict.beans.WikiParseStep;
import cn.kk.kkdict.extraction.dict.WikiExtractorBase;
import cn.kk.kkdict.extraction.dict.WikiPagesMetaCurrentExtractor;
import cn.kk.kkdict.utils.ArrayHelper;
import cn.kk.kkdict.utils.Helper;

/**
 * 
 * 
 * @author x_kez
 * 
 */
public class WikiFileTemplateNamesGenerator extends WikiExtractorBase {    
    public static final String IN_DIR = Configuration.IMPORTER_FOLDER_SELECTED_DICTS.getPath(Source.DICT_WIKIPEDIA);

    public static final String OUT_FILE = Configuration.IMPORTER_FOLDER_GENERATED.getFile(Source.NULL,
            "wikipedia_filetemplate.txt");

    private static final byte[] ATTR_FILE_KEY_BYTES = "key=\"6\"".getBytes(Helper.CHARSET_UTF8);;

    public static void main(String args[]) throws IOException {
        WikiFileTemplateNamesGenerator generator = new WikiFileTemplateNamesGenerator();
        File directory = new File(IN_DIR);
        if (directory.isDirectory()) {
            System.out.print("搜索维基百科pages-meta-current.xml文件'" + IN_DIR + "' ... ");

            File[] files = directory.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return (name.endsWith("-pages-meta-current.xml") || name.endsWith("-pages-meta-current.xml.bz2"))
                            && name.contains("wiki-");
                }
            });
            System.out.println(files.length);

            long start = System.currentTimeMillis();
            ArrayHelper.WARN = false;
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(OUT_FILE), Helper.BUFFER_SIZE);
            System.out.println("写出：" + OUT_FILE + " 。。。");

            for (File f : files) {
                generator.extract(f, out);
            }
            out.close();
            ArrayHelper.WARN = true;

            System.out.println("=====================================");
            System.out.println("总共读取了" + files.length + "个wiki文件，用时："
                    + Helper.formatDuration(System.currentTimeMillis() - start));
            System.out.println("=====================================\n");
        }
    }

    private int extract(final File file, BufferedOutputStream out) throws FileNotFoundException, IOException {
        String f = file.getAbsolutePath();
        initialize(f, null, null, null, null, null, null, null, null, null, null);

        while (-1 != (lineLen = ArrayHelper.readLineTrimmed(in, lineBB))) {
            if (WikiParseStep.HEADER == step) {
                if (ArrayHelper.contains(lineArray, 0, lineLen, SUFFIX_NAMESPACES_BYTES)) {
                    break;
                } else if (ArrayHelper.substringBetweenLast(lineArray, 0, lineLen, SUFFIX_XML_TAG_BYTES,
                        SUFFIX_NAMESPACE_BYTES, tmpBB) > 0) {
                    // System.out.println(ArrayHelper.toString(lineBB));
                    if (ArrayHelper.contains(lineArray, 0, lineLen, ATTR_FILE_KEY_BYTES)) {
                        if (DEBUG) {
                            System.out.println("找到域码：" + ArrayHelper.toString(tmpBB));
                        }
                        out.write(fileLngBytes);
                        out.write(Helper.SEP_DEFINITION_BYTES);
                        out.write(tmpArray, 0, tmpBB.limit());
                        out.write(Helper.SEP_NEWLINE_BYTES);
                        break;
                    }
                }
            }
        }
        cleanup();
        return statOk;
    }
}
