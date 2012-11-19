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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cn.kk.kkdict.utils.Helper;

public class FormattedArrayList<E> extends ArrayList<E> {
  private static final long serialVersionUID = 1414626422972498763L;

  public FormattedArrayList(final List<E> values) {
    super(values);
  }

  public FormattedArrayList() {
    super();
  }

  public FormattedArrayList(final int size) {
    super(size);
  }

  @Override
  public String toString() {
    final Iterator<E> i = this.iterator();
    if (!i.hasNext()) {
      return Helper.EMPTY_STRING;
    }

    final StringBuilder sb = new StringBuilder();
    for (;;) {
      final E e = i.next();
      sb.append(e == this ? Helper.EMPTY_STRING : e);
      if (!i.hasNext()) {
        return sb.toString();
      }
      sb.append(Helper.SEP_LIST);
    }
  }

}
