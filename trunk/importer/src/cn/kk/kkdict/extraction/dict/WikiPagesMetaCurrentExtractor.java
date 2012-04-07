package cn.kk.kkdict.extraction.dict;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;

import cn.kk.kkdict.beans.WikiParseStep;
import cn.kk.kkdict.utils.ArrayHelper;
import cn.kk.kkdict.utils.Helper;

public class WikiPagesMetaCurrentExtractor extends WikiExtractorBase {

    public static final String IN_DIR = Helper.DIR_IN_DICTS + "\\wiki";

    public static final String OUT_DIR = Helper.DIR_OUT_DICTS + "\\wiki";

    public static void main(String args[]) throws IOException {
        WikiPagesMetaCurrentExtractor extractor = new WikiPagesMetaCurrentExtractor();
        File directory = new File(IN_DIR);
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

            ArrayHelper.WARN = false;
            long total = 0;
            for (File f : files) {
                total += extractor.extractWikipediaPagesMetaCurrent(f);
            }
            ArrayHelper.WARN = true;

            System.out.println("=====================================");
            System.out.println("总共读取了" + files.length + "个wiki文件");
            System.out.println("有效词组：" + total);
            System.out.println("=====================================\n");
        }
    }

    private int extractWikipediaPagesMetaCurrent(final File file) throws FileNotFoundException, IOException {
        String f = file.getAbsolutePath();
        initialize(f, OUT_DIR, "output-dict.wiki_", "output-dict_categories.wiki_", "output-dict_related.wiki_");

        while (-1 != (len = ArrayHelper.readLine(in, lineBB))) {
            signal();
            if (WikiParseStep.HEADER == step) {
                parseHeader();
            } else {
                if (ArrayHelper.substringBetween(lineBBArray, 0, len, PREFIX_TITLE_BYTES, SUFFIX_TITLE_BYTES, tmpBB) > 0) {
                    // new title found
                    // write old definition
                    writeDefinition();
                    handleContentTitle();
                } else if (isValid()) {
                    // within content
                    if (ArrayHelper.substringBetween(lineBBArray, 0, len, categoryKeyBytes, SUFFIX_WIKI_TAG_BYTES,
                            tmpBB) > 0
                            || ArrayHelper.substringBetween(lineBBArray, 0, len, categoryKeyBytes2,
                                    SUFFIX_WIKI_TAG_BYTES, tmpBB) > 0) {
                        // new category found for current name
                        addCategory();
                    } else if (ArrayHelper.substringBetween(lineBBArray, 0, len, PREFIX_WIKI_TAG_BYTES,
                            SUFFIX_WIKI_TAG_BYTES, tmpBB) > 0) {
                        // found wiki tag
                        int idx = ArrayHelper.indexOf(tmpBB, (byte) ':');
                        if (idx > 0 && idx < 13) {
                            // has : in tag, perhaps translation
                            addTranslation(idx);
                        } else if (idx == -1 && !isCategoryName) {
                            // something else
                            if (-1 != (idx = ArrayHelper.indexOf(lineBBArray, 0, len, tmpBBArray, 0, tmpBB.limit()))
                                    && idx < 6) {
                                // tag at beginning of line, perhaps related word
                                addRelated();
                            }
                        }
                    }
                }
            }
        }
        // write last definition
        writeDefinition();
        cleanup();
        return statOk;
    }
}
