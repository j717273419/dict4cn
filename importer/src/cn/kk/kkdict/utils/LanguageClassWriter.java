package cn.kk.kkdict.utils;

import java.util.List;

import cn.kk.kkdict.types.Language;

public class LanguageClassWriter {

  /**
   * @param args
   */
  public static void main(String[] args) {

    System.out.println("package cn.kk.dict2go;");

    System.out.println("public final class Language {");

    List<Language> lngs = Language.getSortedLanguages();
    for (Language lng : lngs) {
      if (lng.getId() > 0xff) {
        break;
      }
      System.out.println("public static final int " + lng.name() + " = " + lng.getId() + ";");
    }

    System.out.println("public static String get(final int lng) {");
    System.out.println("switch (lng) {");
    for (Language lng : lngs) {
      if (lng.getId() > 0xff) {
        break;
      }
      System.out.println("case Language." + lng.name() + ": return \"" + lng.name() + "\";");
    }
    System.out.println("default:return String.valueOf(lng);");
    System.out.println("}}}");

  }
}
