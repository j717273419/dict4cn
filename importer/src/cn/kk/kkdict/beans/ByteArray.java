package cn.kk.kkdict.beans;

import java.util.Arrays;

public class ByteArray implements Comparable<ByteArray> {
    private byte[] data;


    public ByteArray() {
    }

    public ByteArray(byte[] data) {
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
        ByteArray other = (ByteArray) obj;
        if (!Arrays.equals(data, other.data))
            return false;
        return true;
    }

    @Override
    public int compareTo(ByteArray o) {
        return Arrays.hashCode(data) - Arrays.hashCode(o.data);
    }

    public byte[] getData() {
        return data;
    }

    public ByteArray setData(byte[] data) {
        this.data = data;
        return this;
    }
}
