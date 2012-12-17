import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import cn.kk.kkdict.utils.Helper;

public class TextFileSplitter {

  /**
   * @param args
   */
  public static void main(String[] args) {
    final int num = 10;
    final String file = "D:\\kkdict\\selected\\words\\words\\words.zhdefs.txt";
    int linesCounter = 0;
    try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), Helper.CHARSET_UTF8))) {
      String line;
      while (null != (line = in.readLine())) {
        linesCounter++;
      }
    } catch (Exception e) {
    }
    final int linesPerFile = linesCounter / num;
    int fileNum = 1;
    try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), Helper.CHARSET_UTF8));) {
      BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file + "_" + fileNum++), Helper.CHARSET_UTF8), Helper.BUFFER_SIZE);
      String line;
      while (null != (line = in.readLine())) {
        linesCounter++;
        out.write(line);
        out.write('\n');
        if (((linesCounter % linesPerFile) == 0) && (fileNum <= num)) {
          out.close();
          out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file + "_" + fileNum++), Helper.CHARSET_UTF8), Helper.BUFFER_SIZE);
        }
      }
      out.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
