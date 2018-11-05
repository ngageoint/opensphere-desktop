package io.opensphere.analysis.export.model;

import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.element.MapDataElement;
import io.opensphere.mantle.data.element.MetaDataProvider;
import io.opensphere.mantle.data.element.VisualizationState;
import io.opensphere.mantle.data.element.impl.SimpleMetaDataProvider;
import io.opensphere.mantle.data.geom.MapGeometrySupport;

/**
 * A {@link DataElement} that wraps an existing data element but provides its
 * own meta data provider.
 */
public class DelegateMapDataElement implements MapDataElement
{
    /**
     * The {@link MetaDataProvider} that provides the additional columns and
     * values.
     */
    private final MetaDataProvider myMetaDataProvider;

    /**
     * The existing {@link DataElement}.
     */
    private final MapDataElement myOriginal;

    /**
     * Constructs a new {@link DelegateMapDataElement}.
     *
     * @param original The existing {@link DataElement}.
     */
    public DelegateMapDataElement(MapDataElement original)
    {
        this(original, new SimpleMetaDataProvider(original.getMetaData()));
    }

    /**
     * Constructs a new {@link DelegateMapDataElement}.
     *
     * @param original The existing {@link DataElement}.
     * @param metaDataProvider The meta data provider.
     */
    public DelegateMapDataElement(MapDataElement original, MetaDataProvider metaDataProvider)
    {
        myOriginal = original;
        myMetaDataProvider = metaDataProvider;
    }

    @Override
    public DataTypeInfo getDataTypeInfo()
    {
        return myOriginal.getDataTypeInfo();
    }

    @Override
    public long getId()
    {
        return myOriginal.getId();
    }

    @Override
    public long getIdInCache()
    {
        return myOriginal.getIdInCache();
    }

    @Override
    public MapGeometrySupport getMapGeometrySupport()
    {
        return myOriginal.getMapGeometrySupport();
    }

    @Override
    public MetaDataProvider getMetaData()
    {
        return myMetaDataProvider;
    }

    @Override
    public TimeSpan getTimeSpan()
    {
        return myOriginal.getTimeSpan();
    }

    @Override
    public VisualizationState getVisualizationState()
    {
        return myOriginal.getVisualizationState();
    }

    @Override
    public boolean isDisplayable()
    {
        return myOriginal.isDisplayable();
    }

    @Override
    public boolean isMappable()
    {
        return myOriginal.isMappable();
    }

    @Override
    public void setDisplayable(boolean displayable, Object source)
    {
        myOriginal.setDisplayable(displayable, source);
    }

    @Override
    public void setIdInCache(long cacheId)
    {
        myOriginal.setIdInCache(cacheId);
    }

    @Override
    public void setMapGeometrySupport(MapGeometrySupport mgs)
    {
        myOriginal.setMapGeometrySupport(mgs);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.element.DataElement#cloneForDatatype(io.opensphere.mantle.data.DataTypeInfo)
     */
    @Override
    public DataElement cloneForDatatype(DataTypeInfo datatype)
    {
        MapDataElement originalDelegate = (MapDataElement)myOriginal.cloneForDatatype(datatype);

        DelegateMapDataElement clone = new DelegateMapDataElement(originalDelegate, myMetaDataProvider);
        return clone;
    }
}
