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
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * Tries to find every libzip deflate stream in a test file
 * 
 * @author keke
 * 
 */
public class DeflateReader {
  private static final boolean WRITE_FILE_BLOCKS         = false;
  private static final int[]   DEFLATE_HEADERS           = { 0x789c, 0x78da, 0x7801, 0x785e, 0x78da };
  private static final int[]   DEFLATE_WITH_DICT_HEADERS = { 0x78bb, 0x78f9, 0x783f, 0x787d };
  static {
    Arrays.sort(DeflateReader.DEFLATE_HEADERS);
    Arrays.sort(DeflateReader.DEFLATE_WITH_DICT_HEADERS);
  }

  public static void main(final String[] args) throws IOException {
    final File dflFile = new File("C:\\Program Files (x86)\\Dehelper\\dic\\combined.bin");
    final String outputFileSuffix = ".raw";
    final File outputFileFinal = new File(dflFile.getAbsolutePath() + outputFileSuffix);
    outputFileFinal.delete();

    // read deflate file into byte array
    final ByteBuffer dataRawBytes = DeflateReader.readBytes(dflFile.getAbsolutePath());

    System.out.println("文件: " + dflFile.getAbsolutePath());
    System.out.println("文件大小: " + dataRawBytes.limit() + " B (0x" + Integer.toHexString(dataRawBytes.limit()) + ")");

    final List<String> outputFiles = new ArrayList<>();
    int outPosition = 0;
    while (dataRawBytes.position() < (dataRawBytes.limit() - 2)) {
      try {
        final int position = dataRawBytes.position();
        final String positionHex = "0x" + Integer.toHexString(position);
        final int header = dataRawBytes.getShort() & 0xffff;
        final String headerHex = "0x" + Integer.toHexString(header);
        if (Arrays.binarySearch(DeflateReader.DEFLATE_WITH_DICT_HEADERS, header) >= 0) {
          System.out.println(positionHex + ": 发现可能的压缩文件头代码 '" + headerHex + "'");
        }
        if (Arrays.binarySearch(DeflateReader.DEFLATE_HEADERS, header) >= 0) {
          // final String outputFile = System.getProperty("java.io.tmpdir") + dflFile.getName() + "_" + outputFiles.size() + "." + positionHex +
          // outputFileSuffix;
          final ByteBuffer plainBytes = DeflateReader.decompress(dataRawBytes, position, dataRawBytes.limit() - position);
          System.out.println(positionHex + ": 解压缩：" + plainBytes.limit() + " B (0x" + Integer.toHexString(plainBytes.limit())
              + ")， 头代码：'\" + headerHex + \"' -> 0x" + Integer.toHexString(outPosition));
          if (DeflateReader.WRITE_FILE_BLOCKS) {
            final File outputFile = new File("D:\\" + dflFile.getName() + "_" + outputFiles.size() + "." + positionHex + outputFileSuffix);
            System.out.println(positionHex + ": 发现压缩文件头代码'" + headerHex + "'。尝试解压缩到'" + outputFile + "'.");
            DeflateReader.writeFile(outputFile.getAbsolutePath(), plainBytes, false);
            outputFiles.add(outputFile.getAbsolutePath());
          }
          DeflateReader.writeFile(outputFileFinal.getAbsolutePath(), plainBytes, true);
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
      DeflateReader.writeInputStream(in, out);
    }
  }

  private static ByteBuffer decompress(final ByteBuffer data, final int offset, final int length) throws IOException {
    final Inflater inflater = new Inflater();
    try (final InflaterInputStream in = new InflaterInputStream(new ByteArrayInputStream(data.array(), offset, length), inflater, 1024 * 8);
        final ByteArrayOutputStream dataOut = new ByteArrayOutputStream(1024 * 8);) {
      DeflateReader.writeInputStream(in, dataOut);
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
