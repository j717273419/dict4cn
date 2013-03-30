package cn.kk.dict2go;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import cn.kk.dict2go.lib.Language;
import cn.kk.dict2go.lib.SearchEngine;

public class Configuration {
  private static final String        CFG_FILE                = "cfg.properties";

  private static final String        KEY_LNGS                = "languages";

  private static final String        KEY_WAIT_CHECK_RESULT   = "wait_check_result_ms";

  private static final String        KEY_WAIT_AUTOCOMPLETE   = "wait_autocomplete_ms";

  private static final String        KEY_MAX_INPUT_HISTORIES = "max_input_histories";

  private static List<String>        lngsSelected            = new ArrayList<>();
  private static int[]               lngsSelectedIds         = new int[0];

  private static int                 waitStartAutocomplete   = 200;

  private static int                 waitCheckResult         = 300;

  private static int                 maxInputHistories       = 20;

  private static Properties          cfg                     = new Properties();

  /**
   * iso-lng-key: lng display name
   */
  private static Map<String, String> lngNamesMapping         = new HashMap<>();

  public static Properties load() {
    final Properties lngsCfg = new Properties();
    try (final Reader r = new InputStreamReader(Configuration.class.getResourceAsStream("/lngs.properties"), "UTF-8");) {
      lngsCfg.load(r);
      final Set<Entry<Object, Object>> entries = lngsCfg.entrySet();
      for (final Entry<Object, Object> e : entries) {
        final String key = (String) e.getKey();
        final String val = (String) e.getValue();
        if ((key != null) && (val != null)) {
          Configuration.lngNamesMapping.put(key.trim().toLowerCase(), val);
        }
      }
    } catch (final Exception e) {
      System.err.println("Failed to load languages file 'lngs.properties': " + e.toString());
    }

    final File cfgFile = new File(Configuration.CFG_FILE);
    if (cfgFile.exists()) {
      try (final Reader r = new InputStreamReader(new FileInputStream(cfgFile), "UTF-8");) {
        Configuration.cfg.load(r);
        Configuration.setSelectedLanguages(Configuration.cfg.getProperty(Configuration.KEY_LNGS));
        Configuration.setWaitCheckTasks(Configuration.cfg.getProperty(Configuration.KEY_WAIT_CHECK_RESULT));
        Configuration.setWaitStartAutocomplete(Configuration.cfg.getProperty(Configuration.KEY_WAIT_AUTOCOMPLETE));
        Configuration.setMaxInputHistories(Configuration.cfg.getProperty(Configuration.KEY_MAX_INPUT_HISTORIES));
      } catch (final Exception e) {
        System.err.println("Failed to load configurations: " + e.toString());
      }
    }
    return Configuration.cfg;
  }

  public static Properties save() {
    final File cfgFile = new File(Configuration.CFG_FILE);
    try (final Writer w = new OutputStreamWriter(new FileOutputStream(cfgFile), "UTF-8");) {
      Configuration.cfg.setProperty(Configuration.KEY_LNGS, Configuration.getSelectedLanguagesAsString());
      Configuration.cfg.setProperty(Configuration.KEY_MAX_INPUT_HISTORIES, String.valueOf(Configuration.getMaxInputHistories()));
      Configuration.cfg.setProperty(Configuration.KEY_WAIT_CHECK_RESULT, String.valueOf(Configuration.getWaitCheckResult()));
      Configuration.cfg.setProperty(Configuration.KEY_WAIT_AUTOCOMPLETE, String.valueOf(Configuration.getWaitStartAutocomplete()));
      Configuration.cfg.store(w, Configuration.CFG_FILE + ": " + new Date());
    } catch (final Exception e) {
      System.err.println("Failed to save configurations: " + e.toString());
      e.printStackTrace();
    }
    return Configuration.cfg;
  }

  static {
    Configuration.load();
  }

  public static int getWaitStartAutocomplete() {
    return Configuration.waitStartAutocomplete;
  }

  public static void setWaitStartAutocomplete(final String val) {
    if (val != null) {
      try {
        Configuration.waitStartAutocomplete = Integer.parseInt(val);
      } catch (final Exception e) {
        System.err.println("Failed to set " + Configuration.KEY_WAIT_AUTOCOMPLETE + ": " + val);
      }
    }
  }

  public static int getMaxInputHistories() {
    return Configuration.maxInputHistories;
  }

  public static void setMaxInputHistories(final String val) {
    if (val != null) {
      try {
        Configuration.maxInputHistories = Integer.parseInt(val);
      } catch (final Exception e) {
        System.err.println("Failed to set " + Configuration.KEY_MAX_INPUT_HISTORIES + ": " + val);
      }
    }
  }

  public static List<String> getSelectedLanguages() {
    if (Configuration.lngsSelected.isEmpty()) {
      return new ArrayList<>(Configuration.lngNamesMapping.keySet());
    } else {
      final List<String> selected = new ArrayList<>(Configuration.lngsSelected);
      selected.retainAll(Configuration.lngNamesMapping.keySet());
      return selected;
    }
  }

  public static String getSelectedLanguagesAsString() {
    final StringBuilder sb = new StringBuilder();
    boolean first = true;
    for (final String lng : Configuration.lngsSelected) {
      if (lng != null) {
        if (first) {
          first = false;
        } else {
          sb.append(", ");
        }
        sb.append(lng.trim().toLowerCase());
      }
    }
    return sb.toString();
  }

  public static void setSelectedLanguages(final String val) {
    if (val != null) {
      try {
        Configuration.lngsSelected.clear();
        final StringTokenizer st = new StringTokenizer(val, ",");
        while (st.hasMoreTokens()) {
          final String t = st.nextToken().trim().toLowerCase();
          if (!t.isEmpty()) {
            Configuration.lngsSelected.add(t);
          }
        }
        Configuration.setSelectedLanguages(new ArrayList<>(Configuration.lngsSelected));
      } catch (final Exception e) {
        System.err.println("Failed to set " + Configuration.KEY_LNGS + ": " + val);
      }
    }
  }

  public static List<String> getAvailableLanguages() {
    if (Configuration.lngsSelected.isEmpty()) {
      return new ArrayList<>();
    } else {
      final List<String> available = new ArrayList<>(Configuration.lngNamesMapping.keySet());
      available.removeAll(Configuration.lngsSelected);
      return available;
    }
  }

  public static List<String> toDisplayNames(final List<String> lngs) {
    final List<String> display = new ArrayList<>(lngs.size());
    if (!lngs.isEmpty()) {
      final int[] counts = SearchEngine.getDefinitionCounts();
      for (final String lng : lngs) {
        final String name = Configuration.lngNamesMapping.get(lng);
        if (name != null) {
          final int lngId = Language.from(lng);
          display.add(lng + ": " + name + " (" + counts[lngId - 1] + ")");
        }
      }
    }
    return display;
  }

  public static List<String> toIsoNames(final List<String> display) {
    final List<String> iso = new ArrayList<>(display.size());
    for (final String val : display) {
      final int idx = val.indexOf(':');
      if (idx != -1) {
        final String lng = val.substring(0, idx).trim().toLowerCase();
        if (Configuration.lngNamesMapping.containsKey(lng)) {
          iso.add(lng);
        }
      }
    }
    return iso;
  }

  public static void setSelectedLanguages(final List<String> selected) {
    Configuration.lngsSelected.clear();
    for (final String lng : selected) {
      if (lng != null) {
        Configuration.lngsSelected.add(lng.trim().toLowerCase());
      }
    }
    Configuration.lngsSelectedIds = SearchEngine.getLanguageIds(Configuration.lngsSelected);
  }

  public static String getBasePath() {
    final String base = Configuration.class.getResource("/icon.png").toString();
    return base.substring(0, base.length() - "icon.png".length());
  }

  public static int getWaitCheckResult() {
    return Configuration.waitCheckResult;
  }

  public static void setWaitCheckTasks(final String val) {
    if (val != null) {
      try {
        Configuration.waitCheckResult = Integer.parseInt(val);
      } catch (final Exception e) {
        System.err.println("Failed to set " + Configuration.KEY_WAIT_CHECK_RESULT + ": " + val);
      }
    }
  }

  public static int[] getSelectedLanguageIds() {
    return Configuration.lngsSelectedIds;
  }

}
