package io.opensphere.server.services;

import java.util.Date;

import io.opensphere.core.event.AbstractSingleStateEvent;

/**
 * The Class ServerRefreshEvent. Prompts plugins to initiate a server refresh
 * that checks for new or updated layers.
 */
public class ServerRefreshEvent extends AbstractSingleStateEvent
{
    /** The source object that generated this Refresh Event. */
    private final Object mySource;

    /** The time that the refresh was requested. */
    private final Date myRefreshTime;

    /**
     * Instantiates a new server refresh event.
     *
     * @param source the source object that generated this Refresh Event.
     * @param refreshTime the refresh time
     */
    public ServerRefreshEvent(Object source, Date refreshTime)
    {
        mySource = source;
        myRefreshTime = (Date)refreshTime.clone();
    }

    @Override
    public String getDescription()
    {
        return "Event that prompts a server refresh.";
    }

    /**
     * Gets the time that the server refresh was requested.
     *
     * @return the refresh time
     */
    public Date getRefreshTime()
    {
        return (Date)myRefreshTime.clone();
    }

    /**
     * Gets the source object that generated the Refresh Event.
     *
     * @return the source object
     */
    public Object getSource()
    {
        return mySource;
    }
}
