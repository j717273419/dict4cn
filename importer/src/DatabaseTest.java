import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DatabaseTest {
    // jdbc:hsqldb:file:C:/usr/db
    public static void main(String[] a)
            throws Exception {
        Class.forName("org.h2.Driver");
        Connection conn = DriverManager.getConnection("jdbc:h2:file:C:/usr/kkdictdb;IFEXISTS=TRUE,create=true", "kkdict", "");
        PreparedStatement ps2 = conn.prepareStatement("SHOW TABLES;");
        ResultSet rs = ps2.executeQuery();

        // add application code here
        conn.close();
    }
}
