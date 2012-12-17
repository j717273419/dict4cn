package cn.kk.kkdict.database;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

import cn.kk.kkdict.utils.PhoneticTranscriptionHelper;

public class SuperIndexGenerator {

  final static int LEN_SRC_KEY = 16;

  /**
   * @param args
   * @throws IOException
   */
  public static void main(String[] args) throws IOException {
    String str = "你好(der) [die]Testt puublic static -., #+üäöüß ÄÖÜ?éÉ void mainioux+-*/%=,.:;!~^_|&#\"'´`{}()<>\\[\\]";
    byte[] phoneticIdx = SuperIndexGenerator.createPhonetecIdx(str);
    System.out.println(Arrays.toString(phoneticIdx));
  }

  private static char[][][] metaphoneMapping;

  private static char[][][] pinyinEncMapping;

  private static char[][][] getPinyinEncodingMapping() {
    if (SuperIndexGenerator.pinyinEncMapping == null) {
      SuperIndexGenerator.init();
    }
    return SuperIndexGenerator.pinyinEncMapping;
  }

  private static char[][][] getMetaphoneMapping() {
    if (SuperIndexGenerator.metaphoneMapping == null) {
      SuperIndexGenerator.init();
    }
    return SuperIndexGenerator.metaphoneMapping;
  }

  private static void init() {
    SuperIndexGenerator.metaphoneMapping = new char[][][] { { "ä".toCharArray(), "e".toCharArray(), }, { "ö".toCharArray(), "v".toCharArray(), },
        { "ü".toCharArray(), "v".toCharArray(), }, { "ß".toCharArray(), "s".toCharArray(), }, { "é".toCharArray(), "ei".toCharArray(), },
        { "è".toCharArray(), "e".toCharArray(), }, { "ê".toCharArray(), "e".toCharArray(), }, { "î".toCharArray(), "i".toCharArray(), },
        { "í".toCharArray(), "i".toCharArray(), }, { "ï".toCharArray(), "i".toCharArray(), }, { "ì".toCharArray(), "i".toCharArray(), },
        { "á".toCharArray(), "a".toCharArray(), }, { "à".toCharArray(), "a".toCharArray(), }, { "â".toCharArray(), "a".toCharArray(), },
        { "ó".toCharArray(), "o".toCharArray(), }, { "ô".toCharArray(), "o".toCharArray(), }, { "ò".toCharArray(), "o".toCharArray(), },
        { "ú".toCharArray(), "u".toCharArray(), }, { "ù".toCharArray(), "u".toCharArray(), }, { "û".toCharArray(), "u".toCharArray(), },
        { "œ".toCharArray(), "v".toCharArray(), }, { "æ".toCharArray(), "e".toCharArray(), }, { "ç".toCharArray(), "s".toCharArray(), },
        { "th".toCharArray(), "s".toCharArray(), }, { "cs".toCharArray(), "x".toCharArray(), }, { "ks".toCharArray(), "x".toCharArray(), },
        { "ion".toCharArray(), "iong".toCharArray(), }, { "sch".toCharArray(), "sh".toCharArray(), }, { "tsch".toCharArray(), "ch".toCharArray(), },
        { "chs".toCharArray(), "x".toCharArray(), }, { "ung".toCharArray(), "ong".toCharArray(), }, { "ea".toCharArray(), "ia".toCharArray(), },
        { "gn".toCharArray(), "n".toCharArray(), }, { "kn".toCharArray(), "n".toCharArray(), }, { "pn".toCharArray(), "n".toCharArray(), },
        { "wr".toCharArray(), "r".toCharArray(), }, { "ps".toCharArray(), "s".toCharArray(), }, { "ck".toCharArray(), "k".toCharArray(), },
        { "ae".toCharArray(), "e".toCharArray(), }, { "oe".toCharArray(), "ou".toCharArray(), }, { "eo".toCharArray(), "iu".toCharArray(), },
        { "ay".toCharArray(), "ai".toCharArray(), }, { "ey".toCharArray(), "ai".toCharArray(), }, { "oi".toCharArray(), "ui".toCharArray(), },
        { "ew".toCharArray(), "iu".toCharArray(), }, { "eu".toCharArray(), "iu".toCharArray(), }, { "ough".toCharArray(), "iu".toCharArray(), },
        { "ault".toCharArray(), "olt".toCharArray(), }, { "oup".toCharArray(), "up".toCharArray(), }, { "ioux".toCharArray(), "ux".toCharArray(), },
        { "csz".toCharArray(), "ch".toCharArray(), }, { "mn".toCharArray(), "n".toCharArray(), }, { "or".toCharArray(), "o".toCharArray(), },
        { "aigh".toCharArray(), "ei".toCharArray(), }, { "eigh".toCharArray(), "ei".toCharArray(), }, { "ow".toCharArray(), "ou".toCharArray(), },
        { "eau".toCharArray(), "ou".toCharArray(), }, { "aoh".toCharArray(), "ou".toCharArray(), }, { "ough".toCharArray(), "ou".toCharArray(), },
        { "igh".toCharArray(), "ai".toCharArray(), }, { "ear".toCharArray(), "ie".toCharArray(), }, { "aire".toCharArray(), "er".toCharArray(), },
        { "eir".toCharArray(), "er".toCharArray(), }, { "air".toCharArray(), "er".toCharArray(), }, { "eye".toCharArray(), "ai".toCharArray(), },
        { "aw".toCharArray(), "oi".toCharArray(), }, { "uoy".toCharArray(), "oi".toCharArray(), }, { "err".toCharArray(), "er".toCharArray(), },
        { "eur".toCharArray(), "er".toCharArray(), }, { "oeu".toCharArray(), "er".toCharArray(), }, { "uer".toCharArray(), "er".toCharArray(), },
        { "ieu".toCharArray(), "iu".toCharArray(), }, { "iew".toCharArray(), "iu".toCharArray(), }, { "ueue".toCharArray(), "iu".toCharArray(), },
        { "gh".toCharArray(), "f".toCharArray(), }, { "hj".toCharArray(), "x".toCharArray(), }, { "kj".toCharArray(), "k".toCharArray(), },
        { "nk".toCharArray(), "k".toCharArray(), }, { "bl".toCharArray(), "pl".toCharArray(), }, { "gn".toCharArray(), "ni".toCharArray(), },
        { "pf".toCharArray(), "f".toCharArray(), }, { "sci".toCharArray(), "si".toCharArray(), }, };
    SuperIndexGenerator.pinyinEncMapping = new char[][][] { { "shi".toCharArray(), new char[] { 1 } }, { "ji".toCharArray(), new char[] { 2 } },
        { "yi".toCharArray(), new char[] { 3 } }, { "zhi".toCharArray(), new char[] { 4 } }, { "xing".toCharArray(), new char[] { 5 } },
        { "li".toCharArray(), new char[] { 6 } }, { "fu".toCharArray(), new char[] { 7 } }, { "xi".toCharArray(), new char[] { 8 } },
        { "xian".toCharArray(), new char[] { 9 } }, { "qi".toCharArray(), new char[] { 10 } }, { "wei".toCharArray(), new char[] { 11 } },
        { "jia".toCharArray(), new char[] { 12 } }, { "jing".toCharArray(), new char[] { 13 } }, { "wu".toCharArray(), new char[] { 14 } },
        { "hua".toCharArray(), new char[] { 15 } }, { "he".toCharArray(), new char[] { 16 } }, { "da".toCharArray(), new char[] { 17 } },
        { "yuan".toCharArray(), new char[] { 18 } }, { "zi".toCharArray(), new char[] { 19 } }, { "er".toCharArray(), new char[] { 20 } },
        { "jin".toCharArray(), new char[] { 21 } }, { "bu".toCharArray(), new char[] { 22 } }, { "zhong".toCharArray(), new char[] { 23 } },
        { "xin".toCharArray(), new char[] { 24 } }, { "yu".toCharArray(), new char[] { 25 } }, { "jie".toCharArray(), new char[] { 26 } },
        { "jian".toCharArray(), new char[] { 27 } }, { "dian".toCharArray(), new char[] { 28 } }, { "shu".toCharArray(), new char[] { 29 } },
        { "dong".toCharArray(), new char[] { 30 } }, { "xiang".toCharArray(), new char[] { 31 } }, { "de".toCharArray(), new char[] { 32 } },
        { "yan".toCharArray(), new char[] { 33 } }, { "gong".toCharArray(), new char[] { 34 } }, { "xiao".toCharArray(), new char[] { 35 } },
        { "jiao".toCharArray(), new char[] { 36 } }, { "si".toCharArray(), new char[] { 37 } }, { "di".toCharArray(), new char[] { 38 } },
        { "you".toCharArray(), new char[] { 39 } }, { "ke".toCharArray(), new char[] { 40 } }, { "sheng".toCharArray(), new char[] { 41 } },
        { "gu".toCharArray(), new char[] { 42 } }, { "yang".toCharArray(), new char[] { 43 } }, { "ye".toCharArray(), new char[] { 44 } },
        { "fang".toCharArray(), new char[] { 45 } }, { "bao".toCharArray(), new char[] { 46 } }, { "zhao".toCharArray(), new char[] { 47 } },
        { "shao".toCharArray(), new char[] { 47 } }, { "tao".toCharArray(), new char[] { 47 } }, { "pao".toCharArray(), new char[] { 47 } },
        { "kao".toCharArray(), new char[] { 47 } }, { "zhu".toCharArray(), new char[] { 48 } }, { "guan".toCharArray(), new char[] { 49 } },
        { "tong".toCharArray(), new char[] { 50 } }, { "cheng".toCharArray(), new char[] { 51 } }, { "chang".toCharArray(), new char[] { 52 } },
        { "ju".toCharArray(), new char[] { 53 } }, { "an".toCharArray(), new char[] { 54 } }, { "lu".toCharArray(), new char[] { 55 } },
        { "guo".toCharArray(), new char[] { 56 } }, { "shen".toCharArray(), new char[] { 57 } }, { "qing".toCharArray(), new char[] { 58 } },
        { "yin".toCharArray(), new char[] { 59 } }, { "xue".toCharArray(), new char[] { 60 } }, { "she".toCharArray(), new char[] { 61 } },
        { "bei".toCharArray(), new char[] { 62 } }, { "suan".toCharArray(), new char[] { 63 } }, { "liu".toCharArray(), new char[] { 64 } },
        { "fen".toCharArray(), new char[] { 65 } }, { "ren".toCharArray(), new char[] { 66 } }, { "tian".toCharArray(), new char[] { 67 } },
        { "shang".toCharArray(), new char[] { 68 } }, { "ying".toCharArray(), new char[] { 69 } }, { "feng".toCharArray(), new char[] { 70 } },
        { "mei".toCharArray(), new char[] { 71 } }, { "ya".toCharArray(), new char[] { 72 } }, { "du".toCharArray(), new char[] { 73 } },
        { "hu".toCharArray(), new char[] { 74 } }, { "shan".toCharArray(), new char[] { 75 } }, { "dan".toCharArray(), new char[] { 76 } },
        { "zheng".toCharArray(), new char[] { 77 } }, { "fa".toCharArray(), new char[] { 78 } }, { "dou".toCharArray(), new char[] { 79 } },
        { "diu".toCharArray(), new char[] { 79 } }, { "xiu".toCharArray(), new char[] { 79 } }, { "niu".toCharArray(), new char[] { 79 } },
        { "miu".toCharArray(), new char[] { 79 } }, { "dao".toCharArray(), new char[] { 80 } }, { "gan".toCharArray(), new char[] { 81 } },
        { "ban".toCharArray(), new char[] { 82 } }, { "mo".toCharArray(), new char[] { 83 } }, { "chu".toCharArray(), new char[] { 84 } },
        { "qu".toCharArray(), new char[] { 85 } }, { "guang".toCharArray(), new char[] { 86 } }, { "bi".toCharArray(), new char[] { 87 } },
        { "bai".toCharArray(), new char[] { 88 } }, { "wen".toCharArray(), new char[] { 89 } }, { "fei".toCharArray(), new char[] { 90 } },
        { "ti".toCharArray(), new char[] { 91 } }, { "hui".toCharArray(), new char[] { 92 } }, { "ge".toCharArray(), new char[] { 93 } },
        { "shui".toCharArray(), new char[] { 94 } }, { "san".toCharArray(), new char[] { 95 } }, { "lin".toCharArray(), new char[] { 96 } },
        { "zui".toCharArray(), new char[] { 97 } }, { "cui".toCharArray(), new char[] { 97 } }, { "sui".toCharArray(), new char[] { 97 } },
        { "kui".toCharArray(), new char[] { 97 } }, { "gei".toCharArray(), new char[] { 97 } }, { "kei".toCharArray(), new char[] { 97 } },
        { "zhen".toCharArray(), new char[] { 98 } }, { "hai".toCharArray(), new char[] { 99 } }, { "nan".toCharArray(), new char[] { 100 } },
        { "tai".toCharArray(), new char[] { 101 } }, { "ma".toCharArray(), new char[] { 102 } }, { "ding".toCharArray(), new char[] { 103 } },
        { "bing".toCharArray(), new char[] { 104 } }, { "gao".toCharArray(), new char[] { 105 } }, { "zhuang".toCharArray(), new char[] { 106 } },
        { "luo".toCharArray(), new char[] { 107 } }, { "huo".toCharArray(), new char[] { 108 } }, { "ai".toCharArray(), new char[] { 109 } },
        { "wai".toCharArray(), new char[] { 109 } }, { "se".toCharArray(), new char[] { 110 } }, { "re".toCharArray(), new char[] { 110 } },
        { "men".toCharArray(), new char[] { 111 } }, { "en".toCharArray(), new char[] { 111 } }, { "gen".toCharArray(), new char[] { 111 } },
        { "geng".toCharArray(), new char[] { 111 } }, { "ken".toCharArray(), new char[] { 111 } }, { "keng".toCharArray(), new char[] { 111 } },
        { "shou".toCharArray(), new char[] { 112 } }, { "zhou".toCharArray(), new char[] { 113 } }, { "bo".toCharArray(), new char[] { 114 } },
        { "lian".toCharArray(), new char[] { 115 } }, { "pin".toCharArray(), new char[] { 116 } }, { "qin".toCharArray(), new char[] { 116 } },
        { "bin".toCharArray(), new char[] { 116 } }, { "qian".toCharArray(), new char[] { 117 } }, { "huang".toCharArray(), new char[] { 118 } },
        { "a".toCharArray(), new char[] { 119 } }, { "ka".toCharArray(), new char[] { 119 } }, { "ga".toCharArray(), new char[] { 119 } },
        { "xia".toCharArray(), new char[] { 120 } }, { "qia".toCharArray(), new char[] { 120 } }, { "dia".toCharArray(), new char[] { 120 } },
        { "lia".toCharArray(), new char[] { 120 } }, { "lv".toCharArray(), new char[] { 121 } }, { "hong".toCharArray(), new char[] { 122 } },
        { "su".toCharArray(), new char[] { 123 } }, { "nang".toCharArray(), new char[] { 124 } }, { "mang".toCharArray(), new char[] { 124 } },
        { "lang".toCharArray(), new char[] { 124 } }, { "rang".toCharArray(), new char[] { 124 } }, { "niang".toCharArray(), new char[] { 124 } },
        { "hang".toCharArray(), new char[] { 124 } }, { "ang".toCharArray(), new char[] { 124 } }, { "jiang".toCharArray(), new char[] { 125 } },
        { "ba".toCharArray(), new char[] { 126 } }, { "bian".toCharArray(), new char[] { 127 } }, { "chao".toCharArray(), new char[] { 128 } },
        { "ao".toCharArray(), new char[] { 128 } }, { "rong".toCharArray(), new char[] { 129 } }, { "nong".toCharArray(), new char[] { 129 } },
        { "yong".toCharArray(), new char[] { 130 } }, { "tou".toCharArray(), new char[] { 131 } }, { "pan".toCharArray(), new char[] { 132 } },
        { "bang".toCharArray(), new char[] { 132 } }, { "pang".toCharArray(), new char[] { 132 } }, { "dang".toCharArray(), new char[] { 132 } },
        { "ping".toCharArray(), new char[] { 133 } }, { "liang".toCharArray(), new char[] { 134 } }, { "e".toCharArray(), new char[] { 135 } },
        { "ei".toCharArray(), new char[] { 135 } }, { "hei".toCharArray(), new char[] { 135 } }, { "duo".toCharArray(), new char[] { 136 } },
        { "wang".toCharArray(), new char[] { 137 } }, { "tu".toCharArray(), new char[] { 138 } }, { "fan".toCharArray(), new char[] { 139 } },
        { "kong".toCharArray(), new char[] { 140 } }, { "niao".toCharArray(), new char[] { 141 } }, { "miao".toCharArray(), new char[] { 141 } },
        { "liao".toCharArray(), new char[] { 141 } }, { "fiao".toCharArray(), new char[] { 141 } }, { "pei".toCharArray(), new char[] { 142 } },
        { "bie".toCharArray(), new char[] { 142 } }, { "pie".toCharArray(), new char[] { 142 } }, { "qie".toCharArray(), new char[] { 142 } },
        { "chen".toCharArray(), new char[] { 143 } }, { "peng".toCharArray(), new char[] { 143 } }, { "pen".toCharArray(), new char[] { 143 } },
        { "beng".toCharArray(), new char[] { 143 } }, { "ce".toCharArray(), new char[] { 144 } }, { "che".toCharArray(), new char[] { 144 } },
        { "pian".toCharArray(), new char[] { 145 } }, { "nian".toCharArray(), new char[] { 145 } }, { "deng".toCharArray(), new char[] { 146 } },
        { "den".toCharArray(), new char[] { 146 } }, { "teng".toCharArray(), new char[] { 146 } }, { "ting".toCharArray(), new char[] { 146 } },
        { "na".toCharArray(), new char[] { 147 } }, { "wa".toCharArray(), new char[] { 147 } }, { "ha".toCharArray(), new char[] { 147 } },
        { "dai".toCharArray(), new char[] { 148 } }, { "kuang".toCharArray(), new char[] { 149 } }, { "kuan".toCharArray(), new char[] { 149 } },
        { "juan".toCharArray(), new char[] { 149 } }, { "chuang".toCharArray(), new char[] { 149 } }, { "quan".toCharArray(), new char[] { 150 } },
        { "ci".toCharArray(), new char[] { 151 } }, { "mu".toCharArray(), new char[] { 152 } }, { "kou".toCharArray(), new char[] { 153 } },
        { "kuo".toCharArray(), new char[] { 153 } }, { "jiu".toCharArray(), new char[] { 154 } }, { "wan".toCharArray(), new char[] { 155 } },
        { "huan".toCharArray(), new char[] { 156 } }, { "min".toCharArray(), new char[] { 157 } }, { "ning".toCharArray(), new char[] { 157 } },
        { "nin".toCharArray(), new char[] { 157 } }, { "ni".toCharArray(), new char[] { 158 } }, { "pi".toCharArray(), new char[] { 159 } },
        { "hao".toCharArray(), new char[] { 160 } }, { "nao".toCharArray(), new char[] { 160 } }, { "chun".toCharArray(), new char[] { 161 } },
        { "dun".toCharArray(), new char[] { 161 } }, { "tun".toCharArray(), new char[] { 161 } }, { "cai".toCharArray(), new char[] { 162 } },
        { "han".toCharArray(), new char[] { 163 } }, { "chuan".toCharArray(), new char[] { 164 } }, { "te".toCharArray(), new char[] { 165 } },
        { "tui".toCharArray(), new char[] { 165 } }, { "dei".toCharArray(), new char[] { 165 } }, { "tei".toCharArray(), new char[] { 165 } },
        { "mi".toCharArray(), new char[] { 166 } }, { "gou".toCharArray(), new char[] { 167 } }, { "po".toCharArray(), new char[] { 167 } },
        { "pou".toCharArray(), new char[] { 167 } }, { "wo".toCharArray(), new char[] { 168 } }, { "fo".toCharArray(), new char[] { 168 } },
        { "ou".toCharArray(), new char[] { 168 } }, { "o".toCharArray(), new char[] { 168 } }, { "yo".toCharArray(), new char[] { 168 } },
        { "long".toCharArray(), new char[] { 169 } }, { "lou".toCharArray(), new char[] { 170 } }, { "rou".toCharArray(), new char[] { 170 } },
        { "mou".toCharArray(), new char[] { 170 } }, { "nou".toCharArray(), new char[] { 170 } }, { "lo".toCharArray(), new char[] { 170 } },
        { "nuo".toCharArray(), new char[] { 170 } }, { "ruo".toCharArray(), new char[] { 170 } }, { "cun".toCharArray(), new char[] { 171 } },
        { "chan".toCharArray(), new char[] { 172 } }, { "gua".toCharArray(), new char[] { 172 } }, { "kua".toCharArray(), new char[] { 172 } },
        { "shua".toCharArray(), new char[] { 172 } }, { "zhua".toCharArray(), new char[] { 172 } }, { "chua".toCharArray(), new char[] { 172 } },
        { "mian".toCharArray(), new char[] { 173 } }, { "chi".toCharArray(), new char[] { 174 } }, { "yue".toCharArray(), new char[] { 175 } },
        { "nv".toCharArray(), new char[] { 175 } }, { "lue".toCharArray(), new char[] { 175 } }, { "nue".toCharArray(), new char[] { 175 } },
        { "xu".toCharArray(), new char[] { 176 } }, { "gui".toCharArray(), new char[] { 177 } }, { "zhang".toCharArray(), new char[] { 178 } },
        { "ru".toCharArray(), new char[] { 179 } }, { "nu".toCharArray(), new char[] { 179 } }, { "ming".toCharArray(), new char[] { 180 } },
        { "tang".toCharArray(), new char[] { 181 } }, { "lun".toCharArray(), new char[] { 182 } }, { "run".toCharArray(), new char[] { 182 } },
        { "hun".toCharArray(), new char[] { 182 } }, { "yao".toCharArray(), new char[] { 183 } }, { "heng".toCharArray(), new char[] { 184 } },
        { "hen".toCharArray(), new char[] { 184 } }, { "leng".toCharArray(), new char[] { 184 } }, { "reng".toCharArray(), new char[] { 184 } },
        { "weng".toCharArray(), new char[] { 184 } }, { "man".toCharArray(), new char[] { 185 } }, { "ran".toCharArray(), new char[] { 185 } },
        { "pu".toCharArray(), new char[] { 186 } }, { "ling".toCharArray(), new char[] { 187 } }, { "la".toCharArray(), new char[] { 188 } },
        { "can".toCharArray(), new char[] { 189 } }, { "zan".toCharArray(), new char[] { 189 } }, { "cang".toCharArray(), new char[] { 189 } },
        { "zang".toCharArray(), new char[] { 189 } }, { "sang".toCharArray(), new char[] { 189 } }, { "zuo".toCharArray(), new char[] { 190 } },
        { "yun".toCharArray(), new char[] { 191 } }, { "zhuan".toCharArray(), new char[] { 192 } }, { "shuan".toCharArray(), new char[] { 192 } },
        { "zuan".toCharArray(), new char[] { 192 } }, { "cuan".toCharArray(), new char[] { 192 } }, { "zu".toCharArray(), new char[] { 193 } },
        { "pai".toCharArray(), new char[] { 194 } }, { "gai".toCharArray(), new char[] { 194 } }, { "shuo".toCharArray(), new char[] { 195 } },
        { "zhuo".toCharArray(), new char[] { 195 } }, { "chuo".toCharArray(), new char[] { 195 } }, { "cuo".toCharArray(), new char[] { 195 } },
        { "chou".toCharArray(), new char[] { 195 } }, { "zou".toCharArray(), new char[] { 195 } }, { "sou".toCharArray(), new char[] { 195 } },
        { "cou".toCharArray(), new char[] { 195 } }, { "hou".toCharArray(), new char[] { 196 } }, { "fou".toCharArray(), new char[] { 196 } },
        { "jun".toCharArray(), new char[] { 197 } }, { "qiang".toCharArray(), new char[] { 198 } }, { "lai".toCharArray(), new char[] { 199 } },
        { "nai".toCharArray(), new char[] { 199 } }, { "chong".toCharArray(), new char[] { 200 } }, { "mao".toCharArray(), new char[] { 201 } },
        { "gang".toCharArray(), new char[] { 202 } }, { "kang".toCharArray(), new char[] { 203 } }, { "kan".toCharArray(), new char[] { 203 } },
        { "duan".toCharArray(), new char[] { 204 } }, { "xie".toCharArray(), new char[] { 205 } }, { "xuan".toCharArray(), new char[] { 206 } },
        { "tuan".toCharArray(), new char[] { 206 } }, { "zhai".toCharArray(), new char[] { 207 } }, { "chai".toCharArray(), new char[] { 207 } },
        { "shai".toCharArray(), new char[] { 207 } }, { "sai".toCharArray(), new char[] { 207 } }, { "lan".toCharArray(), new char[] { 208 } },
        { "shuang".toCharArray(), new char[] { 209 } }, { "lao".toCharArray(), new char[] { 210 } }, { "rao".toCharArray(), new char[] { 210 } },
        { "jue".toCharArray(), new char[] { 211 } }, { "que".toCharArray(), new char[] { 211 } }, { "tiao".toCharArray(), new char[] { 212 } },
        { "diao".toCharArray(), new char[] { 212 } }, { "mai".toCharArray(), new char[] { 213 } }, { "shun".toCharArray(), new char[] { 214 } },
        { "zhun".toCharArray(), new char[] { 214 } }, { "sun".toCharArray(), new char[] { 214 } }, { "zun".toCharArray(), new char[] { 214 } },
        { "zhui".toCharArray(), new char[] { 215 } }, { "chui".toCharArray(), new char[] { 215 } }, { "zei".toCharArray(), new char[] { 215 } },
        { "zhei".toCharArray(), new char[] { 215 } }, { "shei".toCharArray(), new char[] { 215 } }, { "ze".toCharArray(), new char[] { 215 } },
        { "ben".toCharArray(), new char[] { 216 } }, { "qiao".toCharArray(), new char[] { 217 } }, { "piao".toCharArray(), new char[] { 217 } },
        { "zha".toCharArray(), new char[] { 218 } }, { "sa".toCharArray(), new char[] { 218 } }, { "za".toCharArray(), new char[] { 218 } },
        { "ca".toCharArray(), new char[] { 218 } }, { "tan".toCharArray(), new char[] { 219 } }, { "sha".toCharArray(), new char[] { 220 } },
        { "neng".toCharArray(), new char[] { 221 } }, { "nen".toCharArray(), new char[] { 221 } }, { "meng".toCharArray(), new char[] { 221 } },
        { "xun".toCharArray(), new char[] { 222 } }, { "qun".toCharArray(), new char[] { 222 } }, { "gun".toCharArray(), new char[] { 222 } },
        { "kun".toCharArray(), new char[] { 222 } }, { "cha".toCharArray(), new char[] { 223 } }, { "ta".toCharArray(), new char[] { 224 } },
        { "pa".toCharArray(), new char[] { 224 } }, { "cao".toCharArray(), new char[] { 225 } }, { "ceng".toCharArray(), new char[] { 226 } },
        { "cen".toCharArray(), new char[] { 226 } }, { "zeng".toCharArray(), new char[] { 226 } }, { "zen".toCharArray(), new char[] { 226 } },
        { "sen".toCharArray(), new char[] { 226 } }, { "seng".toCharArray(), new char[] { 226 } }, { "lie".toCharArray(), new char[] { 227 } },
        { "nie".toCharArray(), new char[] { 227 } }, { "mie".toCharArray(), new char[] { 227 } }, { "tie".toCharArray(), new char[] { 228 } },
        { "die".toCharArray(), new char[] { 228 } }, { "zhe".toCharArray(), new char[] { 229 } }, { "kai".toCharArray(), new char[] { 230 } },
        { "le".toCharArray(), new char[] { 231 } }, { "me".toCharArray(), new char[] { 231 } }, { "ne".toCharArray(), new char[] { 231 } },
        { "dui".toCharArray(), new char[] { 232 } }, { "kuai".toCharArray(), new char[] { 233 } }, { "guai".toCharArray(), new char[] { 233 } },
        { "shuai".toCharArray(), new char[] { 233 } }, { "chuai".toCharArray(), new char[] { 233 } }, { "zhuai".toCharArray(), new char[] { 233 } },
        { "huai".toCharArray(), new char[] { 233 } }, { "biao".toCharArray(), new char[] { 234 } }, { "suo".toCharArray(), new char[] { 235 } },
        { "song".toCharArray(), new char[] { 236 } }, { "cong".toCharArray(), new char[] { 236 } }, { "ri".toCharArray(), new char[] { 237 } },
        { "rui".toCharArray(), new char[] { 237 } }, { "zhan".toCharArray(), new char[] { 238 } }, { "lei".toCharArray(), new char[] { 239 } },
        { "zong".toCharArray(), new char[] { 240 } }, { "qiu".toCharArray(), new char[] { 241 } }, { "zai".toCharArray(), new char[] { 242 } },
        { "zao".toCharArray(), new char[] { 243 } }, { "sao".toCharArray(), new char[] { 243 } }, { "nei".toCharArray(), new char[] { 244 } },
        { "tuo".toCharArray(), new char[] { 245 } }, { "ku".toCharArray(), new char[] { 246 } }, { "cu".toCharArray(), new char[] { 246 } },
        { "ruan".toCharArray(), new char[] { 247 } }, { "luan".toCharArray(), new char[] { 247 } }, { "nuan".toCharArray(), new char[] { 247 } },
        { "xiong".toCharArray(), new char[] { 248 } }, { "qiong".toCharArray(), new char[] { 248 } }, { "jiong".toCharArray(), new char[] { 248 } }, };
    final Comparator<char[][]> sorter = new Comparator<char[][]>() {
      @Override
      public int compare(char[][] o1, char[][] o2) {
        if (o1[0].length != o2[0].length) {
          return o2[0].length - o1[0].length;
        } else {
          return o2[0][0] - o1[0][0];
        }
      }
    };
    Arrays.sort(SuperIndexGenerator.metaphoneMapping, sorter);
    Arrays.sort(SuperIndexGenerator.pinyinEncMapping, sorter);
  }

  public final static byte[] createPhonetecIdx(final String str) {
    final String strSpelling = SuperIndexGenerator.toPhoneticSpelling(str);

    // use metaphone (soundex) algorithm to modify string
    final String strMetaphoned = SuperIndexGenerator.replaceCharacters(strSpelling, SuperIndexGenerator.getMetaphoneMapping());
    // replace pinyin phonetic codecs
    final String strEncoded = SuperIndexGenerator.replaceCharacters(strMetaphoned, SuperIndexGenerator.getPinyinEncodingMapping());

    // System.out.println(strEncoded.toString());
    return SuperIndexGenerator.toBytes(strEncoded.toCharArray());
  }

  private static String replaceCharacters(final String strSpelling, final char[][][] mapping) {
    final char[] chars = strSpelling.toCharArray();
    final int len = chars.length;

    StringBuilder sb = new StringBuilder(len);
    char last = (char) -1;
    for (int i = 0; i < len; i++) {
      char current = chars[i];
      int idxFound = -1;
      for (int j = 0; j < mapping.length; j++) {
        final char[] m = mapping[j][0];
        if ((len - i) >= m.length) {
          boolean found = true;
          for (int k = 0; k < m.length; k++) {
            if (m[k] != chars[i + k]) {
              found = false;
              break;
            }
          }
          if (found) {
            idxFound = j;
            break;
          }
        }
      }
      if (idxFound != -1) {
        final char[] m = mapping[idxFound][0];
        final char[] r = mapping[idxFound][1];
        // System.out.println(new String(m) + " -> " + new String(r));
        i += m.length - 1;
        sb.append(r);
        last = (char) -1;
        continue;
      }
      if (current == last) {
        continue;
      } else {
        sb.append(current);
        last = current;
      }
    }
    return sb.toString();
  }

  private static final byte[] BUFFER = new byte[SuperIndexGenerator.LEN_SRC_KEY];

  private static byte[] toBytes(char[] str) {
    final int len = Math.min(SuperIndexGenerator.LEN_SRC_KEY, str.length);
    int c = 0;
    for (int i = 0; i < len; i++) {
      byte b1 = (byte) ((str[i] & 0xff00) >> 8);
      byte b2 = (byte) str[i];
      if (b1 != 0) {
        SuperIndexGenerator.BUFFER[c++] = b1;
        if (c == SuperIndexGenerator.LEN_SRC_KEY) {
          break;
        }
      }
      SuperIndexGenerator.BUFFER[c++] = b2;
      if (c == SuperIndexGenerator.LEN_SRC_KEY) {
        break;
      }
    }
    final byte[] r = new byte[c];
    System.arraycopy(SuperIndexGenerator.BUFFER, 0, r, 0, c);
    return r;
  }

  // Tries to make word to latin string
  private static String toPhoneticSpelling(String str) {
    // clean, change v here, as v is used as 'ue' later
    String s = str.toLowerCase();
    if (((s.charAt(0) == '(') && (s.charAt(s.length() - 1) == ')')) || ((s.charAt(0) == '[') && (s.charAt(s.length() - 1) == ']'))) {
      s = s.substring(1, s.length() - 1);
    }
    s = s.replaceAll("\\s|(\\(.+?\\))|(\\[.+?\\])|[\\+\\-\\*/%=\\?,\\.:;、。，？；：《》【】!~^_\\|&#\"'´`{}「」\\(\\)<>\\[\\]\\\\]", "");
    // System.out.println(s);
    // to pinyin
    s = PhoneticTranscriptionHelper.getPinyin(s);
    // System.out.println(s);
    s = s.replace('v', 'f');
    return s;
  }

}
