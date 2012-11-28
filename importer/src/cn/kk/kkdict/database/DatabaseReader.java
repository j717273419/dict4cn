package cn.kk.kkdict.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseReader {

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
      try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(DatabaseReader.query);) {
        while (rs.next()) {
          int i = 1;
          long trlId = rs.getInt(i++);
          String srcKeyHex = rs.getString(i++);
          int srcLng = rs.getInt(i++);
          int tgtLng = rs.getInt(i++);
          String srcVal = rs.getString(i++);
          String tgtVal = rs.getString(i++);
          int srcGender = rs.getInt(i++);
          int srcCategory = rs.getInt(i++);
          int srcType = rs.getInt(i++);
          int srcUsage = rs.getInt(i++);
          DatabaseReader.writeTranslation(new Translation(trlId, srcKeyHex, srcLng, tgtLng, srcVal, tgtVal, srcGender, srcCategory, srcType, srcUsage));
        }
      } catch (SQLException e) {
        e.printStackTrace();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static void writeTranslation(Translation trl) {
    System.out.println(trl);

  }

  private static final String query = "SELECT trl_id, HEX(src_key), src_lng, tgt_lng, src_val, tgt_val, src_gen, src_cat, src_typ, src_use FROM translation";

}
