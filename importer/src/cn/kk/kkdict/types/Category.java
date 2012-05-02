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

import java.util.Arrays;
import java.util.Set;

import cn.kk.kkdict.beans.FormattedTreeSet;
import cn.kk.kkdict.utils.Helper;

public enum Category {
    PEOPLE("人"), // 工作，职业，人际关系
    NAME("名"), // 地名， 山， 河， 桥， 路， 街 学校 单位, 品牌
    BIOLOGY("生"),
    GEOGRAPHY("地"), // 天文， 地理，地质，航海，天气
    COSMETICS("美"), // 美容， 化妆, 护肤, 穿着, 眼饰
    PHILOSOPHY("哲"),
    PSYCHOLOGY("心"),
    HISTORY("史"),
    EDUCATION("教"),
    AUTOMOBILE("车"),
    MARITIME("船"),
    RAILWAYS("轨"),
    AERIAL("飞"),
    INDUSTRY("工"),
    SOFTWARE("软"),
    TECHNOLOGY("技"),
    AGRICULTURE("农"), // 农业，渔业，林业
    CHEMISTRY("化"),
    MATHEMATICS("数"),
    LITERATURE("文"), // 文学，语言
    MILITARY("军"),
    SPORTS("体"),
    MUSIC("音"),
    RELIGION("信"),
    ARTS("艺"),
    HOBBY("趣"),
    ECONOMY("商"), // 
    MYTHOLOGY("神"),
    PHYSICS("物"),
    POLITICS("政"), // 政府，机构，政治
    LAW("法"),
    HOME("家"), // 家具，文具，办公室
    CUISINE("吃"), // 吃喝烟酒
    MEDICINE("医"),
    TIME("时"),
    BUILDING("建"), // 交通， 建筑
    PETS("宠"),
    GAME("玩"),
    MEDIA("媒"),    
    SOCIETY("社"),
    ASTRONOMY("星"),
    SECURITY("安"),
    COMMUNICATION("讯"),    
    TRAVEL("旅"),
    INSURANCE("保"),;
    public static final String TYPE_ID = "类";
    public static final byte[] TYPE_ID_BYTES = TYPE_ID.getBytes(Helper.CHARSET_UTF8);
    public static final String[] KEYS;
    static {
        Category[] values = Category.values();
        KEYS = new String[values.length];
        int i = 0;
        for (Category c : values) {
            KEYS[i] = c.key;
            i++;
        }
        Arrays.sort(KEYS);
    }
    public final String key;

    Category(String key) {
        this.key = key;
    }

    public static Set<String> parseValid(String[] split) {
        Set<String> result = new FormattedTreeSet<String>();
        for (String s : split) {
            if (Arrays.binarySearch(KEYS, s) >= 0) {
                result.add(s);
            }
        }
        return result;
    }
}
