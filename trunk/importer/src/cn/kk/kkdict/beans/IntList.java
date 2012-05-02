/*  Copyright (c) 2010 Xiaoyun Zhu
 * 
 *  Permission is hereby granted, free of charge, to any person obtaining a copy  
 *  of this software and associated documentation files (the "Software"), to deal  
 *  in the Software without restriction, including without limitation the rights  
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell  
 *  copies of the Software, and to permit persons to whom the Software is  
 *  furnished to do so, subject to the following conditions:
 *  
 *  The above copyright notice and this permission notice shall be included in  
 *  all copies or substantial portions of the Software.
 *  
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR  
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,  
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE  
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER  
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,  
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN  
 *  THE SOFTWARE.  
 */
package cn.kk.kkdict.beans;

public class IntList {
    public int ints[] = null;
    public int num = 0;
    private int growBy = 0;

    public IntList() {
        this(10, 10);
    }

    public IntList(final int size) {
        this(size, 10);
    }

    public IntList(final int size, final int growBy) {
        this.growBy = growBy;
        ints = new int[size];
    }

    public IntList(final int[] ints) {
        this(ints, ints.length);
    }

    public IntList(final int[] theInts, final int length) {
        this(length, 0);
        System.arraycopy(theInts, 0, ints, 0, length);
        num = length;
    }

    public final void add(final int o) {
        if (num >= ints.length && growBy > 0) {
            int[] temp = new int[ints.length + growBy];
            System.arraycopy(ints, 0, temp, 0, num);
            ints = temp;
        }
        ints[num] = o;
        ++num;
    }

    public final void removeIndex(final int i) {
        if (i < num) {
            --num;
            ints[i] = ints[num];
        } else {
            throw new IllegalArgumentException("Index " + i + " should within " + num + "!");
        }
    }

    public final int get(final int i) {
        if (i < num) {
            return ints[i];
        } else {
            throw new IllegalArgumentException("Index " + i + " should within " + num + "!");
        }
    }

    public final int getLast() {
        if (num > 0) {
            return ints[num - 1];
        } else {
            throw new IllegalArgumentException("List is empty!");
        }
    }

    public final void set(final int i, final int o) {
        if (i < num) {
            ints[i] = o;
        } else {
            throw new IllegalArgumentException("Index " + i + " should within " + num + "!");
        }
    }

    public final int find(final int o) {
        for (int i = 0; i < num; ++i) {
            if (ints[i] == o)
                return i;
        }
        return -1;
    }

    public final int removeDuplicates() {
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

    public final int size() {
        return num;
    }

    public final IntList size(final int size) {
        if (ints.length <= size) {
            int[] temp = new int[size];
            System.arraycopy(ints, 0, temp, 0, num);
            ints = temp;
            num = size;
        }
        num = size;
        return this;
    }

    public final void clear() {
        num = 0;
    }
}