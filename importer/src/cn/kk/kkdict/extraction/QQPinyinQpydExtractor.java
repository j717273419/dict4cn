package cn.kk.kkdict.extraction;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.zip.InflaterOutputStream;

import cn.kk.kkdict.types.TranslationSource;
import cn.kk.kkdict.utils.ChineseHelper;
import cn.kk.kkdict.utils.Helper;

public class QQPinyinQpydExtractor {
    public static final String IN_DIR = "X:\\kkdict\\dicts\\qq";
    public static final String OUT_FILE = "O:\\imedicts\\output-words."+ TranslationSource.QQ_QPYD.key;

    public static void main(String[] args) throws IOException {
        File directory = new File(IN_DIR);
        if (directory.isDirectory()) {
            BufferedWriter writer = new BufferedWriter(new FileWriter(OUT_FILE), Helper.BUFFER_SIZE);

            File[] files = directory.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".qpyd");
                }
            });

            int total = 0;
            for (File f : files) {
                System.out.print("读取QPYD文件'" + f + "' ... ");
                int counter = extractQpydToFile(f, writer);
                System.out.println(counter);
                total += counter;
            }

            writer.close();
            System.out.println("\n=====================================");
            System.out.println("总共读取了" + files.length + "个QQ输入法文件");
            System.out.println("总共词汇：" + total);
            System.out.println("=====================================");
        }
    }

    private static int extractQpydToFile(File qpydFile, BufferedWriter writer) throws IOException {
        int counter = 0;

        // read qpyd into byte array
        ByteArrayOutputStream dataOut = new ByteArrayOutputStream();
        FileChannel fChannel = new RandomAccessFile(qpydFile, "r").getChannel();
        fChannel.transferTo(0, fChannel.size(), Channels.newChannel(dataOut));
        fChannel.close();

        // qpyd as bytes
        ByteBuffer dataRawBytes = ByteBuffer.wrap(dataOut.toByteArray());
        dataRawBytes.order(ByteOrder.LITTLE_ENDIAN);

        // read info of compressed data
        int startZippedDictAddr = dataRawBytes.getInt(0x38);
        int zippedDictLength = dataRawBytes.limit() - startZippedDictAddr;

        // read zipped qqyd dictionary into byte array
        dataOut.reset();
        Channels.newChannel(new InflaterOutputStream(dataOut)).write(
                ByteBuffer.wrap(dataRawBytes.array(), startZippedDictAddr, zippedDictLength));

        // uncompressed qqyd dictionary as bytes
        ByteBuffer dataUnzippedBytes = ByteBuffer.wrap(dataOut.toByteArray());
        dataUnzippedBytes.order(ByteOrder.LITTLE_ENDIAN);

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
            }

            String pinyin = new String(Arrays.copyOfRange(byteArray, pinyinStartAddr, pinyinStartAddr + pinyinLength),
                    "UTF-8");
            String word = new String(Arrays.copyOfRange(byteArray, wordStartAddr, wordStartAddr + wordLength),
                    "UTF-16LE");
            if (Helper.checkValidPinyin(pinyin)) {
                writer.write(ChineseHelper.toSimplifiedChinese(cleanWord(word)));
                writer.write(Helper.SEP_PARTS);
                writer.write(pinyin);
                writer.write(Helper.SEP_NEWLINE);
                counter++;
            }
        }
        return counter;
    }

    private static String cleanWord(String word) {
        return word.replaceAll("\\(.*\\)", Helper.EMPTY_STRING);
    }

}
