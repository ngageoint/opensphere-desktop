package io.opensphere.analysis.base.model;

import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;

import net.jcip.annotations.NotThreadSafe;

import io.opensphere.core.util.javafx.ConcurrentObjectProperty;

/** UI data model for binning tools. */
@NotThreadSafe
public class BinDataModel
{
    /** The bins. */
    private final ObservableList<UIBin> myBins = FXCollections.observableArrayList(b -> new Observable[] { b.countProperty() });

    /** The layer color. */
    private final ObjectProperty<Color> myLayerColor = new ConcurrentObjectProperty<>(this, "layerColor");

    /**
     * Gets the bins.
     *
     * @return the bins
     */
    public ObservableList<UIBin> getBins()
    {
        return myBins;
    }

    /**
     * Gets the layer color property.
     *
     * @return the layer color property
     */
    public ObjectProperty<Color> layerColorProperty()
    {
        return myLayerColor;
    }
}
