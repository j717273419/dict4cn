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

import cn.kk.kkdict.beans.FormattedTreeMap;
import cn.kk.kkdict.beans.FormattedTreeSet;
import cn.kk.kkdict.utils.Helper;

public class WikiPagesMetaCurrentChineseExtractor {
    public static final String WIKI_PAGES_META_CURRENT_XML_FILE = "X:\\kkdict\\dicts\\wiki\\zhwiki-latest-pages-meta-current.xml";

    public static final String OUT_DIR = "X:\\kkdict\\out\\wiki\\wiki_zh";

    public static final String[] RELEVANT_LANGUAGES = { "de", "en", "ru", "ja", "ko", "fr", "it", "es", "la", "tr", "pt", "ar", "nl", "iw", "hi", "sv", "th" };

    public static final String[] IRRELEVANT_PREFIX = { "Diskussion:", "Benutzer:", "Hilfe:", "Kategorie:", "Vorlage:",
        "Vorlage Diskussion:", "Datei:", "Benutzer Diskussion:", "Hilfe Diskussion:", "Liste der ",
        "Wikipedia Diskussion:", "Template:", "Template talk:", "Category:", "Help talk:", "File:",
        "Wikipedia talk:", "User:", "Help:", "Talk:", "User talk:", "Wikipedia:", "Portal:", "MediaWiki:", "Wiktionary:",
        "Media:", "Special:", "Wiktionary talk:", "Category:", "Category talk:", "Thread:", "Thread talk:", "Summary:", "Summary talk:",
        "Appendix:", "Appendix talk:", "Concordance:", "Concordance talk:", "Index:", "Index talk:", "Rhymes:", "Rhymes talk:", "Transwiki:",
        "Transwiki talk:", "Wikisaurus:", "Wikisaurus talk:", "Citations:", "Citations talk:", "Sign gloss:", "Sign gloss talk:"};

    public static void main(String args[]) throws IOException {
        extractWikipediaPagesMetaCurrent();
    }

    private static void extractWikipediaPagesMetaCurrent() throws FileNotFoundException, IOException {
        long timeStarted = System.currentTimeMillis();
        Helper.precheck(WIKI_PAGES_META_CURRENT_XML_FILE, OUT_DIR);
        BufferedReader reader = new BufferedReader(new FileReader(WIKI_PAGES_META_CURRENT_XML_FILE), 8192000);
        BufferedWriter skippedNoTranslationWriter = new BufferedWriter(new FileWriter(OUT_DIR + File.separator
                + "skipped_no-translation.txt"), 8192000);
        BufferedWriter skippedIrrelevantWriter = new BufferedWriter(new FileWriter(OUT_DIR + File.separator
                + "skipped_irrelevant.txt"), 8192000);
        BufferedWriter skippedNoNeededTranslationsWriter = new BufferedWriter(new FileWriter(OUT_DIR + File.separator
                + "skipped_no-de-en-translations.txt"), 8192000);
        BufferedWriter englishOnlyWriter = new BufferedWriter(new FileWriter(OUT_DIR + File.separator
                + "english-only.txt"), 8192000);
        BufferedWriter writer = new BufferedWriter(new FileWriter(OUT_DIR + File.separator + "output.txt"), 8192000);

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
                        skippedNoNeededTranslationsWriter, englishOnlyWriter)) {
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
                skippedNoNeededTranslationsWriter, englishOnlyWriter)) {
            statOk++;
        } else {
            statSkipped++;
        }
        reader.close();
        writer.close();
        skippedIrrelevantWriter.close();
        skippedNoTranslationWriter.close();
        skippedNoNeededTranslationsWriter.close();
        englishOnlyWriter.close();

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
            BufferedWriter skippedNoNeededTranslationsWriter, BufferedWriter englishOnlyWriter) throws IOException {
        boolean ok = false;
        if (name != null) {
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
            } else if (languages.isEmpty()) {
                skippedNoTranslationWriter.write(name);
                skippedNoTranslationWriter.write(Helper.SEP_NEWLINE);
            } else if (languages.containsKey("de")) {
                writer.write(name + Helper.SEP_PARTS + languages + Helper.SEP_PARTS + categories + Helper.SEP_NEWLINE);
                ok = true;
            } else if (languages.containsKey("en")) {
                englishOnlyWriter.write(name + Helper.SEP_PARTS + languages + Helper.SEP_PARTS + categories
                        + Helper.SEP_NEWLINE);
            } else {
                skippedNoNeededTranslationsWriter.write(name + Helper.SEP_PARTS + languages + Helper.SEP_PARTS
                        + categories + Helper.SEP_NEWLINE);
            }

        }
        return ok;
    }

}
