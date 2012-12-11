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
package cn.kk.kkdict.extraction.dict;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.tools.bzip2.CBZip2InputStream;

import cn.kk.kkdict.Configuration;
import cn.kk.kkdict.Configuration.Source;
import cn.kk.kkdict.types.Language;
import cn.kk.kkdict.utils.ArrayHelper;
import cn.kk.kkdict.utils.ChineseHelper;
import cn.kk.kkdict.utils.DictHelper;
import cn.kk.kkdict.utils.Helper;

public class WiktionaryPagesMetaCurrentExtractor
{
  private static final boolean DEBUG = false;

  public static final String IN_DIR = Configuration.IMPORTER_FOLDER_SELECTED_DICTS.getPath(Source.DICT_WIKTIONARY);

  public static final String OUT_DIR = Configuration.IMPORTER_FOLDER_EXTRACTED_DICTS.getPath(Source.DICT_WIKTIONARY);

  public static final String OUT_DIR_FINISHED = WiktionaryPagesMetaCurrentExtractor.OUT_DIR + "/finished";

  private static Map<String, List<String>> transMap = new HashMap<String, List<String>>();
  static
  {
    try (BufferedReader in =
        new BufferedReader(new InputStreamReader(Helper.findResourceAsStream("wikt_trans.txt"), Helper.CHARSET_UTF8)))
    {
      String line = null;
      while (null != (line = in.readLine()))
      {
        String[] strs = line.split("=");
        if (strs.length > 1)
        {
          final String key = strs[0].trim();
          List<String> trls = transMap.get(key);
          if (trls == null)
          {
            trls = new ArrayList<String>();
            transMap.put(key, trls);
          }
          trls.add(strs[1].trim());
        }
      }
    } catch (IllegalArgumentException | IOException e)
    {
      e.printStackTrace();
    }
  }

  private static Map<String, String> lngNamesLowerCased;
  static
  {
    try
    {
      lngNamesLowerCased = loadLanguageNames();
    } catch (IOException e)
    {
      e.printStackTrace();
    }
  }


  public static void main(final String args[]) throws IOException
  {
    final File directory = new File(IN_DIR);
    new File(OUT_DIR_FINISHED).mkdirs();

    if (directory.isDirectory())
    {
      System.out.print("搜索维基词典pages-meta-current.xml文件'" + IN_DIR + "' ... ");
      new File(OUT_DIR).mkdirs();

      final File[] files = directory.listFiles(new FilenameFilter()
      {
        @Override
        public boolean accept(final File dir, final String name)
        {
          return (name.endsWith("-pages-meta-current.xml") || name.endsWith("-pages-meta-current.xml.bz2"))
              && name.contains("wiktionary");
        }
      });
      System.out.println(files.length);

      WiktionaryPagesMetaCurrentExtractor extractor = new WiktionaryPagesMetaCurrentExtractor();
      final long start = System.currentTimeMillis();
      ArrayHelper.WARN = false;
      long total = 0;
      for (final File f : files)
      {
        total += extractor.extractWiktionaryPagesMetaCurrent(f);
        // f.renameTo(new File(OUT_DIR_FINISHED, f.getName()));
      }
      ArrayHelper.WARN = true;

      System.out.println("=====================================");
      System.out.println("总共读取了" + files.length + "个wikt文件，用时："
          + Helper.formatDuration(System.currentTimeMillis() - start));
      System.out.println("总共有效词组：" + total);
      System.out.println("=====================================\n");
    }
  }

  public static enum Step
  {
    Start,
    ParseNamespaces,
    ParseTitle,
    CheckNS,
    ParseText,
    FindText,
    ParseTranslation,
    ParseTextDef
  }


  private int extractWiktionaryPagesMetaCurrent(final File f) throws FileNotFoundException, IOException
  {
    int defCounter = 0;
    try (BufferedReader in =
        f.getAbsolutePath().endsWith(".bz2") ? new BufferedReader(new InputStreamReader(new CBZip2InputStream(
            (new BufferedInputStream(new FileInputStream(f), Helper.BUFFER_SIZE))), Helper.CHARSET_UTF8),
            Helper.BUFFER_SIZE) : new BufferedReader(
            new InputStreamReader(new FileInputStream(f), Helper.CHARSET_UTF8), Helper.BUFFER_SIZE);
        BufferedWriter out =
            new BufferedWriter(new OutputStreamWriter(new FileOutputStream(OUT_DIR + "/" + f.getName() + "_out.txt"),
                Helper.CHARSET_UTF8), Helper.BUFFER_SIZE))
    {
      Language fLng = DictHelper.getWikiLanguage(f.getName());
      ArrayList<String> namespaces = new ArrayList<String>(100);
      Step step = Step.Start;
      String line;
      ParseInfo info = new ParseInfo(out, fLng);
      while (null != (line = in.readLine()))
      {
        switch (step)
        {
          case Start:
            if (line.startsWith("    <namespaces>"))
            {
              step = Step.ParseNamespaces;
            }
            break;
          case ParseNamespaces:
            String ns = Helper.substringBetweenLast(line, ">", "</namespace>");
            if (ns != null)
            {
              namespaces.add(ns);
            } else if (line.startsWith("  <page>"))
            {
              step = Step.ParseTitle;
            }
            break;
          case ParseTitle:
            info.clear();
            info.defLng = fLng;
            String title = Helper.substringBetween(line, "    <title>", "</title>");
            if (title != null)
            {
              info.setDefVal(ChineseHelper.toSimplifiedChinese(title));
              step = Step.CheckNS;
            }
            break;
          case CheckNS:
            String nsId = Helper.substringBetween(line, "    <ns>", "</ns>");
            if ("0".equals(nsId))
            {
              step = Step.FindText;
            } else
            {
              step = Step.ParseTitle;
            }
            break;
          case ParseTextDef:
            if (line.trim().length() == 0 || info.getDefLng() == info.fLng)
            {
              step = Step.ParseText;
            } else if (line.startsWith("#") || !line.contains("#"))
            {
              if (!line.startsWith("{") && !line.contains(":") && !line.contains("：") && !line.contains("|")
                  && !line.contains("--") && !line.startsWith("}") && !line.startsWith(" ") && !line.startsWith("=")
                  && !line.contains("'''") && !line.startsWith("`"))
              {
                final String fLngVal =
                    Helper
                        .unescapeHtml(line)
                        .replaceAll(
                            "([\\*#\\{\\}\\|:\\[\\]])|(<[/a-zA-Z0-9=\"\' ]+>)|(﹝.+?﹞)|(\\(.+?\\))|(\\[.+?\\])|(\\（.+?\\）)|(［.+?］)|(/.+?/)|(adj\\. )|(vt\\. )|(inv\\. )|(n\\.m\\. )|(n\\. )|(\\-[a-zA-Z0-9]+\\-)",
                            "")
                        .replaceAll("([ ]*、[ ]*)|([ ]*;[ ]*)|([ ]*,[ ]*)|([ ]*，[ ]*)|([ ]*；[ ]*)|([ ]*。[ ]*)", ", ")
                        .replaceAll("\\.[\\.]+", "").trim();
                if (Helper.isNotEmptyOrNull(fLngVal))
                {
                  info.fVal = ChineseHelper.toSimplifiedChinese(fLngVal);
                  info.addTitlefLng();
                }
              }
              // else
              // {
              // step = Step.ParseText;
              // }
            }
            if (line.endsWith("</text>"))
            {
              step = Step.ParseTitle;
            }
            break;
          case FindText:
            if (line.startsWith("      <text xml:space=\"preserve\">"))
            {
              line = line.substring("      <text xml:space=\"preserve\">".length());
              step = Step.ParseText;
              // out.write("\n\n\n===>>> " + info.title + "\n");
            } else if (line.startsWith("  <page>"))
            {
              step = Step.ParseTitle;
              break;
            } else
            {
              break;
            }
          case ParseText:
            line = ChineseHelper.toSimplifiedChinese(line);
            if (findDefinitionLanguage(line, info))
            {
              step = Step.ParseTextDef;
            } else if (findTranslationBlock(line, info))
            {
              step = Step.ParseTranslation;
              break;
            }
            if (line.endsWith("</text>"))
            {
              step = Step.ParseTitle;
            }
            break;
          case ParseTranslation:
            line = ChineseHelper.toSimplifiedChinese(line);
            Language tgtLng = findTranslationBlockTgtLng(line, info);
            if (tgtLng != null)
            {
              info.tgtLng = tgtLng;

              findTranslationBlockTgtVal1(line, info);

              findTranslationBlockTgtVal2(line, info);
            }
            if (line.startsWith("----") || line.startsWith("==="))
            {
              // TODO check ===
              step = Step.ParseText;
            }
            if (line.endsWith("</text>"))
            {
              step = Step.ParseTitle;
            }
            break;
        }
      }
      defCounter += info.defsCount;
      info.clear();
    }
    return defCounter;
  }


  private void findTranslationBlockTgtVal2(String line, ParseInfo info)
  {
    List<String> tgtVals =
        Helper.substringBetweens(
            line.replace("]][[", Helper.SEP_SAME_MEANING).replace("]] [[",
                Helper.SEP_SAME_MEANING + Helper.SEP_SAME_MEANING), "[[", "]]");
    int idx;
    for (String tgtVal : tgtVals)
    {
      String[] st = tgtVal.split(Helper.SEP_SAME_MEANING);
      StringBuilder sb = new StringBuilder();
      boolean found = false;
      for (int i = 0; i < st.length; i++)
      {
        String t = st[i];
        if (t.isEmpty())
        {
          sb.append(" ");
        } else
        {
          if (t.startsWith(":" + info.tgtLng.getKey() + ":"))
          {
            t = t.substring(info.tgtLng.getKey().length() + 2);
            idx = t.indexOf('/');
            if (idx != -1)
            {
              t = t.substring(0, idx);
            }
            if ((idx = t.indexOf('|')) != -1)
            {
              t = t.substring(0, idx);
            }
          } else if ((idx = (t.indexOf('#'))) != -1)
          {
            int idx2 = t.indexOf('|');
            if (idx2 != -1)
            {
              t = t.substring(idx2 + 1);
            } else
            {
              t = t.substring(0, idx);
            }
          }
          sb.append(Helper.unescapeHtml(t));
          found = true;
        }
      }
      if (found)
      {
        info.tgtVal = sb.toString();
        info.addTitleTgt();
      }
    }
  }


  private void findTranslationBlockTgtVal1(String line, ParseInfo info)
  {
    List<String> tgtVals =
        Helper.substringBetweens(
            line.replace("}}{{", Helper.SEP_SAME_MEANING).replace("}} {{",
                Helper.SEP_SAME_MEANING + Helper.SEP_SAME_MEANING), "{{", "}}");
    for (String tgtVal : tgtVals)
    {
      if (tgtVal.length() < 5 || Helper.containsAny(tgtVal, '=', '[', ':'))
      {
        continue;
      }
      String[] st = tgtVal.split(Helper.SEP_SAME_MEANING);
      StringBuilder sb = new StringBuilder();
      boolean found = false;
      for (int i = 0; i < st.length; i++)
      {
        String t = st[i];
        if (t.isEmpty())
        {
          sb.append(" ");
        } else
        {
          int idx = t.indexOf('|');
          if (idx != -1)
          {
            int idx1 = t.indexOf('|', idx + 1);
            if (idx1 != -1)
            {
              Language tgtLng = findTargetLanguage(t.substring(idx + 1, idx1));
              if (tgtLng != null)
              {
                info.tgtLng = tgtLng;
              }
              int idx2 = t.indexOf('|', idx1 + 1);
              if (idx2 == -1 && t.length() > idx1 + 1)
              {
                idx2 = t.length();
              }
              if (idx2 != -1)
              {
                String val = t.substring(idx1 + 1, idx2);
                int idx3 = val.indexOf('#');
                if (idx3 != -1)
                {
                  val = val.substring(0, idx3);
                }
                found = true;
                sb.append(Helper.unescapeHtml(val));
                // System.out.println(tgtLng + " -> " + tgtVal + ": " +
                // t);
              }
            }
          }
        }
      }
      if (found)
      {
        info.tgtVal = st.toString();
        info.addTitleTgt();
      }
    }
  }


  private static Map<String, String> loadLanguageNames() throws IOException, FileNotFoundException
  {
    Properties lngNames = new Properties();
    for (Language lng : Language.values())
    {
      File lngNamesFile = Helper.findResource("lng2name_" + lng.getKey().toUpperCase() + ".txt");
      if (lngNamesFile != null)
      {
        try (InputStream lngNamesIn = new FileInputStream(lngNamesFile))
        {
          lngNames.load(new InputStreamReader(lngNamesIn, Helper.CHARSET_UTF8));
        }
      }
    }
    Map<String, String> result = new HashMap<String, String>();
    for (Object k : lngNames.keySet())
    {
      String key = (String) k;
      result.put(key.toLowerCase(), lngNames.getProperty(key).toLowerCase());
    }
    System.out.println("总：" + lngNames.size() + " 语言名代号 (过滤后：" + result.size() + ")");
    return result;
  }


  private Language findTranslationBlockTgtLng(String line, ParseInfo info) throws IOException
  {
    Language tgtLng = null;
    String lng;
    if ((lng = Helper.substringBetween(line, "*{{", "}}")) != null)
    {
      tgtLng = findTargetLanguage(lng);
    }
    if (tgtLng == null
        && line.startsWith("*:")
        && ((lng = Helper.substringBetween(line, "*:", "：")) != null || (lng = Helper.substringBetween(line, "*:", ":")) != null))
    {
      tgtLng = findTargetLanguage(lng);
    } else if (tgtLng == null
        && line.startsWith("*")
        && ((lng = Helper.substringBetween(line, "*", "：")) != null || (lng = Helper.substringBetween(line, "*", ":")) != null))
    {
      tgtLng = findTargetLanguage(lng);
    }
    if (tgtLng == null && (lng = Helper.substringBetween(line, "{{T|", "}}")) != null)
    {
      tgtLng = findTargetLanguage(lng);
    }
    if (tgtLng == null && (lng = Helper.substringBetween(line, "{{T|", "|")) != null)
    {
      tgtLng = findTargetLanguage(lng);
    }

    if (tgtLng == null)
    {
      lng = Helper.substringBetween(line, "*{{to|", "|");
      if (lng != null)
      {
        tgtLng = findTargetLanguage(lng);
        if (tgtLng != null)
        {
          String tgtVal = Helper.substringBetweenLast(line, "|", "}}");
          if (Helper.isNotEmptyOrNull(tgtVal))
          {
            info.tgtVal = tgtVal;

            info.addTitleTgt();
          }
          tgtLng = null;
        }
      }
    }
    return tgtLng;
  }


  private Language findTargetLanguage(String lng)
  {
    Language tgtLng;
    tgtLng = Language.fromKey(lng);
    if (tgtLng == null)
    {
      String lng2 = lngNamesLowerCased.get(lng.toLowerCase());
      if (lng2 != null)
      {
        tgtLng = Language.fromKey(lng2);
      }
    }
    return tgtLng;
  }


  private boolean findTranslationBlock(String line, ParseInfo info)
  {
    final String lineLower = line.toLowerCase();
    if (lineLower.startsWith("{{-trad-}}"))
    {
      return true;
    }
    List<String> trans = transMap.get(info.fLng.getKey());
    if (trans == null)
    {
      System.err.println("没找到翻译代码！");
    } else
      for (String t : trans)
      {
        String translationText = t.toLowerCase();
        if (lineLower.startsWith("===" + translationText + "===")
            || lineLower.startsWith("====" + translationText + "====")
            || lineLower.startsWith("=====" + translationText + "=====")
            || lineLower.startsWith("=== " + translationText + " ===")
            || lineLower.startsWith("==== " + translationText + " ====")
            || lineLower.startsWith("===== " + translationText + " ====="))
        {
          return true;
        }
      }
    return false;
  }


  private boolean findDefinitionLanguage(String line, ParseInfo info) throws IOException
  {
    String lng = null;
    Language defLng = null;
    if (line.startsWith("{{汉语") || line.startsWith("{{=n=|汉|"))
    {
      defLng = Language.ZH;
    } else if (line.startsWith("{{-")
        && (null != (lng = Helper.substringBetween(line, "{{-", "-}}")) || null != (lng =
            Helper.substringBetween(line, "{{-", "-|"))))
    {
      defLng = Language.fromKey(lng);
    } else if (line.startsWith("=[[") && null != (lng = Helper.substringBetween(line, "=[[", "]]=")))
    {
      defLng = Language.fromKey(lng);
    } else if (line.startsWith("===") && null != (lng = Helper.substringBetweenNarrow(line, "|", "}}")))
    {
      defLng = findTargetLanguage(lng);
    } else if (line.startsWith("==") && null != (lng = Helper.substringBetween(line, "==", "==")))
    {
      defLng = Language.fromKey(lng);
      if (defLng == null)
      {
        defLng = findTargetLanguage(lng);
      }
      if (defLng == null)
      {
        lng = Helper.substringBetween(lng, "|", "}}");
        if (lng != null)
        {
          defLng = Language.fromKey(lng);
        }
      }
    }
    if (defLng != null)
    {
      info.setDefLng(defLng);
      return true;
    } else
    {
      return false;
    }
  }

  public static class ParseInfo
  {
    public String fVal;

    public int defsCount;

    public List<String> vals = new ArrayList<String>();

    public List<String> lngs = new ArrayList<String>();

    public Language tgtLng;

    public String tgtVal;

    private Language defLng;

    public Language fLng;

    private String defVal;

    private final BufferedWriter out;


    public ParseInfo(BufferedWriter out, Language fLng)
    {
      this.out = out;
      this.fLng = fLng;
    }


    public void write() throws IOException
    {
      final int size = lngs.size();
      if (size > 1)
      {
        for (int i = 0; i < size; i++)
        {
          if (i > 0)
          {
            out.write(Helper.SEP_LIST);
          }
          String key = lngs.get(i);
          String val = vals.get(i);
          out.write(key);
          out.write(Helper.SEP_DEFINITION);
          out.write(val.replaceAll("^([,\\.，。；?!！？\\|\\-\\+\\* ]+)|([,\\.，。；?!！？\\|\\-\\+\\* ]+$)|('[']+)", ""));
        }
        out.write(Helper.SEP_NEWLINE);
        defsCount++;
      }
      clearNow();
    }


    public void clear() throws IOException
    {
      write();
    }


    private void clearNow() throws IOException
    {
      vals.clear();
      lngs.clear();
      fVal = null;
      tgtVal = null;
      tgtLng = null;
    }


    public void addTitleTgt()
    {
      if (tgtVal != null && Helper.isNotEmptyOrNull(tgtVal) && tgtLng != null && defLng != null && defVal != null
          && defLng != tgtLng)
      {
        if (!lngs.contains(getDefLng().getKey()))
        {
          lngs.add(getDefLng().getKey());
          vals.add(getDefVal());
        }
        lngs.add(tgtLng.getKey());
        vals.add(tgtVal);
      } else if (DEBUG)
      {
        System.err.println(getDefLng() + "=" + getDefVal() + ", " + tgtLng + "=" + tgtVal);
      }
    }


    public void addTitlefLng()
    {
      if (fVal != null && Helper.isNotEmptyOrNull(fVal) && fLng != null && defLng != null && defVal != null
          && defLng != tgtLng)
      {
        if (!lngs.contains(getDefLng().getKey()))
        {
          lngs.add(getDefLng().getKey());
          vals.add(getDefVal());
        }
        lngs.add(fLng.getKey());
        vals.add(fVal);
      } else if (DEBUG)
      {
        System.err.println(getDefLng() + "=" + getDefVal() + ", " + fLng + "=" + fVal);
      }
    }


    public String getDefVal()
    {
      return defVal;
    }


    public void setDefVal(String defVal) throws IOException
    {
      write();
      this.defVal = defVal;
    }


    public Language getDefLng()
    {
      return defLng;
    }


    public void setDefLng(Language defLng) throws IOException
    {
      write();
      this.defLng = defLng;
    }

  }
}