package cn.kk.kkdict.types;

import java.lang.Character.UnicodeBlock;
import java.text.Collator;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public enum WritingSystem {
    ARABIC("1256", Collator.getInstance(new Locale(Language.AR.key)), Character.UnicodeBlock.ARABIC),
    CHINESE("gb18030", Collator.getInstance(Locale.SIMPLIFIED_CHINESE),
    // Character.UnicodeBlock.CJK_COMPATIBILITY,
    // Character.UnicodeBlock.CJK_COMPATIBILITY_FORMS,
            Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS,
            Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS_SUPPLEMENT,
            Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS,
            Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A,
            Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
    // Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION,
    // Character.UnicodeBlock.CJK_RADICALS_SUPPLEMENT,
    // Character.UnicodeBlock.KANGXI_RADICALS,
    // Character.UnicodeBlock.IDEOGRAPHIC_DESCRIPTION_CHARACTERS,
    ),
    CYRILLIC("1251",
            Collator.getInstance(new Locale(Language.RU.key)),
            Character.UnicodeBlock.CYRILLIC,
            Character.UnicodeBlock.CYRILLIC_SUPPLEMENTARY), // Belarusian, Russian
    DEVANAGARI("UTF-8", Collator.getInstance(new Locale(Language.HI.key)), Character.UnicodeBlock.DEVANAGARI),
    GREEK("1253", Collator.getInstance(new Locale(Language.EL.key)), Character.UnicodeBlock.GREEK),
    HEBREW("1255", Collator.getInstance(new Locale(Language.IW.key)), Character.UnicodeBlock.HEBREW),
    JAPANESE("932",
            Collator.getInstance(Locale.JAPANESE),
            Character.UnicodeBlock.KATAKANA,
            Character.UnicodeBlock.HIRAGANA),
    KOREAN("949", Collator.getInstance(Locale.KOREAN), Character.UnicodeBlock.KANBUN),
    LATIN_BALTIC("1257", Collator.getInstance(new Locale(Language.LV.key))), // Latvian, Lithuanian
    LATIN_CENTRAL_EUROPEAN("1250", Collator.getInstance(new Locale(Language.PL.key))), // Czech, Hungarian, Polish
    LATIN_MALTESE("UTF-8", Collator.getInstance(new Locale(Language.MT.key))), // Maltese
    LATIN_TURKIC("1254", Collator.getInstance(new Locale(Language.TR.key))), // Turkish
    LATIN_WESTERN_EUROPEAN("1252", Collator.getInstance(Locale.GERMAN)), // English, French, German, Italian,
    // Spanish, Swedish
    THAI("874", Collator.getInstance(new Locale(Language.TH.key)), Character.UnicodeBlock.THAI), ;
    private final String encoding;

    private final Set<UnicodeBlock> unicodeBlocks;

    private final Collator collator;

    WritingSystem(String encoding, Collator collator, UnicodeBlock... unicodeBlocks) {
        this.encoding = encoding;
        this.collator = collator;
        this.collator.setStrength(Collator.PRIMARY);
        this.unicodeBlocks = new HashSet<UnicodeBlock>(Arrays.asList(unicodeBlocks));
    }

    public boolean detect(String text) {
        for (char c : text.toCharArray()) {
            if (unicodeBlocks.contains(UnicodeBlock.of(c))) {
                return true;
            }
        }
        return false;
    }
}
