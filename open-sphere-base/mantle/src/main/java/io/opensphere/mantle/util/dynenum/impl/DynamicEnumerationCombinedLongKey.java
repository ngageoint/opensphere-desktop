package io.opensphere.mantle.util.dynenum.impl;

import io.opensphere.mantle.util.dynenum.util.DynamicEnumerationLongKeyUtility;

/**
 * The Class DynamicEnumerationByteKey.
 */
@SuppressWarnings("PMD.AvoidUsingShortType")
public class DynamicEnumerationCombinedLongKey extends AbstractDynamicEnumerationKey
{
    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /** The Type id. */
    private final long myCompositeKey;

    /**
     * Instantiates a new dynamic enumeration combined long key.
     *
     * @param compositeKey the composite key
     */
    public DynamicEnumerationCombinedLongKey(long compositeKey)
    {
        myCompositeKey = compositeKey;
    }

    /**
     * Instantiates a new dynamic enumeration byte/byte/byte key.
     *
     * @param typeId the type id
     * @param mdiKeyId the mdi key id
     * @param valueId the value id
     */
    public DynamicEnumerationCombinedLongKey(short typeId, short mdiKeyId, short valueId)
    {
        myCompositeKey = DynamicEnumerationLongKeyUtility.createCombinedLongKeyValue(typeId, mdiKeyId, valueId);
    }

    /**
     * Gets the composite key.
     *
     * @return the composite key
     */
    public long getCompositeKey()
    {
        return myCompositeKey;
    }

    @Override
    public short getMetaDataKeyId()
    {
        return DynamicEnumerationLongKeyUtility.extractMdkIdFromCombinedLongKey(myCompositeKey);
    }

    @Override
    public short getTypeId()
    {
        return DynamicEnumerationLongKeyUtility.extractTypeIdFromCombinedLongKey(myCompositeKey);
    }

    @Override
    public short getValueId()
    {
        return DynamicEnumerationLongKeyUtility.extractValIdFromCombinedLongKey(myCompositeKey);
    }
}
