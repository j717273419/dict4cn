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

import cn.kk.kkdict.utils.Helper;

public class BaiduBdictExtractor {
    public static final String IN_DIR = "X:\\kkdict\\dicts\\baidu";
    public static final String OUT_FILE = "X:\\kkdict\\out\\imedicts\\output-baidu.kpy";

    public static void main(String[] args) throws IOException {
        File directory = new File(IN_DIR);
        if (directory.isDirectory()) {
            BufferedWriter writer = new BufferedWriter(new FileWriter(OUT_FILE), 8192000);

            File[] files = directory.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".bcd");
                }
            });

            int total = 0;
            for (File f : files) {
                System.out.print("Extracting '" + f + " ... ");
                int counter = extractBdictToFile(f, writer);
                System.out.println(counter);
                total += counter;
            }

            writer.close();
            System.out.println("\n=====================================");
            System.out.println("Total Completed: " + files.length + " Files");
            System.out.println("Total Words: " + total);
            System.out.println("=====================================");
        }
    }

    private static int extractBdictToFile(File bcdFile, BufferedWriter writer) throws IOException {
        int counter = 0;

        // read qpyd into byte array
        ByteArrayOutputStream dataOut = new ByteArrayOutputStream();
        FileChannel fChannel = new RandomAccessFile(bcdFile, "r").getChannel();
        fChannel.transferTo(0, fChannel.size(), Channels.newChannel(dataOut));
        fChannel.close();

        // qpyd as bytes
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
                pinyin.append(Helper.FEN_MU[dataRawBytes.get()] + Helper.YUN_MU[dataRawBytes.get()]);
            }
            dataRawBytes.get(buf, 0, 2 * length);
            String word = new String(buf, 0, 2 * length, "UTF-16LE");

            writer.write(word);
            writer.write(Helper.SEP_PARTS);
            writer.write(pinyin.toString());
            writer.write(Helper.SEP_NEWLINE);

            counter++;
        }

        return counter;
    }

}
