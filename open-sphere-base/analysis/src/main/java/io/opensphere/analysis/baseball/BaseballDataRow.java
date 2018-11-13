package io.opensphere.analysis.baseball;

import io.opensphere.core.util.lang.Pair;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;

public class BaseballDataRow extends ListCell<Pair<String, String>>
{
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

    private Node buildList(Pair<String, String> item)
    {
        GridPane box = new GridPane();
        box.setHgap(5);
        box.add(new Label(item.getFirstObject()), 0, 0);
        String secondLabel = item.getSecondObject() == null ? "" : item.getSecondObject().toString();
        box.add(new Label(secondLabel), 1, 0);
        ColumnConstraints column1 = new ColumnConstraints();
        column1.setPercentWidth(50);
        ColumnConstraints column2 = new ColumnConstraints();
        column2.setPercentWidth(50);
        box.getColumnConstraints().addAll(column1, column2);
        box.setHgap(5);
        return box;
    }
}
