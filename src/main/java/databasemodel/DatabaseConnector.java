package databasemodel;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnector {
    private static final String DB_PATH = "C:/Users/yeems214/ProjectAFinalProject/PROJADB.accdb";
    private static final String DB_URL = "jdbc:ucanaccess://" + DB_PATH;

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }
}
