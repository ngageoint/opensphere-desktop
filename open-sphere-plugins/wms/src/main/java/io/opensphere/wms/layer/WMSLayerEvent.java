package io.opensphere.wms.layer;

import io.opensphere.core.event.AbstractSingleStateEvent;

/**
 * An event on a layer.
 */
public class WMSLayerEvent extends AbstractSingleStateEvent
{
    /** Action which this event represents. */
    private final WMSLayerEventAction myEventAction;

    /** The layer which the action is against. */
    private final WMSDataTypeInfo myInfo;

    /**
     * Construct me.
     *
     * @param info the WMS DataTypeInfo
     * @param action action which represents this event.
     */
    public WMSLayerEvent(WMSDataTypeInfo info, WMSLayerEventAction action)
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
    public WMSLayerEventAction getEventAction()
    {
        return myEventAction;
    }

    /**
     * Get the DataTypeInfo.
     *
     * @return the DataTypeInfo
     */
    public WMSDataTypeInfo getInfo()
    {
        return myInfo;
    }

    /** Type of action which has occurred. */
    public enum WMSLayerEventAction
    {
        /** Activate event. */
        ACTIVATE,

        /** Deactivate event. */
        DEACTIVATE,

        /** Deactivate and Re-Activate a layer. */
        RESET,
    }
}
