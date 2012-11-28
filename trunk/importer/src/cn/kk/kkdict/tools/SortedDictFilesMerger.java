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
package cn.kk.kkdict.tools;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

import cn.kk.kkdict.beans.DictByteBufferRow;
import cn.kk.kkdict.types.Language;
import cn.kk.kkdict.utils.ArrayHelper;
import cn.kk.kkdict.utils.DictHelper;
import cn.kk.kkdict.utils.Helper;

/**
 * 将多个已排序的文件合并为一个文件。 TODO test
 */
public class SortedDictFilesMerger {
  private static boolean          noticed  = false;
  private final static boolean    DEBUG    = false;
  private final String[]          inFiles;
  private final Language          mergeLng;
  public static final String      OUT_FILE = "output-dict_mrg-result.wiki";
  public final String             outFile;
  private final ByteBuffer        lngBB;
  private final String            inFileMain;
  private final DictByteBufferRow mainRow  = new DictByteBufferRow();
  private final DictByteBufferRow otherRow = new DictByteBufferRow();
  private int                     mainIdx;
  private int                     otherIdx;

  public SortedDictFilesMerger(final Language mergeLng, final String outDir, final String outFile, final String... inFiles) {
    if (new File(outDir).isDirectory()) {
      if (!SortedDictFilesMerger.noticed) {
        System.out.println("温馨提示：需合并的文件必须事先排序。");
        SortedDictFilesMerger.noticed = true;
      }
      if (inFiles.length != 2) {
        this.inFiles = null;
        this.inFileMain = null;
        this.mergeLng = null;
        this.outFile = null;
        this.lngBB = null;
        System.err.println("本程序现在只能合并两个文件！");
      } else {
        String maxFile = null;
        long max = -1;
        final List<String> files = new LinkedList<>();
        for (final String f : inFiles) {
          final long l = new File(f).length();
          if (l > max) {
            max = l;
            maxFile = f;
          }
          files.add(f);
        }
        files.remove(maxFile);
        this.inFiles = files.toArray(new String[files.size()]);
        this.inFileMain = maxFile;
        this.mergeLng = mergeLng;
        if (outFile != null) {
          this.outFile = outDir + File.separator + outFile;
        } else {
          this.outFile = outDir + File.separator + SortedDictFilesMerger.OUT_FILE;
        }
        this.lngBB = ByteBuffer.wrap(mergeLng.getKeyBytes());
      }
    } else {
      this.inFiles = null;
      this.inFileMain = null;
      this.mergeLng = null;
      this.outFile = null;
      this.lngBB = null;
      System.err.println("文件夹不可读：'" + outDir + "'!");
    }
  }

  public void merge() throws IOException {
    final long start = System.currentTimeMillis();
    System.out.println("合并含有'" + this.mergeLng.getKey() + "'的词典  。。。" + (this.inFiles.length + 1));
    File f = new File(this.inFileMain);
    if (f.isFile()) {
      if (SortedDictFilesMerger.DEBUG) {
        System.out.println("导入主合并文件'" + f.getAbsolutePath() + "'（" + Helper.formatSpace(f.length()) + "）。。。");
      }
    } else {
      System.err.println("主合并文件'" + f.getAbsolutePath() + "'不存在！");
      return;
    }
    try (BufferedInputStream inFilesMainIn = new BufferedInputStream(new FileInputStream(f), Helper.BUFFER_SIZE);) {
      final BufferedInputStream[] inFilesIns = new BufferedInputStream[this.inFiles.length];
      int i = 0;
      for (final String inFile : this.inFiles) {
        f = new File(inFile);
        if (f.isFile()) {
          if (SortedDictFilesMerger.DEBUG) {
            System.out.println("导入分文件'" + f.getAbsolutePath() + "'（" + Helper.formatSpace(f.length()) + "）。。。");
          }
          inFilesIns[i] = new BufferedInputStream(new FileInputStream(f), Helper.BUFFER_SIZE);
        } else {
          System.err.println("分文件不可读'" + f.getAbsolutePath() + "'！");
        }
        i++;
      }
      if (SortedDictFilesMerger.DEBUG) {
        System.out.println("创建输出文件'" + this.outFile + "'。。。");
      }
      try (final BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(this.outFile), Helper.BUFFER_SIZE);) {

        this.merge(out, inFilesMainIn, inFilesIns);

        for (final BufferedInputStream in : inFilesIns) {
          if (in != null) {
            in.close();
          }
        }
      }
    }
    System.out.println("合并成功'" + this.outFile + "'（" + Helper.formatSpace(new File(this.outFile).length()) + "）。用时："
        + Helper.formatDuration(System.currentTimeMillis() - start));
  }

  private void merge(final BufferedOutputStream out, final BufferedInputStream inFilesMainIn, final BufferedInputStream[] inFilesIns) throws IOException {
    final ByteBuffer[] inFileBBs = new ByteBuffer[inFilesIns.length];
    for (int i = 0; i < inFilesIns.length; i++) {
      inFileBBs[i] = ArrayHelper.borrowByteBufferMedium();
      inFileBBs[i].limit(0);
    }
    final ByteBuffer mergeBB = ArrayHelper.borrowByteBufferLarge();
    final ByteBuffer lineBB = ArrayHelper.borrowByteBufferMedium();
    while (-1 != ArrayHelper.readLine(inFilesMainIn, lineBB)) {
      if (SortedDictFilesMerger.DEBUG) {
        System.out.println("合并词组：" + ArrayHelper.toString(lineBB));
      }
      this.mainRow.parseFrom(lineBB).sortValues();
      if (-1 == (this.mainIdx = this.mainRow.indexOfLanguage(this.lngBB))) {
        // main file has no more sort key
        out.write(this.mainRow.array(), 0, this.mainRow.limit());
        out.write(Helper.SEP_NEWLINE_CHAR);
        break;
      }
      // copy original line
      ArrayHelper.copy(this.mainRow.getByteBuffer(), mergeBB);
      for (int i = 0; i < inFilesIns.length; i++) {
        this.mergeInFile(mergeBB, inFileBBs, inFilesIns, out, i);
      }
      out.write(mergeBB.array(), 0, mergeBB.limit());
      out.write(Helper.SEP_NEWLINE_CHAR);
    }
    int len;
    while ((len = inFilesMainIn.read(mergeBB.array())) != -1) {
      out.write(mergeBB.array(), 0, len);
    }
    int i = 0;
    for (final BufferedInputStream inFileIn : inFilesIns) {
      final ByteBuffer inBB = inFileBBs[i];
      if ((inBB != null) && inBB.hasRemaining()) {
        out.write(inBB.array(), 0, inBB.limit());
        out.write(Helper.SEP_NEWLINE_CHAR);
        inFileBBs[i] = null;
        ArrayHelper.giveBack(inBB);
      }
      if (inFileIn != null) {
        while ((len = inFileIn.read(mergeBB.array())) != -1) {
          out.write(mergeBB.array(), 0, len);
        }
      }
      i++;
    }
    ArrayHelper.giveBack(mergeBB);
  }

  @SuppressWarnings("resource")
  private int mergeInFile(final ByteBuffer mergeBB, final ByteBuffer[] inFileBBs, final BufferedInputStream[] inFileIns, final BufferedOutputStream out,
      final int inFileIdx) throws IOException {
    boolean predessor = false;
    final BufferedInputStream inFileIn = inFileIns[inFileIdx];
    if (inFileIn != null) {
      do {
        final ByteBuffer inBB = inFileBBs[inFileIdx];
        if (inBB != null) {
          boolean eof = false;
          if (inBB.limit() == 0) {
            if (-1 != ArrayHelper.readLine(inFileIn, inBB)) {
              this.otherRow.parseFrom(inBB).sortValues();
              if (-1 == (this.otherIdx = this.otherRow.indexOfLanguage(this.lngBB))) {
                // in file has no more sort key
                eof = true;
              }
            } else {
              eof = true;
            }
          } else {
            this.otherRow.parseFrom(inBB).sortValues();
          }
          if (eof) {
            if (SortedDictFilesMerger.DEBUG) {
              System.out.println(inFileIdx + ": end");
            }
            inFileBBs[inFileIdx] = null;
            ArrayHelper.giveBack(inBB);
            predessor = false;
          } else {
            if (SortedDictFilesMerger.DEBUG) {
              System.out.println(inFileIdx + ": cmp " + ArrayHelper.toStringP(this.mainRow.getByteBuffer()) + " <> "
                  + ArrayHelper.toStringP(this.otherRow.getByteBuffer()));
            }
            predessor = ArrayHelper.isPredessorEqualsP(this.otherRow.getFirstValue(this.otherIdx), this.mainRow.getFirstValue(this.mainIdx));

            if (predessor) {
              if (ArrayHelper.equalsP(this.otherRow.getFirstValue(this.otherIdx), this.mainRow.getFirstValue(this.mainIdx))) {
                // merge
                DictHelper.mergeDefinitionsAndAttributes(this.mainRow, this.otherRow, mergeBB);
                if (SortedDictFilesMerger.DEBUG) {
                  System.out.println(inFileIdx + ": merge " + ArrayHelper.toStringP(this.otherRow.getByteBuffer()) + " == "
                      + ArrayHelper.toStringP(this.mainRow.getByteBuffer()));
                }
                this.mainRow.parseFrom(mergeBB);
              } else {
                if (SortedDictFilesMerger.DEBUG) {
                  System.out.println(inFileIdx + ": skip " + ArrayHelper.toStringP(this.otherRow.getByteBuffer()) + " < "
                      + ArrayHelper.toStringP(this.mainRow.getByteBuffer()));
                }
                out.write(this.otherRow.array(), 0, this.otherRow.limit());
                out.write(Helper.SEP_NEWLINE_CHAR);
              }
              inBB.limit(0);
            } else {
              if (SortedDictFilesMerger.DEBUG) {
                System.out.println(inFileIdx + ": skip post " + ArrayHelper.toString(this.otherRow.getByteBuffer()) + " > "
                    + ArrayHelper.toString(this.mainRow.getByteBuffer()));
              }
              // reset inBB limit and position
              this.otherRow.getByteBuffer();
            }
          }
        }
      } while (predessor);
    }
    return mergeBB.position();
  }
}
