package io.opensphere.merge.controller;

import java.util.concurrent.atomic.AtomicInteger;

import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.element.impl.AbstractDataElementProvider;
import io.opensphere.mantle.data.element.impl.DefaultDataElement;
import io.opensphere.mantle.data.element.impl.DefaultMapDataElement;
import io.opensphere.mantle.data.element.impl.SimpleMetaDataProvider;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.merge.model.MergedDataRow;

/**
 * The merge data element provider.
 */
public class MergeDataElementProvider extends AbstractDataElementProvider<MergedDataRow>
{
    /** The unique type counter. */
    private static final AtomicInteger ourUniqueTypeCounter = new AtomicInteger(1000);

    /**
     * Constructs a new data element provider.
     *
     * @param dataType The data type to provide data elements for.
     * @param sourceItems The data to convert to data elements.
     */
    public MergeDataElementProvider(DataTypeInfo dataType, Iterable<MergedDataRow> sourceItems)
    {
        super(dataType, sourceItems);
    }

    @Override
    protected DataElement createDataElement(MergedDataRow item)
    {
        return createElt(getDataTypeInfo(), item);
    }

    /**
     * Same as createDataElement, but more transportable.
     *
     * @param dti the host type
     * @param item the item of data
     * @return a DataElement
     */
    public static DataElement createElt(DataTypeInfo dti, MergedDataRow item)
    {
        SimpleMetaDataProvider provider = new SimpleMetaDataProvider(item.getData());
        MapGeometrySupport mgs = item.getGeometry();
        int id = ourUniqueTypeCounter.incrementAndGet();
        if (mgs != null)
        {
            return new DefaultMapDataElement(id, item.getTimespan(), dti, provider, mgs);
        }
        else
        {
            return new DefaultDataElement(id, item.getTimespan(), dti, provider);
        }
    }
}
