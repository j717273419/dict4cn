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

import java.util.Iterator;
import java.util.TreeSet;

import cn.kk.kkdict.utils.Helper;

public class FormattedTreeSet<E> extends TreeSet<E> {
  private static final long serialVersionUID = -8035295407619357235L;
  private String            sep              = Helper.SEP_LIST;

  public FormattedTreeSet() {
    super();
  }

  public FormattedTreeSet(final String sep) {
    super();
    this.sep = sep;
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
      sb.append(this.sep);
    }
  }

  public String getSep() {
    return this.sep;
  }

  public void setSep(final String sep) {
    this.sep = sep;
  }

}
