package cn.kk.kkdict.extraction.word;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.Collections;
import java.util.Set;

import cn.kk.kkdict.types.Category;
import cn.kk.kkdict.types.WordSource;
import cn.kk.kkdict.utils.ChineseHelper;
import cn.kk.kkdict.utils.Helper;
import cn.kk.kkdict.utils.PinyinHelper;

public class BaiduBcdExtractor {
    public static final String IN_DIR = Helper.DIR_IN_WORDS+"\\baidu";
    public static final String OUT_DIR = Helper.DIR_OUT_WORDS;
    public static final String OUT_FILE = OUT_DIR + "\\output-words." + WordSource.BAIDU_BDICT.key;

    public static void main(String[] args) throws IOException {
        File directory = new File(IN_DIR);
        if (directory.isDirectory()) {
            new File(OUT_DIR).mkdirs();
            System.out.print("搜索百度BCD文件'" + IN_DIR + "' ... ");
            BufferedWriter writer = new BufferedWriter(new FileWriter(OUT_FILE), Helper.BUFFER_SIZE);

            File[] files = directory.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".bcd");
                }
            });
            System.out.println(files.length);

            int total = 0;
            String tmp;
            for (File f : files) {
                System.out.print("读取BCD文件'" + f + "' ... ");
                Set<String> categories = Collections.emptySet();
                if (null != (tmp = Helper.substringBetween(f.getName(), "_", ".bcd"))) {
                    categories = Category.parseValid(tmp.split("_"));
                }
                int counter = extractBdictToFile(f, writer, categories);
                System.out.println(counter);
                total += counter;
            }

            writer.close();
            System.out.println("\n=====================================");
            System.out.println("总共读取了" + files.length + "个百度BCD文件");
            System.out.println("有效词组：" + total);
            System.out.println("=====================================");
        }
    }

    private static int extractBdictToFile(File bcdFile, BufferedWriter writer, Set<String> categories) throws IOException {
        int counter = 0;

        // read bcds into byte array
        FileChannel fChannel = new RandomAccessFile(bcdFile, "r").getChannel();
        ByteBuffer dataRawBytes = ByteBuffer.allocate((int) fChannel.size());
        fChannel.read(dataRawBytes);
        fChannel.close();
        dataRawBytes.order(ByteOrder.LITTLE_ENDIAN);
        dataRawBytes.rewind();

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
                pinyin.append(PinyinHelper.FEN_MU[dataRawBytes.get()] + PinyinHelper.YUN_MU[dataRawBytes.get()]);
            }
            dataRawBytes.get(buf, 0, 2 * length);
            String word = new String(buf, 0, 2 * length, "UTF-16LE");

            writer.write(Helper.appendCategories(ChineseHelper.toSimplifiedChinese(word), categories));
            writer.write(Helper.SEP_ATTRIBUTE);
            writer.write(WordSource.TYPE_ID);
            writer.write(WordSource.BAIDU_BDICT.key);
            writer.write(Helper.SEP_PARTS);
            writer.write(pinyin.toString());
            writer.write(Helper.SEP_NEWLINE);

            counter++;
        }
        return counter;
    }

}
