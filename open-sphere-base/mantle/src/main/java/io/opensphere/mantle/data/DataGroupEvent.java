package io.opensphere.mantle.data;

import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;

/**
 * An event describing something that occurred in reference to a data group.
 */
public class DataGroupEvent extends Event
{
    /**
     * The unique identifier used for serialization operations.
     */
    private static final long serialVersionUID = -7585733709038698363L;

    /**
     * The event type used when a data group is triggered.
     */
    public static final EventType<DataGroupEvent> TRIGGER = new EventType<>(Event.ANY, "TRIGGER");

    /**
     * The event type used when a data group is activated.
     */
    public static final EventType<DataGroupEvent> ACTIVATE = new EventType<>(Event.ANY, "ACTIVATE");

    /**
     * The event type used when a data group is deactivated.
     */
    public static final EventType<DataGroupEvent> DEACTIVATE = new EventType<>(Event.ANY, "DEACTIVATE");

    /**
     * The data group affected by the event.
     */
    private final transient DataGroupInfo myDataGroup;

    /**
     * Creates a new data group event, with a type of {@link #TRIGGER}, and the supplied data group as the payload.
     *
     * @param pDataGroup the data group that caused the event to be fired.
     */
    public DataGroupEvent(DataGroupInfo pDataGroup)
    {
        super(TRIGGER);
        myDataGroup = pDataGroup;
    }

    /**
     * Creates a new data group event, with a type of {@link #TRIGGER}, the supplied data group as the payload, and the source and
     * target populated with the supplied values.
     *
     * @param pSource the event source which sent the event
     * @param pTarget the event target to associate with the event
     * @param pDataGroup the data group that caused the event to be fired.
     */
    public DataGroupEvent(Object pSource, EventTarget pTarget, DataGroupInfo pDataGroup)
    {
        super(pSource, pTarget, TRIGGER);
        myDataGroup = pDataGroup;
    }

    /**
     * Creates a new data group event, with the supplied event type, the supplied data group as the payload, and the source and
     * target populated with the supplied values.
     *
     * @param pSource the event source which sent the event
     * @param pTarget the event target to associate with the event
     * @param pEventType the type of event that occurred.
     * @param pDataGroup the data group that caused the event to be fired.
     */
    public DataGroupEvent(Object pSource, EventTarget pTarget, EventType<DataGroupEvent> pEventType, DataGroupInfo pDataGroup)
    {
        super(pSource, pTarget, pEventType);
        myDataGroup = pDataGroup;
    }

    /**
     * Gets the value of the {@link #myDataGroup} field.
     *
     * @return the value stored in the {@link #myDataGroup} field.
     */
    public DataGroupInfo getDataGroup()
    {
        return myDataGroup;
    }
}
