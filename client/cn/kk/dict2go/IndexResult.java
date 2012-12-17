package cn.kk.dict2go;

import cn.kk.kkdict.beans.IntList;

public class IndexResult {
  private static final int MAX_RESULTS = 200;

  private final IndexKey[] indexes;
  final IntList            idxExacts;
  final IntList            idxStartsWiths;
  final IntList            idxEndsWiths;
  final IntList            idxContains;
  boolean                  dirty;

  public IndexResult() {
    this.indexes = new IndexKey[IndexResult.MAX_RESULTS];
    this.idxExacts = new IntList(50);
    this.idxStartsWiths = new IntList(100);
    this.idxEndsWiths = new IntList(100);
    this.idxContains = new IntList(100);

    for (int i = 0; i < this.indexes.length; i++) {
      this.indexes[i] = new IndexKey();
    }
  }

  public void clear() {
    this.idxExacts.clear();
    this.idxStartsWiths.clear();
    this.idxEndsWiths.clear();
    this.idxContains.clear();

  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

}
