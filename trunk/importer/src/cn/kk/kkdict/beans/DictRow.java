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

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class DictRow {
  private static final Map<String, String> EMPTY_TREE_MAP = Collections.unmodifiableMap(new FormattedTreeMap<String, String>());
  private static final Set<String>         EMPTY_TREE_SET = Collections.unmodifiableSet(new FormattedTreeSet<String>());
  private String                           name;
  private String                           pronounciation;
  private Set<String>                      categories;
  private Map<String, String>              translations;

  public DictRow() {
    this.categories = DictRow.EMPTY_TREE_SET;
    this.translations = DictRow.EMPTY_TREE_MAP;
  }

  public DictRow(final String name, final Set<String> categories, final Map<String, String> translations) {
    super();
    this.name = name;
    this.categories = categories;
    this.translations = translations;
  }

  public String getName() {
    return this.name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public Set<String> getCategories() {
    return this.categories;
  }

  public void setCategories(final Set<String> categories) {
    this.categories = categories;
  }

  public Map<String, String> getTranslations() {
    return this.translations;
  }

  public void setTranslations(final Map<String, String> translations) {
    this.translations = translations;
  }

  public String getPronounciation() {
    return this.pronounciation;
  }

  public void setPronounciation(final String pronounciation) {
    this.pronounciation = pronounciation;
  }
}
