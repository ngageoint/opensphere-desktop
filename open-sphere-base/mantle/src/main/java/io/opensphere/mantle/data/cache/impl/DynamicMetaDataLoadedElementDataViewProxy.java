package io.opensphere.mantle.data.cache.impl;

import java.util.List;

import io.opensphere.mantle.data.cache.LoadedElementDataView;
import io.opensphere.mantle.data.dynmeta.DynamicMetadataDataTypeController;
import io.opensphere.mantle.data.dynmeta.impl.DynamicMetaDataListViewProxy;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.util.dynenum.DynamicEnumerationRegistry;

/**
 * The Class DynamicMetaDataLoadedElementDataViewProxy.
 */
public class DynamicMetaDataLoadedElementDataViewProxy implements LoadedElementDataView
{
    /** The Cache id. */
    private long myCacheId;

    /** The Coordinator. */
    private DynamicMetadataDataTypeController myCoordinator;

    /** The Data view. */
    private LoadedElementDataView myDataView;

    /** The Dynamic enum registry. */
    private final DynamicEnumerationRegistry myDynamicEnumRegistry;

    /** The Meta data. */
    private List<Object> myMetaData;

    /**
     * Instantiates a new dynamic meta data loaded element data view proxy.
     *
     * @param cacheId the cache id
     * @param view the view
     * @param cdr the cdr
     * @param deReg the de reg
     */
    public DynamicMetaDataLoadedElementDataViewProxy(long cacheId, LoadedElementDataView view,
            DynamicMetadataDataTypeController cdr, DynamicEnumerationRegistry deReg)
    {
        myDynamicEnumRegistry = deReg;
        myCacheId = cacheId;
        myDataView = view;
        myCoordinator = cdr;
        List<Object> metaData = myDataView == null ? null : DynamicEnumDecoder.decode(deReg, myDataView.getMetaData());
        if (myCoordinator != null && myCoordinator.getDynamicColumnCount() > 0)
        {
            myMetaData = new DynamicMetaDataListViewProxy(myCacheId, metaData, myCoordinator);
        }
        if (myMetaData == null)
        {
            myMetaData = metaData;
        }
    }

    @Override
    public MapGeometrySupport getMapGeometrySupport()
    {
        return myDataView == null ? null : myDataView.getMapGeometrySupport();
    }

    @Override
    public List<Object> getMetaData()
    {
        return myMetaData;
    }

    @Override
    public Long getOriginId()
    {
        return myDataView == null ? null : myDataView.getOriginId();
    }

    /**
     * Sets the view.
     *
     * @param cacheId the cache id
     * @param view the view
     * @param controller the controller
     */
    public void setView(long cacheId, LoadedElementDataView view, DynamicMetadataDataTypeController controller)
    {
        myCacheId = cacheId;
        myDataView = view;
        myCoordinator = controller;
        myMetaData = null;
        List<Object> metaData = myDataView == null ? null
                : DynamicEnumDecoder.decode(myDynamicEnumRegistry, myDataView.getMetaData());
        if (myCoordinator != null && myCoordinator.getDynamicColumnCount() > 0)
        {
            myMetaData = new DynamicMetaDataListViewProxy(myCacheId, metaData, myCoordinator);
        }
        if (myMetaData == null)
        {
            myMetaData = metaData;
        }
    }
}
