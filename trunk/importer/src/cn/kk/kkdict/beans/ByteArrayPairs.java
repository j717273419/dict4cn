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
  private final byte[][]               keys;
  private final byte[][]               values;
  private boolean                      sorted;

  public ByteArrayPairs(final int length) {
    this.keys = new byte[length][];
    this.values = new byte[length][];
    this.keysIdent = new HashMap<>(length);
  }

  public ByteArrayPairs put(final int i, final byte[] k, final byte[] v) {
    byte[] key;
    final ByteArray keyObj = new ByteArray(k);
    if (null == (key = this.keysIdent.get(keyObj))) {
      key = k;
      this.keysIdent.put(keyObj, key);
    }
    this.keys[i] = key;
    this.values[i] = v;
    return this;
  }

  public ByteArrayPairs sort() {
    final Map<ByteArray, byte[]> entries = new HashMap<>(this.keys.length);
    for (int i = 0; i < this.keys.length; i++) {
      entries.put(new ByteArray(this.values[i]), this.keys[i]);
    }
    if (entries.size() != this.keys.length) {
      System.err.println("找到重叠名称。排序失败！");
      return this;
    }
    final List<ByteArray> vs = new ArrayList<>(entries.keySet());
    Collections.sort(vs);
    for (int i = 0; i < this.keys.length; i++) {
      final ByteArray v = vs.get(i);
      this.values[i] = v.getData();
      this.keys[i] = entries.get(v);
    }
    this.sorted = true;
    return this;
  }

  public boolean isSorted() {
    return this.sorted;
  }

  public final byte[] findKey(final ByteBuffer value) {
    final int offset = ArrayHelper.findTrimmedOffset(value);
    final int len = ArrayHelper.findTrimmedEndIdx(value) - offset;
    return this.findKey(ArrayHelper.toBytes(value, offset, len));
  }

  public final byte[] findKey(final byte[] value) {
    final int idx = Arrays.binarySearch(this.values, value, ArrayHelper.COMPARATOR_BYTE_ARRAY);
    if (idx >= 0) {
      return this.keys[idx];
    } else {
      return null;
    }
  }

  public final byte[] containsKey(final ByteBuffer key) {
    final int offset = ArrayHelper.findTrimmedOffset(key);
    final int len = ArrayHelper.findTrimmedEndIdx(key) - offset;
    return this.containsKey(ArrayHelper.toBytes(key, offset, len));
  }

  public final byte[] containsKey(final byte[] key) {
    return this.keysIdent.get(new ByteArray(key));
  }

}
