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
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.tools.bzip2.CBZip2InputStream;

import cn.kk.kkdict.Configuration;
import cn.kk.kkdict.Configuration.Source;
import cn.kk.kkdict.beans.ByteArrayPairs;
import cn.kk.kkdict.beans.WikiParseStep;
import cn.kk.kkdict.types.Gender;
import cn.kk.kkdict.types.Language;
import cn.kk.kkdict.types.LanguageConstants;
import cn.kk.kkdict.types.WordType;
import cn.kk.kkdict.utils.ArrayHelper;
import cn.kk.kkdict.utils.ChineseHelper;
import cn.kk.kkdict.utils.DictHelper;
import cn.kk.kkdict.utils.Helper;

public class WiktionaryPagesMetaCurrentExtractor
{

  public static final String IN_DIR = Configuration.IMPORTER_FOLDER_SELECTED_DICTS.getPath(Source.DICT_WIKTIONARY);

  public static final String OUT_DIR = Configuration.IMPORTER_FOLDER_EXTRACTED_DICTS.getPath(Source.DICT_WIKTIONARY);

  public static final String OUT_DIR_FINISHED = WiktionaryPagesMetaCurrentExtractor.OUT_DIR + "/finished";

  public static final Language[] RELEVANT_LANGUAGES =
  {Language.EN, Language.RU, Language.PL, Language.JA, Language.KO, Language.ZH, Language.DE, Language.FR, Language.IT,
      Language.ES, Language.PT, Language.NL, Language.SV, Language.UK, Language.VI, Language.CA, Language.NO,
      Language.FI, Language.CS, Language.HU, Language.ID, Language.TR, Language.RO, Language.FA, Language.AR,
      Language.DA, Language.EO, Language.SR, Language.LT, Language.SK, Language.SL, Language.MS, Language.HE,
      Language.BG, Language.KK, Language.EU, Language.VO, Language.WAR, Language.HR, Language.HI, Language.LA,
      Language.BR, Language.LI, Language.LB, Language.HSB, Language.MG, Language.CSB, Language.AST, Language.GL,
      Language.LV, Language.BS, Language.IO, Language.BE, Language.CY, Language.EL, Language.KL, Language.ET,
      Language.NAH, Language.GU, Language.AF, Language.GA, Language.FJ, Language.JV, Language.IS, Language.UR,
      Language.OC, Language.WA, Language.KA, Language.AZ, Language.UZ, Language.FY, Language.SO, Language.TG,
      Language.ML, Language.LN, Language.TH, Language.SI, Language.KW, Language.ZH_MIN_NAN, Language.CHR, Language.TI,
      Language.SCN, Language.FO, Language.ZA, Language.SW, Language.NDS, Language.WO, Language.ROA_RUP, Language.SU,
      Language.LO, Language.MN, Language.AN, Language.AY, Language.MI, Language.TPI, Language.KN, Language.KM,
      Language.IU, Language.ANG, Language.TL, Language.MY, Language.TE, Language.TA, Language.SH, Language.ZU,
      Language.TK, Language.UG, Language.KU, Language.OM, Language.NA, Language.CO, Language.KY, Language.SS,
      Language.GV, Language.SA, Language.SM, Language.MT, Language.SQ, Language.IA, Language.HY, Language.TT,
      Language.YI, Language.MK, Language.RW, Language.QU};


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
      Properties lngNames = loadLanguageNames(fLng);
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
            String title = Helper.substringBetween(line, "    <title>", "</title>");
            if (title != null)
            {
              info.defVal = ChineseHelper.toSimplifiedChinese(title);
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
            if (line.trim().length() == 0 || info.defLng == info.fLng)
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
                  info.writeTitlefLng();
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
            Language tgtLng = findTranslationBlockTgtLng(line, lngNames, info);
            if (tgtLng != null)
            {
              info.tgtLng = tgtLng;
              List<String> tgtVals = Helper.substringBetweens(line, "[[", "]]");
              int idx;
              for (String tgtVal : tgtVals)
              {
                if (tgtVal.startsWith(":" + tgtLng.getKey() + ":"))
                {
                  tgtVal = tgtVal.substring(tgtLng.getKey().length() + 2);
                  idx = tgtVal.indexOf('/');
                  if (idx != -1)
                  {
                    tgtVal = tgtVal.substring(0, idx);
                  }
                  if ((idx = tgtVal.indexOf('|')) != -1)
                  {
                    tgtVal = tgtVal.substring(0, idx);
                  }
                } else if ((idx = (tgtVal.indexOf('#'))) != -1)
                {
                  int idx2 = tgtVal.indexOf('|');
                  if (idx2 != -1)
                  {
                    tgtVal = tgtVal.substring(idx2 + 1);
                  } else
                  {
                    tgtVal = tgtVal.substring(0, idx);
                  }
                }
                info.tgtVal = Helper.unescapeHtml(tgtVal);
                info.writeTitleTgt();
              }
            }
            if (line.startsWith("----"))
            {
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
    }
    return defCounter;
  }


  private Properties loadLanguageNames(Language fLng) throws IOException, FileNotFoundException
  {
    Properties lngNames = new Properties();
    File lngNamesFile = Helper.findResource("lng2name_" + fLng.getKey().toUpperCase());
    if (lngNamesFile != null)
    {
      try (InputStream lngNamesIn = new FileInputStream(lngNamesFile))
      {
        lngNames.load(lngNamesIn);
      }
    }
    return lngNames;
  }


  private Language findTranslationBlockTgtLng(String line, Properties lngNames, ParseInfo info) throws IOException
  {
    Language tgtLng = null;
    String lng;
    if ((lng = Helper.substringBetween(line, "*{{", "}}")) != null)
    {
      tgtLng = findTargetLanguage(lngNames, lng);
    } else if ((lng = Helper.substringBetween(line, "*", "：")) != null)
    {
      tgtLng = findTargetLanguage(lngNames, lng);
    }

    if (tgtLng == null)
    {
      lng = Helper.substringBetween(line, "*{{to|", "|");
      if (lng != null)
      {
        tgtLng = findTargetLanguage(lngNames, lng);
        if (tgtLng != null)
        {
          String tgtVal = Helper.substringBetweenLast(line, "|", "}}");
          if (Helper.isNotEmptyOrNull(tgtVal))
          {
            info.tgtVal = tgtVal;

            info.writeTitleTgt();
          }
          tgtLng = null;
        }
      }
    }
    return tgtLng;
  }


  private Language findTargetLanguage(Properties lngNames, String lng)
  {
    Language tgtLng;
    tgtLng = Language.fromKey(lng);
    if (tgtLng == null)
    {
      String lng2 = lngNames.getProperty(lng);
      if (lng2 != null)
      {
        tgtLng = Language.fromKey(lng2);
      }
    }
    return tgtLng;
  }

  private static Map<String, List<String>> transMap = new HashMap<String, List<String>>();
  static
  {
    List<String> transZh = new LinkedList<String>();
    transZh.add("翻译");
    transMap.put(Language.ZH.getKey(), transZh);
  }


  private boolean findTranslationBlock(String line, ParseInfo info)
  {
    List<String> trans = transMap.get(info.fLng.getKey());
    for (String t : trans)
    {
      if (line.startsWith("===" + t + "===") || line.startsWith("====" + t + "===="))
      {
        return true;
      }
    }
    return false;
  }


  private boolean findDefinitionLanguage(String line, ParseInfo info)
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
    } else if (line.startsWith("==") && null != (lng = Helper.substringBetween(line, "==", "==")))
    {
      defLng = Language.fromKey(lng);
    }
    if (defLng != null)
    {
      info.defLng = defLng;
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

    public String tgtVal;

    public Language tgtLng;

    public Language defLng;

    public Language fLng;

    private String defVal;

    private final BufferedWriter out;


    public ParseInfo(BufferedWriter out, Language fLng)
    {
      this.out = out;
      this.fLng = fLng;
    }


    public void writeTitlefLng() throws IOException
    {
      if (fLng != null && fVal != null && defLng != null && defVal != null)
      {
        out.write(defLng.getKey());
        out.write(Helper.SEP_DEFINITION);
        out.write(defVal);
        out.write(Helper.SEP_LIST);
        out.write(fLng.getKey());
        out.write(Helper.SEP_DEFINITION);
        out.write(fVal);
        out.write(Helper.SEP_NEWLINE);
        defsCount++;
      }
    }


    public void clear()
    {
      tgtVal = null;
      tgtLng = null;
      defLng = null;
      defVal = null;
      fVal = null;
    }


    public void writeTitleTgt() throws IOException
    {
      if (tgtVal != null && tgtLng != null && defLng != null && defVal != null)
      {
        out.write(defLng.getKey());
        out.write(Helper.SEP_DEFINITION);
        out.write(defVal);
        out.write(Helper.SEP_LIST);
        out.write(tgtLng.getKey());
        out.write(Helper.SEP_DEFINITION);
        out.write(tgtVal);
        out.write(Helper.SEP_NEWLINE);
        defsCount++;
      }
    }

  }
}