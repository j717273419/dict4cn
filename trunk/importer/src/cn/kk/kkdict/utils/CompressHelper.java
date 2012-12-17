package cn.kk.kkdict.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.SeekableFileInputStream;
import org.tukaani.xz.SeekableXZInputStream;
import org.tukaani.xz.UnsupportedOptionsException;
import org.tukaani.xz.XZOutputStream;

public class CompressHelper {
  // 越小越快，但产生文件会更大
  public static final int           BLOCK_SIZE = 1024 * 64;

  private static final byte[]       BUFFER     = new byte[8192];

  private static final LZMA2Options OPTIONS;
  static {
    try {
      OPTIONS = new LZMA2Options(9);
      CompressHelper.OPTIONS.setDictSize(Math.min(CompressHelper.OPTIONS.getDictSize(), Math.max(LZMA2Options.DICT_SIZE_MIN, CompressHelper.BLOCK_SIZE)));
    } catch (UnsupportedOptionsException e) {
      throw new UnknownError(e.toString());
    }
  }

  @SuppressWarnings("resource")
  public static SeekableXZInputStream getDecompressedInputStream(String inFile, long offset, long len) throws IOException {
    SeekableFileInputStream file = new SeekableFileInputStream(inFile, offset, len);
    SeekableXZInputStream in = new SeekableXZInputStream(file);
    in.seek(0);
    return in;
  }

  /**
   * 
   * @param inFile
   * @param outFile
   * @return compressed length (byte)
   * @throws FileNotFoundException
   * @throws IOException
   */
  public static long compress(String inFile, String outFile) throws FileNotFoundException, IOException {
    final File outFileObj = new File(outFile);
    try (XZOutputStream out = new XZOutputStream(new FileOutputStream(outFileObj), CompressHelper.OPTIONS); FileInputStream in = new FileInputStream(inFile);) {
      int left = CompressHelper.BLOCK_SIZE;
      while (true) {
        int size = in.read(CompressHelper.BUFFER, 0, Math.min(CompressHelper.BUFFER.length, left));
        if (size == -1) {
          break;
        }
        out.write(CompressHelper.BUFFER, 0, size);
        left -= size;
        if (left == 0) {
          out.endBlock();
          left = CompressHelper.BLOCK_SIZE;
        }
      }
      out.finish();
    }
    return outFileObj.length();
  }
}
