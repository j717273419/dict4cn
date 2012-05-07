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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.kk.kkdict.Configuration;
import cn.kk.kkdict.Configuration.Source;
import cn.kk.kkdict.beans.FormattedTreeMap;
import cn.kk.kkdict.beans.FormattedTreeSet;
import cn.kk.kkdict.beans.Stat;
import cn.kk.kkdict.types.Category;
import cn.kk.kkdict.types.Language;
import cn.kk.kkdict.types.TranslationSource;
import cn.kk.kkdict.types.Usage;
import cn.kk.kkdict.types.WordType;
import cn.kk.kkdict.utils.ChineseHelper;
import cn.kk.kkdict.utils.Helper;

/**
 * Download: http://www.handedict.de/chinesisch_deutsch.php?mode=dl // edited
 * 
 * @author x_kez
 * 
 */
public class EdictZhDeExtractor {
    public static final String EDICT_FILE = Configuration.IMPORTER_FOLDER_SELECTED_DICTS.getFile(Source.DICT_EDICT,
            "handedict_nb.u8");

    public static final String OUT_DIR = Configuration.IMPORTER_FOLDER_EXTRACTED_DICTS.getPath(Source.DICT_EDICT);

    public static final String[] IRRELEVANT_WORDS_STRINGS = { "(u.E.)" };

    public static void main(String args[]) throws IOException {
        Map<String, EdictCategory> categoriesMap = new HashMap<String, EdictCategory>();
        final HanDeCategory[] csValues = HanDeCategory.values();
        for (int i = 0; i < csValues.length; i++) {
            HanDeCategory c = csValues[i];
            categoriesMap.put(c.getConstantName(), c);
        }
        new EdictZhDeExtractor().extractDict(TranslationSource.EDICT_ZH_DE, EDICT_FILE, Helper.CHARSET_UTF8, OUT_DIR,
                Language.ZH, Language.DE, categoriesMap, IRRELEVANT_WORDS_STRINGS);
    }

    protected void extractDict(TranslationSource translationSource, String file, Charset encoding, String outDir,
            Language srcLng, Language trgLng, Map<String, EdictCategory> catMap, String[] irrelevants)
            throws FileNotFoundException, IOException {
        long timeStarted = System.currentTimeMillis();
        Helper.precheck(file, outDir);
        System.out.println("读取词典文件'" + file + "' 。。。");
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding),
                Helper.BUFFER_SIZE);
        String dictFileName = outDir + File.separator + "output-dict_" + srcLng.key + "_" + trgLng.key + "."
                + translationSource.key;
        String srcFileName = outDir + File.separator + "output-dict_" + srcLng.key + "_" + trgLng.key + "_src."
                + translationSource.key;
        System.out.print("导出词典文件'" + dictFileName + "'。。。");
        BufferedWriter writer = new BufferedWriter(new FileWriter(dictFileName), Helper.BUFFER_SIZE);
        BufferedWriter srcWriter = new BufferedWriter(new FileWriter(srcFileName), Helper.BUFFER_SIZE);

        String line;
        String name = null;
        String translations;
        Map<String, Integer> globalCategories = new FormattedTreeMap<String, Integer>();
        Set<String> categories = null;
        Set<String> descriptions = null;
        String tmp;

        int statSkipped = 0;
        int statOk = 0;

        while ((line = reader.readLine()) != null) {
            if (srcLng == Language.ZH) {
                line = ChineseHelper.toSimplifiedChinese(line.trim());
            }
            if (line.startsWith("#")) {
                continue;
            } else if (Helper.isNotEmptyOrNull(tmp = readName(line))) {
                // System.out.println(tmp);
                name = tmp;
                translations = Helper.substringBetweenEnclose(line, "/", "/");
                if (Helper.isNotEmptyOrNull(name) && Helper.isNotEmptyOrNull(translations)) {
                    String[] transArray = translations.split("/");
                    for (String translation : transArray) {
                        if (!translation.contains("???")) {
                            categories = new FormattedTreeSet<String>();
                            descriptions = new FormattedTreeSet<String>(", ");
                            for (String i : irrelevants) {
                                translation = translation.replace(i, Helper.EMPTY_STRING);
                            }

                            translation = Helper.unescapeHtml(translation);
                            translation = translation.replace("&gt", ">");
                            while (!translation.equals((tmp = extractCategories(translation, globalCategories,
                                    categories, catMap, descriptions)))) {
                                translation = tmp;
                            }

                            translation = translation
                                    .replaceAll("([\\(\\)\\{\\}\\[\\]])|([，,.]+$)", Helper.EMPTY_STRING)
                                    .replaceAll("[ ]+", " ").trim();

                            if (Helper.isNotEmptyOrNull(translation)) {
                                String descriptionText = Helper.EMPTY_STRING;
                                if (!descriptions.isEmpty()) {
                                    if (Helper.isEmptyOrNull(translation)) {
                                        descriptionText = "(" + descriptions.toString() + ")";
                                    } else {
                                        descriptionText = " (" + descriptions.toString() + ")";
                                    }
                                }

                                if (translation.contains(", ") || translation.contains("; ")) {
                                    translation = translation.replace(", ", descriptionText + Helper.SEP_SAME_MEANING);
                                    translation = translation.replace("; ", descriptionText + Helper.SEP_SAME_MEANING);
                                } else {
                                    translation += descriptionText;
                                }
                                String tmp1 = name.trim();
                                String tmp2 = translation;
                                if (!categories.isEmpty()) {
                                    for (String cat : categories) {
                                        EdictCategory c = catMap.get(Helper.toConstantName(cat));
                                        if (c != null && c.getAttributes() != null) {
                                            tmp1 += Helper.SEP_ATTRIBUTE + c.getAttributes();
                                            tmp2 += Helper.SEP_ATTRIBUTE + c.getAttributes();
                                        }
                                    }
                                }
                                writer.write(srcLng.key + Helper.SEP_DEFINITION + tmp1);
                                writer.write(Helper.SEP_LIST + trgLng.key + Helper.SEP_DEFINITION + tmp2);
                                writer.write(Helper.SEP_NEWLINE_CHAR);
                                srcWriter.write(srcLng + Helper.SEP_DEFINITION + name.trim() + Helper.SEP_ATTRIBUTE
                                        + TranslationSource.TYPE_ID + translationSource.key + Helper.SEP_NEWLINE);
                                statOk++;
                            }
                            continue;
                        }
                    }
                }
                statSkipped++;
            }
        }
        reader.close();
        writer.close();
        srcWriter.close();
        System.out.println("成功");
        System.out.println("导出来源文件'" + srcFileName + "'。。。成功");

        String categoriesFileName = outDir + File.separator + "output-categories." + translationSource.key;
        System.out.print("导出分类文件'" + categoriesFileName + "'。。。");
        BufferedWriter categoriesWriter = new BufferedWriter(new FileWriter(categoriesFileName), Helper.BUFFER_SIZE);
        List<Stat> list = new ArrayList<Stat>();
        Set<String> keys = globalCategories.keySet();
        for (String k : keys) {
            list.add(new Stat(globalCategories.get(k), k));
        }
        Collections.sort(list);

        long totalOccurrences = 0L;
        for (Stat s : list) {
            if (s.counter.intValue() > 2) {
                categoriesWriter.write(s.key);
                categoriesWriter.write(Helper.SEP_PARTS);
                categoriesWriter.write(s.counter.toString());
                categoriesWriter.write(Helper.SEP_NEWLINE);
                totalOccurrences += s.counter.longValue();
            }
        }
        categoriesWriter.close();
        System.out.println("成功");
        System.out.println("==============\n成功读取词典文件。总共用去："
                + Helper.formatDuration(System.currentTimeMillis() - timeStarted));
        System.out.println("类别数目：" + globalCategories.size());
        System.out.println("类别出现次数：" + totalOccurrences);
        System.out.println("有效词组数：" + statOk);
        System.out.println("跳过词组数：" + statSkipped + "\n==============\n");
    }

    protected String readName(String line) {
        return Helper.substringBetween(line, " ", " ");
    }

    private static String extractCategories(String translation, Map<String, Integer> globalCategories,
            Set<String> categories, Map<String, EdictCategory> catMap, Set<String> descriptions) {
        String[][] pairs = { { "(", ")" }, { "<", ">" }, { "[", "]" } };
        int pairIdx = 0;
        String cText = null;
        String tmp;
        do {
            cText = Helper.substringBetweenLast(translation, pairs[pairIdx][0], pairs[pairIdx][1]);
            if (Helper.isNotEmptyOrNull(cText)) {
                tmp = translation.replace(pairs[pairIdx][0] + cText + pairs[pairIdx][1], Helper.EMPTY_STRING).trim();
                cText = cText.trim();
                if (Helper.isNotEmptyOrNull(cText)) {
                    translation = tmp;
                    String[] split = cText.split(",");

                    for (String s : split) {
                        s = s.replaceAll("['.]*$", Helper.EMPTY_STRING).trim();
                        tmp = Helper.toConstantName(s);
                        if (Helper.isNotEmptyOrNull(tmp)) {
                            EdictCategory c = catMap.get(tmp);
                            if (c != null) {
                                categories.add(s);
                                Helper.add(globalCategories, s);
                                if (c.getReplacement() != null) {
                                    descriptions.add(c.getReplacement());
                                }
                            } else {
                                Helper.add(globalCategories, pairIdx + "< " + s);
                                descriptions.add(s);
                            }
                        }
                    }
                }
            }
            pairIdx++;
        } while (pairIdx < pairs.length && cText == null);
        return translation;
    }

    public static enum HanDeCategory implements EdictCategory {
        Adj(null, WordType.TYPE_ID + WordType.ADJECTIVE.key),
        Adv(null, WordType.TYPE_ID + WordType.ADVERB.key),
        Agrar(null, Category.TYPE_ID + Category.AGRICULTURE.key),
        Arch(null, Category.TYPE_ID + Category.BUILDING.key),
        Astron(null, Category.TYPE_ID + Category.ASTRONOMY.key),
        Auto(null, Category.TYPE_ID + Category.AUTOMOBILE.key),
        Bio(null, Category.TYPE_ID + Category.BIOLOGY.key),
        Buddh(null, Category.TYPE_ID + Category.RELIGION.key),
        Chem(null, Category.TYPE_ID + Category.CHEMISTRY.key),
        Druckw(null, Category.TYPE_ID + Category.INDUSTRY.key),
        Dulu(null, null),
        EDV(null, Category.TYPE_ID + Category.SOFTWARE.key),
        Eig(null, null),
        Ess(null, Category.TYPE_ID + Category.CUISINE.key),
        Fam(null, Category.TYPE_ID + Category.PEOPLE.key),
        Geo(null, Category.TYPE_ID + Category.GEOGRAPHY.key),
        Geol(null, Category.TYPE_ID + Category.GEOGRAPHY.key),
        Gesch(null, Category.TYPE_ID + Category.HISTORY.key),
        Hayao(null, null),
        Hideaki(null, null),
        Hideo(null, null),
        Int(null, WordType.TYPE_ID + WordType.INTERJECTION.key),
        Konj(null, WordType.TYPE_ID + WordType.CONJUNCTION.key),
        Kunst(null, Category.TYPE_ID + Category.ARTS.key),
        Lit(null, Category.TYPE_ID + Category.LITERATURE.key),
        Masami(null, null),
        Math(null, Category.TYPE_ID + Category.MATHEMATICS.key),
        Med(null, Category.TYPE_ID + Category.MEDICINE.key),
        Met(null, Category.TYPE_ID + Category.GEOGRAPHY.key),
        Mil(null, Category.TYPE_ID + Category.MILITARY.key),
        Mus(null, Category.TYPE_ID + Category.MUSIC.key),
        Num(null, Category.TYPE_ID + Category.MATHEMATICS.key),
        Org(null, Category.TYPE_ID + Category.NAME.key),
        Pers(null, Category.TYPE_ID + Category.PEOPLE.key),
        Philos(null, Category.TYPE_ID + Category.PHILOSOPHY.key),
        Philosoph(null, Category.TYPE_ID + Category.PHILOSOPHY.key),
        Phys(null, Category.TYPE_ID + Category.PHYSICS.key),
        Pol(null, Category.TYPE_ID + Category.POLITICS.key),
        Präp(null, WordType.TYPE_ID + WordType.PREPOSITION.key),
        Pron(null, WordType.TYPE_ID + WordType.PRONOUN.key),
        Psych(null, Category.TYPE_ID + Category.PSYCHOLOGY.key),
        Rechtsw(null, Category.TYPE_ID + Category.LAW.key),
        Rel(null, Category.TYPE_ID + Category.RELIGION.key),
        S(null, WordType.TYPE_ID + WordType.NOUN.key),
        Sport(null, Category.TYPE_ID + Category.SPORTS.key),
        Sprachw(null, Category.TYPE_ID + Category.LITERATURE.key),
        Sprichw(null, Usage.TYPE_ID + Usage.FIGURATIVE.key),
        Tech(null, Category.TYPE_ID + Category.TECHNOLOGY.key),
        Tite(null, null),
        ugs(null, Usage.TYPE_ID + Usage.FAMILIAR.key),
        V(null, WordType.TYPE_ID + WordType.VERB.key),
        Vorn(null, Category.TYPE_ID + Category.PEOPLE.key),
        Werk(null, Category.TYPE_ID + Category.LITERATURE.key),
        Wirtsch(null, Category.TYPE_ID + Category.ECONOMY.key),
        vulg(null, Usage.TYPE_ID + Usage.VULGAR.key),
        Zähl(null, WordType.TYPE_ID + WordType.NUMERAL.key),
        wörtl(null, Usage.TYPE_ID + Usage.FAMILIAR.key),
        veraltet(null, Usage.TYPE_ID + Usage.DATED.key),
        umgangssprachlich(null, Usage.TYPE_ID + Usage.FAMILIAR.key),
        umg(null, Usage.TYPE_ID + Usage.FAMILIAR.key),
        r(null, null),
        pl(null, WordType.TYPE_ID + WordType.PLURAL.key),
        n(null, null),
        jur(null, Category.TYPE_ID + Category.LAW.key),
        jap(null, null),
        fig(null, Usage.TYPE_ID + Usage.FIGURATIVE.key),
        en(null, null),
        engl(null, null),
        englisch(null, null),
        e(null, null),
        poet(null, Usage.TYPE_ID + Usage.POETIC.key),
        lat(null, null),
        methaphorisch(null, Usage.TYPE_ID + Usage.METAPHORICAL.key),
        Redew(null, Usage.TYPE_ID + Usage.FIGURATIVE.key),
        Biochem(null, Category.TYPE_ID + Category.BIOLOGY.key),
        Stadt(null, Category.TYPE_ID + Category.NAME.key),
        Berg(null, Category.TYPE_ID + Category.NAME.key),
        Fluss(null, Category.TYPE_ID + Category.NAME.key),
        Kleidung(null, Category.TYPE_ID + Category.COSMETICS.key),
        Fisch(null, Category.TYPE_ID + Category.ANIMALS.key),
        Autor_Shanghai_Daily(null, null),
        Architekt(null, Category.TYPE_ID + Category.BUILDING.key),
        Bopomofo_Aussprache_Zeichen(null, null),
        Grammatik(null, Category.TYPE_ID + Category.LITERATURE.key),
        Hund(null, Category.TYPE_ID + Category.PETS.key),
        Satzzeichen(null, Category.TYPE_ID + Category.LITERATURE.key),
        Taxonomie(null, null),
        astrol(null, Category.TYPE_ID + Category.MYTHOLOGY.key),
        geh(null, null),
        Aktienmarkt(null, Category.TYPE_ID + Category.ECONOMY.key),
        Automarke(null, Category.TYPE_ID + Category.NAME.key),
        Autonomes_Gebiet_Innere_Mongolei(null, Category.TYPE_ID + Category.NAME.key),
        Autonomes_Gebiet_Tibet(null, Category.TYPE_ID + Category.NAME.key),
        Autonomes_Gebiet_Xinjiang(null, Category.TYPE_ID + Category.NAME.key),
        Autonomes_Gebiet_in_China(null, Category.TYPE_ID + Category.NAME.key),
        Avalokiteśvara(null, null),
        Beijing(null, Category.TYPE_ID + Category.NAME.key),
        Bergbau(null, Category.TYPE_ID + Category.INDUSTRY.key),
        Bezirk_in_Anhui(null, Category.TYPE_ID + Category.NAME.key),
        Bezirk_in_Fujian(null, Category.TYPE_ID + Category.NAME.key),
        Bezirk_in_Gansu(null, Category.TYPE_ID + Category.NAME.key),
        Bezirk_in_Guangdong(null, Category.TYPE_ID + Category.NAME.key),
        Bezirk_in_Guangxi(null, Category.TYPE_ID + Category.NAME.key),
        Bezirk_in_Guizhou(null, Category.TYPE_ID + Category.NAME.key),
        Bezirk_in_Hebei(null, Category.TYPE_ID + Category.NAME.key),
        Bezirk_in_Heilongjiang(null, Category.TYPE_ID + Category.NAME.key),
        Bezirk_in_Henan(null, Category.TYPE_ID + Category.NAME.key),
        Bezirk_in_Hongkong(null, Category.TYPE_ID + Category.NAME.key),
        Bezirk_in_Hubei(null, Category.TYPE_ID + Category.NAME.key),
        Bezirk_in_Hunan(null, Category.TYPE_ID + Category.NAME.key),
        Bezirk_in_Jiangsu(null, Category.TYPE_ID + Category.NAME.key),
        Bezirk_in_Jiangxi(null, Category.TYPE_ID + Category.NAME.key),
        Bezirk_in_Jilin(null, Category.TYPE_ID + Category.NAME.key),
        Bezirk_in_Shaanxi(null, Category.TYPE_ID + Category.NAME.key),
        Bezirk_in_Shandong(null, Category.TYPE_ID + Category.NAME.key),
        Bezirk_in_Sichuan(null, Category.TYPE_ID + Category.NAME.key),
        Bezirk_in_Tibet(null, Category.TYPE_ID + Category.NAME.key),
        Bezirk_in_Xinjiang(null, Category.TYPE_ID + Category.NAME.key),
        Bezirk_in_Yunnan(null, Category.TYPE_ID + Category.NAME.key),
        Bezirk_in_Zhejiang(null, Category.TYPE_ID + Category.NAME.key),
        Bezirk_von_Hongkong(null, Category.TYPE_ID + Category.NAME.key),
        Bibel(null, Category.TYPE_ID + Category.RELIGION.key),
        Bildschirm(null, Category.TYPE_ID + Category.SOFTWARE.key),
        Bildverarbeitung(null, Category.TYPE_ID + Category.SOFTWARE.key),
        Bleisatz(null, null),
        Blütenpfeffer(null, null),
        Bodhisattva_des_Mitgefühls(null, null),
        Botanik(null, Category.TYPE_ID + Category.BIOLOGY.key),
        Bruchrechnung(null, Category.TYPE_ID + Category.MATHEMATICS.key),
        Buch(null, Category.TYPE_ID + Category.LITERATURE.key),
        Buchbinderei(null, Category.TYPE_ID + Category.INDUSTRY.key),
        Buddhismus(null, Category.TYPE_ID + Category.RELIGION.key),
        Bundesstaat_der_USA(null, Category.TYPE_ID + Category.NAME.key),
        Bundesstaat_von_Malaysia(null, Category.TYPE_ID + Category.NAME.key),
        Bühnentechnik(null, Category.TYPE_ID + Category.TECHNOLOGY.key),
        Canton(null, Category.TYPE_ID + Category.NAME.key),
        China(null, Category.TYPE_ID + Category.NAME.key),
        Christentum(null, Category.TYPE_ID + Category.RELIGION.key),
        Computer(null, Category.TYPE_ID + Category.SOFTWARE.key),
        Computerspiel(null, Category.TYPE_ID + Category.GAME.key),
        Departement_in_Frankreich(null, Category.TYPE_ID + Category.NAME.key),
        Deutschland(null, Category.TYPE_ID + Category.NAME.key),
        Dialekt(null, Usage.TYPE_ID + Usage.DIALECT.key),
        Diamant(null, null),
        Dimsum(null, null),
        Dorf_in_Taiwan(null, Category.TYPE_ID + Category.NAME.key),
        Druck(null, Category.TYPE_ID + Category.INDUSTRY.key),
        Drucktechnik(null, Category.TYPE_ID + Category.TECHNOLOGY.key),
        Druckwesen(null, Category.TYPE_ID + Category.INDUSTRY.key),
        Eisenbahn(null, Category.TYPE_ID + Category.RAILWAYS.key),
        Energie(null, Category.TYPE_ID + Category.TECHNOLOGY.key),
        England(null, Category.TYPE_ID + Category.NAME.key),
        Familienname(null, Category.TYPE_ID + Category.PEOPLE.key),
        Farbe(null, Category.TYPE_ID + Category.ARTS.key),
        Fenchel(null, Category.TYPE_ID + Category.PLANTS.key),
        Fernsehserie(null, Category.TYPE_ID + Category.MEDIA.key),
        Feuer(null, null),
        Film(null, Category.TYPE_ID + Category.MEDIA.key),
        Filmtitel(null, Category.TYPE_ID + Category.MEDIA.key),
        Finalpartikel(null, null),
        Firma(null, Category.TYPE_ID + Category.NAME.key),
        Firmenname(null, Category.TYPE_ID + Category.NAME.key),
        Flugzeug(null, Category.TYPE_ID + Category.AERIAL.key),
        Foto(null, Category.TYPE_ID + Category.ARTS.key),
        Fotografie(null, Category.TYPE_ID + Category.ARTS.key),
        Frankreich(null, Category.TYPE_ID + Category.NAME.key),
        Fu_ballklub(null, Category.TYPE_ID + Category.SPORTS.key),
        Gegend_in_Hebei(null, Category.TYPE_ID + Category.NAME.key),
        Gegend_in_Shandong(null, Category.TYPE_ID + Category.NAME.key),
        Gegend_in_Sichuan(null, Category.TYPE_ID + Category.NAME.key),
        Gegend_in_Taiwan(null, Category.TYPE_ID + Category.NAME.key),
        Geld(null, Category.TYPE_ID + Category.ECONOMY.key),
        Georgia(null, Category.TYPE_ID + Category.NAME.key),
        Gewürz(null, Category.TYPE_ID + Category.CUISINE.key),
        Gewürznelken(null, Category.TYPE_ID + Category.CUISINE.key),
        Haar(null, null),
        Halsdrüsengeschwulst(null, null),
        Hong_Kong(null, Category.TYPE_ID + Category.NAME.key),
        Hongkong(null, Category.TYPE_ID + Category.NAME.key),
        I(null, null),
        II(null, null),
        III(null, null),
        Indiana(null, Category.TYPE_ID + Category.NAME.key),
        Indien(null, Category.TYPE_ID + Category.NAME.key),
        Italien(null, Category.TYPE_ID + Category.NAME.key),
        Jacob_und_Wilhelm_Grimm(null, null),
        Japan(null, Category.TYPE_ID + Category.NAME.key),
        Kalifornien(null, Category.TYPE_ID + Category.NAME.key),
        Kalligraphie_Zeichen_Bestandteil(null, null),
        Kalligraphie_Zeichenbestandteil(null, null),
        Kamera(null, Category.TYPE_ID + Category.ARTS.key),
        Kanada(null, Category.TYPE_ID + Category.NAME.key),
        Krankheit(null, Category.TYPE_ID + Category.MEDICINE.key),
        Kredit(null, Category.TYPE_ID + Category.ECONOMY.key),
        Kreis_Changxing(null, Category.TYPE_ID + Category.NAME.key),
        Kreis_in_Shandong(null, Category.TYPE_ID + Category.NAME.key),
        Kreis_in_Sichuan(null, Category.TYPE_ID + Category.NAME.key),
        Kreuzfahrtschiff(null, Category.TYPE_ID + Category.MARITIME.key),
        L(null, null),
        Manga_Serie(null, Category.TYPE_ID + Category.HOBBY.key),
        Medizin(null, Category.TYPE_ID + Category.MEDICINE.key),
        Mensch(null, Category.TYPE_ID + Category.PEOPLE.key),
        Messer(null, null),
        Minnesota(null, null),
        Motor(null, Category.TYPE_ID + Category.AUTOMOBILE.key),
        Musik(null, Category.TYPE_ID + Category.MUSIC.key),
        Mythologie(null, Category.TYPE_ID + Category.MYTHOLOGY.key),
        Mähdrescher(null, Category.TYPE_ID + Category.AGRICULTURE.key),
        Name(null, Category.TYPE_ID + Category.NAME.key),
        OECD(null, null),
        Offsetdruck(null, Category.TYPE_ID + Category.INDUSTRY.key),
        Ort(null, Category.TYPE_ID + Category.NAME.key),
        Ort_in_Anhui(null, Category.TYPE_ID + Category.NAME.key),
        Ort_in_Beijing(null, Category.TYPE_ID + Category.NAME.key),
        Ort_in_Fujian(null, Category.TYPE_ID + Category.NAME.key),
        Ort_in_Gansu(null, Category.TYPE_ID + Category.NAME.key),
        Ort_in_Guangdong(null, Category.TYPE_ID + Category.NAME.key),
        Ort_in_Guangxi(null, Category.TYPE_ID + Category.NAME.key),
        Ort_in_Guizhou(null, Category.TYPE_ID + Category.NAME.key),
        Ort_in_Hainan(null, Category.TYPE_ID + Category.NAME.key),
        Ort_in_Hebei(null, Category.TYPE_ID + Category.NAME.key),
        Ort_in_Heilongjiang(null, Category.TYPE_ID + Category.NAME.key),
        Ort_in_Henan(null, Category.TYPE_ID + Category.NAME.key),
        Ort_in_Hubei(null, Category.TYPE_ID + Category.NAME.key),
        Ort_in_Hunan(null, Category.TYPE_ID + Category.NAME.key),
        Ort_in_Inner_Mongolia(null, Category.TYPE_ID + Category.NAME.key),
        Ort_in_Jiangsu(null, Category.TYPE_ID + Category.NAME.key),
        Ort_in_Jiangxi(null, Category.TYPE_ID + Category.NAME.key),
        Ort_in_Jilin(null, Category.TYPE_ID + Category.NAME.key),
        Ort_in_Kham_prov_of_Tibet(null, Category.TYPE_ID + Category.NAME.key),
        Ort_in_Liaoning(null, Category.TYPE_ID + Category.NAME.key),
        Ort_in_Ningxia(null, Category.TYPE_ID + Category.NAME.key),
        Ort_in_Qinghai(null, Category.TYPE_ID + Category.NAME.key),
        Ort_in_Shaanxi(null, Category.TYPE_ID + Category.NAME.key),
        Ort_in_Shandong(null, Category.TYPE_ID + Category.NAME.key),
        Ort_in_Shanghai(null, Category.TYPE_ID + Category.NAME.key),
        Ort_in_Shanxi(null, Category.TYPE_ID + Category.NAME.key),
        Ort_in_Sichuan(null, Category.TYPE_ID + Category.NAME.key),
        Ort_in_Taiwan(null, Category.TYPE_ID + Category.NAME.key),
        Ort_in_Tianjin(null, Category.TYPE_ID + Category.NAME.key),
        Ort_in_Tibet(null, Category.TYPE_ID + Category.NAME.key),
        Ort_in_Xinjiang(null, Category.TYPE_ID + Category.NAME.key),
        Ort_in_Yunnan(null, Category.TYPE_ID + Category.NAME.key),
        Ort_in_Zhejiang(null, Category.TYPE_ID + Category.NAME.key),
        Ort_in_central_Tibet(null, Category.TYPE_ID + Category.NAME.key),
        Ortsname(null, Category.TYPE_ID + Category.NAME.key),
        Papier(null, null),
        Patent(null, null),
        Person(null, Category.TYPE_ID + Category.PEOPLE.key),
        Pferd(null, Category.TYPE_ID + Category.PETS.key),
        Platte(null, null),
        Provinz_Anhui(null, Category.TYPE_ID + Category.NAME.key),
        Provinz_Fujian(null, Category.TYPE_ID + Category.NAME.key),
        Provinz_Gansu(null, Category.TYPE_ID + Category.NAME.key),
        Provinz_Guangdong(null, Category.TYPE_ID + Category.NAME.key),
        Provinz_Guangxi(null, Category.TYPE_ID + Category.NAME.key),
        Provinz_Guizhou(null, Category.TYPE_ID + Category.NAME.key),
        Provinz_Hainan(null, Category.TYPE_ID + Category.NAME.key),
        Provinz_Hebei(null, Category.TYPE_ID + Category.NAME.key),
        Provinz_Heilongjiang(null, Category.TYPE_ID + Category.NAME.key),
        Provinz_Henan(null, Category.TYPE_ID + Category.NAME.key),
        Provinz_Hubei(null, Category.TYPE_ID + Category.NAME.key),
        Provinz_Hunan(null, Category.TYPE_ID + Category.NAME.key),
        Provinz_Jiangsu(null, Category.TYPE_ID + Category.NAME.key),
        Provinz_Jiangxi(null, Category.TYPE_ID + Category.NAME.key),
        Provinz_Jilin(null, Category.TYPE_ID + Category.NAME.key),
        Provinz_Liaoning(null, Category.TYPE_ID + Category.NAME.key),
        Provinz_Qinghai(null, Category.TYPE_ID + Category.NAME.key),
        Provinz_Shaanxi(null, Category.TYPE_ID + Category.NAME.key),
        Provinz_Shandong(null, Category.TYPE_ID + Category.NAME.key),
        Provinz_Shanxi(null, Category.TYPE_ID + Category.NAME.key),
        Provinz_Sichuan(null, Category.TYPE_ID + Category.NAME.key),
        Provinz_Xinjiang(null, Category.TYPE_ID + Category.NAME.key),
        Provinz_Yunnan(null, Category.TYPE_ID + Category.NAME.key),
        Provinz_Zhejiang(null, Category.TYPE_ID + Category.NAME.key),
        Region_in_Deutschland(null, Category.TYPE_ID + Category.NAME.key),
        Region_in_Italien(null, Category.TYPE_ID + Category.NAME.key),
        Repro(null, null),
        Rollendruckmaschine(null, Category.TYPE_ID + Category.INDUSTRY.key),
        Rollenoffset(null, Category.TYPE_ID + Category.INDUSTRY.key),
        Rollenoffsetdruck(null, Category.TYPE_ID + Category.INDUSTRY.key),
        Russland(null, Category.TYPE_ID + Category.NAME.key),
        Sanskrit(null, Category.TYPE_ID + Category.RELIGION.key),
        Sanskrit_m(null, Category.TYPE_ID + Category.RELIGION.key),
        Satz(null, null),
        Schauspielerin(null, Category.TYPE_ID + Category.PEOPLE.key),
        Schiff(null, Category.TYPE_ID + Category.MARITIME.key),
        Schulden(null, Category.TYPE_ID + Category.ECONOMY.key),
        Schule(null, Category.TYPE_ID + Category.EDUCATION.key),
        Schweiz(null, Category.TYPE_ID + Category.NAME.key),
        Sci_Fi_Serie(null, Category.TYPE_ID + Category.MEDIA.key),
        Shanghai(null, Category.TYPE_ID + Category.NAME.key),
        Sichuan_Spezialität(null, Category.TYPE_ID + Category.CUISINE.key),
        Siebdruck(null, Category.TYPE_ID + Category.INDUSTRY.key),
        Sophora_japonica(null, null),
        Spiel(null, Category.TYPE_ID + Category.GAME.key),
        Spielkarte(null, Category.TYPE_ID + Category.GAME.key),
        Sprichwort(null, Usage.TYPE_ID + Usage.POETIC.key),
        Staat_in_Afrika(null, Category.TYPE_ID + Category.NAME.key),
        Stadt_der_Provinz_Fujian(null, Category.TYPE_ID + Category.NAME.key),
        Stadt_im_Autonomen_Gebiet_Innere_Mongolei(null, Category.TYPE_ID + Category.NAME.key),
        Stadt_im_Landkreis_Taipeh(null, Category.TYPE_ID + Category.NAME.key),
        Stadt_im_Landkreis_Taipei(null, Category.TYPE_ID + Category.NAME.key),
        Stadt_im_Norden_Taiwans(null, Category.TYPE_ID + Category.NAME.key),
        Stadt_in_Anhui(null, Category.TYPE_ID + Category.NAME.key),
        Stadt_in_Deutschland(null, Category.TYPE_ID + Category.NAME.key),
        Stadt_in_England(null, Category.TYPE_ID + Category.NAME.key),
        Stadt_in_Frankreich(null, Category.TYPE_ID + Category.NAME.key),
        Stadt_in_Fujian(null, Category.TYPE_ID + Category.NAME.key),
        Stadt_in_Gansu(null, Category.TYPE_ID + Category.NAME.key),
        Stadt_in_Guangdong(null, Category.TYPE_ID + Category.NAME.key),
        Stadt_in_Guangxi(null, Category.TYPE_ID + Category.NAME.key),
        Stadt_in_Guizhou(null, Category.TYPE_ID + Category.NAME.key),
        Stadt_in_Hainan(null, Category.TYPE_ID + Category.NAME.key),
        Stadt_in_Hebei(null, Category.TYPE_ID + Category.NAME.key),
        Stadt_in_Heilongjiang(null, Category.TYPE_ID + Category.NAME.key),
        Stadt_in_Henan(null, Category.TYPE_ID + Category.NAME.key),
        Stadt_in_Hubei(null, Category.TYPE_ID + Category.NAME.key),
        Stadt_in_Hunan(null, Category.TYPE_ID + Category.NAME.key),
        Stadt_in_Indien(null, Category.TYPE_ID + Category.NAME.key),
        Stadt_in_Inner_Mongolia(null, Category.TYPE_ID + Category.NAME.key),
        Stadt_in_Italien(null, Category.TYPE_ID + Category.NAME.key),
        Stadt_in_Japan(null, Category.TYPE_ID + Category.NAME.key),
        Stadt_in_Jiangsu(null, Category.TYPE_ID + Category.NAME.key),
        Stadt_in_Jiangxi(null, Category.TYPE_ID + Category.NAME.key),
        Stadt_in_Jilin(null, Category.TYPE_ID + Category.NAME.key),
        Stadt_in_Liaoning(null, Category.TYPE_ID + Category.NAME.key),
        Stadt_in_Niedersachsen(null, Category.TYPE_ID + Category.NAME.key),
        Stadt_in_Norwegen(null, Category.TYPE_ID + Category.NAME.key),
        Stadt_in_Pakistan(null, Category.TYPE_ID + Category.NAME.key),
        Stadt_in_Polen(null, Category.TYPE_ID + Category.NAME.key),
        Stadt_in_Russland(null, Category.TYPE_ID + Category.NAME.key),
        Stadt_in_Sachsen_Anhalt(null, Category.TYPE_ID + Category.NAME.key),
        Stadt_in_Shaanxi(null, Category.TYPE_ID + Category.NAME.key),
        Stadt_in_Shandong(null, Category.TYPE_ID + Category.NAME.key),
        Stadt_in_Shanxi(null, Category.TYPE_ID + Category.NAME.key),
        Stadt_in_Sichuan(null, Category.TYPE_ID + Category.NAME.key),
        Stadt_in_Taiwan(null, Category.TYPE_ID + Category.NAME.key),
        Stadt_in_Tschechien(null, Category.TYPE_ID + Category.NAME.key),
        Stadt_in_Xinjiang(null, Category.TYPE_ID + Category.NAME.key),
        Stadt_in_Yunnan(null, Category.TYPE_ID + Category.NAME.key),
        Stadt_in_Zhejiang(null, Category.TYPE_ID + Category.NAME.key),
        Stadt_in_den_Niederlanden(null, Category.TYPE_ID + Category.NAME.key),
        Stadt_in_der_Provinz_Anhui(null, Category.TYPE_ID + Category.NAME.key),
        Stadt_in_der_Provinz_Fujian(null, Category.TYPE_ID + Category.NAME.key),
        Stadt_in_der_Provinz_Gansu(null, Category.TYPE_ID + Category.NAME.key),
        Stadt_in_der_Provinz_Guangdong(null, Category.TYPE_ID + Category.NAME.key),
        Stadt_in_der_Provinz_Guizhou(null, Category.TYPE_ID + Category.NAME.key),
        Stadt_in_der_Provinz_Hainan(null, Category.TYPE_ID + Category.NAME.key),
        Stadt_in_der_Provinz_Hebei(null, Category.TYPE_ID + Category.NAME.key),
        Stadt_in_der_Provinz_Henan(null, Category.TYPE_ID + Category.NAME.key),
        Stadt_in_der_Provinz_Shaanxi(null, Category.TYPE_ID + Category.NAME.key),
        Stadt_in_der_Provinz_Yunnan(null, Category.TYPE_ID + Category.NAME.key),
        Stadtbezirk_in_Putian(null, Category.TYPE_ID + Category.NAME.key),
        Stadtbezirk_von_Kaohsiung(null, Category.TYPE_ID + Category.NAME.key),
        Stadtbezirk_von_Peking(null, Category.TYPE_ID + Category.NAME.key),
        Stadtteil_von_Taipeh(null, Category.TYPE_ID + Category.NAME.key),
        Statistik(null, Category.TYPE_ID + Category.MATHEMATICS.key),
        Sternanis(null, null),
        Sternbild(null, Category.TYPE_ID + Category.ASTRONOMY.key),
        Strom(null, null),
        Sänger(null, Category.TYPE_ID + Category.MUSIC.key),
        TCM(null, null),
        TV(null, Category.TYPE_ID + Category.MEDIA.key),
        Tag_des_Monats(null, Category.TYPE_ID + Category.TIME.key),
        Taiwan(null, Category.TYPE_ID + Category.NAME.key),
        Teil_des_Wiederkäuermagens(null, null),
        Tennis(null, Category.TYPE_ID + Category.SPORTS.key),
        Texas(null, Category.TYPE_ID + Category.NAME.key),
        Text(null, Category.TYPE_ID + Category.LITERATURE.key),
        Textil(null, Category.TYPE_ID + Category.HOME.key),
        Tiefdruckzylinder(null, Category.TYPE_ID + Category.INDUSTRY.key),
        Tier(null, Category.TYPE_ID + Category.ANIMALS.key),
        Tierkreiszeichen(null, Category.TYPE_ID + Category.MYTHOLOGY.key),
        Tokio(null, Category.TYPE_ID + Category.NAME.key),
        USA(null, Category.TYPE_ID + Category.NAME.key),
        Uhr(null, Category.TYPE_ID + Category.TIME.key),
        Umgangssprache(null, Usage.TYPE_ID + Usage.FAMILIAR.key),
        VR_China(null, Category.TYPE_ID + Category.NAME.key),
        Versandraum(null, null),
        Virginia(null, Category.TYPE_ID + Category.NAME.key),
        Volksgruppe_in_China(null, Category.TYPE_ID + Category.PEOPLE.key),
        WTO(null, null),
        Waren(null, Category.TYPE_ID + Category.ECONOMY.key),
        Wasser(null, null),
        Werkzeug(null, Category.TYPE_ID + Category.HOME.key),
        Werkzeug_zum_Finden_von_Computerfehlern(null, Category.TYPE_ID + Category.SOFTWARE.key),
        West_Virginia(null, Category.TYPE_ID + Category.NAME.key),
        Zeit(null, Category.TYPE_ID + Category.TIME.key),
        Zeitung(null, Category.TYPE_ID + Category.MEDIA.key),
        Zimt(null, Category.TYPE_ID + Category.CUISINE.key),
        ab(null, null),
        alternative_Form_für_Finanzwesen(null, Category.TYPE_ID + Category.ECONOMY.key),
        altägyptischer_Pharao(null, Category.TYPE_ID + Category.HISTORY.key),
        an(null, null),
        anspucken_und(null, null),
        auf_der_Speisekarte(null, Category.TYPE_ID + Category.CUISINE.key),
        berücksichtigt_verschiedene_Sichtweisen_gebildet(null, null),
        britischer_Politiker(null, Category.TYPE_ID + Category.POLITICS.key),
        chinesische_Provinz(null, Category.TYPE_ID + Category.NAME.key),
        chinesische_Provinz_Fujian(null, Category.TYPE_ID + Category.NAME.key),
        der_Feuerwehr(null, null),
        deutscher_Politiker(null, Category.TYPE_ID + Category.POLITICS.key),
        ein(null, null),
        ein_Dinosaurier(null, Category.TYPE_ID + Category.ANIMALS.key),
        ein_Enzym(null, Category.TYPE_ID + Category.BIOLOGY.key),
        ein_Fisch(null, Category.TYPE_ID + Category.ANIMALS.key),
        ein_Mineral(null, Category.TYPE_ID + Category.GEOGRAPHY.key),
        ein_Mond_des_Planeten_Jupiter(null, Category.TYPE_ID + Category.ASTRONOMY.key),
        ein_Schwimmstil(null, Category.TYPE_ID + Category.SPORTS.key),
        ein_Sensenfisch(null, null),
        ein_Sortieralgorithmus(null, Category.TYPE_ID + Category.MATHEMATICS.key),
        ein_Speisepilz(null, Category.TYPE_ID + Category.CUISINE.key),
        ein_Stadtbezirk_von_Tokio(null, Category.TYPE_ID + Category.NAME.key),
        ein_Teletubby(null, null),
        ein_Vogel(null, Category.TYPE_ID + Category.ANIMALS.key),
        ein_Wasserstoffisotop(null, Category.TYPE_ID + Category.CHEMISTRY.key),
        ein_Zeichenlexikon_der_chinesischen_Sprache(null, Category.TYPE_ID + Category.LITERATURE.key),
        ein_regelmä_iger_Körper(null, Category.TYPE_ID + Category.MATHEMATICS.key),
        eine(null, null),
        eine_Fischfamilie(null, Category.TYPE_ID + Category.BIOLOGY.key),
        eine_Kinderkrankheit(null, Category.TYPE_ID + Category.MEDICINE.key),
        eine_Längeneinheit(null, Category.TYPE_ID + Category.MATHEMATICS.key),
        eine_Ordnung_der_Säugetiere(null, Category.TYPE_ID + Category.BIOLOGY.key),
        eine_Periode_der_Erdgeschichte(null, Category.TYPE_ID + Category.HISTORY.key),
        eine_Pflanze(null, Category.TYPE_ID + Category.PLANTS.key),
        eine_Pflanzengattung(null, Category.TYPE_ID + Category.PLANTS.key),
        eine_Provinz_in_Russland(null, Category.TYPE_ID + Category.NAME.key),
        eine_Vogelart(null, Category.TYPE_ID + Category.ANIMALS.key),
        eine_Vogelfamilie(null, Category.TYPE_ID + Category.BIOLOGY.key),
        einer_Zeitung(null, Category.TYPE_ID + Category.MEDIA.key),
        einer_der_fünf_Heiligen_Berge_des_Daoismus_in_China(null, Category.TYPE_ID + Category.RELIGION.key),
        einer_der_vier_heiligen_Berge_des_Buddhismus(null, Category.TYPE_ID + Category.RELIGION.key),
        eines_Buches(null, Category.TYPE_ID + Category.LITERATURE.key),
        engl_Greenhorn(null, null),
        englischer_Fu_ballspieler(null, Category.TYPE_ID + Category.SPORTS.key),
        englischer_Fu_ballverein(null, Category.TYPE_ID + Category.SPORTS.key),
        etc(null, null),
        etw(null, null),
        e_barer_Algen(null, Category.TYPE_ID + Category.CUISINE.key),
        früher(null, null),
        fälschungssicherer(null, null),
        gehen(null, null),
        griech_Gott(null, Category.TYPE_ID + Category.MYTHOLOGY.key),
        griech_Göttin(null, Category.TYPE_ID + Category.MYTHOLOGY.key),
        griech_Mythologie(null, Category.TYPE_ID + Category.MYTHOLOGY.key),
        hier(null, null),
        hist(null, null),
        in(null, null),
        in_Shanghai(null, Category.TYPE_ID + Category.NAME.key),
        incoterms(null, Category.TYPE_ID + Category.ECONOMY.key),
        indischer_Bundesstaat(null, Category.TYPE_ID + Category.NAME.key),
        italienischer_Fu_ballverein(null, Category.TYPE_ID + Category.SPORTS.key),
        kastriertes_Pferd(null, Category.TYPE_ID + Category.PETS.key),
        lassen(null, null),
        lat_Acacia_homalophylla(null, null),
        lat_Eleocharis_dulcis(null, null),
        lat_Gekkonidae(null, null),
        lat_Ostreidae(null, null),
        lat_Porphyra_yezoensis(null, null),
        lat_Robinia_pseudoacacia(null, null),
        lat_Styphnolobium_japonicum(null, null),
        lat_Tremella_fuciformis(null, null),
        lat_Auricularia_auricula_judae(null, null),
        lat_Hibiscus_sabdariffa(null, null),
        lat_Quercus(null, null),
        mit_dessen_Erlös_gewaltförmige_Konflikte_finanziert_werden(null, null),
        mütterlicherseits(null, Category.TYPE_ID + Category.PEOPLE.key),
        nach(null, null),
        pres_Sichuan(null, null),
        regierungsunmittelbare_Stadt_Chongqing(null, Category.TYPE_ID + Category.NAME.key),
        roter_Stein_im_chinesischen_Schach(null, Category.TYPE_ID + Category.SPORTS.key),
        schwarzer_Stein_im_chinesischen_Schach(null, Category.TYPE_ID + Category.SPORTS.key),
        schwedischer_Chemiker(null, Category.TYPE_ID + Category.CHEMISTRY.key),
        sein(null, null),
        sich(null, WordType.TYPE_ID + WordType.REFLEXIVE.key),
        stūpa(null, null),
        subjektiv_ohne_andere_Sichtweise_zu_berücksichtigen(null, null),
        väterlicherseits(null, Category.TYPE_ID + Category.PEOPLE.key),
        wie(null, null),
        Überseedepartement_in_Frankreich(null, Category.TYPE_ID + Category.NAME.key),
        über(null, null),
        Sprachwissenschafter_und_Sammler_von_Märchen(null, Category.TYPE_ID + Category.LITERATURE.key),
        Anime(null, Category.TYPE_ID + Category.HOBBY.key),
        Lehnwort(null, null),
        Machilus_nanmu(null, null),
        Phoebe_nanmu(null, null),
        alt(null, null),
        lat_Castanea_mollissima(null, null),
        lat_Phoebe_zhennan_SLee_et_FNWei(null, null),
        lat_Brassica_oleracea_var_botrytis(null, null),
        lat_Brassica_oleracea_var_silvestris(null, null),
        成(null, null),
        方(null, null),
        Alabama(null, Category.TYPE_ID + Category.NAME.key),
        Autonomes_Gebiet_Guangxi_der_Zhuang(null, Category.TYPE_ID + Category.NAME.key),
        Autonomes_Gebiet_Ningxia(null, Category.TYPE_ID + Category.NAME.key),
        Autor_Gaston_Leroux(null, null),
        Autor_Gu_Yanwu(null, null),
        Autor_Yang_Xiong(null, null),
        Colorado(null, Category.TYPE_ID + Category.NAME.key),
        Dichter_und_Gelehrter(null, null),
        English_World_Trade_Organization(null, Category.TYPE_ID + Category.POLITICS.key),
        Florida(null, Category.TYPE_ID + Category.NAME.key),
        Illinois(null, Category.TYPE_ID + Category.NAME.key),
        Kreis_in_Henan(null, Category.TYPE_ID + Category.NAME.key),
        Kreis_in_Shanxi(null, Category.TYPE_ID + Category.NAME.key),
        Kreis_in_Xinjiang(null, Category.TYPE_ID + Category.NAME.key),
        Licht(null, null),
        Oregon(null, null),
        Stadt_der_Provinz_Zhejiang(null, Category.TYPE_ID + Category.NAME.key),
        Stadt_in_Kalifornien(null, Category.TYPE_ID + Category.NAME.key),
        Stadt_in_der_Provinz_Hubei(null, Category.TYPE_ID + Category.NAME.key),
        Stadt_in_der_Provinz_Jiangsu(null, Category.TYPE_ID + Category.NAME.key),
        Stadt_in_der_Provinz_Shandong(null, Category.TYPE_ID + Category.NAME.key),
        Stadt_in_der_Provinz_Sichuan(null, Category.TYPE_ID + Category.NAME.key),
        Teil_des_Flugwerks(null, Category.TYPE_ID + Category.AERIAL.key),
        Tennessee(null, Category.TYPE_ID + Category.NAME.key),
        dm(null, null),
        regierungsunmittelbare_Stadt_Tianjin(null, Category.TYPE_ID + Category.NAME.key),
        स्तूप(null, null),
        Abk(null, WordType.TYPE_ID + WordType.ABBREVIATION.key),
        Amt(null, null),
        Armbanduhr(null, Category.TYPE_ID + Category.TIME.key),
        Auszeichnung(null, null),
        Bakterien(null, Category.TYPE_ID + Category.BIOLOGY.key),
        Bankkonto(null, Category.TYPE_ID + Category.ECONOMY.key),
        Baseball(null, Category.TYPE_ID + Category.SPORTS.key),
        Bezirk_in_Shanxi(null, Category.TYPE_ID + Category.NAME.key),
        Bogen(null, null),
        Buchdruckform(null, Category.TYPE_ID + Category.LITERATURE.key),
        Buchschnitt(null, Category.TYPE_ID + Category.LITERATURE.key),
        Bus(null, null),
        Börse(null, Category.TYPE_ID + Category.ECONOMY.key),
        Dao(null, null),
        Drehmaschine(null, Category.TYPE_ID + Category.INDUSTRY.key),
        E_Mail(null, Category.TYPE_ID + Category.SOFTWARE.key),
        Ehe(null, null),
        Eis(null, null),
        Fabelwesen(null, Category.TYPE_ID + Category.MYTHOLOGY.key),
        Falzbogen(null, null),
        Feng_Shui(null, Category.TYPE_ID + Category.MYTHOLOGY.key),
        Film_von_Ang_Lee(null, null),
        Filmspule(null, null),
        Fischart(null, Category.TYPE_ID + Category.ANIMALS.key),
        Fleisch(null, Category.TYPE_ID + Category.CUISINE.key),
        Fluss_in_China(null, Category.TYPE_ID + Category.NAME.key),
        Fotosatz(null, Category.TYPE_ID + Category.ARTS.key),
        Freude(null, null),
        Futur(null, null),
        Gefä_(null, null),
        Gesicht(null, Category.TYPE_ID + Category.PEOPLE.key),
        Getränk_aus_Tee(null, null),
        Gewicht(null, null),
        GmbH(null, Category.TYPE_ID + Category.NAME.key),
        Gras(null, null),
        Guangdong(null, Category.TYPE_ID + Category.NAME.key),
        Hauptstadt_von_Taiwan(null, Category.TYPE_ID + Category.NAME.key),
        Heilmittel(null, Category.TYPE_ID + Category.MEDICINE.key),
        Heirat(null, Category.TYPE_ID + Category.SOCIETY.key),
        Hut(null, Category.TYPE_ID + Category.COSMETICS.key),
        Ich(null, null),
        Internet(null, Category.TYPE_ID + Category.SOFTWARE.key),
        Kantonesisch(null, null),
        Kinder(null, null),
        Konserve(null, Category.TYPE_ID + Category.CUISINE.key),
        Konversation(null, null),
        Kriminalfall(null, Category.TYPE_ID + Category.SOCIETY.key),
        Kǒng_Fūzǐ_Lehrmeister_Kong(null, null),
        Kǒngzǐ(null, null),
        Land(null, null),
        Leute(null, null),
        Liebe(null, null),
        Metall(null, Category.TYPE_ID + Category.CHEMISTRY.key),
        Pflanzen(null, Category.TYPE_ID + Category.PLANTS.key),
        Programmierung(null, Category.TYPE_ID + Category.SOFTWARE.key),
        Provinz_in_China(null, Category.TYPE_ID + Category.NAME.key),
        Recht(null, Category.TYPE_ID + Category.LAW.key),
        Reproduktion(null, null),
        Rockband(null, Category.TYPE_ID + Category.MUSIC.key),
        Rufname(null, null),
        Salz_der_Chlorsäure(null, Category.TYPE_ID + Category.CHEMISTRY.key),
        Schimpfwort(null, Usage.TYPE_ID + Usage.VULGAR.key),
        Shanghai_Dialekt(null, Usage.TYPE_ID + Usage.DIALECT.key),
        Spielfigur_im_chin_Schach(null, null),
        Stadt_in_Ningxia(null, Category.TYPE_ID + Category.NAME.key),
        Stand(null, null),
        Tabakblätter(null, Category.TYPE_ID + Category.PLANTS.key),
        Tageszeitung_aus_Taiwan(null, Category.TYPE_ID + Category.MEDIA.key),
        Technik(null, Category.TYPE_ID + Category.TECHNOLOGY),
        Tee(null, Category.TYPE_ID + Category.CUISINE.key),
        Teil_des_Gehirns(null, Category.TYPE_ID + Category.BIOLOGY.key),
        Textiltechnik(null, Category.TYPE_ID + Category.TECHNOLOGY),
        Textilw(null, Category.TYPE_ID + Category.INDUSTRY.key),
        UN(null, null),
        Vietnam(null, Category.TYPE_ID + Category.NAME.key),
        Zeichentrickfilm(null, Category.TYPE_ID + Category.MEDIA.key),
        Zeitschrift(null, Category.TYPE_ID + Category.MEDIA.key),
        Zug(null, Category.TYPE_ID + Category.RAILWAYS.key),
        abwertend(null, Usage.TYPE_ID + Usage.PEJORATIVE.key),
        allgemein(null, null),
        als_Trauer(null, null),
        als_Ware(null, null),
        altes_Zeichen_der_Trauer(null, null),
        am_Dorf(null, Category.TYPE_ID + Category.NAME.key),
        am_Ort(null, Category.TYPE_ID + Category.NAME.key),
        amtl(null, null),
        auf(null, null),
        buddhistische(null, Category.TYPE_ID + Category.RELIGION.key),
        chin(null, null),
        chin_Bekleidung_für_Frauen_aus_der_Qing_Zeit(null, Category.TYPE_ID + Category.HISTORY.key),
        chin_Blasinstrument(null, Category.TYPE_ID + Category.MUSIC.key),
        chin_孔子(null, Category.TYPE_ID + Category.HISTORY.key),
        das(null, null),
        ein_Elementarteilchen(null, Category.TYPE_ID + Category.PHYSICS.key),
        ein_Herzglykosid(null, null),
        ein_Hund_hat_etw(null, null),
        ein_Kasus(null, null),
        eine_Apfelsorte(null, Category.TYPE_ID + Category.CUISINE.key),
        eine_Ordnung_der_Vögel(null, Category.TYPE_ID + Category.ANIMALS.key),
        eine_Pflanzenfamilie(null, Category.TYPE_ID + Category.PLANTS.key),
        eine_Pflanzenordnung(null, Category.TYPE_ID + Category.PLANTS.key),
        eine_Provinz_der_Mongolei(null, Category.TYPE_ID + Category.NAME.key),
        eine_Rinderrasse(null, Category.TYPE_ID + Category.ANIMALS.key),
        eine_Sensenfischgattung(null, Category.TYPE_ID + Category.ANIMALS.key),
        eine_Vogelgattung(null, Category.TYPE_ID + Category.ANIMALS.key),
        eine_Volksgruppe_in_China(null, Category.TYPE_ID + Category.PEOPLE.key),
        english(null, null),
        freier(null, null),
        früher_Kunstfaser(null, null),
        für_Kinder(null, null),
        gefüllte_Teigtäschchen(null, null),
        geographische_Region_Kroatiens(null, null),
        griech_Philosoph(null, Category.TYPE_ID + Category.PHILOSOPHY.key),
        haben(null, null),
        hoch(null, null),
        ich(null, null),
        im_Flugzeug(null, Category.TYPE_ID + Category.AERIAL.key),
        im_alten_China(null, Category.TYPE_ID + Category.NAME.key),
        in_Prozent(null, null),
        in_Yunnan(null, Category.TYPE_ID + Category.NAME.key),
        ist(null, null),
        jap_Manga_Serie(null, Category.TYPE_ID + Category.HOBBY.key),
        kandierte(null, null),
        kleines(null, null),
        lat_Acacia_confusa(null, null),
        lat_Acacia_melanoxylon(null, null),
        lat_Acacia_senegal(null, null),
        lat_Rhodoplantae(null, null),
        lat_Sciurus_vulgaris(null, null),
        lat_Trapa_natans(null, null),
        lat_Alopex_lagopus(null, null),
        lat_Capsicum_annuum(null, null),
        lat_Fraxinus_velutina(null, null),
        latinisiert_aus_孔夫子(null, null),
        literarisch(null, Usage.TYPE_ID + Usage.POETIC.key),
        machen(null, null),
        militärische(null, Category.TYPE_ID + Category.MILITARY.key),
        mit(null, null),
        obenstehend(null, null),
        oft_durch_Zecken_übertragen(null, null),
        schriftliche(null, null),
        taiwanisches_Computerunternehmen(null, null),
        ver(null, null),
        vom_Kühlschrank(null, null),
        von(null, null),
        zu(null, null),
        zusammen_messbar(null, null),
        Österreich(null, Category.TYPE_ID + Category.NAME.key),
        Überschall_Verkehrsflugzeug(null, Category.TYPE_ID + Category.AERIAL.key),
        älterer_Bruder_des_Vaters(null, null),
        über_Galvanisation(null, null),
        书经shu1jing1(null, null),
        印刷故障(null, null),
        四川Sìchuān(null, null),
        成语(null, Usage.TYPE_ID + Usage.POETIC.key),
        排版(null, null),
        摘取要点(null, null),
        油墨(null, null),
        白蛇传_Báishézhuàn(null, null),
        道藏Dao4zang4(null, null),
        Zeitform(null, null),
        Theater(null, Category.TYPE_ID + Category.MUSIC.key),
        anatom(null, Category.TYPE_ID + Category.MEDICINE.key),
        finanz(null, Category.TYPE_ID + Category.ECONOMY.key),
        katonesisch(null, null),
        metaphorisch(null, Usage.TYPE_ID + Usage.METAPHORICAL.key),
        口(null, null),
        雅geh(null, null),
        Abk_董事(null, null),
        Glückwunsch_zur_Hochzeit(null, null),
        auch_头疼(null, null),
        auch_扇(null, null),
        auch_樑(null, null),
        auch_煽(null, null),
        auch_熏(null, null),
        auch_燻(null, null),
        ca_7m_über_NN(null, null),
        dickflüssig(null, null),
        dünnflüssig(null, null),
        hist_für_世(null, null),
        kanton_Lehnwort(null, null),
        lat_Lycium_barbarum_var_barbarum_L(null, null),
        lat_Panax_ginseng(null, null),
        lat_Pseudorca_crassidens(null, null),
        lat_Brassica_rapa_pekinensis(null, null),
        siehe_余_yu(null, null),
        veralt(null, Usage.TYPE_ID + Usage.DATED.key),
        搧(null, null),
        燻(null, null),
        薰(null, null),
        视(null, null),
        Getreide(null, Category.TYPE_ID + Category.AGRICULTURE.key),
        Gummituch(null, null),
        Handwerker(null, Category.TYPE_ID + Category.INDUSTRY.key),
        Insel(null, Category.TYPE_ID + Category.NAME.key),
        Maschine(null, Category.TYPE_ID + Category.INDUSTRY.key),
        Stadt_in_der_Provinz_Liaoning(null, Category.TYPE_ID + Category.NAME.key),
        Wind(null, null),
        eine_Aminosäure(null, Category.TYPE_ID + Category.CHEMISTRY.key),
        Österr(null, null),
        书经shujing(null, null),
        道藏Daozang(null, null);
        private final String attrs;
        private final String value;

        HanDeCategory(String value, String attrs) {
            this.value = value;
            this.attrs = attrs;
        }

        @Override
        public String getAttributes() {
            return attrs;
        }

        @Override
        public String getReplacement() {
            return value;
        }

        @Override
        public String getConstantName() {
            return Helper.toConstantName(this.name());
        }
    }

    public static interface EdictCategory {
        public String getAttributes();

        public String getReplacement();

        public String getConstantName();
    }
}
