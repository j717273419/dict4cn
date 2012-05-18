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

public enum Usage {
	// Indication of Usage
	CHILDRENS("童"),
	COLLOQUIAL("口"),
	DIMINUTIVE("压"),
	ELEVATED("夸"),
	DATED("古"),
	FAMILIAR("俗"),
	FIGURATIVE("谚"),
	METAPHORICAL("转"),
	FORMAL("书"),
	HUMOROUS("谑"),
	OBSOLETE("过"),
	HUMBLE("谦"),
	PEJORATIVE("贬"),
	POETIC("诗"),
	RARE("罕"),
	JUDICIAL("咒"),
	DIALECT("方"),
	VULGAR("粗"),
	POLITE("友"),
	SLANG("俚"),
	RESPECTFUL("敬"),
	TABOO("禁"),
	;
	public static final String	TYPE_ID	= "用";

	public final String					key;

	Usage(String key) {
		this.key = key;
	}

}
