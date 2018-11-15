package io.opensphere.analysis.baseball;

import io.opensphere.core.util.lang.Pair;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;

/**
 * The row constructor for the data for the baseball card.
 */
public class BaseballDataRow extends ListCell<Pair<String, String>>
{
	/** The constant EMPTY_VALUE. */
    private final static String EMPTY_VALUE = "";

    /** The constant BACKGROUND. */
    private final static String BACKGROUND = "-fx-background-color: transparent";

    @Override
    protected void updateItem(Pair<String, String> item, boolean empty)
    {
        super.updateItem(item, empty);

        if (item == null)
        {
            setGraphic(null);
        }
        else
        {
            setGraphic(buildList(item));
        }
    }

    /**
     * Build a row in the data list.
     *
     * @param item the pair of strings to be added to the row
     * @return the new row
     */
    private Node buildList(Pair<String, String> item)
    {
        GridPane box = new GridPane();
        box.setHgap(5);
        TextField field1 = new TextField(item.getFirstObject());
        field1.setStyle(BACKGROUND);
        field1.setEditable(false);
        box.add(field1, 0, 0);
        String secondLabel = item.getSecondObject() == null ? EMPTY_VALUE : item.getSecondObject().toString();
        TextField field2 = new TextField(secondLabel);
        field2.setStyle(BACKGROUND);
        field2.setEditable(false);
        box.add(field2, 1, 0);
        ColumnConstraints column1 = new ColumnConstraints();
        column1.setPercentWidth(50);
        ColumnConstraints column2 = new ColumnConstraints();
        column2.setPercentWidth(50);
        box.getColumnConstraints().addAll(column1, column2);
        return box;
    }
}
