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

import java.util.Map;

import cn.kk.kkdict.types.Language;
import cn.kk.kkdict.utils.Helper;

public class TranslationInfo implements Comparable<TranslationInfo> {
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + ((this.key == null) ? 0 : this.key.hashCode());
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
    final TranslationInfo other = (TranslationInfo) obj;
    if (this.key == null) {
      if (other.key != null) {
        return false;
      }
    } else if (!this.key.equals(other.key)) {
      return false;
    }
    return true;
  }

  public TranslationInfo() {
  }

  public String              key       = Helper.EMPTY_STRING;
  public String              family    = Helper.EMPTY_STRING;
  public String              iso1      = Helper.EMPTY_STRING;
  public String              iso2      = Helper.EMPTY_STRING;
  public String              iso3      = Helper.EMPTY_STRING;
  public Map<String, String> lngMap    = new FormattedTreeMap<>();

  public String              direction = Helper.EMPTY_STRING;
  public String              original  = Helper.EMPTY_STRING;
  public String              comment   = Helper.EMPTY_STRING;

  public void invalidate() {
    this.key = Helper.EMPTY_STRING;
    this.iso1 = Helper.EMPTY_STRING;
    this.iso2 = Helper.EMPTY_STRING;
    this.iso3 = Helper.EMPTY_STRING;
  }

  public boolean isValid() {
    if (this.key.isEmpty()) {
      if (Helper.isNotEmptyOrNull(this.iso1)) {
        this.key = this.iso1;
      } else if (Helper.isNotEmptyOrNull(this.iso2)) {
        this.key = this.iso2;
      } else if (Helper.isNotEmptyOrNull(this.iso3)) {
        this.key = this.iso3;
      }
    }
    return Helper.isNotEmptyOrNull(this.key);
  }

  @Override
  public int compareTo(final TranslationInfo o) {
    return this.key.compareTo(o.key);
  }

  public void put(final Language lng, final String value) {
    this.lngMap.put(lng.key, value);
  }

  public String get(final Language lng) {
    return this.lngMap.get(lng.key);
  }

}
