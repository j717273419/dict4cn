package cn.kk.kkdict.tools;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import cn.kk.kkdict.utils.ArrayHelper;
import cn.kk.kkdict.utils.Helper;

public class FilesAppender {
    public final String outFile;
    public final String[] inFiles;

    public FilesAppender(String outFile, String... inFiles) {
        this.outFile = outFile;
        this.inFiles = inFiles;
    }

    public void append() throws IOException {
        long size = Helper.getFilesSize(inFiles);
        System.out.println("合并" + inFiles.length + "文件（" + Helper.formatSpace(size) + "）至'" + outFile + "'。。。");
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outFile), Helper.BUFFER_SIZE);
        ByteBuffer bb = ArrayHelper.borrowByteBufferLarge();
        byte[] array = bb.array();
        for (String f : inFiles) {
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(f), Helper.BUFFER_SIZE);
            int l;
            int lastChar = -1;
            while (-1 != (l = in.read(array))) {
                lastChar = array[l - 1];
                out.write(array, 0, l);
            }
            if (lastChar != '\n') {
                out.write('\n');
            }
            in.close();
        }
        out.close();
        ArrayHelper.giveBack(bb);
        System.out.println("合并成功。合并后文件：'" + outFile + "'（" + Helper.formatSpace(new File(outFile).length()) + "）");
    }

    public static void main(String[] args) throws IOException {
        String inFile0 = Helper.DIR_OUT_DICTS + "\\wiki\\test\\test0.txt";
        String inFile1 = Helper.DIR_OUT_DICTS + "\\wiki\\test\\test1.txt";
        String inFile2 = Helper.DIR_OUT_DICTS + "\\wiki\\test\\test2.txt";
        String outFile = Helper.DIR_OUT_DICTS + "\\wiki\\test\\test-out.txt";

        new FilesAppender(outFile, inFile0, inFile1, inFile2).append();
    }
}
