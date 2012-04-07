package cn.kk.kkdict.beans;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Set;

import cn.kk.kkdict.utils.Helper;

public class WiktContentState {

    private ByteBuffer line;
    private String name;
    private String sourceLanguage;
    private String targetLanguage;
    private String sourceWordType;
    private String sourceGender;
    private String targetWordType;
    private String targetGender;
    private Map<String, String> languages;
    private Set<String> categories;
    private boolean translationContent;
    private final String fileLanguage;

    public WiktContentState(String lng) {
        this.fileLanguage = lng;
        languages = new FormattedTreeMap<String, String>();
        categories = new FormattedTreeSet<String>();
        invalidate();
    }

    public String getSourceLanguage() {
        return sourceLanguage;
    }

    public void setSourceLanguage(String sourceLanguage) {
        this.sourceLanguage = sourceLanguage;
    }
    
    public void putTranslation(String key, String val) {
        String v1 = null;
        if ((v1 = languages.get(key)) != null) {
            this.languages.put(key, v1 + Helper.SEP_SAME_MEANING + val);
        } else {
            this.languages.put(key, val);
        }
    }
    
    private void clear() {
        languages.clear();
        sourceWordType = Helper.EMPTY_STRING;
        sourceGender = Helper.EMPTY_STRING;
        targetWordType = Helper.EMPTY_STRING;
        targetGender = Helper.EMPTY_STRING;
    }

    public String getName() {
        return name;
    }

    public boolean hasTranslations() {
        if (languages.isEmpty()) {
            return false;
        } else if (languages.size() > 1) {
            return true;
        } else {
            return languages.get(sourceLanguage) == null;
        }
    }

    public String getTranslations() {
        return this.languages.toString();
    }

    public void invalidate() {
        this.name = null;
        sourceLanguage = null;
        targetLanguage = null;

        clear();
    }

    public void init(String n) {
        this.name = n;
        clear();
    }

    public boolean isValid() {
        return name != null;
    }

    public String getTargetLanguage() {
        return targetLanguage;
    }

    public void setTargetLanguage(String targetLanguage) {
        this.targetLanguage = targetLanguage;
    }

    public boolean isTranslationContent() {
        return translationContent;
    }

    public void setTranslationContent(boolean translationContent) {
        this.translationContent = translationContent;
        if (translationContent) {
            setSourceGender(Helper.EMPTY_STRING);
        }
    }

    public String getSourceWordType() {
        return sourceWordType;
    }

    public void setSourceWordType(String sourceWordType) {
        this.sourceWordType = sourceWordType;
    }

    public String getSourceGender() {
        return sourceGender;
    }

    public void setSourceGender(String sourceGender) {
        this.sourceGender = sourceGender;
    }

    public String getTargetWordType() {
        return targetWordType;
    }

    public void setTargetWordType(String targetWordType) {
        this.targetWordType = targetWordType;
    }

    public String getTargetGender() {
        return targetGender;
    }

    public void setTargetGender(String targetGender) {
        this.targetGender = targetGender;
    }

    public void clearTargetAttributes() {
        this.targetGender = Helper.EMPTY_STRING;
        this.targetWordType = Helper.EMPTY_STRING;
    }

    public boolean hasTargetGender() {
        return !this.targetGender.isEmpty();
    }

    public boolean hasTargetWordType() {
        return !this.targetWordType.isEmpty();
    }

    public void clearSourceAttributes() {
        this.sourceGender = Helper.EMPTY_STRING;
        this.sourceWordType = Helper.EMPTY_STRING;
    }

    public String getFileLanguage() {
        return fileLanguage;
    }

    public Set<String> getCategories() {
        return categories;
    }

    public void setCategories(Set<String> categories) {
        this.categories = categories;
    }

    public void addCategory(String category) {
        this.categories.add(category);
    }

    public boolean hasCategories() {
        return !this.categories.isEmpty();
    }

    public boolean hasSourceGender() {
        return !this.sourceGender.isEmpty();
    }

    public boolean hasSourceWordType() {
        return !this.sourceWordType.isEmpty();
    }

    public Map<String, String> getLanguages() {
        return languages;
    }

    public String getTranslation(String targetLng) {
        return languages.get(targetLng);
    }

    public void setTranslation(String targetLng, String trans) {
        languages.put(targetLng, trans);
    }

    public ByteBuffer getLine() {
        return line;
    }

    public void setLine(ByteBuffer line) {
        this.line = line;
    }
}
