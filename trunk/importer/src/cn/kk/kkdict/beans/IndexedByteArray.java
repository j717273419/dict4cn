package cn.kk.kkdict.beans;

import java.util.Arrays;


public class IndexedByteArray implements Comparable<IndexedByteArray> {
    private byte[] data;
    private int idx;
    private int weight;


    public IndexedByteArray() {
    }

    public IndexedByteArray(int idx, byte[] data) {
        this.idx = idx;
        this.data = data;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(data);
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
        IndexedByteArray other = (IndexedByteArray) obj;
        if (!Arrays.equals(data, other.data))
            return false;
        return true;
    }

    @Override
    public int compareTo(IndexedByteArray o) {
        return Arrays.hashCode(data) - Arrays.hashCode(o.data);
    }

    public byte[] getData() {
        return data;
    }

    public IndexedByteArray setData(byte[] data) {
        this.data = data;
        return this;
    }

    public int getIdx() {
        return idx;
    }

    public void setIdx(int idx) {
        this.idx = idx;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }
}
