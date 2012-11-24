import java.util.Locale;

public class JavaLanguagesLister {

  /**
   * @param args
   */
  public static void main(String[] args) {
    final Locale[] locales = Locale.getAvailableLocales();
    for (int i = 0; i < locales.length; i++) {
      final Locale locale = locales[i];
      System.out.println(locale.getLanguage() + ": " + locale.getDisplayName(locale) + " (" + locale.getDisplayName(Locale.ENGLISH) + ")");
    }
  }

}
