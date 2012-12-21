package cn.kk.dict2go;

import java.io.IOException;

import cn.kk.kkdict.database.SuperIndexGenerator;

public class AppService
{
  private Dictionary dict;

  private final IndexKey inputKey;


  public static void main(String[] args) throws IOException
  {
    AppService svc = new AppService();
    svc.initialize();
    Context.input = "schule";
    svc.find();
  }


  public AppService()
  {
    this.inputKey = new IndexKey();
  }


  public void shutdown()
  {
    Dictionary.close();
  }


  private void initialize() throws IOException
  {
    Context.change(Step.init);
    Dictionary.loadMeta();
    Dictionary.loadCachedIndexes();
    Context.change(Step.find);
  }


  public void find()
  {
    final byte[] inputKeyBytes = SuperIndexGenerator.createPhonetecIdx(Context.input);
    this.inputKey.setKey(inputKeyBytes);
    final int cacheStartIdx = Dictionary.findCachedStartIdx(this.inputKey);
    System.out.println("input: " + Context.input + " (" + this.inputKey + ")");
    System.out.println("cache start idx: " + cacheStartIdx);

    Context.searchResult.clear();
    // finds exacts and starts with (~ 2ms)
    // long start = System.currentTimeMillis();
    Dictionary.findExactAndStartsWithIndexes(this.inputKey, cacheStartIdx);
    // System.out.println(System.currentTimeMillis() - start);

    // finds ends with and contains (~ 80ms/mb)
    // start = System.currentTimeMillis();
    Dictionary.findContainsAndEndsWithIndexes(this.inputKey);
    // System.out.println(System.currentTimeMillis() - start);

    // System.out.println(Dictionary.readIndexKey(cacheStartIdx));
    System.out.println("equals: " + Context.searchResult.idxExacts);
    System.out.println("starts: " + Context.searchResult.idxStartsWiths);
    System.out.println("ends: " + Context.searchResult.idxEndsWiths);
    System.out.println("contains: " + Context.searchResult.idxContains);

    // this.printResultSeparate();
    // IndexResult findIndexesByKey(Context.sortedLngs, IndexResult, IndexKey)
    // // idxEndWiths, idxContains
    // Index[] getSortedIndexesForFindMode(Context.lngs, IndexResult) // srclng,
    // tgtlng, exact, startsWith, endsWith, contains
    // Translation[] loadTranslationsByIndexesAndFilter(Context.input,
    // IndexResult) // filter trls AND idxResult by srcVal containing input

  }


  private void printResultSeparate()
  {
    System.out.println("exacts: ");
    for (int i = 0; i < Context.searchResult.idxExacts.size(); i++)
    {
      int idxData = Context.searchResult.idxExacts.get(i);
      try
      {
        System.out.println(Dictionary.readDataKey(idxData));
      } catch (IOException e)
      {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    System.out.println("starts with: ");
    for (int i = 0; i < Context.searchResult.idxStartsWiths.size(); i++)
    {
      int idxData = Context.searchResult.idxStartsWiths.get(i);
      try
      {
        System.out.println(Dictionary.readDataKey(idxData));
      } catch (IOException e)
      {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    System.out.println("ends with: ");
    for (int i = 0; i < Context.searchResult.idxEndsWiths.size(); i++)
    {
      int idxData = Context.searchResult.idxEndsWiths.get(i);
      try
      {
        System.out.println(Dictionary.readDataKey(idxData));
      } catch (IOException e)
      {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    System.out.println("contains: ");
    for (int i = 0; i < Context.searchResult.idxContains.size(); i++)
    {
      int idxData = Context.searchResult.idxContains.get(i);
      try
      {
        System.out.println(Dictionary.readDataKey(idxData));
      } catch (IOException e)
      {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }
}
