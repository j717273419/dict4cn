package cn.kk.dict2go.lib;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public final class SearchEngine {

  private final static Timer                  timer     = new Timer();

  private final static ExecutorService        esIndex   = Executors.newFixedThreadPool(1);

  private final static ExecutorService        esData    = Executors.newFixedThreadPool(1);

  final static List<SearchTask>               tasks     = new ArrayList<>(5);

  final static List<SearchResultListener>     listeners = new ArrayList<>(5);

  final static LinkedList<Context>            ctx       = new LinkedList<>();

  final static LinkedList<SearchTaskCallable> callables = new LinkedList<>();

  static Semaphore                            ctxLock;

  public static Context borrowContext() {
    synchronized (SearchEngine.ctx) {
      try {
        SearchEngine.ctxLock.acquire();
        return SearchEngine.ctx.getFirst();
      } catch (InterruptedException e) {
        e.printStackTrace();
        return null;
      }
    }
  }

  public static void giveBack(Context context) {
    synchronized (SearchEngine.ctx) {
      SearchEngine.ctx.push(context);
      SearchEngine.ctxLock.release();
    }
  }

  public static SearchTaskCallable borrowCallable() {
    synchronized (SearchEngine.callables) {
      if (SearchEngine.callables.isEmpty()) {
        return new SearchTaskCallable();
      } else {
        return SearchEngine.callables.getFirst();
      }
    }
  }

  public static void giveBack(SearchTaskCallable callable) {
    synchronized (SearchEngine.callables) {
      if (SearchEngine.callables.size() < 20) {
        SearchEngine.callables.push(callable);
      }
    }
  }

  public static void main(String[] args) {
    final int[] lngs = { 2, 3, 4, 5, 6, 7, 8, 9 };
    SearchEngine.initialize(1, lngs, 200);
    final Context c = SearchEngine.borrowContext();
    SearchEngine.find(c, "Moià", lngs);
    SearchEngine.shutdown();
  }

  public static void startSearchTask(SearchType type, final Context searchContext, final String txtInput) {
    final SearchTask task = new SearchTask(type, searchContext, txtInput, SearchEngine.borrowCallable());
    synchronized (SearchEngine.tasks) {
      for (final SearchTask t : SearchEngine.tasks) {
        t.cancel();
      }
    }
    SearchEngine.execute(task);
  }

  static void execute(final SearchTask task) {
    synchronized (SearchEngine.tasks) {
      SearchEngine.tasks.add(task);
    }
    if ((task.type == SearchType.SEARCH) || (task.type == SearchType.DEEP_SEARCH)) {
      SearchEngine.esIndex.execute(task);
    } else {
      // TODO
      SearchEngine.esIndex.execute(task);
    }
  }

  public static Timer getTimer() {
    return SearchEngine.timer;
  }

  public static void addListener(final SearchResultListener l) {
    SearchEngine.listeners.add(l);
  }

  public static ExecutorService getExecutor() {
    return SearchEngine.esData;
  }

  static void executeListTask(final Context context) {
    SearchTask t = new SearchTask(SearchType.LIST, context, SearchEngine.borrowCallable());
    SearchEngine.execute(t);
  }

  public static void initialize(final int maxCtxs, final int[] selectedLngIds, int waitCheckResult) {
    try {
      SearchEngine.ctxLock = new Semaphore(maxCtxs);
      Dictionary.loadMeta();
      Dictionary.loadCachedIndexes();

      for (int i = 0; i < maxCtxs; i++) {
        final Context c = new Context();
        SearchEngine.ctx.push(c);
        SearchEngine.setLanguages(c, selectedLngIds);
      }

      SearchEngine.getTimer().schedule(new TimerTask() {
        private final List<SearchTask> done    = new ArrayList<>(5);
        private final List<SearchTask> changed = new ArrayList<>(5);

        @Override
        public void run() {
          synchronized (SearchEngine.tasks) {
            for (final SearchTask t : SearchEngine.tasks) {
              if (t.isFinished()) {
                this.done.add(t);
              }
              if (t.changed) {
                t.changed = false;
                this.changed.add(t);
              }
            }
            for (SearchTask t : this.changed) {
              System.out.println("changed: " + t.key + ": " + t.getResult());

              final List<Translation> trls = t.getResult().getTranslations();
              synchronized (trls) {
                for (final SearchResultListener l : SearchEngine.listeners) {
                  if ((l.getType() == t.type) || (l.getType() == SearchType.ALL)) {
                    try {
                      l.onResultChanged(t);
                    } catch (final Exception e) {
                      System.err.println("Exception in listener: " + e.toString());
                      e.printStackTrace();
                    }
                  }
                }
              }
            }
            if (!this.done.isEmpty()) {
              SearchEngine.tasks.removeAll(this.done);
              for (SearchTask t : this.done) {
                SearchEngine.giveBack(t.callable);
              }
            }
          }
          this.done.clear();
          this.changed.clear();
        }
      }, 1000, waitCheckResult);
    } catch (IOException e) {
      System.err.println("Failed to start search engine: " + e.toString());
      e.printStackTrace();
    }
  }

  public static int[] getLanguageIds(final List<String> selected) {
    final int[] lngs = new int[selected.size()];
    int i = 0;
    for (String lng : selected) {
      lngs[i++] = Language.from(lng);
    }
    return lngs;
  }

  public static void setLanguages(final Context context, final int[] selectedLngs) {
    context.updateUserLanguages(selectedLngs);
  }

  public static int[] getDefinitionCounts() {
    return Dictionary.getDefinitionCounts();
  }

  public static void shutdown() {
    Dictionary.close();
    SearchEngine.timer.cancel();
    SearchEngine.esData.shutdownNow();
    SearchEngine.esIndex.shutdownNow();
  }

  public static final void initializeSearch(final Context context, final String input) {
    context.setInput(input);
  }

  // test only
  public static final void find(final Context context, final String input, int[] lngs) {
    SearchEngine.initializeSearch(context, input);
    final int cacheStartIdx = Dictionary.findCachedStartIdx(context);

    System.out.println("input: " + context.inputLower + " (" + context.inputKey + ")");
    System.out.println("cache start idx: " + cacheStartIdx + ", " + Dictionary.readIndexKey(cacheStartIdx));

    // finds exacts and starts with (~ 2ms)
    // long start = System.currentTimeMillis();
    Dictionary.findExactAndStartsWithIndexes(context, cacheStartIdx);
    // System.out.println(System.currentTimeMillis() - start);

    Dictionary.readTranslations(context);
    Collections.sort(context.trls, new TranslationComparator(context, lngs));
    SearchEngine.printResult(context.trls);
    System.out.println("=== deep search ===");

    // finds ends with and contains (~ 80ms/mb)
    // start = System.currentTimeMillis();
    Dictionary.findContainsAndEndsWithIndexes(context);
    // System.out.println(System.currentTimeMillis() - start);

    // System.out.println(Dictionary.readIndexKey(cacheStartIdx));
    System.out.println("result: " + context.searchResult.getIdxResult());

    Dictionary.readTranslations(context);
    Collections.sort(context.trls, new TranslationComparator(context, lngs));
    SearchEngine.printResult(context.trls);

    // Index[] getSortedIndexesForFindMode(Context.lngs, IndexResult) // srclng,
    // tgtlng, exact, startsWith, endsWith, contains
    // Translation[] loadTranslationsByIndexesAndFilter(Context.input,
    // IndexResult) // filter trls AND idxResult by srcVal containing input

  }

  protected static void printResult(List<Translation> trls) {
    System.out.println("list trls: ");
    for (Translation trl : trls) {
      System.out.println(trl);
    }
  }
}
