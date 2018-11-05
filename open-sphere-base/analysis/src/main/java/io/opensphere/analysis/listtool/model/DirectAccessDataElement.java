package io.opensphere.analysis.listtool.model;

import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.cache.DirectAccessRetriever;
import io.opensphere.mantle.data.cache.impl.DefaultDirectAccessRetriever;
import io.opensphere.mantle.data.cache.impl.DiskCacheDirectAccessRetriever;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.element.MetaDataProvider;
import io.opensphere.mantle.data.element.VisualizationState;

/**
 * A fake DataElement that delegates supported operations to the
 * DirectAccessRetriever for performance.
 */
class DirectAccessDataElement implements DataElement
{
    /** The cache id. */
    private final long myCacheId;

    /** The direct access retriever. */
    private final DirectAccessRetriever myDirectAccessRetriever;

    /** The meta data provider. */
    private final DirectAccessMetaDataProvider myMetaDataProvider;

    /**
     * Constructor.
     *
     * @param cacheId The cache id
     * @param directAccessRetriever The direct access retriever
     */
    public DirectAccessDataElement(long cacheId, DirectAccessRetriever directAccessRetriever)
    {
        myCacheId = cacheId;
        myDirectAccessRetriever = directAccessRetriever;
        myMetaDataProvider = new DirectAccessMetaDataProvider(cacheId, directAccessRetriever);
    }

    @Override
    public DataTypeInfo getDataTypeInfo()
    {
        return myDirectAccessRetriever.getDataType();
    }

    @Override
    public long getId()
    {
        throw new UnsupportedOperationException("getId() is not supported for DirectAccessDataElement");
    }

    @Override
    public long getIdInCache()
    {
        return myCacheId;
    }

    @Override
    public MetaDataProvider getMetaData()
    {
        return myMetaDataProvider;
    }

    @Override
    public TimeSpan getTimeSpan()
    {
        return myDirectAccessRetriever.getTimeSpan(myCacheId);
    }

    @Override
    public VisualizationState getVisualizationState()
    {
        return myDirectAccessRetriever.getVisualizationState(myCacheId);
    }

    @Override
    public boolean isDisplayable()
    {
        throw new UnsupportedOperationException("isDisplayable() is not supported for DirectAccessDataElement");
    }

    @Override
    public boolean isMappable()
    {
        throw new UnsupportedOperationException("isMappable() is not supported for DirectAccessDataElement");
    }

    @Override
    public void setDisplayable(boolean displayable, Object source)
    {
        throw new UnsupportedOperationException("setDisplayable() is not supported for DirectAccessDataElement");
    }

    @Override
    public void setIdInCache(long cacheId)
    {
    }

    /**
     * Gets the cacheId.
     *
     * @return the cacheId
     */
    protected long getCacheId()
    {
        return myCacheId;
    }

    /**
     * Gets the directAccessRetriever.
     *
     * @return the directAccessRetriever
     */
    protected DirectAccessRetriever getDirectAccessRetriever()
    {
        return myDirectAccessRetriever;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.element.DataElement#cloneForDatatype(io.opensphere.mantle.data.DataTypeInfo)
     */
    @Override
    public DataElement cloneForDatatype(DataTypeInfo datatype)
    {
        DirectAccessRetriever cloneRetriever;

        if (myDirectAccessRetriever instanceof DiskCacheDirectAccessRetriever)
        {
            DiskCacheDirectAccessRetriever original = (DiskCacheDirectAccessRetriever)myDirectAccessRetriever;

            cloneRetriever = new DiskCacheDirectAccessRetriever(original.getDiskCacheAssistant(), datatype,
                    original.getCacheRefMap(), original.getDynamicMetadataManager());
        }
        else if (myDirectAccessRetriever instanceof DefaultDirectAccessRetriever)
        {
            DefaultDirectAccessRetriever original = (DefaultDirectAccessRetriever)myDirectAccessRetriever;
            cloneRetriever = new DefaultDirectAccessRetriever(datatype, original.getCacheRefMap(),
                    original.getDynamicMetadataManager());
        }
        else
        {
            throw new UnsupportedOperationException(
                    "Unable to clone. Unclonable direct access retriever: " + myDirectAccessRetriever.getClass().getName());
        }

        DirectAccessDataElement clone = new DirectAccessDataElement(myCacheId * 10, cloneRetriever);
        return clone;
    }
}
