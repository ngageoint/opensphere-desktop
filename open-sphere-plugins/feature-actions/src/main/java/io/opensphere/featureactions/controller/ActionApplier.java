package io.opensphere.featureactions.controller;

import java.util.Collection;
import java.util.List;

import io.opensphere.featureactions.model.Action;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.element.MapDataElement;

/** Interface for appliers of actions. */
public interface ActionApplier
{
    /**
     * Applies the actions to the data elements of the data type.
     *
     * @param actions the actions
     * @param elements the data elements
     * @param dataType the data type of the elements
     */
    void applyActions(Collection<? extends Action> actions, List<? extends MapDataElement> elements, DataTypeInfo dataType);

    /**
     * Un-applies any actions to the data elements of the data type.
     *
     * @param elementIds the data element IDs
     * @param dataType the data type of the elements
     */
    void clearActions(Collection<Long> elementIds, DataTypeInfo dataType);

    /**
     * Handles removing the data elements of the data type.
     *
     * @param elementIds the data element IDs
     * @param dataType the data type of the elements
     */
    void removeElements(Collection<Long> elementIds, DataTypeInfo dataType);
}
