import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;

/**
 * Baidu Pinyin IME BDICT File Reader
 * 
 * <pre>
 * BDICT Format overview:
 * 
 * General Information:
 * - Chinese characters and pinyin are all encoded with UTF-16LE.
 * - Numbers are using little endian byte order.
 * 
 * BDICT hex analysis:
 * - 0x250         total number of words
 * - 0x350         dictionary offset
 * - 0x<Offset>    Dictionary
 * 
 * Dictionary format:
 * - It can interpreted as a list of 
 *   [amount of characters (short not integer!)
 *       pinyin construction using fenmu and yunmu,
 *       word as string 
 *   ].
 * 
 * </pre>
 * 
 * @author keke
 */
public class BaiduBdictReader {
    private static final String[] FEN_MU = { "c", "d", "b", "f", "g", "h", "ch", "j", "k", "l", "m", "n", "", "p", "q",
            "r", "s", "t", "sh", "zh", "w", "x", "y", "z" };
    private static final String[] YUN_MU = { "uang", "iang", "ong", "ang", "eng", "ian", "iao", "ing", "ong", "uai",
            "uan", "ai", "an", "ao", "ei", "en", "er", "ua", "ie", "in", "iu", "ou", "ia", "ue", "ui", "un", "uo", "a",
            "e", "i", "a", "u", "v" };

    public static void main(String[] args) throws IOException {
        // download from http://r6.mo.baidu.com/web/iw/index/
        String bdictFile = "D:\\test.bcd";

        // read scel into byte array
        ByteArrayOutputStream dataOut = new ByteArrayOutputStream();
        FileChannel fChannel = new RandomAccessFile(bdictFile, "r").getChannel();
        fChannel.transferTo(0, fChannel.size(), Channels.newChannel(dataOut));
        fChannel.close();

        // bdict as bytes
        ByteBuffer dataRawBytes = ByteBuffer.wrap(dataOut.toByteArray());
        dataRawBytes.order(ByteOrder.LITTLE_ENDIAN);

        byte[] buf = new byte[1024];
        int total = dataRawBytes.getInt(0x250);
        // dictionary offset
        dataRawBytes.position(0x350);
        for (int i = 0; i < total; i++) {
            int length = dataRawBytes.getShort();
            dataRawBytes.getShort();
            boolean first = true;
            StringBuilder pinyin = new StringBuilder();
            for (int j = 0; j < length; j++) {
                if (first) {
                    first = false;
                } else {
                    pinyin.append('\'');
                }
                pinyin.append(FEN_MU[dataRawBytes.get()] + YUN_MU[dataRawBytes.get()]);
            }
            dataRawBytes.get(buf, 0, 2 * length);
            String word = new String(buf, 0, 2 * length, "UTF-16LE");
            System.out.println(word+"\t"+pinyin);
        }

        System.out.println("\nExtracted '" + bdictFile + "': " + total);
    }
}
