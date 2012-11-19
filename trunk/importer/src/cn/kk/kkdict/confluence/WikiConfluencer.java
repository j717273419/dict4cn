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
package cn.kk.kkdict.confluence;

public class WikiConfluencer {
  /*
   * public static final String[] INPUT_FILES = { "O:\\wiki\\wiki_zh\\output.txt", "O:\\wiki\\wiki_de\\output.txt" };
   * 
   * public static final String OUT_DIR = "O:\\wiki\\confluence";
   * 
   * public static final String[] IRRELEVANT_WORDS_STRINGS = { "(u.E.)" };
   * 
   * public static final String substringBetween(String text, String start, String end) { int nStart = text.indexOf(start); int nEnd = text.indexOf(end, nStart
   * + 1); if (nStart < nEnd && nStart != -1 && nEnd != -1) { return text.substring(nStart + start.length(), nEnd); } else { return null; } }
   * 
   * public final static String padding(String text, int len, char c) { if (text != null && len > text.length()) { char[] spaces = new char[len -
   * text.length()]; Arrays.fill(spaces, c); return new String(spaces) + text; } else { return text; } }
   * 
   * public final static String padding(long value, int len, char c) { return padding(String.valueOf(value), len, c); }
   * 
   * public final static String formatDuration(long duration) { long v = Math.abs(duration); long days = v / 1000 / 60 / 60 / 24; long hours = (v / 1000 / 60 /
   * 60) % 24; long mins = (v / 1000 / 60) % 60; long secs = (v / 1000) % 60; long millis = v % 1000; StringBuilder out = new StringBuilder(); if (days > 0) {
   * out.append(days).append(':').append(padding(hours, 2, '0')).append(':').append(padding(mins, 2, '0')) .append(":").append(padding(secs, 2,
   * '0')).append(".").append(padding(millis, 3, '0')); } else if (hours > 0) { out.append(hours).append(':').append(padding(mins, 2,
   * '0')).append(":").append(padding(secs, 2, '0')) .append(".").append(padding(millis, 3, '0')); } else if (mins > 0) {
   * out.append(mins).append(":").append(padding(secs, 2, '0')).append(".").append(padding(millis, 3, '0')); } else {
   * out.append(secs).append(".").append(padding(millis, 3, '0')); } return out.toString();
   * 
   * }
   * 
   * private final static String substringBetweenLast(String text, String start, String end) { int nEnd = text.lastIndexOf(end); int nStart = -1; if (nEnd > 1)
   * { nStart = text.lastIndexOf(start, nEnd - 1); } else { return null; } if (nStart < nEnd && nStart != -1 && nEnd != -1) { return text.substring(nStart +
   * start.length(), nEnd); } else { return null; }
   * 
   * }
   * 
   * private static boolean isNotEmptyOrNull(String text) { return text != null && text.length() > 0; }
   * 
   * private static void precheck() { if (!new File(HAN_DE_DICT_UTF8_FILE).isFile()) { System.err.println("Could not read input file: " +
   * HAN_DE_DICT_UTF8_FILE); System.exit(-100); } if (!(new File(OUT_DIR).isDirectory() || new File(OUT_DIR).mkdirs())) {
   * System.err.println("Could not create output directory: " + OUT_DIR); System.exit(-101); } }
   * 
   * public static void main(String args[]) throws IOException, SQLException { long timeStarted = System.currentTimeMillis(); precheck(); BufferedReader reader
   * = new BufferedReader(new FileReader(HAN_DE_DICT_UTF8_FILE), 8192000); BufferedWriter writer = new BufferedWriter(new FileWriter(OUT_DIR + File.separator +
   * "output.txt"), 8192000); BufferedWriter skippedIncompleteWriter = new BufferedWriter(new FileWriter(OUT_DIR + File.separator + "skipped-incomplete.txt"),
   * 8192000);
   * 
   * String line; String name = null; String pinyin; String translation; Set<String> globalCategories = new TreeSet<String>(); Set<String> categories = null;
   * Set<String> descriptions = null; String tmp;
   * 
   * int statSkipped = 0; int statOk = 0; Category[] csValues = Category.values(); String[] cs = new String[csValues.length]; for (int i = 0; i <
   * csValues.length; i++) { Category c = csValues[i]; cs[i] = c.name(); } Arrays.sort(cs); while ((line = reader.readLine()) != null) { if ((tmp =
   * substringBetween(line, " ", " ")) != null) { name = tmp; pinyin = substringBetween(line, "[", "]"); translation = substringBetween(line, "/", "/"); if
   * (isNotEmptyOrNull(name) && isNotEmptyOrNull(pinyin) && isNotEmptyOrNull(translation) && !translation.contains("???")) { categories = new TreeSet<String>();
   * descriptions = new TreeSet<String>(); for (String i : IRRELEVANT_WORDS_STRINGS) { translation = translation.replace(i, Helper.EMPTY_STRING); }
   * 
   * translation = extractCategories(translation, globalCategories, categories, cs, descriptions); translation = extractCategories(translation,
   * globalCategories, categories, cs, descriptions); translation = extractCategories(translation, globalCategories, categories, cs, descriptions); translation
   * = extractCategories(translation, globalCategories, categories, cs, descriptions); translation = extractCategories(translation, globalCategories,
   * categories, cs, descriptions);
   * 
   * translation = translation.replaceAll("[(,]*[ ]*$", Helper.EMPTY_STRING);
   * 
   * for (String d : descriptions) { translation += "(" + d + ")"; }
   * 
   * name = name.trim(); pinyin = pinyin.trim().replaceAll("[0-9] ", "'").replaceAll("[0-9]", Helper.EMPTY_STRING); writer.write(name);
   * writer.write(Helper.SEP_PARTS); writer.write(pinyin); writer.write(Helper.SEP_PARTS); writer.write(translation); writer.write(Helper.SEP_PARTS);
   * writer.write(categories.toString()); writer.write(Helper.SEP_NEWLINE); statOk++; } else { skippedIncompleteWriter.write(line); statSkipped++; }
   * 
   * } else { System.out.println("Skipped line: " + tmp); } } reader.close(); writer.close(); skippedIncompleteWriter.close();
   * 
   * BufferedWriter categoriesWriter = new BufferedWriter(new FileWriter(OUT_DIR + File.separator + "output-categories.txt"), 8192000); for (String c :
   * globalCategories) { categoriesWriter.write(c); categoriesWriter.write(Helper.SEP_NEWLINE); } categoriesWriter.close();
   * System.out.println("\n==============\nExtract HanDeDict Duration: " + formatDuration(System.currentTimeMillis() - timeStarted));
   * System.out.println("Categories: " + globalCategories.size()); System.out.println("OK: " + statOk); System.out.println("SKIPPED: " + statSkipped +
   * "\n==============\n"); }
   */
}
