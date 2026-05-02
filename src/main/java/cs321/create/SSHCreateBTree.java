package cs321.create;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

import cs321.btree.BTree;
import cs321.btree.TreeObject;
import cs321.common.ParseArgumentException;



/**
 * SSHCreateBTree is the main class responsible for creating a B-Tree from a given SSH log file based on specified command-line arguments. 
 * It processes the SSH log file, extracts relevant information, and inserts it into the B-Tree according to the specified tree type. 
 * The program also supports dumping the contents of the B-Tree to a file or a database.
 * @author Austin Bartram, Calvin McKee
 */
public class SSHCreateBTree {

    /**
     * Main driver of program. Reads in command line arguments, creates a BTree, and processes 
     * the SSH log file to populate the BTree.
     * @param args the command line arguments passed to the program
     * @throws Exception if there is an error during execution, such as invalid arguments or file issues
     */
    public static void main(String[] args) throws Exception 
	{
        SSHCreateBTreeArguments myArgs;
        BTree myTree;
        // Process command line arguments and handle any parsing errors
        try {
            myArgs = parseArguments(args);

            String randomAccessFileName = "SSH_log.txt.ssh.btree."+myArgs.getTreeType() + "." + args[1].split("=")[1];
            
            if (myArgs.getUseCache() == true) {
                myTree = new BTree(myArgs.getDegree(), randomAccessFileName, myArgs.getCacheSize(), true);
            } else {
                myTree = new BTree(myArgs.getDegree(), randomAccessFileName);
            }

            SSHFileReader reader = new SSHFileReader(myArgs.getSSHFileName());
            /* this reads the SSH log file line by line and then take given info and extracts the relevant information to
             * build a key based on the specified tree type. It then inserts the key into the BTree.
             */
            while (reader.hasNextLine()) {
                String line = reader.nextLine();
                
                String[] parts = line.trim().split("\\s+");
                
                if (parts.length < 5) continue;

                String date = parts[0];
                String time = parts[1];
                String status = parts[2];
                String subject = parts[3];
                String ip = parts[4];

                String key = buildKey(myArgs.getTreeType(), date, time, status, subject, ip);

                if (key != null) {
                    myTree.insert(new TreeObject(key));
                }
            }
            reader.close();

            if (myArgs.getUseDatabase()) {
                myTree.dumpToDatabase("SSHLogDB.db", myArgs.getTreeType().replace("-", ""));
            }

            if (myArgs.getDebugLevel() == 1) {
                PrintWriter fileDump = new PrintWriter(new FileWriter("dump-" + myArgs.getTreeType() + "." + args[1].split("=")[1] + ".txt"));
                myTree.dumpToFile(fileDump);
                fileDump.close();
            }


            myTree.close();
        } catch (Exception e) {
            printUsageAndExit(e.toString());
        }
	}

    /**
     * This method builds a key for the B-Tree based on the specified tree type and the relevant 
     * information extracted from the SSH log line.
     * @param type the type of B-Tree being created, which determines how the key is constructed
     * @param date the date extracted from the SSH log line
     * @param time the time extracted from the SSH log line
     * @param status the status extracted from the SSH log line
     * @param user the user/subject extracted from the SSH log line
     * @param ip the IP address extracted from the SSH log line
     * @return a String representing the key to be inserted into the B-Tree, or null if the line does not match the specified tree type conditions
     */
    private static String buildKey(String type, String date, String time, String status, String user, String ip) {
        String shortTime = (time != null && time.length() >= 5) ? time.substring(0, 5) : time;

        switch (type) {
            case "accepted-ip":
                if (!status.equals("Accepted")) return null;
                return status + "-" + ip;
            case "accepted-time":
                if (!status.equals("Accepted")) return null;
                return status + "-" + shortTime;
            case "invalid-ip":
                if (!status.equals("Invalid")) return null;
                return status + "-" + ip;
            case "invalid-time":
                if (!status.equals("Invalid")) return null;
                return status + "-" + shortTime;
            case "failed-ip":
                if (!status.equals("Failed")) return null;
                return status + "-" + ip;
            case "failed-time":
                if (!status.equals("Failed")) return null;
                return status + "-" + shortTime;
            case "reverseaddress-ip":
                if (!(status.equals("reverse") || status.equals("Address"))) return null;
                if (status.equals("reverse")) return status + "-" + ip;
                return status + "-" + user;
            case "reverseaddress-time":
                if (!(status.equals("reverse") || status.equals("Address"))) return null;
                return status + "-" + shortTime;
            case "user-ip":
                if (status.equals("reverse") || status.equals("Address")) return null;
                return user + "-" + ip;
            default:
                return null;
        }
    }


    /**
     * Process command line arguments.
     * @param args  The command line arguments passed to the main method.
     */
    public static SSHCreateBTreeArguments parseArguments(String[] args) throws ParseArgumentException
    {
        final List<String> SUPPORTED_TYPES = Arrays.asList("accepted-ip", "accepted-time", "invalid-ip","invalid-time","failed-ip","failed-time","reverseaddress-ip","reverseaddress-time","user-ip");
        if (args.length < 5 || args.length > 7) {
            throw new ParseArgumentException("Invalid number of arguments");
        }

        boolean tempUseCache = false;
        int tempDegree = 0;
        String tempSSHFileName = "";
        String tempTreeType = "";
        int tempCacheSize = 0;
        int tempDebugLevel = 0;
        boolean tempUseDatabase = false;

        for (String arg : args) {
            String[] parts = arg.split("=", 2); // Split into exactly 2 parts
            if (parts.length < 2) continue; // Skip if no value provided after '='

            String key = parts[0];
            String value = parts[1];

            if (key.equals("--cache")) {
                if (value.equals("1")) {
                    tempUseCache = true;
                } else if (value.equals("0")) {
                    tempUseCache = false;
                } else {
                    throw new ParseArgumentException("Error: cache value " + value + ". Must be 1 or 0.");
                }
            } else if (key.equals("--degree")) {
                try {
                    tempDegree = Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    throw new ParseArgumentException("Error: The degree value '" + value + "' must be an integer.");
                }
                if (tempDegree < 0) {
                    throw new ParseArgumentException("Error: The degree value must be non negative");
                }
            } else if (key.equals("--sshFile")) {
                if (value.toLowerCase().endsWith(".txt")) {
                    tempSSHFileName = value;
                } else {
                    throw new ParseArgumentException("Error: File must be a .txt file.");
                }
            } else if (key.equals("--type")) {
                if (!SUPPORTED_TYPES.contains(value)) {
                    throw new ParseArgumentException("Unsupported tree type: " + value);
                } else {
                    tempTreeType = value;
                }
            } else if (key.equals("--cache-size")) {
                try {
                    tempCacheSize = Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    throw new ParseArgumentException("Error: The degree value '" + value + "' must be an integer.");
                }
                if (tempCacheSize < 100 || tempCacheSize > 10000) {
                    throw new ParseArgumentException("Error: The cache size must be between 100 and 10000");
                }
            } else if (key.equals("--database")) {
                if (value.equalsIgnoreCase("yes")) {
                    tempUseDatabase = true;
                } else if(value.equalsIgnoreCase("no")) {
                    tempUseDatabase = false;
                } else {
                    throw new ParseArgumentException("Error: using the database was not specified correctly");
                }
            } else if (key.equals("--debug")) {
                try {
                    tempDebugLevel = Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    throw new ParseArgumentException("Error: The debug value '" + value + "' must be an integer.");
                }
                if (tempDebugLevel != 1) {
                    tempDebugLevel = 0;
                }
            }
        }

        if (tempUseCache == true && tempCacheSize == 0) {
            throw new ParseArgumentException("Error: The cache size must be initialized when use cache is true");
        }
        if (tempSSHFileName.isEmpty()) {
            throw new ParseArgumentException("Error: --sshFile is missing a value");
        }
        if (tempTreeType.isEmpty()) {
            throw new ParseArgumentException("Error: --type is missing a value");
        }

        SSHCreateBTreeArguments myArgs = new SSHCreateBTreeArguments(tempUseCache, tempDegree, tempSSHFileName, tempTreeType, tempCacheSize, tempDebugLevel, tempUseDatabase);
        return myArgs;
    }


	/** 
	 * Print usage message and exit.
	 * @param errorMessage the error message for proper usage
	 */
	private static void printUsageAndExit(String errorMessage)
    {
        System.err.println(errorMessage);
        System.err.println("java -jar build/libs/SSHCreateBTree.jar --cache=<0/1> --degree=<btree-degree> \n" +
                        "          --sshFile=<ssh-File> --type=<tree-type> [--cache-size=<n>] \n" + 
                        "          --database=<yes/no> [--debug=<0|1>]\n" +
                        "cache: specifies whether the program should use cache, if 1 cache-size is required\n" +
                        "degree: the degree to be used for the BTree, if 0 sets as the best option\n" +
                        "sshFile: the input .txt file containing the wrangled SSH log file\n" +
                        "type:the type of BTree used and is one of nine options:\n" +
                                                        "\n" +
                                                        "accepted-ip\n" +
                                                        "accepted-time\n" +
                                                        "invalid-ip\n" + 
                                                        "invalid-time\n" +
                                                        "failed-ip\n" +
                                                        "failed-time\n" +
                                                        "reverseaddress-ip\n" +
                                                        "reverseaddress-time\n" +
                                                        "user-ip\n" +
                                                        "\n" +
                        "cache-size: optional argument, which is an integer between 100 and 10000 (inclusive) that represents the maximum number of BTreeNode objects that can be stored in the memory cache\n" +
                        "database: the path to the SQL database created after BTree creation for a specific BTree type. The name of the database file should be SSHLogDB.db\n" +
                        "debug: if enabled outputs more detailed error messages for debugging ");
        System.exit(1);
	}

}
