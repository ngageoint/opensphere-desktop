package io.opensphere.core.net.manager.view;

import io.opensphere.core.net.manager.model.HttpKeyValuePair;
import io.opensphere.core.util.fx.FXUtilities;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;

/** A simple list cell used to render a key / value pair. */
public class HeaderListCell extends ListCell<HttpKeyValuePair>
{
    /** The label in which the key is rendered. */
    private final Label myKeyLabel = new Label();

    /** The label in which the value is rendered. */
    private final Label myValueLabel = new Label();

    /** Creates a new cell. */
    public HeaderListCell()
    {
        myKeyLabel.setStyle("-fx-text-fill: #FCC;");
        myValueLabel.setStyle("-fx-text-fill: #CCF;");

        FXUtilities.runOnFXThread(() -> setGraphic(new HBox(5, myKeyLabel, myValueLabel)));
    }

    /**
     * {@inheritDoc}
     *
     * @see javafx.scene.control.Cell#updateItem(java.lang.Object, boolean)
     */
    @Override
    protected void updateItem(HttpKeyValuePair item, boolean empty)
    {
        super.updateItem(item, empty);
        if (empty)
        {
            myKeyLabel.setText("");
            myValueLabel.setText("");
        }
        else
        {
            myKeyLabel.setText(item.nameProperty().get() + ": ");
            myValueLabel.setText(item.valueProperty().get());
        }
    }
}
