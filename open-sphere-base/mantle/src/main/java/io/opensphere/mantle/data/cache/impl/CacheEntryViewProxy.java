package io.opensphere.mantle.data.cache.impl;

import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.mantle.data.cache.CacheEntryView;
import io.opensphere.mantle.data.cache.LoadedElementDataView;
import io.opensphere.mantle.data.dynmeta.impl.DynamicMetadataManagerImpl;
import io.opensphere.mantle.data.element.VisualizationState;
import io.opensphere.mantle.util.dynenum.DynamicEnumerationRegistry;

/**
 * The Class CacheEntryViewProxy.
 */
public class CacheEntryViewProxy implements CacheEntryView
{
    /** The DC manager. */
    private final DynamicMetadataManagerImpl myDCManager;

    /** The entry. */
    private CacheEntry myEntry;

    /** The led. */
    private LoadedElementData myLed;

    /** The Dyn col list view proxy. */
    private final DynamicMetaDataLoadedElementDataViewProxy myLEDProxy;

    /**
     * Instantiates a new cache entry view proxy.
     *
     * @param dcm the dcm
     * @param deReg the de reg
     */
    public CacheEntryViewProxy(DynamicMetadataManagerImpl dcm, DynamicEnumerationRegistry deReg)
    {
        myDCManager = dcm;
        myLEDProxy = new DynamicMetaDataLoadedElementDataViewProxy(-1, null, null, deReg);
    }

    /**
     * Cache entry view.
     *
     * @param entryId the entry id
     * @param dcm the dcm
     * @param deReg the de reg
     * @param entry the entry
     * @param led the led
     */
    public CacheEntryViewProxy(long entryId, DynamicMetadataManagerImpl dcm, DynamicEnumerationRegistry deReg, CacheEntry entry,
            LoadedElementData led)
    {
        myDCManager = dcm;
        myEntry = entry;
        myLed = led;
        myLEDProxy = new DynamicMetaDataLoadedElementDataViewProxy(entryId, led,
                entry == null ? null : myDCManager.getController(entry.getDataTypeKey()), deReg);
    }

    /**
     * Clears the references for the cache entry and loaded element data.
     */
    public void clear()
    {
        myEntry = null;
        myLed = null;
    }

    @Override
    public String getDataTypeKey()
    {
        return myEntry.getDataTypeKey();
    }

    @Override
    public LoadedElementDataView getLoadedElementData()
    {
        return myLEDProxy;
    }

    @Override
    public TimeSpan getTime()
    {
        return myEntry.getTime();
    }

    @Override
    public VisualizationState getVisState()
    {
        return myEntry.getVisState();
    }

    @Override
    public boolean isCached()
    {
        return myEntry.isCached();
    }

    @Override
    public boolean isMapGeometrySupportCached()
    {
        return myEntry.isMapGeometrySupportCached();
    }

    @Override
    public boolean isMetaDataInfoCached()
    {
        return myEntry.isMetaDataInfoCached();
    }

    @Override
    public boolean isOriginIdCached()
    {
        return myEntry.isOriginIdCached();
    }

    /**
     * Sets the parts.
     *
     * @param entryId the entry id
     * @param entry the entry
     * @param led the led
     */
    public void setParts(long entryId, CacheEntry entry, LoadedElementData led)
    {
        myEntry = entry;
        myLed = led;
        myLEDProxy.setView(entryId, myLed, entry == null ? null : myDCManager.getController(entry.getDataTypeKey()));
    }
}
