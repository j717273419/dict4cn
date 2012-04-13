package cn.kk.kkdict.beans;

import java.util.Map;

import cn.kk.kkdict.types.Language;
import cn.kk.kkdict.utils.Helper;

public class TranslationInfo implements Comparable<TranslationInfo> {
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TranslationInfo other = (TranslationInfo) obj;
        if (key == null) {
            if (other.key != null)
                return false;
        } else if (!key.equals(other.key))
            return false;
        return true;
    }

    public TranslationInfo() {
    }

    public String key = Helper.EMPTY_STRING;
    public String family = Helper.EMPTY_STRING;
    public String iso1 = Helper.EMPTY_STRING;
    public String iso2 = Helper.EMPTY_STRING;
    public String iso3 = Helper.EMPTY_STRING;
    public Map<String, String> lngMap = new FormattedTreeMap<String, String>();

    public String direction = Helper.EMPTY_STRING;
    public String original = Helper.EMPTY_STRING;
    public String comment = Helper.EMPTY_STRING;

    public void invalidate() {
        key = Helper.EMPTY_STRING;
        iso1 = Helper.EMPTY_STRING;
        iso2 = Helper.EMPTY_STRING;
        iso3 = Helper.EMPTY_STRING;
    }

    public boolean isValid() {
        if (key.isEmpty()) {
            if (Helper.isNotEmptyOrNull(iso1)) {
                key = iso1;
            } else if (Helper.isNotEmptyOrNull(iso2)) {
                key = iso2;
            } else if (Helper.isNotEmptyOrNull(iso3)) {
                key = iso3;
            }
        }
        return Helper.isNotEmptyOrNull(key);
    }

    @Override
    public int compareTo(TranslationInfo o) {
        return key.compareTo(o.key);
    }

    public void put(Language lng, String value) {
        this.lngMap.put(lng.key, value);
    }

    public String get(Language lng) {
        return this.lngMap.get(lng.key);
    }

}
