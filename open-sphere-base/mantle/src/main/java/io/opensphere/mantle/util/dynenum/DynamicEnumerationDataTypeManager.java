package io.opensphere.mantle.util.dynenum;

/**
 * The Interface DynamicEnumerationDataTypeManager.
 *
 * Manages all the collections of enumeration data for different meta data info
 * keys.
 */
@SuppressWarnings("PMD.AvoidUsingShortType")
public interface DynamicEnumerationDataTypeManager
{
    /**
     * Adds the value to the enumeration specified with the metadata key.
     *
     * @param metaDataKeyName the meta data key name
     * @param value the value to be added to the enumeration
     * @return the dynamic enumeration key for the value that was added (or the
     *         key for the existing value if it was already part of the set.)
     */
    DynamicEnumerationKey addValue(String metaDataKeyName, Object value);

    /**
     * Creates the enumeration data for mdi key name.
     *
     * @param keyName the key name
     * @param keyClass the key class
     * @return the dynamic enumeration mdi column data
     */
    DynamicEnumerationMDIColumnData createEnumerationDataForMDIKeyName(String keyName, Class<?> keyClass);

    /**
     * Gets the enumeration data for mdi key id.
     *
     * @param mdiKeyId the mdi key id
     * @return the enumeration data for mdi key id
     */
    DynamicEnumerationMDIColumnData getEnumerationDataForMDIKeyId(short mdiKeyId);

    /**
     * Gets the enumeration data for mdi key name.
     *
     * @param keyName the key name
     * @return the enumeration data for mdi key name or null if not in manager.
     */
    DynamicEnumerationMDIColumnData getEnumerationDataForMDIKeyName(String keyName);

    /**
     * Gets the enumeration value for a given {@link DynamicEnumerationKey}
     * provided the value exists in the registry.
     *
     * @param key the {@link DynamicEnumerationKey} to use for the lookup.
     * @return the enumeration value for the specified key or null if the value
     *         for that id is null or if the type and value do not occur in the
     *         registry.
     */
    Object getEnumerationValue(DynamicEnumerationKey key);

    /**
     * Gets the enumeration value for a given data info key id, and value id
     * provided the type and value exist in the registry. Returns null if type
     * or value not found in the registry or the value in the registry for that
     * type and value id is null.
     *
     * @param mdikeyId the meta data info key id
     * @param valueId the value id
     * @return the enumeration value
     */
    Object getEnumerationValue(short mdikeyId, short valueId);

    /**
     * Removes the all data for all type keys.
     */
    void removeAllData();

    /**
     * Removes enumeration data for a specific meta data key, provided that key
     * is used.
     *
     * @param keyName the key name to be removed.
     */
    void removeEnumerationDataForMdiKey(String keyName);

    /**
     * Removes the enumeration data for mdi key id provided that id is in use by
     * this type.
     *
     * @param mdiKeyId the mdi key id
     */
    void removeEnumerationDataForMdiKeyId(short mdiKeyId);
}
