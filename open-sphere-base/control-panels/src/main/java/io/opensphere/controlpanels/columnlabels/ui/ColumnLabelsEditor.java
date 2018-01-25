package io.opensphere.controlpanels.columnlabels.ui;

import java.util.List;

import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;

import io.opensphere.controlpanels.columnlabels.controller.ColumnsController;
import io.opensphere.controlpanels.columnlabels.model.ColumnLabel;
import io.opensphere.controlpanels.columnlabels.model.ColumnLabels;
import io.opensphere.core.util.fx.FXUtilities;
import io.opensphere.core.util.image.IconUtil.IconType;

/**
 * A UI that allows the user to configure their column labels.
 */
public class ColumnLabelsEditor extends GridPane implements ColumnLabelsView
{
    /**
     * The add button.
     */
    private Button myAddButton;

    /**
     * The always show labels check box.
     */
    private CheckBox myAlwaysShowLabels;

    /**
     * Keeps the UI and model synchronized.
     */
    private final ColumnLabelsBinder myBinder;

    /**
     * The columns that are included in the label.
     */
    private ListView<ColumnLabel> myColumnLabels;

    /**
     * The model to edit.
     */
    private final ColumnLabels myModel;

    /**
     * The columns controller.
     */
    private final ColumnsController myController;

    /**
     * Constructs a new column label editor.
     *
     * @param model The model to edit.
     * @param availableColumns List of column names that can be in the label.
     */
    public ColumnLabelsEditor(ColumnLabels model, List<String> availableColumns)
    {
        myModel = model;
        myController = new ColumnsController(model, availableColumns);
        createUI();
        myBinder = new ColumnLabelsBinder(this, model);
    }

    /**
     * Creates the UI components.
     */
    private void createUI()
    {
        setHgap(5);
        setVgap(5);

        myAlwaysShowLabels = new CheckBox("Always Show Labels");
        myAlwaysShowLabels.setTooltip(new Tooltip("Always show labels.  If unchecked labels will only be shown on hover."));

        myAddButton = FXUtilities.newIconButton(null, IconType.PLUS, Color.LIME);
        myAddButton.setTooltip(new Tooltip("Add a label"));

        add(myAlwaysShowLabels, 0, 0);
        add(myAddButton, 1, 0);

        myColumnLabels = new ListView<>();
        myColumnLabels.setCellFactory(param -> new ColumnLabelRow(myModel));
        myColumnLabels.setPrefSize(400, 120);

        add(myColumnLabels, 0, 1, 3, 2);
    }

    /**
     * Stops editing the model.
     */
    public void close()
    {
        myBinder.close();
        myController.close();
    }

    @Override
    public Button getAddButton()
    {
        return myAddButton;
    }

    @Override
    public CheckBox getAlwaysShowLabels()
    {
        return myAlwaysShowLabels;
    }

    @Override
    public ListView<ColumnLabel> getColumnLabels()
    {
        return myColumnLabels;
    }
}
