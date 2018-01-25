package io.opensphere.mantle.mp;

import io.opensphere.core.event.AbstractSingleStateEvent;
import io.opensphere.core.event.SourceableEvent;
import io.opensphere.mantle.data.DataTypeInfo;

/**
 * Events from changes to DataTypeInfo.
 */
public class MapAnnotationPointChangeEvent extends AbstractSingleStateEvent implements SourceableEvent
{
    /** The source. */
    private final Object myChangeSource;

    /** A value if appropriate. */
    private final Object myChangeValue;

    /** The DataTypeInfo. */
    private final MutableMapAnnotationPoint myMapAnnotationPoint;

    /**
     * Event CTOR.
     *
     * @param pt - the {@link MutableMapAnnotationPoint}
     * @param source - the source of the event.
     */
    public MapAnnotationPointChangeEvent(MutableMapAnnotationPoint pt, Object source)
    {
        this(pt, null, source);
    }

    /**
     * Event CTOR.
     *
     * @param dti - the {@link DataTypeInfo}
     * @param value - the value that changed if appropriate.
     * @param source - the source of the event.
     */
    public MapAnnotationPointChangeEvent(MutableMapAnnotationPoint dti, Object value, Object source)
    {
        myMapAnnotationPoint = dti;
        myChangeSource = source;
        myChangeValue = value;
    }

    @Override
    public String getDescription()
    {
        return "Changes to a MapAnnotationPoint";
    }

    /**
     * Gets the {@link MutableMapAnnotationPoint} dispatching the event.
     *
     * @return the {@link MutableMapAnnotationPoint}
     */
    public MutableMapAnnotationPoint getMapAnnotationPoint()
    {
        return myMapAnnotationPoint;
    }

    @Override
    public Object getSource()
    {
        return myChangeSource;
    }

    /**
     * Gets the value for the change if provided for this type.
     *
     * @return the value or null if none.
     */
    public Object getValue()
    {
        return myChangeValue;
    }
}
