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
package cn.kk.kkdict.types;

import cn.kk.kkdict.utils.Helper;

public enum WordType {
  NOUN("名", "名词"),
  VERB("动", "动词"),
  ADJECTIVE("形", "形容词"),
  ADVERB("副", "副词"),
  PREPOSITION("介", "介词"),
  PRONOUN("代", "代词"),
  PROPER_NOUN("专", "专用名词"),
  CONJUNCTION("连", "连词"),
  INTERJECTION("叹", "感叹词"),
  DETERMINER("定", "定词"),
  ARTICLE("冠", "冠词"),
  NUMBER("量", "量词"),
  PHRASE("句", "句子"),
  NUMERAL("数", "数字"),
  PREFIX("前", "前缀"),
  SUFFIX("后", "后缀"),
  ABBREVIATION("缩", "缩写"),
  PARTICLE("助", "助词"),
  SINGULAR("单", "单数"),
  PARTICIPLE("分", "分词"),
  PLURAL("复", "复数"),
  VERB_REFLEXIVE("自", "反身动词"),
  CONTRACTION("缩", "缩写词"),
  VERB_TRANSITIVE("及", "及物动词"),
  VERB_INTRANSITIVE("莫", "不及物动词"),
  VERB_PAST_PARTICIPLE("过", "过去分词"),
  VERB_GERUND("化", "动名词"),
  AD_COMPARATIVE("比", "比较级"),
  AD_SUPERLATIVE("最", "最高级"),
  PROVERB("谚", "谚语"),
  IDIOM("习", "习惯用语"),
  COMPOUND_WORD("混", "复合词"),
  EXAMPLE("例", "例子"), ;

  public static final String TYPE_ID       = "词";

  public static final byte[] TYPE_ID_BYTES = WordType.TYPE_ID.getBytes(Helper.CHARSET_UTF8);

  public final String        key;

  public final byte[]        keyBytes;

  public final String        name;

  WordType(final String key, final String name) {
    this.key = key;
    this.name = name;
    this.keyBytes = key.getBytes(Helper.CHARSET_UTF8);
  }
}
