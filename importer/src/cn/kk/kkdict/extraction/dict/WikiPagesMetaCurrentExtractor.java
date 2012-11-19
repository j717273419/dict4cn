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
import cn.kk.kkdict.types.WordType;
import cn.kk.kkdict.utils.ArrayHelper;
import cn.kk.kkdict.utils.Helper;

/**
 * 
 * 用时：中文~10分钟（全），8分钟（无图片链接，坐标） TODO: categories
 */
public class WikiPagesMetaCurrentExtractor extends WikiExtractorBase {

  public static final String IN_DIR           = Configuration.IMPORTER_FOLDER_SELECTED_DICTS.getPath(Source.DICT_WIKIPEDIA);

  public static final String OUT_DIR          = Configuration.IMPORTER_FOLDER_EXTRACTED_DICTS.getPath(Source.DICT_WIKIPEDIA);

  public static final String OUT_DIR_FINISHED = WikiPagesMetaCurrentExtractor.OUT_DIR + "/finished";

  public static void main(final String args[]) throws IOException {
    final WikiPagesMetaCurrentExtractor extractor = new WikiPagesMetaCurrentExtractor();
    final File directory = new File(WikiPagesMetaCurrentExtractor.IN_DIR);
    new File(WikiPagesMetaCurrentExtractor.OUT_DIR_FINISHED).mkdirs();

    if (directory.isDirectory()) {
      System.out.print("搜索维基百科pages-meta-current.xml文件'" + WikiPagesMetaCurrentExtractor.IN_DIR + "' ... ");
      new File(WikiPagesMetaCurrentExtractor.OUT_DIR).mkdirs();

      final File[] files = directory.listFiles(new FilenameFilter() {
        @Override
        public boolean accept(final File dir, final String name) {
          return (name.endsWith("-pages-meta-current.xml") || name.endsWith("-pages-meta-current.xml.bz2")) && name.contains("wiki-");
        }
      });
      System.out.println(files.length);

      final long start = System.currentTimeMillis();
      ArrayHelper.WARN = false;
      long total = 0;
      for (final File f : files) {
        total += extractor.extractWikipediaPagesMetaCurrent(f);
        f.renameTo(new File(WikiPagesMetaCurrentExtractor.OUT_DIR_FINISHED, f.getName()));
      }
      ArrayHelper.WARN = true;

      System.out.println("=====================================");
      System.out.println("总共读取了" + files.length + "个wiki文件，用时：" + Helper.formatDuration(System.currentTimeMillis() - start));
      System.out.println("总共有效词组：" + total);
      System.out.println("=====================================\n");
    }
  }

  private int extractWikipediaPagesMetaCurrent(final File file) throws FileNotFoundException, IOException {
    final String f = file.getAbsolutePath();
    this.initialize(f, WikiPagesMetaCurrentExtractor.OUT_DIR, "output-dict.wiki_", "output-dict_categories.wiki_", "output-dict_related.wiki_",
        "output-dict_abstracts.wiki_", "output-dict_redirects.wiki_", "output-dict_images.wiki_", "output-dict_coords.wiki_", "output-dict_src.wiki_",
        "output-dict_attrs.wiki_");
    // initialize(f, OUT_DIR, "output-dict.wiki_", null, "output-dict_related.wiki_", "output-dict_abstracts.wiki_",
    // "output-dict_redirects.wiki_", "output-dict_images.wiki_", "output-dict_coords.wiki_",
    // "output-dict_src.wiki_", null);
    // initialize(f, OUT_DIR, "output-dict.wiki_", null, null, null, null, null, null);

    while (-1 != (this.lineLen = ArrayHelper.readLineTrimmed(this.in, this.lineBB))) {
      if ((++this.lineCount % WikiExtractorBase.OK_NOTICE) == 0) {
        this.signal();
      }
      if (WikiParseStep.HEADER == this.step) {
        this.parseDocumentHeader();
      } else if (WikiParseStep.BEFORE_TITLE == this.step) {
        // System.out.println(ArrayHelper.toString(lineBB));
        if ((this.lineLen > WikiExtractorBase.MIN_TITLE_LINE_BYTES)
            && (ArrayHelper.substringBetween(this.lineArray, 0, this.lineLen, WikiExtractorBase.PREFIX_TITLE_BYTES, WikiExtractorBase.SUFFIX_TITLE_BYTES,
                this.tmpBB) > 0)) {
          // new title found
          // write old definition
          this.writeDefinition();
          this.handleContentTitle();
          this.wordType = WordType.NOUN;
        }
      } else if (this.step == WikiParseStep.TITLE_FOUND) {
        int idx;
        if (-1 != (idx = ArrayHelper.indexOf(this.lineArray, 0, this.lineLen, WikiExtractorBase.TAG_TEXT_BEGIN_BYTES))) {
          this.handleTextBeginLine(idx);
          if ((this.step == WikiParseStep.PAGE) && (this.lineLen > 14) && (this.lineArray[0] == '#')) {
            this.checkRedirectLine();
          }
        } else if ((this.lineLen > WikiExtractorBase.MIN_REDIRECT_LINE_BYTES)
            && (ArrayHelper.substringBetween(this.lineArray, 0, this.lineLen, WikiExtractorBase.TAG_REDIRECT_BEGIN_BYTES,
                WikiExtractorBase.SUFFIX_REDIRECT_BYTES, this.tmpBB) > 0)) {
          this.writeRedirectLine();
        }
      }
      if (this.step == WikiParseStep.PAGE) {
        int idx;
        if (-1 != (idx = ArrayHelper.indexOf(this.lineArray, 0, this.lineLen, WikiExtractorBase.TAG_TEXT_END_BYTES))) {
          this.handleTextEndLine(idx);
        }
        // within content
        if ((this.lineLen > this.minCatBytes)
            && ((ArrayHelper.substringBetween(this.lineArray, 0, this.lineLen, this.catKeyBytes, WikiExtractorBase.SUFFIX_WIKI_TAG_BYTES, this.tmpBB) > 0) || (ArrayHelper
                .substringBetween(this.lineArray, 0, this.lineLen, this.catKeyBytes2, WikiExtractorBase.SUFFIX_WIKI_TAG_BYTES, this.tmpBB) > 0))) {
          // new category found for current name
          this.addCategory();
        } else if (ArrayHelper.substringBetween(this.lineArray, 0, this.lineLen, WikiExtractorBase.PREFIX_WIKI_TAG_BYTES,
            WikiExtractorBase.SUFFIX_WIKI_TAG_BYTES, this.tmpBB) > 0) {
          // found wiki tag [[...]]
          idx = ArrayHelper.indexOf(this.tmpBB, (byte) ':');
          if ((idx > 0) && (idx < 13)) {
            // has : in tag, perhaps translation
            this.addTranslation(idx);
          } else if ((idx == -1) && !this.catName) {
            // something else
            if ((-1 != (idx = ArrayHelper.indexOf(this.lineArray, 0, this.lineLen, this.tmpArray, 0, this.tmpBB.limit()))) && (idx < 6)) {
              // tag at beginning of line, perhaps related word
              this.addRelated();
            }
          }
        }
        if (this.parseAbstract) {
          this.parseAbstract();
        }
      }
    }

    // write last definition
    this.writeDefinition();
    this.cleanup();
    return this.statOk;
  }

}
