import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Arrays;
import java.util.zip.InflaterOutputStream;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

/**
 * QQ Pinyin IME QPYD File Reader
 * 
 * @author keke
 */
public class QQPinyinQpydReader {
    public static void main(String[] args) throws IOException {
        // download from http://dict.py.qq.com/list.php
        String qqydFile = "D:\\成语.qpyd";

        // read qpyd into byte array
        ByteArrayOutputStream dataOut = new ByteArrayOutputStream();
        FileChannel fChannel = new RandomAccessFile(qqydFile, "r").getChannel();
        fChannel.transferTo(0, fChannel.size(), Channels.newChannel(dataOut));
        fChannel.close();

        // qpyd as bytes
        ByteBuffer dataRawBytes = ByteBuffer.wrap(dataOut.toByteArray());
        dataRawBytes.order(ByteOrder.LITTLE_ENDIAN);
        // qpys as UTF-16LE string
        String dataString = new String(dataRawBytes.array(), "UTF-16LE");

        // print header
        System.out.println("名称：" + substringBetween(dataString, "Name: ", "\n"));
        System.out.println("类型：" + substringBetween(dataString, "Type: ", "\n"));
        System.out.println("子类型：" + substringBetween(dataString, "FirstType: ", "\n"));
        System.out.println("词库说明：" + substringBetween(dataString, "Intro: ", "\n"));
        System.out.println("词库样例：" + substringBetween(dataString, "Example: ", "\n"));
        System.out.println("词条数：" + dataRawBytes.getInt(0x44));
        int startZippedDictAddr = indexOf(dataRawBytes.array(), new byte[] { (byte) 0x78, (byte) 0x9C });
        System.out.println("压缩词库数据地址：0x" + Integer.toHexString(startZippedDictAddr));
        System.out.println();

        // read zipped qqyd dictionary into byte array
        dataOut.reset();
        WritableByteChannel dataChannel = Channels.newChannel(new InflaterOutputStream(dataOut));
        dataChannel.write(ByteBuffer.wrap(dataRawBytes.array(), startZippedDictAddr, dataRawBytes.limit()
                - startZippedDictAddr));

        // uncompressed qqyd dictionary as bytes
        ByteBuffer dataUnzippedBytes = ByteBuffer.wrap(dataOut.toByteArray());
        dataUnzippedBytes.order(ByteOrder.LITTLE_ENDIAN);

        // stores the start address of actual dictionary data
        int unzippedDictStartAddr = -1;
        int idx = 0;
        while (unzippedDictStartAddr == -1 || idx < unzippedDictStartAddr) {
            // read word
            int pinyinStartAddr = dataUnzippedBytes.getInt(idx + 0x6);
            int pinyinLength = dataUnzippedBytes.get(idx + 0x0);
            int wordStartAddr = pinyinStartAddr + pinyinLength;
            int wordLength = dataUnzippedBytes.get(idx + 0x1);
            if (unzippedDictStartAddr == -1) {
                unzippedDictStartAddr = pinyinStartAddr;
            }
            String pinyin = new String(Arrays.copyOfRange(dataUnzippedBytes.array(), pinyinStartAddr, pinyinStartAddr
                    + pinyinLength), "UTF-8");
            String word = new String(Arrays.copyOfRange(dataUnzippedBytes.array(), wordStartAddr, wordStartAddr
                    + wordLength),
                    "UTF-16LE");
            System.out.println(word + "\t\t" + pinyin);
            // step up
            idx += 0xa;
        }
    }

    public static final String substringBetween(String text, String start, String end) {
        int nStart = text.indexOf(start);
        int nEnd = text.indexOf(end, nStart + 1);
        if (nStart != -1 && nEnd != -1) {
            return text.substring(nStart + start.length(), nEnd - 1);
        } else {
            return null;
        }
    }

    public static final int indexOf(byte[] data, byte[] pattern) {
        for (int i = 0; i < data.length; i++) {
            boolean found = true;
            for (int j = 0; j < pattern.length; j++) {
                if (data[i + j] != pattern[j]) {
                    found = false;
                    break;
                }
            }
            if (found) {
                return i;
            }
        }
        return -1;
    }
}
