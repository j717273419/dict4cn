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
package cn.kk.kkdict.summarization;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import cn.kk.kkdict.Configuration;
import cn.kk.kkdict.Configuration.Source;
import cn.kk.kkdict.beans.FormattedArrayList;
import cn.kk.kkdict.beans.ListStat;
import cn.kk.kkdict.utils.Helper;
import cn.kk.kkdict.utils.PinyinHelper;

public class PinyinIndexGenerator {
    private static final String OUT_FILE = Configuration.IMPORTER_FOLDER_MERGED_WORDS.getFile(Source.NULL,
            "output-summarized.pinyin");
    private static final String IN_FILE = Configuration.IMPORTER_FOLDER_MERGED_WORDS.getFile(Source.NULL,
            "output-occurrences.pinyin");

    public static final String[][][] SIMILARS = {
            { { "en", "eng" }, { "in", "ing" } },
            { { "z", "c", "s" }, { "zh", "ch", "sh" }, { "b", "p" }, { "n", "m" }, { "f", "w" }, { "g", "k" },
                    { "l", "r" }, { "x", "q", "j" }, { "d", "t" } },
            { { "l", "n", "r" }, { "f", "h" }, { "z", "zh" }, { "c", "ch" }, { "s", "sh" } },
            { { "iang", "ang" }, { "uan", "uang" }, { "o", "ou", "uo" }, { "i", "ui", "ei" }, { "e", "ie" },
                    { "v", "ue" }, { "ong", "iong" } },
            { { "x", "q", "j", "k", "g" }, { "f", "w", "l", "r", "m", "n", "h" } },
            { { "ian", "iang" }, { "an", "ang" }, { "o", "ou", "uo", "iu" }, { "i", "ui", }, { "ei", "e", "ie" } },
            { { "c", "d", "b", "g", "ch", "j", "k", "p", "q", "s", "t", "sh", "zh", "x", "z" },
                    { "f", "h", "l", "m", "n", "", "r", "w", "y" } },
            { { "c", "d", "b", "g", "ch", "j", "k", "p", "q", "s", "t", "sh", "zh", "x", "z", "f", "h", "l", "m", "n",
                    "", "r", "w", "y" } },
            { { "a", "ong", "iong", "uang", "iang", "ang", "iao", "uai", "ai", "an", "ao", "ua", "ia" },
                    { "u", "un", "uan", "uo", "v", "o" }, { "e", "eng", "ian", "ing", "en", "er", "ie", "ei", },
                    { "i", "in", "iu", "ou", "ue", "ui", } },
            { { "c", "d", "b", "g", "ch", "j", "k", "p", "q", "s", "t", "sh", "zh", "x", "z", "f", "h", "l", "m", "n",
                    "", "r", "w", "y" } } };

    private static final int MAX_AMOUNT = 250;

    public static void main(String args[]) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(IN_FILE));
        String line;
        LinkedList<ListStat> combinations = new LinkedList<ListStat>();
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(Helper.SEP_PARTS);
            if (parts.length == 2) {
                combinations.add(new ListStat(Integer.parseInt(parts[1].trim()), parts[0].trim()));
            }
        }
        Collections.sort(combinations);
        System.out.println("Total " + combinations.size() + " combinations of pinyin found.");

        double percentile = 0.6;
        for (; percentile > 0; percentile -= 0.005) {
            LinkedList<ListStat> clone = clone(combinations);
            if (trySummarize(clone, percentile)) {
                combinations = clone;
                break;
            }
        }

        BufferedWriter writer = new BufferedWriter(new FileWriter(OUT_FILE), Helper.BUFFER_SIZE);
        for (ListStat s : combinations) {
            writer.write(s.getCounter());
            writer.write(Helper.SEP_PARTS);
            writer.write(s.getValues().toString());
            writer.write(Helper.SEP_NEWLINE);
        }
        writer.close();

        System.out.println("\n\n=====================================");
        System.out.println("Number after summarization: " + combinations.size());
        System.out.println("Marker used: " + Math.round(percentile * 100d) / 100d);
        System.out.println("=====================================");

    }

    private static boolean trySummarize(LinkedList<ListStat> combinations, double percentile) {
        for (String[][] similarFinders : SIMILARS) {
            for (String[] similars : similarFinders) {
                LinkedList<ListStat> clone = clone(combinations);
                List<Integer> changes = replaceSimilars(clone, similars);
                summarizeSimilars(combinations, clone, changes, percentile);
                Collections.sort(combinations);
                if (combinations.size() < MAX_AMOUNT) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean summarizeSimilars(LinkedList<ListStat> combinations, LinkedList<ListStat> clone,
            List<Integer> changes, double percentile) {
        int marker = findMarker(combinations, percentile);
        int size = changes.size();
        Collections.sort(changes);
        boolean found = false;
        for (int i = size - 1; i > 0; i--) {
            int iValue = changes.get(i).intValue();
            ListStat target = clone.get(iValue);
            List<String> tValues = target.getValues();

            FOUND: for (int j = i - 1; j >= 0; j--) {
                int jValue = changes.get(j).intValue();
                ListStat source = clone.get(jValue);
                List<String> sValues = source.getValues();
                if (source.getCounter() + target.getCounter() > marker) {
                    break;
                }
                for (String o : tValues) {
                    for (String v : sValues) {
                        if (o.equals(v)) {
                            ListStat removed = combinations.remove(iValue);
                            combinations.get(jValue).add(removed);
                            found = true;
                            break FOUND;
                        }
                    }
                }
            }
        }
        return found;
    }

    private static LinkedList<ListStat> clone(LinkedList<ListStat> combinations) {
        LinkedList<ListStat> clone = new LinkedList<ListStat>();
        for (ListStat s : combinations) {
            clone.add(new ListStat(s.getCounter(), new FormattedArrayList<String>(s.getValues())));
        }
        return clone;
    }

    private static List<Integer> replaceSimilars(LinkedList<ListStat> combinations, String[] similars) {
        List<Integer> changes = new FormattedArrayList<Integer>();
        for (int i = 0; i < combinations.size(); i++) {
            ListStat stat = combinations.get(i);
            List<String> words = stat.getValues();
            for (int j = 0; j < words.size(); j++) {
                String v = words.get(j);
                String[] parts = PinyinHelper.getShenMuYunMu(v);
                for (String similar : similars) {
                    boolean found = false;
                    if (similar.equals(parts[0])) {
                        parts[0] = similars[0];
                        found = true;
                    } else if (similar.equals(parts[1])) {
                        parts[1] = similars[0];
                        found = true;
                    }
                    if (found) {
                        words.set(j, parts[0] + parts[1]);
                        Integer iInteger = Integer.valueOf(i);
                        if (!changes.contains(iInteger)) {
                            changes.add(iInteger);
                        }
                        break;
                    }
                }
            }
        }
        return changes;
    }

    private static int findMarker(LinkedList<ListStat> combinations, double percentile) {
        int idx = (int) (combinations.size() * percentile);
        if (idx > 0) {
            return combinations.get(idx).getCounter();
        } else {
            return combinations.getFirst().getCounter();
        }
    }
}
