package io.opensphere.mantle.util.dynenum.impl;

import io.opensphere.mantle.util.dynenum.util.DynamicEnumerationIntKeyUtility;
import io.opensphere.mantle.util.dynenum.util.DynamicEnumerationLongKeyUtility;

/**
 * The Class DynamicEnumerationCombinedIntKey.
 */
@SuppressWarnings("PMD.AvoidUsingShortType")
public class DynamicEnumerationCombinedIntKey extends AbstractDynamicEnumerationKey
{
    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /** The Type id. */
    private final int myCompositeKey;

    /**
     * Instantiates a new dynamic enumeration byte/byte/byte key.
     *
     * @param typeId the type id
     * @param mdiKeyId the mdi key id
     * @param valueId the value id
     */
    public DynamicEnumerationCombinedIntKey(byte typeId, byte mdiKeyId, byte valueId)
    {
        myCompositeKey = DynamicEnumerationIntKeyUtility.createCombinedIntKeyValue(typeId, mdiKeyId, valueId);
    }

    /**
     * Instantiates a new dynamic enumeration combined long key.
     *
     * @param compositeKey the composite key
     */
    public DynamicEnumerationCombinedIntKey(int compositeKey)
    {
        myCompositeKey = compositeKey;
    }

    /**
     * Gets the composite key.
     *
     * @return the composite key
     */
    public int getCompositeKey()
    {
        return myCompositeKey;
    }

    /**
     * Gets the composite long key.
     *
     * @return the composite long key
     */
    public long getCompositeLongKey()
    {
        return DynamicEnumerationLongKeyUtility.createCombinedLongKeyValue(getTypeId(), getMetaDataKeyId(), getValueId());
    }

    @Override
    public short getMetaDataKeyId()
    {
        return DynamicEnumerationIntKeyUtility.extractMdkIdFromCombinedIntKey(myCompositeKey);
    }

    @Override
    public short getTypeId()
    {
        return DynamicEnumerationIntKeyUtility.extractTypeIdFromCombinedIntKey(myCompositeKey);
    }

    @Override
    public short getValueId()
    {
        return DynamicEnumerationIntKeyUtility.extractValIdFromCombinedIntKey(myCompositeKey);
    }
}
