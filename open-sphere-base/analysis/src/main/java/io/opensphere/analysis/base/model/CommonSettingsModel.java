package io.opensphere.analysis.base.model;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import net.jcip.annotations.NotThreadSafe;

import io.opensphere.core.util.javafx.ConcurrentObjectProperty;
import io.opensphere.mantle.data.DataTypeInfo;

/** Model common to various tools containing mostly data type information. */
@NotThreadSafe
public class CommonSettingsModel
{
    /** The currently active layer. */
    private final ObjectProperty<DataTypeInfo> myCurrentLayer = new ConcurrentObjectProperty<>(this, "currentLayer");

    /** The available layers. */
    private final ObservableList<DataTypeInfo> myAvailableLayers = FXCollections.observableArrayList();

    /**
     * Gets the current layer.
     *
     * @return the current layer
     */
    public final DataTypeInfo getCurrentLayer()
    {
        return myCurrentLayer.get();
    }

    /**
     * Sets the current layer.
     *
     * @param currentLayer the current layer
     */
    public final void setCurrentLayer(DataTypeInfo currentLayer)
    {
        assert Platform.isFxApplicationThread();
        myCurrentLayer.set(currentLayer);
    }

    /**
     * Gets the current layer property.
     *
     * @return the current layer property
     */
    public ObjectProperty<DataTypeInfo> currentLayerProperty()
    {
        return myCurrentLayer;
    }

    /**
     * Gets the available layers property.
     *
     * @return the available layers property
     */
    public ObservableList<DataTypeInfo> availableLayersProperty()
    {
        return myAvailableLayers;
    }
}
