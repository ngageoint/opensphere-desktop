package io.opensphere.mantle.data.element;

import java.io.Serializable;
import java.util.List;

import io.opensphere.mantle.data.DataTypeInfo;

/**
 * Interface for DataElements that provide Meta Data for tooling.
 */
public interface MetaDataProvider
{
    /**
     * Get the keys for the Metadata.
     *
     * Note: this should be an unmodifiable collection or a copy of the set of
     * keys, changes to the returned list will not impact the provider's key
     * set.
     *
     * @return the key set
     */
    List<String> getKeys();

    /**
     * Gets a value for a given key. Null returned if key does not exist
     *
     * @param key - the key
     * @return the value or null if no value for key
     */
    Object getValue(String key);

    /**
     * Gets an immutable list of values in key order. Note that some values may
     * be null.
     *
     * @return the values
     */
    List<Object> getValues();

    /**
     * Returns true if this MetaDataProvider has the key in its keyset.
     *
     * @param key - the key to check
     * @return true if it has the key, false if not
     */
    boolean hasKey(String key);

    /**
     * Returns true if the key set is mutable from within the provider. If this
     * is false removeKey(key) may throw {@link UnsupportedOperationException}:
     * and setValue(key,value) will return false for all adds for keys that are
     * not already in the key list.
     *
     *
     * @return true if keys are mutable on the fly, false if not.
     */
    boolean keysMutable();

    /**
     * Removes a key and its associated value from the provider. Optional
     * method: throw {@link UnsupportedOperationException} if not implemented.
     *
     * @param key - the key to remove
     */
    void removeKey(String key);

    /**
     * Sets the value for a key. Note that if this is not a keysMutable()
     * provider that using a key not in the key set will not make any change to
     * the set of data and the method will return false.
     *
     * @param key - the key
     * @param value - the value to set
     * @return true if set, false if not
     */
    boolean setValue(String key, Serializable value);

    /**
     * Returns true if the value set is mutable from within the provider. If
     * this is false removeKey(key) may throw
     * {@link UnsupportedOperationException}: and setValue(key,value) will
     * return false for all adds.
     *
     *
     *
     * @return true if keys are mutable on the fly, false if not.
     */
    boolean valuesMutable();

    MetaDataProvider createCopy(DataTypeInfo datatype);
}
