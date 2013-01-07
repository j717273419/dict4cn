package cn.kk.dict2go;

import java.io.IOException;
import java.util.Arrays;

import cn.kk.kkdict.database.SuperIndexGenerator;

public class AppService {
  private final IndexKey inputKey;

  public static void main(String[] args) throws IOException {
    AppService svc = new AppService();
    AppService.initialize();
    for (int i = 2; i < 9; i++) {
      Context.lngs[i - 2] = i;
    }

    Context.updateUserLanguages();
    Context.input = "Moià";
    svc.find();
  }

  public AppService() {
    this.inputKey = new IndexKey();
  }

  public static void shutdown() {
    Dictionary.close();
  }

  private static void initialize() throws IOException {
    Context.change(Step.init);
    Dictionary.loadMeta();
    Dictionary.loadCachedIndexes();
    Context.change(Step.find);
  }

  public void find() {
    final byte[] inputKeyBytes = SuperIndexGenerator.createPhonetecIdx(Context.input);
    this.inputKey.setKey(inputKeyBytes);
    final int cacheStartIdx = Dictionary.findCachedStartIdx(this.inputKey);
    System.out.println("input: " + Context.input + " (" + this.inputKey + ")");
    System.out.println("cache start idx: " + cacheStartIdx + ", " + Dictionary.readIndexKey(cacheStartIdx));
    Context.searchResult.clear();
    // finds exacts and starts with (~ 2ms)
    // long start = System.currentTimeMillis();
    Dictionary.findExactAndStartsWithIndexes(this.inputKey, cacheStartIdx);
    // System.out.println(System.currentTimeMillis() - start);

    final Translation[] lstd = Dictionary.readTranslations();
    Arrays.sort(lstd);
    AppService.printResult(lstd);
    System.out.println("=== deep search ===");

    // finds ends with and contains (~ 80ms/mb)
    // start = System.currentTimeMillis();
    Dictionary.findContainsAndEndsWithIndexes(this.inputKey);
    // System.out.println(System.currentTimeMillis() - start);

    // System.out.println(Dictionary.readIndexKey(cacheStartIdx));
    System.out.println("result: " + Context.searchResult.getIdxResult());

    final Translation[] lst = Dictionary.readTranslations();
    Arrays.sort(lst);
    AppService.printResult(lst);

    // Index[] getSortedIndexesForFindMode(Context.lngs, IndexResult) // srclng,
    // tgtlng, exact, startsWith, endsWith, contains
    // Translation[] loadTranslationsByIndexesAndFilter(Context.input,
    // IndexResult) // filter trls AND idxResult by srcVal containing input

  }

  protected static void printResult(Translation[] lst) {
    System.out.println("list trls: ");
    for (int i = 0; i < lst.length; i++) {
      Translation trl = lst[i];
      System.out.println(trl);
    }
  }
}
