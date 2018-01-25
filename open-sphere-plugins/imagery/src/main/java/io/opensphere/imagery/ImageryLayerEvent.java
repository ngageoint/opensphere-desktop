package io.opensphere.imagery;

import io.opensphere.core.event.AbstractSingleStateEvent;

/**
 * An event on a layer.
 */
public class ImageryLayerEvent extends AbstractSingleStateEvent
{
    /** Action which this event represents. */
    private final ImageryLayerEventAction myEventAction;

    /** The layer which the action is against. */
    private final ImageryDataTypeInfo myInfo;

    /**
     * Construct me.
     *
     * @param info the WMS DataTypeInfo
     * @param action action which represents this event.
     */
    public ImageryLayerEvent(ImageryDataTypeInfo info, ImageryLayerEventAction action)
    {
        myInfo = info;
        myEventAction = action;
    }

    @Override
    public String getDescription()
    {
        return "An event indicating that a layer should be activated or deactivated.";
    }

    /**
     * Get the eventAction.
     *
     * @return the eventAction
     */
    public ImageryLayerEventAction getEventAction()
    {
        return myEventAction;
    }

    /**
     * Get the DataTypeInfo.
     *
     * @return the DataTypeInfo
     */
    public ImageryDataTypeInfo getInfo()
    {
        return myInfo;
    }

    /** Type of action which has occurred. */
    public enum ImageryLayerEventAction
    {
        /** Activate event. */
        ACTIVATE,

        /** Deactivate event. */
        DEACTIVATE,

        /** Deactivate and Re-Activate a layer. */
        RESET,
    }
}
