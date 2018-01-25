package io.opensphere.mantle.util.dynenum.util;

/**
 * The Class ColumnAndValueIntKeyUtility.
 */
@SuppressWarnings("PMD.AvoidUsingShortType")
public final class ColumnAndValueIntKeyUtility
{
    /** The Constant RIGHT. */
    private static final int RIGHT = 0xFFFF;

    /**
     * Creates a composite long that represents the composite of the three key
     * components.
     *
     * @param mdiKeyId the mdi key id
     * @param valueIdKey the value id key
     * @return the combined key value as a long.
     */
    public static int createCombinedIntKeyValue(short mdiKeyId, short valueIdKey)
    {
        return mdiKeyId << 16 | valueIdKey & RIGHT;
    }

    /**
     * Extract metadata info key id from combined long key.
     *
     * @param combinedKey the combined key
     * @return the metadata info key id
     */
    public static short extractMdkIdFromCombinedIntKey(int combinedKey)
    {
        return (short)(combinedKey >> 16);
    }

    /**
     * Extract the value id from combined long key.
     *
     * @param combinedKey the combined key
     * @return the value id as a short.
     */
    public static short extractValIdFromCombinedIntKey(int combinedKey)
    {
        return (short)(combinedKey & RIGHT);
    }

    /**
     * Instantiates a new dynamic enumeration long key utility.
     */
    private ColumnAndValueIntKeyUtility()
    {
        // Don't allow.
    }
}
