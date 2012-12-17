package cn.kk.kkdict.database;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import cn.kk.kkdict.beans.DictByteBufferRow;
import cn.kk.kkdict.types.Language;
import cn.kk.kkdict.utils.ArrayHelper;
import cn.kk.kkdict.utils.ChineseHelper;
import cn.kk.kkdict.utils.Helper;

public class DatabaseWriter {
  private static final String url    = "jdbc:mysql://localhost:3306/dict2go?autoReconnect=true";

  private static final String driver = "com.mysql.jdbc.Driver";

  /**
   * @param args
   * @throws ClassNotFoundException
   * @throws SQLException
   */
  public static void main(String[] args) throws ClassNotFoundException, SQLException {
    Class.forName(DatabaseWriter.driver);
    DatabaseWriter.start();
  }

  private static void start() throws ClassNotFoundException, SQLException {
    try (Connection conn = DriverManager.getConnection(DatabaseWriter.url, "moderator", "rotaredom");) {
      File directory = new File("D:\\kkdict\\out\\test");
      LinkedList<File> selected = new LinkedList<File>();
      DatabaseWriter.findDictionaries(directory, selected);
      for (int i = 1; i < 100; i++) {
        directory = new File(directory, String.valueOf(i));
        DatabaseWriter.findDictionaries(directory, selected);
      }

      if (!selected.isEmpty()) {
        final long start = System.currentTimeMillis();
        ArrayHelper.WARN = false;
        long total = 0;
        for (final File f : selected) {
          DatabaseWriter.write(conn, f);
        }

        System.out.println("=====================================");
        System.out.println("总共读取了" + selected.size() + "个词典文件，用时：" + Helper.formatDuration(System.currentTimeMillis() - start));
        System.out.println("总共有效词组：" + total);
        System.out.println("=====================================\n");
      }
    }
  }

  private static void findDictionaries(final File directory, LinkedList<File> selected) {
    if (directory.isDirectory()) {
      System.out.print("输入词典文件夹'" + directory.getAbsolutePath() + "' ... ");

      selected.addAll(Arrays.asList(directory.listFiles(new FileFilter() {
        @Override
        public boolean accept(File f) {
          return f.isFile();
        }
      })));
      System.out.println(selected.size());
    }
  }

  private final static int BATCH_SIZE = 5000;

  private static void write(Connection conn, File f) {
    System.out.println("\n输入文件：" + f.getAbsolutePath() + " (" + Helper.formatSpace(f.length()) + ")");
    final boolean bi = f.getAbsolutePath().endsWith("_bi");
    final List<Translation> trls = new ArrayList<>();
    try (final BufferedInputStream in = new BufferedInputStream(new FileInputStream(f), Helper.BUFFER_SIZE);) {
      conn.setAutoCommit(false);

      final ByteBuffer lineBB = ArrayHelper.borrowByteBufferLarge();
      final DictByteBufferRow row = new DictByteBufferRow();

      while (-1 != ArrayHelper.readLineTrimmed(in, lineBB)) {
        try {
          row.parseFrom(lineBB);
          int idxZh = -1;
          final int[] idxLngs = new int[row.size()];
          for (int i = 0; i < row.size(); i++) {
            idxLngs[i] = Language.fromKey(ArrayHelper.toStringP(row.getLanguage(i))).getId();
            if (idxLngs[i] == Language.ZH.getId()) {
              idxZh = i;
            }
          }
          if (idxZh != -1) {
            final int zhLngId = Language.ZH.getId();
            final String zhVal = ChineseHelper.toSimplifiedChinese(DatabaseWriter.clean(row.getValuesAsString(idxZh)));
            for (int i = 0; i < idxLngs.length; i++) {
              if (i != idxZh) {
                final int otherLngId = idxLngs[i];
                final String otherVal = DatabaseWriter.clean(row.getValuesAsString(i));
                boolean zhSingle;
                boolean otherSingle;
                if (bi) {
                  zhSingle = true;
                  otherSingle = true;
                } else {
                  zhSingle = !Helper.containsAny(zhVal, ',', '"', '|', '.', '(', ')', '[', ']', '：', ':') && (zhVal.length() < 20);
                  otherSingle = !Helper.containsAny(otherVal, ',', '"', '|', '.', '(', ')', '[', ']', '：', ':') && (otherVal.length() < 40);
                  if (idxZh < i) {
                    zhSingle = true;
                  } else {
                    otherSingle = true;
                  }
                }
                if (zhSingle) {
                  Translation trl = Translation.from((short) zhLngId, zhVal, (short) otherLngId, otherVal);
                  trls.add(trl);
                }
                if (otherSingle) {
                  Translation trl = Translation.from((short) otherLngId, otherVal, (short) zhLngId, zhVal);
                  trls.add(trl);
                }
              }
            }
          }
          if (trls.size() > DatabaseWriter.BATCH_SIZE) {
            DatabaseWriter.write(conn, trls);
            trls.clear();
          }
        } catch (Exception e) {
          System.err.println(ArrayHelper.toString(row.getByteBuffer()));
          throw e;
        }
      }
    } catch (Exception e) {
      System.err.println("错误：" + e.toString());
      e.printStackTrace();
    }
  }

  private static String clean(String val) {
    return val.replaceAll("( [ ]+)", " ").replaceAll("[ ]*,([ ]*,)*[ ]*", ", ").replaceAll("[,: ]+$", "").trim();
  }

  private static void writeAgain(Connection conn, List<Translation> trls) throws SQLException {
    try (Statement stmt = conn.createStatement();) {
      for (Translation trl : trls) {
        String sql = "";
        try {
          // System.out.println(DatabaseWriter.toSQL(trl));
          sql = DatabaseWriter.toSQL(trl.getSrcKey(), trl.getSrcLng(), trl.getTgtLng(), trl.getSrcVal(), trl.getTgtVal(), trl.getSrcGender(),
              trl.getSrcCategory(), trl.getSrcType(), trl.getSrcUsage());
          stmt.execute(sql);
        } catch (SQLException e) {
          System.err.println("\n" + sql + ", " + trl + ", write again: " + e);
        }
      }
      conn.commit();
    }
  }

  private static void write(Connection conn, List<Translation> trls) throws SQLException {
    String sql = null;
    try (Statement stmt = conn.createStatement();) {
      // System.out.println("---");
      for (Translation trl : trls) {
        // System.out.println(trl);
        if (Helper.isNotEmptyOrNull(trl.getSrcVal()) && Helper.isNotEmptyOrNull(trl.getTgtVal())) {
          sql = DatabaseWriter.addTranslationBatch(stmt, trl);
        }
      }
      int[] updateCounts = stmt.executeBatch();
      DatabaseWriter.checkUpdateCounts(updateCounts);
      conn.commit();
      System.out.print(".");
    } catch (SQLException e) {
      System.err.println("\nwrite: " + sql + ", " + e);
      DatabaseWriter.writeAgain(conn, trls);
      System.out.print("x");
    }
  }

  private static String addTranslationBatch(Statement stmt, Translation trl) throws SQLException {
    String sql = DatabaseWriter.toSQL(trl.getSrcKey(), trl.getSrcLng(), trl.getTgtLng(), trl.getSrcVal(), trl.getTgtVal(), trl.getSrcGender(),
        trl.getSrcCategory(), trl.getSrcType(), trl.getSrcUsage());
    stmt.addBatch(sql);
    return sql;
  }

  private static String toSQL(byte[] srcKey, int srcLng, int tgtLng, String srcVal, String tgtVal, int srcGender, int srcCategory, int srcType, int srcUsage) {
    String sql = "call addTranslation(UNHEX('" + ArrayHelper.toHexString(srcKey, false) + "')," + srcLng + "," + tgtLng + ",'" + DatabaseWriter.escape(srcVal)
        + "','" + DatabaseWriter.escape(tgtVal) + "'," + srcGender + "," + srcCategory + "," + srcType + "," + srcUsage + ");";
    return sql;
  }

  private static String escape(String srcVal) {
    return srcVal.replace("'", "\\'");
  }

  private static void checkUpdateCounts(int[] updateCounts) {
    int errors = 0;
    for (int i = 0; i < updateCounts.length; i++) {
      if (updateCounts[i] == Statement.EXECUTE_FAILED) {
        errors++;
      }
    }
    if (errors > 0) {
      System.err.print(errors);
    } else {
      System.out.print(".");
    }
  }
}
