import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;

import cn.kk.kkdict.Helper;

/**
 * Sougou Pinyin IME SCEL File Reader
 * 
 * @author keke
 */
public class SogouScelReader {
    public static void main(String[] args) throws IOException {
        // download from http://pinyin.sogou.com/dict
        String scelFile = "D:\\test.scel";

        // read scel into byte array
        ByteArrayOutputStream dataOut = new ByteArrayOutputStream();
        FileChannel fChannel = new RandomAccessFile(scelFile, "r").getChannel();
        fChannel.transferTo(0, fChannel.size(), Channels.newChannel(dataOut));
        fChannel.close();

        // scel as bytes
        ByteBuffer dataRawBytes = ByteBuffer.wrap(dataOut.toByteArray());
        dataRawBytes.order(ByteOrder.LITTLE_ENDIAN);

        byte[] buf = new byte[1024];
        String[] pyDict = new String[512];

        int totalWords = dataRawBytes.getInt(0x120);

        // pinyin offset
        dataRawBytes.position(dataRawBytes.getInt());
        int totalPinyin = dataRawBytes.getInt();
        for (int i = 0; i < totalPinyin; i++) {
            int mark = dataRawBytes.getShort();
            int len = dataRawBytes.getShort();
            dataRawBytes.get(buf, 0, len);
            pyDict[mark] = new String(buf, 0, len, "UTF-16LE");
        }

        // extract dictionary
        int counter = 0;
        for (int i = 0; i < totalWords; i++) {
            StringBuilder py = new StringBuilder();
            StringBuilder word = new StringBuilder();

            int size = dataRawBytes.getShort();
            int len = dataRawBytes.getShort() / 2;
            boolean first = true;
            while (len-- > 0) {
                int key = dataRawBytes.getShort();
                if (first) {
                    first = false;
                } else {
                    py.append(Helper.SEP_PY);
                }
                py.append(pyDict[key]);
            }

            while (size-- > 0) {
                len = dataRawBytes.getShort();
                dataRawBytes.get(buf, 0, len);
                word.append(new String(buf, 0, len, "UTF-16LE"));
                dataRawBytes.get(buf, 0, dataRawBytes.getShort());
            }
            System.out.println(word.toString() + "\t" + py.toString());
            counter++;
        }
        System.out.println("\nExtracted '" + scelFile + "': " + counter);
    }
}
