package cn.kk.kkdict.extraction;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

import cn.kk.kkdict.types.TranslationSource;
import cn.kk.kkdict.utils.ChineseHelper;
import cn.kk.kkdict.utils.Helper;

public class SogouScelPinyinExtractor {
    private static final String IN_DIR = "X:\\kkdict\\dicts\\sogou";
    private static final String OUT_FILE = "O:\\imedicts\\output-words."+TranslationSource.SOGOU_SCEL.key;
    
    public static void main(String args[]) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(OUT_FILE), Helper.BUFFER_SIZE);

        byte[] buf = new byte[1024];
        String[] pyDict = new String[512];
        File directory = new File(IN_DIR);
        int total = 0;
        if (directory.isDirectory()) {
            File[] files = directory.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".scel");
                }
            });

            for (File f : files) {
                System.out.print("读取SCEL文件'" + f.getAbsolutePath() + "' ... ");
                int counter = 0;
                FileChannel fChannel = new RandomAccessFile(f, "r").getChannel();
                ByteBuffer fBuf = fChannel.map(FileChannel.MapMode.READ_ONLY, 0, (int) fChannel.size());
                fBuf.order(ByteOrder.LITTLE_ENDIAN);

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
                    StringBuilder word = new StringBuilder();

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
                        word.append(new String(buf, 0, len, "UTF-16LE"));
                        fBuf.get(buf, 0, fBuf.getShort());
                        writer.write(ChineseHelper.toSimplifiedChinese(word.toString()));
                        writer.write(Helper.SEP_PARTS);
                        writer.write(py.toString());
                        writer.write(Helper.SEP_NEWLINE);
                        counter++;
                    }
                }
                System.out.println(counter);
                total += counter;
            }
            writer.close();

            System.out.println("\n=====================================");
            System.out.println("总共读取了" + files.length +"个搜狗输入法词库文件");
            System.out.println("总共词汇：" + total);
            System.out.println("=====================================");

        }
        System.out.println();
    }
}
