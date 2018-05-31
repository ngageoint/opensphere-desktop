package io.opensphere.infinity.util;

import io.opensphere.mantle.data.DataTypeInfo;

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
        return dataType.hasTag(".es-url");
    }

    /** Disallow instantiation. */
    private InfinityUtilities()
    {
    }
}
