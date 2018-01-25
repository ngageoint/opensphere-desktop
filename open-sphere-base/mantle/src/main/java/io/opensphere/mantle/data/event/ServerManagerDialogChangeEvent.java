package io.opensphere.mantle.data.event;

import java.awt.Color;

import io.opensphere.core.event.AbstractSingleStateEvent;
import io.opensphere.core.event.SourceableEvent;

/**
 * This class provides a mechanism to listen to and request server status as
 * well as providing a way to show/hide the server manager dialog.
 */
public class ServerManagerDialogChangeEvent extends AbstractSingleStateEvent implements SourceableEvent
{
    /** The Source. */
    private final Object mySource;

    /** The Status color. Either green, yellow, or red. */
    private Color myStatus;

    /** The Event type. */
    private final EventType myEventType;

    /**
     * Instantiates a new server manager change event.
     *
     * @param source the source
     * @param type the type
     */
    public ServerManagerDialogChangeEvent(Object source, EventType type)
    {
        mySource = source;
        myEventType = type;
    }

    /**
     * Instantiates a new server manager dialog change event.
     *
     * @param source the source
     * @param statusColor the status color
     */
    public ServerManagerDialogChangeEvent(Object source, Color statusColor)
    {
        mySource = source;
        myStatus = statusColor;
        myEventType = EventType.SERVER_STATUS_UPDATE;
    }

    @Override
    public String getDescription()
    {
        return "Server manager dialog visibility changed";
    }

    /**
     * Gets the event type.
     *
     * @return the event type
     */
    public EventType getEventType()
    {
        return myEventType;
    }

    @Override
    public Object getSource()
    {
        return mySource;
    }

    /**
     * Gets the status.
     *
     * @return the status
     */
    public Color getStatus()
    {
        return myStatus;
    }

    /**
     * The Enum EventType.
     */
    public enum EventType
    {
        /** Request a server status update. */
        SERVER_STATUS_REQUEST,

        /** Server manager dialog visibility change flag. */
        VISIBILITY_CHANGE,

        /** Update listeners based on current server status. */
        SERVER_STATUS_UPDATE
    }
}
