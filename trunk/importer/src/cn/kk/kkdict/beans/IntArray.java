package cn.kk.kkdict.beans;

import java.util.Arrays;

public class IntArray implements Comparable<IntArray> {
    private int[] data;


    public IntArray() {
    }

    public IntArray(int[] data) {
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
        IntArray other = (IntArray) obj;
        if (!Arrays.equals(data, other.data))
            return false;
        return true;
    }

    @Override
    public int compareTo(IntArray o) {
        return Arrays.hashCode(data) - Arrays.hashCode(o.data);
    }

    public int[] getData() {
        return data;
    }

    public IntArray setData(int[] data) {
        this.data = data;
        return this;
    }
}
