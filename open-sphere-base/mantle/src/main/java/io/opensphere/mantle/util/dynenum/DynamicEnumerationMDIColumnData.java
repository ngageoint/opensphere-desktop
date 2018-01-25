package io.opensphere.mantle.util.dynenum;

/**
 * The Interface DynamicEnumerationMDIColumnData.
 */
@SuppressWarnings("PMD.AvoidUsingShortType")
public interface DynamicEnumerationMDIColumnData
{
    /**
     * Adds the value to the enumeration if not already in the set.
     *
     * @param value the {@link DynamicEnumerationKey} for the value or null if
     *            the value was null.
     * @return the dynamic enumeration value
     */
    DynamicEnumerationKey addValue(Object value);

    /**
     * Clear all.
     */
    void clearAll();

    /**
     * Gets the data type key.
     *
     * @return the data type key
     */
    String getDataTypeKey();

    /**
     * Gets the enumeration value given a DynamicEnumerationKey.
     *
     * @param key the {@link DynamicEnumerationKey}
     * @return the enumeration value or null if the key is not in the data set
     *         or the key type id is not this sets type id.
     */
    Object getEnumerationValue(DynamicEnumerationKey key);

    /**
     * Gets the underlying enumeration value for the value id.
     *
     * @param valueId the value id to retrieve.
     * @return the value or null if the valueId is not in the set.
     */
    Object getEnumerationValue(short valueId);

    /**
     * Gets the {@link DynamicEnumerationKey} for the id.
     *
     * @param valueId the value id
     * @return the value
     */
    DynamicEnumerationKey getKey(short valueId);

    /**
     * Gets the key class.
     *
     * @return the key class
     */
    Class<?> getKeyClass();

    /**
     * Gets the meta data key id.
     *
     * @return the meta data key id
     */
    int getMetaDataKeyId();

    /**
     * Gets the meta data key name.
     *
     * @return the meta data key name
     */
    String getMetaDataKeyName();

    /**
     * Gets the type id.
     *
     * @return the type id
     */
    int getTypeId();

    /**
     * Gets the total number of unique values for the enumeration.
     *
     * @return the unique enumeration value count
     */
    int getValueCount();
}
