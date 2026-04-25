package cs321.cache;

import java.util.LinkedList;

/**
 * Creates a cache and implements CacheInterface allowing for a get, add, remove, clear and toString method. Also it has a constructor to create a cache
 * @author Calvin McKee
 */
public class Cache<K, V extends KeyInterface<K>> implements CacheInterface<K, V> {

    private LinkedList<V> cache;
    private int maxSize;
    private int references;
    private int hits;

    /**
     * Creates a new cache set to a maximum size
     * @param size the maximum size of the cache
     */
    public Cache(int size){
        this.cache = new LinkedList<>();
        this.maxSize = size;
        this.references = 0;
        this.hits = 0;
    }

    @Override
    public V get(K key) {
        //add +1 reference
        references++;
        //search every value
        for (V value : cache) {
            //if the value has the key we are looking for run this
            if (value.getKey().equals(key)) {
                //add +1 hits
                hits++;
                //first remove and add to the front
                cache.remove(value);
                cache.addFirst(value);
                //the return the value
                return value;
            }
        }
        //if nothing is caught/found return null
        return null;
    }

    @Override
    public V add(V value) {
        //first check if it is already in the list
        for (V v : cache) {
            //if it is in the list, run this
            if (v.getKey().equals(value.getKey())) {
                //remove if in the list
                cache.remove(v);
                break;
            }
        }

        //add the value to the cache
        cache.addFirst(value);

        //check if size limit is met
        if (cache.size() == maxSize + 1) {
            //remove and return removed value
            return cache.removeLast();
        } else {
            return null;
        }
    }

    @Override
    public V remove(K key) {
        //check to see if the key is in the list
        for (V value : cache) {
            if (value.getKey().equals(key)) {
                //if it is in there, remove it and return that value
                cache.remove(value);
                return value;
            }
        }

        //if not in the list return null
        return null;
    }

    @Override
    public void clear() {
        cache.clear();
    }

    @Override
    public String toString() {
        //determine hit percent/ratio
        double hitRatio = 0.0;// incase there are no references
        
        if (references > 0) {
            hitRatio = (double) hits / (double) references;
        }
        
        //format the ratio
        java.text.DecimalFormat df = new java.text.DecimalFormat("0.00%");

        //build the string
        StringBuilder sb = new StringBuilder();
        sb.append("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
        sb.append("Cache with ").append(maxSize).append(" entries has been created\n");
        sb.append("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
        sb.append("Total number of references:        ").append(references).append("\n");
        sb.append("Total number of cache hits:        ").append(hits).append("\n");
        sb.append("Cache hit percent:                 ").append(df.format(hitRatio)).append("\n");

        //return the string
        return sb.toString();
    }
    
}
