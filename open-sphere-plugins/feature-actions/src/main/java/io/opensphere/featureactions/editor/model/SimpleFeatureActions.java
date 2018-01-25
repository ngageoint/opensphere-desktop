package io.opensphere.featureactions.editor.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Contains all feature actions for a single layer.
 */
public class SimpleFeatureActions
{
    /**
     * The list of feature action groups which contains feature actions.
     */
    private final ObservableList<SimpleFeatureActionGroup> myFeatureGroups = FXCollections.observableArrayList();

    /**
     * The id of the layer the actions are for.
     */
    private final String myLayerId;

    /**
     * Constructs a new collection of simple feature actions.
     *
     * @param layerId The id of the layer the actions are for.
     */
    public SimpleFeatureActions(String layerId)
    {
        myLayerId = layerId;
    }

    /**
     * Gets the list of feature actions groups, which contain feature actions.
     *
     * @return The list of feature action groups.
     */
    public ObservableList<SimpleFeatureActionGroup> getFeatureGroups()
    {
        return myFeatureGroups;
    }

    /**
     * Gets the id of the layer these actions belong to.
     *
     * @return The id of the layer these actions are for.
     */
    public String getLayerId()
    {
        return myLayerId;
    }
}
