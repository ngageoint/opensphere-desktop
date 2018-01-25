package io.opensphere.controlpanels.layers.event;

import io.opensphere.core.event.AbstractSingleStateEvent;
import io.opensphere.mantle.data.DataGroupInfo;

/**
 * Event to request that the UI which is currently showing the group make the
 * group visible and select it.
 */
public class AvailableGroupSelectionEvent extends AbstractSingleStateEvent
{
    /** The tab. */
    private final DataGroupInfo myDGI;

    /**
     * Constructor.
     *
     * @param dgi the a {@link DataGroupInfo}
     */
    public AvailableGroupSelectionEvent(DataGroupInfo dgi)
    {
        myDGI = dgi;
    }

    /**
     * Gets the tab.
     *
     * @return the tab
     */
    public DataGroupInfo getDataGroupInfo()
    {
        return myDGI;
    }

    @Override
    public String getDescription()
    {
        return "Request selection of data group.";
    }
}
