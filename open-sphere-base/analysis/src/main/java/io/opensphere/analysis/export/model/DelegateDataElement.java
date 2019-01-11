package io.opensphere.analysis.export.model;

import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.element.MetaDataProvider;
import io.opensphere.mantle.data.element.VisualizationState;
import io.opensphere.mantle.data.element.impl.SimpleMetaDataProvider;

/**
 * A {@link DataElement} that wraps an existing data element but provides its
 * own meta data provider.
 */
public class DelegateDataElement implements DataElement
{
    /**
     * The existing {@link DataElement}.
     */
    private final DataElement myOriginal;

    /**
     * The {@link MetaDataProvider} that provides the additional columns and
     * values.
     */
    private final MetaDataProvider myMetaDataProvider;

    /**
     * Constructs a new {@link DelegateDataElement}.
     *
     * @param original The existing {@link DataElement}.
     */
    public DelegateDataElement(DataElement original)
    {
        this(original, new SimpleMetaDataProvider(original.getMetaData()));
    }

    /**
     * Constructs a new {@link DelegateDataElement}.
     *
     * @param original The existing {@link DataElement}.
     * @param metaDataProvider The meta data provider.
     */
    public DelegateDataElement(DataElement original, MetaDataProvider metaDataProvider)
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
    public long getIdInCache()
    {
        return myOriginal.getIdInCache();
    }

    @Override
    public void setIdInCache(long cacheId)
    {
        myOriginal.setIdInCache(cacheId);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.element.DataElement#cloneForDatatype(io.opensphere.mantle.data.DataTypeInfo, long)
     */
    @Override
    public DataElement cloneForDatatype(DataTypeInfo datatype, long newId)
    {
        DataElement alternateOriginal = myOriginal.cloneForDatatype(datatype, newId);
        DelegateDataElement dataElement = new DelegateDataElement(alternateOriginal, myMetaDataProvider);

        return dataElement;
    }
}
