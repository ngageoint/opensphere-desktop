package io.opensphere.mantle.data.cache.impl;

import java.io.Serializable;

import io.opensphere.core.util.lang.BitArrays;
import io.opensphere.mantle.data.cache.CacheStoreType;

/**
 * The Interface CacheReference.
 */
@SuppressWarnings("PMD.AvoidUsingShortType")
public abstract class CacheReference implements Serializable
{
    /** Mask for lob visible flag. */
    public static final byte MAP_GEOMETRY_SUPPORT_CACHED = 4;

    /** Mask for Selection flag. */
    public static final byte META_DATA_INFO_CACHED = 1;

    /** Mask for visible flag. */
    public static final byte ORIGIN_ID_CACHED = 2;

    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The value of the highest bit defined by this class. This is to be used as
     * an offset by subclasses that need to define their own bits.
     */
    protected static final short HIGH_BIT = MAP_GEOMETRY_SUPPORT_CACHED;

    /** The my bit field. */
    private volatile byte myFlagField;

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }
        return myFlagField == ((CacheReference)obj).myFlagField;
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    public abstract CacheStoreType getType();

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + myFlagField;
        return result;
    }

    /**
     * Checks to see if a flag is set in the internal bit field.
     *
     * @param mask - the mask to check
     * @return true if set, false if not
     */
    public final boolean isFlagSet(byte mask)
    {
        return BitArrays.isFlagSet(mask, myFlagField);
    }

    /**
     * Checks if is map geometry support cached.
     *
     * @return true, if is map geometry support cached
     */
    public final boolean isMapGeometrySupportCached()
    {
        return isFlagSet(MAP_GEOMETRY_SUPPORT_CACHED);
    }

    /**
     * Checks if is meta data info cached.
     *
     * @return true, if is meta data info cached
     */
    public final boolean isMetaDataInfoCached()
    {
        return isFlagSet(META_DATA_INFO_CACHED);
    }

    /**
     * Checks if is origin id cached.
     *
     * @return true, if is origin id cached
     */
    public final boolean isOriginIdCached()
    {
        return isFlagSet(ORIGIN_ID_CACHED);
    }

    /**
     * Sets (or un-sets) a flag in the internal bit field.
     *
     * @param mask - the mask to use
     * @param on - true to set on, false to set off
     * @return true if changed.
     */
    public final boolean setFlag(byte mask, boolean on)
    {
        byte oldBitField = myFlagField;
        byte newBitField = BitArrays.setFlag(mask, on, oldBitField);
        boolean changed = newBitField != oldBitField;
        myFlagField = newBitField;
        return changed;
    }

    /**
     * Sets the map geometry support cached.
     *
     * @param selected the selected
     * @return true, if successful
     */
    public final boolean setMapGeometrySupportCached(boolean selected)
    {
        return setFlag(MAP_GEOMETRY_SUPPORT_CACHED, selected);
    }

    /**
     * Sets the meta data info cached.
     *
     * @param selected the selected
     * @return true, if successful
     */
    public final boolean setMetaDataInfoCached(boolean selected)
    {
        return setFlag(META_DATA_INFO_CACHED, selected);
    }

    /**
     * Sets the origin id cached.
     *
     * @param selected the selected
     * @return true, if successful
     */
    public final boolean setOriginIdCached(boolean selected)
    {
        return setFlag(ORIGIN_ID_CACHED, selected);
    }
}
