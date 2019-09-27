package io.opensphere.featureactions.editor.ui;

import java.util.List;
import java.util.Map;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.AwesomeIconSolid;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.fx.FXUtilities;
import io.opensphere.core.util.fx.FxIcons;
import io.opensphere.core.util.fx.JFXDialog;
import io.opensphere.featureactions.registry.FeatureActionsRegistry;
import io.opensphere.mantle.crust.DataTypeChecker;
import io.opensphere.mantle.util.MantleToolboxUtils;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;

/**
 * The main panel for the feature action manager.
 */
public class FeatureActionPanel extends GridPane
{
    /** The toolbox. */
    private final Toolbox myToolbox;

    /** The feature actions registry. */
    private final FeatureActionsRegistry myRegistry;

    /** The currently showing feature action editor. */
    private SimpleFeatureActionEditorUI myCurrentEditor;

    /** The map of display names to type names for data type infos. */
    private final Map<String, String> myDisplayNameToTypeMap = New.map();

    /**
     * Construct a new feature action panel.
     *
     * @param toolbox the toolbox
     * @param registry the feature action registry
     * @param dialog the dialog this panel is being placed in
     */
    public FeatureActionPanel(Toolbox toolbox, FeatureActionsRegistry registry, JFXDialog dialog)
    {
        myToolbox = toolbox;
        myRegistry = registry;
        add(createUpperRow(), 0, 0);
        dialog.setAcceptListener(() -> saveChanges());
    }

    /**
     * Create the dropdown menu for selecting layers.
     *
     * @return the ComboBox containing the layers
     */
    private Node createLayerSelector()
    {
        List<String> activeLayerNames = New.list();
        MantleToolboxUtils.getMantleToolbox(myToolbox).getDataGroupController().getActiveMembers(false).stream()
                .filter(e -> DataTypeChecker.isFeatureType(e)).forEach(e -> 
                {
                    myDisplayNameToTypeMap.put(e.getDisplayName(), e.getTypeKey());
                    activeLayerNames.add(e.getDisplayName());
                });

        ComboBox<String> layerSelector = new ComboBox<>(FXCollections.observableList(activeLayerNames));
        if (activeLayerNames.size() > 0)
        {
            layerSelector.setValue(activeLayerNames.get(0));
            handleLayerChange(myDisplayNameToTypeMap.get(activeLayerNames.get(0)));
        }
        layerSelector.setOnAction(e -> FXUtilities.runOnFXThread(() ->
                handleLayerChange(myDisplayNameToTypeMap.get(layerSelector.getValue()))));

        return layerSelector;
    }

    /**
     * Creates the button that will save the feature actions.
     *
     * @return the save button
     */
    private Node createSaveButton()
    {
        Button saveButton = new Button();
        saveButton.setGraphic(FxIcons.createClearIcon(AwesomeIconSolid.SAVE, Color.WHITE, 16));
        saveButton.setTooltip(new Tooltip("Save changes made to feature actions"));
        saveButton.setOnAction(e -> saveChanges());
        return saveButton;
    }

    /**
     * Create the row along the top side of the feature action manager.
     *
     * @return the row that contains the layer selector and save button
     */
    private Node createUpperRow()
    {
        HBox box = new HBox(5);
        box.getChildren().addAll(new Label("Current Layer:"), createLayerSelector(), /*separator,*/ createSaveButton());
        box.setAlignment(Pos.CENTER_LEFT);
        GridPane.setHgrow(box, Priority.ALWAYS);
        return box;
    }

    /**
     * Changes the displayed feature action editor to the one that corresponds
     * to the given data type key.
     *
     * @param dataType the data type key
     */
    private void handleLayerChange(String dataType)
    {
        myCurrentEditor = new SimpleFeatureActionEditorUI(myToolbox, myRegistry, null,
                MantleToolboxUtils.getMantleToolbox(myToolbox).getDataTypeInfoFromKey(dataType));
        GridPane.setHgrow(myCurrentEditor, Priority.ALWAYS);
        add(myCurrentEditor, 0, 1);
    }

    /**
     * Save the current state of the feature action editor.
     */
    private void saveChanges()
    {
        myCurrentEditor.accept();
    }
}
