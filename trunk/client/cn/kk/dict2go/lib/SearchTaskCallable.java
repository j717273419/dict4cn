package cn.kk.dict2go.lib;

import java.util.concurrent.Callable;

public class SearchTaskCallable implements Callable<SearchTask> {
  private SearchTask task;

  public SearchTask getTask() {
    return this.task;
  }

  public void setTask(final SearchTask task) {
    this.task = task;
  }

  @Override
  public SearchTask call() throws Exception {
    try {
      switch (this.task.type) {
        case SEARCH:
          this.search();
          break;
        case LIST:
          Dictionary.readTranslations(this.task.context);
          break;
        case DEEP_SEARCH:
          this.deepSearch();
          break;
        default:
          break;
      }
      System.out.println(this.task.type + ": called");
      // this.task.getResult().add(this.getTask().getType() + ": changed");
      this.task.changed = true;
    } finally {
      this.task.finished = true;
    }
    return this.task;
  }

  private void deepSearch() {
    this.search();
    Dictionary.findContainsAndEndsWithIndexes(this.task.context);
  }

  private void search() {
    SearchEngine.initializeSearch(this.task.context, this.task.key);
    final int cacheStartIdx = Dictionary.findCachedStartIdx(this.task.context);
    System.out.println("input: " + this.task.context.inputLower + " (" + this.task.context.inputKey + ")");
    System.out.println("cache start idx: " + cacheStartIdx + ", " + Dictionary.readIndexKey(cacheStartIdx));
    // finds exacts and starts with (~ 2ms)
    // long start = System.currentTimeMillis();
    Dictionary.findExactAndStartsWithIndexes(this.task.context, cacheStartIdx);
  }
}
