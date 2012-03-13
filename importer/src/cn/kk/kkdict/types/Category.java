package cn.kk.kkdict.types;

public enum Category {
    PEOPLE("人"),
    NAME("名"),
    BIOLOGY("生"),
    GEOGRAPHY("地"),
    PHYLOSOPHY("心"),
    HISTORY("历"),
    EDUCATION("教"),
    INDUSTRY("工"),
    TECHNOLOGY("技"),
    AGRICULTURE("农"),
    CHEMISTRY("化"),
    MATHEMATICS("数"),
    LITERATURE("文"),
    MILITARY("军"),
    SPORTS("体"),
    RELIGION("信"),
    ARTS("艺"),
    HOBBY("趣"),
    ECONOMY("商"),
    MYTHOLOGY("神"),
    PHYSICS("物"),
    POLITICS("政"),
    HOME("家"),
    MEDICINE("医"),
    TIME("时"),
    TRAFFIC("交");
    public static final String TYPE_ID = "类";

    public final String key;

    Category(String key) {
        this.key = key;
    }

}
