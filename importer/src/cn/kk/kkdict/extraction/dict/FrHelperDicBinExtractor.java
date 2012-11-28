package cn.kk.kkdict.extraction.dict;

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

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import cn.kk.kkdict.Configuration;
import cn.kk.kkdict.Configuration.Source;
import cn.kk.kkdict.types.Language;
import cn.kk.kkdict.utils.ChineseHelper;
import cn.kk.kkdict.utils.Helper;

/**
 * Tries to read Dehelper, Frhelper, Eshelper dic/combined.bin files
 * 
 * keywords: Dehlper/Frhelper/Eshelper dictionary extractor/exporter/reader
 * 
 * <pre>
 * BIN file format:
 * Encoding:   UTF-8
 * 0x00000006: {data} zipped data streams address
 * 0x{data} - 2B aligned to 8B - 8B: last zipped data stream address
 * 
 * After zipped data streams
 * 0x0000000: definition mapping data: definition idx (4B), translation idx (8B, from unzipped data), translation length (4B, unzipped data)
 * 
 * </pre>
 */
public class FrHelperDicBinExtractor {
  private static final String REGEX_CLEAR_TYPES         = "[\\[\\]\\.\\(\\)【】]";
  private static final int[]  DEFLATE_HEADERS           = { 0x789c, 0x78da, 0x7801, 0x785e, 0x78da };
  private static final int[]  DEFLATE_WITH_DICT_HEADERS = { 0x78bb, 0x78f9, 0x783f, 0x787d };
  static {
    Arrays.sort(FrHelperDicBinExtractor.DEFLATE_HEADERS);
    Arrays.sort(FrHelperDicBinExtractor.DEFLATE_WITH_DICT_HEADERS);
  }

  public static void main(final String[] args) throws IOException {
    final long startTime = System.currentTimeMillis();
    final File dflFile = new File(Configuration.IMPORTER_FOLDER_RAW_DICTS.getFile(Source.DICT_HELPER, "combined_fr_zh.bin"));
    final File outputFileTranslationUnzipped = new File(
        Configuration.IMPORTER_FOLDER_EXTRACTED_DICTS.getFile(Source.DICT_HELPER, "combined_fr_zh.bin.unzipped"));
    final File outputFileFinalDe = new File(Configuration.IMPORTER_FOLDER_EXTRACTED_DICTS.getFile(Source.DICT_HELPER, "combined_fr.frhelper"));
    final File outputFileFinalZh = new File(Configuration.IMPORTER_FOLDER_EXTRACTED_DICTS.getFile(Source.DICT_HELPER, "combined_zh.frhelper"));
    outputFileTranslationUnzipped.delete();

    final int posWordsIdx = FrHelperDicBinExtractor.extractZippedDataStreams(dflFile, outputFileTranslationUnzipped);
    FrHelperDicBinExtractor.extractDefinitions(dflFile, outputFileTranslationUnzipped, outputFileFinalDe, outputFileFinalZh, posWordsIdx);
    System.out.println("完成时间：" + Helper.formatTime(System.currentTimeMillis()) + "，用时：" + Helper.formatDuration(System.currentTimeMillis() - startTime));
  }

  private static void extractDefinitions(final File dflFile, final File outputFileTranslationUnzipped, final File outputFileFinalDe, File outputFileFinalZh,
      final int posWordsIdx) throws FileNotFoundException, IOException {
    final long lenTrsTotal = outputFileTranslationUnzipped.length();
    System.out.println("词典数据结束位置：0x" + Integer.toHexString(posWordsIdx));
    System.out.println("词典数据解压缩后文件：" + outputFileTranslationUnzipped.getAbsolutePath());
    System.out.println("词典数据解压缩后大小：" + Helper.formatSpace(lenTrsTotal));

    final ByteBuffer mappingRawBytes = FrHelperDicBinExtractor.readBytes(dflFile.getAbsolutePath());
    mappingRawBytes.order(ByteOrder.LITTLE_ENDIAN);
    mappingRawBytes.position(posWordsIdx);

    int counter = 0;
    int idxDefLast = mappingRawBytes.getInt();
    int idxDefDataStart = -1;
    while (true) {
      long idxTrs = mappingRawBytes.getLong();
      int lenTrs = mappingRawBytes.getInt();
      int idxDef = mappingRawBytes.getInt();
      if ((idxTrs > lenTrsTotal) || (idxDef < idxDefLast) || ((idxDef - idxDefLast) > 2000)) {
        idxDefDataStart = mappingRawBytes.position() - 8 - 4;
        break;
      }
      idxDefLast = idxDef;
      counter++;
    }
    System.out.println("词典定义数据开始地址：0x" + Integer.toHexString(idxDefDataStart));
    System.out.println("写入结果文件：" + outputFileFinalDe.getAbsolutePath() + "。。。");
    final ByteBuffer translationRawBytes = FrHelperDicBinExtractor.readBytes(outputFileTranslationUnzipped.getAbsolutePath());
    mappingRawBytes.position(posWordsIdx);
    idxDefLast = mappingRawBytes.getInt();
    final int size = counter;
    counter = 0;

    Set<String> lstCategories = new TreeSet<>();
    Set<String> lstTypes = new TreeSet<>();
    try (BufferedOutputStream out1 = new BufferedOutputStream(new FileOutputStream(outputFileFinalDe));
        BufferedOutputStream out2 = new BufferedOutputStream(new FileOutputStream(outputFileFinalZh))) {
      for (int i = 0; i < size; i++) {
        long idxTrs = mappingRawBytes.getLong();
        int lenTrs = mappingRawBytes.getInt();
        int idxDefStart = mappingRawBytes.getInt();
        final int off = idxDefDataStart + idxDefLast;
        final int len = idxDefStart - idxDefLast;
        String def = new String(mappingRawBytes.array(), off, len);
        String val = new String(translationRawBytes.array(), (int) idxTrs, lenTrs);
        // TODO categories, genders etc.
        def = FrHelperDicBinExtractor.transformDef(def);
        val = FrHelperDicBinExtractor.transformVal(lstCategories, lstTypes, val);
        if (ChineseHelper.containsChinese(def)) {
          out2.write((Language.ZH.getKey() + Helper.SEP_DEFINITION + def).getBytes(Helper.CHARSET_UTF8));
          out2.write((Helper.SEP_LIST + Language.FR.getKey() + Helper.SEP_DEFINITION + val).getBytes(Helper.CHARSET_UTF8));
          out2.write(Helper.SEP_NEWLINE_BYTES);
        } else {
          out1.write((Language.FR.getKey() + Helper.SEP_DEFINITION + def).getBytes(Helper.CHARSET_UTF8));
          out1.write((Helper.SEP_LIST + Language.ZH.getKey() + Helper.SEP_DEFINITION + val).getBytes(Helper.CHARSET_UTF8));
          out1.write(Helper.SEP_NEWLINE_BYTES);
        }

        idxDefLast = idxDefStart;
        counter++;
      }
    } finally {
      System.out.println("结果文件单词总数：" + counter);
    }
    System.out.println("categories: ");
    for (String t : lstCategories) {
      System.out.println(t);
    }
    System.out.println("\ntypes: ");
    for (String t : lstTypes) {
      System.out.println(t);
    }
    System.out.println("结果文件最终大小：" + Helper.formatSpace(outputFileFinalDe.length()));
    System.out.println("结果文件最终大小：" + Helper.formatSpace(outputFileFinalZh.length()));
  }

  private static String transformDef(String def) {
    return def.replace("… ", "…").replace("……", "…").replace("...", "…");
  }

  private static String transformVal(Set<String> lstCategories, Set<String> lstTypes, String val) {
    String v = val;

    String t = Helper.substringBetween(v, "<span class=\"cara\">", "</span>", false);
    if (t != null) {
      lstTypes.add(t.replaceAll(FrHelperDicBinExtractor.REGEX_CLEAR_TYPES, "").trim());
      v = v.replace("<span class=\"cara\">" + t + "</span>", "");
    }
    t = Helper.substringBetween(v, "<font color=\"#0000ff\">", "</font>", false);
    if (t != null) {
      lstTypes.add(t.replaceAll(FrHelperDicBinExtractor.REGEX_CLEAR_TYPES, "").trim());
      v = v.replace("<font color=\"#0000ff\">" + t + "</font>", "");
    }
    t = Helper.substringBetween(v, "<font color=#0000ff>", "</font>", false);
    if (t != null) {
      lstTypes.add(t.replaceAll(FrHelperDicBinExtractor.REGEX_CLEAR_TYPES, "").trim());
      v = v.replace("<font color=#0000ff>" + t + "</font>", "");
    }
    t = Helper.substringBetween(v, "<span class=\"cara\">", "</span>", false);
    if (t != null) {
      lstTypes.add(t.replaceAll(FrHelperDicBinExtractor.REGEX_CLEAR_TYPES, "").trim());
      v = v.replace("<span class=\"cara\">" + t + "</span>", "");
    }
    t = Helper.substringBetween(v, "<span class=cara>", "</span>", false);
    if (t != null) {
      lstTypes.add(t.replaceAll(FrHelperDicBinExtractor.REGEX_CLEAR_TYPES, "").trim());
      v = v.replace("<span class=cara>" + t + "</span>", "");
    }
    t = Helper.substringBetween(v, "<span class=cara>", "</span>", false);
    if (t != null) {
      lstTypes.add(t.replaceAll(FrHelperDicBinExtractor.REGEX_CLEAR_TYPES, "").trim());
      v = v.replace("<span class=cara>" + t + "</span>", "");
    }
    t = Helper.substringBetween(v, "<span class=cara>", "</span>", false);
    if (t != null) {
      lstTypes.add(t.replaceAll(FrHelperDicBinExtractor.REGEX_CLEAR_TYPES, "").trim());
      v = v.replace("<span class=cara>" + t + "</span>", "");
    }
    t = Helper.substringBetween(v, "[", "]", false);
    if (t != null) {
      lstCategories.add(t.replaceAll(FrHelperDicBinExtractor.REGEX_CLEAR_TYPES, "").trim());
      v = v.replace("[" + t + "]", "");
    }
    t = Helper.substringBetween(v, "【", "】", false);
    if (t != null) {
      lstCategories.add(t.replaceAll(FrHelperDicBinExtractor.REGEX_CLEAR_TYPES, "").trim());
      v = v.replace("【" + t + "】", "");
    }
    t = Helper.substringBetween(v, "【", "】", false);
    if (t != null) {
      lstCategories.add(t.replaceAll(FrHelperDicBinExtractor.REGEX_CLEAR_TYPES, "").trim());
      v = v.replace("【" + t + "】", "");
    }
    t = Helper.substringBetween(v, "【", "】", false);
    if (t != null) {
      lstCategories.add(t.replaceAll(FrHelperDicBinExtractor.REGEX_CLEAR_TYPES, "").trim());
      v = v.replace("【" + t + "】", "");
    }
    t = Helper.substringBetweenNarrow(v, "<", ":>");
    if (t != null) {
      lstCategories.add(t.replaceAll(FrHelperDicBinExtractor.REGEX_CLEAR_TYPES, "").trim());
      v = v.replace("<" + t + ":>", "");
    }
    t = Helper.substringBetweenNarrow(v, "<", ":>");
    if (t != null) {
      lstCategories.add(t.replaceAll(FrHelperDicBinExtractor.REGEX_CLEAR_TYPES, "").trim());
      v = v.replace("<" + t + ":>", "");
    }
    if (v.contains("(adj)")) {
      lstTypes.add("adj");
      v = v.replace("(adj)", "");
    }
    if (v.contains("(vt)")) {
      lstTypes.add("vt");
      v = v.replace("(vt)", "");
    }
    if (v.contains("(细)")) {
      lstTypes.add("细");
      v = v.replace("(细)", "");
    }
    if (v.contains("(refl)")) {
      lstTypes.add("refl");
      v = v.replace("(refl)", "");
    }
    if (v.contains("<Geographie>")) {
      lstTypes.add("Geographie");
      v = v.replace("<Geographie>", "");
    }
    v = FrHelperDicBinExtractor.transformDef(v);

    v = v
        .replaceAll(
            "( \\.\\.)|( +- +)|(<span style=\"color:red;\">.+?</span>)|(<span class=\"greytxt\">.+?</span>)|(<span style=\"color:white;font-size:1px\">.+?</span>)|(<div class=\"PY\">.+?</div>)|(<div class=PY>.+?</div>)|(<LJ>.+?</LJ>)|(<FF>.+?</FF>)|(<FE>.+?</FE>)|(\\(如:.+?\\))|[①②③④⑤⑥⑦⑧⑨⑩⑾]|(1\\. )|(2\\. )|(3\\. )|(4\\. )|(,*\\s+$)",
            "");
    v = v.replaceAll("(<BR>)|(<br />)|([,;；，。])", ", ");
    v = Helper.unescapeHtml(v);
    v = Helper.stripHtmlText(v, true);
    v = v.replaceAll("\\s+,\\s+", ", ").replaceAll("(, *)+", ", ").replaceAll("\\s+", " ").replace(" <", "<");

    return v;
  }

  private static int extractZippedDataStreams(final File dflFile, final File outputFileFinal) throws FileNotFoundException, IOException {
    // read deflate file into byte array
    final ByteBuffer dataRawBytes = FrHelperDicBinExtractor.readBytes(dflFile.getAbsolutePath());
    dataRawBytes.order(ByteOrder.LITTLE_ENDIAN);

    System.out.println("词典源文件: " + dflFile.getAbsolutePath());
    System.out.println("文件大小: " + dataRawBytes.limit() + " B (0x" + Integer.toHexString(dataRawBytes.limit()) + ")");

    final int fileType = dataRawBytes.getShort(0);
    final int posDataStart = dataRawBytes.getInt(6);
    final int posDataEnd = dataRawBytes.getInt(((((posDataStart - 2) / 8) * 8) - 8) + 2);
    System.out.println("词典头代码：0x" + Integer.toHexString(fileType) + "，词典数据位置：[0x" + Integer.toHexString(posDataStart) + " - 0x"
        + Integer.toHexString(posDataEnd) + "]");
    dataRawBytes.position(posDataStart);

    dataRawBytes.order(ByteOrder.BIG_ENDIAN);
    int outPosition = 0;
    int counter = 1;
    while (dataRawBytes.position() < (dataRawBytes.limit() - 2)) {
      try {
        final int position = dataRawBytes.position();
        if (position > posDataEnd) {
          break;
        }
        final String positionHex = "0x" + Integer.toHexString(position);
        final int header = dataRawBytes.getShort() & 0xffff;
        final String headerHex = "0x" + Integer.toHexString(header);
        if (Arrays.binarySearch(FrHelperDicBinExtractor.DEFLATE_WITH_DICT_HEADERS, header) >= 0) {
          System.out.println(positionHex + ": 发现可能的压缩文件头代码 '" + headerHex + "'");
        }
        if (Arrays.binarySearch(FrHelperDicBinExtractor.DEFLATE_HEADERS, header) >= 0) {
          final ByteBuffer plainBytes = FrHelperDicBinExtractor.decompress(dataRawBytes, position, dataRawBytes.limit() - position);
          // System.out.println(counter++ + ". " + positionHex + ": 解压缩：" + plainBytes.limit() + " B (0x" + Integer.toHexString(plainBytes.limit()) + ")， 头代码："
          // + headerHex + " -> 0x" + Integer.toHexString(outPosition));
          FrHelperDicBinExtractor.writeFile(outputFileFinal.getAbsolutePath(), plainBytes, true);
          outPosition += plainBytes.array().length;
        }
      } catch (final Throwable e) {
        // System.out.println("分析时出现错误： " + e.toString());
      }
    }

    // System.out.println("写入总文件：'" + outputFileFinal + "'");
    // DeflateReader.writeFile(outputFileFinal, ByteBuffer.wrap(new byte[0]), false);
    // for (final String outputFile : outputFiles) {
    // DeflateReader.writeFile(outputFileFinal, DeflateReader.readBytes(outputFile), true);
    // }
    final int posWordsIdx = dataRawBytes.position();
    return posWordsIdx;
  }

  private static ByteBuffer readBytes(final String dflFile) throws FileNotFoundException, IOException {
    final ByteArrayOutputStream dataOut = new ByteArrayOutputStream();
    try (RandomAccessFile file = new RandomAccessFile(dflFile, "r"); final FileChannel fChannel = file.getChannel()) {
      fChannel.transferTo(0, fChannel.size(), Channels.newChannel(dataOut));
    }

    final ByteBuffer dataRawBytes = ByteBuffer.wrap(dataOut.toByteArray());
    return dataRawBytes;
  }

  private static void writeFile(final String outputFile, final ByteBuffer data, final boolean append) throws IOException {
    try (final ByteArrayInputStream in = new ByteArrayInputStream(data.array()); final FileOutputStream out = new FileOutputStream(outputFile, append);) {
      FrHelperDicBinExtractor.writeInputStream(in, out);
    }
  }

  private static ByteBuffer decompress(final ByteBuffer data, final int offset, final int length) throws IOException {
    final Inflater inflater = new Inflater();
    try (final InflaterInputStream in = new InflaterInputStream(new ByteArrayInputStream(data.array(), offset, length), inflater, 1024 * 8);
        final ByteArrayOutputStream dataOut = new ByteArrayOutputStream(1024 * 8);) {
      FrHelperDicBinExtractor.writeInputStream(in, dataOut);
      data.position(offset + (int) inflater.getBytesRead());
      inflater.end();
      return ByteBuffer.wrap(dataOut.toByteArray());
    }
  }

  private static void writeInputStream(final InputStream in, final OutputStream out) throws IOException {
    final byte[] buffer = new byte[1024 * 8];
    int len;
    while ((len = in.read(buffer)) > 0) {
      out.write(buffer, 0, len);
    }
  }
}
