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
import cn.kk.kkdict.utils.PhoneticTranscriptionHelper;

public class PinyinIndexGenerator {
  private static final String      OUT_FILE   = Configuration.IMPORTER_FOLDER_MERGED_WORDS.getFile(Source.NULL, "output-summarized.pinyin");
  private static final String      IN_FILE    = Configuration.IMPORTER_FOLDER_MERGED_WORDS.getFile(Source.NULL, "output-occurrences.pinyin");

  public static final String[][][] SIMILARS   = {
      { { "en", "eng" }, { "in", "ing" } },
      { { "z", "c", "s" }, { "zh", "ch", "sh" }, { "b", "p" }, { "n", "m" }, { "f", "w" }, { "g", "k" }, { "l", "r" }, { "x", "q", "j" }, { "d", "t" } },
      { { "l", "n", "r" }, { "f", "h" }, { "z", "zh" }, { "c", "ch" }, { "s", "sh" } },
      { { "iang", "ang" }, { "uan", "uang" }, { "o", "ou", "uo" }, { "i", "ui", "ei" }, { "e", "ie" }, { "v", "ue" }, { "ong", "iong" } },
      { { "x", "q", "j", "k", "g" }, { "f", "w", "l", "r", "m", "n", "h" } },
      { { "ian", "iang" }, { "an", "ang" }, { "o", "ou", "uo", "iu" }, { "i", "ui", }, { "ei", "e", "ie" } },
      { { "c", "d", "b", "g", "ch", "j", "k", "p", "q", "s", "t", "sh", "zh", "x", "z" }, { "f", "h", "l", "m", "n", "", "r", "w", "y" } },
      { { "c", "d", "b", "g", "ch", "j", "k", "p", "q", "s", "t", "sh", "zh", "x", "z", "f", "h", "l", "m", "n", "", "r", "w", "y" } },
      { { "a", "ong", "iong", "uang", "iang", "ang", "iao", "uai", "ai", "an", "ao", "ua", "ia" }, { "u", "un", "uan", "uo", "v", "o" },
      { "e", "eng", "ian", "ing", "en", "er", "ie", "ei", }, { "i", "in", "iu", "ou", "ue", "ui", } },
      { { "c", "d", "b", "g", "ch", "j", "k", "p", "q", "s", "t", "sh", "zh", "x", "z", "f", "h", "l", "m", "n", "", "r", "w", "y" } } };

  private static final int         MAX_AMOUNT = 250;

  public static void main(final String args[]) throws IOException {
    LinkedList<ListStat> combinations = new LinkedList<>();
    try (final BufferedReader reader = new BufferedReader(new FileReader(PinyinIndexGenerator.IN_FILE));) {
      String line;

      while ((line = reader.readLine()) != null) {
        final String[] parts = line.split(Helper.SEP_PARTS);
        if (parts.length == 2) {
          combinations.add(new ListStat(Integer.parseInt(parts[1].trim()), parts[0].trim()));
        }
      }
    }
    Collections.sort(combinations);
    System.out.println("Total " + combinations.size() + " combinations of pinyin found.");

    double percentile = 0.6;
    for (; percentile > 0; percentile -= 0.005) {
      final LinkedList<ListStat> clone = PinyinIndexGenerator.clone(combinations);
      if (PinyinIndexGenerator.trySummarize(clone, percentile)) {
        combinations = clone;
        break;
      }
    }

    try (final BufferedWriter writer = new BufferedWriter(new FileWriter(PinyinIndexGenerator.OUT_FILE), Helper.BUFFER_SIZE);) {
      int i = 1;
      for (final ListStat s : combinations) {
        writer.write(s.getCounter());
        writer.write(Helper.SEP_PARTS);
        writer.write(s.getValues().toString());
        writer.write(Helper.SEP_NEWLINE);

        for (String str : s.getValues()) {
          System.out.println("{ \"" + str + "\".toCharArray(), new char[] { " + i + " } },");
        }
        i++;
      }
    }

    System.out.println("\n\n=====================================");
    System.out.println("Number after summarization: " + combinations.size());
    System.out.println("Marker used: " + (Math.round(percentile * 100d) / 100d));
    System.out.println("=====================================");

  }

  private static boolean trySummarize(final LinkedList<ListStat> combinations, final double percentile) {
    for (final String[][] similarFinders : PinyinIndexGenerator.SIMILARS) {
      for (final String[] similars : similarFinders) {
        final LinkedList<ListStat> clone = PinyinIndexGenerator.clone(combinations);
        final List<Integer> changes = PinyinIndexGenerator.replaceSimilars(clone, similars);
        PinyinIndexGenerator.summarizeSimilars(combinations, clone, changes, percentile);
        Collections.sort(combinations);
        if (combinations.size() < PinyinIndexGenerator.MAX_AMOUNT) {
          return true;
        }
      }
    }
    return false;
  }

  private static boolean summarizeSimilars(final LinkedList<ListStat> combinations, final LinkedList<ListStat> clone, final List<Integer> changes,
      final double percentile) {
    final int marker = PinyinIndexGenerator.findMarker(combinations, percentile);
    final int size = changes.size();
    Collections.sort(changes);
    boolean found = false;
    for (int i = size - 1; i > 0; i--) {
      final int iValue = changes.get(i).intValue();
      final ListStat target = clone.get(iValue);
      final List<String> tValues = target.getValues();

      FOUND: for (int j = i - 1; j >= 0; j--) {
        final int jValue = changes.get(j).intValue();
        final ListStat source = clone.get(jValue);
        final List<String> sValues = source.getValues();
        if ((source.getCounter() + target.getCounter()) > marker) {
          break;
        }
        for (final String o : tValues) {
          for (final String v : sValues) {
            if (o.equals(v)) {
              final ListStat removed = combinations.remove(iValue);
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

  private static LinkedList<ListStat> clone(final LinkedList<ListStat> combinations) {
    final LinkedList<ListStat> clone = new LinkedList<>();
    for (final ListStat s : combinations) {
      clone.add(new ListStat(s.getCounter(), new FormattedArrayList<>(s.getValues())));
    }
    return clone;
  }

  private static List<Integer> replaceSimilars(final LinkedList<ListStat> combinations, final String[] similars) {
    final List<Integer> changes = new FormattedArrayList<>();
    for (int i = 0; i < combinations.size(); i++) {
      final ListStat stat = combinations.get(i);
      final List<String> words = stat.getValues();
      for (int j = 0; j < words.size(); j++) {
        final String v = words.get(j);
        final String[] parts = PhoneticTranscriptionHelper.getShenMuYunMu(v);
        for (final String similar : similars) {
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
            final Integer iInteger = Integer.valueOf(i);
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

  private static int findMarker(final LinkedList<ListStat> combinations, final double percentile) {
    final int idx = (int) (combinations.size() * percentile);
    if (idx > 0) {
      return combinations.get(idx).getCounter();
    } else {
      return combinations.getFirst().getCounter();
    }
  }
}
