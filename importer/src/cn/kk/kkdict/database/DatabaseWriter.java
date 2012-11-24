package cn.kk.kkdict.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import cn.kk.kkdict.utils.ArrayHelper;

public class DatabaseWriter {

  /**
   * @param args
   * @throws ClassNotFoundException
   */
  public static void main(String[] args) throws ClassNotFoundException {
    String url = "jdbc:mysql://localhost:3306/dict2go?autoReconnect=true&characterEncoding=utf-8&useUnicode=true";
    String driver = "com.mysql.jdbc.Driver";
    Class.forName(driver);
    try (Connection conn = DriverManager.getConnection(url, "moderator", "rotaredom");) {
      conn.setAutoCommit(false);
      try (Statement stmt = conn.createStatement();) {
        byte[] sidx = { 1, 2, 3, 4 };
        int srcLng = 0;
        int tgtLng = 1;
        String srcVal = "lalala";
        String tgtVal = "tatata";
        int srcGender = 0;
        int srcCategory = 0;
        int srcType = 0;
        int srcUsage = 0;

        DatabaseWriter.addTranslationBatch(stmt, sidx, srcLng, tgtLng, srcVal, tgtVal, srcGender, srcCategory, srcType, srcUsage);

        int[] updateCounts = stmt.executeBatch();
        DatabaseWriter.checkUpdateCounts(updateCounts);
        conn.commit();

      } catch (SQLException e) {
        e.printStackTrace();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static void addTranslationBatch(Statement stmt, byte[] sidx, int srcLng, int tgtLng, String srcVal, String tgtVal, int srcGender, int srcCategory,
      int srcType, int srcUsage) throws SQLException {
    stmt.addBatch("call addTranslation(UNHEX('" + ArrayHelper.toHexString(sidx, false) + "')," + srcLng + "," + tgtLng + ",'" + DatabaseWriter.escape(srcVal)
        + "','" + DatabaseWriter.escape(tgtVal) + "'," + srcGender + "," + srcCategory + "," + srcType + "," + srcUsage + ");");
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

//
// http://alvinalexander.com/java/java-mysql-select-query-example
