package cn.kk.kkdict.extraction.dict;

import java.io.IOException;
import java.util.Arrays;

import cn.kk.kkdict.types.Language;
import cn.kk.kkdict.types.TranslationSource;
import cn.kk.kkdict.utils.Helper;

/**
 * Download:
 * @author x_kez
 *
 */
public class EdictJaEnExtractor {
    public static final String EDICT_FILE = Helper.DIR_IN_DICTS+"\\edict\\edict.jp.u8";

    public static final String OUT_DIR = Helper.DIR_OUT_DICTS+"\\edict";

    public static final String[] IRRELEVANT_WORDS_STRINGS = {};

    public static void main(String args[]) throws IOException {
        JECategory[] csValues = JECategory.values();
        String[] validCategoryKeys = new String[csValues.length];
        for (int i = 0; i < csValues.length; i++) {
            JECategory c = csValues[i];
            validCategoryKeys[i] = c.name().toUpperCase();
        }
        Arrays.sort(validCategoryKeys);
        EdictZhDeExtractor.extractDict(TranslationSource.EDICT_JA_EN, EDICT_FILE, Helper.CHARSET_EUCJP, OUT_DIR, Language.JA, Language.EN,
                validCategoryKeys, IRRELEVANT_WORDS_STRINGS);
    }

    public static enum JECategory {
    }
}
