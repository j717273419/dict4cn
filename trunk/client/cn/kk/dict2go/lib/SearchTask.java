package cn.kk.dict2go.lib;

import java.util.concurrent.FutureTask;

public class SearchTask extends FutureTask<SearchTask> {
  final SearchType         type;

  final String             key;

  boolean                  changed;

  boolean                  finished;

  final Context            context;

  final SearchTaskCallable callable;

  private SearchTask(final SearchType type, final String key, final SearchTaskCallable callable, final Context result) {
    super(callable);
    callable.setTask(this);
    this.callable = callable;
    this.type = type;
    this.key = key;
    this.context = result;
  }

  public SearchTask(final SearchType type, final Context context, final String key, final SearchTaskCallable callable) {
    this(type, key, callable, context);
  }

  public SearchTask(final SearchType type, final Context context, final SearchTaskCallable callable) {
    this(type, context.input, callable, context);
  }

  public Context getResult() {
    return this.context;
  }

  public boolean isFinished() {
    return this.finished;
  }

  public String getInput() {
    return this.key;
  }

  public void cancel() {
    this.finished = true;
    super.cancel(true);
  }

  public boolean isComplete() {
    return this.context.doneSearch;
  }

  public boolean isDeepComplete() {
    return this.context.doneDeepSearch;
  }

}
