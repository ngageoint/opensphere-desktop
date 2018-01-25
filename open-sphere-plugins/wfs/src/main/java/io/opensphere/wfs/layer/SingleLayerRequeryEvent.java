package io.opensphere.wfs.layer;

import io.opensphere.core.event.AbstractSingleStateEvent;

/**
 * Event that prompts WFS plugin to reload features for a single layer.
 */
public class SingleLayerRequeryEvent extends AbstractSingleStateEvent
{
    /** The re-query type. */
    private final RequeryType myRequeryType;

    /** The {@link WFSDataType} for the layer to re-query. */
    private final WFSDataType myDataType;

    /**
     * Instantiates an event to re-query the data for a single layer.
     *
     * @param dataType the {@link WFSDataType} for the layer to re-query
     * @param requeryType the re-query type
     */
    public SingleLayerRequeryEvent(WFSDataType dataType, RequeryType requeryType)
    {
        myDataType = dataType;
        myRequeryType = requeryType;
    }

    /**
     * Gets the {@link WFSDataType} for the layer to re-query.
     *
     * @return the {@link WFSDataType}
     */
    public WFSDataType getDataTypeInfo()
    {
        return myDataType;
    }

    @Override
    public String getDescription()
    {
        return "Event that prompts the WFS plugin to reload the features for "
                + "the specified data type using the currently-active query " + "regions.";
    }

    /**
     * Gets the re-query type. Determines whether the existing features should
     * be removed before re-querying the data type.
     *
     * @return the re-query type
     */
    public RequeryType getRequeryType()
    {
        return myRequeryType;
    }

    /** Re-query type enumeration. */
    public enum RequeryType
    {
        /** Remove all data and re-query. */
        FULL_REQUERY,

        /**
         * Just update the queries that have already been made. Does not remove
         * the existing features first.
         */
        SIMPLE_UPDATE,
    }
}
