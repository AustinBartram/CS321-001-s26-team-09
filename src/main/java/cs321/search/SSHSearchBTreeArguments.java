package cs321.search;

public class SSHSearchBTreeArguments
{
    private final boolean useCache;
    private final int degree;
    private final String BTreeFileName;
    private final String queryFileName;
    private final int cacheSize;
    private final int debugLevel;
    private final int topFrequency;

    /**
     * Builds a new SSHCreateBTreeArguments with the specified
     * command line arguments and tests their validity.
     *
     * @param useCache boolean for using cache or not
     * @param degree degree for BTree
     * @param BTreeFileName String of filename
     * @param queryFileName type of tree
     * @param cacheSize size of cache if using
     * @param debugLevel level of debugging
     * @param topFrequency the top digits to return
     */
    public SSHSearchBTreeArguments(boolean useCache, int degree, String BTreeFileName, String queryFileName, int cacheSize, int debugLevel, int topFrequency)
    {
        this.useCache = useCache;
        this.BTreeFileName = BTreeFileName;
        this.queryFileName = queryFileName;
        this.cacheSize = cacheSize;
        this.debugLevel = debugLevel;
        this.topFrequency = topFrequency;
        
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
     * Returns the BTreeFileName
     * @return the BTreeFileName as a String
     */
    public String getBTreeFileName() {
        return this.BTreeFileName;
    }

    /**
     * Returns the query file name
     * @return the query file name as a String
     */
    public String getqueryFileName() {
        return this.queryFileName;
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
     * Returns the top frequency
     * @return the top frequency as an int
     */
    public int gettopFrequency() {
        return this.topFrequency;
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
        return "BTreeFileNameCreateBTreeArguments{" +
                "useCache=" + useCache +
                ", degree=" + degree +
                ", btree-file='" + BTreeFileName + '\'' +
                ", query-file=" + queryFileName +
                ", top-frequency="+ topFrequency +
                ", cacheSize=" + cacheSize +
                ", debugLevel=" + debugLevel +
                '}';
    }
}
