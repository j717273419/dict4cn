import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import cn.kk.kkdict.utils.Helper;

public class LongLinesRemover {
  static final int max = 120;

  /**
   * @param args
   */
  public static void main(String[] args) {
    final String file = "D:\\kkdict\\out\\lingoes\\output-Quick Japanese-Chinese Dictionary_ja_zh.ld2.lingoes_ld2";
    LongLinesRemover.remove(file);
  }

  private static void remove(String file) {
    try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), Helper.CHARSET_UTF8));
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file + "_llremoved"), Helper.CHARSET_UTF8), Helper.BUFFER_SIZE);) {
      String line;
      int total = 0;
      int removed = 0;
      while (null != (line = in.readLine())) {
        total++;
        if (line.length() < LongLinesRemover.max) {
          out.write(line);
          out.write('\n');
        } else {
          removed++;
        }
      }
      System.out.println("总：" + total + ", 删：" + removed);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
