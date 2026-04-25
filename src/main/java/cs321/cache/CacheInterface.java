package cs321.cache;

/**
 * Interface that provides the capability of a cache
 * @author CS321 Instructors
 */

public interface CacheInterface<K, V extends KeyInterface<K>> {

    /**
     * Whenever an application requires a specific object, it searches/reads the cache using a key.  The cache hits when the key matches an object present in the cache, then returns the object to the calling program and moves it to the first position in the cache (Most Recently Used [MRU] scheme).  Alternatively, if the object is not present and the cache misses, the returned value is null.
     * @param key the key to be searched for
     * @return the value of type V associated with the key or null if not found
     */
    public V get(K key);

    /**
     * Adds an object to the first position of the cache. If the cache is full, the last entry (Least Recently Used (LRU) scheme) in the cache is removed before a new entry can be added. This is because the size of the cache is fixed. If an object is removed, it should be returned to the calling program, or null otherwise.
     * @param value the value to be added
     * @return Null if no objects are removed and value type V of the removed value if removed
     */
    public V add(V value);

    /**
     * Search the cache by key value, then remove the matched object and return it to the calling program. If the object wasn’t in the cache, then a null value is returned.
     * @param key the key to be removed
     * @return the value of type V that was removed or null if none were removed
     */
    public V remove(K key);

    /**
     * Clears all entries from the cache.
     */
    public void clear();

    /**
     * {@inheritDoc} 
     */
    public String toString();
}
