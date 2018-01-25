package io.opensphere.mantle.crust;

import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.time.ExtentAccumulator;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.element.MapDataElement;
import io.opensphere.mantle.data.impl.DefaultDataTypeInfo;
import io.opensphere.mantle.data.impl.DefaultTimeExtents;

/**
 * Helper that updates a data type with spatial and temporal bounds.
 */
public class SpaceTimeHelper
{
    /** The data type as a DefaultDataTypeInfo, if available. */
    private final DefaultDataTypeInfo myDefaultDataType;

    /** The elements' time extents accumulator. */
    private final ExtentAccumulator myExtentAccumulator = new ExtentAccumulator();

    /**
     * Constructor.
     *
     * @param dataType The data type
     */
    public SpaceTimeHelper(DataTypeInfo dataType)
    {
        myDefaultDataType = dataType instanceof DefaultDataTypeInfo ? (DefaultDataTypeInfo)dataType : null;
        if (myDefaultDataType != null && myDefaultDataType.getTimeExtents() != null)
        {
            addTime(myDefaultDataType.getTimeExtents().getExtent());
        }
    }

    /**
     * Handles a hasNext() result.
     *
     * @param hasNext whether there is a next element
     */
    public void handleHasNext(boolean hasNext)
    {
        if (!hasNext && myDefaultDataType != null)
        {
            myDefaultDataType.setTimeExtents(new DefaultTimeExtents(myExtentAccumulator.getExtent()), this);
        }
    }

    /**
     * Handles a next() result.
     *
     * @param element the next element
     */
    public void handleNext(DataElement element)
    {
        if (myDefaultDataType != null)
        {
            addTime(element.getTimeSpan());
            if (element instanceof MapDataElement)
            {
                GeographicBoundingBox boundingBox = ((MapDataElement)element).getMapGeometrySupport().getBoundingBox(null);
                myDefaultDataType.addBoundingBox(boundingBox);
            }
        }
    }

    /**
     * Adds a time span to the accumulator if possible.
     *
     * @param span the time span
     */
    private void addTime(TimeSpan span)
    {
        if (span.isBounded() && !span.isZero())
        {
            myExtentAccumulator.add(span);
        }
    }
}
