package io.opensphere.analysis.base.controller;

import io.opensphere.analysis.base.model.CommonSettingsModel;
import io.opensphere.core.Toolbox;
import io.opensphere.core.event.EventListenerService;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.controller.DataTypeController;
import io.opensphere.mantle.controller.event.AbstractDataTypeControllerEvent;
import io.opensphere.mantle.controller.event.impl.ActiveDataGroupsChangedEvent;
import io.opensphere.mantle.controller.event.impl.CurrentDataTypeChangedEvent;
import io.opensphere.mantle.controller.event.impl.DataTypeAddedEvent;
import io.opensphere.mantle.controller.event.impl.DataTypeRemovedEvent;
import io.opensphere.mantle.data.AbstractDataTypeInfoChangeEvent;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.event.DataTypeInfoLoadsToChangeEvent;
import io.opensphere.mantle.data.event.DataTypeVisibilityChangeEvent;
import javafx.application.Platform;

/**
 * Controller for SettingsModel. Essentially adapts mantle data type state into
 * an observable model.
 */
public class SettingsModelController extends EventListenerService
{
    /** The model. */
    private final CommonSettingsModel myModel;

    /** The data type controller. */
    private final DataTypeController myDataTypeController;

    /**
     * Constructor.
     *
     * @param toolbox The toolbox
     * @param model The model
     */
    public SettingsModelController(Toolbox toolbox, CommonSettingsModel model)
    {
        super(toolbox.getEventManager(), 4);
        myModel = model;
        myDataTypeController = toolbox.getPluginToolboxRegistry().getPluginToolbox(MantleToolbox.class).getDataTypeController();
        bindEvent(ActiveDataGroupsChangedEvent.class, this::handleActiveDataGroupsChangedEvent);
        bindEvent(AbstractDataTypeControllerEvent.class, this::handleDataTypeControllerEvent);
        bindEvent(AbstractDataTypeInfoChangeEvent.class, this::handleDataTypeInfoChangeEvent);
        bindModelFX(model.currentLayerProperty(), (obs, o, n) -> handleCurrentLayerPropertyChange(n));
    }

    /**
     * Handles a change in the current layer model state (UI => Mantle).
     *
     * @param layer the new layer
     */
    private void handleCurrentLayerPropertyChange(DataTypeInfo layer)
    {
        if (layer != null)
        {
            myDataTypeController.setCurrentDataType(layer, this);
        }
    }

    /**
     * Handles ActiveDataGroupsChangedEvent (Mantle => UI).
     *
     * @param event the event
     */
    private void handleActiveDataGroupsChangedEvent(ActiveDataGroupsChangedEvent event)
    {
        Platform.runLater(() ->
        {
            for (DataGroupInfo group : event.getActivatedGroups())
            {
                for (DataTypeInfo layer : group.getMembers(false))
                {
                    addAvailableLayerIfQualified(layer);
                }
            }
            for (DataGroupInfo group : event.getDeactivatedGroups())
            {
                for (DataTypeInfo layer : group.getMembers(false))
                {
                    removeAvailableLayer(layer);
                }
            }
        });
    }

    /**
     * Handles AbstractDataTypeControllerEvent (Mantle => UI).
     *
     * @param event the event
     */
    private void handleDataTypeControllerEvent(AbstractDataTypeControllerEvent event)
    {
        Platform.runLater(() ->
        {
            if (event instanceof DataTypeAddedEvent)
            {
                DataTypeInfo layer = ((DataTypeAddedEvent)event).getDataType();
                addAvailableLayerIfQualified(layer);
            }
            else if (event instanceof DataTypeRemovedEvent)
            {
                DataTypeInfo layer = ((DataTypeRemovedEvent)event).getDataType();
                removeAvailableLayer(layer);
            }
            else if (event instanceof CurrentDataTypeChangedEvent && event.getSource() != this)
            {
                DataTypeInfo layer = ((CurrentDataTypeChangedEvent)event).getNewType();
                if (isToolQualifiedType(layer))
                {
                    myModel.setCurrentLayer(layer);
                }
            }
        });
    }

    /**
     * Handles AbstractDataTypeInfoChangeEvent (Mantle => UI).
     *
     * @param event the event
     */
    private void handleDataTypeInfoChangeEvent(AbstractDataTypeInfoChangeEvent event)
    {
        Platform.runLater(() ->
        {
            if (event instanceof DataTypeVisibilityChangeEvent)
            {
                DataTypeVisibilityChangeEvent visibilityEvent = (DataTypeVisibilityChangeEvent)event;
                addOrRemoveAvailableLayer(visibilityEvent.isVisible(), visibilityEvent.getDataTypeInfo());
            }
            else if (event instanceof DataTypeInfoLoadsToChangeEvent)
            {
                DataTypeInfo layer = event.getDataTypeInfo();
                addOrRemoveAvailableLayer(layer.getBasicVisualizationInfo().getLoadsTo().isAnalysisEnabled(), layer);
            }
        });
    }

    /**
     * Adds or removes the layer to the available layers.
     *
     * @param add whether to add
     * @param layer the layer
     */
    private void addOrRemoveAvailableLayer(boolean add, DataTypeInfo layer)
    {
        if (add)
        {
            addAvailableLayerIfQualified(layer);
        }
        else
        {
            removeAvailableLayer(layer);
        }
    }

    /**
     * Adds an available layer if the layer is tool qualified.
     *
     * @param layer the layer
     */
    private void addAvailableLayerIfQualified(DataTypeInfo layer)
    {
        if (isToolQualifiedType(layer) && !myModel.availableLayersProperty().contains(layer))
        {
            int index = CollectionUtilities.indexOf(layer, myModel.availableLayersProperty(),
                    (o1, o2) -> o1.getSourcePrefixAndDisplayNameCombo().compareTo(o2.getSourcePrefixAndDisplayNameCombo()));
            myModel.availableLayersProperty().add(index, layer);
        }
    }

    /**
     * Removes an available layer.
     *
     * @param layer the layer
     */
    private void removeAvailableLayer(DataTypeInfo layer)
    {
        myModel.availableLayersProperty().remove(layer);
        if (myModel.availableLayersProperty().isEmpty())
        {
            myModel.setCurrentLayer(null);
        }
    }

    /**
     * Checks if the layer is tool qualified.
     *
     * @param layer the {@link DataTypeInfo}
     * @return true, if the layer is tool qualified
     */
    private boolean isToolQualifiedType(DataTypeInfo layer)
    {
        return layer != null && layer.isVisible() && layer.getMetaDataInfo() != null
                && layer.getBasicVisualizationInfo().getLoadsTo().isAnalysisEnabled() && isActive(layer)
                && myDataTypeController.hasDataTypeInfoForTypeKey(layer.getTypeKey());
    }

    /**
     * Determines if the layer is in an active group.
     *
     * @param layer the {@link DataTypeInfo}
     * @return whether it's in an active group
     */
    private static boolean isActive(DataTypeInfo layer)
    {
        DataGroupInfo parent = layer.getParent();
        return parent != null && parent.activationProperty().isActive();
    }
}
