package cn.kk.kkdict.generators;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import cn.kk.kkdict.utils.Helper;
import cn.kk.kkdict.utils.PhoneticTranscriptionHelper;

public class ChineseConvertionGenerator {
  public static void main(final String[] args) {
    final String res = "simple2traditional-source.txt";
    ChineseConvertionGenerator.printSortConverterMap(res);
  }

  protected static void printSortConverterMap(final String res) {

    int[] simple = null;
    int[] traditional = null;
    try (final BufferedReader reader = new BufferedReader(new InputStreamReader(PhoneticTranscriptionHelper.class.getResourceAsStream("/" + res)));) {
      String line;
      while (null != (line = reader.readLine())) {
        final String[] parts = line.split(Helper.SEP_PARTS);
        if (parts.length == 2) {
          final String map = parts[1];
          final int length = map.length();
          if (parts[0].equals("simple")) {
            simple = new int[length];
            for (int i = 0; i < length; i++) {
              simple[i] = map.codePointAt(i);
              if (map.substring(i, i).equals(new String(Character.toChars(simple[i])))) {
                System.err.println(map.substring(i, i + 1) + "->" + new String(Character.toChars(simple[i])));
              }
            }
          } else if (parts[0].equals("traditional")) {
            traditional = new int[length];
            for (int i = 0; i < length; i++) {
              traditional[i] = map.codePointAt(i);
            }
          }
        }
      }

      if ((simple == null) || (traditional == null) || (simple.length != traditional.length)) {
        System.err.println("文件损坏：" + res + "！");
        return;
      }
      final Map<Integer, Integer> simpleMap = new TreeMap<>();
      for (int i = 0; i < simple.length; i++) {
        simpleMap.put(Integer.valueOf(simple[i]), Integer.valueOf(traditional[i]));
      }
      System.out.println("简繁转换：");
      Set<Integer> keys = simpleMap.keySet();
      System.out.print("sortedSimple" + Helper.SEP_PARTS);
      for (final Integer i : keys) {
        System.out.print(Character.toChars(i.intValue()));
      }
      System.out.println();
      System.out.print("sortedTraditional" + Helper.SEP_PARTS);
      for (final Integer i : keys) {
        System.out.print(Character.toChars(simpleMap.get(i).intValue()));
      }
      System.out.println();

      final Map<Integer, Integer> traditionalMap = new TreeMap<>();
      for (int i = 0; i < traditional.length; i++) {
        traditionalMap.put(Integer.valueOf(traditional[i]), Integer.valueOf(simple[i]));
      }
      System.out.println("繁简转换：");
      keys = traditionalMap.keySet();
      System.out.print("sortedTraditional" + Helper.SEP_PARTS);
      for (final Integer i : keys) {
        System.out.print(Character.toChars(i.intValue()));
      }
      System.out.println();
      System.out.print("sortedSimple" + Helper.SEP_PARTS);
      for (final Integer i : keys) {
        System.out.print(Character.toChars(traditionalMap.get(i).intValue()));
      }
      System.out.println();
    } catch (final IOException e) {
      System.out.println("文件不可读：" + res + "！");
    }
  }
}
