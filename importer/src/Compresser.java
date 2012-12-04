import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.SeekableFileInputStream;
import org.tukaani.xz.SeekableXZInputStream;
import org.tukaani.xz.XZOutputStream;

public class Compresser {
  private static final String testFile         = "D:\\vpn-autoconnect-1.0.0.exe";
  private static final String testCompressed   = "D:\\vpn-autoconnect-1.0.0.exe.xz";
  private static final String testDecompressed = "D:\\vpn-autoconnect-1.0.0.exe.xz.exe";

  public static void main(String[] args) throws FileNotFoundException, IOException {
    Compresser.compress();
    Compresser.decompress();
  }

  private static void decompress() throws FileNotFoundException, IOException {
    SeekableFileInputStream file = new SeekableFileInputStream(Compresser.testCompressed, 0);
    SeekableXZInputStream in = new SeekableXZInputStream(file);
    FileOutputStream out = new FileOutputStream(Compresser.testDecompressed);

    byte[] data = new byte[(int) new File(Compresser.testFile).length()];
    in.seek(0);
    in.read(data);
    out.write(data);
    out.close();

    File f1 = new File(Compresser.testFile);
    File f2 = new File(Compresser.testDecompressed);
    if (f1.length() != f2.length()) {
      System.err.println("文件大小不相同！");
    } else {
      System.out.println("文件大小相同.");
    }
    ByteBuffer b1 = ByteBuffer.allocate((int) f1.length());
    ByteBuffer b2 = ByteBuffer.allocate((int) f2.length());
    new FileInputStream(f1).getChannel().read(b1);
    new FileInputStream(f2).getChannel().read(b2);
    if (!Arrays.equals(b1.array(), b2.array())) {
      System.err.println("文件不相同！");
    } else {
      System.out.println("文件相同.");
    }

  }

  private static void compress() throws FileNotFoundException, IOException {
    LZMA2Options options = new LZMA2Options(9);

    int blockSize = 1024 * 64; // smaller is faster but bigger output

    options.setDictSize(Math.min(options.getDictSize(), Math.max(LZMA2Options.DICT_SIZE_MIN, blockSize)));

    try (XZOutputStream out = new XZOutputStream(new FileOutputStream(Compresser.testCompressed), options);
        FileInputStream in = new FileInputStream(Compresser.testFile);) {
      byte[] buf = new byte[8192];
      int left = blockSize;

      while (true) {
        int size = in.read(buf, 0, Math.min(buf.length, left));
        if (size == -1) {
          break;
        }

        out.write(buf, 0, size);
        left -= size;

        if (left == 0) {
          out.endBlock();
          left = blockSize;
        }
      }

      out.finish();
    }
  }
}
