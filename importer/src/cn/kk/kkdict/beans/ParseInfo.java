package cn.kk.kkdict.beans;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.kk.kkdict.extraction.dict.WiktionaryPagesMetaCurrentExtractor;
import cn.kk.kkdict.types.Language;
import cn.kk.kkdict.utils.ChineseHelper;
import cn.kk.kkdict.utils.Helper;

public class ParseInfo {

  private String               fVal;

  private int                  defsCount;

  private final List<String>   vals = new ArrayList<>();

  private final List<String>   lngs = new ArrayList<>();

  private Language             tgtLng;

  private String               tgtVal;

  private Language             defLng;

  private final Language       fLng;

  private String               defVal;

  private final BufferedWriter out;

  public ParseInfo(BufferedWriter out, Language fLng) {
    this.out = out;
    this.fLng = fLng;
  }

  public void write() throws IOException {
    final int size = this.lngs.size();
    if (size > 1) {
      for (int i = 0; i < size; i++) {
        if (i > 0) {
          this.out.write(Helper.SEP_LIST);
        }
        String key = this.lngs.get(i);
        String val = this.vals.get(i);
        this.out.write(key);
        this.out.write(Helper.SEP_DEFINITION);
        this.out.write(val.replaceAll("^([,\\.，。；?!！？\\|\\-\\+\\* ]+)|([,\\.，。；?!！？\\|\\-\\+\\* ]+$)|('[']+)", "").trim());
      }
      this.out.write(Helper.SEP_NEWLINE);
      this.setDefsCount(this.getDefsCount() + 1);
    }
    this.clearNow();
  }

  public void clear() throws IOException {
    this.write();
  }

  private void clearNow() {
    this.vals.clear();
    this.lngs.clear();
    this.setfVal(null);
    this.setTgtVal(null);
    this.setTgtLng(null);
  }

  public void addTitleTgt() {
    if ((this.getTgtVal() != null) && Helper.isNotEmptyOrNull(this.getTgtVal()) && (this.getTgtLng() != null) && (this.defLng != null) && (this.defVal != null)
        && (this.defLng != this.getTgtLng())) {
      if (!this.lngs.contains(this.getDefLng().getKey())) {
        this.lngs.add(this.getDefLng().getKey());
        this.vals.add(this.getDefVal());
      }
      this.lngs.add(this.getTgtLng().getKey());
      this.vals.add(this.getTgtVal());
    } else if (WiktionaryPagesMetaCurrentExtractor.DEBUG) {
      System.err.println(this.getDefLng() + "=" + this.getDefVal() + ", " + this.getTgtLng() + "=" + this.getTgtVal());
    }
  }

  public void addTitlefLng() {
    if ((this.getfVal() != null) && Helper.isNotEmptyOrNull(this.getfVal()) && (this.getfLng() != null) && (this.defLng != null) && (this.defVal != null)
        && (this.defLng != this.getTgtLng())) {
      if (!this.lngs.contains(this.getDefLng().getKey())) {
        this.lngs.add(this.getDefLng().getKey());
        this.vals.add(this.getDefVal());
      }
      this.lngs.add(this.getfLng().getKey());
      this.vals.add(this.getfVal());
    } else if (WiktionaryPagesMetaCurrentExtractor.DEBUG) {
      System.err.println(this.getDefLng() + "=" + this.getDefVal() + ", " + this.getfLng() + "=" + this.getfVal());
    }
  }

  public String getDefVal() {
    if (this.getDefLng() == null) {
      return null;
    }
    if (this.getDefLng().getId() == 1) {
      return ChineseHelper.toSimplifiedChinese(this.defVal);
    } else {
      return this.defVal;
    }
  }

  public void setDefVal(String defVal) throws IOException {
    this.write();
    this.defVal = defVal;
  }

  public Language getDefLng() {
    return this.defLng;
  }

  public void setDefLng(Language defLng) throws IOException {
    this.write();
    this.defLng = defLng;
  }

  public String getfVal() {
    if (this.getfLng() == null) {
      return null;
    }
    if (this.getfLng().getId() == 1) {
      return ChineseHelper.toSimplifiedChinese(this.fVal);
    } else {
      return this.fVal;
    }
  }

  public void setfVal(String fVal) {
    this.fVal = fVal;
  }

  public Language getfLng() {
    return this.fLng;
  }

  public String getTgtVal() {
    if (this.getTgtLng() == null) {
      return null;
    }
    if (this.getTgtLng().getId() == 1) {
      return ChineseHelper.toSimplifiedChinese(this.tgtVal);
    } else {
      return this.tgtVal;
    }
  }

  public void setTgtVal(String tgtVal) {
    this.tgtVal = tgtVal;
  }

  public int getDefsCount() {
    return this.defsCount;
  }

  public void setDefsCount(int defsCount) {
    this.defsCount = defsCount;
  }

  public Language getTgtLng() {
    return this.tgtLng;
  }

  public void setTgtLng(Language tgtLng) {
    this.tgtLng = tgtLng;
  }

}
