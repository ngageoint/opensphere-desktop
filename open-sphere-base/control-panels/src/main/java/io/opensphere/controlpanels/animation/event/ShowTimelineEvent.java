package io.opensphere.controlpanels.animation.event;

import io.opensphere.controlpanels.animation.model.ViewPreference;
import io.opensphere.core.event.AbstractSingleStateEvent;

/** The show timeline event. */
public class ShowTimelineEvent extends AbstractSingleStateEvent
{
    /** The view requested to be shown. */
    private final ViewPreference myViewToShow;

    /**
     * Constructor.
     *
     * @param viewToShow The view requested to be shown
     */
    public ShowTimelineEvent(ViewPreference viewToShow)
    {
        super();
        myViewToShow = viewToShow;
    }

    @Override
    public String getDescription()
    {
        return "Request the timeline be shown";
    }

    /**
     * Gets the view requested to be shown.
     *
     * @return the view requested to be shown
     */
    public ViewPreference getViewToShow()
    {
        return myViewToShow;
    }
}
