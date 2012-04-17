package cn.kk.kkdict.beans;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeMap;

import cn.kk.kkdict.utils.Helper;

public class FormattedTreeMap<K, V> extends TreeMap<K, V> {
    private static final long serialVersionUID = -5668968019957553436L;

    @Override
    public String toString() {
        Iterator<Entry<K, V>> i = entrySet().iterator();
        if (!i.hasNext())
            return Helper.EMPTY_STRING;

        StringBuilder sb = new StringBuilder();
        for (;;) {
            Entry<K, V> e = i.next();
            K key = e.getKey();
            V value = e.getValue();
            sb.append(key == this ? Helper.EMPTY_STRING : key);
            sb.append(Helper.SEP_DEFINITION);
            sb.append(value == this ? Helper.EMPTY_STRING : value);
            if (!i.hasNext())
                return sb.toString();
            sb.append(Helper.SEP_LIST);
        }
    }
}
