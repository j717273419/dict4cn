package cn.kk.kkdict.summarization;

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

import cn.kk.kkdict.beans.FormattedTreeMap;
import cn.kk.kkdict.beans.Stat;
import cn.kk.kkdict.utils.Helper;

public class PinyinOccurrenceCounter {
    private static final String IN_DIR = Helper.DIR_OUT_WORDS;
    private static final String OUT_DIR = Helper.DIR_OUT_WORDS;
    private static final String OUT_FILE = OUT_DIR + "\\output-occurrences.pinyin";

    public static void main(String args[]) throws IOException {
        File directory = new File(IN_DIR);
        if (directory.isDirectory()) {
            new File(OUT_DIR).mkdirs();
            System.out.print("搜索词组文件'" + IN_DIR + "' ... ");

            File[] files = directory.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.startsWith("output-words.");
                }
            });
            
            System.out.println(files.length);
            
            Map<String, Integer> statsMap = new FormattedTreeMap<String, Integer>();
            int total = 0;
            for (File f : files) {
                System.out.print("正在读取词组文件'" + f + " 。。。");
                int counter = readPinyinFromFile(f, statsMap);
                System.out.println(counter);
                total += counter;
            }

            BufferedWriter writer = new BufferedWriter(new FileWriter(OUT_FILE), Helper.BUFFER_SIZE);
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
            System.out.println("总共读取词语文件：" + files.length);
            System.out.println("词语数目：" + total);
            System.out.println("拼音总数：" + list.size());
            System.out.println("拼音出现次数：" + totalOccurrences);
            System.out.println("=====================================");
        }
    }

    private static int readPinyinFromFile(File f, Map<String, Integer> statsMap) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(f), Helper.BUFFER_SIZE);
        String line;
        int statOk = 0;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(Helper.SEP_PARTS);
            if (parts.length == 2) {
                String[] py = parts[1].split(Helper.SEP_PINYIN);
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
