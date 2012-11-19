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

public class IndexedByteArray implements Comparable<IndexedByteArray> {
  private byte[] data;
  private int    idx;
  private int    weight;

  public IndexedByteArray() {
  }

  public IndexedByteArray(final int idx, final byte[] data) {
    this.idx = idx;
    this.data = data;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + Arrays.hashCode(this.data);
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (this.getClass() != obj.getClass()) {
      return false;
    }
    final IndexedByteArray other = (IndexedByteArray) obj;
    if (!Arrays.equals(this.data, other.data)) {
      return false;
    }
    return true;
  }

  @Override
  public int compareTo(final IndexedByteArray o) {
    return Arrays.hashCode(this.data) - Arrays.hashCode(o.data);
  }

  public byte[] getData() {
    return this.data;
  }

  public IndexedByteArray setData(final byte[] data) {
    this.data = data;
    return this;
  }

  public int getIdx() {
    return this.idx;
  }

  public void setIdx(final int idx) {
    this.idx = idx;
  }

  public int getWeight() {
    return this.weight;
  }

  public void setWeight(final int weight) {
    this.weight = weight;
  }
}
