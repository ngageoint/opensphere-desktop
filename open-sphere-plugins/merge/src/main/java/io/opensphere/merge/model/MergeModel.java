package io.opensphere.merge.model;

import java.util.Collection;
import java.util.List;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.DataTypeInfo;

/**
 * The model for the merge UI.
 */
public class MergeModel
{
    /**
     * The layers we are merging.
     */
    private final List<DataTypeInfo> myLayers;

    /**
     * The new layer name.
     */
    private final StringProperty myNewLayerName = new SimpleStringProperty();

    /**
     * A message to show to the user.
     */
    private final StringProperty myUserMessage = new SimpleStringProperty();

    /**
     * Constructs a new model.
     *
     * @param layers The layers that will be merged.
     */
    public MergeModel(Collection<DataTypeInfo> layers)
    {
        myLayers = New.unmodifiableList(layers);
    }

    /**
     * Gets the layers to merge.
     *
     * @return the layers to merge.
     */
    public List<DataTypeInfo> getLayers()
    {
        return myLayers;
    }

    /**
     * Gets the name of the new merged layer.
     *
     * @return the new layer name.
     */
    public StringProperty getNewLayerName()
    {
        return myNewLayerName;
    }

    /**
     * Gets a message to show to the user.
     *
     * @return A message to show to the user.
     */
    public StringProperty getUserMessage()
    {
        return myUserMessage;
    }
}
