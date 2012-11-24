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
package cn.kk.kkdict;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import cn.kk.kkdict.utils.Helper;

public enum Configuration {
  IMPORTER_FOLDER_GENERATED("importer.folder.generated"),
  IMPORTER_FOLDER_RAW_DICTS("importer.folder.raw.dicts"),
  IMPORTER_FOLDER_RAW_WORDS("importer.folder.raw.words"),
  IMPORTER_FOLDER_SELECTED_WORDS("importer.folder.selected.words"),
  IMPORTER_FOLDER_SELECTED_DICTS("importer.folder.selected.dicts"),
  IMPORTER_FOLDER_EXTRACTED_WORDS("importer.folder.extracted.words"),
  IMPORTER_FOLDER_EXTRACTED_DICTS("importer.folder.extracted.dicts"),
  IMPORTER_FOLDER_EXTRACTED_CRAWLED("importer.folder.extracted.crawled"),
  IMPORTER_FOLDER_MERGED_WORDS("importer.folder.merged.words"),
  IMPORTER_FOLDER_MERGED_DICTS("importer.folder.merged.dicts"),
  IMPORTER_FOLDER_FILTERED_CRAWLED("importer.folder.filtered.crawled"),
  IMPORTER_FOLDER_FILTERED_DICTS("importer.folder.filtered.dicts"),
  IMPORTER_FOLDER_SUMMARIZED_CRAWLED("importer.folder.summarized.crawled"),
  IMPORTER_FOLDER_SUMMARIZED_DICTS("importer.folder.summarized.dicts"),
  IMPORTER_FOLDER_REVIEWED_CRAWLED("importer.folder.reviewed.crawled"),
  IMPORTER_FOLDER_REVIEWED_DICTS("importer.folder.reviewed.dicts"),
  IMPORTER_FOLDER_CONFLUENCED("importer.folder.confluenced"),
  IMPORTER_FOLDER_INDEXED("importer.folder.indexed"),
  IMPORTER_FOLDER_EXPORT("importer.folder.export"), ;
  private static final String PREFIX_WORD    = "WORD";
  private static final String SUFFIX_WORDS   = "WORDS";
  private static final String PREFIX_DICT    = "DICT";
  private static final String SUFFIX_DICTS   = "DICTS";
  private static final String SUFFIX_CRAWLED = "CRAWLED";

  public static enum Source {
    DICT_WIKIPEDIA("wikipedia"),
    DICT_WIKTIONARY("wiktionary"),
    DICT_LINGOES("lingoes"),
    DICT_EDICT("edict"),
    DICT_HELPER("helper"),
    DICT_STARDICT("stardict"),
    WORD_BAIDU("baidu"),
    WORD_QQ("qq"),
    WORD_SOGOU("sogou"),
    NULL(""),
    DICT_WIKIPEDIA_IMAGES("wikipedia.images"),
    WORD_TERMWIKI("termwiki"),
    WORD_BABLA("babla");
    public final String          name;
    public final static Source[] DICTS;
    public final static Source[] WORDS;

    static {
      int dicts = 0;
      int words = 0;
      for (final Source src : Source.values()) {
        if (src.name().startsWith(Configuration.PREFIX_DICT)) {
          dicts++;
        } else if (src.name().startsWith(Configuration.PREFIX_WORD)) {
          words++;
        }
      }
      DICTS = new Source[dicts];
      WORDS = new Source[words];
      for (final Source src : Source.values()) {
        if (src.name().startsWith(Configuration.PREFIX_DICT)) {
          Source.DICTS[--dicts] = src;
        } else if (src.name().startsWith(Configuration.PREFIX_WORD)) {
          Source.WORDS[--words] = src;
        }
      }
    }

    Source(final String name) {
      this.name = name;
    }
  }

  private final static Properties CFG = new Properties();
  static {
    try {
      Configuration.CFG.load(new InputStreamReader(new FileInputStream(Helper.findResource("config.properties")), Helper.CHARSET_UTF8));
    } catch (final Exception e) {
      e.printStackTrace();
    }
  }

  private final String            key;

  Configuration(final String key) {
    this.key = key;
  }

  public static final String getPath(final Configuration cfg, final Configuration.Source src) {
    String k;
    if (src == Source.NULL) {
      k = cfg.key;
    } else {
      k = cfg.key + "." + src.name;
    }
    String tmp = Configuration.CFG.getProperty(k);
    String p = null;
    if (tmp != null) {
      p = tmp.replace('\\', '/');
    }
    int idx;
    while (((p == null) || !new File(p).isAbsolute()) && ((idx = k.lastIndexOf('.')) != -1)) {
      k = k.substring(0, idx);
      tmp = Configuration.CFG.getProperty(k);
      if (tmp != null) {
        if (p == null) {
          p = tmp.replace('\\', '/');
        } else {
          p = tmp.replace('\\', '/') + "/" + p;
        }
      }
    }
    return p;
  }

  public static final void makeDirectories() {
    for (final Configuration cfg : Configuration.values()) {
      for (final Source src : Source.values()) {
        if ((cfg.name().endsWith(Configuration.SUFFIX_DICTS) && src.name().startsWith(Configuration.PREFIX_DICT))
            || ((cfg.name().endsWith(Configuration.SUFFIX_WORDS) || cfg.name().endsWith(Configuration.SUFFIX_CRAWLED)) && src.name().startsWith(
                Configuration.PREFIX_WORD))) {
          final String f = Configuration.getPath(cfg, src);
          // System.out.println(f);
          if (f != null) {
            final File file = new File(f);
            if (!file.exists()) {
              System.out.println("创建文件夹：" + file.getAbsolutePath());
              file.mkdirs();
            }
          }
        }
      }
    }
  }

  public String getFile(final Source src, final String f) {
    Configuration.check();
    return Configuration.getPath(this, src) + '/' + f;
  }

  private final static void check() {
    if (Configuration.first) {
      Configuration.first = false;
      Configuration.makeDirectories();
    }
  }

  public String getPath(final Source src) {
    Configuration.check();
    return Configuration.getPath(this, src);
  }

  private static boolean first = true;
}
