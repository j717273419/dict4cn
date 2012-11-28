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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import cn.kk.kkdict.Configuration;
import cn.kk.kkdict.Configuration.Source;
import cn.kk.kkdict.beans.DictByteBufferRow;
import cn.kk.kkdict.types.ImageLocation;
import cn.kk.kkdict.types.Language;
import cn.kk.kkdict.utils.ArrayHelper;
import cn.kk.kkdict.utils.DictHelper;
import cn.kk.kkdict.utils.Helper;

/**
 * 写出完整的图像连接，过滤不存在的或有版权保护的连接。下载原图片。把wiki图像转换成完整的URL。
 */
public class WikiDictImageLocationCorrector {
  public static final String               IN_DIR                 = Configuration.IMPORTER_FOLDER_EXTRACTED_DICTS.getPath(Source.DICT_WIKIPEDIA);

  public static final String               IN_STATUS              = Configuration.IMPORTER_FOLDER_EXTRACTED_DICTS.getFile(Source.DICT_WIKIPEDIA,
                                                                      "wiki_img_status.txt");

  public static final String               OUT_DIR                = Configuration.IMPORTER_FOLDER_MERGED_DICTS.getPath(Source.DICT_WIKIPEDIA);

  public static final String               OUT_IMG_DIR            = Configuration.IMPORTER_FOLDER_MERGED_DICTS.getPath(Source.DICT_WIKIPEDIA_IMAGES);

  public static final String               OUT_DIR_FINISHED       = WikiDictImageLocationCorrector.OUT_DIR + "/finished";

  private static final byte[][]            NON_FREE_UPPER         = new byte[][] { "{{non-free".toUpperCase().getBytes(Helper.CHARSET_UTF8) };

  private static final byte[][]            NON_FREE_LOWER         = new byte[][] { "{{non-free".getBytes(Helper.CHARSET_UTF8) };

  private static final String              URL_COMMONS_RAW_PREFIX = "http://commons.wikimedia.org/w/index.php?action=raw&title=File:";

  private static final String              URL_IMG_COMMONS_PREFIX = "http://upload.wikimedia.org/wikipedia/commons/";

  private static final String              URL_IMG_LNG_PREFIX     = "http://upload.wikimedia.org/wikipedia/";

  public static final String               SUFFIX_CORRECTED       = "_corrected";

  private static final boolean             DEBUG                  = false;

  private static final Map<String, String> FILETEMPLATE_NAMES     = new HashMap<>();

  private static void createFileTemplateNamesMap() {
    System.out.println("读出wiki-filetemplate名称  ... ");
    final String file = "wikipedia_filetemplate.txt";
    try (final BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(Helper.findResource(file)), Helper.CHARSET_UTF8));) {
      String line;
      while (null != (line = reader.readLine())) {
        final String[] parts = line.split(Helper.SEP_DEFINITION);
        if (parts.length == 2) {
          WikiDictImageLocationCorrector.FILETEMPLATE_NAMES.put(parts[0], parts[1]);
          if (WikiDictImageLocationCorrector.DEBUG) {
            System.out.println(parts[0] + "=" + parts[1]);
          }
        }
      }
    } catch (final IOException e) {
      System.out.println("读出filetemplate名称失败：" + file + "：" + e.toString());
    }
  }

  /**
   * @param args
   * @throws IOException
   */
  public static void main(final String[] args) throws IOException {
    new File(WikiDictImageLocationCorrector.OUT_DIR).mkdirs();
    new File(WikiDictImageLocationCorrector.OUT_DIR_FINISHED).mkdirs();
    final File inDirFile = new File(WikiDictImageLocationCorrector.IN_DIR);
    if (inDirFile.isDirectory()) {
      WikiDictImageLocationCorrector.createFileTemplateNamesMap();
      System.out.print("修复wiki图形连接文件'" + WikiDictImageLocationCorrector.IN_DIR + "' ... ");
      final File[] files = inDirFile.listFiles(new FilenameFilter() {
        @Override
        public boolean accept(final File dir, final String name) {
          return name.startsWith("output-dict_images.");
        }
      });
      System.out.println(files.length);

      final long start = System.currentTimeMillis();
      final String[] filePaths = Helper.getFileNames(files);
      final ByteBuffer lineBB = ArrayHelper.borrowByteBufferSmall();
      final DictByteBufferRow row = new DictByteBufferRow();
      for (final String f : filePaths) {
        final Language lng = DictHelper.getWikiLanguage(f);
        if (lng != null) {
          int skipLines = (int) Helper.readStatsFile(WikiDictImageLocationCorrector.IN_STATUS);
          if (WikiDictImageLocationCorrector.DEBUG) {
            System.out.println("处理图像连接文件：" + f + " [" + skipLines + "]");
          }
          final String outFile = WikiDictImageLocationCorrector.OUT_DIR + File.separator
              + Helper.appendFileName(new File(f).getName(), WikiDictImageLocationCorrector.SUFFIX_CORRECTED);

          final long startFile = System.currentTimeMillis();
          final String fileTemplateName = WikiDictImageLocationCorrector.FILETEMPLATE_NAMES.get(lng.getKey());
          System.out.println("filetemplate前缀：" + fileTemplateName);

          int statValid = 0;
          int statInvalid = 0;
          try (final BufferedInputStream in = new BufferedInputStream(new FileInputStream(f), Helper.BUFFER_SIZE);
              final BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outFile), Helper.BUFFER_SIZE);) {
            int count = skipLines;
            while (-1 != ArrayHelper.readLine(in, lineBB)) {
              if (skipLines == 0) {
                row.parseFrom(lineBB);
                if ((row.size() == 1) && (row.getAttributesSize(0, 0) == 1)) {
                  if (-1 != WikiDictImageLocationCorrector.getCheckedImageLocation(row, fileTemplateName)) {
                    statValid++;
                    out.write(lineBB.array(), 0, lineBB.limit());
                    out.write(Helper.SEP_NEWLINE_CHAR);
                    if (WikiDictImageLocationCorrector.DEBUG) {
                      System.out.println("写入图像连接：" + ArrayHelper.toString(lineBB));
                    }
                  } else {
                    statInvalid++;
                    if (WikiDictImageLocationCorrector.DEBUG) {
                      System.err.println("跳过图像连接：" + ArrayHelper.toString(lineBB));
                    }
                  }
                }
                Helper.writeStatsFile(WikiDictImageLocationCorrector.IN_STATUS, ++count);
              } else {
                skipLines--;
              }
            }
          }
          System.out.println("完成'" + outFile + "'，有效：" + statValid + "，无效：" + statInvalid + "（" + Helper.formatSpace(new File(outFile).length()) + "），用时："
              + Helper.formatDuration(System.currentTimeMillis() - startFile));
          final File file = new File(f);
          file.renameTo(new File(WikiDictImageLocationCorrector.OUT_DIR_FINISHED, file.getName()));
          Helper.writeStatsFile(WikiDictImageLocationCorrector.IN_STATUS, 0L);
        }
      }
      ArrayHelper.giveBack(lineBB);
      System.out.println("修复图像连接文件总共用时：" + Helper.formatDuration(System.currentTimeMillis() - start));
    }
  }

  /**
   * 在bb里给出完整的连接，写出新的bb总长度。如图像连接不存在或有版权保护写出返回值-1。
   * 
   * @param row
   * @param fileTemplateName
   * @return
   * @throws UnsupportedEncodingException
   */
  private static int getCheckedImageLocation(final DictByteBufferRow row, String f) throws UnsupportedEncodingException {
    final String lngName = ArrayHelper.toStringP(row.getLanguage(0));
    String fileTemplateName = f;
    if (fileTemplateName == null) {
      fileTemplateName = "File";
    }

    final ByteBuffer imgBB = row.getAttribute(0, 0, 0);
    final int insertIdx = ArrayHelper.positionP(imgBB, ImageLocation.TYPE_ID_BYTES.length);
    ArrayHelper.replaceP(imgBB, (byte) ' ', (byte) '_');
    final byte hash = ArrayHelper.md5P(imgBB)[0];
    final byte first = ArrayHelper.toHexChar((hash & 0xff) >> 4);
    final byte second = ArrayHelper.toHexChar(hash & 0x0f);

    final String toImgName = URLEncoder.encode(ArrayHelper.toStringP(imgBB), Helper.CHARSET_UTF8.name());
    final String toImgDir = WikiDictImageLocationCorrector.OUT_IMG_DIR + File.separator + (char) first + File.separator + (char) first + (char) second;
    new File(toImgDir).mkdirs();
    final String toImgFile = toImgDir + File.separator + ArrayHelper.toStringP(imgBB);

    // -> 8/8e/Wang_Bo.jpg
    final ByteBuffer tmpBB = ArrayHelper.borrowByteBufferSmall();
    try {
      tmpBB.put(first).put((byte) '/').put(first).put(second).put((byte) '/');
      ArrayHelper.copyP(imgBB, tmpBB);
      // System.out.println(ArrayHelper.toString(tmpBB));
      final File imgFile = new File(toImgFile);
      if (imgFile.isFile() && (imgFile.length() > 0)) {
        tmpBB.rewind();
        ArrayHelper.copyP(tmpBB, imgBB);
        return imgBB.limit();
      } else if (WikiDictImageLocationCorrector.checkFreeLicense(lngName, toImgName, fileTemplateName)) {
        // http://upload.wikimedia.org/wikipedia/commons/8/8e/Wang_Bo.jpg
        boolean success = false;
        try {
          final String url = WikiDictImageLocationCorrector.URL_IMG_COMMONS_PREFIX + (char) first + '/' + (char) first + (char) second + '/' + toImgName;
          Helper.download(url, toImgFile, true);
          success = true;
        } catch (final IOException e) {
          // ignore
        }
        if (!success) {
          // http://upload.wikimedia.org/wikipedia/en/8/8e/Wang_Bo.jpg
          try {
            final String url = WikiDictImageLocationCorrector.URL_IMG_LNG_PREFIX + lngName + '/' + (char) first + '/' + (char) first + (char) second + '/'
                + toImgName;
            Helper.download(url, toImgFile, true);
            success = true;
          } catch (final IOException e) {
            // ignore
          }
        }
        if (success) {
          imgBB.limit(imgBB.capacity()).position(insertIdx);
          tmpBB.rewind();
          ArrayHelper.copyP(tmpBB, imgBB);
          return imgBB.limit();
        }
      }
      return -1;
    } finally {
      ArrayHelper.giveBack(tmpBB);
    }
  }

  private static boolean checkFreeLicense(final String lngName, final String toImgName, final String fileTemplateName) {
    // Wang_Bo.jpg
    // http://en.wikipedia.org/w/index.php?action=raw&title=Main_Page
    boolean free = true;
    final ByteBuffer tmpBB = ArrayHelper.borrowByteBufferSmall();
    try {
      boolean success = false;
      try {
        try (final BufferedInputStream in = new BufferedInputStream(
            Helper.openUrlInputStream(WikiDictImageLocationCorrector.URL_COMMONS_RAW_PREFIX + toImgName));) {
          while (-1 != ArrayHelper.readLine(in, tmpBB)) {
            // System.out.println(ArrayHelper.toString(tmpBB));
            if (-1 != ArrayHelper.indexOfP(tmpBB, WikiDictImageLocationCorrector.NON_FREE_LOWER, WikiDictImageLocationCorrector.NON_FREE_UPPER)) {
              free = false;
              break;
            }
          }
        }
        success = true;
      } catch (final IOException e) {
        if (WikiDictImageLocationCorrector.DEBUG) {
          System.err.println("没找到图像信息：" + e.toString());
        }
      }
      if (!success) {
        try (final BufferedInputStream in = new BufferedInputStream(Helper.openUrlInputStream("http://" + lngName
            + ".wikipedia.org/w/index.php?action=raw&title=" + fileTemplateName + ":" + toImgName));) {
          while (-1 != ArrayHelper.readLine(in, tmpBB)) {
            if (-1 != ArrayHelper.indexOfP(tmpBB, WikiDictImageLocationCorrector.NON_FREE_LOWER, WikiDictImageLocationCorrector.NON_FREE_UPPER)) {
              free = false;
              break;
            }
          }
          in.close();
          success = true;
        } catch (final IOException e) {
          if (WikiDictImageLocationCorrector.DEBUG) {
            System.err.println("没找到图像信息：" + e.toString());
          }
        }
      }
      if (!success) {
        System.err.println("没找到图像信息：" + toImgName);
      }
    } finally {
      ArrayHelper.giveBack(tmpBB);
    }
    if (!free) {
      System.err.println("版权保护：" + toImgName);
    }
    return free;
  }
}
