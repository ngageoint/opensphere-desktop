package io.opensphere.controlpanels.layers.event;

import io.opensphere.core.event.AbstractSingleStateEvent;

/**
 * Event to request that the UI which allows viewing/editing layer sets be
 * shown.
 */
public class ShowLayerSetManagerEvent extends AbstractSingleStateEvent
{
    @Override
    public String getDescription()
    {
        return "Event to request that the UI which allows viewing/editing layer sets be shown.";
    }
}
