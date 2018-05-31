package io.opensphere.mantle.data;

import java.awt.Component;
import java.util.List;

import javax.swing.Icon;

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

    /**
     * Gets any layer icons to show in the Layers UI.
     *
     * @return the layer icons
     */
    List<Icon> getLayerIcons();
}
