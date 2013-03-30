package cn.kk.kkdict.extraction.crawl;

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
import java.util.concurrent.atomic.AtomicLong;

import cn.kk.kkdict.Configuration;
import cn.kk.kkdict.Configuration.Source;
import cn.kk.kkdict.types.GoogleLanguage;
import cn.kk.kkdict.utils.Helper;
import cn.kk.kkdict.utils.TranslationHelper;

public class GoogleTranslateCrawler {
  private static final boolean DEBUG            = false;

  public static final String   IN_DIR           = Configuration.IMPORTER_FOLDER_SELECTED_WORDS.getPath(Source.NULL);

  public static final String   OUT_DIR          = Configuration.IMPORTER_FOLDER_EXTRACTED_CRAWLED.getPath(Source.NULL);

  public static final String   OUT_DIR_FINISHED = GoogleTranslateCrawler.OUT_DIR + "/finished";

  /**
   * @param args
   * @throws IOException
   * @throws FileNotFoundException
   */
  public static void main(String[] args) throws FileNotFoundException, IOException {
    final File directory = new File(GoogleTranslateCrawler.IN_DIR + "/words");
    new File(GoogleTranslateCrawler.OUT_DIR_FINISHED).mkdirs();

    if (directory.isDirectory()) {
      System.out.print("搜索中文词组文件'" + GoogleTranslateCrawler.IN_DIR + "' ... ");
      new File(GoogleTranslateCrawler.OUT_DIR).mkdirs();

      final File[] files = directory.listFiles(new FilenameFilter() {
        @Override
        public boolean accept(final File dir, final String name) {
          return (name.contains(".zhdefs.txt"));
        }
      });
      System.out.println(files.length);

      final long start = System.currentTimeMillis();
      final AtomicLong total = new AtomicLong(0);
      Thread[] threads = new Thread[files.length];
      for (int i = 0; i < files.length; i++) {
        final File f = files[i];
        threads[i] = new Thread() {
          @Override
          public void run() {
            System.out.println("读取" + f.getAbsolutePath() + " 。。。");
            GoogleTranslateCrawler crawler = new GoogleTranslateCrawler();
            try {
              total.addAndGet(crawler.crawl(f));
            } catch (IOException e) {
              e.printStackTrace();
            }
          }
        };
        threads[i].start();
        // f.renameTo(new File(OUT_DIR_FINISHED, f.getName()));
      }
      for (int i = 0; i < files.length; i++) {
        try {
          threads[i].join();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
      System.out.println("=====================================");
      System.out.println("总共读取了" + files.length + "个中文词组文件，用时：" + Helper.formatDuration(System.currentTimeMillis() - start));
      System.out.println("总共有效词组：" + total);
      System.out.println("=====================================\n");
    }
  }

  long crawl(File f) throws FileNotFoundException, IOException {
    int startCounter = 0;
    int lineCounter = 0;
    int defCounter = 0;
    try (BufferedReader inStatus = new BufferedReader(new InputStreamReader(new FileInputStream(GoogleTranslateCrawler.OUT_DIR + "/" + f.getName()
        + "_out.txt.status"), Helper.CHARSET_UTF8))) {
      startCounter = Integer.parseInt(inStatus.readLine());
    } catch (Exception e) {
      System.err.println(e);
    }
    System.out.println("从" + startCounter + "行开始");
    try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(f), Helper.CHARSET_UTF8), Helper.BUFFER_SIZE);
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(GoogleTranslateCrawler.OUT_DIR + "/" + f.getName() + "_out.txt",
            startCounter > 0), Helper.CHARSET_UTF8), Helper.BUFFER_SIZE)) {
      String line;
      while (null != (line = in.readLine())) {
        lineCounter++;
        if (lineCounter > startCounter) {
          System.out.print(".");
          if (!Helper.containsAny(line, ',', '.', '，', '。', '？', '?', '!', '(', ')', '（', '）')) {
            String result = GoogleTranslateCrawler.translate(GoogleLanguage.ZH, line);
            if (result != null) {
              out.write(result);
              System.out.print("X");
              defCounter++;
              out.flush();
              this.updateStatus(lineCounter, f);
            }
          }
          if ((lineCounter % 10) == 0) {
            this.updateStatus(lineCounter, f);
          }
        }
      }
    }
    return defCounter;
  }

  private void updateStatus(int lineCounter, File f) throws IOException, FileNotFoundException {
    try (BufferedWriter outStatus = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(GoogleTranslateCrawler.OUT_DIR + "/" + f.getName()
        + "_out.txt.status"), Helper.CHARSET_UTF8))) {
      outStatus.write(String.valueOf(lineCounter));
    }
  }

  private static String translate(GoogleLanguage lng, String text) {
    StringBuilder sb = new StringBuilder();
    sb.append(lng.lng.getKey()).append(Helper.SEP_DEFINITION).append(text);
    final GoogleLanguage[] values = GoogleLanguage.values();
    Arrays.sort(values, new Comparator<GoogleLanguage>() {
      @Override
      public int compare(GoogleLanguage o1, GoogleLanguage o2) {
        return o1.lng.getKey().compareTo(o2.lng.getKey());
      }
    });
    boolean found = false;
    for (GoogleLanguage l : values) {
      if (l != lng) {
        List<String> trls;
        try {
          trls = TranslationHelper.getGoogleTranslations(lng, l, text);

          for (String trl : trls) {
            found = true;
            sb.append(Helper.SEP_LIST);
            sb.append(l.lng.getKey()).append(Helper.SEP_DEFINITION).append(trl);
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
    sb.append(Helper.SEP_NEWLINE);
    if (found) {
      return sb.toString();
    } else {
      return null;
    }
  }

}
