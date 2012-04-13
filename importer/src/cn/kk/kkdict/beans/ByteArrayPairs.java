package cn.kk.kkdict.beans;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.kk.kkdict.utils.ArrayHelper;

/**
 * values must be distinct
 */
public class ByteArrayPairs {
    private final Map<ByteArray, byte[]> keysIdent;
    private final byte[][] keys;
    private final byte[][] values;
    private boolean sorted;

    public ByteArrayPairs(final int length) {
        this.keys = new byte[length][];
        this.values = new byte[length][];
        this.keysIdent = new HashMap<ByteArray, byte[]>(length);
    }

    public ByteArrayPairs put(final int i, final byte[] k, byte[] v) {
        byte[] key;
        ByteArray keyObj = new ByteArray(k);
        if (null == (key = keysIdent.get(keyObj))) {
            key = k;
            keysIdent.put(keyObj, key);
        }
        this.keys[i] = key;
        this.values[i] = v;
        return this;
    }

    public ByteArrayPairs sort() {
        Map<ByteArray, byte[]> entries = new HashMap<ByteArray, byte[]>(keys.length);
        for (int i = 0; i < keys.length; i++) {
            entries.put(new ByteArray(values[i]), keys[i]);
        }
        if (entries.size() != keys.length) {
            System.err.println("找到重叠名称。排序失败！");
            return this;
        }
        List<ByteArray> vs = new ArrayList<ByteArray>(entries.keySet());
        Collections.sort(vs);
        for (int i = 0; i < keys.length; i++) {
            ByteArray v = vs.get(i);
            values[i] = v.getData();
            keys[i] = entries.get(v);
        }
        sorted = true;
        return this;
    }

    public boolean isSorted() {
        return sorted;
    }

    public final byte[] findKey(final ByteBuffer value) {
        final int offset = ArrayHelper.findTrimmedOffset(value);
        final int len = ArrayHelper.findTrimmedEndIdx(value) - offset;
        return findKey(ArrayHelper.toBytes(value, offset, len));
    }

    public final byte[] findKey(final byte[] value) {
        int idx = Arrays.binarySearch(values, value, ArrayHelper.COMPARATOR_BYTE_ARRAY);
        if (idx >= 0) {
            return keys[idx];
        } else {
            return null;
        }
    }

    public final byte[] containsKey(final ByteBuffer key) {
        final int offset = ArrayHelper.findTrimmedOffset(key);
        final int len = ArrayHelper.findTrimmedEndIdx(key) - offset;
        return containsKey(ArrayHelper.toBytes(key, offset, len));
    }

    public final byte[] containsKey(final byte[] key) {
        return this.keysIdent.get(new ByteArray(key));
    }

}
