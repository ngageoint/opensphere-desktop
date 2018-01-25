package io.opensphere.mantle.util.dynenum.util;

import io.opensphere.mantle.util.bitmanip.BitMaskGenerator;

/**
 * The Class DynamicEnumerationKeyUtility.
 */
@SuppressWarnings("PMD.AvoidUsingShortType")
public final class DynamicEnumerationIntKeyUtility
{
    /** The our int mdi key id bit shift. */
    public static final int ourIntMdiKeyIdBitShift = 9;

    /** The our int type id bit shift. */
    public static final int ourIntTypeIdBitShift = 18;

    /** The Constant ourIntMdiKeyIdBitMask. */
    private static final int ourIntMdiKeyIdBitMask = BitMaskGenerator.createIntRangedBitMask(9, 16);

    /** The Constant ourIntMdiKeyIdSignBitMask. */
    private static final int ourIntMdiKeyIdSignBitMask = BitMaskGenerator.createIntRangedBitMask(17, 17);

    /** The Constant ourIntTypeIdBitMask. */
    private static final int ourIntTypeIdBitMask = BitMaskGenerator.createIntRangedBitMask(18, 25);

    /** The Constant ourIntTypeIdSignBitMask. */
    private static final int ourIntTypeIdSignBitMask = BitMaskGenerator.createIntRangedBitMask(26, 26);

    /** The Constant ourIntValueIdBitMask. */
    private static final int ourIntValueIdBitMask = BitMaskGenerator.createIntRangedBitMask(0, 7);

    /** The Constant ourIntValueIdSignBitMask. */
    private static final int ourIntValueIdSignBitMask = BitMaskGenerator.createIntRangedBitMask(8, 8);

    /**
     * Creates a composite long that represents the composite of the three key
     * components.
     *
     * @param dataTypeId the data type id
     * @param mdiKeyId the mdi key id
     * @param valueIdKey the value id key
     * @return the combined key value as a long.
     */
    public static int createCombinedIntKeyValue(byte dataTypeId, byte mdiKeyId, byte valueIdKey)
    {
        int typeId = dataTypeId < 0 ? -1 * dataTypeId : dataTypeId;
        typeId = typeId << ourIntTypeIdBitShift;

        int keyId = mdiKeyId < 0 ? -1 * mdiKeyId : mdiKeyId;
        keyId = keyId << ourIntMdiKeyIdBitShift;

        int valueId = valueIdKey < 0 ? -1 * valueIdKey : valueIdKey;
        int combined = typeId ^ keyId ^ valueId;
        if (dataTypeId < 0)
        {
            combined = combined ^ ourIntTypeIdSignBitMask;
        }

        if (mdiKeyId < 0)
        {
            combined = combined ^ ourIntMdiKeyIdSignBitMask;
        }

        if (valueIdKey < 0)
        {
            combined = combined ^ ourIntValueIdSignBitMask;
        }

        return combined;
    }

    /**
     * Extract metadata info key id from combined int key.
     *
     * @param combinedKey the combined key
     * @return the metadata info key id
     */
    public static short extractMdkIdFromCombinedIntKey(int combinedKey)
    {
        int mdkId = combinedKey & ourIntMdiKeyIdBitMask;
        mdkId = mdkId >> ourIntMdiKeyIdBitShift;
        if ((combinedKey & ourIntMdiKeyIdSignBitMask) == ourIntMdiKeyIdSignBitMask)
        {
            mdkId = -1 * mdkId;
        }
        return (short)mdkId;
    }

    /**
     * Extract type id from combined int key.
     *
     * @param combinedKey the combined key
     * @return the byte
     */
    public static short extractTypeIdFromCombinedIntKey(int combinedKey)
    {
        int typeId = combinedKey & ourIntTypeIdBitMask;
        typeId = typeId >> ourIntTypeIdBitShift;

        if ((combinedKey & ourIntTypeIdSignBitMask) == ourIntTypeIdSignBitMask)
        {
            typeId = -1 * typeId;
        }
        return (short)typeId;
    }

    /**
     * Extract the value id from combined long key.
     *
     * @param combinedKey the combined key
     * @return the value id as a short.
     */
    public static short extractValIdFromCombinedIntKey(int combinedKey)
    {
        int valId = combinedKey & ourIntValueIdBitMask;
        if ((combinedKey & ourIntValueIdSignBitMask) == ourIntValueIdSignBitMask)
        {
            valId = -1 * valId;
        }
        return (short)valId;
    }

    /**
     * Instantiates a new dynamic enumeration long key utility.
     */
    private DynamicEnumerationIntKeyUtility()
    {
        // Don't allow.
    }
}
