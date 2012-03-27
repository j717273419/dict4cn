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

public class SogouScelPinyinExtractor {
    private static final String IN_DIR = Helper.DIR_IN_WORDS+"\\sogou";
    private static final String OUT_DIR = Helper.DIR_OUT_WORDS;
    private static final String OUT_FILE = OUT_DIR + "\\output-words." + WordSource.SOGOU_SCEL.key;

    public static void main(String args[]) throws IOException {
        byte[] buf = new byte[1024];
        String[] pyDict = new String[512];
        File directory = new File(IN_DIR);
        int total = 0;
        if (directory.isDirectory()) {
            new File(OUT_DIR).mkdirs();
            System.out.print("搜索搜狗SCEL文件'" + IN_DIR + "' ... ");

            File[] files = directory.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".scel");
                }
            });
            System.out.println(files.length);

            String tmp;
            BufferedWriter writer = new BufferedWriter(new FileWriter(OUT_FILE), Helper.BUFFER_SIZE);
            for (File f : files) {
                System.out.print("读取SCEL文件'" + f.getAbsolutePath() + "' ... ");
                Set<String> categories = Collections.emptySet();
                if (null != (tmp = Helper.substringBetween(f.getName(), "_", ".scel"))) {
                    categories = Category.parseValid(tmp.split("_"));
                }
                int counter = 0;
                FileChannel fChannel = new RandomAccessFile(f, "r").getChannel();
                ByteBuffer fBuf = ByteBuffer.allocate((int) fChannel.size());
                fChannel.read(fBuf);
                fBuf.order(ByteOrder.LITTLE_ENDIAN);
                fBuf.rewind();
                
                int totalWords = fBuf.getInt(0x120);

                // pinyin offset
                fBuf.position(fBuf.getInt());
                int totalPinyin = fBuf.getInt();
                for (int i = 0; i < totalPinyin; i++) {
                    int idx = fBuf.getShort();
                    int len = fBuf.getShort();
                    fBuf.get(buf, 0, len);
                    pyDict[idx] = new String(buf, 0, len, "UTF-16LE");
                }

                // extract dictionary
                for (int i = 0; i < totalWords; i++) {
                    StringBuilder py = new StringBuilder();

                    int alternatives = fBuf.getShort();
                    int len = fBuf.getShort() / 2;
                    boolean first = true;
                    while (len-- > 0) {
                        int key = fBuf.getShort();
                        if (first) {
                            first = false;
                        } else {
                            py.append(Helper.SEP_PINYIN);
                        }
                        py.append(pyDict[key]);
                    }

                    while (alternatives-- > 0) {
                        len = fBuf.getShort();
                        fBuf.get(buf, 0, len);
                        StringBuilder word = new StringBuilder();
                        word.append(new String(buf, 0, len, "UTF-16LE"));
                        fBuf.get(buf, 0, fBuf.getShort());
                        String wordStr = word.toString();
                        writer.write(Helper.appendCategories(ChineseHelper.toSimplifiedChinese(wordStr), categories));
                        writer.write(Helper.SEP_ATTRIBUTE);
                        writer.write(WordSource.TYPE_ID);
                        writer.write(WordSource.SOGOU_SCEL.key);
                        writer.write(Helper.SEP_PARTS);
                        writer.write(py.toString());
                        writer.write(Helper.SEP_NEWLINE);
                        counter++;
                    }
                }
                System.out.println(counter);
                total += counter;
                fChannel.close();
            }
            writer.close();

            System.out.println("\n=====================================");
            System.out.println("总共读取了" + files.length + "个搜狗输入法词库文件");
            System.out.println("总共词汇：" + total);
            System.out.println("=====================================");

        }
        System.out.println();
    }
}
