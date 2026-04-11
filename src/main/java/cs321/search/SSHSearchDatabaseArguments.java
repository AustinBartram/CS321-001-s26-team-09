package cs321.search;

import cs321.common.ParseArgumentException;
import cs321.common.ParseArgumentUtils;

/**
 * This class represents the arguments for the SSHSearchDatabase program.
 * 
 * @author Calvin McKee
 */
public class SSHSearchDatabaseArguments {
    private String type;
    private String databasePath;
    private int topFrequency;
    private boolean isTest = false;

    /**
     * Parses the command-line arguments and initializes the fields.
     * @param args
     * @throws ParseArgumentException
     */
    public SSHSearchDatabaseArguments(String[] args) throws ParseArgumentException {
        if (args.length < 2 || args.length > 3) {
            printUsage();
            throw new ParseArgumentException("Invalid number of arguments");
        }

        for (String arg : args) {
            String[] parts = arg.split("=", 2); // Split into exactly 2 parts
            if (parts.length < 2) continue; // Skip if no value provided after '='

            String key = parts[0];
            String value = parts[1];

            if (key.equals("--type")) {
                if (value.equals("test")) {
                    value = "acceptedip";
                    isTest = true;
                }
                this.type = value;
            } else if (key.equals("--database")) {
                this.databasePath = value;
            } else if (key.equals("--top-frequency")) {
                try {
                    if (!isTest) {
                        this.topFrequency = Integer.parseInt(value);
                    }
                } catch (NumberFormatException e) {
                    throw new ParseArgumentException("Frequency must be a number.");
                }
            }
        }

        if (type == null || databasePath == null) {
            printUsage();
            throw new ParseArgumentException("Type and Database are required.");
        }

        if (!isTest) {
            if (topFrequency != 10 && topFrequency != 25 && topFrequency != 50) {
                throw new ParseArgumentException("Invalid frequency: must be 10, 25, or 50.");
            }
        }
    }

    /**
     * Returns the SQL table name based on the type argument.
     * @return
     */
    public String getSqlTableName() {
        return type.replace("-", "").toLowerCase();
    }
    
    /**
     * Returns the database path.
     * @return
     */
    public String getDatabasePath() { 
        return databasePath; 
    }

    /**
     * Returns the top frequency value.
     * @return
     */
    public int getTopFrequency() { 
        return topFrequency; 
    }

    /**
     * Returns whether the program is in test mode.
     * @return
     */
    public boolean isTestMode() { 
        return isTest; 
    }
    
    /**
     * Prints usage instructions to stderr.
     */
    private void printUsage() {
        System.err.println("Usage: java -jar build/libs/SSHSearchDatabase.jar --type=<tree-type> --database=<SQLite-database-path> --top-frequency=<10/25/50>");
        System.err.println("  type: the type of the database (e.g., 'btree'), use test for testing");
        System.err.println("  databasePath: the path to the database file");
        System.err.println("  top-frequency: the number of top frequent items to return");
    }
}
