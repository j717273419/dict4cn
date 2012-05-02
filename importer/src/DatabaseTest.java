/*  Copyright (c) 2010 Xiaoyun Zhu
 * 
 *  Permission is hereby granted, free of charge, to any person obtaining a copy  
 *  of this software and associated documentation files (the "Software"), to deal  
 *  in the Software without restriction, including without limitation the rights  
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell  
 *  copies of the Software, and to permit persons to whom the Software is  
 *  furnished to do so, subject to the following conditions:
 *  
 *  The above copyright notice and this permission notice shall be included in  
 *  all copies or substantial portions of the Software.
 *  
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR  
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,  
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE  
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER  
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,  
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN  
 *  THE SOFTWARE.  
 */
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
