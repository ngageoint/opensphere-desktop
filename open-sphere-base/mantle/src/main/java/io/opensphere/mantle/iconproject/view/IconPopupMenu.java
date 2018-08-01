package io.opensphere.mantle.iconproject.view;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.iconproject.impl.IconPopupMenuImpl;

/**
 * The Class IconPopup Menu.
 */
public class IconPopupMenu extends ContextMenu
{
    /**
     * The constructor for the class IconPopupMenu.
     * @param selectedIcon the selected icon
     */
    public IconPopupMenu(IconRecord selectedIcon)
    {
        IconPopupMenuImpl selector = new IconPopupMenuImpl(selectedIcon);

        MenuItem item1 = new MenuItem("Add to Favorites");
        item1.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                selector.addToFav();
            }
        });

        MenuItem item2 = new MenuItem("Rotate Icon");
        item2.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                selector.rotate();
            }
        });

        MenuItem item3 = new MenuItem("Delete Icon");
        item3.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                selector.delete();
            }
        });

        getItems().addAll(item1, item2, item3);
    }
}
