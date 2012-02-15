package cn.kk.kkdict;

import java.util.Iterator;
import java.util.TreeSet;

public class FormattedTreeSet<E> extends TreeSet<E> {
    private static final long serialVersionUID = -8035295407619357235L;

    @Override
    public String toString() {
        Iterator<E> i = iterator();
        if (!i.hasNext())
            return Helper.EMPTY_STRING;

        StringBuilder sb = new StringBuilder();
        for (;;) {
            E e = i.next();
            sb.append(e == this ? Helper.EMPTY_STRING : e);
            if (!i.hasNext())
                return sb.toString();
            sb.append(Helper.SEP_LIST);
        }
    }

}
