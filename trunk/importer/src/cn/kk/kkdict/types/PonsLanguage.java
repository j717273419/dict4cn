package cn.kk.kkdict.types;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public enum PonsLanguage {
    EN(Language.EN, "en", "English"),
    FR(Language.FR, "fr", "French"),
    DE(Language.DE, "de", "German"),
    EL(Language.EL, "el", "Greek"),
    IT(Language.IT, "it", "Italian"),
    LA(Language.LA, "la", "Latin"),
    PL(Language.PL, "pl", "Polish"),
    PT(Language.PT, "pt", "Portuguese"),
    RU(Language.RU, "ru", "Russian"),
    SL(Language.SL, "sl", "Slovenian"),
    ES(Language.ES, "es", "Spanish"),
    TR(Language.TR, "tr", "Turkish"),
    DA(Language.DA, "da", "Danish"),
    NL(Language.NL, "nl", "Dutch"),
    HU(Language.HU, "hu", "Hungarian"),
    NO(Language.NO, "no", "Norwegian"),
    SV(Language.SV, "sv", "Swedish");

    public final Language lng;
    public final String key;
    public final String name;
    public final static Map<Language, PonsLanguage> LNG_MAPPING;

    private PonsLanguage(Language lng, String key, String name) {
        this.lng = lng;
        this.key = key;
        this.name = name;
    }

    static {
        LNG_MAPPING = new EnumMap<Language, PonsLanguage>(Language.class);
        for (PonsLanguage l : values()) {
            LNG_MAPPING.put(l.lng, l);
        }
    }
}
