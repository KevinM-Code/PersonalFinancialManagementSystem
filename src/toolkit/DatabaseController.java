/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package toolkit;

import java.sql.*;
import java.util.logging.Level;

/**
 * This class has tools to manipulate the database and is used for database
 * connection.
 *
 * @author Kevin Mock
 */
public class DatabaseController {

    private static final String CONN_URL = "jdbc:sqlite:" + SecurityController.PATH_TO_DEFAULT_DB;

    /**
     * This is used to connect to the database
     *
     * @return null if there is no connection
     */
    public static Connection dBConnector() {
        try {
            Connection conn = null;
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection(CONN_URL);
            return conn;
        } catch (ClassNotFoundException | SQLException ex) {
            java.util.logging.Logger.getLogger(DatabaseController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Used to create new tables
     *
     * @param sqlQuery
     */
    public static void createNewTable(String sqlQuery) {

        try (Connection conn = dBConnector()) {
            if (conn != null) {
                Statement stmt = conn.createStatement();
                // create a new table
                stmt.execute(sqlQuery);
            }
        } catch (SQLException e) {
            java.util.logging.Logger.getLogger(DatabaseController.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    /**
     * Used to insert a query with seven columns into the database
     *
     * @param sqlQuery
     * @param one
     * @param two
     * @param three
     * @param four
     * @param five
     * @param six
     * @param seven
     * @throws SQLException
     */
    public static void insertSevenColumnsIntoDatabase(String sqlQuery, String one, String two, String three, String four, String five, String six, String seven) throws SQLException {

        try {
            Class.forName("org.sqlite.JDBC");
            try (Connection conn = dBConnector()) {
                if (conn != null) {
                    PreparedStatement insertPrepStmt = conn.prepareStatement(sqlQuery);

                    //Parameters
                    insertPrepStmt.setString(1, one);
                    insertPrepStmt.setString(2, two);
                    insertPrepStmt.setString(3, three);
                    insertPrepStmt.setString(4, four);
                    insertPrepStmt.setString(5, five);
                    insertPrepStmt.setString(6, six);
                    insertPrepStmt.setString(7, seven);

                    insertPrepStmt.executeUpdate();
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(DatabaseController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * This is a reusable method for a select query for one column when your
     * getting one <code>String</code> back.
     *
     * @param sqlQuery
     * @param columnNameString
     * @return String
     * @throws SQLException
     */
    public static String selectDBQueryReturnOneString(String sqlQuery, String columnNameString) throws SQLException {

        Connection conn = dBConnector();
        if (conn != null) {
            PreparedStatement prepStmt = conn.prepareStatement(sqlQuery);
            ResultSet resultSet = prepStmt.executeQuery();

            if (resultSet.next()) {
                String result = resultSet.getString(columnNameString);
                conn.close();
                return result;
            }
            conn.close();
        }
        return null;
    }
    
    public static int selectDBQueryReturnOneInt(String sqlQuery, String columnNameString) throws SQLException {

        Connection conn = dBConnector();
        if (conn != null) {
            PreparedStatement prepStmt = conn.prepareStatement(sqlQuery);
            ResultSet resultSet = prepStmt.executeQuery();

            if (resultSet.next()) {
                int result = resultSet.getInt(columnNameString);
                conn.close();
                return result;
            }
            conn.close();
        }
        return 0;
    }
}
