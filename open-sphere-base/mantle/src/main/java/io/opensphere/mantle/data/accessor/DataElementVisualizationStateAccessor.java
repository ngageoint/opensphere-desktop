package io.opensphere.mantle.data.accessor;

import io.opensphere.core.cache.accessor.SerializableAccessor;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.element.MapDataElement;
import io.opensphere.mantle.data.element.VisualizationState;

/**
 * The Class VisualizationStateAccessor.
 */
public class DataElementVisualizationStateAccessor extends SerializableAccessor<DataElement, VisualizationState>
{
    /**
     * Instantiates a new visualization state accessor.
     */
    public DataElementVisualizationStateAccessor()
    {
        super(VisualizationState.PROPERTY_DESCRIPTOR);
    }

    @Override
    public VisualizationState access(DataElement input)
    {
        return input.getVisualizationState() == null ? new VisualizationState(input instanceof MapDataElement)
                : input.getVisualizationState();
    }
}
