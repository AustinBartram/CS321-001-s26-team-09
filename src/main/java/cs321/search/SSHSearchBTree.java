package cs321.search;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Scanner;

import cs321.btree.BTree;
import cs321.btree.TreeObject;
import cs321.common.ParseArgumentException;

public class SSHSearchBTree {
		
	/**
	 * Main driver of program.
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		try {
			SSHSearchBTreeArguments bTreeArguments = parseArguments(args);

			BTree BTreeCache;
			if (bTreeArguments.getUseCache()) {
				BTreeCache = new BTree(bTreeArguments.getDegree(), bTreeArguments.getBTreeFileName(), bTreeArguments.getCacheSize(), true);
			} else {
				BTreeCache = new BTree(bTreeArguments.getDegree(), bTreeArguments.getBTreeFileName());
			}

			Scanner fileScanner = new Scanner(new File(bTreeArguments.getqueryFileName()));

			PriorityQueue<TreeObject> priorityQueue = new PriorityQueue<>((a, b) -> {
				if (b.getCount() != a.getCount()) {
					return Long.compare(b.getCount(), a.getCount());
				}
				return a.getKey().compareTo(b.getKey());
			});

			while (fileScanner.hasNextLine()) {
				String query = fileScanner.nextLine().trim();
				if (!query.isEmpty()) {
					TreeObject result = BTreeCache.search(query);
					if (result != null) {
						priorityQueue.add(result);
					}
				}
			}
			fileScanner.close();
			BTreeCache.close();

			int limit = (bTreeArguments.gettopFrequency() != -1) ? bTreeArguments.gettopFrequency() : Integer.MAX_VALUE;
			int count = 0;
			while (!priorityQueue.isEmpty() && count < limit) {
				TreeObject obj = priorityQueue.poll();
				System.out.println(obj.getKey() + " " + obj.getCount());
				count++;
			}

		} catch (Exception e) {
			printUsageAndExit(e.toString());
		}
	}



	/**
	 * Process command line arguments.
	 * @param args  The command line arguments passed to the main method.
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
				if (value.startsWith("SSH_log.txt.ssh.btree.")) {
					
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
						throw new ParseArgumentException("Error: File must be with SSH_log.txt.ssh.btree.<type>.<degree> where type is one of accepted-ip, accepted-time, invalid-ip, invalid-time, failed-ip, failed-time, reverseaddress-ip, reverseaddress-time or user-ip");
					}

				} else {
					throw new ParseArgumentException("Error: File must be with SSH_log.txt.ssh.btree.<type>.<degree>");
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
