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
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

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
public class HelperDicBinReader {
  private static final int[] DEFLATE_HEADERS           = { 0x789c, 0x78da, 0x7801, 0x785e, 0x78da };
  private static final int[] DEFLATE_WITH_DICT_HEADERS = { 0x78bb, 0x78f9, 0x783f, 0x787d };
  static {
    Arrays.sort(HelperDicBinReader.DEFLATE_HEADERS);
    Arrays.sort(HelperDicBinReader.DEFLATE_WITH_DICT_HEADERS);
  }

  public static void main(final String[] args) throws IOException {
    final File dflFile = new File("C:\\Program Files (x86)\\Frhelper\\dic\\combined.bin");
    // final File dflFile = new File("C:\\Program Files (x86)\\Eshelper\\dic\\combined.bin");
    // final File dflFile = new File("C:\\Program Files (x86)\\Dehelper\\dic\\combined.bin");
    final File outputFileTranslationUnzipped = new File(dflFile.getAbsolutePath() + ".unzipped");
    final File outputFileFinal = new File(dflFile.getAbsolutePath() + "-result.txt");
    outputFileTranslationUnzipped.delete();

    final int posWordsIdx = HelperDicBinReader.extractZippedDataStreams(dflFile, outputFileTranslationUnzipped);
    HelperDicBinReader.extractDefinitions(dflFile, outputFileTranslationUnzipped, outputFileFinal, posWordsIdx);
    System.out.println("完成：" + Helper.formatTime(System.currentTimeMillis()));
  }

  private static void extractDefinitions(final File dflFile, final File outputFileTranslationUnzipped, final File outputFileFinal, final int posWordsIdx)
      throws FileNotFoundException, IOException {
    final long lenTrsTotal = outputFileTranslationUnzipped.length();
    System.out.println("词典数据结束位置：0x" + Integer.toHexString(posWordsIdx));
    System.out.println("词典数据解压缩后文件：" + outputFileTranslationUnzipped.getAbsolutePath());
    System.out.println("词典数据解压缩后大小：" + Helper.formatSpace(lenTrsTotal));

    final ByteBuffer mappingRawBytes = HelperDicBinReader.readBytes(dflFile.getAbsolutePath());
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
    System.out.println("写入结果文件：" + outputFileFinal.getAbsolutePath() + "。。。");
    final ByteBuffer translationRawBytes = HelperDicBinReader.readBytes(outputFileTranslationUnzipped.getAbsolutePath());
    mappingRawBytes.position(posWordsIdx);
    idxDefLast = mappingRawBytes.getInt();
    final int size = counter;
    counter = 0;
    try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outputFileFinal))) {
      for (int i = 0; i < size; i++) {
        long idxTrs = mappingRawBytes.getLong();
        int lenTrs = mappingRawBytes.getInt();
        int idxDefStart = mappingRawBytes.getInt();
        final int off = idxDefDataStart + idxDefLast;
        final int len = idxDefStart - idxDefLast;
        out.write(mappingRawBytes.array(), off, len);
        out.write(" = ".getBytes(Helper.CHARSET_UTF8));
        out.write(translationRawBytes.array(), (int) idxTrs, lenTrs);
        out.write(Helper.SEP_NEWLINE_BYTES);
        idxDefLast = idxDefStart;
        counter++;
      }
    } finally {
      System.out.println("结果文件单词总数：" + counter);
    }
    System.out.println("结果文件最终大小：" + Helper.formatSpace(outputFileFinal.length()) + "。。。");
  }

  private static int extractZippedDataStreams(final File dflFile, final File outputFileFinal) throws FileNotFoundException, IOException {
    // read deflate file into byte array
    final ByteBuffer dataRawBytes = HelperDicBinReader.readBytes(dflFile.getAbsolutePath());
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
        if (Arrays.binarySearch(HelperDicBinReader.DEFLATE_WITH_DICT_HEADERS, header) >= 0) {
          System.out.println(positionHex + ": 发现可能的压缩文件头代码 '" + headerHex + "'");
        }
        if (Arrays.binarySearch(HelperDicBinReader.DEFLATE_HEADERS, header) >= 0) {
          final ByteBuffer plainBytes = HelperDicBinReader.decompress(dataRawBytes, position, dataRawBytes.limit() - position);
          // System.out.println(counter++ + ". " + positionHex + ": 解压缩：" + plainBytes.limit() + " B (0x" + Integer.toHexString(plainBytes.limit()) + ")， 头代码："
          // + headerHex + " -> 0x" + Integer.toHexString(outPosition));
          HelperDicBinReader.writeFile(outputFileFinal.getAbsolutePath(), plainBytes, true);
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
      HelperDicBinReader.writeInputStream(in, out);
    }
  }

  private static ByteBuffer decompress(final ByteBuffer data, final int offset, final int length) throws IOException {
    final Inflater inflater = new Inflater();
    try (final InflaterInputStream in = new InflaterInputStream(new ByteArrayInputStream(data.array(), offset, length), inflater, 1024 * 8);
        final ByteArrayOutputStream dataOut = new ByteArrayOutputStream(1024 * 8);) {
      HelperDicBinReader.writeInputStream(in, dataOut);
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
