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

import cn.kk.kkdict.Helper;

public class QQPinyinQpydExtractor {
    public static final String IN_DIR = "X:\\kkdict\\dicts\\qq";
    public static final String OUT_FILE = "X:\\kkdict\\out\\imedicts\\output-qq.txt";

    public static void main(String[] args) throws IOException {
        File directory = new File(IN_DIR);
        if (directory.isDirectory()) {
            BufferedWriter writer = new BufferedWriter(new FileWriter(OUT_FILE), 8192000);

            File[] files = directory.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".qpyd");
                }
            });

            int total = 0;
            for (File f : files) {
                System.out.print("Extracting '" + f + " ... ");
                int counter = extractQpydToFile(f, writer);
                System.out.println(counter);
                total += counter;
            }

            writer.close();
            System.out.println("\n=====================================");
            System.out.println("Total Completed: " + files.length +" Files");
            System.out.println("Total Words: " + total);
            System.out.println("=====================================");
        }
    }

    private static int extractQpydToFile(File qqydFile, BufferedWriter writer) throws IOException {
        int counter = 0;

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

        // read zipped qqyd dictionary into byte array
        dataOut.reset();
        Channels.newChannel(new InflaterOutputStream(dataOut)).write(
                ByteBuffer.wrap(dataRawBytes.array(), startZippedDictAddr, zippedDictLength));

        // uncompressed qqyd dictionary as bytes
        ByteBuffer dataUnzippedBytes = ByteBuffer.wrap(dataOut.toByteArray());
        dataUnzippedBytes.order(ByteOrder.LITTLE_ENDIAN);

        // stores the start address of actual dictionary data
        int unzippedDictStartAddr = -1;
        int idx = 0;
        byte[] byteArray = dataUnzippedBytes.array();
        while (unzippedDictStartAddr == -1 || idx < unzippedDictStartAddr) {
            // read word
            int pinyinStartAddr = dataUnzippedBytes.getInt(idx + 0x6);
            int pinyinLength = dataUnzippedBytes.get(idx + 0x0) & 0xff;
            int wordStartAddr = pinyinStartAddr + pinyinLength;
            int wordLength = dataUnzippedBytes.get(idx + 0x1) & 0xff;
            if (unzippedDictStartAddr == -1) {
                unzippedDictStartAddr = pinyinStartAddr;
            }

            String pinyin = new String(Arrays.copyOfRange(byteArray, pinyinStartAddr, pinyinStartAddr + pinyinLength),
                    "UTF-8");
            String word = new String(Arrays.copyOfRange(byteArray, wordStartAddr, wordStartAddr + wordLength),
                    "UTF-16LE");
            writer.write(word);
            writer.write(Helper.SEP_PARTS);
            writer.write(pinyin);
            writer.write(Helper.SEP_NEWLINE);
            // step up
            idx += 0xa;
            counter++;
        }
        return counter;
    }

}
