package io.opensphere.mantle.data.event;

import java.awt.Color;
import java.util.concurrent.atomic.AtomicLong;

import io.opensphere.mantle.data.AbstractDataTypeInfoChangeEvent;
import io.opensphere.mantle.data.DataTypeInfo;

/**
 * Events from changes to DataTypeInfo.
 */
public class DataTypeInfoColorChangeEvent extends AbstractDataTypeInfoChangeEvent
{
    /** The our update counter. */
    private static AtomicLong ourUpdateCounter = new AtomicLong();

    /** The is opacity change only. */
    private final boolean myIsOpacityChangeOnly;

    /** The update number. */
    private final long myUpdateNumber = ourUpdateCounter.incrementAndGet();

    /**
     * Event CTOR.
     *
     * @param dti - the {@link DataTypeInfo}
     * @param value - the new color
     * @param opacityChangeOnly true if opacity change only
     * @param source - the source of the event.
     */
    public DataTypeInfoColorChangeEvent(DataTypeInfo dti, Color value, boolean opacityChangeOnly, Object source)
    {
        super(dti, Type.TYPE_COLOR_CHANGED, value, source);
        myIsOpacityChangeOnly = opacityChangeOnly;
    }

    /**
     * Gets the color for the change.
     *
     * @return the new color.
     */
    public Color getColor()
    {
        return (Color)getValue();
    }

    /**
     * Gets the update number.
     *
     * A unique number that is one-up counted for each consolidated event. A
     * listener can use this number to make sure they do not process events that
     * precede the last event that was processed or generated.
     *
     * @return the update number
     */
    public long getUpdateNumber()
    {
        return myUpdateNumber;
    }

    /**
     * Checks if is opacity change only.
     *
     * @return true, if is opacity change only
     */
    public boolean isOpacityChangeOnly()
    {
        return myIsOpacityChangeOnly;
    }
}
