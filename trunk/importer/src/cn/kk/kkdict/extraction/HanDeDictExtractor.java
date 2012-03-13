package cn.kk.kkdict.extraction;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import cn.kk.kkdict.beans.FormattedTreeMap;
import cn.kk.kkdict.beans.FormattedTreeSet;
import cn.kk.kkdict.types.Category;
import cn.kk.kkdict.types.Language;
import cn.kk.kkdict.types.TranslationSource;
import cn.kk.kkdict.utils.ChineseHelper;
import cn.kk.kkdict.utils.Helper;

public class HanDeDictExtractor {
    public static final String HAN_DE_DICT_UTF8_FILE = "X:\\kkdict\\dicts\\handedict\\handedict_nb.u8";

    public static final String OUT_DIR = "O:\\handedict";

    public static final String[] IRRELEVANT_WORDS_STRINGS = { "(u.E.)" };

    public static void main(String args[]) throws IOException {
        long timeStarted = System.currentTimeMillis();
        Helper.precheck(HAN_DE_DICT_UTF8_FILE, OUT_DIR);
        System.out.print("读取HanDeDict文件'" + HAN_DE_DICT_UTF8_FILE + "' ... ");
        BufferedReader reader = new BufferedReader(new FileReader(HAN_DE_DICT_UTF8_FILE), Helper.BUFFER_SIZE);
        BufferedWriter writer = new BufferedWriter(new FileWriter(OUT_DIR + File.separator + "output-dict_zh_de."
                + TranslationSource.HANDE_DICT.key), Helper.BUFFER_SIZE);

        String line;
        String name = null;
        String translation;
        Set<String> globalCategories = new FormattedTreeSet<String>();
        Set<String> categories = null;
        Set<String> descriptions = null;
        String tmp;

        int statSkipped = 0;
        int statOk = 0;
        HanDeCategory[] csValues = HanDeCategory.values();
        String[] cs = new String[csValues.length];
        for (int i = 0; i < csValues.length; i++) {
            HanDeCategory c = csValues[i];
            cs[i] = c.name();
        }
        Arrays.sort(cs);
        Map<String, String> languages = new FormattedTreeMap<String, String>();
        while ((line = reader.readLine()) != null) {
            if (Helper.isNotEmptyOrNull(tmp = Helper.substringBetween(line, " ", " "))) {
                name = tmp;
                translation = Helper.substringBetween(line, "/", "/");
                if (Helper.isNotEmptyOrNull(name) && Helper.isNotEmptyOrNull(translation)
                        && !translation.contains("???")) {
                    categories = new FormattedTreeSet<String>();
                    descriptions = new FormattedTreeSet<String>();
                    for (String i : IRRELEVANT_WORDS_STRINGS) {
                        translation = translation.replace(i, Helper.EMPTY_STRING);
                    }

                    translation = Helper.unescapeHtml(translation);
                    translation = extractCategories(translation, globalCategories, categories, cs, descriptions);
                    translation = extractCategories(translation, globalCategories, categories, cs, descriptions);
                    translation = extractCategories(translation, globalCategories, categories, cs, descriptions);
                    translation = extractCategories(translation, globalCategories, categories, cs, descriptions);
                    translation = extractCategories(translation, globalCategories, categories, cs, descriptions);

                    translation = translation.replace("&gt", ">").replaceAll("\\([^\\)]*\\)", Helper.EMPTY_STRING)
                            .replaceAll("<[^>]*>", Helper.EMPTY_STRING).replaceAll("[(,]*[ ]*$", Helper.EMPTY_STRING);

                    for (String d : descriptions) {
                        translation += "(" + d + ")";
                    }

                    String trans = translation.replaceAll("([ ]*;[ ]*)|([ ]*,[ ]*)|([ ]*.[ ]*)",
                            Helper.SEP_SAME_MEANING);
                    String cats = categories.toString();
                    if (cats.isEmpty()) {
                        languages.put(Language.ZH.key, ChineseHelper.toSimplifiedChinese(name.trim()));
                        languages.put(Language.DE.key, trans);
                    } else {
                        // TODO
                        languages.put(Language.ZH.key, ChineseHelper.toSimplifiedChinese(name.trim()));
                        languages.put(Language.DE.key, trans + Helper.SEP_ATTRIBUTE + Category.TYPE_ID);
                    }
                    writer.write(languages.toString());
                    writer.write(Helper.SEP_PARTS);
                    writer.write(cats);
                    writer.write(Helper.SEP_NEWLINE);
                    statOk++;
                } else {
                    statSkipped++;
                }
            }
        }
        reader.close();
        writer.close();

        BufferedWriter categoriesWriter = new BufferedWriter(new FileWriter(OUT_DIR + File.separator
                + "output-categories.handedict"), Helper.BUFFER_SIZE);
        for (String c : globalCategories) {
            categoriesWriter.write(c);
            categoriesWriter.write(Helper.SEP_NEWLINE);
        }
        categoriesWriter.close();
        System.out.println("\n==============\n成功读取汉德词典文件。总共用去："
                + Helper.formatDuration(System.currentTimeMillis() - timeStarted));
        System.out.println("类别数目：" + globalCategories.size());
        System.out.println("有效词组数：" + statOk);
        System.out.println("跳过词组数：" + statSkipped + "\n==============\n");
    }

    private static String extractCategories(String translation, Set<String> globalCategories, Set<String> categories,
            String[] cs, Set<String> descriptions) {
        String cText = Helper.substringBetweenLast(translation, "(", ")");
        if (Helper.isNotEmptyOrNull(cText)) {
            String[] split = cText.split(",");
            if (split.length > 0) {
                boolean found = false;

                for (String s : split) {
                    s = s.trim();
                    s = s.replaceAll("[',.]*$", Helper.EMPTY_STRING);
                    if (Helper.isNotEmptyOrNull(s) && Arrays.binarySearch(cs, s) >= 0) {
                        found = true;
                        break;
                    }
                }
                if (found) {
                    translation = translation.substring(0, translation.lastIndexOf('(')).trim();
                    for (String s : split) {
                        s = s.trim();
                        s = s.replaceAll("[',.]*$", Helper.EMPTY_STRING);
                        if (Helper.isNotEmptyOrNull(s)) {
                            if (Arrays.binarySearch(cs, s) >= 0) {
                                categories.add(s);
                                globalCategories.add(s);
                            } else {
                                descriptions.add(s.trim());
                            }
                        }
                    }
                }
            }

        }
        return translation;
    }

    public static enum HanDeCategory {
        Adj,
        Adv,
        Arch,
        Bio,
        Buddh,
        Chem,
        Dulu,
        EDV,
        Eig,
        Ess,
        Fam,
        Geo,
        Geol,
        Gesch,
        Hayao,
        Hideaki,
        Hideo,
        Int,
        Kunst,
        Lit,
        Masami,
        Math,
        Med,
        Met,
        Mil,
        Mus,
        Org,
        Pers,
        Philos,
        Philosoph,
        Phys,
        Pol,
        Psych,
        Rechtsw,
        Rel,
        S,
        Sport,
        Sprachw,
        Sprichw,
        Tech,
        ugs,
        V,
        Vorn,
        Werk,
        Wirtsch,
        vulg,
    }
}
