package cn.kk.dict2go.lib;

public class IndexResult {
  private static final int MAX_RESULTS = 200;

  private final IntList    idxResult;

  boolean                  dirty;

  public IndexResult() {
    this.idxResult = new IntList(100);
  }

  public void clear() {
    this.dirty = true;
    this.idxResult.clear();
  }

  public int getIdxResult(int i) {
    return this.idxResult.get(i);
  }

  public boolean addIdxResult(int i) {
    if (this.idxResult.size() < IndexResult.MAX_RESULTS) {
      this.idxResult.add(i);
      return true;
    } else {
      return false;
    }
  }

  public IntList getIdxResult() {
    return this.idxResult;
  }

}
