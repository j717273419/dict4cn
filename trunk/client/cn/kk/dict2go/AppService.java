package cn.kk.dict2go;

import java.io.IOException;

import cn.kk.kkdict.database.SuperIndexGenerator;

public class AppService {
  private Dictionary     dict;
  private final CacheKey inputKey;

  public static void main(String[] args) throws IOException {
    AppService svc = new AppService();
    svc.initialize();
    Context.input = "hallo";
    svc.find();
  }

  public AppService() {
    this.inputKey = new CacheKey();
  }

  private void initialize() throws IOException {
    Context.change(Step.init);
    this.dict = new Dictionary();
    this.dict.loadMeta();
    this.dict.loadCachedIndexes();
    Context.change(Step.find);
  }

  public void find() {
    final byte[] inputKeyBytes = SuperIndexGenerator.createPhonetecIdx(Context.input);
    this.inputKey.setKey(inputKeyBytes);
    final int cacheStartIdx = this.dict.findCachedStartIdx(this.inputKey);
    System.out.println(cacheStartIdx);
    // int findStartIdxInCachedIndex(IndexKey)
    // IndexResult findIndexesByStartIdx(Context.sortedLngs, IndexResult, IndexKey, int) // idxExacts, idxStartsWiths
    // IndexResult findIndexesByKey(Context.sortedLngs, IndexResult, IndexKey) // idxEndWiths, idxContains
    // Index[] getSortedIndexesForFindMode(Context.lngs, IndexResult) // srclng, tgtlng, exact, startsWith, endsWith, contains
    // Translation[] loadTranslationsByIndexesAndFilter(Context.input, IndexResult) // filter trls AND idxResult by srcVal containing input

  }
}
