package cn.kk.kkdict.beans;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class DictRow {
    private static final Map<String, String> EMPTY_TREE_MAP = Collections.unmodifiableMap(new FormattedTreeMap<String, String>());
    private static final Set<String> EMPTY_TREE_SET = Collections.unmodifiableSet(new FormattedTreeSet<String>());
    private String name;
    private String pronounciation;
    private Set<String> categories;
    private Map<String, String> translations;

    public DictRow() {
        this.categories = EMPTY_TREE_SET;
        this.translations = EMPTY_TREE_MAP;
    }

    public DictRow(String name, Set<String> categories, Map<String, String> translations) {
        super();
        this.name = name;
        this.categories = categories;
        this.translations = translations;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<String> getCategories() {
        return categories;
    }

    public void setCategories(Set<String> categories) {
        this.categories = categories;
    }

    public Map<String, String> getTranslations() {
        return translations;
    }

    public void setTranslations(Map<String, String> translations) {
        this.translations = translations;
    }

    public String getPronounciation() {
        return pronounciation;
    }

    public void setPronounciation(String pronounciation) {
        this.pronounciation = pronounciation;
    }
}
