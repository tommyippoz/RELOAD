/**
 * 
 */
package ippoz.reload.commons.utils;


/**
 * @author Tommy
 *
 */
public class ObjectPair<K, V> {

    /**
     * Key of this <code>Pair</code>.
     */
    private K key;

    /**
     * Gets the key for this pair.
     * @return key for this pair
     */
    public K getKey() {
        return key;
    }

    /**
     * Value of this this <code>Pair</code>.
     */
    private V value;

    /**
     * Gets the value for this pair.
     * @return value for this pair
     */
    public V getValue() {
        return value;
    }

    /**
     * Creates a new pair
     * @param key The key for this pair
     * @param value The value to use for this pair
     */
    public ObjectPair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String toString() {
        return key + "=" + value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o instanceof ObjectPair) {
        	ObjectPair pair = (ObjectPair) o;
            if (key != null ? !key.equals(pair.key) : pair.key != null)
                return false;
            if (value != null ? !value.equals(pair.value) : pair.value != null)
                return false;
            return true;
        }
        return false;
    }
	
	

}
