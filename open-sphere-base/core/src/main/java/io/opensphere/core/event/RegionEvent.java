package io.opensphere.core.event;

import io.opensphere.core.model.GeographicBoundingBox;

/**
 * An event that describes something that took place in a region, either creating, deleting, or other region events.
 */
public class RegionEvent extends AbstractSingleStateEvent implements SourceableEvent
{
    /**
     * The Object that originated this event.
     */
    private final Object mySource;

    /**
     * The type of region event that occurred.
     */
    private final RegionEventType myType;

    /**
     * The region that triggered the event.
     */
    private final GeographicBoundingBox myRegion;

    /**
     * Creates a new event to notify registered listeners of a region selection event.
     *
     * @param pSource The Object that originated this event.
     * @param pType The type of region event that occurred.
     * @param pRegion the region that triggered the event.
     */
    public RegionEvent(Object pSource, RegionEventType pType, GeographicBoundingBox pRegion)
    {
        mySource = pSource;
        myType = pType;
        myRegion = pRegion;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.event.Event#getDescription()
     */
    @Override
    public String getDescription()
    {
        if (myType == RegionEventType.REGION_DESELECTED)
        {
            return "Event that notifies listeners that a region has been deselected.";
        }
        return "Event that notifies listeners that a region has been selected.";
    }

    /**
     * Gets the value of the {@link #myType} field.
     *
     * @return the value stored in the {@link #myType} field.
     */
    public RegionEventType getType()
    {
        return myType;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.event.SourceableEvent#getSource()
     */
    @Override
    public Object getSource()
    {
        return mySource;
    }

    /**
     * Gets the value of the {@link #myRegion} field.
     *
     * @return the value stored in the {@link #myRegion} field.
     */
    public GeographicBoundingBox getRegion()
    {
        return myRegion;
    }

    /**
     * An enumeration over the set of available region selection event types.
     */
    public enum RegionEventType
    {
        /**
         * The event type used when a region is de-selected.
         */
        REGION_DESELECTED,

        /**
         * The event type used when a region is selected.
         */
        REGION_SELECTED,

        /**
         * The event type used when a region is completed.
         */
        REGION_COMPLETED;
    }
}
