package io.opensphere.mantle.data.cache.impl;

import java.util.List;

import io.opensphere.mantle.data.geom.MapGeometrySupport;

/**
 * The Class RetrieveResult.
 */
public class RegistryCacheRetrieveResult
{
    /** The my map geometry support. */
    private final MapGeometrySupport myMapGeometrySupport;

    /** The my meta data. */
    private final List<Object> myMetaData;

    /** The my origin id. */
    private final Long myOriginId;

    /** The my reg id. */
    private final long myRegId;

    /**
     * Instantiates a new retrieve result.
     *
     * @param regId the reg id
     * @param originId the origin id
     * @param metaData the meta data
     * @param mgs the mgs
     */
    public RegistryCacheRetrieveResult(long regId, Long originId, List<Object> metaData, MapGeometrySupport mgs)
    {
        myRegId = regId;
        myOriginId = originId;
        myMetaData = metaData;
        myMapGeometrySupport = mgs;
    }

    /**
     * Gets the map geometry support.
     *
     * @return the map geometry support
     */
    public MapGeometrySupport getMapGeometrySupport()
    {
        return myMapGeometrySupport;
    }

    /**
     * Gets the meta data.
     *
     * @return the meta data
     */
    public List<Object> getMetaData()
    {
        return myMetaData;
    }

    /**
     * Gets the origin id.
     *
     * @return the origin id
     */
    public Long getOriginId()
    {
        return myOriginId;
    }

    /**
     * Gets the reg id.
     *
     * @return the reg id
     */
    public long getRegId()
    {
        return myRegId;
    }
}
