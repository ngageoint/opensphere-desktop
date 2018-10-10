package io.opensphere.mantle.iconproject.view;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

import io.opensphere.mantle.iconproject.impl.TreePopupMenuImpl;
import io.opensphere.mantle.iconproject.model.PanelModel;

/**
 * A popup menu to be called from a tree object to perform an action such as a
 * delete feature. All actions are handled inside this class.
 */
public class TreePopupMenu extends ContextMenu
{
    /**
     * The main method to instantiate a new IconPopupMenu.
     *
     * @param panelModel the model used to get registry items.
     */
    public TreePopupMenu(PanelModel panelModel)
    {
        TreePopupMenuImpl selector = new TreePopupMenuImpl(panelModel);

        MenuItem removeAction = new MenuItem("Remove Items");
        removeAction.setOnAction(event ->  selector.remove(false));
        MenuItem deleteAction = new MenuItem("Delete Items");
        deleteAction.setOnAction(event -> selector.remove(true));
        getItems().addAll(removeAction, deleteAction);
    }
}
