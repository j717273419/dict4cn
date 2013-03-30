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
  private static final int    LAST_VALID_LNG_ID = 255;

  // private static final String SOURCE = "D:\\kkdict\\out";
  private static final String SOURCE            = "D:\\kkdict\\out";

  private static final String url               = "jdbc:mysql://localhost:3306/dict2go?autoReconnect=true";

  private static final String driver            = "com.mysql.jdbc.Driver";

  /**
   * @param args
   * @throws ClassNotFoundException
   * @throws SQLException
   */
  public static void main(String[] args) throws ClassNotFoundException, SQLException {
    Class.forName(DatabaseWriter.driver);
    DatabaseWriter.start();
  }

  private static void start() throws SQLException {
    try (Connection conn = DriverManager.getConnection(DatabaseWriter.url, "moderator", "rotaredom");) {
      LinkedList<File> selected = new LinkedList<>();
      DatabaseWriter.findDictionaries(new File(DatabaseWriter.SOURCE), selected);
      for (int i = 1; i < 100; i++) {
        DatabaseWriter.findDictionaries(new File(DatabaseWriter.SOURCE + "\\" + i), selected);
      }
      System.out.println("导入" + selected.size() + "词库源");
      if (!selected.isEmpty()) {
        final long start = System.currentTimeMillis();
        ArrayHelper.WARN = false;
        long total = 0;
        for (final File f : selected) {
          System.out.println("导入词库源：" + f.getAbsolutePath());
          total += DatabaseWriter.write(conn, f);
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

  private static int write(Connection conn, File f) {
    System.out.println("\n输入文件：" + f.getAbsolutePath() + " (" + Helper.formatSpace(f.length()) + ")");
    final boolean bi = f.getAbsolutePath().endsWith("_bi");
    final List<Translation> trls = new ArrayList<>();
    final ByteBuffer lineBB = ArrayHelper.borrowByteBufferLarge();
    int count = 0;
    try (final BufferedInputStream in = new BufferedInputStream(new FileInputStream(f), Helper.BUFFER_SIZE);) {
      conn.setAutoCommit(false);

      final DictByteBufferRow row = new DictByteBufferRow();
      List<Integer> idxZhs = new ArrayList<>();
      while (-1 != ArrayHelper.readLineTrimmed(in, lineBB)) {
        try {
          // System.out.println(ArrayHelper.toStringP(lineBB));
          row.parseFrom(lineBB);
          idxZhs.clear();
          final int[] idxLngs = new int[row.size()];
          for (int i = 0; i < row.size(); i++) {
            Language iLng = Language.fromKey(ArrayHelper.toStringP(row.getLanguage(i)));
            if (iLng != null) {
              idxLngs[i] = iLng.getId();
              if (idxLngs[i] == Language.ZH.getId()) {
                idxZhs.add(Integer.valueOf(i));
              }
            }
          }
          if (!idxZhs.isEmpty()) {
            final int zhLngId = Language.ZH.getId();
            for (Integer idxZh : idxZhs) {
              final int valueSizeZh = row.getValueSize(idxZh.intValue());
              for (int iZh = 0; iZh < valueSizeZh; iZh++) {
                final String zhVal = ChineseHelper.toSimplifiedChinese(ArrayHelper.toStringP(row.getValue(idxZh.intValue(), iZh)));
                for (int iLng = 0; iLng < idxLngs.length; iLng++) {

                  final int otherLngId = idxLngs[iLng];
                  if (iZh != otherLngId) {
                    final int valueSizeOther = row.getValueSize(iLng);
                    for (int iOther = 0; iOther < valueSizeOther; iOther++) {
                      final String otherVal = ArrayHelper.toStringP(row.getValue(iLng, iOther));
                      boolean zhSingle;
                      boolean otherSingle;
                      if (bi) {
                        zhSingle = true;
                        otherSingle = true;
                      } else {
                        zhSingle = !Helper.containsAny(zhVal, ',', '"', '|', '.', '(', ')', '[', ']', '：', ':') && (zhVal.length() < 20);
                        otherSingle = !Helper.containsAny(otherVal, ',', '"', '|', '.', '(', ')', '[', ']', '：', ':') && (otherVal.length() < 40);
                        if (0 < iLng) {
                          zhSingle = true;
                        } else {
                          otherSingle = true;
                        }
                      }
                      if (zhSingle) {
                        String[] vals = otherVal.split(", ");
                        for (String val : vals) {
                          if ((zhVal.length() < 2000) && (val.length() < 2000) && !zhVal.endsWith(" (消歧义)") && !val.endsWith(" (disambiguation)")) {
                            Translation trl = Translation.from((short) zhLngId, zhVal, (short) otherLngId, val);
                            DatabaseWriter.addTranslation(trls, trl);
                          }
                        }
                      }
                      if (otherSingle) {
                        String[] vals = zhVal.split(", ");
                        for (String val : vals) {
                          if ((otherVal.length() < 2000) && (val.length() < 2000) && !zhVal.endsWith(" (消歧义)") && !val.endsWith(" (disambiguation)")) {
                            Translation trl = Translation.from((short) otherLngId, otherVal, (short) zhLngId, val);
                            DatabaseWriter.addTranslation(trls, trl);
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
          }
          if (trls.size() > DatabaseWriter.BATCH_SIZE) {
            count += DatabaseWriter.write(conn, trls);
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
    } finally {
      ArrayHelper.giveBack(lineBB);
    }
    return count;
  }

  private static void addTranslation(final List<Translation> trls, Translation trl) {
    if ((trl != null) && (trl.getSrcLng() != trl.getTgtLng()) && Helper.isNotEmptyOrNull(trl.getSrcVal()) && Helper.isNotEmptyOrNull(trl.getTgtVal())) {
      // System.out.println(trl);
      trls.add(trl);
    }
  }

  static String clean(String val) {
    return val.replaceAll("( [ ]+)", " ").replaceAll("[ ]*,([ ]*,)*[ ]*", ", ").replaceAll("(^[\\.!,: ]+)|([\\.,: ]+$)", "").trim();
  }

  private static int writeAgain(Connection conn, List<Translation> trls) throws SQLException {
    int count = 0;
    try (Statement stmt = conn.createStatement();) {
      for (Translation trl : trls) {
        if (trl.getSrcLng() != trl.getTgtLng()) {
          String sql = "";
          try {
            // System.out.println(DatabaseWriter.toSQL(trl));
            sql = DatabaseWriter.toSQL(trl.getSrcKey(), trl.getSrcLng(), trl.getTgtLng(), trl.getSrcVal(), trl.getTgtVal(), trl.getSrcGender(),
                trl.getSrcCategory(), trl.getSrcType(), trl.getSrcUsage());
            stmt.execute(sql);
            count++;
          } catch (SQLException e) {
            System.err.println("\n" + sql + ", " + trl + ", write again: " + e);
          }
        }
      }
      conn.commit();
      System.out.print("x");
    }
    return count;
  }

  private static int write(Connection conn, List<Translation> trls) throws SQLException {
    String sql = null;
    try (Statement stmt = conn.createStatement();) {
      // System.out.println("---");
      for (Translation trl : trls) {
        // System.out.println(trl);
        if ((trl.getSrcLng() != trl.getTgtLng()) && Helper.isNotEmptyOrNull(trl.getSrcVal()) && Helper.isNotEmptyOrNull(trl.getTgtVal())) {
          sql = DatabaseWriter.addTranslationBatch(stmt, trl);
        }
      }
      int[] updateCounts = stmt.executeBatch();
      DatabaseWriter.checkUpdateCounts(updateCounts);
      conn.commit();
      System.out.print(".");
      return trls.size();
    } catch (SQLException e) {
      System.err.println("\nwrite: " + sql + ", " + e);
      return DatabaseWriter.writeAgain(conn, trls);
    }
  }

  private static String addTranslationBatch(Statement stmt, Translation trl) throws SQLException {
    String sql = DatabaseWriter.toSQL(trl.getSrcKey(), trl.getSrcLng(), trl.getTgtLng(), trl.getSrcVal(), trl.getTgtVal(), trl.getSrcGender(),
        trl.getSrcCategory(), trl.getSrcType(), trl.getSrcUsage());
    stmt.addBatch(sql);
    return sql;
  }

  private static String toSQL(byte[] srcKey, int srcLng, int tgtLng, String srcVal, String tgtVal, int srcGender, int srcCategory, int srcType, int srcUsage) {
    if ((srcLng <= DatabaseWriter.LAST_VALID_LNG_ID) && (tgtLng <= DatabaseWriter.LAST_VALID_LNG_ID)) {
      String sql = "call addTranslation(UNHEX('" + ArrayHelper.toHexString(srcKey, false) + "')," + srcLng + "," + tgtLng + ",'"
          + DatabaseWriter.escape(srcVal) + "','" + DatabaseWriter.escape(tgtVal) + "'," + srcGender + "," + srcCategory + "," + srcType + "," + srcUsage
          + ");";
      return sql;
    } else {
      String sql = "call addTranslationInvalid(UNHEX('" + ArrayHelper.toHexString(srcKey, false) + "')," + srcLng + "," + tgtLng + ",'"
          + DatabaseWriter.escape(srcVal) + "','" + DatabaseWriter.escape(tgtVal) + "'," + srcGender + "," + srcCategory + "," + srcType + "," + srcUsage
          + ");";
      return sql;

    }
  }

  private static String escape(String srcVal) {
    return srcVal.replace("\\", "\\\\").replace("'", "\\'");
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
