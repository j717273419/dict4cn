package cn.kk.dict2go;

public final class Language {
  public static final int ZH = 1;
  public static final int EN = 2;
  public static final int DE = 3;
  public static final int FR = 4;
  public static final int IT = 5;
  public static final int JA = 6;
  public static final int KO = 7;
  public static final int RU = 8;
  public static final int ES = 9;
  public static final int PL = 10;

  public static String get(final int lng) {
    switch (lng) {
      case Language.ZH:
        return "ZH";
      case Language.EN:
        return "EN";
      case Language.DE:
        return "DE";
      default:
        return String.valueOf(lng);
    }
  }
}
