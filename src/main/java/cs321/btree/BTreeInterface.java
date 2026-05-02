package cs321.btree;

import java.io.IOException;
import java.io.PrintWriter;
/**
 * BTreeInterface defines the methods that must be implemented by any class that implements a 
 * B-Tree data structure. It includes methods for getting the size, degree, number of nodes, 
 * and height of the B-Tree, as well as methods for inserting a key, and dumping the contents of the 
 * B-Tree to a file or database. 
 * @author Austin Bartram, Calvin McKee
 */
public interface BTreeInterface {

    /**
     * @return Returns the number of keys in the BTree.
     */
    long getSize();


    /**
     * @return The degree of the BTree.
     */
    int getDegree();


    /**
     * @return Returns the number of nodes in the BTree.
     */
    long getNumberOfNodes();


    /**
     * @return The height of the BTree
     */
    int getHeight();

    
    /**
     *
     * Insert a given SSH key into the B-Tree. If the key already exists in the B-Tree,
     * the frequency count is incremented. Otherwise, a new node is inserted
     * following the B-Tree insertion algorithm.
     *
     * @param obj
     *            A TreeObject representing an SSH key.
     *
     */
    void insert(TreeObject obj) throws IOException;


    /**
     * Print out all objects in the given BTree in an inorder traversal to a file.
     *
     * @param out PrintWriter object representing output.
     */
    void dumpToFile(PrintWriter out) throws IOException;


    /**
     * Dump out all objects in the given BTree in an inorder traversal to a table in the database.
     *
	 * If the database does not exist, then it is created and the table is added.
	 *
     * If the provided database already exists, then the table is added. If the table already exists,
     * then the table is replaced. 
     *
     * @param dbName String referring to the name of the database.
     * @param tableName String referring to the table of the database.
     */
    void dumpToDatabase(String dbName, String tableName) throws IOException;


    /**
     * Searches for a key in the given BTree.
     *
     * @param key
     *            The key value to search for.
     */
    TreeObject search(String key) throws IOException;


	/**
     * Deletes a key from the BTree. Not Implemented.
     *
     * @param key the key to be deleted
     */
    void delete(String key);

}
