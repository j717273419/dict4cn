package cn.kk.kkdict.types;

public enum Usage {
    // Indication of Usage
    COLLOQUIAL("口"),
    DATED("过"),
    FAMILIAR("俗"),
    FIGURATIVE("转"),
    FORMAL("书"),
    HUMOROUS("谑"),
    OBSOLETE("古"),
    PEJORATIVE("贬"),
    POETIC("诗"),
    RARE("罕"),
    SLANG("咒"),
    VULGAR("粗"),   
    ;
    public static final String TYPE_ID = "用";
    
    public final String key;
    Usage(String key) {
        this.key = key;
    }

}
