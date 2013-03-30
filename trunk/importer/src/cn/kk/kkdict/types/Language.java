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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;

import cn.kk.kkdict.utils.Helper;
// http://wikokit.googlecode.com/svn-history/r766/trunk/common_wiki/src/wikokit/base/wikipedia/language/LanguageType.java
// NOTICE: final version, do not change anything.

public enum Language implements KeyType<Language> {
  AA("aa", LanguageFamily.NONE, 236),
  AAK("aak", LanguageFamily.NONE),
  AAO("aao", LanguageFamily.ARABIC),
  AB("ab", LanguageFamily.NONE, 225),
  ABE("abe", LanguageFamily.NONE, 122),
  ABH("abh", LanguageFamily.ARABIC),
  ABM("abm", LanguageFamily.NONE),
  ABQ("abq", LanguageFamily.NONE),
  ABS("abs", LanguageFamily.AUSTRONESIAN),
  ABV("abv", LanguageFamily.ARABIC),
  ACE("ace", LanguageFamily.NONE, 176),
  ACH("ach", LanguageFamily.NONE),
  ACM("acm", LanguageFamily.ARABIC),
  ACU("acu", LanguageFamily.NONE),
  ACV("acv", LanguageFamily.NONE),
  ACW("acw", LanguageFamily.ARABIC),
  ACX("acx", LanguageFamily.ARABIC),
  ACY("acy", LanguageFamily.ARABIC),
  ADA("ada", LanguageFamily.NONE),
  ADE("ade", LanguageFamily.NONE),
  ADF("adf", LanguageFamily.ARABIC),
  // ADJ("adj", LanguageFamily.NONE),
  ADT("adt", LanguageFamily.NONE),
  ADY("ady", LanguageFamily.NONE),
  AE("ae", LanguageFamily.NONE),
  AF("af", LanguageFamily.NONE, 78),
  AGG("agg", LanguageFamily.NONE),
  AGJ("agj", LanguageFamily.NONE),
  AGT("agt", LanguageFamily.AUSTRONESIAN),
  AGV("agv", LanguageFamily.AUSTRONESIAN),
  AGX("agx", LanguageFamily.NONE),
  AGY("agy", LanguageFamily.AUSTRONESIAN),
  AGZ("agz", LanguageFamily.AUSTRONESIAN),
  AHH("ahh", LanguageFamily.NONE),
  AIB("aib", LanguageFamily.NONE),
  AII("aii", LanguageFamily.NONE),
  AIW("aiw", LanguageFamily.NONE),
  AJI("aji", LanguageFamily.NONE),
  AJP("ajp", LanguageFamily.ARABIC),
  AJT("ajt", LanguageFamily.ARABIC),
  AJU("aju", LanguageFamily.ARABIC),
  AK("ak", LanguageFamily.NONE),
  AKE("ake", LanguageFamily.NONE),
  AKK("akk", LanguageFamily.NONE),
  AKL("akl", LanguageFamily.AUSTRONESIAN),
  AKZ("akz", LanguageFamily.NONE),
  ALE("ale", LanguageFamily.NONE),
  ALI("ali", LanguageFamily.NONE),
  ALQ("alq", LanguageFamily.NONE),
  ALR("alr", LanguageFamily.NONE),
  ALS("als", LanguageFamily.NONE, 104),
  ALU("alu", LanguageFamily.NONE),
  ALY("aly", LanguageFamily.NONE),
  AM("am", LanguageFamily.NONE, 105),
  AMK("amk", LanguageFamily.NONE),
  AMN("amn", LanguageFamily.NONE),
  AN("an", LanguageFamily.NONE, 68),
  AND("and", LanguageFamily.NONE),
  ANG("ang", LanguageFamily.NONE),
  ANI("ani", LanguageFamily.NONE),
  ANO("ano", LanguageFamily.NONE),
  AOA("aoa", LanguageFamily.NONE),
  APD("apd", LanguageFamily.ARABIC),
  APJ("apj", LanguageFamily.APACHE),
  APK("apk", LanguageFamily.APACHE),
  APL("apl", LanguageFamily.APACHE),
  APM("apm", LanguageFamily.APACHE),
  APW("apw", LanguageFamily.APACHE),
  APY("apy", LanguageFamily.NONE),
  AQC("aqc", LanguageFamily.NONE),
  AR("ar", LanguageFamily.ARABIC, 25),
  ARB("arb", LanguageFamily.ARABIC, 246),
  ARC("arc", LanguageFamily.NONE, 187),
  ARL("arl", LanguageFamily.NONE),
  ARN("arn", LanguageFamily.NONE),
  ARP("arp", LanguageFamily.NONE),
  ARQ("arq", LanguageFamily.ARABIC),
  ARS("ars", LanguageFamily.ARABIC),
  ARW("arw", LanguageFamily.NONE),
  ARY("ary", LanguageFamily.ARABIC),
  ARZ("arz", LanguageFamily.ARABIC, 203),
  AS("as", LanguageFamily.NONE, 223),
  ASE("ase", LanguageFamily.NONE),
  AST("ast", LanguageFamily.NONE, 89),
  AUS_SYD("aus_syd", LanguageFamily.NONE),
  AUZ("auz", LanguageFamily.ARABIC),
  AV("av", LanguageFamily.NONE, 230),
  AVL("avl", LanguageFamily.ARABIC),
  AWA("awa", LanguageFamily.NONE),
  AWK("awk", LanguageFamily.NONE),
  AWS("aws", LanguageFamily.AWYU),
  AWU("awu", LanguageFamily.AWYU),
  AWV("awv", LanguageFamily.AWYU),
  AWW("aww", LanguageFamily.AWYU),
  AWY("awy", LanguageFamily.AWYU),
  AY("ay", LanguageFamily.NONE, 199),
  AYH("ayh", LanguageFamily.ARABIC),
  AYN("ayn", LanguageFamily.ARABIC),
  AYP("ayp", LanguageFamily.ARABIC),
  AZ("az", LanguageFamily.AZERI, 82),
  AZG("azg", LanguageFamily.NONE),
  AZJ("azj", LanguageFamily.AZERI),
  AZR("azr", LanguageFamily.NONE),
  BA("ba", LanguageFamily.NONE, 94),
  BAL("bal", LanguageFamily.NONE, 135),
  BAN("ban", LanguageFamily.NONE),
  BAR("bar", LanguageFamily.NONE, 134),
  BAT_SMG("bat_smg", LanguageFamily.NONE, 101),
  BCL("bcl", LanguageFamily.NONE, 133),
  BE("be", LanguageFamily.NONE, 55),
  BEJ("bej", LanguageFamily.NONE),
  BEM("bem", LanguageFamily.NONE),
  BEW("bew", LanguageFamily.NONE),
  BE_X_OLD("be_x_old", LanguageFamily.NONE, 55),
  BG("bg", LanguageFamily.NONE, 34),
  BH("bh", LanguageFamily.NONE, 153),
  BHO("bho", LanguageFamily.NONE),
  BI("bi", LanguageFamily.NONE, 247),
  BIK("bik", LanguageFamily.NONE),
  BJN("bjn", LanguageFamily.NONE, 215),
  BLA("bla", LanguageFamily.NONE),
  BM("bm", LanguageFamily.NONE, 252),
  BN("bn", LanguageFamily.NONE, 76),
  BO("bo", LanguageFamily.NONE, 141),
  BPY("bpy", LanguageFamily.NONE, 72),
  BR("br", LanguageFamily.NONE, 56),
  BS("bs", LanguageFamily.NONE, 66),
  BUG("bug", LanguageFamily.NONE, 108),
  BXR("bxr", LanguageFamily.NONE),
  CA("ca", LanguageFamily.NONE, 16),
  CAD("cad", LanguageFamily.NONE),
  CBK_ZAM("cbk_zam", LanguageFamily.NONE, 192),
  CDO("cdo", LanguageFamily.CHINESE, 110),
  CE("ce", LanguageFamily.NONE, 207),
  CEB("ceb", LanguageFamily.NONE, 57),
  CH("ch", LanguageFamily.NONE),
  CHC("chc", LanguageFamily.NONE),
  CHG("chg", LanguageFamily.NONE),
  CHH("chh", LanguageFamily.NONE),
  CHN("chn", LanguageFamily.NONE),
  CHO("cho", LanguageFamily.NONE),
  CHR("chr", LanguageFamily.NONE, 214),
  CHY("chy", LanguageFamily.NONE, 245),
  CIC("cic", LanguageFamily.NONE),
  CJY("cjy", LanguageFamily.CHINESE, 1),
  CKB("ckb", LanguageFamily.NONE, 123),
  CKC("ckc", LanguageFamily.CAKCHIQUEL),
  CKD("ckd", LanguageFamily.CAKCHIQUEL),
  CKE("cke", LanguageFamily.CAKCHIQUEL),
  CKF("ckf", LanguageFamily.CAKCHIQUEL),
  CKI("cki", LanguageFamily.CAKCHIQUEL),
  CKJ("ckj", LanguageFamily.CAKCHIQUEL),
  CKK("ckk", LanguageFamily.CAKCHIQUEL),
  CKU("cku", LanguageFamily.NONE),
  CLM("clm", LanguageFamily.SALISHAN),
  CMN("cmn", LanguageFamily.CHINESE, 1),
  CO("co", LanguageFamily.NONE, 125),
  COC("coc", LanguageFamily.NONE),
  COM("com", LanguageFamily.NONE),
  CPG("cpg", LanguageFamily.NONE),
  CPX("cpx", LanguageFamily.CHINESE, 1),
  CR("cr", LanguageFamily.CREE),
  CRH("crh", LanguageFamily.NONE, 197),
  CRJ("crj", LanguageFamily.CREE),
  CRK("crk", LanguageFamily.CREE),
  CRL("crl", LanguageFamily.CREE),
  CRM("crm", LanguageFamily.CREE),
  CS("cs", LanguageFamily.NONE, 19),
  CSB("csb", LanguageFamily.NONE, 151),
  CSW("csw", LanguageFamily.CREE),
  CU("cu", LanguageFamily.NONE, 227),
  CV("cv", LanguageFamily.NONE, 98),
  CWD("cwd", LanguageFamily.CREE),
  CY("cy", LanguageFamily.NONE, 62),
  CZH("czh", LanguageFamily.CHINESE, 1),
  CZO("czo", LanguageFamily.CHINESE, 1),
  DA("da", LanguageFamily.NONE, 26),
  DAK("dak", LanguageFamily.NONE),
  DAR("dar", LanguageFamily.NONE),
  DE("de", LanguageFamily.NONE, 3),
  DIN("din", LanguageFamily.NONE),
  DIQ("diq", LanguageFamily.NONE, 93),
  DLM("dlm", LanguageFamily.NONE, 242),
  DNG("dng", LanguageFamily.SINO_TIBETAN),
  DSB("dsb", LanguageFamily.NONE, 189),
  DUA("dua", LanguageFamily.NONE),
  DUL("dul", LanguageFamily.NONE),
  DV("dv", LanguageFamily.NONE, 165),
  DZ("dz", LanguageFamily.NONE),
  EE("ee", LanguageFamily.NONE, 235),
  EFI("efi", LanguageFamily.NONE),
  EL("el", LanguageFamily.NONE, 42),
  EML("eml", LanguageFamily.NONE, 194),
  EMS("ems", LanguageFamily.NONE),
  EN("en", LanguageFamily.NONE, 2),
  ENM("enm", LanguageFamily.NONE),
  EO("eo", LanguageFamily.NONE, 27),
  ES("es", LanguageFamily.NONE, 9),
  ET("et", LanguageFamily.NONE, 81),
  ETT("ett", LanguageFamily.NONE),
  EU("eu", LanguageFamily.NONE, 36),
  EVN("evn", LanguageFamily.NONE),
  EXT("ext", LanguageFamily.NONE, 166),
  FA("fa", LanguageFamily.INDO_EUROPEAN, 24),
  FAN("fan", LanguageFamily.NONE),
  FF("ff", LanguageFamily.NONE),
  FFM("ffm", LanguageFamily.NIGER_CONGO),
  FI("fi", LanguageFamily.URALIC, 18),
  FIL("fil", LanguageFamily.AUSTRONESIAN),
  FIT("fit", LanguageFamily.URALIC),
  FIU_VRO("fiu_vro", LanguageFamily.NONE, 111),
  FJ("fj", LanguageFamily.NONE, 217),
  FKV("fkv", LanguageFamily.URALIC),
  FO("fo", LanguageFamily.INDO_EUROPEAN, 132),
  FOS("fos", LanguageFamily.AUSTRONESIAN),
  FR("fr", LanguageFamily.INDO_EUROPEAN, 4),
  FRC("frc", LanguageFamily.INDO_EUROPEAN),
  FRK("frk", LanguageFamily.INDO_EUROPEAN),
  FRM("frm", LanguageFamily.INDO_EUROPEAN),
  FRO("fro", LanguageFamily.INDO_EUROPEAN),
  FRP("frp", LanguageFamily.INDO_EUROPEAN, 168),
  FRR("frr", LanguageFamily.INDO_EUROPEAN, 170),
  FRS("frs", LanguageFamily.INDO_EUROPEAN),
  FUB("fub", LanguageFamily.NIGER_CONGO),
  FUE("fue", LanguageFamily.NIGER_CONGO),
  FUH("fuh", LanguageFamily.NIGER_CONGO),
  FUI("fui", LanguageFamily.NIGER_CONGO),
  FUQ("fuq", LanguageFamily.NIGER_CONGO),
  FUR("fur", LanguageFamily.INDO_EUROPEAN, 160),
  FUV("fuv", LanguageFamily.NIGER_CONGO),
  FY("fy", LanguageFamily.INDO_EUROPEAN, 71),
  GA("ga", LanguageFamily.NONE, 97),
  GAA("gaa", LanguageFamily.NONE),
  GAG("gag", LanguageFamily.TURKIC, 208),
  GAN("gan", LanguageFamily.CHINESE, 120),
  GAY("gay", LanguageFamily.NONE),
  GD("gd", LanguageFamily.NONE, 107),
  GIL("gil", LanguageFamily.NONE),
  GL("gl", LanguageFamily.NONE, 83),
  GLK("glk", LanguageFamily.NONE),
  GMH("gmh", LanguageFamily.NONE, 3),
  GML("gml", LanguageFamily.NONE, 3),
  GN("gn", LanguageFamily.NONE, 184),
  GNI("gni", LanguageFamily.NONE),
  GOH("goh", LanguageFamily.NONE, 3),
  GOT("got", LanguageFamily.NONE, 238),
  GRC("grc", LanguageFamily.NONE),
  GSW("gsw", LanguageFamily.GERMAN, 3),
  GU("gu", LanguageFamily.NONE, 79),
  GUL("gul", LanguageFamily.NONE),
  GUN("gun", LanguageFamily.NONE),
  GV("gv", LanguageFamily.NONE, 137),
  GWC("gwc", LanguageFamily.NONE),
  GWI("gwi", LanguageFamily.NONE),
  HA("ha", LanguageFamily.NONE, 243),
  HAI("hai", LanguageFamily.NONE),
  HAK("hak", LanguageFamily.CHINESE, 155),
  HAW("haw", LanguageFamily.NONE, 173),
  HE("he", LanguageFamily.NONE, 33),
  HI("hi", LanguageFamily.NONE, 40),
  HIF("hif", LanguageFamily.NONE, 140),
  HIT("hit", LanguageFamily.NONE),
  HLU("hlu", LanguageFamily.NONE),
  HMC("hmc", LanguageFamily.HMONG),
  HMD("hmd", LanguageFamily.HMONG),
  HME("hme", LanguageFamily.HMONG),
  HMF("hmf", LanguageFamily.HMONG),
  HMG("hmg", LanguageFamily.HMONG),
  HMH("hmh", LanguageFamily.HMONG),
  HMI("hmi", LanguageFamily.HMONG),
  HMJ("hmj", LanguageFamily.HMONG),
  HML("hml", LanguageFamily.HMONG),
  HMN("hmn", LanguageFamily.HMONG),
  HMP("hmp", LanguageFamily.HMONG),
  HMQ("hmq", LanguageFamily.HMONG),
  HMS("hms", LanguageFamily.HMONG),
  HMU("hmu", LanguageFamily.HMONG),
  HMV("hmv", LanguageFamily.HMONG),
  HMW("hmw", LanguageFamily.HMONG),
  HMY("hmy", LanguageFamily.HMONG),
  HMZ("hmz", LanguageFamily.HMONG),
  HO("ho", LanguageFamily.NONE),
  HR("hr", LanguageFamily.NONE, 39),
  HSB("hsb", LanguageFamily.NONE, 131),
  HSN("hsn", LanguageFamily.CHINESE),
  HT("ht", LanguageFamily.NONE, 51),
  HU("hu", LanguageFamily.NONE, 20),
  HY("hy", LanguageFamily.NONE, 70),
  HZ("hz", LanguageFamily.NONE),
  IA("ia", LanguageFamily.NONE, 99),
  ID("id", LanguageFamily.NONE, 21),
  IE("ie", LanguageFamily.NONE, 171),
  IG("ig", LanguageFamily.NONE, 226),
  II("ii", LanguageFamily.NONE),
  IK("ik", LanguageFamily.NONE),
  IKE("ike", LanguageFamily.INUKTITUT),
  IKT("ikt", LanguageFamily.INUKTITUT),
  ILO("ilo", LanguageFamily.NONE, 143),
  INE_PRO("ine_pro", LanguageFamily.NONE),
  INH("inh", LanguageFamily.NONE),
  IO("io", LanguageFamily.NONE, 77),
  IS("is", LanguageFamily.NONE, 65),
  IT("it", LanguageFamily.NONE, 5),
  IU("iu", LanguageFamily.INUKTITUT, 228),
  IW("iw", LanguageFamily.NONE),
  JA("ja", LanguageFamily.NONE, 6),
  JAM("jam", LanguageFamily.NONE),
  JBO("jbo", LanguageFamily.NONE, 113),
  JV("jv", LanguageFamily.NONE, 60),
  JYE("jye", LanguageFamily.ARABIC),
  KA("ka", LanguageFamily.NONE, 48),
  KAA("kaa", LanguageFamily.NONE, 234),
  KAB("kab", LanguageFamily.NONE, 237),
  KBD("kbd", LanguageFamily.NONE, 222),
  KCN("kcn", LanguageFamily.NONE),
  KFL("kfl", LanguageFamily.NONE),
  KG("kg", LanguageFamily.NONE, 233),
  KI("ki", LanguageFamily.NONE, 253),
  KJ("kj", LanguageFamily.NONE),
  KJU("kju", LanguageFamily.NONE),
  KK("kk", LanguageFamily.NONE, 35),
  KKY("kky", LanguageFamily.NONE),
  KL("kl", LanguageFamily.NONE, 186),
  KM("km", LanguageFamily.NONE, 190),
  KN("kn", LanguageFamily.NONE, 106),
  KO("ko", LanguageFamily.NONE, 7),
  KOI("koi", LanguageFamily.NONE, 191),
  KOK("kok", LanguageFamily.NONE),
  KPY("kpy", LanguageFamily.NONE),
  KR("kr", LanguageFamily.NONE),
  KRC("krc", LanguageFamily.NONE, 182),
  KRI("kri", LanguageFamily.NONE),
  KRL("krl", LanguageFamily.NONE),
  KS("ks", LanguageFamily.NONE),
  KSH("ksh", LanguageFamily.NONE, 175),
  KU("ku", LanguageFamily.NONE, 88),
  KUD("kud", LanguageFamily.NONE),
  KV("kv", LanguageFamily.NONE, 146),
  KW("kw", LanguageFamily.NONE, 158),
  KY("ky", LanguageFamily.NONE, 142),
  LA("la", LanguageFamily.NONE, 41),
  LAD("lad", LanguageFamily.NONE, 167),
  LB("lb", LanguageFamily.NONE, 64),
  LBE("lbe", LanguageFamily.NONE, 218),
  LG("lg", LanguageFamily.NONE),
  LI("li", LanguageFamily.NONE, 128),
  LIJ("lij", LanguageFamily.NONE, 156),
  LIV("liv", LanguageFamily.NONE),
  LKT("lkt", LanguageFamily.NONE),
  LLD("lld", LanguageFamily.NONE),
  LMO("lmo", LanguageFamily.NONE, 69),
  LN("ln", LanguageFamily.NONE, 188),
  LO("lo", LanguageFamily.NONE, 205),
  LSY("lsy", LanguageFamily.NONE),
  LT("lt", LanguageFamily.NONE, 29),
  LTC("ltc", LanguageFamily.CHINESE, 1),
  LTG("ltg", LanguageFamily.NONE, 206),
  LV("lv", LanguageFamily.NONE, 58),
  LZH("lzh", LanguageFamily.CHINESE, 1),
  MAD("mad", LanguageFamily.NONE),
  MAP_BMS("map_bms", LanguageFamily.NONE, 102),
  MDF("mdf", LanguageFamily.NONE, 220),
  MFE("mfe", LanguageFamily.NONE),
  MG("mg", LanguageFamily.NONE, 61),
  MH("mh", LanguageFamily.NONE),
  MHR("mhr", LanguageFamily.NONE, 145),
  MI("mi", LanguageFamily.NONE, 159),
  MIN("min", LanguageFamily.NONE),
  MIQ("miq", LanguageFamily.NONE),
  MK("mk", LanguageFamily.NONE, 49),
  ML("ml", LanguageFamily.NONE, 73),
  MN("mn", LanguageFamily.NONE, 117),
  MNC("mnc", LanguageFamily.NONE, 201),
  MNP("mnp", LanguageFamily.CHINESE, 1),
  MO("mo", LanguageFamily.NONE, 249),
  MOH("moh", LanguageFamily.NONE),
  MR("mr", LanguageFamily.NONE, 63),
  MRC("mrc", LanguageFamily.NONE),
  MRJ("mrj", LanguageFamily.NONE, 148),
  MRV("mrv", LanguageFamily.NONE),
  MS("ms", LanguageFamily.NONE, 32),
  MSD("msd", LanguageFamily.NONE),
  MT("mt", LanguageFamily.NONE, 177),
  MUS("mus", LanguageFamily.NONE),
  MWF("mwf", LanguageFamily.NONE),
  MWL("mwl", LanguageFamily.NONE, 200),
  MWP("mwp", LanguageFamily.NONE),
  MY("my", LanguageFamily.NONE, 96),
  MYP("myp", LanguageFamily.NONE),
  MYV("myv", LanguageFamily.NONE, 196),
  MZN("mzn", LanguageFamily.NONE, 136),
  NA("na", LanguageFamily.NONE, 224),
  NAH("nah", LanguageFamily.NONE, 118),
  NAN("nan", LanguageFamily.CHINESE, 110),
  NAP("nap", LanguageFamily.NONE, 100),
  NAQ("naq", LanguageFamily.NONE),
  NB("nb", LanguageFamily.NORSK_BOKMÅL),
  NDS("nds", LanguageFamily.NONE, 86),
  NDS_NL("nds_nl", LanguageFamily.NONE, 112),
  NE("ne", LanguageFamily.NONE, 85),
  NEW("new", LanguageFamily.NONE, 44),
  NG("ng", LanguageFamily.NONE),
  NJB("njb", LanguageFamily.NAGA),
  NJH("njh", LanguageFamily.NAGA),
  NJM("njm", LanguageFamily.NAGA),
  NJN("njn", LanguageFamily.NAGA),
  NJO("njo", LanguageFamily.NAGA),
  NL("nl", LanguageFamily.NONE, 12),
  NMN("nmn", LanguageFamily.NONE),
  NN("nn", LanguageFamily.NONE, 84),
  NO("no", LanguageFamily.NONE, 17),
  NON("non", LanguageFamily.NONE, 250),
  NOV("nov", LanguageFamily.NONE, 162),
  NRM("nrm", LanguageFamily.NONE, 138),
  NRN("nrn", LanguageFamily.NONE),
  NSO("nso", LanguageFamily.NONE, 232),
  NV("nv", LanguageFamily.NONE, 183),
  NY("ny", LanguageFamily.NONE),
  OC("oc", LanguageFamily.NONE, 47),
  OCH("och", LanguageFamily.CHINESE, 1),
  ODT("odt", LanguageFamily.NONE),
  OFS("ofs", LanguageFamily.FRIESISCH),
  OJ("oj", LanguageFamily.NONE),
  OM("om", LanguageFamily.NONE),
  OOD("ood", LanguageFamily.NONE, 116),
  OR("or", LanguageFamily.NONE, 185),
  OS("os", LanguageFamily.NONE, 202),
  OSC("osc", LanguageFamily.NONE),
  OSX("osx", LanguageFamily.NONE),
  PA("pa", LanguageFamily.NONE, 150),
  PAG("pag", LanguageFamily.NONE, 239),
  PAM("pam", LanguageFamily.NONE, 124),
  PAP("pap", LanguageFamily.NONE, 198),
  PAU("pau", LanguageFamily.NONE),
  PCD("pcd", LanguageFamily.NONE, 180),
  PDC("pdc", LanguageFamily.NONE, 216),
  PFL("pfl", LanguageFamily.NONE, 204),
  PHN("phn", LanguageFamily.NONE),
  PI("pi", LanguageFamily.NONE, 157),
  PIH("pih", LanguageFamily.NONE, 251),
  PJT("pjt", LanguageFamily.NONE),
  PL("pl", LanguageFamily.INDO_EUROPEAN, 10),
  PMS("pms", LanguageFamily.NONE, 52),
  PNB("pnb", LanguageFamily.NONE, 74),
  PNT("pnt", LanguageFamily.NONE, 231),
  PRG("prg", LanguageFamily.NONE),
  PRO("pro", LanguageFamily.NONE),
  PRP("prp", LanguageFamily.NONE),
  PRS("prs", LanguageFamily.NONE),
  PS("ps", LanguageFamily.NONE, 163),
  PT("pt", LanguageFamily.NONE, 11),
  QU("qu", LanguageFamily.QUECHUA, 92),
  QUB("qub", LanguageFamily.QUECHUA),
  QUC("quc", LanguageFamily.QUICHÉ),
  QUD("qud", LanguageFamily.QUICHUA),
  QUF("quf", LanguageFamily.QUECHUA),
  QUG("qug", LanguageFamily.QUICHUA),
  QUH("quh", LanguageFamily.QUECHUA),
  QUJ("quj", LanguageFamily.QUICHÉ),
  QUK("quk", LanguageFamily.QUECHUA),
  QUL("qul", LanguageFamily.QUECHUA),
  QUP("qup", LanguageFamily.QUECHUA),
  QUR("qur", LanguageFamily.QUECHUA),
  QUS("qus", LanguageFamily.QUICHUA),
  QUT("qut", LanguageFamily.QUICHÉ),
  QUU("quu", LanguageFamily.QUICHÉ),
  QUW("quw", LanguageFamily.QUICHUA),
  QUX("qux", LanguageFamily.QUECHUA),
  QUY("quy", LanguageFamily.QUECHUA),
  QUZ("quz", LanguageFamily.QUECHUA),
  QVA("qva", LanguageFamily.QUECHUA),
  QVC("qvc", LanguageFamily.QUECHUA),
  QVE("qve", LanguageFamily.QUECHUA),
  QVH("qvh", LanguageFamily.QUECHUA),
  QVI("qvi", LanguageFamily.QUICHUA),
  QVJ("qvj", LanguageFamily.QUICHUA),
  QVL("qvl", LanguageFamily.QUECHUA),
  QVM("qvm", LanguageFamily.QUECHUA),
  QVN("qvn", LanguageFamily.QUECHUA),
  QVO("qvo", LanguageFamily.QUECHUA),
  QVP("qvp", LanguageFamily.QUECHUA),
  QVS("qvs", LanguageFamily.QUECHUA),
  QVW("qvw", LanguageFamily.QUECHUA),
  QVZ("qvz", LanguageFamily.QUICHUA),
  QWA("qwa", LanguageFamily.QUECHUA),
  QWC("qwc", LanguageFamily.QUECHUA),
  QWH("qwh", LanguageFamily.QUECHUA),
  QWS("qws", LanguageFamily.QUECHUA),
  QXA("qxa", LanguageFamily.QUECHUA),
  QXC("qxc", LanguageFamily.QUECHUA),
  QXH("qxh", LanguageFamily.QUECHUA),
  QXI("qxi", LanguageFamily.QUICHÉ),
  QXL("qxl", LanguageFamily.QUICHUA),
  QXN("qxn", LanguageFamily.QUECHUA),
  QXO("qxo", LanguageFamily.QUECHUA),
  QXP("qxp", LanguageFamily.QUECHUA),
  QXR("qxr", LanguageFamily.QUICHUA),
  QXT("qxt", LanguageFamily.QUECHUA),
  QXU("qxu", LanguageFamily.QUECHUA),
  QXW("qxw", LanguageFamily.QUECHUA),
  RAR("rar", LanguageFamily.NONE),
  RHG("rhg", LanguageFamily.NONE),
  RM("rm", LanguageFamily.NONE, 181),
  RME("rme", LanguageFamily.NONE),
  RMN("rmn", LanguageFamily.NONE, 181),
  RMY("rmy", LanguageFamily.NONE, 181),
  RN("rn", LanguageFamily.NONE),
  RO("ro", LanguageFamily.NONE, 23),
  ROA_JER("roa_jer", LanguageFamily.NONE, 138),
  ROA_RUP("roa_rup", LanguageFamily.NONE, 46),
  ROA_TARA("roa_tara", LanguageFamily.NONE, 130),
  RU("ru", LanguageFamily.NONE, 8),
  RUE("rue", LanguageFamily.NONE, 149),
  RUO("ruo", LanguageFamily.NONE),
  RUP("rup", LanguageFamily.NONE, 211),
  RUQ("ruq", LanguageFamily.NONE),
  RW("rw", LanguageFamily.NONE, 195),
  RYU("ryu", LanguageFamily.NONE),
  SA("sa", LanguageFamily.NONE, 127),
  SAH("sah", LanguageFamily.NONE, 121),
  SAS("sas", LanguageFamily.NONE),
  SC("sc", LanguageFamily.NONE, 164),
  SCN("scn", LanguageFamily.NONE, 90),
  SCO("sco", LanguageFamily.NONE, 115),
  SD("sd", LanguageFamily.NONE, 248),
  SE("se", LanguageFamily.NONE, 126),
  SEI("sei", LanguageFamily.NONE),
  SEM_AMM("sem_amm", LanguageFamily.NONE),
  SG("sg", LanguageFamily.NONE),
  SGA("sga", LanguageFamily.NONE),
  SH("sh", LanguageFamily.NONE, 45),
  SHH("shh", LanguageFamily.NONE),
  SHU("shu", LanguageFamily.ARABIC),
  SI("si", LanguageFamily.NONE, 154),
  SIA("sia", LanguageFamily.NONE),
  SIM("sim", LanguageFamily.NONE),
  SIMPLE("simple", LanguageFamily.NONE, 2),
  SJD("sjd", LanguageFamily.NONE),
  SJE("sje", LanguageFamily.NONE),
  SJK("sjk", LanguageFamily.NONE),
  SJT("sjt", LanguageFamily.NONE),
  SJU("sju", LanguageFamily.NONE),
  SK("sk", LanguageFamily.NONE, 30),
  SL("sl", LanguageFamily.NONE, 31),
  SM("sm", LanguageFamily.NONE, 240),
  SMA("sma", LanguageFamily.NONE),
  SMJ("smj", LanguageFamily.NONE),
  SMN("smn", LanguageFamily.NONE),
  SMS("sms", LanguageFamily.NONE),
  SN("sn", LanguageFamily.NONE, 212),
  SO("so", LanguageFamily.NONE, 178),
  SOG("sog", LanguageFamily.NONE),
  SQ("sq", LanguageFamily.NONE, 59),
  SR("sr", LanguageFamily.NONE, 28),
  SRN("srn", LanguageFamily.NONE, 221),
  SS("ss", LanguageFamily.NONE, 255),
  ST("st", LanguageFamily.NONE, 241),
  STQ("stq", LanguageFamily.NONE, 161),
  SU("su", LanguageFamily.NONE, 91),
  SUL("sul", LanguageFamily.NONE),
  SUX("sux", LanguageFamily.NONE),
  SV("sv", LanguageFamily.NONE, 13),
  SVA("sva", LanguageFamily.NONE),
  SW("sw", LanguageFamily.NONE, 75),
  SYR("syr", LanguageFamily.NONE),
  SZL("szl", LanguageFamily.NONE, 179),
  TA("ta", LanguageFamily.NONE, 54),
  TAY("tay", LanguageFamily.NONE),
  TCS("tcs", LanguageFamily.NONE),
  TE("te", LanguageFamily.NONE, 53),
  TET("tet", LanguageFamily.NONE, 229),
  TFN("tfn", LanguageFamily.NONE),
  TG("tg", LanguageFamily.NONE, 109),
  TH("th", LanguageFamily.NONE, 43),
  TI("ti", LanguageFamily.NONE),
  TK("tk", LanguageFamily.NONE, 129),
  TKL("tkl", LanguageFamily.NONE),
  TL("tl", LanguageFamily.AUSTRONESIAN, 50),
  TLI("tli", LanguageFamily.NONE),
  TN("tn", LanguageFamily.NONE, 254),
  TO("to", LanguageFamily.NONE, 244),
  TOKI("toki", LanguageFamily.NONE),
  TPI("tpi", LanguageFamily.NONE, 193),
  TPN("tpn", LanguageFamily.NONE),
  TR("tr", LanguageFamily.NONE, 22),
  TS("ts", LanguageFamily.NONE),
  TSG("tsg", LanguageFamily.NONE),
  TT("tt", LanguageFamily.NONE, 95),
  TUM("tum", LanguageFamily.NONE),
  TVL("tvl", LanguageFamily.NONE),
  TW("tw", LanguageFamily.NONE),
  TXB("txb", LanguageFamily.NONE),
  TY("ty", LanguageFamily.NONE, 213),
  UDM("udm", LanguageFamily.NONE, 174),
  UG("ug", LanguageFamily.NONE, 147),
  UK("uk", LanguageFamily.NONE, 14),
  UR("ur", LanguageFamily.NONE, 87),
  UZ("uz", LanguageFamily.NONE, 80),
  VAI("vai", LanguageFamily.NONE),
  VE("ve", LanguageFamily.NONE),
  VEC("vec", LanguageFamily.NONE, 114),
  VEP("vep", LanguageFamily.NONE, 169),
  VI("vi", LanguageFamily.AUSTRO_ASIATIC, 15),
  VLS("vls", LanguageFamily.NONE, 144),
  VO("vo", LanguageFamily.NONE, 37),
  VRO("vro", LanguageFamily.NONE, 111),
  WA("wa", LanguageFamily.NONE, 103),
  WAM("wam", LanguageFamily.NONE),
  WAR("war", LanguageFamily.NONE, 38),
  WBP("wbp", LanguageFamily.NONE),
  WIM("wim", LanguageFamily.NONE),
  WO("wo", LanguageFamily.NONE, 210),
  WUU("wuu", LanguageFamily.CHINESE, 1),
  XAA("xaa", LanguageFamily.NONE),
  XAL("xal", LanguageFamily.NONE, 172),
  XAQ("xaq", LanguageFamily.NONE),
  XCG("xcg", LanguageFamily.NONE),
  XDC("xdc", LanguageFamily.NONE),
  XFA("xfa", LanguageFamily.NONE),
  XH("xh", LanguageFamily.NONE),
  XLC("xlc", LanguageFamily.NONE),
  XLD("xld", LanguageFamily.NONE),
  XLE("xle", LanguageFamily.NONE),
  XMF("xmf", LanguageFamily.NONE, 152),
  XMK("xmk", LanguageFamily.NONE),
  XNO("xno", LanguageFamily.NONE),
  XPG("xpg", LanguageFamily.NONE),
  XPI("xpi", LanguageFamily.NONE),
  XRN("xrn", LanguageFamily.NONE),
  XSS("xss", LanguageFamily.NONE),
  XTA("xta", LanguageFamily.NONE),
  XTO("xto", LanguageFamily.NONE),
  XUM("xum", LanguageFamily.NONE),
  XVO("xvo", LanguageFamily.NONE),
  YHD("yhd", LanguageFamily.ARABIC),
  YI("yi", LanguageFamily.NONE, 119),
  YIJ("yij", LanguageFamily.NONE),
  YO("yo", LanguageFamily.NONE, 67),
  YUE("yue", LanguageFamily.CHINESE, 1),
  ZA("za", LanguageFamily.NONE, 219),
  ZAA("zaa", LanguageFamily.ZAPOTEC),
  ZAB("zab", LanguageFamily.ZAPOTEC),
  ZAC("zac", LanguageFamily.ZAPOTEC),
  ZAD("zad", LanguageFamily.ZAPOTEC),
  ZAE("zae", LanguageFamily.ZAPOTEC),
  ZAF("zaf", LanguageFamily.ZAPOTEC),
  ZAI("zai", LanguageFamily.ZAPOTEC),
  ZAM("zam", LanguageFamily.ZAPOTEC),
  ZAO("zao", LanguageFamily.ZAPOTEC),
  ZAP("zap", LanguageFamily.ZAPOTEC),
  ZAQ("zaq", LanguageFamily.ZAPOTEC),
  ZAR("zar", LanguageFamily.ZAPOTEC),
  ZAS("zas", LanguageFamily.ZAPOTEC),
  ZAT("zat", LanguageFamily.ZAPOTEC),
  ZAV("zav", LanguageFamily.ZAPOTEC),
  ZAW("zaw", LanguageFamily.ZAPOTEC),
  ZAX("zax", LanguageFamily.ZAPOTEC),
  ZCA("zca", LanguageFamily.ZAPOTEC),
  ZEA("zea", LanguageFamily.NONE, 139),
  ZH("zh", LanguageFamily.CHINESE, 1),
  ZH_CLASSICAL("zh_classical", LanguageFamily.CHINESE, 1),
  ZH_MIN_NAN("zh_min_nan", LanguageFamily.CHINESE, 110),
  ZH_YUE("zh_yue", LanguageFamily.CHINESE, 1),
  ZKU("zku", LanguageFamily.NONE),
  ZOO("zoo", LanguageFamily.ZAPOTEC),
  ZPA("zpa", LanguageFamily.ZAPOTEC),
  ZPB("zpb", LanguageFamily.ZAPOTEC),
  ZPC("zpc", LanguageFamily.ZAPOTEC),
  ZPD("zpd", LanguageFamily.ZAPOTEC),
  ZPE("zpe", LanguageFamily.ZAPOTEC),
  ZPF("zpf", LanguageFamily.ZAPOTEC),
  ZPG("zpg", LanguageFamily.ZAPOTEC),
  ZPH("zph", LanguageFamily.ZAPOTEC),
  ZPI("zpi", LanguageFamily.ZAPOTEC),
  ZPJ("zpj", LanguageFamily.ZAPOTEC),
  ZPK("zpk", LanguageFamily.ZAPOTEC),
  ZPL("zpl", LanguageFamily.ZAPOTEC),
  ZPM("zpm", LanguageFamily.ZAPOTEC),
  ZPN("zpn", LanguageFamily.ZAPOTEC),
  ZPO("zpo", LanguageFamily.ZAPOTEC),
  ZPP("zpp", LanguageFamily.ZAPOTEC),
  ZPQ("zpq", LanguageFamily.ZAPOTEC),
  ZPR("zpr", LanguageFamily.ZAPOTEC),
  ZPS("zps", LanguageFamily.ZAPOTEC),
  ZPT("zpt", LanguageFamily.ZAPOTEC),
  ZPU("zpu", LanguageFamily.ZAPOTEC),
  ZPV("zpv", LanguageFamily.ZAPOTEC),
  ZPW("zpw", LanguageFamily.ZAPOTEC),
  ZPX("zpx", LanguageFamily.ZAPOTEC),
  ZPY("zpy", LanguageFamily.ZAPOTEC),
  ZPZ("zpz", LanguageFamily.ZAPOTEC),
  ZQE("zqe", LanguageFamily.ZAPOTEC),
  ZRO("zro", LanguageFamily.NONE),
  ZSR("zsr", LanguageFamily.ZAPOTEC),
  ZTE("zte", LanguageFamily.ZAPOTEC),
  ZTG("ztg", LanguageFamily.ZAPOTEC),
  ZTL("ztl", LanguageFamily.ZAPOTEC),
  ZTM("ztm", LanguageFamily.ZAPOTEC),
  ZTN("ztn", LanguageFamily.ZAPOTEC),
  ZTP("ztp", LanguageFamily.ZAPOTEC),
  ZTQ("ztq", LanguageFamily.ZAPOTEC),
  ZTS("zts", LanguageFamily.ZAPOTEC),
  ZTT("ztt", LanguageFamily.ZAPOTEC),
  ZTU("ztu", LanguageFamily.ZAPOTEC),
  ZTX("ztx", LanguageFamily.ZAPOTEC),
  ZTY("zty", LanguageFamily.ZAPOTEC),
  ZU("zu", LanguageFamily.NONE, 209),
  ZUN("zun", LanguageFamily.NONE),
  ZWA("zwa", LanguageFamily.NONE),
  ZZA("zza", LanguageFamily.NONE), ;

  public static final String                 TYPE_ID = "语";

  private int                                id;

  private final String                       key;

  private final byte[]                       keyBytes;

  public final LanguageFamily                family;

  private static final Map<String, Language> KEYS_MAP;

  private static final Language[]            VALUES;

  private boolean                            auto    = true;
  static {
    List<Language> values = new ArrayList<>(Arrays.asList(Language.values()));
    Collections.sort(values, new KeyTypeComparator<Language>());
    VALUES = new Language[values.size()];
    KEYS_MAP = new TreeMap<>();
    int i = 0;
    for (Language c : values) {
      if (c.id != -1) {
        i = c.id;
        c.auto = false;
      } else {
        c.id = i;
      }
      Language.KEYS_MAP.put(c.key, c);
      Language.VALUES[i++] = c;
    }
    Properties lngsAlt = new Properties();
    try {
      lngsAlt.load(Helper.findResourceAsStream("lng2alt.txt"));
      Enumeration<Object> keys = lngsAlt.keys();
      while (keys.hasMoreElements()) {
        String key = (String) keys.nextElement();
        String val = lngsAlt.getProperty(key);
        Language.KEYS_MAP.put(key, Language.KEYS_MAP.get(val));
      }
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static final Language fromId(final int id) {
    return Language.VALUES[id - 1];
  }

  public static final Language fromKey(final String key) {
    if (key == null) {
      return null;
    } else {
      return Language.KEYS_MAP.get(key.toLowerCase().replace('-', '_'));
    }
  }

  Language(final String key, final LanguageFamily family) {
    this(key, family, -1);
  }

  Language(final String key, final LanguageFamily family, final int id) {
    this.id = id;
    this.key = key;
    this.family = family;
    this.keyBytes = key.getBytes(Helper.CHARSET_UTF8);
  }

  public static void main(String[] args) throws IOException {
    Language.printWithName();
  }

  private void printWithId() throws IOException {
    Map<String, Language> lngNames = new HashMap<>();

    try (InputStream in = Language.class.getResourceAsStream("/lng2name_ZH.txt");) {
      Properties lngsAlt = new Properties();
      try (Reader r = new InputStreamReader(in, Helper.CHARSET_UTF8)) {
        lngsAlt.load(r);
        Enumeration<Object> keys = lngsAlt.keys();
        while (keys.hasMoreElements()) {
          String key = (String) keys.nextElement();
          String val = lngsAlt.getProperty(key);
          lngNames.put(key, Language.KEYS_MAP.get(val));
        }
      } catch (IllegalArgumentException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    List<Language> lngs = Language.getSortedLanguages();

    for (Language lng : lngs) {
      if (lng.id > 0) {
        System.out.print(lng.id + "=" + lng.key + (lng.auto ? " (*): " : ": "));
        for (Entry<String, Language> e : lngNames.entrySet()) {
          if (e.getValue() == lng) {
            System.out.print(e.getKey() + ",");
          }
        }
        System.out.println();
      }
    }

    for (Language lng : Language.VALUES) {
      if (lng == null) {
        break;
      }
      System.out.println(lng.id + "=" + lng.key + (lng.auto ? " (*): " : ": "));
      for (Entry<String, Language> e : Language.KEYS_MAP.entrySet()) {
        if (e.getValue() == lng) {
          System.out.print(e.getKey() + ",");
        }
      }
      System.out.println();
    }
  }

  private static void printWithName() throws IOException {
    Map<String, Language> lngNames = new HashMap<>();

    try (InputStream in = Language.class.getResourceAsStream("/lng2name_ZH.txt");) {
      Properties lngsAlt = new Properties();
      try (Reader r = new InputStreamReader(in, Helper.CHARSET_UTF8)) {
        lngsAlt.load(r);
        Enumeration<Object> keys = lngsAlt.keys();
        while (keys.hasMoreElements()) {
          String key = (String) keys.nextElement();
          String val = lngsAlt.getProperty(key);
          lngNames.put(key, Language.KEYS_MAP.get(val));
        }
      } catch (IllegalArgumentException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    List<Language> lngs = Language.getSortedLanguages();

    for (Language lng : lngs) {
      if (lng.id > 0) {
        System.out.print(lng.key + "=");
        for (Entry<String, Language> e : lngNames.entrySet()) {
          if (e.getValue() == lng) {
            System.out.print(e.getKey() + ",");
          }
        }
        System.out.println();
      }
    }

    for (Language lng : Language.VALUES) {
      if (lng == null) {
        break;
      }
      System.out.println(lng.key + "=");
      for (Entry<String, Language> e : Language.KEYS_MAP.entrySet()) {
        if (e.getValue() == lng) {
          System.out.print(e.getKey() + ",");
        }
      }
      System.out.println();
    }
  }

  public final static List<Language> getSortedLanguages() {
    List<Language> lngs = Arrays.asList(Language.values());
    Collections.sort(lngs, new Comparator<Language>() {
      @Override
      public int compare(Language o1, Language o2) {
        return o1.id - o2.id;
      }
    });
    return lngs;
  }

  @Override
  public int getId() {
    return this.id;
  }

  @Override
  public String getKey() {
    return this.key;
  }

  @Override
  public byte[] getKeyBytes() {
    return this.keyBytes;
  }

}
