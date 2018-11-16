package io.opensphere.analysis.baseball;

import io.opensphere.core.util.fx.FXUtilities;
import io.opensphere.mantle.data.element.DataElement;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Circle;

/**
 * The row constructor for DataElements for the baseball card.
 */
public class BaseballElementRow extends ListCell<DataElement>
{
    /** The DataElement the row is being built for. */
    private DataElement myDataElement;

    @Override
    protected void updateItem(DataElement item, boolean empty)
    {
        super.updateItem(item, empty);

        if (item == null)
        {
            setGraphic(null);
        }
        else
        {
            setGraphic(buildList(item));
            myDataElement = item;
        }
    }

    /**
     * Build a row in the DataElement list.
     *
     * @param item the DataElement to be added to the row
     * @return the new row
     */
    private Node buildList(DataElement item)
    {
        Circle colorCircle = new Circle();
        colorCircle.setFill(FXUtilities.fromAwtColor(item.getVisualizationState().getColor()));
        colorCircle.setRadius(10);
        Label myTime = new Label(item.getTimeSpan().toDisplayString());
        GridPane box = new GridPane();
        box.setHgap(5);
        box.add(colorCircle, 0, 0);
        box.add(myTime, 1, 0);
        return box;
    }

    /**
     * Gets the DataElement for this row.
     *
     * @return the DataElement
     */
    public DataElement getDataElement()
    {
        return myDataElement;
    }
}
