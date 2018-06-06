package io.opensphere.infinity.util;

import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.server.services.AbstractServerDataTypeInfo;

/** Infinity utilities. */
public final class InfinityUtilities
{
    /**
     * Determines if the data type is infinity-enabled.
     *
     * @param dataType the data type
     * @return whether it's infinity-enabled
     */
    public static boolean isInfinityEnabled(DataTypeInfo dataType)
    {
        return dataType instanceof AbstractServerDataTypeInfo
                && dataType.getTags().stream().anyMatch(t -> t.startsWith(".es-url="));
    }

    /**
     * Gets the URL for the data type.
     *
     * @param dataType the data type
     * @return the URL, or null
     */
    public static String getUrl(DataTypeInfo dataType)
    {
        String url = getTagValue(".es-url", dataType);
        String index = getTagValue(".es-index", dataType);
        if (index != null)
        {
            url += "?" + index;
        }
        return url;
    }

    /**
     * Gets the value for the tag key.
     *
     * @param tagKey the tag key
     * @param dataType the data type
     * @return the value, or null
     */
    public static String getTagValue(String tagKey, DataTypeInfo dataType)
    {
        String completeKey = tagKey + "=";
        return dataType.getTags().stream().filter(t -> t.startsWith(completeKey)).map(t -> t.replace(completeKey, "")).findAny()
                .orElse(null);
    }

    /** Disallow instantiation. */
    private InfinityUtilities()
    {
    }
}
