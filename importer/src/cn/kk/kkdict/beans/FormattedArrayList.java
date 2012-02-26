package cn.kk.kkdict.beans;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cn.kk.kkdict.utils.Helper;

public class FormattedArrayList<E> extends ArrayList<E> {
    private static final long serialVersionUID = 1414626422972498763L;

    public FormattedArrayList(List<E> values) {
        super(values);
    }

    public FormattedArrayList() {
        super();
    }

    public FormattedArrayList(int size) {
        super(size);
    }

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
