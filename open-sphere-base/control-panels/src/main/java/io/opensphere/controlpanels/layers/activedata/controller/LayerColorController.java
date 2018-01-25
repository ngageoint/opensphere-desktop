package io.opensphere.controlpanels.layers.activedata.controller;

import java.awt.Color;
import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;

import javax.swing.SwingUtilities;

import io.opensphere.controlpanels.layers.base.LayerSelectionUtilities;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.event.EventManagerListenerHandle;
import io.opensphere.core.util.ColorUtilities;
import io.opensphere.core.util.CompositeService;
import io.opensphere.core.util.ObservableValueListenerHandle;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.core.util.swing.input.model.ColorModel;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.event.DataTypeInfoColorChangeEvent;

/**
 * Controller that binds mantle layer color to a ColorModel.
 */
public final class LayerColorController extends CompositeService
{
    /** Predicate for feature data types. */
    private static final Predicate<DataTypeInfo> IS_FEATURE = value -> value.getMapVisualizationInfo() != null
            && value.getMapVisualizationInfo().usesMapDataElements();

    /** The color model. */
    private final ColorModel myModel;

    /** The selected layers. */
    private final Collection<Object> myLayers = New.list();

    /** Handle to the mantle updater. */
    private final ObservableValueListenerHandle<Color> myMantleUpdater;

    /** The last color change update number. */
    private long myLastColorChangeUpdateNumber;

    /**
     * Constructor.
     *
     * @param model The color model
     * @param eventManager The event manager
     */
    public LayerColorController(ColorModel model, EventManager eventManager)
    {
        myModel = model;
        addService(new EventManagerListenerHandle<>(eventManager, DataTypeInfoColorChangeEvent.class, event -> updateModelFromMantle(event)));
        myMantleUpdater = new ObservableValueListenerHandle<>(myModel, (obs, old, newValue) -> updateMantleFromModel(old, newValue));
        addService(myMantleUpdater);
    }

    /**
     * Sets the selected data groups and types.
     *
     * @param selectedDataGroups the selected data groups
     * @param selectedDataTypes the selected data types
     */
    public void setSelectedItems(Collection<DataGroupInfo> selectedDataGroups, Collection<DataTypeInfo> selectedDataTypes)
    {
        Collection<Object> layers = CollectionUtilities.concat(selectedDataGroups, selectedDataTypes);

        // This first round of filtering is to clean up what's passed in. We get
        // types with their parent group, so remove the parent group in this
        // case. Not perfect.
        for (DataGroupInfo group : selectedDataGroups)
        {
            for (DataTypeInfo type : selectedDataTypes)
            {
                if (group.hasMember(type, true))
                {
                    layers.remove(group);
                }
            }
        }

        // This filtering assumes we now have exactly the items the user
        // selected. We're now going to filter it down to the items we're
        // actually going to act on.
        layers = LayerSelectionUtilities.filter(layers);

        myLayers.clear();
        myLayers.addAll(layers);

        updateModelFromSelection();
    }

    /**
     * Gets the one true selected data type.
     *
     * @return the selected data type
     */
    public DataTypeInfo getSelectedDataType()
    {
        DataTypeInfo dataType = null;

        Object firstLayer = CollectionUtilities.getItemOrNull(myLayers, 0);
        if (firstLayer instanceof DataTypeInfo)
        {
            dataType = (DataTypeInfo)firstLayer;
        }
        else if (firstLayer instanceof DataGroupInfo)
        {
            Set<DataTypeInfo> featureDataTypes = ((DataGroupInfo)firstLayer).findMembers(IS_FEATURE, false, true);
            dataType = CollectionUtilities.getItemOrNull(featureDataTypes, 0);
        }

        return dataType;
    }

    /**
     * Updates the model from the current selection.
     */
    private void updateModelFromSelection()
    {
        assert SwingUtilities.isEventDispatchThread();

        myMantleUpdater.pause();

        DataTypeInfo dataType = getSelectedDataType();
        if (dataType != null)
        {
            if (dataType.getMapVisualizationInfo() != null
                    && dataType.getMapVisualizationInfo().getTileRenderProperties() != null)
            {
                myModel.set(dataType.getMapVisualizationInfo().getTileRenderProperties().getColor());
            }
            else
            {
                myModel.set(dataType.getBasicVisualizationInfo().getTypeColor());
            }
        }

        myMantleUpdater.resume();
    }

    /**
     * Updates the model from a mantle event.
     *
     * @param event the event
     */
    private void updateModelFromMantle(final DataTypeInfoColorChangeEvent event)
    {
        if (event.getUpdateNumber() > myLastColorChangeUpdateNumber)
        {
            myLastColorChangeUpdateNumber = event.getUpdateNumber();

            boolean update = false;
            if (myLayers.size() == 1)
            {
                DataTypeInfo dataType = getSelectedDataType();
                if (dataType != null)
                {
                    update = dataType.equals(event.getDataTypeInfo());
                }
            }

            if (update)
            {
                EventQueueUtilities.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        myMantleUpdater.pause();
                        myModel.set(event.getColor());
                        myMantleUpdater.resume();
                    }
                });
            }
        }
    }

    /**
     * Updates mantle from the model.
     *
     * @param oldValue the old value
     * @param newValue the new value
     */
    private void updateMantleFromModel(Color oldValue, Color newValue)
    {
        Collection<DataTypeInfo> dataTypes = New.list();
        for (Object layer : myLayers)
        {
            if (layer instanceof DataTypeInfo)
            {
                dataTypes.add((DataTypeInfo)layer);
            }
            else if (layer instanceof DataGroupInfo)
            {
                dataTypes.addAll(((DataGroupInfo)layer).getMembers(true));
            }
        }

        boolean isOpacityChange = ColorUtilities.isEqualIgnoreAlpha(oldValue, newValue);

        for (DataTypeInfo dataType : dataTypes)
        {
            if (isOpacityChange || !IS_FEATURE.test(dataType))
            {
                dataType.getBasicVisualizationInfo().setTypeOpacity(myModel.get().getAlpha(), this);
            }
            else
            {
                dataType.getBasicVisualizationInfo().setTypeColor(myModel.get(), this);
            }
        }
    }
}
