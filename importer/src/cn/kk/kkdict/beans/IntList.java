package cn.kk.kkdict.beans;

public class IntList {
    public int ints[] = null;
    public int num = 0;
    private int growBy = 0;

    public IntList() {
        this(10, 10);
    }

    public IntList(int size) {
        this(size, 5);
    }

    public IntList(int size, int growBy) {
        this.growBy = growBy;
        ints = new int[size];
    }

    public IntList(int[] ints) {
        this(ints, ints.length);
    }

    public IntList(int[] theInts, int length) {
        this(length, 0);
        System.arraycopy(theInts, 0, ints, 0, length);
        num = length;
    }

    public void add(int o) {
        if (num >= ints.length && growBy > 0) {
            int[] temp = new int[ints.length + growBy];
            System.arraycopy(ints, 0, temp, 0, num);
            ints = temp;
        }
        ints[num] = o;
        ++num;
    }

    public void removeIndex(int i) {
        --num;
        ints[i] = ints[num];
    }

    public int get(int i) {
        return ints[i];
    }

    int getLast() {
        return ints[num - 1];
    }

    public void set(int i, int o) {
        ints[i] = o;
    }

    public int find(int o) {
        for (int i = 0; i < num; ++i) {
            if (ints[i] == o)
                return i;
        }
        return -1;
    }
    
    public int removeDuplicates() {
        int count = 0;
        for (int i = 0; i < num; ++i) {
            for (int j = i + 1; j < num; ++j) {
                if (ints[j] == ints[i]) {
                    removeIndex(j);
                    --j;
                    ++count;
                }
            }
        }
        return count;
    }

    public int size() {
        return num;
    }
}