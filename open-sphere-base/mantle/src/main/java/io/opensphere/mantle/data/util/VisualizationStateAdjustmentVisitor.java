package io.opensphere.mantle.data.util;

import java.util.List;

import io.opensphere.mantle.data.element.VisualizationState;
import io.opensphere.mantle.data.element.event.consolidated.AbstractConsolidatedDataElementChangeEvent;

/**
 * The Interface used to make adjustments and produce events for state changes
 * to a VisualizationState.
 */
public interface VisualizationStateAdjustmentVisitor
{
    /**
     * Adjust state of the VisualizationState based on the type of adjustment.
     *
     * @param stateToAdjust the state to adjust
     * @param dataElementId the data element id to use in the event
     * @param dtKey the DataTypeInfo key
     * @param source the source the source of the event.
     */
    void adjustState(VisualizationState stateToAdjust, long dataElementId, String dtKey, Object source);

    /**
     * Gets the events.
     *
     * @return the events
     */
    List<AbstractConsolidatedDataElementChangeEvent> getEvents();

    /**
     * Checks for events.
     *
     * @return true, if successful
     */
    boolean wereAdjustmentsMade();
}
