package cs321.search;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Arrays;
import java.util.List;

import cs321.btree.BTree;
import cs321.btree.TreeObject;
import cs321.common.ParseArgumentException;

/**
 * SSHSearchBTree is the main class responsible for searching a B-Tree based on given command-line arguments. 
 * It reads a B-Tree from a specified file, processes a query file containing keys to search for, and outputs the 
 * frequency of each key in the B-Tree. The program also supports options for using a cache, limiting the number of 
 * top results, and debugging output.
 * @author Austin Bartram, Calvin McKee
 */
public class SSHSearchBTree {
		
	/**
	 * Main driver of program.
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

    SSHSearchBTreeArguments myArgs;
    BTree myTree;

    try {
		myArgs = parseArguments(args);
		String btreeFileName = myArgs.getBTreeFileName();

		// Initialize BTree
		if (myArgs.getUseCache()) {
			myTree = new BTree(myArgs.getDegree(), btreeFileName, myArgs.getCacheSize(), true);
		} else {
			myTree = new BTree(myArgs.getDegree(), btreeFileName);
		}

		BufferedReader reader = new BufferedReader(new FileReader(myArgs.getqueryFileName()));
		
		java.util.Map<String, Integer> results = new java.util.HashMap<>();
		String query;

		while ((query = reader.readLine()) != null) {
			query = query.trim();
			if (query.isEmpty()) continue;

			TreeObject result = myTree.search(query);
			int count = (result != null) ? (int) result.getCount() : 0;
			results.put(query, count);
		}

		reader.close();
		
		System.err.println("DEBUG: Map size is: " + results.size());

		if (myArgs.gettopFrequency() == -1) {
			// Just print all results as they appear in the map
			for (String key : results.keySet()) {
				System.out.println(key + " " + results.get(key));
			}
		} else {
			java.util.List<java.util.Map.Entry<String, Integer>> list = new java.util.ArrayList<>(results.entrySet());

			list.sort((e1, e2) -> {
				int freqCompare = e2.getValue().compareTo(e1.getValue());
				if (freqCompare != 0) return freqCompare;
				return e1.getKey().compareTo(e2.getKey());
			});

			int limit = Math.min(myArgs.gettopFrequency(), list.size());
			for (int i = 0; i < limit; i++) {
				java.util.Map.Entry<String, Integer> entry = list.get(i);
				System.out.println(entry.getKey() + " " + entry.getValue());
			}
		}

		myTree.close();

	} catch (Exception e) {
		printUsageAndExit(e.toString());
	}
}



	/**
	 * Process command line arguments. Validates the arguments and returns an SSHSearchBTreeArguments 
	 * object containing the parsed values.
	 * @param args  The command line arguments passed to the main method.
	 * @return an SSHSearchBTreeArguments object containing the parsed command line arguments
	 */
	public static SSHSearchBTreeArguments parseArguments(String[] args) throws ParseArgumentException 
	{
		final List<String> SUPPORTED_TYPES = Arrays.asList("accepted-ip", "accepted-time", "invalid-ip","invalid-time","failed-ip","failed-time","reverseaddress-ip","reverseaddress-time","user-ip");
		if (args.length < 5 || args.length > 7) {
			throw new ParseArgumentException("Invalid number of arguments");
		}

		boolean tempUseCache = false;
		int tempDegree = 0;
		String tempBTreeFileName = "";
		String tempQueryFileName = "";
		int tempCacheSize = 0;
		int tempDebugLevel = 0;
		int tempTopFrequency = -1;

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
			} else if (key.equals("--btree-file")) {
                // Check if the filename contains one of the supported types
                boolean isValid = false;
                for (String sub : SUPPORTED_TYPES) {
                    if (value.contains(sub)) {
                        isValid = true;
                        break;
                    }
                }

                if (isValid) {
                    tempBTreeFileName = value;
                } else {
                    throw new ParseArgumentException("Error: Invalid BTree file type. Must contain one of: " + SUPPORTED_TYPES);
                }
			} else if (key.equals("--query-file")) {
				if (value.toLowerCase().endsWith(".txt")) {
                    tempQueryFileName = value;
                } else {
                    throw new ParseArgumentException("Error: File must be a .txt file.");
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
			} else if (key.equals("--top-frequency")) {
				try {
					tempTopFrequency = Integer.parseInt(value);
				} catch (NumberFormatException e) {
					throw new ParseArgumentException("Error: The top frequency value '" + value + "' must be an integer.");
				}
				if (tempTopFrequency != 10 && tempTopFrequency != 25 && tempTopFrequency != 50) {
					throw new ParseArgumentException("Error: top frequency needs to be 10, 25 or 50");
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
		if (tempBTreeFileName.isEmpty()) {
			throw new ParseArgumentException("Error: --btree-file is missing a value");
		}
		if (tempQueryFileName.isEmpty()) {
			throw new ParseArgumentException("Error: --query-file is missing a value");
		}

		SSHSearchBTreeArguments myArgs = new SSHSearchBTreeArguments(tempUseCache, tempDegree, tempBTreeFileName, tempQueryFileName, tempCacheSize, tempDebugLevel, tempTopFrequency);
		return myArgs;
	}


	/**
	 * Print usage message and exit.
	 * @param errorMessage the error message for proper usage
	 */
	private static void printUsageAndExit(String errorMessage)
	{
		System.err.println(errorMessage);
		System.err.println("java -jar build/libs/SSHSearchBTree.jar --cache=<0/1> --degree=<btree-degree> \n" +
						"          --btree-file=<btree-filename> --query-file=<query-fileaname> \n" + 
						"          [--top-frequency=<10/25/50>] [--cache-size=<n>]  [--debug=<0|1>]\n" +
						"cache: specifies whether the program should use cache, if 1 cache-size is required\n" +
						"degree: the degree to be used for the BTree, if 0 sets as the best option\n" +
						"btree-file: the input file containing the previously created BTree file\n" +
						"query-file: the input file containing the keys to search for\n" +
						"top-frequency: the optional limit of top queries to return\n" +
						"cache-size: optional argument, which is an integer between 100 and 10000 (inclusive) that represents the maximum number of BTreeNode objects that can be stored in the memory cache\n" +
						"debug: if enabled outputs more detailed error messages for debugging ");
		System.exit(1);
	}

}
