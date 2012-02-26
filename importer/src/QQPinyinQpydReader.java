import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.zip.InflaterOutputStream;

/**
 * QQ Pinyin IME QPYD File Reader
 * 
 * <pre>
 * QPYD Format overview:
 * 
 * General Information:
 * - Chinese characters are all encoded with UTF-16LE.
 * - Pinyin are encoded in ascii (or UTF-8).
 * - Numbers are using little endian byte order.
 * 
 * QPYD hex analysis:
 * - 0x00 QPYD file identifier
 * - 0x38 offset of compressed data (word-pinyin-dictionary)
 * - 0x44 total words in qpyd
 * - 0x60 start of header information
 * 
 * Compressed data analysis:
 * - zip/standard (beginning with 0x789C) is used in (all analyzed) qpyd files
 * - data is divided in two parts
 * -- 1. offset and length information (16 bytes for each pinyin-word pair)
 *       0x06 offset points to first pinyin
 *       0x00 length of pinyin
 *       0x01 length of word
 * -- 2. actual data
 *       Dictionary data has the form ((pinyin)(word))* with no separators.
 *       Data can only be read using offset and length information.
 * 
 * </pre>
 * 
 * @author keke
 */
public class QQPinyinQpydReader {
    public static void main(String[] args) throws IOException {
        // download from http://dict.py.qq.com/list.php
        String qqydFile = "D:\\test.qpyd";

        // read qpyd into byte array
        ByteArrayOutputStream dataOut = new ByteArrayOutputStream();
        FileChannel fChannel = new RandomAccessFile(qqydFile, "r").getChannel();
        fChannel.transferTo(0, fChannel.size(), Channels.newChannel(dataOut));
        fChannel.close();

        // qpyd as bytes
        ByteBuffer dataRawBytes = ByteBuffer.wrap(dataOut.toByteArray());
        dataRawBytes.order(ByteOrder.LITTLE_ENDIAN);

        // read info of compressed data
        int startZippedDictAddr = dataRawBytes.getInt(0x38);
        int zippedDictLength = dataRawBytes.limit() - startZippedDictAddr;

        // qpys as UTF-16LE string
        String dataString = new String(Arrays.copyOfRange(dataRawBytes.array(), 0x60, startZippedDictAddr), "UTF-16LE");

        // print header
        System.out.println("名称：" + substringBetween(dataString, "Name: ", "\r\n"));
        System.out.println("类型：" + substringBetween(dataString, "Type: ", "\r\n"));
        System.out.println("子类型：" + substringBetween(dataString, "FirstType: ", "\r\n"));
        System.out.println("词库说明：" + substringBetween(dataString, "Intro: ", "\r\n"));
        System.out.println("词库样例：" + substringBetween(dataString, "Example: ", "\r\n"));
        System.out.println("词条数：" + dataRawBytes.getInt(0x44));

        // read zipped qqyd dictionary into byte array
        dataOut.reset();
        Channels.newChannel(new InflaterOutputStream(dataOut)).write(
                ByteBuffer.wrap(dataRawBytes.array(), startZippedDictAddr, zippedDictLength));

        // uncompressed qqyd dictionary as bytes
        ByteBuffer dataUnzippedBytes = ByteBuffer.wrap(dataOut.toByteArray());
        dataUnzippedBytes.order(ByteOrder.LITTLE_ENDIAN);

        // for debugging: save unzipped data to *.unzipped file
        Channels.newChannel(new FileOutputStream(qqydFile + ".unzipped")).write(dataUnzippedBytes);
        System.out.println("压缩数据：0x" + Integer.toHexString(startZippedDictAddr) + " (解压前：" + zippedDictLength
                + " B, 解压后：" + dataUnzippedBytes.limit() + " B)");
        
        // stores the start address of actual dictionary data
        int unzippedDictStartAddr = -1;
        byte[] byteArray = dataUnzippedBytes.array();
        dataUnzippedBytes.position(0);
        while (unzippedDictStartAddr == -1 || dataUnzippedBytes.position() < unzippedDictStartAddr) {
            // read word
            int pinyinLength = dataUnzippedBytes.get() & 0xff;
            int wordLength = dataUnzippedBytes.get() & 0xff;
            dataUnzippedBytes.getInt(); // garbage
            int pinyinStartAddr = dataUnzippedBytes.getInt();            
            int wordStartAddr = pinyinStartAddr + pinyinLength;
            
            if (unzippedDictStartAddr == -1) {
                unzippedDictStartAddr = pinyinStartAddr;
                System.out.println("词库地址（解压后）：0x" + Integer.toHexString(unzippedDictStartAddr) + "\n");
            }

            String pinyin = new String(Arrays.copyOfRange(byteArray, pinyinStartAddr, pinyinStartAddr + pinyinLength),
                    "UTF-8");
            String word = new String(Arrays.copyOfRange(byteArray, wordStartAddr, wordStartAddr + wordLength),
                    "UTF-16LE");
            System.out.println(word + "\t" + pinyin);
        }
    }

    public static final String substringBetween(String text, String start, String end) {
        int nStart = text.indexOf(start);
        int nEnd = text.indexOf(end, nStart + 1);
        if (nStart != -1 && nEnd != -1) {
            return text.substring(nStart + start.length(), nEnd);
        } else {
            return null;
        }
    }
}
