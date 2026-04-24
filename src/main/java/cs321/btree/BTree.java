package cs321.btree;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;

public class BTree implements BTreeInterface {

    private BTreeNode root;
    private int t; // degree

    // fields for tracking size and number of nodes in the tree.
    private int size = 0;
    private int nodeCount = 1;

    // fields for disk storage
    private FileChannel file;
    private ByteBuffer buffer;
    private long rootAddress;
    private long nextAddress;
    private static final int METADATA_SIZE = Long.BYTES;
    private int nodeSize;


    //constructor (degree = 2)
    public BTree(String filename) {
        this(2, filename);
        // this allows for the user to then add in their own input

    }

    /**
     * Constructor for the BTree class. It initializes the BTree with a given degree and sets up the file for disk storage.
     * @param t
     * @param filename
     */
    public BTree(int t, String filename) {
        this.t = t;

        // this sets up the file for disk storage. It creates a RandomAccessFile and gets its FileChannel for reading and writing nodes to disk.
        try {
            RandomAccessFile randomFile = new RandomAccessFile(filename, "rw");
            file = randomFile.getChannel();

            // calculate the size of a node in bytes. This includes the number of keys (int), 
            // the leaf flag (byte), the keys and counts (2t - 1 TreeObjects), and the child addresses (2t longs).
            nodeSize = Integer.BYTES + 1 + (2 * t - 1) * TreeObject.BYTES + (2 * t) * Long.BYTES;

            // initialize the buffer to the size of a node. This buffer will be used for reading and writing nodes to disk.
            buffer = ByteBuffer.allocate(nodeSize);

            // initialize the root node and set its address. The root node is created as a leaf node with no keys and no children.
            root = new BTreeNode(true);
            root.address = METADATA_SIZE;

            // write the root node to disk and set the root address and next address for the next node to be written.
            diskWrite(root);
            rootAddress = root.address;
            nextAddress = rootAddress + nodeSize;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class BTreeNode {
        int numKeys;
        boolean isLeaf;
        TreeObject[] keys;
        long[] childrenAddresses;
        long address;

        /**
         * Constructor for the BTreeNode class. It initializes a BTreeNode with the given leaf flag and sets up the keys and children arrays.
         * @param isLeaf
         */
        BTreeNode(boolean isLeaf) {
            this.isLeaf = isLeaf;
            keys = new TreeObject[2 * t - 1];
            childrenAddresses = new long[2 * t];
            numKeys = 0;
        }
    }

    public void insert(TreeObject key) {
        // duplicate maintenance: if key already exists, just increment count
        BTreeNode existing = nodeSearchHelper(root, key);
        if (existing != null) {
            searchHelper(existing, key).incCount();
            diskWrite(existing);
            return;
        }

        BTreeNode r = root;

        // If the root is full split it and create a new node. This increases the height. 
        if (r.numKeys >= 2 * t - 1) {
            BTreeNode s = new BTreeNode(false);
            s.address = nextAddress;
            nextAddress += nodeSize;
            nodeCount++;

            s.childrenAddresses[0] = r.address;
            root = s;
            rootAddress = s.address;

            // this splits the old root and moves the key up. The new node s becomes the new root and has one key and two children.
            splitChild(s, 0, r);
            insertHelper(s, key);
            diskWrite(root);
            // writeHeader();
        } else {
            insertHelper(r, key);
        }
    }

    private void insertHelper(BTreeNode node, TreeObject key) {

        int i = node.numKeys - 1;

        // if node is a leaf, then insert the key to the right position. 
        if (node.isLeaf) {
            // this goes and shifts the keys to the right until the positon is right.
            while (i >= 0 && key.compareTo(node.keys[i]) < 0) {
                node.keys[i + 1] = node.keys[i];
                i--;
            }

            // insert the key to the right position and increment the number of keys in the node.
            node.keys[i + 1] = key;
            node.numKeys++;
            size++;
        // if the node is not a leaf, then we need to find the child to go to.
        } else {
            while (i >= 0 && key.compareTo(node.keys[i]) < 0) {
                i--;
            }
            i++;

            long childAddr = node.childrenAddresses[i];
            BTreeNode childNode = diskRead(childAddr);

            // if the child is full then split it and then insert the key to the right child.
            if (childNode.numKeys == 2 * t - 1) {
                splitChild(node, i, childNode);

                if (key.compareTo(node.keys[i]) > 0) {
                    i++;
                }
                childNode = diskRead(node.childrenAddresses[i]);
            }

            // insert the key to the right child.
            insertHelper(childNode, key);
        }

        // after inserting the key, we need to write the node back to disk to update it.
        diskWrite(node);
    }

    /**
     * This method splits a full child node into two nodes and moves the middle key up to the parent.
     * @param index
     * @param fullChild
     */
    private void splitChild(BTreeNode parent, int index, BTreeNode fullChild) {
        // this creates a new node that will hold the last t - 1 keys of the full child.
        BTreeNode newNode = new BTreeNode(fullChild.isLeaf);
        newNode.address = nextAddress;
        nextAddress += nodeSize;

        newNode.numKeys = t - 1;

        // copy keys over to the new node. The middle key is at index t - 1 and will be shifted to the parent. the keys are copied from
        // t to 2t - 2 to the new node.
        for (int j = 0; j < t - 1; j++) {
            newNode.keys[j] = fullChild.keys[j + t];
        }

        // this copies the children if the full child is not a leaf. The children are copied from t to 2t - 1 to the new node.
        if (!fullChild.isLeaf) {
            for (int j = 0; j < t; j++) {
                newNode.childrenAddresses[j] = fullChild.childrenAddresses[j + t];
            }
        }

        // the full child now has only the first t - 1 keys and the new node has the last t - 1 keys. The middle key is moved up to the parent.
        fullChild.numKeys = t - 1;

        // this shifts the children in the parent to the right to make space for the new child. 
        // The new child is inserted at index + 1.
        for (int j = parent.numKeys; j >= index + 1; j--) {
            parent.childrenAddresses[j + 1] = parent.childrenAddresses[j];
        }

        parent.childrenAddresses[index + 1] = newNode.address;

        // this shifts the keys in the parent to the right to make space for the middle key 
        // from the full child.
        for (int j = parent.numKeys - 1; j >= index; j--) {
            parent.keys[j + 1] = parent.keys[j];
        }

        // move the middle key up to the parent and increment the number of keys in the parent.
        parent.keys[index] = fullChild.keys[t - 1];
        parent.numKeys++;

        nodeCount++;

        // after splitting the child, we need to write the parent, full child, and new node back to disk to update them.
        diskWrite(fullChild);
        diskWrite(newNode);
        diskWrite(parent);

    }

    // this method searches for a key in the tree and returns the TreeObject if found.
    public TreeObject search(String key) {
        return searchHelper(root, new TreeObject(key));
    }

    // this method searches for a key in the tree starting from the given node.
    private TreeObject searchHelper(BTreeNode node, TreeObject key) {

        int i = 0;

        // this goes through the keys in the node until it finds a key that is greater than or equal
        // to the search key.
        while (i < node.numKeys && key.compareTo(node.keys[i]) > 0) {
            i++;
        }

        // if the key is found in the node, return it.
        if (i < node.numKeys && key.compareTo(node.keys[i]) == 0) {
            return node.keys[i];
        }

        // if the key is not found and the node is a leaf, then the key does not exist in the tree and we 
        // return null.
        if (node.isLeaf) {
            return null;
        }
        // if the key is not found and the node is not a leaf.
        return searchHelper(diskRead(node.childrenAddresses[i]), key);
    }

    // this method searches for a key in the tree starting from the given node.
    // It returns the BTreeNode the key is within if found, otherwise it returns null.
    private BTreeNode nodeSearchHelper(BTreeNode node, TreeObject key) {

        int i = 0;

        // this goes through the keys in the node until it finds a key that is greater than or equal
        // to the search key. If it finds a key that is equal to the search key, it returns it. If it
        // finds a key that is greater than the search key, it stops and goes to the appropriate child.
        while (i < node.numKeys && key.compareTo(node.keys[i]) > 0) {
            i++;
        }

        // if the key is found in the node, return it.
        if (i < node.numKeys && key.compareTo(node.keys[i]) == 0) {
            return node;
        }

        // if the key is not found and the node is a leaf, then the key does not exist in the tree and we 
        // return null.
        if (node.isLeaf) {
            return null;
        }
        // if the key is not found and the node is not a leaf, then we go to the appropriate child.
        return nodeSearchHelper(diskRead(node.childrenAddresses[i]), key);
    }

    // this method returns the total number of unique keys in the tree.
    public long getSize() {
        return size;
    }

    // this method returns the degree of the tree.
    public int getDegree() {
        return t;
    }

    // this method returns the number of nodes in the tree.
    public long getNumberOfNodes() {
        return nodeCount;
    }

    // this method calculates the height of the tree
    public int getHeight() {
        return getHeightMethod(root);
    }

    // this method calculates the height of the tree by recursively going down the leftmost path until 
    // it reaches a leaf node.
    private int getHeightMethod(BTreeNode node) {
        if (node.isLeaf) return 0;
        return 1 + getHeightMethod(diskRead(node.childrenAddresses[0]));
    }

    // this method returns an array of all the keys in the tree in sorted order. It does this by doing 
    // an inorder traversal.
    public String[] getSortedKeyArray() {
        ArrayList<String> list = new ArrayList<>();
        inorder(root, list);
        return list.toArray(new String[0]);
    }

    // this method does an inorder traversal of the tree and adds the keys to the list in sorted order.
    private void inorder(BTreeNode node, ArrayList<String> list) {
        if (node == null) return;
        // this goes through the keys and children in order. For each key, it first goes to the left child, 
        // then adds the key to the list, then goes to the right child.
        for (int i = 0; i < node.numKeys; i++) {
            if (!node.isLeaf) {
                inorder(diskRead(node.childrenAddresses[i]), list);
            }
            if (node.keys[i] != null) {
                list.add(node.keys[i].getKey());
            }
        }

        // this is for the last child if the node is not a leaf. The last child is at index numKeys.
        if (!node.isLeaf) {
            inorder(diskRead(node.childrenAddresses[node.numKeys]), list);
        }
    }

    /**
     * this method reads a BTreeNode from disk given its address.
     * @param diskAddress
     * @return
     * @throws Exception
     */
    private void writeTreeObject(TreeObject obj) {
        byte[] bytes = new byte[64];

        // this converts the key to bytes and writes it to the buffer. If the object is null, 
        // it writes an empty byte array and a count of 0.
        if (obj != null) {
            byte[] keyBytes = obj.getKey().getBytes();
            int length = Math.min(keyBytes.length, 64);
            Arrays.fill(bytes, (byte) 0);
            System.arraycopy(keyBytes, 0, bytes, 0, length);
            buffer.put(bytes);
            buffer.putLong(obj.getCount());
        // if the object is null, we write an empty byte array and a count of 0 to indicate that there is 
        // no key at this position in the node.
        } else {
            buffer.put(bytes);
            buffer.putLong(0);
        }
    }
    // this method reads a TreeObject from the buffer. It first reads 64 bytes for the key and then reads 
    // a long for the count.
    private TreeObject readTreeObject() {
        byte[] keyBytes = new byte[64];
        buffer.get(keyBytes);

        String key = new String(keyBytes).trim();
        long count = buffer.getLong();

        if (key.isEmpty()) return null;
        return new TreeObject(key, count);
    }

    private void diskWrite(BTreeNode node) {
        try {    
            file.position(node.address);
            buffer.clear();

            // write metadata
            buffer.putInt(node.numKeys);
            buffer.put((byte)(node.isLeaf ? 1 : 0));

            // write keys
            for (int i = 0; i < 2 * t - 1; i++) {
                writeTreeObject(node.keys[i]);
            }

            // write children addresses
            for (int i = 0; i < 2 * t; i++) {
                buffer.putLong(node.childrenAddresses[i]);
            }

            buffer.flip();
            file.write(buffer);
        } catch (Exception e) {
            System.err.println("Error writing node to disk at address: " + node.address);
            e.printStackTrace();
        }
    }

    /**
     * this method reads a BTreeNode from disk given its address. It first checks if the disk address is 0,
     * which indicates that the node does not exist on disk and returns null.
     * @param diskAddress
     * @return
     */
    public BTreeNode diskRead(long diskAddress) {
        if (diskAddress == 0) return null;

        BTreeNode node = null;

        try {
            file.position(diskAddress);
            
            buffer.clear();
            file.read(buffer);
            buffer.flip();

            // read metadata
            int numKeys = buffer.getInt();
            boolean isLeaf = (buffer.get() == 1);

            node = new BTreeNode(isLeaf);
            node.numKeys = numKeys;
            node.address = diskAddress;

            // read keys
            for (int i = 0; i < 2 * t - 1; i++) {
                node.keys[i] = readTreeObject();
            }

            for (int i = 0; i < 2 * t; i++) {
                node.childrenAddresses[i] = buffer.getLong();
            }
        } catch (IOException e) {
            System.err.println("Error reading node from disk at address: " + diskAddress);
            e.printStackTrace();
        }

        return node;
    }

    @Override
    public void dumpToFile(PrintWriter out) throws IOException {
        dumpHelper(root, out);
        out.flush();
    }

    /**
     * This method does inorder traversal of the tree and writes the keys and frequencies to the output file in sorted order
     * @param node the current node being traversed
     * @param out the PrintWriter object used to write to the output file
     */
    private void dumpHelper(BTreeNode node, PrintWriter out) {
        if (node == null) return;
        for (int i = 0; i < node.numKeys; i++) {
            if (!node.isLeaf) {
                dumpHelper(diskRead(node.childrenAddresses[i]), out);
            }
            if (node.keys[i] != null) {
                out.println(node.keys[i].toString());
            }
        }

        // this is for the last child if the node is not a leaf. The last child is at index numKeys.
        if (!node.isLeaf) {
            dumpHelper(diskRead(node.childrenAddresses[node.numKeys]), out);
        }
    }


    @Override
    public void dumpToDatabase(String dbName, String tableName) throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'dumpToDatabase'");
    }

    @Override
    public void delete(String key) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }
}