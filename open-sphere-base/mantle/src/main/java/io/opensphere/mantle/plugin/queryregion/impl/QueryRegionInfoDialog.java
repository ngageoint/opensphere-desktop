package io.opensphere.mantle.plugin.queryregion.impl;

import java.awt.Window;
import java.util.function.Consumer;

import javax.swing.JDialog;

import io.opensphere.core.datafilter.DataFilterGroup;
import io.opensphere.core.datafilter.DataFilterRegistry;
import io.opensphere.core.util.fx.FXUtilities;
import io.opensphere.mantle.controller.DataGroupController;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.plugin.queryregion.QueryRegion;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;

/** Dialog that shows information about a query region. */
public class QueryRegionInfoDialog extends JDialog
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The data filter registry. */
    private final DataFilterRegistry myDataFilterRegistry;

    /** The data group controller. */
    private final DataGroupController myDataGroupController;

    /** The region. */
    private final QueryRegion myRegion;

    /**
     * Constructor.
     *
     * @param parent The parent window.
     * @param region The region.
     * @param dataGroupController The data group controller.
     * @param dataFilterRegistry The data filter registry.
     */
    public QueryRegionInfoDialog(Window parent, QueryRegion region, DataGroupController dataGroupController,
            DataFilterRegistry dataFilterRegistry)
    {
        super(parent);
        myRegion = region;
        myDataGroupController = dataGroupController;
        myDataFilterRegistry = dataFilterRegistry;
    }

    /**
     * Show the dialog.
     */
    public void buildAndShow()
    {
        setLocationRelativeTo(getParent());
        setSize(400, 400);

        JFXPanel panel = new JFXPanel();
        setContentPane(panel);
        setVisible(true);

        Runnable showRunner = () ->
        {
            TreeItem<Object> root = new TreeItem<>("Layers for Query Region");
            root.setExpanded(true);

            myRegion.getTypeKeyToFilterMap().forEach((k, v1) ->
            {
                DataTypeInfo dti = myDataGroupController.findMemberById(k);
                if (dti != null)
                {
                    TreeItem<Object> layerNode = new TreeItem<>(dti);
                    Consumer<Object> filterAdder = o -> layerNode.getChildren().add(new TreeItem<>(o));
                    if (v1 != null)
                    {
                        if (v1.getFilterGroup().getGroups().isEmpty())
                        {
                            filterAdder.accept(v1.getFilterGroup());
                        }
                        else
                        {
                            v1.getFilterGroup().getGroups().forEach(filterAdder);
                        }
                    }
                    root.getChildren().add(layerNode);
                }
            });
            TreeView<Object> tree = new TreeView<>(root);
            tree.setCellFactory(v2 -> new TreeCellExtension());
            tree.setOnMouseClicked(e -> handleMouseClick(myDataFilterRegistry, tree, e));

            Scene scene = new Scene(tree);
            FXUtilities.addDesktopStyle(scene);

            panel.setScene(scene);
        };

        Platform.runLater(showRunner);
    }

    /**
     * Handle mouse click.
     *
     * @param dataFilterRegistry The data filter registry.
     * @param tree The tree.
     * @param event The mouse event.
     */
    protected void handleMouseClick(DataFilterRegistry dataFilterRegistry, TreeView<Object> tree, MouseEvent event)
    {
        if (event.getClickCount() == 2)
        {
            TreeItem<Object> item = tree.getSelectionModel().getSelectedItem();
            DataFilterGroup filter = item != null && item.getValue() instanceof DataFilterGroup ? (DataFilterGroup)item.getValue()
                    : null;
            while (item != null && !(item.getValue() instanceof DataTypeInfo))
            {
                item = item.getParent();
            }
            if (item != null)
            {
                DataTypeInfo type = (DataTypeInfo)item.getValue();
                dataFilterRegistry.showEditor(type.getTypeKey(), filter);
            }
            event.consume();
        }
    }

    /** Tree cell that knows how to show my stuff. */
    private static final class TreeCellExtension extends TreeCell<Object>
    {
        @Override
        protected void updateItem(Object item, boolean empty)
        {
            super.updateItem(item, empty);

            if (empty || item == null)
            {
                setText(null);
                setGraphic(null);
            }
            else if (item instanceof DataTypeInfo)
            {
                setText(((DataTypeInfo)item).getSourcePrefixAndDisplayNameCombo());
            }
            else if (item instanceof DataFilterGroup)
            {
                setText(((DataFilterGroup)item).getName());
            }
            else
            {
                setText(item.toString());
            }
        }
    }
}
