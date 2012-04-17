package cn.kk.kkdict.tools;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;

import cn.kk.kkdict.beans.DictByteBufferRow;
import cn.kk.kkdict.utils.ArrayHelper;
import cn.kk.kkdict.utils.Helper;

/**
 * 
 * TODO refactoring
 * 
 */
public class DictReader {
    // public static final String IN_FILE = "O:\\handedict\\output-dict_zh_de.handedict_u8";
    // public static final String IN_FILE = "O:\\handedict\\output-dict_zh_en.cedict_u8";
    public static final String IN_FILE = Helper.DIR_OUT_DICTS + File.separator + "wiki" + File.separator
            + "output-dict.wiki_ak";

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(IN_FILE));
        ByteBuffer bb = ArrayHelper.borrowByteBufferLarge();

        DictByteBufferRow row = new DictByteBufferRow();
        while (-1 != ArrayHelper.readLine(in, bb)) {
            row.parseFrom(bb);

            for (int defIdx = 0; defIdx < row.size(); defIdx++) {
                System.out.println(row.toString(defIdx));
            }

            System.out.println();
        }
    }

}
