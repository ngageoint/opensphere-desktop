package io.opensphere.analysis.binning.editor.view;

import java.text.DecimalFormat;
import java.text.ParsePosition;

import io.opensphere.analysis.binning.criteria.BinCriteriaElement;
import io.opensphere.analysis.binning.editor.controller.CriterionController;
import io.opensphere.analysis.binning.editor.model.BinCriteriaModel;
import io.opensphere.analysis.binning.editor.model.CriterionModel;
import io.opensphere.core.util.fx.FXUtilities;
import io.opensphere.core.util.fx.NewAutoCompleteComboBoxListener;
import io.opensphere.core.util.image.IconUtil.IconType;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

/**
 * Represents one row in the criteria editor panel, showing the values for a
 * specific criteria.
 */
public class CriterionCell extends ListCell<BinCriteriaElement> implements CriterionCellView
{
    /** The bin type. */
    private final ComboBox<String> myBinType = new ComboBox<>();

    /** The horizontal layout box. */
    private final HBox myBox = new HBox(6);

    /**
     * The controller for this view.
     */
    private CriterionController myController;

    /**
     * The current binder.
     */
    private CriterionCellBinder myCurrentCellBinder;

    /** The field. */
    private final ComboBox<String> myField = new ComboBox<>();

    /**
     * The bin criteria model.
     */
    private final BinCriteriaModel myModel;

    /** The plus/minus text. */
    private final Label myPlusMinus = new Label("+/-");

    /**
     * The button that removes this criteria from the model.
     */
    private final Button myRemoveButton;

    /** The tolerance value. */
    private final TextField myTolerance = new TextField();

    /**
     * Constructs a new criterion cell UI.
     *
     * @param model The overall criteria model.
     */
    public CriterionCell(BinCriteriaModel model)
    {
        myModel = model;

        myField.setTooltip(new Tooltip("A field to use when grouping records"));
        myBinType.setTooltip(new Tooltip("How to compare values when grouping records"));
        initToleranceText();
        myRemoveButton = createRemoveButton();

        myBox.setAlignment(Pos.CENTER_LEFT);
        myBox.getChildren().addAll(myField, myBinType, myPlusMinus, myTolerance, FXUtilities.newHSpacer(), myRemoveButton);

        NewAutoCompleteComboBoxListener listener = new NewAutoCompleteComboBoxListener();
        listener.setupComboBox(myField);
    }

    @Override
    public ComboBox<String> getBinTypeBox()
    {
        return myBinType;
    }

    @Override
    public ComboBox<String> getFieldBox()
    {
        return myField;
    }

    @Override
    public Button getRemoveButton()
    {
        return myRemoveButton;
    }

    @Override
    public TextField getTolerance()
    {
        return myTolerance;
    }

    @Override
    public Label getToleranceLabel()
    {
        return myPlusMinus;
    }

    /**
     * Initializes the tolerance text field.
     */
    private void initToleranceText()
    {
        myTolerance.setTooltip(new Tooltip(""));
        myTolerance.setPrefWidth(80);
        DecimalFormat format = new DecimalFormat("#;");
        myTolerance.setTextFormatter(new TextFormatter<>(c ->
        {
            if (c.getControlNewText().isEmpty())
            {
                return c;
            }

            ParsePosition parsePosition = new ParsePosition(0);
            Object object = format.parse(c.getControlNewText(), parsePosition);

            if (object == null || parsePosition.getIndex() < c.getControlNewText().length())
            {
                return null;
            }
            else
            {
                return c;
            }
        }));
    }

    @Override
    protected void updateItem(BinCriteriaElement item, boolean empty)
    {
        FXUtilities.runOnFXThread(() ->
        {
            super.updateItem(item, empty);
            if (item != null)
            {
                if (myCurrentCellBinder != null)
                {
                    myCurrentCellBinder.close();
                }

                if (myController != null)
                {
                    myController.close();
                }

                CriterionModel criterionModel = new CriterionModel(item);
                myCurrentCellBinder = new CriterionCellBinder(this, criterionModel, myModel);
                myController = new CriterionController(myModel, criterionModel);
                setGraphic(myBox);
            }
            else
            {
                setGraphic(null);
            }
        });
    }

    /**
     * Creates the remove button.
     *
     * @return the button
     */
    private Button createRemoveButton()
    {
        Button button = FXUtilities.newIconButton(IconType.CLOSE, Color.RED);
        button.setTooltip(new Tooltip("Remove"));
        return button;
    }
}
