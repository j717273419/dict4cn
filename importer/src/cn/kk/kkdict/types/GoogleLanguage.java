package cn.kk.kkdict.types;

import java.util.EnumMap;
import java.util.Map;

public enum GoogleLanguage {
  AF(Language.AF, "af", "Afrikaans"),
  SQ(Language.SQ, "sq", "Albanian"),
  AR(Language.AR, "ar", "Arabic"),
  HY(Language.HY, "hy", "Armenian"),
  AZ(Language.AZ, "az", "Azerbaijani"),
  EU(Language.EU, "eu", "Basque"),
  BE(Language.BE, "be", "Belarusian"),
  BN(Language.BN, "bn", "Bengali"),
  BG(Language.BG, "bg", "Bulgarian"),
  CA(Language.CA, "ca", "Catalan"),
  ZH(Language.ZH, "zh-CN", "Chinese"),
  HR(Language.HR, "hr", "Croatian"),
  CS(Language.CS, "cs", "Czech"),
  DA(Language.DA, "da", "Danish"),
  NL(Language.NL, "nl", "Dutch"),
  EN(Language.EN, "en", "English"),
  EO(Language.EO, "eo", "Esperanto"),
  ET(Language.ET, "et", "Estonian"),
  TL(Language.TL, "tl", "Filipino"),
  FI(Language.FI, "fi", "Finnish"),
  FR(Language.FR, "fr", "French"),
  GL(Language.GL, "gl", "Galician"),
  KA(Language.KA, "ka", "Georgian"),
  DE(Language.DE, "de", "German"),
  EL(Language.EL, "el", "Greek"),
  GU(Language.GU, "gu", "Gujarati"),
  HT(Language.HT, "ht", "Haitian Creole"),
  IW(Language.IW, "iw", "Hebrew"),
  HI(Language.HI, "hi", "Hindi"),
  HU(Language.HU, "hu", "Hungarian"),
  IS(Language.IS, "is", "Icelandic"),
  ID(Language.ID, "id", "Indonesian"),
  GA(Language.GA, "ga", "Irish"),
  IT(Language.IT, "it", "Italian"),
  JA(Language.JA, "ja", "Japanese"),
  KN(Language.KN, "kn", "Kannada"),
  KO(Language.KO, "ko", "Korean"),
  LA(Language.LA, "la", "Latin"),
  LV(Language.LV, "lv", "Latvian"),
  LT(Language.LT, "lt", "Lithuanian"),
  MK(Language.MK, "mk", "Macedonian"),
  MS(Language.MS, "ms", "Malay"),
  MT(Language.MT, "mt", "Maltese"),
  NO(Language.NO, "no", "Norwegian"),
  FA(Language.FA, "fa", "Persian"),
  PL(Language.PL, "pl", "Polish"),
  PT(Language.PT, "pt", "Portuguese"),
  RO(Language.RO, "ro", "Romanian"),
  RU(Language.RU, "ru", "Russian"),
  SR(Language.SR, "sr", "Serbian"),
  SK(Language.SK, "sk", "Slovak"),
  SL(Language.SL, "sl", "Slovenian"),
  ES(Language.ES, "es", "Spanish"),
  SW(Language.SW, "sw", "Swahili"),
  SV(Language.SV, "sv", "Swedish"),
  TA(Language.TA, "ta", "Tamil"),
  TE(Language.TE, "te", "Telugu"),
  TH(Language.TH, "th", "Thai"),
  TR(Language.TR, "tr", "Turkish"),
  UK(Language.UK, "uk", "Ukrainian"),
  UR(Language.UR, "ur", "Urdu"),
  VI(Language.VI, "vi", "Vietnamese"),
  CY(Language.CY, "cy", "Welsh"),
  YI(Language.YI, "yi", "Yiddish");
  public final Language                             lng;
  public final String                               key;
  public final String                               name;
  public final static Map<Language, GoogleLanguage> LNG_MAPPING;

  private GoogleLanguage(final Language lng, final String key, final String name) {
    this.lng = lng;
    this.key = key;
    this.name = name;
  }

  static {
    LNG_MAPPING = new EnumMap<>(Language.class);
    for (final GoogleLanguage l : GoogleLanguage.values()) {
      GoogleLanguage.LNG_MAPPING.put(l.lng, l);
    }
  }
}
