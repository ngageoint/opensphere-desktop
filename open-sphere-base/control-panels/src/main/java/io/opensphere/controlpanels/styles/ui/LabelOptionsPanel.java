package io.opensphere.controlpanels.styles.ui;

import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;

import java.util.LinkedList;
import java.util.List;

import io.opensphere.controlpanels.columnlabels.ui.ColumnLabelsEditor;
import io.opensphere.controlpanels.styles.controller.LabelOptionsController;
import io.opensphere.controlpanels.styles.model.LabelOptions;

/**
 * The panel that allows the user to change the bulls eyes label content and
 * look.
 */
public class LabelOptionsPanel extends GridPane implements LabelOptionsView
{
    /**
     * Binds the ui to the model.
     */
    private final LabelOptionsBinder myBinder;

    /**
     * The color picker.
     */
    private ColorPicker myColorPicker;

    /**
     * The model.
     */
    private final LabelOptions myModel;

    /**
     * The size picker.
     */
    private Spinner<Integer> mySizePicker;

    /**
     * The label options controller.
     */
    private final LabelOptionsController myController;

    /**
     * Constructs a new label options panel.
     *
     * @param model The model to edit.
     * @param controller Provides the available columns to label.
     * @param isNew True if this is a create, false if this is an edit.
     */
    public LabelOptionsPanel(LabelOptions model, LabelOptionsController controller, boolean isNew)
    {
        myModel = model;
        myController = controller;
        createUI();
        myBinder = new LabelOptionsBinder(this, myModel);
    }

    /**
     * Construct with a specified list of column names.
     * @param model the model to edit
     * @param columns the column names
     */
    public LabelOptionsPanel(LabelOptions model, List<String> columns)
    {
        myModel = model;
        myController = new DefaultController(columns);
        createUI();
        myBinder = new LabelOptionsBinder(this, myModel);
    }

    /**
     * Default implementation of a LabelOptionsController which can be
     * constructed with a specific list of column names.
     */
    private static class DefaultController implements LabelOptionsController
    {
        /** The column names. */
        private List<String> columns;

        /**
         * Construct with specified column names.
         * @param cols the column names
         */
        public DefaultController(List<String> cols)
        {
            columns = cols;
        }

        @Override
        public List<String> getColumns()
        {
            return new LinkedList<>(columns);
        }
    }

    /**
     * Stops updating the model.
     */
    public void close()
    {
        myBinder.close();
    }

    @Override
    public ColorPicker getColorPicker()
    {
        return myColorPicker;
    }

    @Override
    public Spinner<Integer> getSizePicker()
    {
        return mySizePicker;
    }

    /**
     * Creates the ui components.
     */
    private void createUI()
    {
        setVgap(5);
        setHgap(5);

        Label colorLabel = new Label("Color:");
        myColorPicker = new ColorPicker();
        myColorPicker.setStyle("-fx-color-label-visible: false ;");
        myColorPicker.setTooltip(new Tooltip("Sets the label color"));

        add(colorLabel, 0, 0);
        add(myColorPicker, 1, 0);

        Label sizeLabel = new Label("Size:");
        mySizePicker = new Spinner<>(8, 40, 12);
        mySizePicker.setTooltip(new Tooltip("Sets the label font size."));

        add(sizeLabel, 0, 1);
        add(mySizePicker, 1, 1);

        ColumnLabelsEditor columnLabels = new ColumnLabelsEditor(myModel.getColumnLabels(), myController.getColumns());
        add(columnLabels, 0, 2, 4, 1);
    }
}
