package io.opensphere.core.timeline;

import io.opensphere.core.order.OrderParticipantKey;

/**
 * Timeline change event.
 */
public class TimelineChangeEvent
{
    /** The the key that was changed. */
    private final OrderParticipantKey myKey;

    /** The change type. */
    private final TimelineChangeType myChangeType;

    /**
     * Constructor.
     *
     * @param key The the key that was changed
     * @param changeType The change type
     */
    public TimelineChangeEvent(OrderParticipantKey key, TimelineChangeType changeType)
    {
        myKey = key;
        myChangeType = changeType;
    }

    /**
     * Gets the key.
     *
     * @return The the key that was changed
     */
    public OrderParticipantKey getKey()
    {
        return myKey;
    }

    /**
     * Gets the change type.
     *
     * @return The change type
     */
    public TimelineChangeType getChangeType()
    {
        return myChangeType;
    }

    /** The change type. */
    public enum TimelineChangeType
    {
        /** A timeline layer was added. */
        LAYER_ADDED,

        /** A timeline layer was removed. */
        LAYER_REMOVED,

        /** The time spans were changed. */
        TIME_SPANS,

        /** The color was changed. */
        COLOR,

        /** The visibility was changed. */
        VISIBILITY,
    }
}
