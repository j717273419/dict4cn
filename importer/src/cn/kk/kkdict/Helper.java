package cn.kk.kkdict;

import java.io.File;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

public final class Helper {
    public final static String EMPTY_STRING = "";
    public final static String SEP_NEWLINE = "\n";
    public final static String SEP_PARTS = "║";
    public final static String SEP_WORDS = "│";
    public final static String SEP_LIST = "▫";
    public final static String SEP_DEF = "═";
    public final static String SEP_PY = "'";

    public static final String substringBetween(String text, String start, String end) {
        int nStart = text.indexOf(start);
        int nEnd = text.indexOf(end, nStart + 1);
        if (nStart < nEnd && nStart != -1 && nEnd != -1) {
            return text.substring(nStart + start.length(), nEnd);
        } else {
            return null;
        }
    }

    public final static String padding(String text, int len, char c) {
        if (text != null && len > text.length()) {
            char[] spaces = new char[len - text.length()];
            Arrays.fill(spaces, c);
            return new String(spaces) + text;
        } else {
            return text;
        }
    }

    public final static String padding(long value, int len, char c) {
        return padding(String.valueOf(value), len, c);
    }

    public final static String formatDuration(long duration) {
        long v = Math.abs(duration);
        long days = v / 1000 / 60 / 60 / 24;
        long hours = (v / 1000 / 60 / 60) % 24;
        long mins = (v / 1000 / 60) % 60;
        long secs = (v / 1000) % 60;
        long millis = v % 1000;
        StringBuilder out = new StringBuilder();
        if (days > 0) {
            out.append(days).append(':').append(padding(hours, 2, '0')).append(':').append(padding(mins, 2, '0'))
                    .append(":").append(padding(secs, 2, '0')).append(".").append(padding(millis, 3, '0'));
        } else if (hours > 0) {
            out.append(hours).append(':').append(padding(mins, 2, '0')).append(":").append(padding(secs, 2, '0'))
                    .append(".").append(padding(millis, 3, '0'));
        } else if (mins > 0) {
            out.append(mins).append(":").append(padding(secs, 2, '0')).append(".").append(padding(millis, 3, '0'));
        } else {
            out.append(secs).append(".").append(padding(millis, 3, '0'));
        }
        return out.toString();

    }

    public final static String substringBetweenLast(String text, String start, String end) {
        int nEnd = text.lastIndexOf(end);
        int nStart = -1;
        if (nEnd > 1) {
            nStart = text.lastIndexOf(start, nEnd - 1);
        } else {
            return null;
        }
        if (nStart < nEnd && nStart != -1 && nEnd != -1) {
            return text.substring(nStart + start.length(), nEnd);
        } else {
            return null;
        }

    }

    public final static boolean isNotEmptyOrNull(String text) {
        return text != null && text.length() > 0;
    }

    public final static void precheck(String inFile, String outDirectory) {
        if (!new File(inFile).isFile()) {
            System.err.println("Could not read input file: " + inFile);
            System.exit(-100);
        }
        if (!(new File(outDirectory).isDirectory() || new File(outDirectory).mkdirs())) {
            System.err.println("Could not create output directory: " + outDirectory);
            System.exit(-101);
        }
    }

    public final static Word readWikiWord(String line) {
        if (Helper.isNotEmptyOrNull(line)) {
            String[] parts = line.split(Helper.SEP_PARTS);
            if (parts.length > 1) {
                String name = parts[0];
                if (Helper.isNotEmptyOrNull(name)) {
                    Word w = new Word();
                    w.setName(name);
                    Map<String, String> translations = readMapStringString(parts[1]);
                    w.setTranslations(translations);
                    if (parts.length > 2) {
                        Set<String> categories = readSetString(parts[2]);
                        w.setCategories(categories);
                    }
                    return w;
                }
            }
        }
        return null;
    }

    public final static Set<String> readSetString(String text) {
        String[] many = text.split(Helper.SEP_LIST);
        Set<String> set = new FormattedTreeSet<String>();
        for (String d : many) {
            set.add(d);
        }
        return set;
    }

    public final static Map<String, String> readMapStringString(String text) {
        String[] many = text.split(Helper.SEP_LIST);
        Map<String, String> map = new FormattedTreeMap<String, String>();
        for (String d : many) {
            String[] defs = d.split(Helper.SEP_DEF);
            if (defs.length == 2) {
                map.put(defs[0], defs[1]);
            }
        }
        return map;
    }

    public static final void changeWordLanguage(Word word, String currentLng, Map<String, String> translations) {
        if (translations.containsKey(currentLng)) {
            translations.put(currentLng, word.getName());
            String enDef = translations.get(currentLng);
            word.setName(enDef);
            translations.remove(currentLng);
        }
    }

    public final static Word readPinyinWord(String line) {
        if (Helper.isNotEmptyOrNull(line)) {
            String[] parts = line.split(Helper.SEP_PARTS);
            if (parts.length == 2) {
                String name = parts[0];
                String pinyin = parts[1];
                if (Helper.isNotEmptyOrNull(name) && Helper.isNotEmptyOrNull(pinyin)) {
                    Word w = new Word();
                    w.setName(name);
                    w.setPronounciation(pinyin);
                    return w;
                }
            }
        }
        return null;
    }
}
