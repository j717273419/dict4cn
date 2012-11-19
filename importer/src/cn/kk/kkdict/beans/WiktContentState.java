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
import java.util.Map;
import java.util.Set;

import cn.kk.kkdict.utils.Helper;

public class WiktContentState {

  private ByteBuffer                line;
  private String                    name;
  private String                    sourceLanguage;
  private String                    targetLanguage;
  private String                    sourceWordType;
  private String                    sourceGender;
  private String                    targetWordType;
  private String                    targetGender;
  private final Map<String, String> languages;
  private Set<String>               categories;
  private boolean                   translationContent;
  private final String              fileLanguage;

  public WiktContentState(final String lng) {
    this.fileLanguage = lng;
    this.languages = new FormattedTreeMap<>();
    this.categories = new FormattedTreeSet<>();
    this.invalidate();
  }

  public String getSourceLanguage() {
    return this.sourceLanguage;
  }

  public void setSourceLanguage(final String sourceLanguage) {
    this.sourceLanguage = sourceLanguage;
  }

  public void putTranslation(final String key, final String val) {
    String v1 = null;
    if ((v1 = this.languages.get(key)) != null) {
      this.languages.put(key, v1 + Helper.SEP_SAME_MEANING + val);
    } else {
      this.languages.put(key, val);
    }
  }

  private void clear() {
    this.languages.clear();
    this.sourceWordType = Helper.EMPTY_STRING;
    this.sourceGender = Helper.EMPTY_STRING;
    this.targetWordType = Helper.EMPTY_STRING;
    this.targetGender = Helper.EMPTY_STRING;
  }

  public String getName() {
    return this.name;
  }

  public boolean hasTranslations() {
    if (this.languages.isEmpty()) {
      return false;
    } else if (this.languages.size() > 1) {
      return true;
    } else {
      return this.languages.get(this.sourceLanguage) == null;
    }
  }

  public String getTranslations() {
    return this.languages.toString();
  }

  public void invalidate() {
    this.name = null;
    this.sourceLanguage = null;
    this.targetLanguage = null;

    this.clear();
  }

  public void init(final String n) {
    this.name = n;
    this.clear();
  }

  public boolean isValid() {
    return this.name != null;
  }

  public String getTargetLanguage() {
    return this.targetLanguage;
  }

  public void setTargetLanguage(final String targetLanguage) {
    this.targetLanguage = targetLanguage;
  }

  public boolean isTranslationContent() {
    return this.translationContent;
  }

  public void setTranslationContent(final boolean translationContent) {
    this.translationContent = translationContent;
    if (translationContent) {
      this.setSourceGender(Helper.EMPTY_STRING);
    }
  }

  public String getSourceWordType() {
    return this.sourceWordType;
  }

  public void setSourceWordType(final String sourceWordType) {
    this.sourceWordType = sourceWordType;
  }

  public String getSourceGender() {
    return this.sourceGender;
  }

  public void setSourceGender(final String sourceGender) {
    this.sourceGender = sourceGender;
  }

  public String getTargetWordType() {
    return this.targetWordType;
  }

  public void setTargetWordType(final String targetWordType) {
    this.targetWordType = targetWordType;
  }

  public String getTargetGender() {
    return this.targetGender;
  }

  public void setTargetGender(final String targetGender) {
    this.targetGender = targetGender;
  }

  public void clearTargetAttributes() {
    this.targetGender = Helper.EMPTY_STRING;
    this.targetWordType = Helper.EMPTY_STRING;
  }

  public boolean hasTargetGender() {
    return !this.targetGender.isEmpty();
  }

  public boolean hasTargetWordType() {
    return !this.targetWordType.isEmpty();
  }

  public void clearSourceAttributes() {
    this.sourceGender = Helper.EMPTY_STRING;
    this.sourceWordType = Helper.EMPTY_STRING;
  }

  public String getFileLanguage() {
    return this.fileLanguage;
  }

  public Set<String> getCategories() {
    return this.categories;
  }

  public void setCategories(final Set<String> categories) {
    this.categories = categories;
  }

  public void addCategory(final String category) {
    this.categories.add(category);
  }

  public boolean hasCategories() {
    return !this.categories.isEmpty();
  }

  public boolean hasSourceGender() {
    return !this.sourceGender.isEmpty();
  }

  public boolean hasSourceWordType() {
    return !this.sourceWordType.isEmpty();
  }

  public Map<String, String> getLanguages() {
    return this.languages;
  }

  public String getTranslation(final String targetLng) {
    return this.languages.get(targetLng);
  }

  public void setTranslation(final String targetLng, final String trans) {
    this.languages.put(targetLng, trans);
  }

  public ByteBuffer getLine() {
    return this.line;
  }

  public void setLine(final ByteBuffer line) {
    this.line = line;
  }
}
