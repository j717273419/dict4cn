package cn.kk.kkdict.summarization;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import cn.kk.kkdict.utils.Helper;
import cn.kk.kkdict.utils.PinyinHelper;

public class ChineseConvertionGenerator {
    public static void main(String[] args) throws IOException {
        String res = "simple2traditional-source.txt";
        printSortConverterMap(res);
    }

    protected static void printSortConverterMap(String res) {
        try {
            int[] simple = null;
            int[] traditional = null;
            BufferedReader reader = new BufferedReader(new InputStreamReader(PinyinHelper.class.getResourceAsStream("/"
                    + res)));
            String line;
            while (null != (line = reader.readLine())) {
                String[] parts = line.split(Helper.SEP_PARTS);
                if (parts.length == 2) {
                    String map = parts[1];
                    int length = map.length();
                    if (parts[0].equals("simple")) {
                        simple = new int[length];
                        for (int i = 0; i < length; i++) {
                            simple[i] = map.codePointAt(i);
                        }
                    } else if (parts[0].equals("traditional")) {
                        traditional = new int[length];
                        for (int i = 0; i < length; i++) {
                            traditional[i] = map.codePointAt(i);
                        }
                    }
                }
            }
            reader.close();

            if (simple.length != traditional.length) {
                System.err.println("文件损坏：" + res + "！");
                return;
            }
            Map<Integer, Integer> simpleMap = new TreeMap<Integer, Integer>();
            for (int i = 0; i < simple.length; i++) {
                simpleMap.put(Integer.valueOf(simple[i]), Integer.valueOf(traditional[i]));
            }
            System.out.println("简繁转换：");
            Set<Integer> keys = simpleMap.keySet();
            System.out.print("sortedSimple" + Helper.SEP_PARTS);
            for (Integer i : keys) {
                System.out.print(Character.toChars(i));
            }
            System.out.println();
            System.out.print("sortedTraditional" + Helper.SEP_PARTS);
            for (Integer i : keys) {
                System.out.print(Character.toChars(simpleMap.get(i).intValue()));
            }
            System.out.println();

            Map<Integer, Integer> traditionalMap = new TreeMap<Integer, Integer>();
            for (int i = 0; i < traditional.length; i++) {
                traditionalMap.put(Integer.valueOf(traditional[i]), Integer.valueOf(simple[i]));
            }
            System.out.println("繁简转换：");
            keys = traditionalMap.keySet();
            System.out.print("sortedTraditional" + Helper.SEP_PARTS);
            for (Integer i : keys) {
                System.out.print(Character.toChars(i));
            }
            System.out.println();
            System.out.print("sortedSimple" + Helper.SEP_PARTS);
            for (Integer i : keys) {
                System.out.print(Character.toChars(traditionalMap.get(i).intValue()));
            }
            System.out.println();
        } catch (IOException e) {
            System.out.println("文件不可读：" + res + "！");
        }
    }
}
