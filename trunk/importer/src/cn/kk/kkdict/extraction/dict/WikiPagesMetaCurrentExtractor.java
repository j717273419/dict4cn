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
package cn.kk.kkdict.extraction.dict;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;

import cn.kk.kkdict.Configuration;
import cn.kk.kkdict.Configuration.Source;
import cn.kk.kkdict.beans.WikiParseStep;
import cn.kk.kkdict.utils.ArrayHelper;
import cn.kk.kkdict.utils.Helper;

/**
 * 
 * 用时：中文~10分钟（全），8分钟（无图片链接，坐标） TODO: categories
 */
public class WikiPagesMetaCurrentExtractor extends WikiExtractorBase {

    public static final String IN_DIR = Configuration.IMPORTER_FOLDER_SELECTED_DICTS.getPath(Source.DICT_WIKIPEDIA);

    public static final String OUT_DIR = Configuration.IMPORTER_FOLDER_EXTRACTED_DICTS.getPath(Source.DICT_WIKIPEDIA);

    public static final String OUT_DIR_FINISHED = OUT_DIR + "/finished";

    public static void main(String args[]) throws IOException {
        WikiPagesMetaCurrentExtractor extractor = new WikiPagesMetaCurrentExtractor();
        File directory = new File(IN_DIR);
        new File(OUT_DIR_FINISHED).mkdirs();

        if (directory.isDirectory()) {
            System.out.print("搜索维基百科pages-meta-current.xml文件'" + IN_DIR + "' ... ");
            new File(OUT_DIR).mkdirs();

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
            long total = 0;
            for (File f : files) {
                total += extractor.extractWikipediaPagesMetaCurrent(f);
                f.renameTo(new File(OUT_DIR_FINISHED, f.getName()));
            }
            ArrayHelper.WARN = true;

            System.out.println("=====================================");
            System.out.println("总共读取了" + files.length + "个wiki文件，用时："
                    + Helper.formatDuration(System.currentTimeMillis() - start));
            System.out.println("总共有效词组：" + total);
            System.out.println("=====================================\n");
        }
    }

    private int extractWikipediaPagesMetaCurrent(final File file) throws FileNotFoundException, IOException {
        String f = file.getAbsolutePath();
        initialize(f, OUT_DIR, "output-dict.wiki_", "output-dict_categories.wiki_", "output-dict_related.wiki_",
                "output-dict_abstracts.wiki_", "output-dict_redirects.wiki_", "output-dict_images.wiki_",
                "output-dict_coords.wiki_", "output-dict_src.wiki_", "output-dict_attrs.wiki_");
        // initialize(f, OUT_DIR, "output-dict.wiki_", null, "output-dict_related.wiki_", "output-dict_abstracts.wiki_",
        // "output-dict_redirects.wiki_", "output-dict_images.wiki_", "output-dict_coords.wiki_",
        // "output-dict_src.wiki_", null);
        // initialize(f, OUT_DIR, "output-dict.wiki_", null, null, null, null, null, null);

        while (-1 != (lineLen = ArrayHelper.readLineTrimmed(in, lineBB))) {
            if (++lineCount % OK_NOTICE == 0) {
                signal();
            }
            if (WikiParseStep.HEADER == step) {
                parseDocumentHeader();
            } else if (WikiParseStep.BEFORE_TITLE == step) {
                // System.out.println(ArrayHelper.toString(lineBB));
                if (lineLen > MIN_TITLE_LINE_BYTES
                        && ArrayHelper.substringBetween(lineArray, 0, lineLen, PREFIX_TITLE_BYTES, SUFFIX_TITLE_BYTES,
                                tmpBB) > 0) {
                    // new title found
                    // write old definition
                    writeDefinition();
                    handleContentTitle();
                }
            } else if (step == WikiParseStep.TITLE_FOUND) {
                int idx;
                if (-1 != (idx = ArrayHelper.indexOf(lineArray, 0, lineLen, TAG_TEXT_BEGIN_BYTES))) {
                    handleTextBeginLine(idx);
                    if (step == WikiParseStep.PAGE && lineLen > 14 && lineArray[0] == '#') {
                        checkRedirectLine();
                    }
                } else if (lineLen > MIN_REDIRECT_LINE_BYTES
                        && ArrayHelper.substringBetween(lineArray, 0, lineLen, TAG_REDIRECT_BEGIN_BYTES,
                                SUFFIX_REDIRECT_BYTES, tmpBB) > 0) {
                    writeRedirectLine();
                }
            }
            if (step == WikiParseStep.PAGE) {
                int idx;
                if (-1 != (idx = ArrayHelper.indexOf(lineArray, 0, lineLen, TAG_TEXT_END_BYTES))) {
                    handleTextEndLine(idx);
                }
                // within content
                if (lineLen > minCatBytes
                        && (ArrayHelper.substringBetween(lineArray, 0, lineLen, catKeyBytes, SUFFIX_WIKI_TAG_BYTES,
                                tmpBB) > 0 || ArrayHelper.substringBetween(lineArray, 0, lineLen, catKeyBytes2,
                                SUFFIX_WIKI_TAG_BYTES, tmpBB) > 0)) {
                    // new category found for current name
                    addCategory();
                } else if (ArrayHelper.substringBetween(lineArray, 0, lineLen, PREFIX_WIKI_TAG_BYTES,
                        SUFFIX_WIKI_TAG_BYTES, tmpBB) > 0) {
                    // found wiki tag [[...]]
                    idx = ArrayHelper.indexOf(tmpBB, (byte) ':');
                    if (idx > 0 && idx < 13) {
                        // has : in tag, perhaps translation
                        addTranslation(idx);
                    } else if (idx == -1 && !catName) {
                        // something else
                        if (-1 != (idx = ArrayHelper.indexOf(lineArray, 0, lineLen, tmpArray, 0, tmpBB.limit()))
                                && idx < 6) {
                            // tag at beginning of line, perhaps related word
                            addRelated();
                        }
                    }
                }
                if (parseAbstract) {
                    parseAbstract();
                }
            }
        }

        // write last definition
        writeDefinition();
        cleanup();
        return statOk;
    }
}
