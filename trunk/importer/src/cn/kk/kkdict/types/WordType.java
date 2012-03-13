package cn.kk.kkdict.types;

public enum WordType {
    NOUN("名", "名词"),
    VERB("动", "动词"),
    ADJECTIVE("形", "形容词"),
    ADVERB("副", "副词"),
    PREPOSITION("介", "介词"),
    PRONOUN("代", "代词"),
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
    ANTONYM("反", "反义词"),
    SYNONYM("近", "近义词"),
    PARTICLE("助", "助词"),
    SINGULAR("单", "单数"),
    // TODO
    PARTICIPE("分", "分词"), ;
    public static final String TYPE_ID = "词";
    public final String key;
    public final String name;

    WordType(String key, String name) {
        this.key = key;
        this.name = name;
    }
}
