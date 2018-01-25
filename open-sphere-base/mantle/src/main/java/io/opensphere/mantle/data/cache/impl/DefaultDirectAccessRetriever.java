package io.opensphere.mantle.data.cache.impl;

import java.util.List;
import java.util.Map;

import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.Utilities;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.cache.DirectAccessRetriever;
import io.opensphere.mantle.data.dynmeta.DynamicMetadataDataTypeController;
import io.opensphere.mantle.data.dynmeta.impl.DynamicMetaDataListViewProxy;
import io.opensphere.mantle.data.dynmeta.impl.DynamicMetadataManagerImpl;
import io.opensphere.mantle.data.element.VisualizationState;
import io.opensphere.mantle.data.geom.MapGeometrySupport;

/**
 * The Class DefaultDirectAccessRetriever. A default implementation of the
 * DirectAccessRetriever. If used in this configuration it can only retrieve
 * from the in-memory cache. Sub-classes of this class are expected to be able
 * to handle working with or be provided by the various assistants.
 */
public class DefaultDirectAccessRetriever implements DirectAccessRetriever
{
    /** The my cache ref map. */
    private final Map<Long, CacheEntry> myCacheRefMap;

    /** The my data type. */
    private final DataTypeInfo myDataType;

    /** The Dynamic column coordinator. */
    private final DynamicMetadataDataTypeController myDynamicColumnCoordinator;

    /**
     * Instantiates a new default direct access retriever.
     *
     * @param dti the dti
     * @param cacheRefMap the cache ref map
     * @param dcm the dcm
     */
    public DefaultDirectAccessRetriever(DataTypeInfo dti, Map<Long, CacheEntry> cacheRefMap, DynamicMetadataManagerImpl dcm)
    {
        myDataType = dti;
        myCacheRefMap = cacheRefMap;
        myDynamicColumnCoordinator = dcm.getController(myDataType.getTypeKey());
    }

    @Override
    public void close()
    {
    }

    /**
     * Gets the meta data from in memory cache.
     *
     * @param ce the ce
     * @return the meta data from in memory cache
     */
    public final List<Object> extractMetaDataFromEntryIfAvailable(CacheEntry ce)
    {
        Utilities.checkNull(ce, "ce");
        LoadedElementData led = ce.getLoadedElementData();
        if (led != null && led.getMetaData() != null)
        {
            return led.getMetaData();
        }
        return null;
    }

    /**
     * Gets the map geometry support from in memory cache.
     *
     * @param ce the ce
     * @return the map geometry support from in memory cache
     */
    public final MapGeometrySupport extractMGSFromEntryIfAvailable(CacheEntry ce)
    {
        Utilities.checkNull(ce, "ce");
        LoadedElementData led = ce.getLoadedElementData();
        if (led != null && led.getMapGeometrySupport() != null)
        {
            return led.getMapGeometrySupport();
        }
        return null;
    }

    /**
     * Extract origin id from entry if available.
     *
     * @param ce the ce
     * @return the long
     */
    public final Long extractOriginIdFromEntryIfAvailable(CacheEntry ce)
    {
        Utilities.checkNull(ce, "ce");
        LoadedElementData led = ce.getLoadedElementData();
        if (led != null && led.getOriginId() != null)
        {
            return led.getOriginId();
        }
        return null;
    }

    /**
     * Gets the cache entry.
     *
     * @param cacheId the cache id
     * @return the cache entry
     */
    public final CacheEntry getCacheEntry(long cacheId)
    {
        return getCacheEntryForCacheId(cacheId);
    }

    @Override
    public DataTypeInfo getDataType()
    {
        return myDataType;
    }

    @Override
    public MapGeometrySupport getMapGeometrySupport(long cacheId)
    {
        MapGeometrySupport mgs = null;
        CacheEntry ce = getCacheEntryForCacheId(cacheId);
        if (ce != null)
        {
            mgs = extractMGSFromEntryIfAvailable(ce);
        }
        return mgs;
    }

    @Override
    public List<Object> getMetaData(long cacheId)
    {
        List<Object> metaData = null;
        CacheEntry ce = getCacheEntryForCacheId(cacheId);
        if (ce != null)
        {
            metaData = extractMetaDataFromEntryIfAvailable(ce);
            metaData = new DynamicMetaDataListViewProxy(cacheId, metaData, myDynamicColumnCoordinator);
        }
        return metaData;
    }

    @Override
    public Long getOriginId(long cacheId)
    {
        Long originId = null;
        CacheEntry ce = getCacheEntryForCacheId(cacheId);
        if (ce != null)
        {
            originId = extractOriginIdFromEntryIfAvailable(ce);
        }
        return originId;
    }

    @Override
    public TimeSpan getTimeSpan(long cacheId)
    {
        TimeSpan ts = null;
        CacheEntry ce = null;
        ce = getCacheEntryForCacheId(cacheId);
        if (ce != null)
        {
            ts = ce.getTime();
        }
        return ts;
    }

    @Override
    public VisualizationState getVisualizationState(long cacheId)
    {
        VisualizationState vs = null;
        CacheEntry ce = getCacheEntryForCacheId(cacheId);
        if (ce != null)
        {
            vs = ce.getVisState();
        }
        return vs;
    }

    /**
     * Gets the dynamic column coordinator.
     *
     * @return the dynamic column coordinator
     */
    protected DynamicMetadataDataTypeController getDynamicColumnCoordinator()
    {
        return myDynamicColumnCoordinator;
    }

    /**
     * Gets the cache entry for cache id.
     *
     * @param cacheId the cache id
     * @return the cache entry for cache id
     */
    private CacheEntry getCacheEntryForCacheId(long cacheId)
    {
        return myCacheRefMap.get(cacheId);
    }
}
