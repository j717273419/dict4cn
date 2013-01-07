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

import org.apache.tools.bzip2.CBZip2InputStream;

import cn.kk.kkdict.Configuration;
import cn.kk.kkdict.Configuration.Source;
import cn.kk.kkdict.beans.ParseInfo;
import cn.kk.kkdict.types.Language;
import cn.kk.kkdict.utils.ArrayHelper;
import cn.kk.kkdict.utils.ChineseHelper;
import cn.kk.kkdict.utils.DictHelper;
import cn.kk.kkdict.utils.Helper;

public class WiktionaryPagesMetaCurrentExtractor {
  public static final boolean              DEBUG            = false;

  public static String                     IN_DIR           = Configuration.IMPORTER_FOLDER_SELECTED_DICTS.getPath(Source.DICT_WIKTIONARY);

  public static String                     OUT_DIR          = Configuration.IMPORTER_FOLDER_EXTRACTED_DICTS.getPath(Source.DICT_WIKTIONARY);

  static {
    WiktionaryPagesMetaCurrentExtractor.IN_DIR = Configuration.IMPORTER_FOLDER_SELECTED_DICTS.getPath(Source.DICT_WIKTIONARY) + "/test";

    WiktionaryPagesMetaCurrentExtractor.OUT_DIR = Configuration.IMPORTER_FOLDER_EXTRACTED_DICTS.getPath(Source.DICT_WIKTIONARY) + "/test";
  }

  public static final String               OUT_DIR_FINISHED = WiktionaryPagesMetaCurrentExtractor.OUT_DIR + "/finished";

  private static Map<String, List<String>> transMap         = new HashMap<>();
  static {
    try (BufferedReader in = new BufferedReader(new InputStreamReader(Helper.findResourceAsStream("wikt_trans.txt"), Helper.CHARSET_UTF8))) {
      String line = null;
      while (null != (line = in.readLine())) {
        String[] strs = line.split("=");
        if (strs.length > 1) {
          final String key = strs[0].trim();
          List<String> trls = WiktionaryPagesMetaCurrentExtractor.transMap.get(key);
          if (trls == null) {
            trls = new ArrayList<>();
            WiktionaryPagesMetaCurrentExtractor.transMap.put(key, trls);
          }
          trls.add(strs[1].trim());
        }
      }
    } catch (IllegalArgumentException | IOException e) {
      e.printStackTrace();
    }
  }

  private static Map<String, String>       lngNamesLowerCased;
  static {
    try {
      WiktionaryPagesMetaCurrentExtractor.lngNamesLowerCased = WiktionaryPagesMetaCurrentExtractor.loadLanguageNames();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void main(final String args[]) throws IOException {
    final File directory = new File(WiktionaryPagesMetaCurrentExtractor.IN_DIR);
    new File(WiktionaryPagesMetaCurrentExtractor.OUT_DIR_FINISHED).mkdirs();

    if (directory.isDirectory()) {
      System.out.print("搜索维基词典pages-meta-current.xml文件'" + WiktionaryPagesMetaCurrentExtractor.IN_DIR + "' ... ");
      new File(WiktionaryPagesMetaCurrentExtractor.OUT_DIR).mkdirs();

      final File[] files = directory.listFiles(new FilenameFilter() {
        @Override
        public boolean accept(final File dir, final String name) {
          return (name.endsWith("-pages-meta-current.xml") || name.endsWith("-pages-meta-current.xml.bz2")) && name.contains("wiktionary");
        }
      });
      System.out.println(files.length);

      WiktionaryPagesMetaCurrentExtractor extractor = new WiktionaryPagesMetaCurrentExtractor();
      final long start = System.currentTimeMillis();
      ArrayHelper.WARN = false;
      long total = 0;
      for (final File f : files) {
        total += extractor.extractWiktionaryPagesMetaCurrent(f);
        // f.renameTo(new File(OUT_DIR_FINISHED, f.getName()));
      }
      ArrayHelper.WARN = true;

      System.out.println("=====================================");
      System.out.println("总共读取了" + files.length + "个wikt文件，用时：" + Helper.formatDuration(System.currentTimeMillis() - start));
      System.out.println("总共有效词组：" + total);
      System.out.println("=====================================\n");
    }
  }

  public static enum Step {
    Start,
    ParseNamespaces,
    ParseTitle,
    CheckNS,
    ParseText,
    FindText,
    ParseTranslation,
    ParseTextDef
  }

  private int extractWiktionaryPagesMetaCurrent(final File f) throws FileNotFoundException, IOException {
    int defCounter = 0;
    this.notifyNoTranslationKey = true;
    try (BufferedReader in = f.getAbsolutePath().endsWith(".bz2") ? new BufferedReader(new InputStreamReader(new CBZip2InputStream((new BufferedInputStream(
        new FileInputStream(f), Helper.BUFFER_SIZE))), Helper.CHARSET_UTF8), Helper.BUFFER_SIZE) : new BufferedReader(new InputStreamReader(
        new FileInputStream(f), Helper.CHARSET_UTF8), Helper.BUFFER_SIZE);
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(WiktionaryPagesMetaCurrentExtractor.OUT_DIR + "/" + f.getName()
            + "_out.txt"), Helper.CHARSET_UTF8), Helper.BUFFER_SIZE)) {
      Language fLng = DictHelper.getWikiLanguage(f.getName());
      ArrayList<String> namespaces = new ArrayList<String>(100);
      Step step = Step.Start;
      String line;
      ParseInfo info = new ParseInfo(out, fLng);
      while (null != (line = in.readLine())) {
        // if (line.contains("world")) {
        // System.out.println("-->");
        // }
        switch (step) {
          case Start:
            if (line.startsWith("    <namespaces>")) {
              step = Step.ParseNamespaces;
            }
            break;
          case ParseNamespaces:
            String ns = Helper.substringBetweenLast(line, ">", "</namespace>");
            if (ns != null) {
              namespaces.add(ns);
            } else if (line.startsWith("  <page>")) {
              step = Step.ParseTitle;
            }
            break;
          case ParseTitle:
            info.clear();
            info.setDefLng(fLng);
            String title = Helper.substringBetween(line, "    <title>", "</title>");
            if (title != null) {
              info.setDefVal(title);
              step = Step.CheckNS;
            }
            break;
          case CheckNS:
            String nsId = Helper.substringBetween(line, "    <ns>", "</ns>");
            if ("0".equals(nsId)) {
              step = Step.FindText;
            } else {
              step = Step.ParseTitle;
            }
            break;
          case ParseTextDef:
            if (line.contains("【")) {
              // ==日语==
              // 羽化【うか】
              //
              // 名?自 サ
              //
              // 昆虫 羽化。
              line = in.readLine();
              if (line.trim().length() == 0) {
                line = in.readLine();
              }
              if (line.contains("?")) {
                step = Step.ParseText;
              }
            } else if ((line.trim().length() == 0) || (info.getDefLng() == info.getfLng())) {
              step = Step.ParseText;
            } else if (line.startsWith("#") || !line.contains("#")) {
              if (!line.startsWith("{") && !line.contains(":") && !line.contains("：") && !line.contains("|") && !line.contains("--") && !line.startsWith("}")
                  && !line.startsWith(" ") && !line.startsWith("=") && !line.contains("'''") && !line.startsWith("`")) {
                final String fLngVal = Helper
                    .unescapeHtml(line)
                    .replaceAll(
                        "([\\*#\\{\\}\\|:\\[\\]])|(<[/a-zA-Z0-9=\"\' ]+>)|(﹝.+?﹞)|(\\(.+?\\))|(\\[.+?\\])|(\\（.+?\\）)|(［.+?］)|(/.+?/)|(adj\\. )|(vt\\. )|(inv\\. )|(n\\.m\\. )|(n\\. )|(\\-[a-zA-Z0-9]+\\-)",
                        "").replaceAll("([ ]*、[ ]*)|([ ]*;[ ]*)|([ ]*,[ ]*)|([ ]*，[ ]*)|([ ]*；[ ]*)|([ ]*。[ ]*)", ", ").replaceAll("\\.[\\.]+", "").trim();
                if (Helper.isNotEmptyOrNull(fLngVal)) {
                  info.setfVal(fLngVal);
                  info.addTitlefLng();
                }
              } else if (line.startsWith("#") && !Helper.containsAny(line, '=', '*', '[')) {
                final String fLngVal = Helper
                    .unescapeHtml(line)
                    .replaceAll(
                        "([\\*#\\{\\}\\|:\\[\\]])|(<[/a-zA-Z0-9=\"\' ]+>)|(﹝.+?﹞)|(\\(.+?\\))|(\\[.+?\\])|(\\（.+?\\）)|(［.+?］)|(/.+?/)|(adj\\. )|(vt\\. )|(inv\\. )|(n\\.m\\. )|(n\\. )|(\\-[a-zA-Z0-9]+\\-)",
                        "").replaceAll("([ ]*、[ ]*)|([ ]*;[ ]*)|([ ]*,[ ]*)|([ ]*，[ ]*)|([ ]*；[ ]*)|([ ]*。[ ]*)", ", ").replaceAll("\\.[\\.]+", "").trim();
                if (Helper.isNotEmptyOrNull(fLngVal)) {
                  info.setfVal(fLngVal);
                  info.addTitlefLng();
                }
              }
              // else
              // {
              // step = Step.ParseText;
              // }
            } else if (line.startsWith("n.") || line.startsWith("v.") || line.startsWith("prop.")) {
              // ==法语==
              // n.f. 女仆，女佣
              final String fLngVal = Helper
                  .unescapeHtml(Helper.substringAfter(line, " "))
                  .replaceAll(
                      "([\\*#\\{\\}\\|:\\[\\]])|(<[/a-zA-Z0-9=\"\' ]+>)|(﹝.+?﹞)|(\\(.+?\\))|(\\[.+?\\])|(\\（.+?\\）)|(［.+?］)|(/.+?/)|(adj\\. )|(vt\\. )|(inv\\. )|(n\\.m\\. )|(n\\. )|(\\-[a-zA-Z0-9]+\\-)",
                      "").replaceAll("([ ]*、[ ]*)|([ ]*;[ ]*)|([ ]*,[ ]*)|([ ]*，[ ]*)|([ ]*；[ ]*)|([ ]*。[ ]*)", ", ").replaceAll("\\.[\\.]+", "").trim();
              if (Helper.isNotEmptyOrNull(fLngVal)) {
                info.setfVal(fLngVal);
                info.addTitlefLng();
              }
            }
            if (line.contains("</text>")) {
              step = Step.ParseTitle;
            }
            break;
          case FindText:
            /**
             * Finds text tag
             */
            if (line.startsWith("      <text xml:space=\"preserve\">")) {
              line = line.substring("      <text xml:space=\"preserve\">".length());
              step = Step.ParseText;
              // out.write("\n\n\n===>>> " + info.title + "\n");
            } else if (line.startsWith("  <page>")) {
              step = Step.ParseTitle;
              break;
            } else {
              break;
            }
            //$FALL-THROUGH$
          case ParseText:
            /**
             * Parses text tag content and searches for definition language or translations block
             */
            String cline = ChineseHelper.toSimplifiedChinese(line);
            if (this.findDefinitionLanguage(line, cline, info)) {
              step = Step.ParseTextDef;
            } else if (this.findTranslationBlock(line, info)) {
              step = Step.ParseTranslation;
              break;
            }
            if (line.endsWith("</text>")) {
              step = Step.ParseTitle;
            }
            break;
          case ParseTranslation:
            /**
             * Parses translations block
             */
            Language tgtLng = this.findTranslationBlockTgtLng(line, info);
            if (tgtLng != null) {
              info.setTgtLng(tgtLng);

              line = line.replaceAll("\\|tr=[^\\}]+\\}\\}", "}}");

              WiktionaryPagesMetaCurrentExtractor.findTranslationBlockTgtVal1(line, info);

              WiktionaryPagesMetaCurrentExtractor.findTranslationBlockTgtVal2(line, info);
            }
            if (line.startsWith("----") || line.startsWith("=")) {
              // TODO check =
              step = Step.ParseText;
            }
            if (line.endsWith("</text>")) {
              step = Step.ParseTitle;
            }
            break;
          default:
            step = Step.ParseTitle;
            break;
        }
      }
      defCounter += info.getDefsCount();
      info.clear();
    }
    return defCounter;
  }

  private static void findTranslationBlockTgtVal2(String line, ParseInfo info) {
    List<String> tgtVals = Helper.substringBetweens(
        line.replace("]][[", Helper.SEP_SAME_MEANING).replace("]] [[", Helper.SEP_SAME_MEANING + Helper.SEP_SAME_MEANING)
            .replace("}}; {{", Helper.SEP_SAME_MEANING + "," + Helper.SEP_SAME_MEANING), "[[", "]]");
    int idx;
    for (String tgtVal : tgtVals) {
      String[] st = tgtVal.split(Helper.SEP_SAME_MEANING);
      StringBuilder sb = new StringBuilder();
      boolean found = false;
      for (int i = 0; i < st.length; i++) {
        String t = st[i];
        if (t.isEmpty()) {
          sb.append(" ");
        } else if (",".equals(t)) {
          sb.append(", ");
        } else {
          if (t.startsWith(":" + info.getTgtLng().getKey() + ":")) {
            t = t.substring(info.getTgtLng().getKey().length() + 2);
            idx = t.indexOf('/');
            if (idx != -1) {
              t = t.substring(0, idx);
            }
            if ((idx = t.indexOf('|')) != -1) {
              t = t.substring(0, idx);
            }
          } else if ((idx = (t.indexOf('#'))) != -1) {
            int idx2 = t.indexOf('|');
            if (idx2 != -1) {
              t = t.substring(idx2 + 1);
            } else {
              t = t.substring(0, idx);
            }
          }
          sb.append(Helper.unescapeHtml(t));
          found = true;
        }
      }
      if (found) {
        info.setTgtVal(sb.toString());
        info.addTitleTgt();
      }
    }
  }

  private static void findTranslationBlockTgtVal1(String line, ParseInfo info) {
    List<String> tgtVals = Helper.substringBetweens(
        line.replace("}}{{", Helper.SEP_SAME_MEANING).replace("}} {{", Helper.SEP_SAME_MEANING + Helper.SEP_SAME_MEANING)
            .replace("}}; {{", Helper.SEP_SAME_MEANING + "," + Helper.SEP_SAME_MEANING), "{{", "}}");
    for (String tgtVal : tgtVals) {
      if ((tgtVal.length() < 5) || Helper.containsAny(tgtVal, '=', '[', ':')) {
        continue;
      }
      String[] st = tgtVal.split(Helper.SEP_SAME_MEANING);
      StringBuilder sb = new StringBuilder();
      boolean found = false;
      for (int i = 0; i < st.length; i++) {
        String t = st[i];
        if (t.isEmpty()) {
          sb.append(" ");
        } else if (",".equals(t)) {
          sb.append(", ");
        } else {
          boolean hasPhonetic = (t.length() > 3) && (t.charAt(1) == 'x') && (t.charAt(2) == 'x');

          final int idx0 = t.indexOf('|');
          if (idx0 != -1) {
            final int idx1 = t.indexOf('|', idx0 + 1);
            if (idx1 != -1) {
              Language tgtLng = WiktionaryPagesMetaCurrentExtractor.findTargetLanguage(t.substring(idx0 + 1, idx1));
              if (tgtLng != null) {
                info.setTgtLng(tgtLng);
              }
              int idx2 = t.indexOf('|', idx1 + 1);
              if ((idx2 == -1) && (t.length() > (idx1 + 1))) {
                idx2 = t.length();
              }
              if (idx2 != -1) {
                if (hasPhonetic) {
                  int idx3 = t.indexOf('|', idx2 + 1);
                  if ((idx3 == -1) && (t.length() > (idx2 + 1))) {
                    idx3 = t.length();
                  }
                  if (idx3 != -1) {
                    String val = t.substring(idx2 + 1, idx3);
                    found = true;
                    sb.append(Helper.unescapeHtml(val));
                  }
                }
                if (!found) {
                  String val = t.substring(idx1 + 1, idx2);
                  final int idx3 = val.indexOf('#');
                  if (idx3 != -1) {
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
      }
      if (found) {
        info.setTgtVal(sb.toString());
        info.addTitleTgt();
      }
    }
  }

  private static Map<String, String> loadLanguageNames() throws IOException, FileNotFoundException {
    Properties lngNames = new Properties();
    for (Language lng : Language.values()) {
      File lngNamesFile = Helper.findResource("lng2name_" + lng.getKey().toUpperCase() + ".txt");
      if (lngNamesFile != null) {
        try (InputStream lngNamesIn = new FileInputStream(lngNamesFile)) {
          lngNames.load(new InputStreamReader(lngNamesIn, Helper.CHARSET_UTF8));
        }
      }
    }
    Map<String, String> result = new HashMap<>();
    for (Object k : lngNames.keySet()) {
      String key = (String) k;
      result.put(key.toLowerCase(), lngNames.getProperty(key).toLowerCase());
    }
    System.out.println("总：" + lngNames.size() + " 语言名代号 (过滤后：" + result.size() + ")");
    return result;
  }

  private Language findTranslationBlockTgtLng(String line, ParseInfo info) {
    Language tgtLng = null;
    String lng;
    if ((lng = Helper.substringBetween(line, "*{{", "}}")) != null) {
      tgtLng = WiktionaryPagesMetaCurrentExtractor.findTargetLanguage(lng);
    }
    if ((tgtLng == null) && line.startsWith("*:")
        && (((lng = Helper.substringBetween(line, "*:", "：")) != null) || ((lng = Helper.substringBetween(line, "*:", ":")) != null))) {
      tgtLng = WiktionaryPagesMetaCurrentExtractor.findTargetLanguage(lng);
    } else if ((tgtLng == null) && line.startsWith("*")
        && (((lng = Helper.substringBetween(line, "*", "：")) != null) || ((lng = Helper.substringBetween(line, "*", ":")) != null))) {
      tgtLng = WiktionaryPagesMetaCurrentExtractor.findTargetLanguage(lng);
    }
    if ((tgtLng == null) && ((lng = Helper.substringBetween(line, "{{T|", "}}")) != null)) {
      tgtLng = WiktionaryPagesMetaCurrentExtractor.findTargetLanguage(lng);
    }
    if ((tgtLng == null) && ((lng = Helper.substringBetween(line, "{{T|", "|")) != null)) {
      tgtLng = WiktionaryPagesMetaCurrentExtractor.findTargetLanguage(lng);
    }

    if (tgtLng == null) {
      lng = Helper.substringBetween(line, "*{{to|", "|");
      if (lng != null) {
        tgtLng = WiktionaryPagesMetaCurrentExtractor.findTargetLanguage(lng);
        if (tgtLng != null) {
          String tgtVal = Helper.substringBetweenLast(line, "|", "}}");
          if (Helper.isNotEmptyOrNull(tgtVal)) {
            info.setTgtVal(tgtVal);

            info.addTitleTgt();
          }
          tgtLng = null;
        }
      }
    }

    if ((tgtLng == null) && line.startsWith("|")) {
      lng = Helper.substringBetween(line, "|", "=");
      if ((lng != null) && (lng.length() < 9)) {
        tgtLng = WiktionaryPagesMetaCurrentExtractor.findTargetLanguage(lng);
      }
    }

    if ((tgtLng == null) && line.startsWith("# ")) {
      lng = Helper.substringBetween(line, "# ", " : ");
      if ((lng != null) && (lng.length() < 9)) {
        tgtLng = WiktionaryPagesMetaCurrentExtractor.findTargetLanguage(lng);
      }
    }
    return tgtLng;
  }

  private static Language findTargetLanguage(String lng) {
    Language tgtLng;
    tgtLng = Language.fromKey(lng);
    if (tgtLng == null) {
      String lng2 = WiktionaryPagesMetaCurrentExtractor.lngNamesLowerCased.get(lng.toLowerCase());
      if (lng2 != null) {
        tgtLng = Language.fromKey(lng2);
      }
    }
    return tgtLng;
  }

  private boolean notifyNoTranslationKey = true;

  private boolean findTranslationBlock(String line, ParseInfo info) {
    final String lineLower = line.toLowerCase();
    if (lineLower.startsWith("{{-trad-}}") || lineLower.startsWith("{{-trans-}}") || lineLower.startsWith("{{tt-top}}") || lineLower.startsWith("{{-dika-}}")) {
      return true;
    }
    List<String> trans = WiktionaryPagesMetaCurrentExtractor.transMap.get(info.getfLng().getKey());
    if (trans == null) {
      if (this.notifyNoTranslationKey) {
        System.err.println("没找到翻译代码！");
        this.notifyNoTranslationKey = false;
      }
    } else {
      for (String t : trans) {
        String translationText = t.toLowerCase();
        if (lineLower.startsWith("===" + translationText + "===") || lineLower.startsWith("====" + translationText + "====")
            || lineLower.startsWith("=====" + translationText + "=====") || lineLower.startsWith("=== " + translationText + " ===")
            || lineLower.startsWith("==== " + translationText + " ====") || lineLower.startsWith("===== " + translationText + " =====")
            || lineLower.startsWith("{{" + translationText + "}}")) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean findDefinitionLanguage(String line, String cline, ParseInfo info) throws IOException {
    String lng = null;
    Language defLng = null;
    if (cline.startsWith("{{汉语") || cline.startsWith("{{=n=|汉|")) {
      defLng = Language.ZH;
    } else if (line.startsWith("{{-")
        && ((null != (lng = Helper.substringBetween(line, "{{-", "-}}"))) || (null != (lng = Helper.substringBetween(line, "{{-", "-|"))))) {
      defLng = Language.fromKey(lng);
    } else if (line.startsWith("=[[") && (null != (lng = Helper.substringBetween(line, "=[[", "]]=")))) {
      defLng = Language.fromKey(lng);
    } else if (line.startsWith("===") && (null != (lng = Helper.substringBetweenNarrow(line, "|", "}}")))) {
      defLng = WiktionaryPagesMetaCurrentExtractor.findTargetLanguage(lng);
    } else if (line.startsWith("==") && (null != (lng = Helper.substringBetween(line, "==", "==")))) {
      defLng = Language.fromKey(lng);
      if (defLng == null) {
        defLng = WiktionaryPagesMetaCurrentExtractor.findTargetLanguage(lng);
      }
      if (defLng == null) {
        lng = Helper.substringBetween(lng, "|", "}}");
        if (lng != null) {
          defLng = Language.fromKey(lng);
        }
      }
    }
    if (defLng != null) {
      info.setDefLng(defLng);
      return true;
    } else {
      return false;
    }
  }
}
