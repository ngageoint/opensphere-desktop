package io.opensphere.mantle.iconproject.view;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

import java.awt.EventQueue;

import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.iconproject.impl.IconPopupMenuImpl;
import io.opensphere.mantle.iconproject.model.PanelModel;

/**
 * The Class IconPopup Menu.
 */
public class IconPopupMenu extends ContextMenu
{
    /**
     * The constructor for the class IconPopupMenu.
     * 
     * @param selectedIcon the selected icon
     */
    public IconPopupMenu(PanelModel thePanelModel)
    {
        IconPopupMenuImpl selector = new IconPopupMenuImpl(thePanelModel);

        MenuItem favAction = new MenuItem("Add to Favorites");
        favAction.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                selector.addToFav();
            }
        });

        MenuItem rotateAction = new MenuItem("Rotate Icon");
        rotateAction.setOnAction(event ->
        {
            EventQueue.invokeLater(() ->
            {
                selector.rotate();
            });
        });

        MenuItem deleteAction = new MenuItem("Delete Icon");
        deleteAction.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                selector.delete(true);
            }
        });

        MenuItem removeAction = new MenuItem("Remove Icon");
        removeAction.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                selector.delete(false);
            }
        });

        getItems().addAll(favAction, rotateAction, deleteAction, removeAction);
    }
}
