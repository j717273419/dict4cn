package cn.kk.kkdict.extraction.crawl;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.apache.tools.bzip2.CBZip2InputStream;

import cn.kk.kkdict.Configuration;
import cn.kk.kkdict.Configuration.Source;
import cn.kk.kkdict.extraction.dict.WiktionaryPagesMetaCurrentExtractor;
import cn.kk.kkdict.extraction.dict.WiktionaryPagesMetaCurrentExtractor.ParseInfo;
import cn.kk.kkdict.types.GoogleLanguage;
import cn.kk.kkdict.utils.ArrayHelper;
import cn.kk.kkdict.utils.Helper;
import cn.kk.kkdict.utils.TranslationHelper;

public class GoogleTranslateCrawler
{
  private static final boolean DEBUG = false;

  public static final String IN_DIR = Configuration.IMPORTER_FOLDER_SELECTED_WORDS.getPath(Source.NULL);

  public static final String OUT_DIR = Configuration.IMPORTER_FOLDER_EXTRACTED_CRAWLED.getPath(Source.NULL);

  public static final String OUT_DIR_FINISHED = OUT_DIR + "/finished";


  /**
   * @param args
   * @throws IOException
   * @throws FileNotFoundException
   */
  public static void main(String[] args) throws FileNotFoundException, IOException
  {
    final File directory = new File(IN_DIR);
    new File(OUT_DIR_FINISHED).mkdirs();

    if (directory.isDirectory())
    {
      System.out.print("搜索中文词组文件'" + IN_DIR + "' ... ");
      new File(OUT_DIR).mkdirs();

      final File[] files = directory.listFiles(new FilenameFilter()
      {
        @Override
        public boolean accept(final File dir, final String name)
        {
          return (name.endsWith(".zhdefs.txt"));
        }
      });
      System.out.println(files.length);

      GoogleTranslateCrawler crawler = new GoogleTranslateCrawler();
      final long start = System.currentTimeMillis();
      ArrayHelper.WARN = false;
      long total = 0;
      for (final File f : files)
      {
        total += crawler.crawl(f);
        // f.renameTo(new File(OUT_DIR_FINISHED, f.getName()));
      }
      ArrayHelper.WARN = true;

      System.out.println("=====================================");
      System.out.println("总共读取了" + files.length + "个中文词组文件，用时："
          + Helper.formatDuration(System.currentTimeMillis() - start));
      System.out.println("总共有效词组：" + total);
      System.out.println("=====================================\n");
    }
  }


  private long crawl(File f) throws FileNotFoundException, IOException
  {
    int defCounter = 0;
    try (BufferedReader in =
        new BufferedReader(new InputStreamReader(new FileInputStream(f), Helper.CHARSET_UTF8), Helper.BUFFER_SIZE);
        BufferedWriter out =
            new BufferedWriter(new OutputStreamWriter(new FileOutputStream(OUT_DIR + "/" + f.getName() + "_out.txt"),
                Helper.CHARSET_UTF8), Helper.BUFFER_SIZE))
    {
      String line;
      while (null != (line = in.readLine()))
      {
        String result = translate(GoogleLanguage.ZH, line);
        if (result != null)
        {
          out.write(result);
        }
      }
    }
    return defCounter;
  }


  private static String translate(GoogleLanguage lng, String text)
  {
    StringBuilder sb = new StringBuilder();
    sb.append(lng.lng.getKey()).append(Helper.SEP_DEFINITION).append(text);
    final GoogleLanguage[] values = GoogleLanguage.values();
    Arrays.sort(values, new Comparator<GoogleLanguage>()
    {
      @Override
      public int compare(GoogleLanguage o1, GoogleLanguage o2)
      {
        return o1.lng.getKey().compareTo(o2.lng.getKey());
      }
    });
    boolean found = false;
    for (GoogleLanguage l : values)
    {
      if (l != lng)
      {
        List<String> trls = TranslationHelper.getGoogleTranslations(lng, l, text);
        for (String trl : trls)
        {
          found = true;
          sb.append(Helper.SEP_LIST);
          sb.append(l.lng.getKey()).append(Helper.SEP_DEFINITION).append(trl);
        }
      }
    }
    sb.append(Helper.SEP_NEWLINE);
    if (found)
    {
      return sb.toString();
    } else
    {
      return null;
    }
  }

}
