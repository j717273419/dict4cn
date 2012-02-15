package cn.kk.kkdict;

public class Stat implements Comparable<Stat> {
    public Stat(Integer c, String k) {
        this.key = k;
        this.counter = c;
    }

    public String key;

    public Integer counter;

    @Override
    public String toString() {
        return key + "=" + counter;
    }

    @Override
    public int compareTo(Stat o) {
        return counter.compareTo(o.counter);
    }
}
