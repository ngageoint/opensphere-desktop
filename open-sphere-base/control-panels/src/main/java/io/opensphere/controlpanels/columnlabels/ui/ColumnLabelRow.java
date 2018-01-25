package io.opensphere.controlpanels.columnlabels.ui;

import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;

import io.opensphere.controlpanels.columnlabels.model.ColumnLabel;
import io.opensphere.controlpanels.columnlabels.model.ColumnLabels;
import io.opensphere.core.util.fx.FXUtilities;
import io.opensphere.core.util.image.IconUtil.IconType;

/**
 * Represents one column in a list of columns that possibly make up the label.
 */
public class ColumnLabelRow extends ListCell<ColumnLabel> implements ColumnLabelRowView
{
    /**
     * Keeps the UI and the model synchronized.
     */
    private ColumnLabelRowBinder myBinder;

    /**
     * The column picker.
     */
    private ComboBox<String> myColumns;

    /**
     * The main column label model.
     */
    private final ColumnLabels myMainModel;

    /**
     * Moves the label down in the list.
     */
    private Button myMoveDownButton;

    /**
     * Moves this label up in the list.
     */
    private Button myMoveUpButton;

    /**
     * Contains all the UI components.
     */
    private GridPane myPane;

    /**
     * The remove button.
     */
    private Button myRemoveButton;

    /**
     * Turns the column name on or off in the label.
     */
    private CheckBox myShowColumnName;

    /**
     * Constructs a new column label row editor.
     *
     * @param mainModel The main column label model.
     */
    public ColumnLabelRow(ColumnLabels mainModel)
    {
        myMainModel = mainModel;
        createUI();
    }

    /**
     * Stops updating the model.
     */
    public void close()
    {
        if (myBinder != null)
        {
            myBinder.close();
        }
    }

    @Override
    public ComboBox<String> getColumns()
    {
        return myColumns;
    }

    @Override
    public Button getMoveDownButton()
    {
        return myMoveDownButton;
    }

    @Override
    public Button getMoveUpButton()
    {
        return myMoveUpButton;
    }

    @Override
    public Button getRemoveButton()
    {
        return myRemoveButton;
    }

    @Override
    public CheckBox getShowColumnName()
    {
        return myShowColumnName;
    }

    @Override
    protected void updateItem(ColumnLabel item, boolean empty)
    {
        super.updateItem(item, empty);
        if (item != null)
        {
            if (myBinder != null)
            {
                myBinder.close();
            }

            myBinder = new ColumnLabelRowBinder(this, myMainModel, item);
            setGraphic(myPane);
        }
        else
        {
            setGraphic(null);
        }
    }

    /**
     * Creates the UI components.
     */
    private void createUI()
    {
        myPane = new GridPane();
        myPane.setHgap(5);

        myShowColumnName = new CheckBox("Column");
        myShowColumnName.setTooltip(new Tooltip("Display the column with the value"));
        myColumns = new ComboBox<>();
        myColumns.setTooltip(new Tooltip("Sets the data field used for labels"));

        myMoveUpButton = FXUtilities.newIconButton(null, IconType.MOVE_UP, Color.CORNFLOWERBLUE);
        myMoveUpButton.setTooltip(new Tooltip("Move the label up"));
        myMoveDownButton = FXUtilities.newIconButton(null, IconType.MOVE_DOWN, Color.CORNFLOWERBLUE);
        myMoveDownButton.setTooltip(new Tooltip("Move the label down"));
        myRemoveButton = FXUtilities.newIconButton(null, IconType.CLOSE, Color.RED);
        myRemoveButton.setTooltip(new Tooltip("Removes this label"));

        myPane.add(myShowColumnName, 0, 0);
        myPane.add(myColumns, 1, 0);
        myPane.add(myMoveUpButton, 2, 0);
        myPane.add(myMoveDownButton, 3, 0);
        myPane.add(myRemoveButton, 4, 0);
    }
}
