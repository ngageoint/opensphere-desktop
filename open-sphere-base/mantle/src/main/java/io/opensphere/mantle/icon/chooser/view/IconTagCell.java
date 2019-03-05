package io.opensphere.mantle.icon.chooser.view;

import javafx.scene.control.Label;
import javafx.scene.control.ListCell;

public class IconTagCell extends ListCell<String>
{
    @Override
    protected void updateItem(String item, boolean empty)
    {
        super.updateItem(item, empty);

        if (item == null)
        {
            setGraphic(null);
        }
        else
        {
            setGraphic(new Label(item));
        }
    }
}
