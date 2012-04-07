import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.zip.GZIPInputStream;

import org.apache.tools.bzip2.CBZip2InputStream;
import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarInputStream;

import cn.kk.kkdict.utils.Helper;

public class StardictReader {

    /**
     * @param args
     * @throws IOException
     * @throws FileNotFoundException
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {
        // testDict();
        testIdx();
        // BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        // BufferedReader reader = new BufferedReader(new InputStreamReader(new BZip2CompressorInputStream(new
        // FileInputStream("D:\\tmp\\test.tar.bz2"))));

    }

    private static void testDict() throws IOException, FileNotFoundException {
        TarInputStream tarIn = new TarInputStream(new CBZip2InputStream(new FileInputStream("D:\\tmp\\test.tar.bz2")));
        TarEntry entry;

        while (null != (entry = tarIn.getNextEntry())) {
            InputStream dictIn = null;
            if (entry.getName().endsWith(".dict.dz")) {
                System.out.println("解读：'" + entry.getName() + "'（" + entry.getSize() + "）。。。");
                dictIn = new GZIPInputStream(tarIn);
            } else if (entry.getName().endsWith(".dict")) {
                dictIn = tarIn;
            }
            if (dictIn != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(dictIn));
                String startTag = "<单词原型><![CDATA[";
                String endTag = "]]>";
                String line;
                int startIdx;
                int endIdx;
                while (null != (line = reader.readLine())) {
                    if (-1 != (startIdx = line.indexOf(startTag)) && -1 != (endIdx = line.indexOf(endTag))
                            && endIdx > startIdx) {
                        System.out.println(line.substring(startIdx + startTag.length(), endIdx));
                    }
                }
                dictIn.close();
                break;
            }
        }
        tarIn.close();
    }

    private static void testIdx() throws IOException, FileNotFoundException {
        TarInputStream tarIn = new TarInputStream(new CBZip2InputStream(new FileInputStream("D:\\tmp\\test.tar.bz2")));
        TarEntry entry;
        while (null != (entry = tarIn.getNextEntry())) {
            InputStream dictIn = null;
            if (entry.getName().endsWith(".idx.gz")) {
                System.out.println("解读：'" + entry.getName() + "'（" + entry.getSize() + "）。。。");
                dictIn = new GZIPInputStream(tarIn);
            } else if (entry.getName().endsWith(".idx")) {
                dictIn = tarIn;
            }
            if (dictIn != null) {
                BufferedInputStream in = new BufferedInputStream(dictIn);
                ByteBuffer bb = ByteBuffer.allocate(1024);
                int b;
                int skipCount = 0;
                while (-1 != (b = in.read())) {
                    if (skipCount == 0) {
                        if (b != 0) {
                            bb.put((byte) b);
                        } else {
                            System.out.println(new String(bb.array(), 0, bb.position(), Charset.forName("UTF-8")));
                            skipCount++;
                            bb.clear();
                        }
                    } else if (++skipCount > 8) {

                        skipCount = 0;
                    }
                }
                dictIn.close();
                break;
            }
        }
        tarIn.close(); 
    }
}
