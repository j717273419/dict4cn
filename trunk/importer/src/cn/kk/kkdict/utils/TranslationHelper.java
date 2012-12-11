/*  Copyright (c) 2010 Xiaoyun Zhu
 * 
 *  Permission is hereby granted, free of charge, to any person obtaining a copy  
 *  of this software and associated documentation files (the "Software"), to deal  
 *  in the Software without restriction, including without limitation the rights  
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell  
 *  copies of the Software, and to permit persons to whom the Software is  
 *  furnished to do so, subject to the following conditions:
 *  
 *  The above copyright notice and this permission notice shall be included in  
 *  all copies or substantial portions of the Software.
 *  
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR  
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,  
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE  
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER  
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,  
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN  
 *  THE SOFTWARE.  
 */
package cn.kk.kkdict.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import cn.kk.kkdict.types.GoogleLanguage;

public final class TranslationHelper
{

  private static int GC = 0;


  /**
   * Available languages: zh-CN, de, en, it, ja, ko, ru, fr, la (see
   * https://code
   * .google.com/apis/language/translate/v2/using_rest.html#language-params)
   * 
   * @param source
   * @param target
   * @param input
   * @return
   */
  public static List<String> getGoogleTranslations(final GoogleLanguage source, final GoogleLanguage target,
      final String input)
  {
    List<String> translations = new ArrayList<>();
    try
    {
      final String url =
          String.format("http://translate.google.com/translate_a/t?client=t&text=%s&sl=%s&tl=%s",
              URLEncoder.encode(input, Helper.CHARSET_UTF8.name()), source.key, target.key);
      final URLConnection conn = Helper.getUrlConnection(url);
      conn.setRequestProperty("User-Agent", "Mozilla/6.0 (rev. " + GC++ + ")");
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));)
      {
        final String line = reader.readLine();
        if (line != null)
        {
          final int len = line.length();
          final char[] chars = line.toCharArray();
          int brackets = 0;
          int commas = 0;
          boolean str = false;
          char ch;
          char lastCh = '\n';
          boolean processed;
          int start = -1;
          int end = -1;
          int proStart = -1;
          int proEnd = -1;
          for (int i = 0; i < len; i++)
          {
            processed = false;
            ch = chars[i];
            if (!str)
            {
              if (ch == '"')
              {
                str = true;
                processed = true;
              }
            } else if (ch == '"' && lastCh != '\\')
            {
              str = false;
              processed = true;
            }
            if (!processed && !str)
            {
              if (ch == '[')
              {
                brackets++;
              } else if (ch == ']')
              {
                brackets--;
              } else if (ch == ',' && brackets == 1)
              {
                commas++;
                if (commas == 1)
                {
                  proStart = i + 2;
                } else if (commas == 2)
                {
                  proEnd = i - 1;
                } else if (commas == 5)
                {
                  start = i + 2;
                } else if (commas == 6)
                {
                  end = i - 1;
                }
              }
            }

            lastCh = ch;
          }
          if (proEnd > proStart + 10 && proStart != -1)
          {
            String trls = line.substring(proStart, proEnd);
            findGoogleTranslationsByProposals(translations, input, trls);
          } else if (end > start + 3 + input.length() && start != -1)
          {
            String trls = line.substring(start, end);
            if (trls.substring(2, 2 + input.length()).equals(input))
            {
              findGoogleTranslationsBySelections(translations, input, trls, line);
            }
          }

          // final String translationRaw = Helper.substringBetween(line,
          // "]],[[\"", "]]");
          // final String[] translationParts =
          // translationRaw.split("[\\[\\],\"]+");
          // // System.out.println(Arrays.toString(translationParts));
          // if ((translationParts.length > 5)
          // && input.replaceAll("\\b+", Helper.EMPTY_STRING).equals(
          // translationParts[0].replace(" ", Helper.EMPTY_STRING)))
          // {
          // for (int i = 2; i < translationParts.length; i += 4)
          // {
          // if (Integer.parseInt(translationParts[i + 1]) > 0)
          // {
          // translations.add(translationParts[i]);
          // }
          // if (translations.size() == TranslationHelper.MAX_RESULTS)
          // {
          // break;
          // }
          // }
          // }
        }
      }
    } catch (final Throwable e)
    {
      System.err.println("Failed to get google translations '" + input + "': " + e.toString());
    }
    if ((translations != null) && !translations.isEmpty())
    {
      return translations;
    } else
    {
      return Helper.EMPTY_STRING_LIST;
    }
  }


  private static void findGoogleTranslationsByProposals(List<String> translations, String input, String trls)
  {
    final int len = trls.length();
    final char[] chars = trls.toCharArray();
    int brackets = 0;
    int commas = 0;
    boolean str = false;
    char ch;
    char lastCh = '\n';
    boolean processed;
    int proStart = -1;
    int proEnd = -1;
    for (int i = 0; i < len; i++)
    {
      processed = false;
      ch = chars[i];
      if (!str)
      {
        if (ch == '"')
        {
          str = true;
          processed = true;
        }
      } else if (ch == '"' && lastCh != '\\')
      {
        str = false;
        processed = true;
      }
      if (!processed && !str)
      {
        if (ch == '[')
        {
          brackets++;
        } else if (ch == ']')
        {
          brackets--;
          if (brackets == 0)
          {
            commas = 0;
          }
        } else if (ch == ',' && brackets == 1)
        {
          commas++;
          if (commas == 1)
          {
            proStart = i + 2;
          } else if (commas == 2)
          {
            proEnd = i - 1;
            findGoogleTranslationsByProposal(translations, trls.substring(proStart, proEnd));
          }
        }
      }

      lastCh = ch;
    }
  }


  private static void findGoogleTranslationsByProposal(List<String> translations, String trls)
  {
    final int len = trls.length();
    final char[] chars = trls.toCharArray();
    boolean str = false;
    char ch;
    char lastCh = '\n';
    int proStart = -1;
    int proEnd = -1;
    for (int i = 0; i < len; i++)
    {
      ch = chars[i];
      if (!str)
      {
        if (ch == '"')
        {
          str = true;
          proStart = i + 1;
        }
      } else if (ch == '"' && lastCh != '\\')
      {
        str = false;
        proEnd = i;
        if (proEnd > proStart)
        {
          translations.add(trls.substring(proStart, proEnd));
        }
      }

      lastCh = ch;
    }
  }


  private static void findGoogleTranslationsBySelections(List<String> translations, final String input, String trls,
      String line)
  {
    final int len = trls.length();
    final char[] chars = trls.toCharArray();
    int brackets = 0;
    boolean str = false;
    char ch;
    char lastCh = '\n';
    boolean processed;
    int start = -1;
    int end = -1;
    List<String> parts = new LinkedList<String>();
    for (int i = 0; i < len; i++)
    {
      processed = false;
      ch = chars[i];
      if (!str)
      {
        if (ch == '"')
        {
          str = true;
          processed = true;
        }
      } else if (ch == '"' && lastCh != '\\')
      {
        str = false;
        processed = true;
      }
      if (!processed && !str)
      {
        if (ch == '[')
        {
          brackets++;
          if (brackets == 1)
          {
            start = i + 1;
          }
        } else if (ch == ']')
        {
          brackets--;
          if (brackets == 0)
          {
            end = i;
            final String part = trls.substring(start, end);
            // System.out.println(part + ", " + start + ", " + end);
            if (part.length() > 1 + input.length() && part.substring(1, 1 + input.length()).equals(input))
            {
              int open = part.indexOf("[[");
              int close = part.indexOf("]]");
              if (open != -1 && close > open)
              {
                parts.add(part.substring(open + 1, close + 1));
              }
            }
          }
        }
      }
      lastCh = ch;
    }
    if (!parts.isEmpty())
    {
      findGoogleTranslationsBySelection(translations, parts, line);
    }
  }


  private static void findGoogleTranslationsBySelection(List<String> translations, List<String> parts, String line)
  {
    if (parts.size() == 1)
    {
      String part = parts.get(0);
      final int size = getGoogleTranslationsSize(part);
      for (int i = 0; i < size; i++)
      {
        String trl = getGoogleTranslation(part, i, true);
        if (trl != null)
        {
          translations.add(trl);
        }
      }
    } else
    {
      // StringBuilder sb = new StringBuilder();
      // boolean first = true;
      // for (String part : parts)
      // {
      // final String trl = getGoogleTranslation(part, 0);
      // if (Helper.isNotEmptyOrNull(trl))
      // {
      // if (first)
      // {
      // first = false;
      // } else
      // {
      // sb.append(" ");
      // }
      // sb.append(trl);
      // }
      // }
      translations.add(getGoogleTranslation(line.substring(2), 0, false));
    }
  }


  private static String getGoogleTranslation(String test, int idx, boolean checkPoints)
  {
    int counter = 0;
    final int len = test.length();
    final char[] chars = test.toCharArray();
    boolean str = false;
    char ch;
    char lastCh = '\n';
    boolean processed;
    int start = -1;
    int end = -1;
    boolean check = false;
    int chStart = -1;
    int chEnd = -1;
    for (int i = 0; i < len; i++)
    {
      processed = false;
      ch = chars[i];
      if (!str)
      {
        if (ch == '"')
        {
          str = true;
          processed = true;
          start = i + 1;
        }
      } else if (ch == '"' && lastCh != '\\')
      {
        str = false;
        processed = true;
        if (counter == idx + 1)
        {
          end = i;
          if (start != -1 && end > start)
          {
            if (checkPoints)
            {
              check = true;
            } else
            {
              return test.substring(start, end);
            }
          } else
          {
            return null;
          }
        }
      }
      if (!processed && !str)
      {
        if (ch == '[')
        {
          counter++;
        }
        if (check && ch == ',')
        {
          if (chStart == -1)
          {
            chStart = i + 1;
          } else
          {
            chEnd = i;
            String pointsStr = test.substring(chStart, chEnd);
            if (!pointsStr.isEmpty() && Integer.parseInt(pointsStr) > 0)
            {
              return test.substring(start, end);
            } else
            {
              return null;
            }
          }
        }
      }
      lastCh = ch;
    }

    return null;
  }


  private static int getGoogleTranslationsSize(String test)
  {
    int counter = 0;
    final int len = test.length();
    final char[] chars = test.toCharArray();
    boolean str = false;
    char ch;
    char lastCh = '\n';
    boolean processed;
    for (int i = 0; i < len; i++)
    {
      processed = false;
      ch = chars[i];
      if (!str)
      {
        if (ch == '"')
        {
          str = true;
          processed = true;
        }
      } else if (ch == '"' && lastCh != '\\')
      {
        str = false;
        processed = true;
      }
      if (!processed && !str)
      {
        if (ch == '[')
        {
          counter++;
        }
      }
      lastCh = ch;
    }
    return counter;
  }

}
