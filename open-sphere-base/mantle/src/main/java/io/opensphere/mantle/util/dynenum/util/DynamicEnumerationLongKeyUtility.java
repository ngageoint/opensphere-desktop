package io.opensphere.mantle.util.dynenum.util;

import io.opensphere.mantle.util.bitmanip.BitMaskGenerator;

/**
 * The Class DynamicEnumerationKeyUtility.
 */
@SuppressWarnings("PMD.AvoidUsingShortType")
public final class DynamicEnumerationLongKeyUtility
{
    /** The our long mdi key id bit shift. */
    public static final long ourLongMdiKeyIdBitShift = 17;

    /** The our long type id bit shift. */
    public static final long ourLongTypeIdBitShift = 35;

    /** The Constant ourLongMdiKeyIdBitMask. */
    private static final long ourLongMdiKeyIdBitMask = BitMaskGenerator.createLongRangedBitMask(17, 33);

    /** The Constant ourLongMdiKeyIdSignBitMask. */
    private static final long ourLongMdiKeyIdSignBitMask = BitMaskGenerator.createLongRangedBitMask(34, 34);

    /** The Constant ourLongTypeIdBitMask. */
    private static final long ourLongTypeIdBitMask = BitMaskGenerator.createLongRangedBitMask(35, 51);

    /** The Constant ourLongTypeIdSignBitMask. */
    private static final long ourLongTypeIdSignBitMask = BitMaskGenerator.createLongRangedBitMask(52, 52);

    /** The Constant ourLongValueIdBitMask. */
    private static final long ourLongValueIdBitMask = BitMaskGenerator.createLongRangedBitMask(0, 15);

    /** The Constant ourLongValueIdSignBitMask. */
    private static final long ourLongValueIdSignBitMask = BitMaskGenerator.createLongRangedBitMask(16, 16);

//    private final int ourIntTypeIdBitMask = BitMaskGenerator.createIntRangedBitMask(16, 23);
//
//    private final int ourIntMdiKeyIdBitMask = BitMaskGenerator.createIntRangedBitMask(8, 15);
//
//    private final int ourIntValueIdBitMask = BitMaskGenerator.createIntRangedBitMask(0, 7);
//
//    public static long ourIntTypeIdBitShift = 16;
//
//    public static long ourIntMdiKeyIdBitShift = 8;

    /**
     * Creates a composite long that represents the composite of the three key
     * components.
     *
     * @param dataTypeId the data type id
     * @param mdiKeyId the mdi key id
     * @param valueIdKey the value id key
     * @return the combined key value as a long.
     */
    public static long createCombinedLongKeyValue(short dataTypeId, short mdiKeyId, short valueIdKey)
    {
        long typeId = dataTypeId < 0L ? -1L * dataTypeId : dataTypeId;
        typeId = typeId << ourLongTypeIdBitShift;

        long keyId = mdiKeyId < 0L ? -1L * mdiKeyId : mdiKeyId;
        keyId = keyId << ourLongMdiKeyIdBitShift;

        long valueId = valueIdKey < 0L ? -1L * valueIdKey : valueIdKey;
        long combined = typeId ^ keyId ^ valueId;
        if (dataTypeId < 0)
        {
            combined = combined ^ ourLongTypeIdSignBitMask;
        }

        if (mdiKeyId < 0)
        {
            combined = combined ^ ourLongMdiKeyIdSignBitMask;
        }

        if (valueIdKey < 0)
        {
            combined = combined ^ ourLongValueIdSignBitMask;
        }

        return combined;
    }

    /**
     * Extract metadata info key id from combined long key.
     *
     * @param combinedKey the combined key
     * @return the metadata info key id
     */
    public static short extractMdkIdFromCombinedLongKey(long combinedKey)
    {
        long mdkId = combinedKey & ourLongMdiKeyIdBitMask;
        mdkId = mdkId >> ourLongMdiKeyIdBitShift;
        if ((combinedKey & ourLongMdiKeyIdSignBitMask) == ourLongMdiKeyIdSignBitMask)
        {
            mdkId = -1 * mdkId;
        }
        return (short)mdkId;
    }

    /**
     * Extract type id from combined long key.
     *
     * @param combinedKey the combined key
     * @return the short
     */
    public static short extractTypeIdFromCombinedLongKey(long combinedKey)
    {
        long typeId = combinedKey & ourLongTypeIdBitMask;
        typeId = typeId >> ourLongTypeIdBitShift;

        if ((combinedKey & ourLongTypeIdSignBitMask) == ourLongTypeIdSignBitMask)
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
    public static short extractValIdFromCombinedLongKey(long combinedKey)
    {
        long valId = combinedKey & ourLongValueIdBitMask;
        if ((combinedKey & ourLongValueIdSignBitMask) == ourLongValueIdSignBitMask)
        {
            valId = -1 * valId;
        }
        return (short)valId;
    }

    /**
     * Instantiates a new dynamic enumeration long key utility.
     */
    private DynamicEnumerationLongKeyUtility()
    {
        // Don't allow.
    }
}
