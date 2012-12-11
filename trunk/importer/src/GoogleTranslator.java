import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import cn.kk.kkdict.types.GoogleLanguage;
import cn.kk.kkdict.utils.TranslationHelper;

public class GoogleTranslator
{
  /**
   * @param args
   * @throws IOException
   * @throws FileNotFoundException
   */
  public static void main(String[] args) throws FileNotFoundException, IOException
  {
    System.out.println(translate(GoogleLanguage.ZH, "好"));
  }


  private static String translate(GoogleLanguage lng, String text)
  {
    StringBuilder sb = new StringBuilder();
    sb.append(lng.lng.getKey()).append("=").append(text);
    final GoogleLanguage[] values = GoogleLanguage.values();
    
    Arrays.sort(values, new Comparator<GoogleLanguage>()
    {
      @Override
      public int compare(GoogleLanguage o1, GoogleLanguage o2)
      {
        return o1.lng.getKey().compareTo(o2.lng.getKey());
      }
    });
    boolean found = false;
    for (GoogleLanguage l : values)
    {
      if (l != lng)
      {
        List<String> trls = TranslationHelper.getGoogleTranslations(lng, l, text);
        for (String trl : trls)
        {
          found = true;
          sb.append("\n");
          sb.append(l.lng.getKey()).append("=").append(trl);
        }
      }
    }
    if (found)
    {
      return sb.toString();
    } else
    {
      return null;
    }
  }

}
