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

import java.util.Arrays;

public class IntList {
  public int  ints[] = null;
  public int  num    = 0;
  private int growBy = 0;

  public IntList() {
    this(10, 10);
  }

  public IntList(final int size) {
    this(size, 10);
  }

  public IntList(final int size, final int growBy) {
    this.growBy = growBy;
    this.ints = new int[size];
  }

  public IntList(final int[] ints) {
    this(ints, ints.length);
  }

  public IntList(final int[] theInts, final int length) {
    this(length, 0);
    System.arraycopy(theInts, 0, this.ints, 0, length);
    this.num = length;
  }

  public final void add(final int o) {
    if ((this.num >= this.ints.length) && (this.growBy > 0)) {
      final int[] temp = new int[this.ints.length + this.growBy];
      System.arraycopy(this.ints, 0, temp, 0, this.num);
      this.ints = temp;
    }
    this.ints[this.num] = o;
    ++this.num;
  }

  public final void removeIndex(final int i) {
    if (i < this.num) {
      --this.num;
      this.ints[i] = this.ints[this.num];
    } else {
      throw new IllegalArgumentException("Index " + i + " should within " + this.num + "!");
    }
  }

  public final int get(final int i) {
    if (i < this.num) {
      return this.ints[i];
    } else {
      throw new IllegalArgumentException("Index " + i + " should within " + this.num + "!");
    }
  }

  public final int getLast() {
    if (this.num > 0) {
      return this.ints[this.num - 1];
    } else {
      throw new IllegalArgumentException("List is empty!");
    }
  }

  public final void set(final int i, final int o) {
    if (i < this.num) {
      this.ints[i] = o;
    } else {
      this.size(i + 10);
      this.ints[i] = o;
    }
  }

  public final int find(final int o) {
    for (int i = 0; i < this.num; ++i) {
      if (this.ints[i] == o) {
        return i;
      }
    }
    return -1;
  }

  public final int removeDuplicates() {
    int count = 0;
    for (int i = 0; i < this.num; ++i) {
      for (int j = i + 1; j < this.num; ++j) {
        if (this.ints[j] == this.ints[i]) {
          this.removeIndex(j);
          --j;
          ++count;
        }
      }
    }
    return count;
  }

  public final int size() {
    return this.num;
  }

  public final IntList size(final int size) {
    if (this.ints.length <= size) {
      final int[] temp = new int[size];
      System.arraycopy(this.ints, 0, temp, 0, this.num);
      this.ints = temp;
      this.num = size;
    }
    this.num = size;
    return this;
  }

  public final void clear() {
    this.num = 0;
  }

  public int getFirst() {
    if (this.num > 0) {
      return this.ints[0];
    } else {
      throw new IllegalArgumentException("List is empty!");
    }

  }
  
  public String toString() {
    return Arrays.toString(ints);
  }
}
