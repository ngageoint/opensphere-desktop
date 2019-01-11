package io.opensphere.analysis.listtool.model;

import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.cache.DirectAccessRetriever;
import io.opensphere.mantle.data.cache.impl.DefaultDirectAccessRetriever;
import io.opensphere.mantle.data.cache.impl.DiskCacheDirectAccessRetriever;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.element.MapDataElement;
import io.opensphere.mantle.data.geom.MapGeometrySupport;

/**
 * A fake MapDataElement that delegates supported operations to the
 * DirectAccessRetriever for performance.
 */
class DirectAccessMapDataElement extends DirectAccessDataElement implements MapDataElement
{
    protected DirectAccessMapDataElement(DirectAccessMapDataElement source)
    {
        super(source.getCacheId(), source.getDirectAccessRetriever());
    }

    /**
     * Constructor.
     *
     * @param cacheId The cache id
     * @param directAccessRetriever The direct access retriever
     */
    public DirectAccessMapDataElement(long cacheId, DirectAccessRetriever directAccessRetriever)
    {
        super(cacheId, directAccessRetriever);
    }

    @Override
    public MapGeometrySupport getMapGeometrySupport()
    {
        return getDirectAccessRetriever().getMapGeometrySupport(getCacheId());
    }

    @Override
    public void setMapGeometrySupport(MapGeometrySupport mgs)
    {
        throw new UnsupportedOperationException("setMapGeometrySupport() is not supported for DirectAccessMapDataElement");
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.analysis.listtool.model.DirectAccessDataElement#cloneForDatatype(io.opensphere.mantle.data.DataTypeInfo,
     *      long)
     */
    @Override
    public DataElement cloneForDatatype(DataTypeInfo datatype, long newId)
    {
        DirectAccessRetriever cloneRetriever;

        if (getDirectAccessRetriever() instanceof DiskCacheDirectAccessRetriever)
        {
            DiskCacheDirectAccessRetriever original = (DiskCacheDirectAccessRetriever)getDirectAccessRetriever();

            cloneRetriever = new DiskCacheDirectAccessRetriever(original.getDiskCacheAssistant(), datatype,
                    original.getCacheRefMap(), original.getDynamicMetadataManager());
        }
        else if (getDirectAccessRetriever() instanceof DefaultDirectAccessRetriever)
        {
            DefaultDirectAccessRetriever original = (DefaultDirectAccessRetriever)getDirectAccessRetriever();
            cloneRetriever = new DefaultDirectAccessRetriever(datatype, original.getCacheRefMap(),
                    original.getDynamicMetadataManager());
        }
        else
        {
            throw new UnsupportedOperationException(
                    "Unable to clone. Unclonable direct access retriever: " + getDirectAccessRetriever().getClass().getName());
        }

        return new DirectAccessMapDataElement(this.getCacheId(), cloneRetriever);
    }
}
