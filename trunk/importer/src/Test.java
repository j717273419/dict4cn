import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.SortedMap;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import cn.kk.kkdict.utils.Helper;
import cn.kk.kkdict.utils.PinyinHelper;
import cn.kk.kkdict.utils.TranslationHelper;

public class Test {

    /**
     * @param args
     * @throws IOException
     * @throws DataFormatException
     */
    public static void main(String[] args) throws IOException, DataFormatException {
        // System.sout.println(TranslationHelper.getGoogleTranslations(TranslationHelper.GoogleLanguage.CN,
        // TranslationHelper.GoogleLanguage.DE, "你是谁"));
        //
        System.out.println(Integer.toHexString('\u001e'));
        System.out.println((byte)0xb1);
        for (int level = -1; level < 10; level++) {
            ByteBuffer bb = Helper.compressFile("D:\\header.dat", level);
            byte[] data = new byte[2];
            bb.get(data);
            for (byte b : data) {
                int i = b & 0xff;
                if (i < 0xf) {
                    System.out.print(0);
                }
                System.out.print(Integer.toHexString(i) + " ");
            }
            System.out.println();
            
        }
        System.out.println("----------");
        for (int level = -1; level < 10; level++) {
            ByteBuffer bb = Helper.compressFile("D:\\header.dat", "D:\\dict.dlt", level);
            byte[] data = new byte[2];
            bb.get(data);
            for (byte b : data) {
                int i = b & 0xff;
                if (i < 0xf) {
                    System.out.print(0);
                }
                System.out.print(Integer.toHexString(i) + " ");
            }
            System.out.println();
        }
//        try {
//            Helper.debug(Helper.decompressFile("D:\\stream.dfl", "D:\\dict.dlt").array());
//        } catch (Exception e) {
//            System.out.println(e.toString());
//            e.printStackTrace();
//        }
        Helper.writeBytes(Helper.decompressFile("D:\\stream5.dfl").array(), "d:\\stream5.raw");
    }

}
