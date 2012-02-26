package cn.kk.kkdict.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

public final class ChineseHelper {
    private static int[] CODEPOINTS_ST_SIMPLE;
    private static int[] CODEPOINTS_ST_TRADITIONAL;

    private static int[] CODEPOINTS_TS_SIMPLE;
    private static int[] CODEPOINTS_TS_TRADITIONAL;


    /**
     * Converts input text to traditional chinese text
     * 
     * @param input
     *            simplified chinese text
     * @return traditional chinese text
     */
    public static String toTraditionalChinese(final String input) {
        if (CODEPOINTS_ST_SIMPLE == null) {
            createSimpleTraditionalMap();
        }
        StringBuilder sb = new StringBuilder();

        int idx;
        for (int i = 0; i < input.length(); i++) {
            int codePoint = input.codePointAt(i);
            if ((idx = Arrays.binarySearch(CODEPOINTS_ST_SIMPLE, codePoint)) >= 0) {
                sb.append(Character.toChars(CODEPOINTS_ST_TRADITIONAL[idx]));
            } else {
                sb.append(Character.toChars(codePoint));
            }
        }
        return sb.toString();
    }

    /**
     * Converts input text to simplified chinese text
     * 
     * @param input
     *            traditional chinese text
     * @return simplified chinese text
     */
    public static String toSimplifiedChinese(final String input) {
        if (CODEPOINTS_TS_SIMPLE == null) {
            createTraditionalSimpleMap();
        }
        StringBuilder sb = new StringBuilder();

        int idx;
        for (int i = 0; i < input.length(); i++) {
            int codePoint = input.codePointAt(i);
            if ((idx = Arrays.binarySearch(CODEPOINTS_TS_SIMPLE, codePoint)) >= 0) {
                sb.append(Character.toChars(CODEPOINTS_TS_TRADITIONAL[idx]));
            } else {
                sb.append(Character.toChars(codePoint));
            }
        }
        return sb.toString();
    }

    private static void createTraditionalSimpleMap() {
        final String file = "traditional2simple.txt";
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(PinyinHelper.class.getResourceAsStream("/"
                    + file)));
            String line;
            while (null != (line = reader.readLine())) {
                String[] parts = line.split(Helper.SEP_PARTS);
                if (parts.length == 2) {
                    String map = parts[1];
                    int length = map.length();
                    if (parts[0].equals("sortedSimple")) {
                        CODEPOINTS_TS_SIMPLE = new int[length];
                        for (int i = 0; i < length; i++) {
                            CODEPOINTS_TS_SIMPLE[i] = map.codePointAt(i);
                        }
                    } else if (parts[0].equals("sortedTraditional")) {
                        CODEPOINTS_TS_TRADITIONAL = new int[length];
                        for (int i = 0; i < length; i++) {
                            CODEPOINTS_TS_TRADITIONAL[i] = map.codePointAt(i);
                        }
                    }
                }
            }
            reader.close();
        } catch (IOException e) {
            System.out.println("Failed to load " + file + "!");
        }
    }

    private static void createSimpleTraditionalMap() {
        final String file = "simple2traditional.txt";
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(PinyinHelper.class.getResourceAsStream("/"
                    + file)));
            String line;
            while (null != (line = reader.readLine())) {
                String[] parts = line.split(Helper.SEP_PARTS);
                if (parts.length == 2) {
                    String map = parts[1];
                    int length = map.length();
                    if (parts[0].equals("sortedSimple")) {
                        CODEPOINTS_ST_SIMPLE = new int[length];
                        for (int i = 0; i < length; i++) {
                            CODEPOINTS_ST_SIMPLE[i] = map.codePointAt(i);
                        }
                    } else if (parts[0].equals("sortedTraditional")) {
                        CODEPOINTS_ST_TRADITIONAL = new int[length];
                        for (int i = 0; i < length; i++) {
                            CODEPOINTS_ST_TRADITIONAL[i] = map.codePointAt(i);
                        }
                    }
                }
            }
            reader.close();
        } catch (IOException e) {
            System.out.println("Failed to load " + file + "!");
        }
    }

    protected static void printSortConverterMap() {
        final String file = "traditional2simple.txt";
        try {
            int[] simple = null;
            int[] traditional = null;
            BufferedReader reader = new BufferedReader(new InputStreamReader(PinyinHelper.class.getResourceAsStream("/"
                    + file)));
            String line;
            while (null != (line = reader.readLine())) {
                String[] parts = line.split(Helper.SEP_PARTS);
                if (parts.length == 2) {
                    String map = parts[1];
                    int length = map.length();
                    if (parts[0].equals("simple")) {
                        simple = new int[length];
                        for (int i = 0; i < length; i++) {
                            simple[i] = map.codePointAt(i);
                        }
                    } else if (parts[0].equals("traditional")) {
                        traditional = new int[length];
                        for (int i = 0; i < length; i++) {
                            traditional[i] = map.codePointAt(i);
                        }
                    }
                }
            }
            reader.close();

            CODEPOINTS_ST_SIMPLE = Arrays.copyOf(simple, simple.length);
            CODEPOINTS_ST_TRADITIONAL = new int[simple.length];
            Arrays.sort(CODEPOINTS_ST_SIMPLE);

            for (int i = 0; i < simple.length; i++) {
                for (int j = 0; j < simple.length; j++) {
                    if (CODEPOINTS_ST_SIMPLE[i] == simple[j]) {
                        CODEPOINTS_ST_TRADITIONAL[i] = traditional[j];
                    }
                }
            }

            System.out.print("sortedTraditional" + Helper.SEP_PARTS);
            for (int i : CODEPOINTS_ST_TRADITIONAL) {
                System.out.print(Character.toChars(i));
            }
            System.out.println();
            System.out.print("sortedSimple" + Helper.SEP_PARTS);
            for (int i : CODEPOINTS_ST_SIMPLE) {
                System.out.print(Character.toChars(i));
            }
            System.out.println();

            CODEPOINTS_TS_TRADITIONAL = Arrays.copyOf(traditional, traditional.length);
            CODEPOINTS_TS_SIMPLE = new int[traditional.length];
            Arrays.sort(CODEPOINTS_TS_TRADITIONAL);

            for (int i = 0; i < traditional.length; i++) {
                for (int j = 0; j < traditional.length; j++) {
                    if (CODEPOINTS_TS_TRADITIONAL[i] == traditional[j]) {
                        CODEPOINTS_TS_SIMPLE[i] = simple[j];
                    }
                }
            }

            System.out.print("sortedTraditional" + Helper.SEP_PARTS);
            for (int i : CODEPOINTS_TS_TRADITIONAL) {
                System.out.print(Character.toChars(i));
            }
            System.out.println();
            System.out.print("sortedSimple" + Helper.SEP_PARTS);
            for (int i : CODEPOINTS_TS_SIMPLE) {
                System.out.print(Character.toChars(i));
            }
            System.out.println();

        } catch (IOException e) {
            System.out.println("Failed to load " + file + "!");
        }
    }

}
