package cn.kk.kkdict.extraction;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.kk.kkdict.FormattedTreeMap;
import cn.kk.kkdict.Helper;
import cn.kk.kkdict.Stat;

public class PinyinOccurrenceCounter {
    private static final String IN_DIR = "X:\\kkdict\\out\\imedicts";
    private static final String OUT_FILE = "X:\\kkdict\\out\\pinyin\\output.txt";

    public static void main(String args[]) throws IOException {
        File directory = new File(IN_DIR);
        if (directory.isDirectory()) {
            File[] files = directory.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.startsWith("output-") && name.endsWith(".txt");
                }
            });

            Map<String, Integer> statsMap = new FormattedTreeMap<String, Integer>();
            int total = 0;
            for (File f : files) {
                System.out.print("Reading '" + f + " ... ");
                int counter = readPinyinFromFile(f, statsMap);
                System.out.println(counter);                
                total += counter;
            }

            BufferedWriter writer = new BufferedWriter(new FileWriter(OUT_FILE), 8192000);
            List<Stat> list = new ArrayList<Stat>();
            Set<String> keys = statsMap.keySet();
            for (String k : keys) {
                list.add(new Stat(statsMap.get(k), k));
            }
            Collections.sort(list);

            long totalOccurrences = 0L;
            for (Stat s : list) {
                writer.write(s.key);                
                writer.write(Helper.SEP_PARTS);
                writer.write(s.counter.toString());
                writer.write(Helper.SEP_NEWLINE);
                totalOccurrences += s.counter.longValue();
            }

            writer.close();
            System.out.println("\n=====================================");
            System.out.println("Total Completed: " + files.length + " Files");
            System.out.println("Total Words: " + total);
            System.out.println("Total Pinyins: " + list.size());
            System.out.println("Total Occurrences: " + totalOccurrences);
            System.out.println("=====================================");
        }
    }

    private static int readPinyinFromFile(File f, Map<String, Integer> statsMap) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(f), 8192000);
        String line;
        int statOk = 0;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(Helper.SEP_PARTS);
            if (parts.length == 2) {
                String[] py = parts[1].split(Helper.SEP_PY);
                if (py.length > 0) {
                    for (int i = 0; i < py.length; i++) {
                        String pinyin = py[i].trim();
                        Integer count = statsMap.get(pinyin);
                        if (count == null) {
                            statsMap.put(pinyin, Integer.valueOf(py.length - i));
                        } else {
                            statsMap.put(pinyin, Integer.valueOf(py.length - i + count.intValue()));
                        }
                        statOk++;
                    }
                }
            }
        }

        return statOk;
    }
}
