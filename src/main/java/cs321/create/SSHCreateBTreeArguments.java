package cs321.create;

/**
 * SSHCreateBTreeArguments parses command line arguments for SSHCreateBTree.
 *
 *  @author Calvin McKee
 *
 */
public class SSHCreateBTreeArguments
{

    private final boolean useCache;
    private final int degree;
    private final String SSHFileName;
    private final String treeType;
    private final int cacheSize;
    private final int debugLevel;
    private final boolean useDatabase;

    /**
     * Builds a new SSHCreateBTreeArguments with the specified
     * command line arguments and tests their validity.
     *
     * @param useCache boolean for using cache or not
     * @param degree degree for BTree
     * @param SSHFileName String of filename
     * @param treeType type of tree
     * @param cacheSize size of cache if using
     * @param debugLevel level of debugging
     * @param useDatabase level of debugging
     */
    public SSHCreateBTreeArguments(boolean useCache, int degree, String SSHFileName, String treeType, int cacheSize, int debugLevel, boolean useDatabase)
    {
        this.useCache = useCache;
        this.SSHFileName = SSHFileName;
        this.treeType = treeType;
        this.cacheSize = cacheSize;
        this.debugLevel = debugLevel;
        this.useDatabase = useDatabase;
        
        if (degree == 0) {
            this.degree = CalculateBaseDegree();
        } else {
            this.degree = degree;
        }
    }

    /**
     * Returns if the cache will be used
     * @return boolean of if the cache will be used
     */
    public boolean getUseCache() {
        return this.useCache;
    }

    /**
     * Returns the degree of the BTree
     * @return the degree of the BTree as an int
     */
    public int getDegree() {
        return this.degree;
    }

    /**
     * Returns the SSHFileName
     * @return the SSHFIleName as a String
     */
    public String getSSHFileName() {
        return this.SSHFileName;
    }

    /**
     * Returns the tree type
     * @return the tree type as a String
     */
    public String getTreeType() {
        return this.treeType;
    }

    /**
     * Returns the cache size
     * @return the cache size as an int
     */
    public int getCacheSize() {
        return this.cacheSize;
    }

    /**
     * Returns the debug level
     * @return the debug level as an int
     */
    public int getDebugLevel() {
        return this.debugLevel;
    }

    /**
     * Returns the debug level
     * @return the debug level as an int
     */
    public boolean getUseDatabase() {
        return this.useDatabase;
    }

    /**
     * Used to get the most optimal degree for a drive of size 4096
     * @return the optimal degree as int
     */
    private int CalculateBaseDegree() {
        //Code that can be used to calculate the degree dynamically
        // int totalSize = 0;
        // int currentDegree = 0;
        // while (totalSize <= 4096) {
        //     totalSize = Integer.BYTES + 1 + (currentDegree * 2 - 1) * TreeObject.BYTES + 16 * currentDegree;
        //     currentDegree++;
        // }
        // currentDegree--;
        // totalSize = Integer.BYTES + 1 + (currentDegree * 2 - 1) * TreeObject.BYTES + 16 * currentDegree;
        return 26;
    }


    @Override
    public String toString()
    {
        return "SSHFileNameCreateBTreeArguments{" +
                "useCache=" + useCache +
                ", degree=" + degree +
                ", SSH_Log_File='" + SSHFileName + '\'' +
                ", TreeType=" + treeType +
                ", cacheSize=" + cacheSize +
                ", debugLevel=" + debugLevel +
                ", useDatabase="+ useDatabase +
                '}';
    }
}
