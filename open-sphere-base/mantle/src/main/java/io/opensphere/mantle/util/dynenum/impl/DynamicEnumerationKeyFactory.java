package io.opensphere.mantle.util.dynenum.impl;

import io.opensphere.mantle.util.dynenum.DynamicEnumerationKey;

/**
 * A factory for creating DynamicEnumerationKey objects.
 */
@SuppressWarnings("PMD.AvoidUsingShortType")
public final class DynamicEnumerationKeyFactory
{
    /**
     * Creates a new DynamicEnumerationKey object.
     *
     * @param dataTypeId the data type id
     * @param mdiKeyId the mdi key id
     * @param valueId the value id
     * @return the dynamic enumeration key
     */
    public static DynamicEnumerationKey createKey(short dataTypeId, short mdiKeyId, short valueId)
    {
        DynamicEnumerationKey key = null;
        if (dataTypeId >= Byte.MIN_VALUE && dataTypeId <= Byte.MAX_VALUE && mdiKeyId >= Byte.MIN_VALUE
                && mdiKeyId <= Byte.MAX_VALUE && valueId >= Byte.MIN_VALUE && valueId <= Byte.MAX_VALUE)
        {
            key = new DynamicEnumerationCombinedIntKey((byte)dataTypeId, (byte)mdiKeyId, (byte)valueId);
        }
        else
        {
            key = new DynamicEnumerationCombinedLongKey(dataTypeId, mdiKeyId, valueId);
        }
        return key;
    }

    /**
     * Instantiates a new dynamic enumeration key factory.
     */
    private DynamicEnumerationKeyFactory()
    {
        // Don't allow instantiation.
    }
}
