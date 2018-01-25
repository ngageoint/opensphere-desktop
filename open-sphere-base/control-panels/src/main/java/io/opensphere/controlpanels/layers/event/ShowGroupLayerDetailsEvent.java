package io.opensphere.controlpanels.layers.event;

import io.opensphere.core.event.AbstractSingleStateEvent;

/**
 * Event to request that the UI which allows viewing/editing group details be
 * shown.
 */
public class ShowGroupLayerDetailsEvent extends AbstractSingleStateEvent
{
    /** The tab. */
    private final String myDataGroupId;

    /** The Tab. */
    private final String myTab;

    /**
     * Constructor.
     *
     * @param dataGruopId the data group id
     * @param tab the tab to display (none requested if null or NONE)
     */
    public ShowGroupLayerDetailsEvent(String dataGruopId, String tab)
    {
        myTab = tab;
        myDataGroupId = dataGruopId;
    }

    /**
     * Gets the tab.
     *
     * @return the tab
     */
    public String getDataGroupId()
    {
        return myDataGroupId;
    }

    @Override
    public String getDescription()
    {
        return "Request Layer details coordinator show a layer details panel for the requested group.";
    }

    /**
     * Gets the tab.
     *
     * @return the tab
     */
    public String getTab()
    {
        return myTab;
    }
}
