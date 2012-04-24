package cn.kk.kkdict.types;

import java.util.Arrays;
import java.util.Set;

import cn.kk.kkdict.beans.FormattedTreeSet;
import cn.kk.kkdict.utils.Helper;

public enum Category {
    PEOPLE("人"), NAME("名"), // 地名， 山， 河， 桥， 路， 街 学校 单位
    BIOLOGY("生"),
    GEOGRAPHY("地"), // 天文， 地理，地质，航海
    COSMETICS("美"), // 美容， 化妆, 护肤
    PHYLOSOPHY("心"),
    HISTORY("史"),
    EDUCATION("教"),
    AUTOMOBILE("车"),
    INDUSTRY("工"),
    SOFTWARE("软"),
    TECHNOLOGY("技"),
    AGRICULTURE("农"),
    CHEMISTRY("化"),
    MATHEMATICS("数"),
    LITERATURE("文"),
    MILITARY("军"),
    SPORTS("体"),
    MUSIC("音"),
    RELIGION("信"),
    ARTS("艺"),
    HOBBY("趣"),
    ECONOMY("商"), // 品牌
    MYTHOLOGY("神"),
    PHYSICS("物"),
    POLITICS("政"),
    JUSTICE("法"),
    HOME("家"),
    CUISINE("吃"),
    MEDICINE("医"),
    TIME("时"),
    BUILDING("建"), // 交通， 建筑
    PETS("宠"),
    GAME("玩"),
    MEDIA("视"),
    DIALECT("方"),
    SOCIETY("社"), ;
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
