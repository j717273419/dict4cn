package cn.kk.kkdict.types;

public enum WordSource {
    BAIDU_BDICT("baidu_bcd"), QQ_QPYD("qq_qpyd"), SOGOU_SCEL("sogou_scel"), SOGOU_CORE("sogou_core"), ;
    public static final String TYPE_ID = "典";
    public final String key;

    WordSource(String key) {
        this.key = key;
    }
}
