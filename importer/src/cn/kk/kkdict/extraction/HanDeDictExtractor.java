package cn.kk.kkdict.extraction;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

import cn.kk.kkdict.beans.FormattedTreeSet;
import cn.kk.kkdict.utils.Helper;

public class HanDeDictExtractor {
    public static final String HAN_DE_DICT_UTF8_FILE = "X:\\kkdict\\dicts\\handedict\\handedict_nb.u8";

    public static final String OUT_DIR = "X:\\kkdict\\out\\handedict";

    public static final String[] IRRELEVANT_WORDS_STRINGS = { "(u.E.)" };

    public static void main(String args[]) throws IOException {
        long timeStarted = System.currentTimeMillis();
        Helper.precheck(HAN_DE_DICT_UTF8_FILE, OUT_DIR);
        BufferedReader reader = new BufferedReader(new FileReader(HAN_DE_DICT_UTF8_FILE), 8192000);
        BufferedWriter writer = new BufferedWriter(new FileWriter(OUT_DIR + File.separator + "output.txt"), 8192000);
        BufferedWriter pinyinWriter = new BufferedWriter(new FileWriter(OUT_DIR + File.separator + "output-handedict.kpy"), 8192000);
        BufferedWriter skippedIncompleteWriter = new BufferedWriter(new FileWriter(OUT_DIR + File.separator
                + "skipped_incomplete.txt"), 8192000);

        String line;
        String name = null;
        String pinyin;
        String translation;
        Set<String> globalCategories = new FormattedTreeSet<String>();
        Set<String> categories = null;
        Set<String> descriptions = null;
        String tmp;

        int statSkipped = 0;
        int statOk = 0;
        Category[] csValues = Category.values();
        String[] cs = new String[csValues.length];
        for (int i = 0; i < csValues.length; i++) {
            Category c = csValues[i];
            cs[i] = c.name();
        }
        Arrays.sort(cs);
        while ((line = reader.readLine()) != null) {
            if ((tmp = Helper.substringBetween(line, " ", " ")) != null) {
                name = tmp;
                pinyin = Helper.substringBetween(line, "[", "]");
                translation = Helper.substringBetween(line, "/", "/");
                if (Helper.isNotEmptyOrNull(name) && Helper.isNotEmptyOrNull(pinyin) && Helper.isNotEmptyOrNull(translation)
                        && !translation.contains("???")) {
                    categories = new FormattedTreeSet<String>();
                    descriptions = new FormattedTreeSet<String>();
                    for (String i : IRRELEVANT_WORDS_STRINGS) {
                        translation = translation.replace(i, Helper.EMPTY_STRING);
                    }

                    translation = extractCategories(translation, globalCategories, categories, cs, descriptions);
                    translation = extractCategories(translation, globalCategories, categories, cs, descriptions);
                    translation = extractCategories(translation, globalCategories, categories, cs, descriptions);
                    translation = extractCategories(translation, globalCategories, categories, cs, descriptions);
                    translation = extractCategories(translation, globalCategories, categories, cs, descriptions);

                    translation = translation.replaceAll("[(,]*[ ]*$", Helper.EMPTY_STRING);

                    for (String d : descriptions) {
                        translation += "(" + d + ")";
                    }

                    name = name.trim();
                    pinyin = pinyin.trim().replaceAll("[0-9] ", "'").replaceAll("[0-9]", Helper.EMPTY_STRING);
                    writer.write(name);
                    writer.write(Helper.SEP_PARTS);
                    writer.write(pinyin);
                    writer.write(Helper.SEP_PARTS);
                    writer.write(translation);
                    writer.write(Helper.SEP_PARTS);
                    writer.write(categories.toString());
                    writer.write(Helper.SEP_NEWLINE);

                    pinyinWriter.write(name);
                    pinyinWriter.write(Helper.SEP_PARTS);
                    pinyinWriter.write(pinyin);
                    pinyinWriter.write(Helper.SEP_NEWLINE);
                    
                    statOk++;
                } else {
                    skippedIncompleteWriter.write(line);
                    statSkipped++;
                }

            } else {
                System.out.println("Skipped line: " + tmp);
            }
        }
        reader.close();
        writer.close();
        pinyinWriter.close();
        skippedIncompleteWriter.close();

        BufferedWriter categoriesWriter = new BufferedWriter(new FileWriter(OUT_DIR + File.separator
                + "output-categories.txt"), 8192000);
        for (String c : globalCategories) {
            categoriesWriter.write(c);
            categoriesWriter.write(Helper.SEP_NEWLINE);
        }
        categoriesWriter.close();
        System.out.println("\n==============\nExtract HanDeDict Duration: "
                + Helper.formatDuration(System.currentTimeMillis() - timeStarted));
        System.out.println("Categories: " + globalCategories.size());
        System.out.println("OK: " + statOk);
        System.out.println("SKIPPED: " + statSkipped + "\n==============\n");
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

    public static enum Category {
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
