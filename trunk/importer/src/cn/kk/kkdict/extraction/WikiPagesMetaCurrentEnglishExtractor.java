package cn.kk.kkdict.extraction;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import cn.kk.kkdict.FormattedTreeMap;
import cn.kk.kkdict.FormattedTreeSet;
import cn.kk.kkdict.Helper;
import cn.kk.kkdict.Word;

/**
 * Depends on {@link WikiPagesMetaCurrentChineseExtractor} and {@link WikiPagesMetaCurrentGermanExtractor}
 * 
 * @author x_kez
 * 
 */
public class WikiPagesMetaCurrentEnglishExtractor {
    private static final String LNG_EN = "en";

    public static final String WIKI_PAGES_META_CURRENT_XML_FILE = "X:\\kkdict\\dicts\\wiki\\enwiki-20120211-pages-meta-current.xml";

    public static final Map<String, String> ENGLISH_ONLY_INPUT_FILES = new FormattedTreeMap<String, String>();
    static {
        ENGLISH_ONLY_INPUT_FILES.put("X:\\kkdict\\out\\wiki\\wiki_de\\english-only.txt", "de");
        ENGLISH_ONLY_INPUT_FILES.put("X:\\kkdict\\out\\wiki\\wiki_zh\\english-only.txt", "zh");
    }

    public static final String OUT_DIR = "X:\\kkdict\\out\\wiki\\wiki_en";

    public static final String[] RELEVANT_LANGUAGES = { "zh", "de", "ru", "ja", "ko", "fr", "it", "es", "la" };

    public static final String[] IRRELEVANT_PREFIX = { "Diskussion:", "Benutzer:", "Hilfe:", "Kategorie:", "Vorlage:",
            "Vorlage Diskussion:", "Datei:", "Benutzer Diskussion:", "Hilfe Diskussion:", "Liste der ",
            "Wikipedia Diskussion:", "Template:", "Template talk:", "Category:", "Help talk:", "File:",
            "Wikipedia talk:", "User:", "Help:", "Talk:", "User talk:", "Wikipedia:", "Portal:" };

    public static void main(String args[]) throws IOException {
        Helper.precheck(WIKI_PAGES_META_CURRENT_XML_FILE, OUT_DIR);
        Map<String, Word> words = new FormattedTreeMap<String, Word>();
        BufferedWriter skippedEnglishOnlyNotValidWriter = new BufferedWriter(new FileWriter(OUT_DIR + File.separator
                + "skipped_english-only_not-valid.txt"), 8192000);

        int statSkippedGlobal = 0;
        int statOkGlobal = 0;
        final Set<String> keySet = ENGLISH_ONLY_INPUT_FILES.keySet();
        for (String f : keySet) {
            String lng = ENGLISH_ONLY_INPUT_FILES.get(f);
            BufferedReader reader = new BufferedReader(new FileReader(f), 8192000);
            String line;
            int statSkipped = 0;
            int statOk = 0;
            while ((line = reader.readLine()) != null) {
                Word w = Helper.readWikiWord(line);
                if (w != null) {
                    Map<String, String> translations = w.getTranslations();
                    String lngDef = w.getName();
                    Helper.changeWordLanguage(w, LNG_EN, translations);
                    words.put(lngDef, w);
                    statOk++;
                } else {
                    skippedEnglishOnlyNotValidWriter.write(line);
                    skippedEnglishOnlyNotValidWriter.write(Helper.SEP_NEWLINE);
                    statSkipped++;
                }
            }
            reader.close();
            System.out.println("Read Language '" + lng + "' (" + f + "): ok=" + statOk + ", skipped=" + statSkipped);
            statOkGlobal += statOk;
            statSkippedGlobal += statSkipped;
        }
        System.out.println("Total Read: total=" + words.size() + ", ok=" + statOkGlobal + ", skipped="
                + statSkippedGlobal);
        // System.out.println(words);
        skippedEnglishOnlyNotValidWriter.close();

        extractWikipediaPagesMetaCurrent(words);

    }

    private static void extractWikipediaPagesMetaCurrent(Map<String, Word> words) throws FileNotFoundException,
            IOException {
        long timeStarted = System.currentTimeMillis();
        Helper.precheck(WIKI_PAGES_META_CURRENT_XML_FILE, OUT_DIR);
        BufferedReader reader = new BufferedReader(new FileReader(WIKI_PAGES_META_CURRENT_XML_FILE), 8192000);
        BufferedWriter skippedNoTranslationWriter = new BufferedWriter(new FileWriter(OUT_DIR + File.separator
                + "skipped_no-translation.txt"), 8192000);
        BufferedWriter skippedIrrelevantWriter = new BufferedWriter(new FileWriter(OUT_DIR + File.separator
                + "skipped_irrelevant.txt"), 8192000);
        BufferedWriter skippedNoNeededTranslationsWriter = new BufferedWriter(new FileWriter(OUT_DIR + File.separator
                + "skipped_no-de-zh-translations.txt"), 8192000);
        BufferedWriter writer = new BufferedWriter(new FileWriter(OUT_DIR + File.separator + "output.txt"), 8192000);
        BufferedWriter germanOnlyWriter = new BufferedWriter(new FileWriter(OUT_DIR + File.separator
                + "german-only.txt"), 8192000);

        String line;
        String name = null;
        Set<String> globalCategories = new FormattedTreeSet<String>();
        Set<String> categories = null;
        Map<String, String> languages = null;
        String tmp;

        int statSkipped = 0;
        int statOk = 0;
        while ((line = reader.readLine()) != null) {
            if (Helper.isNotEmptyOrNull(tmp = Helper.substringBetween(line, "<title>", "</title>"))) {
                if (write(skippedNoTranslationWriter, skippedIrrelevantWriter, writer, name, categories, languages,
                        skippedNoNeededTranslationsWriter, germanOnlyWriter, words)) {
                    statOk++;
                } else {
                    statSkipped++;
                }
                name = tmp;
                categories = new FormattedTreeSet<String>();
                languages = new FormattedTreeMap<String, String>();
            } else if (Helper.isNotEmptyOrNull(tmp = Helper.substringBetween(line, "[[Category:", "]]"))) {
                tmp = tmp.trim();
                int wildcardIdx = tmp.indexOf('|');
                if (wildcardIdx != -1) {
                    tmp = tmp.substring(0, wildcardIdx);
                }
                categories.add(tmp);
                globalCategories.add(tmp);
            } else if (Helper.isNotEmptyOrNull(tmp = Helper.substringBetween(line, "[[", "]]"))) {
                for (String lng : RELEVANT_LANGUAGES) {
                    if ((tmp = Helper.substringBetween(line, "[[" + lng + ":", "]]")) != null) {
                        languages.put(lng, tmp);
                        break;
                    }
                }
            }
        }
        if (write(skippedNoTranslationWriter, skippedIrrelevantWriter, writer, name, categories, languages,
                skippedNoNeededTranslationsWriter, germanOnlyWriter, words)) {
            statOk++;
        } else {
            statSkipped++;
        }
        reader.close();
        writer.close();
        skippedIrrelevantWriter.close();
        skippedNoTranslationWriter.close();
        skippedNoNeededTranslationsWriter.close();
        germanOnlyWriter.close();

        BufferedWriter categoriesWriter = new BufferedWriter(new FileWriter(OUT_DIR + File.separator
                + "output-categories.txt"), 8192000);
        for (String c : globalCategories) {
            categoriesWriter.write(c);
            categoriesWriter.write(Helper.SEP_NEWLINE);
        }
        categoriesWriter.close();
        System.out.println("\n==============\nExtract Wiki Duration: "
                + Helper.formatDuration(System.currentTimeMillis() - timeStarted));
        System.out.println("Categories: " + globalCategories.size());
        System.out.println("OK: " + statOk);
        System.out.println("SKIPPED: " + statSkipped + "\n==============\n");
    }

    private static boolean write(BufferedWriter skippedNoTranslationWriter, BufferedWriter skippedIrrelevantWriter,
            BufferedWriter writer, String name, Set<String> categories, Map<String, String> languages,
            BufferedWriter skippedNoNeededTranslationsWriter, BufferedWriter germanOnlyWriter, Map<String, Word> words)
            throws IOException {
        boolean ok = false;
        if (Helper.isNotEmptyOrNull(name)) {
            boolean relevant = true;
            for (String prefix : IRRELEVANT_PREFIX) {
                if (name.startsWith(prefix)) {
                    relevant = false;
                    break;
                }
            }
            if (!relevant) {
                // System.err.println("skipped (irrelevant): " + name);
                skippedIrrelevantWriter.write(name);
                skippedIrrelevantWriter.write(Helper.SEP_NEWLINE);
            } else {
                Word w = words.get(name);
                if (w != null) {
                    Map<String, String> lngs = w.getTranslations();
                    if (!lngs.isEmpty()) {
                        lngs.putAll(languages);
                        languages = lngs;
                    }
                    Set<String> cats = w.getCategories();
                    if (!cats.isEmpty()) {
                        cats.addAll(categories);
                        categories = cats;
                    }
                }
                if (languages.isEmpty()) {
                    skippedNoTranslationWriter.write(name);
                    skippedNoTranslationWriter.write(Helper.SEP_NEWLINE);
                } else if (languages.containsKey("zh")) {
                    writer.write(name + Helper.SEP_PARTS + languages + Helper.SEP_PARTS + categories + Helper.SEP_NEWLINE);
                    ok = true;
                } else if (languages.containsKey("de")) {
                    germanOnlyWriter.write(name + Helper.SEP_PARTS + languages + Helper.SEP_PARTS + categories
                            + Helper.SEP_NEWLINE);
                    ok = true;
                } else {
                    skippedNoNeededTranslationsWriter.write(name + Helper.SEP_PARTS + languages + Helper.SEP_PARTS
                            + categories + Helper.SEP_NEWLINE);
                }
            }

        }
        return ok;
    }
}