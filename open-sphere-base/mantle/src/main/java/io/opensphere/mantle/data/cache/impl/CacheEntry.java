package io.opensphere.mantle.data.cache.impl;

import java.util.Objects;

import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.mantle.data.cache.CacheEntryView;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.element.DynamicMetaDataList;
import io.opensphere.mantle.data.element.VisualizationState;
import io.opensphere.mantle.util.TimeSpanUtility;
import io.opensphere.mantle.util.dynenum.DynamicEnumerationRegistry;

/**
 * The Class CacheEntry.
 */
class CacheEntry extends VisualizationState implements CacheEntryView
{
    /** The Constant ourStartTime. */
    private static final long ourStartTime = System.currentTimeMillis();

    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /** The USING_DYNAMIC_METADATA_LIST flag mask. */
    private static final byte USING_DYNAMIC_METADATA_LIST = HIGH_BIT + 8;

    /** The my cache reference. */
    private CacheReference myCacheReference;

    /** The my data type key. */
    // Could possibly shave off 2 bytes here by converting to a type id number.
    private final String myDataTypeKey;

    /** The end time. */
    private final long myEndTime;

    /** The my last used time. */
    private int myLastUsedTime;

    /** The my loaded element data. */
    private LoadedElementData myLoadedElementData;

    /** The my start time. */
    private final long myStartTime;

    /**
     * Instantiates a new cache ref.
     *
     * @param deReg the de reg
     * @param el the {@link DataElement}
     * @param useDynamicClasses the use dynamic classes
     */
    public CacheEntry(DynamicEnumerationRegistry deReg, DataElement el, boolean useDynamicClasses)
    {
        super(el.getVisualizationState());
        myStartTime = TimeSpanUtility.getWorkaroundStart(el.getTimeSpan());
        myEndTime = TimeSpanUtility.getWorkaroundEnd(el.getTimeSpan());
        myDataTypeKey = el.getDataTypeInfo().getTypeKey().intern();
        myLoadedElementData = new LoadedElementData(deReg, el, useDynamicClasses);
        setFlag(USING_DYNAMIC_METADATA_LIST, myLoadedElementData.getMetaData() instanceof DynamicMetaDataList);
        myLastUsedTime = (int)(System.currentTimeMillis() - ourStartTime);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!super.equals(obj) || getClass() != obj.getClass())
        {
            return false;
        }
        CacheEntry other = (CacheEntry)obj;
        return myEndTime == other.myEndTime && myLastUsedTime == other.myLastUsedTime && myStartTime == other.myStartTime
                && Objects.equals(myLoadedElementData, other.myLoadedElementData)
                && Objects.equals(myDataTypeKey, other.myDataTypeKey) && Objects.equals(myCacheReference, other.myCacheReference);
    }

    /**
     * Gets the cache reference.
     *
     * @return the cache reference
     */
    public CacheReference getCacheReference()
    {
        return myCacheReference;
    }

    @Override
    public String getDataTypeKey()
    {
        return myDataTypeKey;
    }

    /**
     * Gets the last used time.
     *
     * @return the last used time
     */
    public long getLastUsedTime()
    {
        return ourStartTime + myLastUsedTime;
    }

    @Override
    public LoadedElementData getLoadedElementData()
    {
        return myLoadedElementData;
    }

    @Override
    public TimeSpan getTime()
    {
        return TimeSpanUtility.fromStartEnd(myStartTime, myEndTime);
    }

    @Override
    public VisualizationState getVisState()
    {
        return this;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (myCacheReference == null ? 0 : myCacheReference.hashCode());
        result = prime * result + (myDataTypeKey == null ? 0 : myDataTypeKey.hashCode());
        result = prime * result + (int)(myEndTime ^ myEndTime >>> 32);
        result = prime * result + myLastUsedTime;
        result = prime * result + (myLoadedElementData == null ? 0 : myLoadedElementData.hashCode());
        result = prime * result + (int)(myStartTime ^ myStartTime >>> 32);
        return result;
    }

    @Override
    public boolean isCached()
    {
        return myCacheReference != null;
    }

    @Override
    public boolean isMapGeometrySupportCached()
    {
        return myCacheReference != null && myCacheReference.isMapGeometrySupportCached();
    }

    @Override
    public boolean isMetaDataInfoCached()
    {
        return myCacheReference != null && myCacheReference.isMetaDataInfoCached();
    }

    @Override
    public boolean isOriginIdCached()
    {
        return myCacheReference != null && myCacheReference.isOriginIdCached();
    }

    /**
     * Sets the cache reference.
     *
     * @param ref the new cache reference
     */
    public void setCacheReference(CacheReference ref)
    {
        myCacheReference = ref;
    }

    /**
     * Sets the last used time.
     *
     * @param time the new last used time
     */
    public void setLastUsedTime(long time)
    {
        myLastUsedTime = (int)(time - ourStartTime);
    }

    /**
     * Sets the loaded element data.
     *
     * @param led the new loaded element data
     */
    public void setLoadedElementData(LoadedElementData led)
    {
        myLoadedElementData = led;
    }
}
