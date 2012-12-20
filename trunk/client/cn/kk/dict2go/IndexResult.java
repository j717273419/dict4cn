package cn.kk.dict2go;

import cn.kk.kkdict.beans.IntList;

public class IndexResult
{
  private static final int MAX_RESULTS = 200;

  final IntList idxExacts;

  final IntList idxStartsWiths;

  final IntList idxEndsWiths;

  final IntList idxContains;

  boolean dirty;


  public IndexResult()
  {
    this.idxExacts = new IntList(50);
    this.idxStartsWiths = new IntList(100);
    this.idxEndsWiths = new IntList(100);
    this.idxContains = new IntList(100);
  }


  public void clear()
  {
    dirty = true;
    this.idxExacts.clear();
    this.idxStartsWiths.clear();
    this.idxEndsWiths.clear();
    this.idxContains.clear();
  }


}
