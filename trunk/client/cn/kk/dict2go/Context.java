package cn.kk.dict2go;

import java.util.LinkedList;

public final class Context {

  public static Step               step;

  public static int[]              lngs;

  public static int[]              sortedLngs;

  public static String             input;

  public static LinkedList<String> lastInputs;

  public static int                selectedIdx;

  public static void change(Step step) {
    Context.step = step;
  }

}
