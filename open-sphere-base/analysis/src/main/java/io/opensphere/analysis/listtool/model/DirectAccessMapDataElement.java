package io.opensphere.analysis.listtool.model;

import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.cache.DirectAccessRetriever;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.element.MapDataElement;
import io.opensphere.mantle.data.geom.MapGeometrySupport;

/**
 * A fake MapDataElement that delegates supported operations to the
 * DirectAccessRetriever for performance.
 */
class DirectAccessMapDataElement extends DirectAccessDataElement implements MapDataElement
{
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
     * @see io.opensphere.analysis.listtool.model.DirectAccessDataElement#cloneForDatatype(io.opensphere.mantle.data.DataTypeInfo)
     */
    @Override
    public DataElement cloneForDatatype(DataTypeInfo datatype)
    {
        // TODO Auto-generated method stub
        return super.cloneForDatatype(datatype);
    }
}
