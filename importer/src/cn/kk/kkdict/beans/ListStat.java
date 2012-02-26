package cn.kk.kkdict.beans;

import java.util.List;

public class ListStat implements Comparable<ListStat> {

    @Override
    public String toString() {
        return "[counter=" + counter + ", " + (values != null ? "values=" + values : "") + "]";
    }

    public void add(ListStat s) {
        this.counter += s.counter;
        this.values.addAll(s.values);
    }

    private int counter;
    private List<String> values;

    public ListStat(int counter, String firstVal) {
        this.counter = counter;
        this.values = new FormattedArrayList<String>();
        this.values.add(firstVal);
    }

    public ListStat(int counter, List<String> values) {
        this.counter = counter;
        this.values = values;
    }

    @Override
    public int compareTo(ListStat o) {
        // changed to descending order for performance optimization
        return o.counter - this.counter;
    }

    /**
     * @return the counter
     */
    public int getCounter() {
        return counter;
    }

    /**
     * @param counter
     *            the counter to set
     */
    public void setCounter(int counter) {
        this.counter = counter;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

}
