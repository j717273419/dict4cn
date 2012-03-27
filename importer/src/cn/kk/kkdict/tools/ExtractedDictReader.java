package cn.kk.kkdict.tools;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import cn.kk.kkdict.utils.Helper;

public class ExtractedDictReader {
    //public static final String IN_FILE = "O:\\handedict\\output-dict_zh_de.handedict_u8";
    //public static final String IN_FILE = "O:\\handedict\\output-dict_zh_en.cedict_u8";    
    public static final String IN_FILE = "O:\\handedict\\output-dict_ja_en.jedict_u8";
    
    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(IN_FILE));
        String line;
        int idx;
        while (null != (line = reader.readLine())) {
            line = line.trim();
            if (-1 != (idx = line.indexOf(Helper.SEP_PARTS))) {
                line = line.substring(0, idx);
            }
            if (Helper.isNotEmptyOrNull(line)) {
                String[] lngs = line.split(Helper.SEP_LIST);
                for (String lng : lngs) {
                    idx = lng.indexOf(Helper.SEP_DEFINITION);
                    String l = lng.substring(0, idx);
                    System.out.print(l + ": ");
                    String definition = lng.substring(idx + 1);
                    String[] defs = definition.split(Helper.SEP_SAME_MEANING);
                    boolean first = true;
                    for (String def : defs) {
                        idx = def.indexOf(Helper.SEP_ATTRIBUTE);
                        String d;
                        if (idx == -1) {
                            d = def;
                        } else {
                            d = def.substring(0, idx);
                        }
                        if (first) {
                            first = false;
                            System.out.print(d);
                        } else {
                            System.out.print(", " + d);
                        }
                    }
                    System.out.println();
                }
                System.out.println();
            }
        }
    }

}
