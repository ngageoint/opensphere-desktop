package io.opensphere.controlpanels.layers.event;

import io.opensphere.core.event.AbstractSingleStateEvent;

/**
 * Event to request that the UI which allows viewing/editing available data be
 * shown.
 */
public class ShowAvailableDataEvent extends AbstractSingleStateEvent
{
    @Override
    public String getDescription()
    {
        return "Request that the UI which allows viewing/editing available data be shown.";
    }
}
