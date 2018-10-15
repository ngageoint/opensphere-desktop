package io.opensphere.mantle.iconproject.view;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

import java.awt.EventQueue;
import io.opensphere.mantle.iconproject.impl.IconPopupMenuImpl;
import io.opensphere.mantle.iconproject.model.PanelModel;

/**
 * A popup menu to be called from an icon button to perform an action such as
 * favorite,rotate,remove, and delete features. All actions are handled inside
 * this class.
 */
public class IconPopupMenu extends ContextMenu
{
    /**
     * The main method to instantiate a new IconPopupMenu.
     *
     * @param panelModel the model used to get registry items.
     */
    public IconPopupMenu(PanelModel panelModel)
    {
        IconPopupMenuImpl selector = new IconPopupMenuImpl(panelModel);

        MenuItem favAction = new MenuItem("Add Selected Icon(s) to Favorites");
        favAction.setOnAction(event -> selector.addToFav());

        MenuItem rotateAction = new MenuItem("Customize Icon");
        rotateAction.setOnAction(event -> EventQueue.invokeLater(() -> selector.customize()));

        MenuItem deleteAction = new MenuItem("Delete Selected Icon(s)");
        deleteAction.setOnAction(event -> selector.delete(true));

        MenuItem removeAction = new MenuItem("Remove Selected Icon(s)");
        removeAction.setOnAction(event -> selector.delete(false));

        MenuItem unSelectAction = new MenuItem("Deselect All Icons");
        unSelectAction.setOnAction(event -> selector.unSelectIcons());

        MenuItem deSelectAction = new MenuItem("Deselect Icon");
        deSelectAction.setOnAction(event -> EventQueue.invokeLater(() -> selector.unSelectIcon()));

        getItems().addAll(favAction, rotateAction, deSelectAction, unSelectAction, removeAction, deleteAction);
    }
}
