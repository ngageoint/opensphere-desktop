package io.opensphere.mantle.data.columns.gui;

import java.util.List;

import javax.swing.JFrame;

import io.opensphere.core.datafilter.columns.MutableColumnMappingController;

/** Column mapping resources. */
public interface ColumnMappingResources
{
    /**
     * Gets the controller.
     *
     * @return the controller
     */
    MutableColumnMappingController getController();

    /**
     * Gets the parent frame.
     *
     * @return the parent frame
     */
    JFrame getParentFrame();

    /**
     * Gets the layers, including a flag indicating which ones are active.
     *
     * @return the layer keys
     */
    List<DataTypeRef> getLayers();

    /**
     * Gets the layer display name.
     *
     * @param layerKey the layer key
     * @return the layer display name
     */
    String getLayerDisplayName(String layerKey);

    /**
     * Gets the layer columns.
     *
     * @param layerKey the layer key
     * @return the layer columns
     */
    List<String> getLayerColumns(String layerKey);

    /**
     * Gets the data type of the layer/column.
     *
     * @param layerKey the layer key
     * @param layerColumn the layer column
     * @return the data type
     */
    String getType(String layerKey, String layerColumn);
}
