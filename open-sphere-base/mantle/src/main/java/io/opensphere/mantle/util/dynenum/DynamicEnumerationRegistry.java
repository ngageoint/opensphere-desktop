package io.opensphere.mantle.util.dynenum;

import io.opensphere.mantle.data.DataTypeInfo;

/**
 * The Interface DynamicEnumerationRegistry.
 */
public interface DynamicEnumerationRegistry
{
    /**
     * Adds the value to the enumeration specified with the type key and
     * metadata key.
     *
     * @param dtiKey the {@link DataTypeInfo} key.
     * @param metaDataKeyName the meta data key name
     * @param value the value to be added to the enumeration
     * @return the dynamic enumeration key for the value that was added (or the
     *         key for the existing value if it was already part of the set.)
     */
    DynamicEnumerationKey addValue(String dtiKey, String metaDataKeyName, Object value);

    /**
     * Creates an enumeration for a data type and meta data key column.
     *
     * @param dtiKey the {@link DataTypeInfo} key.
     * @param metaDataKeyName the meta data column key name
     * @param keyClass the key class
     */
    void createEnumeration(String dtiKey, String metaDataKeyName, Class<?> keyClass);

    /**
     * Destroys the enumeration for a specific data type and meta data key.
     *
     * @param dtiKey the {@link DataTypeInfo} key.
     * @param metaDataKeyName the meta data key name
     */
    void destroyEnumeration(String dtiKey, String metaDataKeyName);

    /**
     * Removes all dynamic enumerations from the registry for the specified data
     * type.
     *
     * @param dtiKey the {@link DataTypeInfo} key.
     */
    void destroyEnumerations(String dtiKey);

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
     * Gets the enumeration value for a given type id, meta data info key id,
     * and value id provided the type and value exist in the registry. Returns
     * null if type or value not found in the registry or the value in the
     * registry for that type and value id is null.
     *
     * @param typeId the type id
     * @param mdikeyId the meta data info key id
     * @param valueId the value id
     * @return the enumeration value
     */
    Object getEnumerationValue(int typeId, int mdikeyId, int valueId);
}
