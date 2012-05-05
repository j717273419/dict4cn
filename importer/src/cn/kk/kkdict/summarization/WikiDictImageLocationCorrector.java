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
    public static final String IN_DIR = Configuration.IMPORTER_FOLDER_EXTRACTED_DICTS.getPath(Source.DICT_WIKIPEDIA);
    public static final String OUT_DIR = Configuration.IMPORTER_FOLDER_MERGED_DICTS.getPath(Source.DICT_WIKIPEDIA);
    public static final String OUT_IMG_DIR = Configuration.IMPORTER_FOLDER_MERGED_DICTS
            .getPath(Source.DICT_WIKIPEDIA_IMAGES);
    public static final String OUT_DIR_FINISHED = OUT_DIR + "/finished";

    private static final byte[][] NON_FREE_UPPER = new byte[][] { "{{non-free".toUpperCase().getBytes(
            Helper.CHARSET_UTF8) };
    private static final byte[][] NON_FREE_LOWER = new byte[][] { "{{non-free".getBytes(Helper.CHARSET_UTF8) };
    private static final String URL_COMMONS_RAW_PREFIX = "http://commons.wikimedia.org/w/index.php?action=raw&title=File:";
    private static final String URL_IMG_COMMONS_PREFIX = "http://upload.wikimedia.org/wikipedia/commons/";
    private static final String URL_IMG_LNG_PREFIX = "http://upload.wikimedia.org/wikipedia/";
    public static final String SUFFIX_CORRECTED = "_corrected";
    private static final boolean DEBUG = false;
    private static final boolean TRACE = false;
    private static final Map<String, String> FILETEMPLATE_NAMES = new HashMap<String, String>();

    private static void createFileTemplateNamesMap() {
        System.out.println("读出wiki-filetemplate名称  ... ");
        final String file = "wikipedia_filetemplate.txt";
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(
                    Helper.findResource(file)), Helper.CHARSET_UTF8));
            String line;
            while (null != (line = reader.readLine())) {
                String[] parts = line.split(Helper.SEP_DEFINITION);
                if (parts.length == 2) {
                    FILETEMPLATE_NAMES.put(parts[0], parts[1]);
                    if (DEBUG) {
                        System.out.println(parts[0] + "=" + parts[1]);
                    }
                }
            }
            reader.close();
        } catch (IOException e) {
            System.out.println("读出filetemplate名称失败：" + file + "：" + e.toString());
        }
    }

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        new File(OUT_DIR).mkdirs();
        File inDirFile = new File(IN_DIR);
        if (inDirFile.isDirectory()) {
            createFileTemplateNamesMap();
            System.out.print("修复wiki图形连接文件'" + IN_DIR + "' ... ");
            File[] files = inDirFile.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.startsWith("output-dict_images.");
                }
            });
            System.out.println(files.length);

            long start = System.currentTimeMillis();
            String[] filePaths = Helper.getFileNames(files);
            ByteBuffer lineBB = ArrayHelper.borrowByteBufferSmall();
            DictByteBufferRow row = new DictByteBufferRow();
            for (String f : filePaths) {
                if (DEBUG) {
                    System.out.println("处理连接文件：" + f);
                }
                String outFile = OUT_DIR + File.separator
                        + Helper.appendFileName(new File(f).getName(), SUFFIX_CORRECTED);
                Language lng = DictHelper.getWikiLanguage(f);
                if (lng != null) {
                    long startFile = System.currentTimeMillis();
                    final String fileTemplateName = FILETEMPLATE_NAMES.get(lng.key);
                    System.out.println("filetemplate前缀：" + fileTemplateName);

                    BufferedInputStream in = new BufferedInputStream(new FileInputStream(f), Helper.BUFFER_SIZE);
                    BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outFile),
                            Helper.BUFFER_SIZE);
                    int statValid = 0;
                    int statInvalid = 0;
                    while (-1 != ArrayHelper.readLine(in, lineBB)) {
                        row.parseFrom(lineBB);
                        if (row.size() == 1 && row.getAttributesSize(0, 0) == 1) {
                            if (-1 != getCheckedImageLocation(row, fileTemplateName)) {
                                statValid++;
                                out.write(lineBB.array(), 0, lineBB.limit());
                                out.write(Helper.SEP_NEWLINE_CHAR);
                                if (DEBUG) {
                                    System.out.println("写入图像连接：" + ArrayHelper.toString(lineBB));
                                }
                            } else {
                                statInvalid++;
                                if (DEBUG) {
                                    System.err.println("跳过图像连接：" + ArrayHelper.toString(lineBB));
                                }
                            }
                        }
                    }
                    out.close();
                    in.close();
                    System.out.println("完成'" + outFile + "'，有效：" + statValid + "，无效：" + statInvalid + "（"
                            + Helper.formatSpace(new File(outFile).length()) + "），用时："
                            + Helper.formatDuration(System.currentTimeMillis() - startFile));
                    File file = new File(f);
                    file.renameTo(new File(OUT_DIR_FINISHED, file.getName()));
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
    private static int getCheckedImageLocation(DictByteBufferRow row, String fileTemplateName)
            throws UnsupportedEncodingException {
        final String lngName = ArrayHelper.toStringP(row.getLanguage(0));
        if (fileTemplateName == null) {
            fileTemplateName = "File";
        }

        ByteBuffer imgBB = row.getAttribute(0, 0, 0);
        final int insertIdx = ArrayHelper.positionP(imgBB, ImageLocation.TYPE_ID_BYTES.length);
        ArrayHelper.replaceP(imgBB, (byte) ' ', (byte) '_');
        byte hash = ArrayHelper.md5P(imgBB)[0];
        byte first = ArrayHelper.toHexChar((hash & 0xff) >> 4);
        byte second = ArrayHelper.toHexChar(hash & 0x0f);

        final String toImgName = URLEncoder.encode(ArrayHelper.toStringP(imgBB), Helper.CHARSET_UTF8.name());
        final String toImgDir = OUT_IMG_DIR + File.separator + (char) first + File.separator + (char) first
                + (char) second;
        new File(toImgDir).mkdirs();
        final String toImgFile = toImgDir + File.separator + ArrayHelper.toStringP(imgBB);

        // -> 8/8e/Wang_Bo.jpg
        ByteBuffer tmpBB = ArrayHelper.borrowByteBufferSmall();
        try {
            tmpBB.put(first).put((byte) '/').put(first).put(second).put((byte) '/');
            ArrayHelper.copyP(imgBB, tmpBB);
            // System.out.println(ArrayHelper.toString(tmpBB));
            final File imgFile = new File(toImgFile);
            if (imgFile.isFile() && imgFile.length() > 0) {
                tmpBB.rewind();
                ArrayHelper.copyP(tmpBB, imgBB);
                return imgBB.limit();
            } else if (checkFreeLicense(lngName, toImgName, fileTemplateName)) {
                // http://upload.wikimedia.org/wikipedia/commons/8/8e/Wang_Bo.jpg
                boolean success = false;
                try {
                    String url = URL_IMG_COMMONS_PREFIX + (char) first + '/' + (char) first + (char) second + '/'
                            + toImgName;
                    Helper.download(url, toImgFile, true);
                    success = true;
                } catch (IOException e) {
                    // ignore
                }
                if (!success) {
                    // http://upload.wikimedia.org/wikipedia/en/8/8e/Wang_Bo.jpg
                    try {
                        String url = URL_IMG_LNG_PREFIX + lngName + '/' + (char) first + '/' + (char) first
                                + (char) second + '/' + toImgName;
                        Helper.download(url, toImgFile, true);
                        success = true;
                    } catch (IOException e) {
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

    private static boolean checkFreeLicense(String lngName, String toImgName, String fileTemplateName) {
        // Wang_Bo.jpg
        // http://en.wikipedia.org/w/index.php?action=raw&title=Main_Page
        boolean free = true;
        ByteBuffer tmpBB = ArrayHelper.borrowByteBufferSmall();
        try {
            boolean success = false;
            try {
                BufferedInputStream in = new BufferedInputStream(Helper.openUrlInputStream(URL_COMMONS_RAW_PREFIX
                        + toImgName));
                while (-1 != ArrayHelper.readLine(in, tmpBB)) {
                    // System.out.println(ArrayHelper.toString(tmpBB));
                    if (-1 != ArrayHelper.indexOfP(tmpBB, NON_FREE_LOWER, NON_FREE_UPPER)) {
                        free = false;
                        break;
                    }
                }
                in.close();
                success = true;
            } catch (IOException e) {
                if (DEBUG) {
                    System.err.println("没找到图像信息：" + e.toString());
                }
            }
            if (!success) {
                try {
                    BufferedInputStream in = new BufferedInputStream(Helper.openUrlInputStream("http://" + lngName
                            + ".wikipedia.org/w/index.php?action=raw&title=" + fileTemplateName + ":" + toImgName));
                    while (-1 != ArrayHelper.readLine(in, tmpBB)) {
                        if (-1 != ArrayHelper.indexOfP(tmpBB, NON_FREE_LOWER, NON_FREE_UPPER)) {
                            free = false;
                            break;
                        }
                    }
                    in.close();
                    success = true;
                } catch (IOException e) {
                    if (DEBUG) {
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
