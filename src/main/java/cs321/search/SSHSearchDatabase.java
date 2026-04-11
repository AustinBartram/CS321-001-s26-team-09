package cs321.search;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import cs321.common.ParseArgumentException;


/**
 * This class implements the SSHSearchDatabase program which connects to a SQLite database,
 * retrieves the top N entries from a specified table, and prints them to stdout.
 * @author Calvin McKee
 */
public class SSHSearchDatabase {

    public static void main(String[] args) {
        try {
            SSHSearchDatabaseArguments arguments = new SSHSearchDatabaseArguments(args);
            String url = "jdbc:sqlite:" + arguments.getDatabasePath();

            try (Connection connection = DriverManager.getConnection(url)) {
                // If --type=test is specified, we initialize the data first
                if (arguments.isTestMode()) {
                    initializeTestData(connection);
                }

                // Proceed to search (this works for both test and real databases)
                search(connection, arguments);
            }
        } catch (ParseArgumentException e) {
            System.err.println(e.getMessage());
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        }
    }

    /**
     * Creates the 'acceptedip' table and inserts the 25 required test entries.
     * To use run ./gradlew createJarSSHSearchDatabase and then run:
     * java -jar build/libs/SSHSearchDatabase.jar --type=accepted-ip --database=test.db --top-frequency=
     * Finally run
     * java -jar build/libs/SSHSearchDatabase.jar --type=accepted-ip --database=test.db --top-frequency=<10/25/50>
     */
    public static void initializeTestData(Connection conn) throws SQLException {
        String tableName = "acceptedip";

        String[][] testEntries = {
            {"Accepted-111.222.107.90", "25"}, {"Accepted-112.96.173.55", "3"},
            {"Accepted-112.96.33.40", "3"}, {"Accepted-113.116.236.34", "6"},
            {"Accepted-113.118.187.34", "2"}, {"Accepted-113.99.127.215", "2"},
            {"Accepted-119.137.60.156", "1"}, {"Accepted-119.137.62.123", "9"},
            {"Accepted-119.137.62.142", "1"}, {"Accepted-119.137.63.195", "14"},
            {"Accepted-123.255.103.142", "5"}, {"Accepted-123.255.103.215", "5"},
            {"Accepted-137.189.204.138", "1"}, {"Accepted-137.189.204.155", "1"},
            {"Accepted-137.189.204.220", "1"}, {"Accepted-137.189.204.236", "1"},
            {"Accepted-137.189.204.246", "1"}, {"Accepted-137.189.204.253", "3"},
            {"Accepted-137.189.205.44", "2"}, {"Accepted-137.189.206.152", "1"},
            {"Accepted-137.189.206.243", "1"}, {"Accepted-137.189.207.18", "1"},
            {"Accepted-137.189.207.28", "1"}, {"Accepted-137.189.240.159", "1"},
            {"Accepted-137.189.241.19", "2"}
        };

        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DROP TABLE IF EXISTS " + tableName);
            stmt.executeUpdate("CREATE TABLE " + tableName + " (key_value TEXT, frequency INTEGER)");
        }

        conn.setAutoCommit(false);
        String sql = "INSERT INTO " + tableName + " (key_value, frequency) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (String[] entry : testEntries) {
                pstmt.setString(1, entry[0]);
                pstmt.setInt(2, Integer.parseInt(entry[1]));
                pstmt.executeUpdate();
            }
            conn.commit();
        } finally {
            conn.setAutoCommit(true);
        }
    }

    /**
     * Executes the SQL query and prints results to stdout.
     */
    private static void search(Connection conn, SSHSearchDatabaseArguments args) throws SQLException {
        String query = "SELECT key_value, frequency FROM " + args.getSqlTableName() + 
                       " ORDER BY frequency DESC, key_value ASC LIMIT " + args.getTopFrequency();

        try (Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery(query)) {
            while (rs.next()) {
                System.out.println(rs.getString("key_value") + " " + rs.getInt("frequency"));
            }
        }
    }
}