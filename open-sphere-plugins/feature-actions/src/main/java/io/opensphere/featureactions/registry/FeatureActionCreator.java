package io.opensphere.featureactions.registry;

import io.opensphere.featureactions.model.FeatureAction;
import io.opensphere.mantle.data.element.DataElement;

/** Interface for creating feature actions from data elements. */
public interface FeatureActionCreator
{
    /**
     * Creates a feature action from the data element.
     *
     * @param element the data element
     * @return the feature action
     */
    FeatureAction create(DataElement element);
}
