package io.opensphere.featureactions.editor.ui;

import java.awt.Component;

import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;

import io.opensphere.core.Toolbox;
import io.opensphere.featureactions.editor.model.SimpleFeatureAction;
import io.opensphere.featureactions.editor.model.SimpleFeatureActionGroup;
import io.opensphere.featureactions.editor.model.SimpleFeatureActions;
import io.opensphere.mantle.data.DataTypeInfo;

/** Allows the user to edit feature actions for a specific feature group. */
public class SimpleFeatureActionPane extends BorderPane
{
    /** The current layer. */
    private final DataTypeInfo layer;

    /** The model containing all the feature actions for a layer. */
    private final SimpleFeatureActions myActions;

    /** The {@link ListView} showing all feature actions. */
    private ListView<SimpleFeatureAction> myActionsList;

    /**
     * Handles drag and drop to and from this list view.
     */
    private final DragDropHandler myDnDHandler;

    /** The model containing all the feature actions for the group. */
    private final SimpleFeatureActionGroup myGroup;

    /** The system toolbox. */
    private final Toolbox myToolbox;

    /** The parent dialog for the detail editor. */
    private final Component parentDialog;

    /**
     * Constructs a new feature action pane to edit a
     * {@link SimpleFeatureActionGroup}.
     *
     * @param toolbox The system toolbox.
     * @param allActions The model containing all the feature actions for a
     *            layer.
     * @param group The model containing all the feature actions for the group.
     * @param type the layer to which the group of feature actions apply
     * @param dialog the dialog containing this editor
     * @param dndHandler Allows the user to drag and drop actions.
     */
    public SimpleFeatureActionPane(Toolbox toolbox, SimpleFeatureActions allActions, SimpleFeatureActionGroup group,
            DataTypeInfo type, Component dialog, DragDropHandler dndHandler)
    {
        myToolbox = toolbox;
        myActions = allActions;
        myGroup = group;
        layer = type;
        parentDialog = dialog;
        myDnDHandler = dndHandler;
        createCenterPane();
        bindUI();
    }

    /**
     * Gets the list view showing the actions within the group.
     *
     * @return The actions list view.
     */
    protected ListView<SimpleFeatureAction> getListView()
    {
        return myActionsList;
    }

    /** Connects the model and the UI together. */
    private void bindUI()
    {
        myActionsList.setItems(myGroup.getActions());
    }

    /** Creates the center pane. */
    private void createCenterPane()
    {
        setCenter(createCriteriaList());
    }

    /**
     * Creates the criteria list view.
     *
     * @return the list view
     */
    private Node createCriteriaList()
    {
        myActionsList = new ListView<>();
        myActionsList.setCellFactory((param) ->
        {
            SimpleFeatureActionRow row = new SimpleFeatureActionRow(myToolbox, myActions, myGroup, layer, parentDialog);
            myDnDHandler.handleNewCell(myGroup, row);
            return row;
        });
        return myActionsList;
    }
}
