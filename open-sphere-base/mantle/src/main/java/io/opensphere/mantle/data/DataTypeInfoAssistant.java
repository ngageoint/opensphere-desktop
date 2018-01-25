package io.opensphere.mantle.data;

import java.awt.Component;

/**
 * A way for a data type to provide additional functionality, such as additional
 * UIs.
 */
public interface DataTypeInfoAssistant
{
    /**
     * Gets the layer control user interface component.
     *
     * @param dataType the {@link DataTypeInfo}
     * @return the layer control component
     */
    Component getLayerControlUIComponent(DataTypeInfo dataType);
}
