package io.opensphere.wps.ui.detail.bbpicker;

import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

import io.opensphere.core.util.fx.FXUtilities;
import io.opensphere.core.util.image.IconUtil.IconType;

/**
 * A removable ListCell.
 *
 * @param <T> the type of the items
 */
public abstract class RemovableListCell<T> extends ListCell<T>
{
    /** The items model. */
    private final ObservableList<T> myItems;

    /** The box. */
    private final HBox myBox = new HBox(4);

    /**
     * Constructor.
     *
     * @param items the items model
     */
    public RemovableListCell(ObservableList<T> items)
    {
        myItems = items;
    }

    @Override
    public void updateItem(T item, boolean empty)
    {
        super.updateItem(item, empty);
        if (item != null && !empty)
        {
            updateItem(item);
            setGraphic(myBox);
        }
        else
        {
            setGraphic(null);
        }
    }

    /**
     * Method to handle updating the item.
     *
     * @param item the item
     */
    protected abstract void updateItem(T item);

    /**
     * Gets the box.
     *
     * @return the box
     */
    protected HBox getBox()
    {
        return myBox;
    }

    /**
     * Creates the remove button.
     *
     * @return the button
     */
    protected Button createRemoveButton()
    {
        Button button = FXUtilities.newIconButton(IconType.CLOSE, Color.RED);
        button.setTooltip(new Tooltip("Remove"));
        button.setOnAction(e -> myItems.remove(getIndex()));
        return button;
    }
}
