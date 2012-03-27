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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.kk.kkdict.beans.FormattedTreeMap;
import cn.kk.kkdict.beans.FormattedTreeSet;
import cn.kk.kkdict.beans.Stat;
import cn.kk.kkdict.types.Category;
import cn.kk.kkdict.types.Language;
import cn.kk.kkdict.types.TranslationSource;
import cn.kk.kkdict.utils.ChineseHelper;
import cn.kk.kkdict.utils.Helper;

/**
 * Download: http://www.handedict.de/chinesisch_deutsch.php?mode=dl
 * 
 * @author x_kez
 * 
 */
public class EdictZhDeExtractor {
    public static final String EDICT_FILE = Helper.DIR_IN_DICTS+"\\edict\\handedict_nb.u8";

    public static final String OUT_DIR = Helper.DIR_OUT_DICTS+"\\edict";

    public static final String[] IRRELEVANT_WORDS_STRINGS = { "(u.E.)" };

    public static void main(String args[]) throws IOException {
        HanDeCategory[] csValues = HanDeCategory.values();
        String[] validCategoryKeys = new String[csValues.length];
        for (int i = 0; i < csValues.length; i++) {
            HanDeCategory c = csValues[i];
            validCategoryKeys[i] = Helper.toConstantName(c.name());
        }
        Arrays.sort(validCategoryKeys);
        extractDict(TranslationSource.EDICT_ZH_DE, EDICT_FILE, Helper.CHARSET_UTF8, OUT_DIR, Language.ZH, Language.DE,
                validCategoryKeys, IRRELEVANT_WORDS_STRINGS);
    }

    protected static void extractDict(TranslationSource translationSource, String file, Charset encoding, String outDir,
            Language srcLng, Language trgLng, String[] validCategoryKeys, String[] irrelevantWordsStrings)
            throws FileNotFoundException, IOException {
        long timeStarted = System.currentTimeMillis();
        Helper.precheck(file, outDir);
        System.out.println("读取词典文件'" + file + "' 。。。");
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding),
                Helper.BUFFER_SIZE);
        String dictFileName = outDir + File.separator + "output-dict_" + srcLng.key + "_" + trgLng.key + "."
                + translationSource.key;
        System.out.print("导出词典文件'" + dictFileName + "'。。。");
        BufferedWriter writer = new BufferedWriter(new FileWriter(dictFileName), Helper.BUFFER_SIZE);

        String line;
        String name = null;
        String translations;
        Map<String, Integer> globalCategories = new FormattedTreeMap<String, Integer>();
        Set<String> categories = null;
        Set<String> descriptions = null;
        String tmp;

        int statSkipped = 0;
        int statOk = 0;

        Map<String, String> languages = new FormattedTreeMap<String, String>();
        while ((line = reader.readLine()) != null) {
            if (srcLng == Language.ZH) {
                line = ChineseHelper.toSimplifiedChinese(line.trim());
            }
            if (line.startsWith("#")) {
                continue;
            } else if (Helper.isNotEmptyOrNull(tmp = Helper.substringBetween(line, " ", " "))) {
                name = tmp;
                translations = Helper.substringBetweenEnclose(line, "/", "/");
                if (Helper.isNotEmptyOrNull(name) && Helper.isNotEmptyOrNull(translations)) {
                    String[] transArray = translations.split("/");
                    for (String translation : transArray) {
                        if (!translation.contains("???")) {
                            categories = new FormattedTreeSet<String>();
                            descriptions = new FormattedTreeSet<String>(", ");
                            for (String i : irrelevantWordsStrings) {
                                translation = translation.replace(i, Helper.EMPTY_STRING);
                            }

                            translation = Helper.unescapeHtml(translation);
                            translation = translation.replace("&gt", ">");
                            while (!translation.equals((tmp = extractCategories(translation, globalCategories,
                                    categories, validCategoryKeys, descriptions)))) {
                                translation = tmp;
                            }

                            translation = translation
                                    .replaceAll("([\\(\\)\\{\\}\\[\\]])|([，,.]+$)", Helper.EMPTY_STRING)
                                    .replaceAll("[ ]+", " ").trim();

                            String descriptionText = Helper.EMPTY_STRING;
                            if (!descriptions.isEmpty()) {
                                if (Helper.isEmptyOrNull(translation)) {
                                    descriptionText = "(" + descriptions.toString() + ")";
                                } else {
                                    descriptionText = " (" + descriptions.toString() + ")";
                                }
                            }

                            String sourceString = Helper.SEP_ATTRIBUTE + TranslationSource.TYPE_ID
                                    + translationSource.key;
                            if (translation.contains(", ") || translation.contains("; ")) {
                                translation = translation.replace(", ", descriptionText + sourceString
                                        + Helper.SEP_SAME_MEANING);
                                translation = translation.replace("; ", descriptionText + sourceString
                                        + Helper.SEP_SAME_MEANING);
                                if (!translation.endsWith(sourceString)) {
                                    translation += sourceString;
                                }
                            } else {
                                translation += descriptionText + sourceString;
                            }
                            String cats = categories.toString();
                            String tmp1 = name.trim() + sourceString;
                            String tmp2 = translation;
                            if (!cats.isEmpty()) {
                                // TODO
                                tmp1 += Helper.SEP_ATTRIBUTE + Category.TYPE_ID;
                                tmp2 += Helper.SEP_ATTRIBUTE + Category.TYPE_ID;
                            }
                            languages.put(srcLng.key, tmp1);
                            languages.put(trgLng.key, tmp2);
                            writer.write(languages.toString());
                            writer.write(Helper.SEP_NEWLINE);
                            statOk++;
                            continue;
                        }
                    }
                }
                statSkipped++;
            }
        }
        reader.close();
        writer.close();
        System.out.println("成功");

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

    private static String extractCategories(String translation, Map<String, Integer> globalCategories,
            Set<String> categories, String[] cs, Set<String> descriptions) {
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
                            if (Arrays.binarySearch(cs, tmp) >= 0) {
                                categories.add(s);
                                Helper.add(globalCategories, s);
                                if (s.length() > 18 || s.contains(" ") || s.contains(".") || s.contains("-")) {
                                    descriptions.add(s);
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

    public static enum HanDeCategory {
        Adj,
        Adv,
        Agrar,
        Arch,
        Astron,
        Auto,
        Bio,
        Buddh,
        Chem,
        Druckw,
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
        Konj,
        Kunst,
        Lit,
        Masami,
        Math,
        Med,
        Met,
        Mil,
        Mus,
        Num,
        Org,
        Pers,
        Philos,
        Philosoph,
        Phys,
        Pol,
        Präp,
        Pron,
        Psych,
        Rechtsw,
        Rel,
        S,
        Sport,
        Sprachw,
        Sprichw,
        Tech,
        Tite,
        ugs,
        V,
        Vorn,
        Werk,
        Wirtsch,
        vulg,
        Zähl,
        wörtl,
        veraltet,
        umgangssprachlich,
        umg,
        r,
        pl,
        n,
        jur,
        jap,
        fig,
        en,
        engl,
        englisch,
        e,
        poet,
        lat,
        methaphorisch,
        Redew,
        Biochem,
        Stadt,
        Berg,
        Fluss,
        Kleidung,
        Fisch,
        Autor_Shanghai_Daily,
        Architekt,
        Bopomofo_Aussprache_Zeichen,
        Grammatik,
        Hund,
        Satzzeichen,
        Taxonomie,
        astrol,
        geh,
        Aktienmarkt,
        Automarke,
        Autonomes_Gebiet_Innere_Mongolei,
        Autonomes_Gebiet_Tibet,
        Autonomes_Gebiet_Xinjiang,
        Autonomes_Gebiet_in_China,
        Avalokiteśvara,
        Beijing,
        Bergbau,
        Bezirk_in_Anhui,
        Bezirk_in_Fujian,
        Bezirk_in_Gansu,
        Bezirk_in_Guangdong,
        Bezirk_in_Guangxi,
        Bezirk_in_Guizhou,
        Bezirk_in_Hebei,
        Bezirk_in_Heilongjiang,
        Bezirk_in_Henan,
        Bezirk_in_Hongkong,
        Bezirk_in_Hubei,
        Bezirk_in_Hunan,
        Bezirk_in_Jiangsu,
        Bezirk_in_Jiangxi,
        Bezirk_in_Jilin,
        Bezirk_in_Shaanxi,
        Bezirk_in_Shandong,
        Bezirk_in_Sichuan,
        Bezirk_in_Tibet,
        Bezirk_in_Xinjiang,
        Bezirk_in_Yunnan,
        Bezirk_in_Zhejiang,
        Bezirk_von_Hongkong,
        Bibel,
        Bildschirm,
        Bildverarbeitung,
        Bleisatz,
        Blütenpfeffer,
        Bodhisattva_des_Mitgefühls,
        Botanik,
        Bruchrechnung,
        Buch,
        Buchbinderei,
        Buddhismus,
        Bundesstaat_der_USA,
        Bundesstaat_von_Malaysia,
        Bühnentechnik,
        Canton,
        China,
        Christentum,
        Computer,
        Computerspiel,
        Departement_in_Frankreich,
        Deutschland,
        Dialekt,
        Diamant,
        Dimsum,
        Dorf_in_Taiwan,
        Druck,
        Drucktechnik,
        Druckwesen,
        Eisenbahn,
        Energie,
        England,
        Familienname,
        Farbe,
        Fenchel,
        Fernsehserie,
        Feuer,
        Film,
        Filmtitel,
        Finalpartikel,
        Firma,
        Firmenname,
        Flugzeug,
        Foto,
        Fotografie,
        Frankreich,
        Fu_ballklub,
        Gegend_in_Hebei,
        Gegend_in_Shandong,
        Gegend_in_Sichuan,
        Gegend_in_Taiwan,
        Geld,
        Georgia,
        Gewürz,
        Gewürznelken,
        Haar,
        Halsdrüsengeschwulst,
        Hong_Kong,
        Hongkong,
        I,
        II,
        III,
        Indiana,
        Indien,
        Italien,
        Jacob_und_Wilhelm_Grimm,
        Japan,
        Kalifornien,
        Kalligraphie_Zeichen_Bestandteil,
        Kalligraphie_Zeichenbestandteil,
        Kamera,
        Kanada,
        Krankheit,
        Kredit,
        Kreis_Changxing,
        Kreis_in_Shandong,
        Kreis_in_Sichuan,
        Kreuzfahrtschiff,
        L,
        Manga_Serie,
        Medizin,
        Mensch,
        Messer,
        Minnesota,
        Motor,
        Musik,
        Mythologie,
        Mähdrescher,
        Name,
        OECD,
        Offsetdruck,
        Ort,
        Ort_in_Anhui,
        Ort_in_Beijing,
        Ort_in_Fujian,
        Ort_in_Gansu,
        Ort_in_Guangdong,
        Ort_in_Guangxi,
        Ort_in_Guizhou,
        Ort_in_Hainan,
        Ort_in_Hebei,
        Ort_in_Heilongjiang,
        Ort_in_Henan,
        Ort_in_Hubei,
        Ort_in_Hunan,
        Ort_in_Inner_Mongolia,
        Ort_in_Jiangsu,
        Ort_in_Jiangxi,
        Ort_in_Jilin,
        Ort_in_Kham_prov_of_Tibet,
        Ort_in_Liaoning,
        Ort_in_Ningxia,
        Ort_in_Qinghai,
        Ort_in_Shaanxi,
        Ort_in_Shandong,
        Ort_in_Shanghai,
        Ort_in_Shanxi,
        Ort_in_Sichuan,
        Ort_in_Taiwan,
        Ort_in_Tianjin,
        Ort_in_Tibet,
        Ort_in_Xinjiang,
        Ort_in_Yunnan,
        Ort_in_Zhejiang,
        Ort_in_central_Tibet,
        Ortsname,
        Papier,
        Patent,
        Person,
        Pferd,
        Platte,
        Provinz_Anhui,
        Provinz_Fujian,
        Provinz_Gansu,
        Provinz_Guangdong,
        Provinz_Guangxi,
        Provinz_Guizhou,
        Provinz_Hainan,
        Provinz_Hebei,
        Provinz_Heilongjiang,
        Provinz_Henan,
        Provinz_Hubei,
        Provinz_Hunan,
        Provinz_Jiangsu,
        Provinz_Jiangxi,
        Provinz_Jilin,
        Provinz_Liaoning,
        Provinz_Qinghai,
        Provinz_Shaanxi,
        Provinz_Shandong,
        Provinz_Shanxi,
        Provinz_Sichuan,
        Provinz_Xinjiang,
        Provinz_Yunnan,
        Provinz_Zhejiang,
        Region_in_Deutschland,
        Region_in_Italien,
        Repro,
        Rollendruckmaschine,
        Rollenoffset,
        Rollenoffsetdruck,
        Russland,
        Sanskrit,
        Sanskrit_m,
        Satz,
        Schauspielerin,
        Schiff,
        Schulden,
        Schule,
        Schweiz,
        Sci_Fi_Serie,
        Shanghai,
        Sichuan_Spezialität,
        Siebdruck,
        Sophora_japonica,
        Spiel,
        Spielkarte,
        Sprichwort,
        Staat_in_Afrika,
        Stadt_der_Provinz_Fujian,
        Stadt_im_Autonomen_Gebiet_Innere_Mongolei,
        Stadt_im_Landkreis_Taipeh,
        Stadt_im_Landkreis_Taipei,
        Stadt_im_Norden_Taiwans,
        Stadt_in_Anhui,
        Stadt_in_Deutschland,
        Stadt_in_England,
        Stadt_in_Frankreich,
        Stadt_in_Fujian,
        Stadt_in_Gansu,
        Stadt_in_Guangdong,
        Stadt_in_Guangxi,
        Stadt_in_Guizhou,
        Stadt_in_Hainan,
        Stadt_in_Hebei,
        Stadt_in_Heilongjiang,
        Stadt_in_Henan,
        Stadt_in_Hubei,
        Stadt_in_Hunan,
        Stadt_in_Indien,
        Stadt_in_Inner_Mongolia,
        Stadt_in_Italien,
        Stadt_in_Japan,
        Stadt_in_Jiangsu,
        Stadt_in_Jiangxi,
        Stadt_in_Jilin,
        Stadt_in_Liaoning,
        Stadt_in_Niedersachsen,
        Stadt_in_Norwegen,
        Stadt_in_Pakistan,
        Stadt_in_Polen,
        Stadt_in_Russland,
        Stadt_in_Sachsen_Anhalt,
        Stadt_in_Shaanxi,
        Stadt_in_Shandong,
        Stadt_in_Shanxi,
        Stadt_in_Sichuan,
        Stadt_in_Taiwan,
        Stadt_in_Tschechien,
        Stadt_in_Xinjiang,
        Stadt_in_Yunnan,
        Stadt_in_Zhejiang,
        Stadt_in_den_Niederlanden,
        Stadt_in_der_Provinz_Anhui,
        Stadt_in_der_Provinz_Fujian,
        Stadt_in_der_Provinz_Gansu,
        Stadt_in_der_Provinz_Guangdong,
        Stadt_in_der_Provinz_Guizhou,
        Stadt_in_der_Provinz_Hainan,
        Stadt_in_der_Provinz_Hebei,
        Stadt_in_der_Provinz_Henan,
        Stadt_in_der_Provinz_Shaanxi,
        Stadt_in_der_Provinz_Yunnan,
        Stadtbezirk_in_Putian,
        Stadtbezirk_von_Kaohsiung,
        Stadtbezirk_von_Peking,
        Stadtteil_von_Taipeh,
        Statistik,
        Sternanis,
        Sternbild,
        Strom,
        Sänger,
        TCM,
        TV,
        Tag_des_Monats,
        Taiwan,
        Teil_des_Wiederkäuermagens,
        Tennis,
        Texas,
        Text,
        Textil,
        Tiefdruckzylinder,
        Tier,
        Tierkreiszeichen,
        Tokio,
        USA,
        Uhr,
        Umgangssprache,
        VR_China,
        Versandraum,
        Virginia,
        Volksgruppe_in_China,
        WTO,
        Waren,
        Wasser,
        Werkzeug,
        Werkzeug_zum_Finden_von_Computerfehlern,
        West_Virginia,
        Zeit,
        Zeitung,
        Zimt,
        ab,
        alternative_Form_für_Finanzwesen,
        altägyptischer_Pharao,
        an,
        anspucken_und,
        auf_der_Speisekarte,
        berücksichtigt_verschiedene_Sichtweisen_gebildet,
        britischer_Politiker,
        chinesische_Provinz,
        chinesische_Provinz_Fujian,
        der_Feuerwehr,
        deutscher_Politiker,
        ein,
        ein_Dinosaurier,
        ein_Enzym,
        ein_Fisch,
        ein_Mineral,
        ein_Mond_des_Planeten_Jupiter,
        ein_Schwimmstil,
        ein_Sensenfisch,
        ein_Sortieralgorithmus,
        ein_Speisepilz,
        ein_Stadtbezirk_von_Tokio,
        ein_Teletubby,
        ein_Vogel,
        ein_Wasserstoffisotop,
        ein_Zeichenlexikon_der_chinesischen_Sprache,
        ein_regelmä_iger_Körper,
        eine,
        eine_Fischfamilie,
        eine_Kinderkrankheit,
        eine_Längeneinheit,
        eine_Ordnung_der_Säugetiere,
        eine_Periode_der_Erdgeschichte,
        eine_Pflanze,
        eine_Pflanzengattung,
        eine_Provinz_in_Russland,
        eine_Vogelart,
        eine_Vogelfamilie,
        einer_Zeitung,
        einer_der_fünf_Heiligen_Berge_des_Daoismus_in_China,
        einer_der_vier_heiligen_Berge_des_Buddhismus,
        eines_Buches,
        engl_Greenhorn,
        englischer_Fu_ballspieler,
        englischer_Fu_ballverein,
        etc,
        etw,
        e_barer_Algen,
        früher,
        fälschungssicherer,
        gehen,
        griech_Gott,
        griech_Göttin,
        griech_Mythologie,
        hier,
        hist,
        in,
        in_Shanghai,
        incoterms,
        indischer_Bundesstaat,
        italienischer_Fu_ballverein,
        kastriertes_Pferd,
        lassen,
        lat_Acacia_homalophylla,
        lat_Eleocharis_dulcis,
        lat_Gekkonidae,
        lat_Ostreidae,
        lat_Porphyra_yezoensis,
        lat_Robinia_pseudoacacia,
        lat_Styphnolobium_japonicum,
        lat_Tremella_fuciformis,
        lat_Auricularia_auricula_judae,
        lat_Hibiscus_sabdariffa,
        lat_Quercus,
        mit_dessen_Erlös_gewaltförmige_Konflikte_finanziert_werden,
        mütterlicherseits,
        nach,
        pres_Sichuan,
        regierungsunmittelbare_Stadt_Chongqing,
        roter_Stein_im_chinesischen_Schach,
        schwarzer_Stein_im_chinesischen_Schach,
        schwedischer_Chemiker,
        sein,
        sich,
        stūpa,
        subjektiv_ohne_andere_Sichtweise_zu_berücksichtigen,
        väterlicherseits,
        wie,
        Überseedepartement_in_Frankreich,
        über,
        Sprachwissenschafter_und_Sammler_von_Märchen,
        Anime,
        Lehnwort,
        Machilus_nanmu,
        Phoebe_nanmu,
        alt,
        lat_Castanea_mollissima,
        lat_Phoebe_zhennan_SLee_et_FNWei,
        lat_Brassica_oleracea_var_botrytis,
        lat_Brassica_oleracea_var_silvestris,
        成,
        方,
        Alabama,
        Autonomes_Gebiet_Guangxi_der_Zhuang,
        Autonomes_Gebiet_Ningxia,
        Autor_Gaston_Leroux,
        Autor_Gu_Yanwu,
        Autor_Yang_Xiong,
        Colorado,
        Dichter_und_Gelehrter,
        English_World_Trade_Organization,
        Florida,
        Illinois,
        Kreis_in_Henan,
        Kreis_in_Shanxi,
        Kreis_in_Xinjiang,
        Licht,
        Oregon,
        Stadt_der_Provinz_Zhejiang,
        Stadt_in_Kalifornien,
        Stadt_in_der_Provinz_Hubei,
        Stadt_in_der_Provinz_Jiangsu,
        Stadt_in_der_Provinz_Shandong,
        Stadt_in_der_Provinz_Sichuan,
        Teil_des_Flugwerks,
        Tennessee,
        dm,
        regierungsunmittelbare_Stadt_Tianjin,
        स्तूप,
        Abk,
        Amt,
        Armbanduhr,
        Auszeichnung,
        Bakterien,
        Bankkonto,
        Baseball,
        Bezirk_in_Shanxi,
        Bogen,
        Buchdruckform,
        Buchschnitt,
        Bus,
        Börse,
        Dao,
        Drehmaschine,
        E_Mail,
        Ehe,
        Eis,
        Fabelwesen,
        Falzbogen,
        Feng_Shui,
        Film_von_Ang_Lee,
        Filmspule,
        Fischart,
        Fleisch,
        Fluss_in_China,
        Fotosatz,
        Freude,
        Futur,
        Gefä_,
        Gesicht,
        Getränk_aus_Tee,
        Gewicht,
        GmbH,
        Gras,
        Guangdong,
        Hauptstadt_von_Taiwan,
        Heilmittel,
        Heirat,
        Hut,
        Ich,
        Internet,
        Kantonesisch,
        Kinder,
        Konserve,
        Konversation,
        Kriminalfall,
        Kǒng_Fūzǐ_Lehrmeister_Kong,
        Kǒngzǐ,
        Land,
        Leute,
        Liebe,
        Metall,
        Pflanzen,
        Programmierung,
        Provinz_in_China,
        Recht,
        Reproduktion,
        Rockband,
        Rufname,
        Salz_der_Chlorsäure,
        Schimpfwort,
        Shanghai_Dialekt,
        Spielfigur_im_chin_Schach,
        Stadt_in_Ningxia,
        Stand,
        Tabakblätter,
        Tageszeitung_aus_Taiwan,
        Technik,
        Tee,
        Teil_des_Gehirns,
        Textiltechnik,
        Textilw,
        UN,
        Vietnam,
        Zeichentrickfilm,
        Zeitschrift,
        Zug,
        abwertend,
        allgemein,
        als_Trauer,
        als_Ware,
        altes_Zeichen_der_Trauer,
        am_Dorf,
        am_Ort,
        amtl,
        auf,
        buddhistische,
        chin,
        chin_Bekleidung_für_Frauen_aus_der_Qing_Zeit,
        chin_Blasinstrument,
        chin_孔子,
        das,
        ein_Elementarteilchen,
        ein_Herzglykosid,
        ein_Hund_hat_etw,
        ein_Kasus,
        eine_Apfelsorte,
        eine_Ordnung_der_Vögel,
        eine_Pflanzenfamilie,
        eine_Pflanzenordnung,
        eine_Provinz_der_Mongolei,
        eine_Rinderrasse,
        eine_Sensenfischgattung,
        eine_Vogelgattung,
        eine_Volksgruppe_in_China,
        english,
        freier,
        früher_Kunstfaser,
        für_Kinder,
        gefüllte_Teigtäschchen,
        geographische_Region_Kroatiens,
        griech_Philosoph,
        haben,
        hoch,
        ich,
        im_Flugzeug,
        im_alten_China,
        in_Prozent,
        in_Yunnan,
        ist,
        jap_Manga_Serie,
        kandierte,
        kleines,
        lat_Acacia_confusa,
        lat_Acacia_melanoxylon,
        lat_Acacia_senegal,
        lat_Rhodoplantae,
        lat_Sciurus_vulgaris,
        lat_Trapa_natans,
        lat_Alopex_lagopus,
        lat_Capsicum_annuum,
        lat_Fraxinus_velutina,
        latinisiert_aus_孔夫子,
        literarisch,
        machen,
        militärische,
        mit,
        obenstehend,
        oft_durch_Zecken_übertragen,
        schriftliche,
        taiwanisches_Computerunternehmen,
        ver,
        vom_Kühlschrank,
        von,
        zu,
        zusammen_messbar,
        Österreich,
        Überschall_Verkehrsflugzeug,
        älterer_Bruder_des_Vaters,
        über_Galvanisation,
        书经shu1jing1,
        印刷故障,
        四川Sìchuān,
        成语,
        排版,
        摘取要点,
        油墨,
        白蛇传_Báishézhuàn,
        道藏Dao4zang4,
        Zeitform,
        Theater,
        anatom,
        finanz,
        katonesisch,
        metaphorisch,
        口,
        雅geh,
        Abk_董事,
        Glückwunsch_zur_Hochzeit,
        auch_头疼,
        auch_扇,
        auch_樑,
        auch_煽,
        auch_熏,
        auch_燻,
        ca_7m_über_NN,
        dickflüssig,
        dünnflüssig,
        hist_für_世,
        kanton_Lehnwort,
        lat_Lycium_barbarum_var_barbarum_L,
        lat_Panax_ginseng,
        lat_Pseudorca_crassidens,
        lat_Brassica_rapa_pekinensis,
        siehe_余_yu,
        veralt,
        搧,
        燻,
        薰,
        视,
        Getreide,
        Gummituch,
        Handwerker,
        Insel,
        Maschine,
        Stadt_in_der_Provinz_Liaoning,
        Wind,
        eine_Aminosäure,
        Österr,
        书经shujing,
        道藏Daozang,
    }
}
