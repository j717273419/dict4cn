package cn.kk.kkdict.types;

public enum Gender {
    MASCULINE("阳"), FEMININE("阴"), NEUTER("中"), PLURAL("复");
    public static final String TYPE_ID = "冠";
    public final String key;

    private Gender(String key) {
        this.key = key;
    }
}
