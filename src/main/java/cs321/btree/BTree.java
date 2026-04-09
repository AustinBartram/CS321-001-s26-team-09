package cs321.btree;

import java.util.ArrayList;

public class BTree {

    private BTreeNode root;
    private int t; // degree

    private int size = 0;
    private int nodeCount = 1;


    //constructor (degree = 2)
    public BTree(String filename) {
        this(2, filename);
    }

    public BTree(int t, String filename) {
        this.t = t;
        root = new BTreeNode(true);
    }
    class BTreeNode {
        int numKeys;
        boolean isLeaf;
        TreeObject[] keys;
        BTreeNode[] children;

        BTreeNode(boolean isLeaf) {
            this.isLeaf = isLeaf;
            keys = new TreeObject[2 * t - 1];
            children = new BTreeNode[2 * t];
            numKeys = 0;
        }
    }

    public void insert(TreeObject key) {

        // duplicate maintenance: if key already exists, just increment count
        TreeObject existing = search(key.getKey());
        if (existing != null) {
            existing.incCount();
            return;
        }

        BTreeNode r = root;

        // If the root is full split it and create a new node. This increases the height. 
        if (r.numKeys == 2 * t - 1) {
            BTreeNode s = new BTreeNode(false);
            s.children[0] = r;
            root = s;

            // this splits the old root and moves the key up. The new node s becomes the new root and has one key and two children.
            splitChild(s, 0, r);
            insertHelper(s, key);
        } else {
            insertHelper(r, key);
        }

        // track total keys in the tree for unique keys.
        size++;
    }

    // this is the helper that does insertion. It finds the needed leaf and insert the key and if needed splits the child.
    // if the nodes not full it will just insert the key. But if it is full it splits the child.
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

        // if the node is not a lead then it neeeds to find the child. If the child is full then it should split then insert. 
        } else {
            while (i >= 0 && key.compareTo(node.keys[i]) < 0) {
                i--;
            }
            i++;

            // if the child is full then split it and then insert the key to the right child.
            if (node.children[i].numKeys == 2 * t - 1) {
                splitChild(node, i, node.children[i]);

                if (key.compareTo(node.keys[i]) > 0) {
                    i++;
                }
            }

            // insert the key to the right child.
            insertHelper(node.children[i], key);
        }
    }

    /**
     * Split the full child node and move the middle key up to the parent. This is a standard B-Tree split operation.
     * this method is called when a child node is full and we need to split it to maintain the B properties. 
     * @param parent
     * @param index
     * @param fullChild
     */
    private void splitChild(BTreeNode parent, int index, BTreeNode fullChild) {

        BTreeNode newNode = new BTreeNode(fullChild.isLeaf);
        newNode.numKeys = t - 1;

        // copy keys over to the new node. The middle key is at index t - 1 and will be shifted to the parent. the keys are copied from
        // t to 2t - 2 to the new node.
        for (int j = 0; j < t - 1; j++) {
            newNode.keys[j] = fullChild.keys[j + t];
        }

        // this copies the children if the full child is not a leaf. The children are copied from t to 2t - 1 to the new node.
        if (!fullChild.isLeaf) {
            for (int j = 0; j < t; j++) {
                newNode.children[j] = fullChild.children[j + t];
            }
        }

        // the full child now has only the first t - 1 keys and the new node has the last t - 1 keys. The middle key is moved up to the parent.
        fullChild.numKeys = t - 1;

        // this takes care of the parent node and shifts the children and the keys for a new spot to be open for the new node and the middle keys.
        for (int j = parent.numKeys; j >= index + 1; j--) {
            parent.children[j + 1] = parent.children[j];
        }

        // link the new node to the parent
        parent.children[index + 1] = newNode;

        // this shifts the keys in the parent over to make room for the middle keys from the full child that needs to be moved up. 
        // the middle key is moved up and the parents keys shift right. The middle key is at 't-1' and the parents idex is at 
        // 'index' and the keys are shifted from 'index' to 'numKeys - 1' to the right.
        for (int j = parent.numKeys - 1; j >= index; j--) {
            parent.keys[j + 1] = parent.keys[j];
        }

        // move the middle key up to the parent and increase the number of keys in the parent.
        parent.keys[index] = fullChild.keys[t - 1];
        parent.numKeys++;

        // track the new node created for stats.
        nodeCount++;
    }

    /**
     * Search for a key in the B-Tree. Returns the TreeObject if found, otherwise null. 
     * @param key
     * @return
     */
    public TreeObject search(String key) {
        return searchHelper(root, new TreeObject(key));
    }

    /**
     * Helper method for search. This is a standard B-Tree search operation. It traverses the tree based on the key comparisons until 
     * it finds the key or reaches a leaf node.
     * @param node
     * @param key
     * @return
     */
    private TreeObject searchHelper(BTreeNode node, TreeObject key) {

        int i = 0;

        // this loop traverses through the keys to find the right child node to go down, it then stops if the key is found or there is a key of 
        // greater value. If the key is found it should return the key. 
        while (i < node.numKeys && key.compareTo(node.keys[i]) > 0) {
            i++;
        }

        // if the key is found return it. If the key is not found and we are at a leaf node return null. Otherwise, continue searching down the tree.
        if (i < node.numKeys && key.compareTo(node.keys[i]) == 0) {
            return node.keys[i];
        }

        // if we are at a leaf node and the key is not found return null.
        if (node.isLeaf) {
            return null;
        }

        // continue searching down the tree.
        return searchHelper(node.children[i], key);
    }

    // getter for size
    public int getSize() {
        return size;
    }

    // getter for degree
    public int getDegree() {
        return t;
    }

    // getter for number of nodes in the tree.
    public int getNumberOfNodes() {
        return nodeCount;
    }

    // getter for height of the tree. This is calculated by traversing down the leftmost path of the tree until a leaf node is reached.
    // The number of edges traversed is the height of the tree.
    public int getHeight() {
        return getHeightMethod(root);
    }

    // this is the helper method for getHeight. It traverses down the leftmost path of the tree until a leaf node is reached.
    // The number of edges traversed is the height of the tree.
    private int getHeightMethod(BTreeNode node) {
        if (node.isLeaf) return 0;
        return 1 + getHeightMethod(node.children[0]);
    }

    // this method returns an array of the keys in the tree in sorted order. It does an inorder traversal of the tree and collects the keys 
    // in a list, then converts it to an array.
    public String[] getSortedKeyArray() {
        ArrayList<String> list = new ArrayList<>();
        inorder(root, list);
        return list.toArray(new String[0]);
    }

    /**
     * This is the helper method for getSortedKeyArray. It does an inorder traversal of the tree and collects the keys in a list. 
     * The inorder traversal visits the left child, then the key, then the right child. This ensures that the keys are collected in sorted order.
     * @param node
     * @param list
     */
    private void inorder(BTreeNode node, ArrayList<String> list) {

        for (int i = 0; i < node.numKeys; i++) {
            if (!node.isLeaf) {
                inorder(node.children[i], list);
            }
            list.add(node.keys[i].getKey());
        }

        // visit the last child if the node is not a leaf.
        if (!node.isLeaf) {
            inorder(node.children[node.numKeys], list);
        }
    }
}