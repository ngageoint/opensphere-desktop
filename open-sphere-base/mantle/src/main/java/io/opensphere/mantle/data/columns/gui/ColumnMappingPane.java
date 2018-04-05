package io.opensphere.mantle.data.columns.gui;

import java.awt.EventQueue;
import java.util.function.Consumer;

import io.opensphere.core.Toolbox;
import io.opensphere.core.datafilter.columns.ColumnMapping;
import io.opensphere.core.util.collections.StreamUtilities;
import io.opensphere.core.util.fx.FXUtilities;
import io.opensphere.core.util.image.IconUtil.IconType;
import io.opensphere.core.util.swing.EventQueueUtilities;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

/** JavaFX column mapping pane. */
public class ColumnMappingPane extends BorderPane
{
    /** The column mapping resources. */
    private final ColumnMappingResources myResources;

    /** The tree. */
    private TreeView<ColumnMapping> myTree;

    /**
     * A listener used to detect changes in the tree. The reference must be
     * maintained because of weak change support.
     */
    private final Consumer<Void> myColumnMappingListener = t -> myTree.setRoot(createTreeModel());

    /** The toolbox through which application state is accessed. */
    private final Toolbox myToolbox;

    /**
     * Constructor.
     *
     * @param toolbox The toolbox through which application state is accessed.
     * @param resources the column mapping resources
     */
    public ColumnMappingPane(Toolbox toolbox, ColumnMappingResources resources)
    {
        myToolbox = toolbox;
        myResources = resources;

        setTop(createTopPane());
        setCenter(createCenterPane());

        // Listen for mapping updates
        resources.getController().addListener(myColumnMappingListener);
    }

    /**
     * Shows the create/edit mapping dialog.
     *
     * @param definedColumn the defined column
     */
    void showEditor(String definedColumn)
    {
        EventQueue.invokeLater(() ->
        {
            ColumnMappingEditorDialog dialog = new ColumnMappingEditorDialog(definedColumn, myResources);
            dialog.setVisible(true);
        });
    }

    /**
     * Deletes the mapping.
     *
     * @param definedColumn the defined column
     */
    void delete(String definedColumn)
    {
        EventQueueUtilities.invokeLater(() ->
        {
            String message = "Delete all " + Constants.COLUMN_MAPPING.plural() + " for " + definedColumn + "?";
            ButtonType b = FXUtilities.showAlert(myToolbox.getUIRegistry().getMainFrameProvider().get(), message,
                    "Confirm Delete", ButtonType.YES, ButtonType.NO);
            if (b == ButtonType.YES)
            {
                FXUtilities.runOnFXThreadAndWait(() -> myResources.getController().remove(definedColumn));
            }
        });
    }

    /**
     * Creates the top pane.
     *
     * @return the pane
     */
    private Node createTopPane()
    {
        Button createButton = FXUtilities.newIconButton("Create", IconType.PLUS, Color.GREEN);
        createButton.setTooltip(new Tooltip("Create new " + Constants.COLUMN_MAPPING.plural()));
        createButton.setOnAction(e -> showEditor(null));

        HBox box = FXUtilities.newHBox(createButton);
        box.setPadding(new Insets(Constants.GAP));
        return box;
    }

    /**
     * Creates the center pane.
     *
     * @return the pane
     */
    private Node createCenterPane()
    {
        myTree = new TreeView<>(createTreeModel());
        myTree.setShowRoot(false);
        myTree.setCellFactory(param -> new ColumnMappingCell());
        return myTree;
    }

    /**
     * Creates a tree model from the controller.
     *
     * @return the tree model
     */
    private TreeItem<ColumnMapping> createTreeModel()
    {
        TreeItem<ColumnMapping> root = new TreeItem<>();
        for (String definedColumn : myResources.getController().getDefinedColumns())
        {
            TreeItem<ColumnMapping> columnItem = new TreeItem<>(new ColumnMapping(definedColumn, null, null));
            columnItem.getChildren().addAll(
                    StreamUtilities.map(myResources.getController().getMappings(definedColumn), TreeItem<ColumnMapping>::new));
            root.getChildren().add(columnItem);
        }
        return root;
    }

    /** A column mapping tree cell. */
    private class ColumnMappingCell extends TreeCell<ColumnMapping>
    {
        /** The edit button. */
        private final Button myEditButton = FXUtilities.newIconButton(null, IconType.EDIT, null, 16);

        /** The delete button. */
        private final Button myDeleteButton = FXUtilities.newIconButton(null, IconType.CLOSE, null, 16);

        /** Constructor. */
        public ColumnMappingCell()
        {
            setContentDisplay(ContentDisplay.RIGHT);

            myEditButton.setPrefSize(20, 20);
            myEditButton.setTooltip(new Tooltip("Edit the " + Constants.COLUMN_MAPPING.plural()));
            myEditButton.setOnAction(e -> showEditor(getItem().getDefinedColumn()));

            myDeleteButton.setPrefSize(20, 20);
            myDeleteButton.setTooltip(new Tooltip("Delete the " + Constants.COLUMN_MAPPING.plural()));
            myDeleteButton.setOnAction(e -> delete(getItem().getDefinedColumn()));

            HBox box = FXUtilities.newHBox(FXUtilities.newHSpacer(), myEditButton, myDeleteButton);
            setGraphic(box);
        }

        @Override
        public void updateItem(ColumnMapping columnMapping, boolean empty)
        {
            super.updateItem(columnMapping, empty);
            setText(getText(columnMapping));
            boolean showButton = !empty && columnMapping.getLayerKey() == null;
            myEditButton.setVisible(showButton);
            myDeleteButton.setVisible(showButton);
        }

        /**
         * Gets the text for the column mapping.
         *
         * @param columnMapping the column mapping
         * @return the text
         */
        private String getText(ColumnMapping columnMapping)
        {
            if (columnMapping == null)
            {
                return null;
            }
            if (columnMapping.getLayerColumn() != null)
            {
                return columnMapping.getLayerColumn() + " (" + myResources.getLayerDisplayName(columnMapping.getLayerKey()) + ")";
            }
            return columnMapping.getDefinedColumn();
        }
    }
}
