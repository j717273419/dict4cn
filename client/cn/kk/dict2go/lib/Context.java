package cn.kk.dict2go.lib;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class Context {
  final int[]             usrLngs      = new int[8];

  final IndexKey          inputKey     = new IndexKey();

  String                  input;

  String                  inputLower;

  int                     selectedIdx;

  final IndexResult       searchResult = new IndexResult();

  final List<Translation> trls         = new ArrayList<>(Dictionary.MAX_INDEXES_CACHED);

  boolean                 doneSearch;
  boolean                 doneDeepSearch;

  public boolean isUserLanguage(final int lngTest) {
    if (lngTest == Language.ZH) {
      return true;
    } else {
      return (this.usrLngs[lngTest / 32] & (1 << (lngTest % 32))) > 0;
    }
  }

  public final void updateUserLanguages(final int[] selectedLngs) {
    Arrays.fill(this.usrLngs, 0);
    for (int lngId : selectedLngs) {
      this.usrLngs[lngId / 32] = this.usrLngs[lngId / 32] | (1 << (lngId % 32));
    }
  }

  public void setInput(final String input) {
    this.clear();
    this.input = input;
    this.inputLower = input.toLowerCase();
    this.inputKey.setKeyBytes(SuperIndexGenerator.createPhonetecIdx(this.inputLower));
  }

  private void clear() {
    this.doneSearch = false;
    this.doneDeepSearch = false;
    synchronized (this.searchResult) {
      this.searchResult.clear();
    }
    synchronized (this.trls) {
      for (Translation trl : this.trls) {
        ClientHelper.giveBack(trl);
      }
      this.trls.clear();
    }
  }

  public List<Translation> getTranslations() {
    return this.trls;
  }

  public boolean isValid(String val) {
    return val.toLowerCase().contains(this.inputLower);
  }

}
